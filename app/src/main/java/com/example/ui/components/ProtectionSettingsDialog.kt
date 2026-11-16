package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DnsMode
import com.example.data.model.SearchEngine
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
 * Settings Sheet with "Настройка Защиты" tab and General options.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProtectionSettingsDialog(
    sheetState: SheetState,
    currentDnsMode: DnsMode,
    isDntEnabled: Boolean,
    isVulkanEnabled: Boolean,
    isWebRtcProtectionEnabled: Boolean,
    isXferwenProtectEnabled: Boolean,
    isV8OptimizationEnabled: Boolean,
    isAdHiderEnabled: Boolean,
    currentSearchEngine: SearchEngine,
    isAutoSavePasswordsEnabled: Boolean = true,
    isSyntexSearchArchEnabled: Boolean = false,
    onDnsModeChange: (DnsMode) -> Unit,
    onToggleDnt: (Boolean) -> Unit,
    onToggleVulkan: (Boolean) -> Unit,
    onToggleWebRtc: (Boolean) -> Unit,
    onToggleXferwenProtect: (Boolean) -> Unit,
    onToggleV8Optimization: (Boolean) -> Unit,
    onToggleAdHider: (Boolean) -> Unit,
    onToggleAutoSavePasswords: (Boolean) -> Unit = {},
    onToggleSyntexSearchArch: (Boolean) -> Unit = {},
    onSearchEngineChange: (SearchEngine) -> Unit,
    onClearCacheAndCookies: () -> Unit,
    onMakeDefaultBrowser: () -> Unit,
    onShareApk: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var pendingMode by remember { mutableStateOf<DnsMode?>(null) }
    var showWarningDialog by remember { mutableStateOf(false) }

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
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Параметры XFERWEN",
                    color = CyberTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CyberVoid,
                contentColor = CyberCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CyberCyan
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Настройка Защиты",
                            color = if (selectedTab == 0) CyberCyan else CyberTextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Общие",
                            color = if (selectedTab == 1) CyberCyan else CyberTextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (selectedTab == 0) {
                    // TAB: НАСТРОЙКА ЗАЩИТЫ
                    Text(
                        text = "Выбор режима защиты и фильтрации:",
                        color = CyberTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Mode 1: Обход блокировок + Милана V3.2 Flash
                    DnsSettingCard(
                        title = "Обход блокировок + Милана V3.2 Flash",
                        subtitle = "Интеллектуальный обход ChatGPT, Claude, Gemini и нейрофильтрация рекламы",
                        isSelected = currentDnsMode == DnsMode.SYNTEXT_BYPASS,
                        color = CyberCyan,
                        onClick = {
                            if (currentDnsMode != DnsMode.SYNTEXT_BYPASS) {
                                pendingMode = DnsMode.SYNTEXT_BYPASS
                                showWarningDialog = true
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Mode 2: Максимальная защита с Милана V3.2 Ultra
                    DnsSettingCard(
                        title = "Максимальная защита с Милана V3.2 Ultra",
                        subtitle = "Старшая сестра: максимальная блокировка рекламы, трекеров и фишинга",
                        isSelected = currentDnsMode == DnsMode.MILANA_AI_MAX_SHIELD,
                        color = CyberMatrixGreen,
                        onClick = {
                            if (currentDnsMode != DnsMode.MILANA_AI_MAX_SHIELD) {
                                pendingMode = DnsMode.MILANA_AI_MAX_SHIELD
                                showWarningDialog = true
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = CyberCardBorder)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Auto-save passwords toggle
                    SettingToggleRow(
                        icon = Icons.Default.Lock,
                        title = "Автосохранение паролей",
                        description = "Автоматически предлагать сохранить пароли при авторизации на сайтах в зашифрованное хранилище AES-256",
                        isChecked = isAutoSavePasswordsEnabled,
                        onCheckedChange = onToggleAutoSavePasswords
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Syntex Search Architecture toggle
                    SettingToggleRow(
                        icon = Icons.Default.Public,
                        title = "Архитектура поиска SyntexHyperSoft",
                        description = "Использовать датацентр SyntexHyperSoft и реестр проверенных серверов",
                        isChecked = isSyntexSearchArchEnabled,
                        onCheckedChange = onToggleSyntexSearchArch
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // XFERWEN Protect (Google SafeBrowsing & Yandex Protect)
                    SettingToggleRow(
                        icon = Icons.Default.Security,
                        title = "XFERWEN Protect (SafeBrowsing)",
                        description = "Блокировка опасных фишинговых сайтов, криптомайнеров и скама на базе Google & Yandex Protect",
                        isChecked = isXferwenProtectEnabled,
                        onCheckedChange = onToggleXferwenProtect
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // WebRTC leak protection switch
                    SettingToggleRow(
                        icon = Icons.Default.Lock,
                        title = "Защита WebRTC от утечек IP",
                        description = "Блокирует раскрытие реального IP адреса устройства через WebRTC STUN/TURN протоколы",
                        isChecked = isWebRtcProtectionEnabled,
                        onCheckedChange = onToggleWebRtc
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Cosmetic Ad Hider switch
                    SettingToggleRow(
                        icon = Icons.Default.VisibilityOff,
                        title = "Скрытие рекламных блоков и баннеров",
                        description = "Автоматически скрывает пустые рекламные места, RTB-виджеты и всплывающие промо-баннеры",
                        isChecked = isAdHiderEnabled,
                        onCheckedChange = onToggleAdHider
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Google Chrome V8 Engine & JIT switch
                    SettingToggleRow(
                        icon = Icons.Default.Bolt,
                        title = "Движок V8 (Google Chrome) & JIT",
                        description = "Прямое исполнение оптимизированного байткода V8 для максимальной скорости JavaScript и страниц",
                        isChecked = isV8OptimizationEnabled,
                        onCheckedChange = onToggleV8Optimization
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Do Not Track switch
                    SettingToggleRow(
                        icon = Icons.Default.Lock,
                        title = "Do Not Track (DNT)",
                        description = "Запретить веб-сайтам отслеживать действия и собирать цифровой отпечаток",
                        isChecked = isDntEnabled,
                        onCheckedChange = onToggleDnt
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Vulkan direct access switch
                    SettingToggleRow(
                        icon = Icons.Default.Speed,
                        title = "Прямой доступ к Vulkan GPU",
                        description = "Предоставлять напрямую доступ к Vulkan для ускорения HTML5, WebGL и страниц",
                        isChecked = isVulkanEnabled,
                        onCheckedChange = onToggleVulkan
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Anti-bot cloaking indicator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberVoid)
                            .border(1.dp, CyberMatrixGreen.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = CyberMatrixGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Анти-робот & Блокировка Yandex Аналитики", color = CyberTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text(text = "Вся аналитика Яндекса и yastatic трекеры заблокированы. Маскировка под реальный Chrome активна.", color = CyberTextMuted, fontSize = 11.sp, lineHeight = 15.sp)
                        }
                        Text(text = "АКТИВНО", color = CyberMatrixGreen, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Clear data button
                    Button(
                        onClick = onClearCacheAndCookies,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberSurfaceVariant,
                            contentColor = CyberDanger
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CyberDanger.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Очистить кэш, куки и историю сессии", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                } else {
                    // TAB: ОБЩИЕ
                    Text(
                        text = "Поисковая система:",
                        color = CyberTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SearchEngine.values().forEach { engine ->
                        val isSelected = currentSearchEngine == engine
                        if (engine.isExclusiveToXferwen) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) CyberCyan.copy(alpha = 0.12f) else CyberSurfaceVariant)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) CyberCyan else CyberCardBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onSearchEngineChange(engine) }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { onSearchEngineChange(engine) },
                                        colors = RadioButtonDefaults.colors(selectedColor = CyberCyan)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = engine.title,
                                                color = if (isSelected) CyberCyan else CyberTextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(CyberCyan)
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "ТОЛЬКО В XFERWEN",
                                                    color = CyberVoid,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = engine.description,
                                            color = CyberTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSearchEngineChange(engine) }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSearchEngineChange(engine) },
                                    colors = RadioButtonDefaults.colors(selectedColor = CyberCyan)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = engine.title,
                                        color = if (isSelected) CyberCyan else CyberTextPrimary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = engine.description,
                                        color = CyberTextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = CyberCardBorder)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Set as Default Browser Button
                    Button(
                        onClick = onMakeDefaultBrowser,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = CyberVoid
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("set_default_browser_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Public, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Сделать XFERWEN браузером по умолчанию", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Share APK file button
                    Button(
                        onClick = onShareApk,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberSurfaceVariant,
                            contentColor = CyberCyan
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CyberCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .testTag("share_apk_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Поделиться APK файлом приложения", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // App Information
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "Информация о браузере", color = CyberTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Название: XFERWEN", color = CyberCyan, fontSize = 12.sp)
                            Text(text = "Версия: 1.0.0.0", color = CyberTextSecondary, fontSize = 12.sp)
                            Text(text = "Пакет: XFERWEN.SyntaxHyperSoft.com", color = CyberTextSecondary, fontSize = 12.sp)
                            Text(text = "Движок: Google V8 Engine / Vulkan GPU Direct", color = CyberMatrixGreen, fontSize = 12.sp)
                            Text(text = "Безопасность: Milana AI & XFERWEN Protect", color = CyberCyan, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Confirmation Alert Dialog when changing DNS mode in settings
    if (showWarningDialog && pendingMode != null) {
        val target = pendingMode!!
        val isSwitchingToBypass = (target == DnsMode.SYNTEXT_BYPASS)

        val warningTitle = "Внимание"
        val warningMessage = if (isSwitchingToBypass) {
            "Вы уверены что хотите переключить с режима \"Максимальная защита\" на \"Обход блокировок\"? Ваша конфидициальность может снизиться и может появится реклама и не блокироваться все трекеры,но при этом станут доступны некоторые сервисы за счет обхода блокировок?"
        } else {
            "Вы уверены что хотите переключится с режима \"Обход блокировок\" на \"Максимальная защита\"? Это поможет убрать надоедливаю рекламу и улучшить вашу конфиденциальность,но возможно что заблокированные сервисы перестанут работать"
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
                        onDnsModeChange(target)
                        showWarningDialog = false
                        pendingMode = null
                    }
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
                    }
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
private fun DnsSettingCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) color else CyberCardBorder,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CyberSurfaceVariant else CyberVoid
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
private fun SettingToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = CyberTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(text = description, color = CyberTextMuted, fontSize = 11.sp, lineHeight = 15.sp)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CyberVoid,
                checkedTrackColor = CyberCyan,
                uncheckedThumbColor = CyberTextMuted,
                uncheckedTrackColor = CyberSurfaceVariant
            )
        )
    }
}
