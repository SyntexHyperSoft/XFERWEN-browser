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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DnsMode
import com.example.data.model.SearchEngine
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
 * 3-step cyber onboarding wizard providing exact user prompts:
 * Step 1: Welcome to XFERWEN
 * Step 2: Milana V3.2 Flash introduction and sister Milana V3.2 Ultra selection
 * Step 3: Purpose of browser (Web surfing search engine OR SyntexHyperSoft architecture)
 */
@Composable
fun OnboardingWizardDialog(
    initialMode: DnsMode,
    initialSearchEngine: SearchEngine,
    onComplete: (mode: DnsMode, searchEngine: SearchEngine, useSyntexArch: Boolean) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var selectedMode by remember { mutableStateOf(initialMode) }
    var selectedSearchEngine by remember { mutableStateOf(initialSearchEngine) }
    var chosenPurpose by remember { mutableStateOf<String?>(null) }
    var showSyntexConfirmModal by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "onboard_glow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Dialog(
        onDismissRequest = { /* Modal: Must complete setup */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(listOf(CyberCyan, CyberNeonBlue, CyberViolet)),
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("onboarding_wizard_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated Glowing Emblem
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(CyberCyan.copy(alpha = 0.35f), CyberNeonBlue.copy(alpha = 0.1f), Color.Transparent)
                            )
                        )
                        .border(
                            width = 1.5.dp,
                            brush = Brush.sweepGradient(listOf(CyberCyan, CyberNeonBlue, CyberMatrixGreen, CyberCyan)),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (step) {
                            1 -> Icons.Default.Shield
                            2 -> Icons.Default.AutoAwesome
                            else -> Icons.Default.Language
                        },
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                when (step) {
                    1 -> {
                        // ШАГ 1: Приветствие
                        Text(
                            text = "Добро пожаловать в XFERWEN",
                            color = CyberTextPrimary,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Технологичный браузер от SyntexHyperSoft с нейросетью Milana (Милана) V3.2 Flash для блокировки рекламы и трекеров и с обходом блокировок для ChatGPT,Claude,Gemini и других сервисов (НЕ ВСЕ МОГУТ РАБОТАТЬ ПО ПРЕЖНЕМУ!)",
                            color = CyberTextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { step = 2 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyan,
                                contentColor = CyberVoid
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("onboarding_step1_next_btn")
                        ) {
                            Text(
                                text = "Продолжить",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    2 -> {
                        // ШАГ 2: Милана V3.2 Flash и выбор режимов
                        Text(
                            text = "Привет,я Милана V3.2 Flash!",
                            color = CyberCyan,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Я тебе буду помогать в интернете защищая твою конфидициальность,ты можешь выбрать чтобы вместо меня работала моя Старшая сестра \"Милана V3.2 Ultra\" которая лучше будет блокировать рекламу и трекеры,но к сожалению ты сможешь потерять доступ к некоторым сайтам(т.к обход блокировок не будет работать),так что выберай сам",
                            color = CyberTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Режим 1: Обход блокировок + Милана V3.2 Flash
                        OnboardingModeOptionCard(
                            title = "Обход блокировок + Милана V3.2 Flash",
                            subtitle = "Рекомендуемый • Обход ChatGPT, Claude, Gemini",
                            isSelected = selectedMode == DnsMode.SYNTEXT_BYPASS,
                            color = CyberCyan,
                            onClick = { selectedMode = DnsMode.SYNTEXT_BYPASS }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Режим 2: Максимальная защита с Милана V3.2 Ultra
                        OnboardingModeOptionCard(
                            title = "Максимальная защита с Милана V3.2 Ultra",
                            subtitle = "Старшая сестра • Максимум блокировки рекламы",
                            isSelected = selectedMode == DnsMode.MILANA_AI_MAX_SHIELD,
                            color = CyberMatrixGreen,
                            onClick = { selectedMode = DnsMode.MILANA_AI_MAX_SHIELD }
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        Button(
                            onClick = { step = 3 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyberCyan,
                                contentColor = CyberVoid
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("onboarding_step2_next_btn")
                        ) {
                            Text(
                                text = "Продолжить",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    3 -> {
                        // ШАГ 3: Для чего вы выбрали браузер?
                        Text(
                            text = "Для чего вы выбрали браузер?",
                            color = CyberTextPrimary,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Выбор А: Для безопасного и быстрого серфинга в интернете
                        OnboardingPurposeCard(
                            title = "Для безопастного и быстрого серфинга в интернете",
                            subtitle = "Классический веб-серфинг с блокировкой угроз и выбором поисковой системы",
                            isSelected = chosenPurpose == "WEB",
                            color = CyberCyan,
                            onClick = {
                                chosenPurpose = "WEB"
                            }
                        )

                        // If WEB chosen, show search engine selection
                        AnimatedVisibility(visible = chosenPurpose == "WEB") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(CyberSurfaceVariant)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Выберите поисковую систему:",
                                    color = CyberCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    SearchEngineChip(
                                        title = "Яндекс",
                                        isSelected = selectedSearchEngine == SearchEngine.YANDEX,
                                        onClick = { selectedSearchEngine = SearchEngine.YANDEX },
                                        modifier = Modifier.weight(1f)
                                    )
                                    SearchEngineChip(
                                        title = "Google",
                                        isSelected = selectedSearchEngine == SearchEngine.GOOGLE,
                                        onClick = { selectedSearchEngine = SearchEngine.GOOGLE },
                                        modifier = Modifier.weight(1f)
                                    )
                                    SearchEngineChip(
                                        title = "DuckDuckGO",
                                        isSelected = selectedSearchEngine == SearchEngine.DUCKDUCKGO,
                                        onClick = { selectedSearchEngine = SearchEngine.DUCKDUCKGO },
                                        modifier = Modifier.weight(1.1f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        onComplete(selectedMode, selectedSearchEngine, false)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CyberCyan,
                                        contentColor = CyberVoid
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                ) {
                                    Text(
                                        text = "Завершить и начать",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Выбор Б: Для просмотра сайтов от SyntexHyperSoft и других проверенных сайтов
                        OnboardingPurposeCard(
                            title = "Для просмотра сайтов от SyntexHyperSoft и других проверенных сайтов от других разработчиков",
                            subtitle = "Прямой доступ к интернет-датацентру SyntexHyperSoft и проверенным серверам",
                            isSelected = chosenPurpose == "SYNTEX",
                            color = CyberNeonBlue,
                            onClick = {
                                chosenPurpose = "SYNTEX"
                                showSyntexConfirmModal = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal popup confirmation for SyntexHyperSoft architecture choice
    if (showSyntexConfirmModal) {
        AlertDialog(
            onDismissRequest = { /* Must click Ok */ },
            containerColor = CyberSurfaceVariant,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.border(
                1.5.dp,
                Brush.horizontalGradient(listOf(CyberCyan, CyberNeonBlue)),
                RoundedCornerShape(18.dp)
            ),
            icon = {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Архитектура SyntexHyperSoft",
                    color = CyberTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "О,отличный выбор,тогда мы сменяем на свою архитектуру поиска и на свой интернет датацентр где будут сайты только от SyntexHyperSoft и проверенные сайты от других,но в настройке ты всегда можешь это поменять",
                    color = CyberTextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSyntexConfirmModal = false
                        onComplete(selectedMode, selectedSearchEngine, true)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = CyberVoid
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("syntex_arch_confirm_ok_btn")
                ) {
                    Text(
                        text = "Ок",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        )
    }
}

@Composable
private fun OnboardingModeOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CyberSurfaceVariant else CyberVoid
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) color else CyberCardBorder,
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = title,
                    color = if (isSelected) CyberTextPrimary else CyberTextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun OnboardingPurposeCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CyberSurfaceVariant else CyberVoid
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) color else CyberCardBorder,
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    color = if (isSelected) CyberTextPrimary else CyberTextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    color = color,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun SearchEngineChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) CyberCyan.copy(alpha = 0.2f) else CyberVoid)
            .border(
                width = 1.dp,
                color = if (isSelected) CyberCyan else CyberCardBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) CyberCyan else CyberTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}
