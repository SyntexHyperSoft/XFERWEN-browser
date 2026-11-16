package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DnsMode
import com.example.data.model.ProtectionStats
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDanger
import com.example.ui.theme.CyberMatrixGreen
import com.example.ui.theme.CyberNeonBlue
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberVoid
import com.example.ui.theme.CyberWarning

/**
 * Milana AI Cyber Security modal bottom sheet.
 * Features live blocking statistics, DNS selection, and mode change warnings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilanaAiBottomSheet(
    sheetState: SheetState,
    stats: ProtectionStats,
    currentMode: DnsMode,
    onModeChange: (DnsMode) -> Unit,
    onDismiss: () -> Unit
) {
    var pendingMode by remember { mutableStateOf<DnsMode?>(null) }
    var showWarningDialog by remember { mutableStateOf(false) }

    // Pulsating shield animation
    val infiniteTransition = rememberInfiniteTransition(label = "milana_pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CyberSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(CyberCyan.copy(alpha = 0.6f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Glowing Cyber Shield Icon with animated ring
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(pulseGlow)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(CyberCyan.copy(alpha = 0.35f), CyberNeonBlue.copy(alpha = 0.05f), Color.Transparent)
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.sweepGradient(
                            listOf(CyberCyan, CyberNeonBlue, CyberMatrixGreen, CyberCyan)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Milana AI",
                    tint = CyberCyan,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Header Title: Milana AI
            Text(
                text = "Milana AI",
                color = CyberTextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle text required by prompt:
            // "Я защищаю вашу конфидициальность в интернете и помогаю обходить блокировки"
            Text(
                text = "Я защищаю вашу конфидициальность в интернете и помогаю обходить блокировки",
                color = CyberCyan,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Live Blocking Statistics Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        brush = Brush.horizontalGradient(
                            listOf(CyberCardBorder, CyberCyan.copy(alpha = 0.4f), CyberCardBorder)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = CyberMatrixGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Статистика фильтрации",
                            color = CyberTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Всего: ${stats.totalBlocked}",
                            color = CyberMatrixGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = CyberCardBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Line 1: Заблокировано рекламы: N
                    StatRow(
                        label = "Заблокировано рекламы:",
                        value = "${stats.blockedAds}",
                        valueColor = CyberCyan
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Line 2: Заблокировано Трекеров: N
                    StatRow(
                        label = "Заблокировано Трекеров:",
                        value = "${stats.blockedTrackers}",
                        valueColor = CyberMatrixGreen
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Line 3: Заблокировано Фишинговых сайтов: N
                    StatRow(
                        label = "Заблокировано Фишинговых сайтов:",
                        value = "${stats.blockedPhishing}",
                        valueColor = CyberDanger
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active Shield status badge (no DNS URLs)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CyberSurfaceVariant)
                    .border(1.dp, CyberCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(CyberMatrixGreen)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (currentMode == DnsMode.SYNTEXT_BYPASS)
                            "Активна нейросеть: Milana V3.2 Flash"
                        else
                            "Активна нейросеть: Milana V3.2 Ultra",
                        color = CyberTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Киберщит активен • Защита конфиденциальности в сети",
                        color = CyberMatrixGreen,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section: Режимы
            Text(
                text = "Режимы",
                color = CyberTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle: Сделать упор на:
            Text(
                text = "Сделать упор на:",
                color = CyberTextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Option 1: Обход блокировок + Милана V3.2 Flash
            DnsModeCard(
                title = "Обход блокировок + Милана V3.2 Flash",
                subtitle = "Нейросеть Milana V3.2 Flash",
                detail = "Разблокировка ChatGPT, Gemini, Claude, YouTube и сервисов + нейрофильтрация рекламы.",
                isSelected = currentMode == DnsMode.SYNTEXT_BYPASS,
                accentColor = CyberCyan,
                badgeText = "По умолчанию",
                onClick = {
                    if (currentMode != DnsMode.SYNTEXT_BYPASS) {
                        pendingMode = DnsMode.SYNTEXT_BYPASS
                        showWarningDialog = true
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Option 2: Максимальная защита с Милана V3.2 Ultra
            DnsModeCard(
                title = "Максимальная защита с Милана V3.2 Ultra",
                subtitle = "Нейросеть Milana V3.2 Ultra",
                detail = "Старшая сестра Милана V3.2 Ultra. Максимальная блокировка рекламы, трекеров, спама и баннеров.",
                isSelected = currentMode == DnsMode.MILANA_AI_MAX_SHIELD,
                accentColor = CyberMatrixGreen,
                badgeText = "Без рекламы",
                onClick = {
                    if (currentMode != DnsMode.MILANA_AI_MAX_SHIELD) {
                        pendingMode = DnsMode.MILANA_AI_MAX_SHIELD
                        showWarningDialog = true
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Active Protections Quick Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProtectionPill(
                    icon = Icons.Default.Lock,
                    label = "Do Not Track",
                    value = "Включен",
                    modifier = Modifier.weight(1f)
                )
                ProtectionPill(
                    icon = Icons.Default.Speed,
                    label = "Vulkan Direct",
                    value = "Активен",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Confirmation Alert Dialog when switching modes
    if (showWarningDialog && pendingMode != null) {
        val target = pendingMode!!
        val isSwitchingToBypass = (target == DnsMode.SYNTEXT_BYPASS)

        val warningTitle = "Внимание"
        val warningMessage = if (isSwitchingToBypass) {
            "Вы уверены что хотите переключить с режима \"Максимальная защита с Милана V3.2 Ultra\" на \"Обход блокировок + Милана V3.2 Flash\"? Ваша конфидициальность может снизиться и может появится реклама и не блокироваться все трекеры,но при этом станут доступны некоторые сервисы за счет обхода блокировок?"
        } else {
            "Вы уверены что хотите переключится с режима \"Обход блокировок + Милана V3.2 Flash\" на \"Максимальная защита с Милана V3.2 Ultra\"? Это поможет убрать надоедливаю рекламу и улучшить вашу конфиденциальность,но возможно что заблокированные сервисы перестанут работать"
        }

        AlertDialog(
            onDismissRequest = {
                showWarningDialog = false
                pendingMode = null
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = CyberWarning,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = warningTitle,
                    color = CyberTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = warningMessage,
                    color = CyberTextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onModeChange(target)
                        showWarningDialog = false
                        pendingMode = null
                    },
                    modifier = Modifier.testTag("dialog_confirm_button")
                ) {
                    Text(
                        text = "Подтвердить",
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showWarningDialog = false
                        pendingMode = null
                    },
                    modifier = Modifier.testTag("dialog_cancel_button")
                ) {
                    Text(
                        text = "Отмена",
                        color = CyberTextMuted
                    )
                }
            },
            containerColor = CyberSurfaceVariant,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun StatRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = CyberTextSecondary,
            fontSize = 13.sp
        )
        Text(
            text = value,
            color = valueColor,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun DnsModeCard(
    title: String,
    subtitle: String,
    detail: String,
    isSelected: Boolean,
    accentColor: Color,
    badgeText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) accentColor else CyberCardBorder,
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CyberSurfaceVariant.copy(alpha = 0.95f) else CyberVoid
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = accentColor,
                        unselectedColor = CyberTextMuted
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            color = if (isSelected) CyberTextPrimary else CyberTextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = subtitle,
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Text(
                text = detail,
                color = CyberTextMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(start = 38.dp, top = 2.dp)
            )
        }
    }
}

@Composable
private fun ProtectionPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CyberVoid)
            .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CyberCyan,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(text = label, color = CyberTextMuted, fontSize = 10.sp)
                Text(text = value, color = CyberMatrixGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
