package com.example.ui.components

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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DnsMode
import com.example.data.model.FasDomainRegistry
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class VerifiedSiteCard(
    val name: String,
    val domain: String,
    val url: String,
    val fasUrl: String = domain,
    val tags: List<String>,
    val icon: ImageVector = Icons.Default.Language,
    val accentColor: Color = CyberCyan,
    val isFasEcosystem: Boolean = domain.startsWith("fas://", ignoreCase = true)
)

/**
 * High-tech start page for XFERWEN browser crafted exactly according to the design in System_QuadroOS_Loader_Taskbar.html.
 * Features:
 * - Cyber shield logo with glowing radial gradients
 * - Title "XFERWEN" and subtitle "Популярные сайты"
 * - Cyber search bar with "НАЙТИ" button
 * - Filter pills: "Все", "Банк", "Студия", "Динозаврик", "Тест на одиночество", "AI Обход"
 * - Popular verified cards: Банк, Студия (QuadroPhotoshop), Динозаврик, Тест на одиночество, ChatGPT, Claude, Gemini, Яндекс
 * - Background async loader from SyntexHyperSoft Database.txt
 * - Bottom status bar "Готово • XFERWEN Browser"
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NewTabDashboard(
    dnsMode: DnsMode,
    totalBlocked: Int,
    isSyntexSearchArch: Boolean = false,
    onNavigate: (String) -> Unit,
    onOpenShield: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTag by remember { mutableStateOf("Все") }

    val filterPills = remember {
        listOf("Все", "FAS Сайты", "Банк", "Студия", "Динозаврик", "Тест на одиночество", "Читы", "Игры", "AI Обход")
    }

    // Default verified popular sites from FasDomainRegistry and user prompt
    val defaultSites = remember {
        val list = mutableListOf<VerifiedSiteCard>()
        // All 10 FAS Ecosystem official sites with FAS protocol and custom domains
        FasDomainRegistry.SITES.forEach { site ->
            list.add(
                VerifiedSiteCard(
                    name = site.name,
                    domain = site.fasUrl,
                    url = site.targetUrl,
                    fasUrl = site.fasUrl,
                    tags = site.tags + listOf("FAS Сайты"),
                    icon = site.icon,
                    accentColor = site.accentColor,
                    isFasEcosystem = true
                )
            )
        }
        // General Search and AI Services
        list.add(
            VerifiedSiteCard(
                name = "ChatGPT",
                domain = "chatgpt.com",
                url = "https://chatgpt.com",
                fasUrl = "https://chatgpt.com",
                tags = listOf("AI Обход", "ChatGPT", "Обход"),
                icon = Icons.Default.AutoAwesome,
                accentColor = CyberCyan,
                isFasEcosystem = false
            )
        )
        list.add(
            VerifiedSiteCard(
                name = "Claude AI",
                domain = "claude.ai",
                url = "https://claude.ai",
                fasUrl = "https://claude.ai",
                tags = listOf("AI Обход", "Claude", "Обход"),
                icon = Icons.Default.Psychology,
                accentColor = Color(0xFFD97706),
                isFasEcosystem = false
            )
        )
        list.add(
            VerifiedSiteCard(
                name = "Google Gemini",
                domain = "gemini.google.com",
                url = "https://gemini.google.com",
                fasUrl = "https://gemini.google.com",
                tags = listOf("AI Обход", "Gemini", "AI"),
                icon = Icons.Default.AutoAwesome,
                accentColor = CyberNeonBlue,
                isFasEcosystem = false
            )
        )
        list.add(
            VerifiedSiteCard(
                name = "Яндекс",
                domain = "ya.ru",
                url = "https://ya.ru",
                fasUrl = "https://ya.ru",
                tags = listOf("Поиск", "Главная", "Яндекс"),
                icon = Icons.Default.Search,
                accentColor = Color(0xFFFF3333),
                isFasEcosystem = false
            )
        )
        list
    }

    val dynamicSites = remember { mutableStateListOf<VerifiedSiteCard>() }

    // Asynchronously try to fetch live Database.txt from SyntexHyperSoft GitHub
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL("https://raw.githubusercontent.com/SyntexHyperSoft/QuadroOS-Database-internet-servers/main/Database.txt")
                val conn = url.openConnection() as HttpURLConnection
                conn.connectTimeout = 4000
                conn.readTimeout = 4000
                conn.requestMethod = "GET"
                if (conn.responseCode == 200) {
                    val text = conn.inputStream.bufferedReader().use { it.readText() }
                    val parsed = parseDatabaseTxt(text)
                    if (parsed.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            dynamicSites.clear()
                            dynamicSites.addAll(parsed)
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    val allSites = if (dynamicSites.isNotEmpty()) dynamicSites.toList() else defaultSites

    // Filter sites based on selected tag and search query
    val displayedSites = remember(allSites, selectedFilterTag, searchQuery) {
        allSites.filter { site ->
            val matchesTag = if (selectedFilterTag == "Все") true else {
                site.tags.any { it.contains(selectedFilterTag, ignoreCase = true) } ||
                        site.name.contains(selectedFilterTag, ignoreCase = true)
            }
            val matchesQuery = if (searchQuery.isBlank()) true else {
                site.name.contains(searchQuery, ignoreCase = true) ||
                        site.domain.contains(searchQuery, ignoreCase = true) ||
                        site.tags.any { it.contains(searchQuery, ignoreCase = true) }
            }
            matchesTag && matchesQuery
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "xfb_pulse")
    val heroScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberVoid)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Hero Header (.xfb-hero)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo (.xfb-logo)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .scale(heroScale)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF0066FF), Color(0xFF00D4FF)))
                    )
                    .border(1.dp, CyberCyan.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "XFERWEN",
                    color = CyberTextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.5.sp,
                    fontFamily = FontFamily.SansSerif
                )
                Text(
                    text = if (isSyntexSearchArch) "Датацентр SyntexHyperSoft • Проверенные сайты" else "Популярные сайты",
                    color = CyberCyan,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar (.xfb-search)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(CyberSurface)
                .border(1.dp, CyberCyan.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = CyberTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    cursorBrush = SolidColor(CyberCyan),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        if (searchQuery.isNotBlank()) onNavigate(searchQuery)
                    }),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = if (isSyntexSearchArch)
                                    "Поиск по SyntexHyperSoft или адрес..."
                                else
                                    "Найдите сайт по названию или тегу",
                                color = CyberTextMuted,
                                fontSize = 13.sp
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("xfb_search_input")
                )

                Spacer(modifier = Modifier.width(8.dp))

                // CTA Button (.xfb-cta)
                Button(
                    onClick = {
                        if (searchQuery.isNotBlank()) {
                            onNavigate(searchQuery)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF0066FF), Color(0xFF00D4FF))),
                            RoundedCornerShape(12.dp)
                        )
                        .testTag("xfb_search_submit_btn")
                ) {
                    Text(
                        text = "НАЙТИ",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Pills / Tags Row
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filterPills.forEach { pill ->
                val isSelected = selectedFilterTag == pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) CyberCyan.copy(alpha = 0.25f) else CyberSurfaceVariant)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) CyberCyan else CyberCardBorder,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedFilterTag = pill }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = pill,
                        color = if (isSelected) CyberCyan else CyberTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Section Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ПОПУЛЯРНЫЕ САЙТЫ",
                color = CyberCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Найдено: ${displayedSites.size}",
                color = CyberTextMuted,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Cards Grid (.xfb-cards)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            maxItemsInEachRow = 2
        ) {
            displayedSites.forEach { site ->
                XfbSiteCard(
                    site = site,
                    onClick = { onNavigate(site.url) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Milana AI Cyber Shield Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = onOpenShield)
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(listOf(CyberCardBorder, CyberCyan.copy(alpha = 0.5f))),
                    shape = RoundedCornerShape(16.dp)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CyberVoid)
                        .border(1.dp, CyberCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = CyberCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Milana AI Защита",
                        color = CyberTextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (dnsMode == DnsMode.SYNTEXT_BYPASS)
                            "Обход блокировок + Милана V3.2 Flash"
                        else
                            "Максимальная защита с Милана V3.2 Ultra",
                        color = CyberCyan,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Заблокировано угроз: $totalBlocked",
                        color = CyberMatrixGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Open",
                    tint = CyberCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status bar matching HTML (.xfb-status)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CyberSurface.copy(alpha = 0.8f))
                .border(1.dp, CyberCardBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(CyberMatrixGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Готово",
                    color = CyberTextSecondary,
                    fontSize = 11.sp
                )
            }
            Text(
                text = "XFERWEN Browser • SyntexHyperSoft",
                color = CyberCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * Single Card matching .xfb-card from System_QuadroOS_Loader_Taskbar.html
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun XfbSiteCard(
    site: VerifiedSiteCard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = CyberCyan.copy(alpha = 0.25f),
                shape = RoundedCornerShape(14.dp)
            )
            .testTag("xfb_card_${site.name}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberVoid)
                        .border(1.dp, site.accentColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = site.icon,
                        contentDescription = null,
                        tint = site.accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = site.name,
                        color = CyberTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (site.isFasEcosystem) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(CyberCyan.copy(alpha = 0.2f))
                                    .border(0.6.dp, CyberCyan, RoundedCornerShape(3.dp))
                                    .padding(horizontal = 3.dp, vertical = 0.5.dp)
                            ) {
                                Text(
                                    text = "FAS",
                                    color = CyberCyan,
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = site.domain,
                            color = if (site.isFasEcosystem) CyberCyan else CyberTextSecondary,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pills / Tags (.xfb-tags)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                site.tags.take(3).forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(CyberCyan.copy(alpha = 0.12f))
                            .border(1.dp, CyberCyan.copy(alpha = 0.25f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tag,
                            color = Color(0xFFA9DCF0),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Parses lines in format: Tag1/Tag2 - [https://url.com]
 * and Domen:custom.domain lines into VerifiedSiteCards.
 */
