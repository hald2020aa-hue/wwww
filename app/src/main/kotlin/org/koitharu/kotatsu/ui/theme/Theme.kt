package org.koitharu.kotatsu.ui.theme

import androidx.compose.runtime.Composable

@Composable
fun KotatsuTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SSSXTheme(content = content)
}
