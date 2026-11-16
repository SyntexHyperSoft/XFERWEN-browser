package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberMatrixGreen
import com.example.ui.theme.CyberNeonBlue
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberViolet
import com.example.ui.theme.CyberVoid

/**
 * Cyber Address Bar with animated neon glow, SSL lock, search input,
 * Milana AI Cyber Shield badge, Tab counter, and action menu.
 * Fully optimized to prevent elements from overlapping or clipping.
 */
@Composable
fun CyberAddressBar(
    currentUrl: String,
    pageTitle: String,
    isLoading: Boolean,
    progress: Int,
    totalBlocked: Int,
    tabCount: Int,
    isDesktopMode: Boolean,
    onNavigate: (String) -> Unit,
    onReload: () -> Unit,
    onOpenShield: () -> Unit,
    onOpenTabs: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenBookmarks: () -> Unit,
    onOpenPasswords: () -> Unit,
    onOpenSettings: () -> Unit,
    onGoHome: () -> Unit,
    onToggleDesktop: () -> Unit,
    onMakeDefaultBrowser: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Animated glow border
    val transition = rememberInfiniteTransition(label = "border_glow")
    val gradientOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "glow_offset"
    )

    val animatedBorder = Brush.linearGradient(
        colors = listOf(
            CyberCyan.copy(alpha = 0.85f),
            CyberNeonBlue.copy(alpha = 0.7f),
            CyberViolet.copy(alpha = 0.7f),
            CyberCyan.copy(alpha = 0.85f)
        ),
        start = androidx.compose.ui.geometry.Offset(gradientOffset, 0f),
        end = androidx.compose.ui.geometry.Offset(gradientOffset + 500f, 200f)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CyberSurface)
    ) {
        if (isEditing) {
            // EDITING MODE: Dedicated full-width input row so nothing overlaps
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cancel / Back button
                IconButton(
                    onClick = { isEditing = false },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Cancel",
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Text field container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .clip(RoundedCornerShape(21.dp))
                        .background(CyberSurfaceVariant)
                        .border(1.2.dp, animatedBorder, RoundedCornerShape(21.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                        }

                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
                                .testTag("url_text_input"),
                            textStyle = TextStyle(
                                color = CyberTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(CyberCyan),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Go
                            ),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    if (inputText.isNotBlank()) {
                                        onNavigate(inputText)
                                    }
                                    isEditing = false
                                }
                            )
                        )

                        if (inputText.isNotEmpty()) {
                            IconButton(
                                onClick = { inputText = "" },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = CyberTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Go Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CyberCyan)
                        .clickable {
                            if (inputText.isNotBlank()) {
                                onNavigate(inputText)
                            }
                            isEditing = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Go",
                        tint = CyberVoid,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            // NORMAL BROWSING MODE: Cleanly spaced controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Button
                IconButton(
                    onClick = onGoHome,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("home_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = CyberCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(2.dp))

                // Central Address Bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(CyberSurfaceVariant)
                        .border(1.dp, animatedBorder, RoundedCornerShape(20.dp))
                        .clickable {
                            inputText = if (currentUrl == "about:blank") "" else currentUrl
                            isEditing = true
                        }
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val isFas = currentUrl.startsWith("fas://", ignoreCase = true)

                        Icon(
                            imageVector = when {
                                isFas -> Icons.Default.Security
                                currentUrl.startsWith("https") -> Icons.Default.Lock
                                else -> Icons.Default.Search
                            },
                            contentDescription = null,
                            tint = when {
                                isFas -> CyberCyan
                                currentUrl.startsWith("https") -> CyberMatrixGreen
                                else -> CyberCyan
                            },
                            modifier = Modifier.size(14.dp)
                        )

                        if (isFas) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CyberCyan.copy(alpha = 0.2f))
                                    .border(0.8.dp, CyberCyan, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "FAS",
                                    color = CyberCyan,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = if (currentUrl.isBlank() || currentUrl == "about:blank") "Яндекс или FAS://..." else getCleanDisplayUrl(currentUrl),
                            color = if (isFas) CyberCyan else if (currentUrl.isBlank()) CyberTextMuted else CyberTextPrimary,
                            fontWeight = if (isFas) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        // CYBER SHIELD BADGE (Right next to URL, showing blocked count)
                        CyberShieldBadge(
                            totalBlocked = totalBlocked,
                            onClick = onOpenShield
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Refresh / Reload Button
                IconButton(
                    onClick = onReload,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("reload_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload",
                        tint = CyberCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Tabs Counter Button
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .clickable(onClick = onOpenTabs)
                        .border(1.dp, CyberCyan.copy(alpha = 0.7f), RoundedCornerShape(7.dp))
                        .testTag("tabs_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$tabCount",
                        color = CyberCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Overflow Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("overflow_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            tint = CyberTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .background(CyberSurfaceVariant)
                            .border(1.dp, CyberCardBorder, RoundedCornerShape(8.dp))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Кибер-Защита Milana AI", color = CyberCyan, fontWeight = FontWeight.Bold) },
                            onClick = {
                                showMenu = false
                                onOpenShield()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Вкладки ($tabCount)", color = CyberTextPrimary) },
                            onClick = {
                                showMenu = false
                                onOpenTabs()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Закладки", color = CyberTextPrimary) },
                            onClick = {
                                showMenu = false
                                onOpenBookmarks()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("История", color = CyberTextPrimary) },
                            onClick = {
                                showMenu = false
                                onOpenHistory()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🔐 Менеджер паролей", color = CyberMatrixGreen, fontWeight = FontWeight.SemiBold) },
                            onClick = {
                                showMenu = false
                                onOpenPasswords()
                            }
                        )
                        HorizontalDivider(color = CyberCardBorder)
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (isDesktopMode) "✓ Версия для ПК" else "Версия для ПК",
                                    color = CyberTextPrimary
                                )
                            },
                            onClick = {
                                showMenu = false
                                onToggleDesktop()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Сделать браузером по умолчанию", color = CyberCyan) },
                            onClick = {
                                showMenu = false
                                onMakeDefaultBrowser()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Настройки", color = CyberTextSecondary) },
                            onClick = {
                                showMenu = false
                                onOpenSettings()
                            }
                        )
                    }
                }
            }
        }

        // Animated Cyber Loading Progress Bar
        AnimatedVisibility(visible = isLoading && progress in 1..99) {
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = CyberCyan,
                trackColor = CyberVoid
            )
        }
    }
}

private fun getCleanDisplayUrl(url: String): String {
    if (url.startsWith("fas://", ignoreCase = true)) {
        return url
    }
    return try {
        val uri = android.net.Uri.parse(url)
        uri.host ?: url
    } catch (_: Exception) {
        url
    }
}
