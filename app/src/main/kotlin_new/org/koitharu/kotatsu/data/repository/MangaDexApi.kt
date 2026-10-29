package org.koitharu.kotatsu.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.koitharu.kotatsu.data.model.Chapter
import org.koitharu.kotatsu.data.model.Manga
import org.koitharu.kotatsu.data.model.MangaPage
import org.koitharu.kotatsu.data.model.MangaStatus
import java.util.concurrent.TimeUnit

class MangaDexApi {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://api.mangadex.org"
    private val uploadsBaseUrl = "https://uploads.mangadex.org"

    suspend fun fetchPopularManga(limit: Int = 20): List<Manga> = withContext(Dispatchers.IO) {
        val url = "$baseUrl/manga?limit=$limit&includes[]=cover_art&order[followedCount]=desc&contentRating[]=safe&contentRating[]=suggestive"
        executeMangaListRequest(url)
    }

    suspend fun fetchLatestManga(limit: Int = 20): List<Manga> = withContext(Dispatchers.IO) {
        val url = "$baseUrl/manga?limit=$limit&includes[]=cover_art&order[latestUploadedChapter]=desc&contentRating[]=safe&contentRating[]=suggestive"
        executeMangaListRequest(url)
    }

    suspend fun searchManga(query: String, limit: Int = 20): List<Manga> = withContext(Dispatchers.IO) {
        val encoded = java.net.URLEncoder.encode(query, "UTF-8")
        val url = "$baseUrl/manga?title=$encoded&limit=$limit&includes[]=cover_art&contentRating[]=safe&contentRating[]=suggestive"
        executeMangaListRequest(url)
    }

    private fun executeMangaListRequest(url: String): List<Manga> {
        return try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return emptyList()

            val body = response.body?.string() ?: return emptyList()
            val json = JSONObject(body)
            val dataArray = json.optJSONArray("data") ?: return emptyList()

            val result = mutableListOf<Manga>()
            for (i in 0 until dataArray.length()) {
                val item = dataArray.optJSONObject(i) ?: continue
                val id = item.optString("id", "")
                val attrs = item.optJSONObject("attributes") ?: JSONObject()
                val titleObj = attrs.optJSONObject("title") ?: JSONObject()
                val title = titleObj.optString("en", titleObj.optString("ja", "Manga #$i"))

                val descObj = attrs.optJSONObject("description") ?: JSONObject()
                val desc = descObj.optString("en", "No description available.")

                val statusStr = attrs.optString("status", "ongoing")
                val status = when (statusStr.lowercase()) {
                    "completed" -> MangaStatus.COMPLETED
                    "cancelled" -> MangaStatus.SUSPENDED
                    else -> MangaStatus.ONGOING
                }

                val tagsArray = attrs.optJSONArray("tags")
                val genres = mutableListOf<String>()
                if (tagsArray != null) {
                    for (t in 0 until tagsArray.length()) {
                        val tagObj = tagsArray.optJSONObject(t)
                        val tagAttrs = tagObj?.optJSONObject("attributes")
                        val tagName = tagAttrs?.optJSONObject("name")?.optString("en")
                        if (!tagName.isNullOrBlank()) {
                            genres.add(tagName)
                        }
                    }
                }

                // Find cover art file name from relationships
                var coverFileName = ""
                val rels = item.optJSONArray("relationships")
                if (rels != null) {
                    for (r in 0 until rels.length()) {
                        val rel = rels.optJSONObject(r)
                        if (rel?.optString("type") == "cover_art") {
                            val relAttrs = rel.optJSONObject("attributes")
                            coverFileName = relAttrs?.optString("fileName", "") ?: ""
                        }
                    }
                }

                val coverUrl = if (coverFileName.isNotEmpty()) {
                    "$uploadsBaseUrl/covers/$id/$coverFileName.256.jpg"
                } else {
                    "https://picsum.photos/seed/$id/300/450"
                }

                result.add(
                    Manga(
                        id = id,
                        title = title,
                        description = desc,
                        coverUrl = coverUrl,
                        status = status,
                        genres = genres.take(4),
                        source = "MangaDex",
                        rating = 4.7f,
                        totalChapters = 0
                    )
                )
            }
            result
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun fetchChapters(mangaId: String): List<Chapter> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/manga/$mangaId/feed?translatedLanguage[]=en&order[chapter]=asc&limit=100"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val body = response.body?.string() ?: return@withContext emptyList()
            val json = JSONObject(body)
            val dataArray = json.optJSONArray("data") ?: return@withContext emptyList()

            val chapters = mutableListOf<Chapter>()
            for (i in 0 until dataArray.length()) {
                val item = dataArray.optJSONObject(i) ?: continue
                val id = item.optString("id", "")
                val attrs = item.optJSONObject("attributes") ?: JSONObject()
                val chNumberStr = attrs.optString("chapter", (i + 1).toString())
                val chNumber = chNumberStr.toFloatOrNull() ?: (i + 1).toFloat()
                val chTitle = attrs.optString("title", "").ifEmpty { "Chapter $chNumberStr" }
                val pages = attrs.optInt("pages", 16)
                val publishAt = attrs.optString("publishAt", "Recent").take(10)

                chapters.add(
                    Chapter(
                        id = id,
                        mangaId = mangaId,
                        number = chNumber,
                        title = chTitle,
                        uploadDate = publishAt,
                        pageCount = if (pages > 0) pages else 16
                    )
                )
            }
            chapters
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun fetchChapterPages(chapterId: String): List<MangaPage> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/at-home/server/$chapterId"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext emptyList()

            val body = response.body?.string() ?: return@withContext emptyList()
            val json = JSONObject(body)
            val baseUrlServer = json.optString("baseUrl", "https://uploads.mangadex.org")
            val chapterObj = json.optJSONObject("chapter") ?: return@withContext emptyList()
            val hash = chapterObj.optString("hash", "")
            val dataArray = chapterObj.optJSONArray("data") ?: return@withContext emptyList()

            val pages = mutableListOf<MangaPage>()
            for (i in 0 until dataArray.length()) {
                val fileName = dataArray.optString(i, "")
                if (fileName.isNotEmpty()) {
                    pages.add(
                        MangaPage(
                            pageNumber = i + 1,
                            imageUrl = "$baseUrlServer/data/$hash/$fileName"
                        )
                    )
                }
            }
            pages
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
