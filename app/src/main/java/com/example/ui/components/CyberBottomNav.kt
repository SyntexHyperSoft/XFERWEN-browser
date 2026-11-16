package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberMatrixGreen
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary

@Composable
fun CyberBottomNav(
    canGoBack: Boolean,
    canGoForward: Boolean,
    tabCount: Int,
    totalBlocked: Int,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onOpenShield: () -> Unit,
    onBookmark: () -> Unit,
    onOpenTabs: () -> Unit,
    onNewTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(CyberSurface)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .border(
                width = 0.8.dp,
                color = CyberCardBorder.copy(alpha = 0.6f)
            )
            .height(52.dp)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back
            IconButton(
                onClick = onBack,
                enabled = canGoBack,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("nav_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = if (canGoBack) CyberCyan else CyberTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Forward
            IconButton(
                onClick = onForward,
                enabled = canGoForward,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("nav_forward_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Forward",
                    tint = if (canGoForward) CyberCyan else CyberTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Milana AI Shield shortcut
            IconButton(
                onClick = onOpenShield,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("nav_shield_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Milana AI",
                    tint = if (totalBlocked > 0) CyberMatrixGreen else CyberCyan,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Bookmark current page
            IconButton(
                onClick = onBookmark,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("nav_bookmark_button")
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = CyberCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            // New Tab
            IconButton(
                onClick = onNewTab,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("nav_new_tab_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Tab",
                    tint = CyberCyan,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