private fun parseDatabaseTxt(text: String): List<VerifiedSiteCard> {
    val fasEntries = FasDomainRegistry.parseDatabaseText(text)
    if (fasEntries.isNotEmpty()) {
        return fasEntries.map { entry ->
            VerifiedSiteCard(
                name = entry.name,
                domain = entry.fasUrl,
                url = entry.targetUrl,
                fasUrl = entry.fasUrl,
                tags = entry.tags + listOf("FAS Сайты"),
                icon = entry.icon,
                accentColor = entry.accentColor,
                isFasEcosystem = true
            )
        }
    }
    val result = mutableListOf<VerifiedSiteCard>()
    val lines = text.lines()
    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("#")) continue
        val regex = Regex("""^(.*?)-\[(https?://[^\]]+)\]\s*$""", RegexOption.IGNORE_CASE)
        val match = regex.find(trimmed)
        if (match != null) {
            val tagsRaw = match.groupValues[1]
            val url = match.groupValues[2]
            val tags = tagsRaw.split("/").map { it.trim() }.filter { it.isNotEmpty() }
            val host = try { URL(url).host } catch (_: Exception) { url }
            val name = tags.firstOrNull() ?: host
            val fasUrl = "fas://$host.OFFICIAL"
            result.add(
                VerifiedSiteCard(
                    name = name,
                    domain = fasUrl,
                    url = url,
                    fasUrl = fasUrl,
                    tags = tags + listOf("FAS Сайты"),
                    icon = when {
                        name.contains("банк", ignoreCase = true) -> Icons.Default.AccountBalance
                        name.contains("студи", ignoreCase = true) || name.contains("фото", ignoreCase = true) -> Icons.Default.Brush
                        name.contains("дино", ignoreCase = true) || name.contains("игра", ignoreCase = true) -> Icons.Default.Gamepad
                        name.contains("тест", ignoreCase = true) -> Icons.Default.Psychology
                        name.contains("код", ignoreCase = true) -> Icons.Default.Code
                        else -> Icons.Default.Language
                    },
                    isFasEcosystem = true
                )
            )
        }
    }
    return result
}
