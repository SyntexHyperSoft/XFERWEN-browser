package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import java.net.URL

data class FasSiteEntry(
    val id: String,
    val name: String,
    val customDomain: String,       // e.g. "SyntexHyperSoftBank.OFFICIAL"
    val fasUrl: String,             // e.g. "fas://SyntexHyperSoftBank.OFFICIAL"
    val targetUrl: String,          // e.g. "https://syntexhypersoft.github.io/MrFeastProjectBank-Web/"
    val aliases: List<String>,      // e.g. ["Банк", "ОнлайнБанк", "SyntexHyperSoftBank", ...]
    val tags: List<String>,
    val icon: ImageVector = Icons.Default.Language,
    val accentColor: Color = Color(0xFF00D4FF),
    val isOfficial: Boolean = true
)

/**
 * Registry of the SyntexHyperSoft / FAS Ecosystem.
 * Allows proprietary "fas://" protocol resolution, custom domains (.OFFICIAL, .studio.com, etc.),
 * and keyword/alias routing directly to target URLs while maintaining the "fas://" protocol and
 * custom ecosystem domain in the address bar.
 */
object FasDomainRegistry {

    val SITES: List<FasSiteEntry> = listOf(
        // 1. MrFeastProjectStudio
        FasSiteEntry(
            id = "studio",
            name = "Студия MrFeastProject",
            customDomain = "MrFeastProject.studio.com",
            fasUrl = "fas://MrFeastProject.studio.com",
            targetUrl = "https://syntexhypersoft.github.io/MrFeastProjectStudio.com/",
            aliases = listOf(
                "MrFeastProjectStudio",
                "MrFeastProject",
                "МрФист",
                "Студия",
                "MrFeastProject.studio.com",
                "studio.com",
                "QuadroPhotoshop",
                "студия"
            ),
            tags = listOf("Студия", "МрФист", "Quadro", "Дизайн"),
            icon = Icons.Default.Brush,
            accentColor = Color(0xFF00D4FF)
        ),

        // 2. SyntexHyperSoftBank
        FasSiteEntry(
            id = "bank",
            name = "Банк SyntexHyperSoft",
            customDomain = "SyntexHyperSoftBank.OFFICIAL",
            fasUrl = "fas://SyntexHyperSoftBank.OFFICIAL",
            targetUrl = "https://syntexhypersoft.github.io/MrFeastProjectBank-Web/",
            aliases = listOf(
                "SyntexHyperSoftBank",
                "SyntexHyperSoft",
                "Bank",
                "OnlineBank",
                "Банк",
                "ОнлайнБанк",
                "МрФистБанк",
                "МрФист",
                "банк",
                "онлайнбанк",
                "SyntexHyperSoftBank.OFFICIAL"
            ),
            tags = listOf("Банк", "Финансы", "Syntex", "Официальный"),
            icon = Icons.Default.AccountBalance,
            accentColor = Color(0xFF00FFCC)
        ),

        // 3. SyntexHyperSoftDino
        FasSiteEntry(
            id = "dino",
            name = "Динозаврик DinoGO",
            customDomain = "SyntexHyperSoftDinoGO.OFFICIAL",
            fasUrl = "fas://SyntexHyperSoftDinoGO.OFFICIAL",
            targetUrl = "https://syntexhypersoft.github.io/MrFeastInfo/Dino2.html",
            aliases = listOf(
                "SyntexHyperSoftDino",
                "SyntexHyperSoft",
                "Dino",
                "Динозаврик",
                "МрФист",
                "МрФистДинозаврик",
                "динозаврик",
                "дино",
                "DinoGO",
                "SyntexHyperSoftDinoGO.OFFICIAL"
            ),
            tags = listOf("Динозаврик", "Игра", "Vulkan", "DinoGO"),
            icon = Icons.Default.Gamepad,
            accentColor = Color(0xFF39FF14)
        ),

        // 4. SyntexHyperSoftLoneli (Тест на одиночество)
        FasSiteEntry(
            id = "loneliness",
            name = "Тест на одиночество",
            customDomain = "SyntexHyperSoftQuiz.OFFICIAL/Loneliness",
            fasUrl = "fas://SyntexHyperSoftQuiz.OFFICIAL/Loneliness",
            targetUrl = "https://syntexhypersoft.github.io/Loneliness/",
            aliases = listOf(
                "SyntexHyperSoftLoneli",
                "SyntexHyperSoftLoneliness",
                "SyntexHyperSoft",
                "ТестНаОдиночество",
                "ОдиночествоТест",
                "ТестОдиночествоМрФист",
                "Одиночество2026",
                "Тест на одиночество",
                "тест на одиночество",
                "одиночество",
                "SyntexHyperSoftQuiz.OFFICIAL/Loneliness"
            ),
            tags = listOf("Тест на одиночество", "Психология", "SyntexHyperSoft"),
            icon = Icons.Default.Psychology,
            accentColor = Color(0xFFBC13FE)
        ),

        // 5. SyntexHyperSoft FeastExecutor (Читы Roblox)
        FasSiteEntry(
            id = "executor",
            name = "Feast Executor (Roblox)",
            customDomain = "FeastExecutor_SyntexHyperSoft.OFFICIAL",
            fasUrl = "fas://FeastExecutor_SyntexHyperSoft.OFFICIAL",
            targetUrl = "https://syntexhypersoft.github.io/FeastExecutorOfficialKeySystem/",
            aliases = listOf(
                "SyntexHyperSoft",
                "SyntexHyperSoftFeastExecutor",
                "FeastExecutor",
                "Feast",
                "Executor",
                "Читы",
                "Cheats",
                "Чит",
                "Cheat",
                "Roblox",
                "roblox",
                "Роблокс",
                "роблокс",
                "читы",
                "чит",
                "FeastExecutor_SyntexHyperSoft.OFFICIAL"
            ),
            tags = listOf("Читы", "Roblox", "Executor", "Syntex"),
            icon = Icons.Default.Code,
            accentColor = Color(0xFFFF0055)
        ),

        // 6. SyntexHyperSoft Quiz VPN
        FasSiteEntry(
            id = "quiz_vpn",
            name = "Тест знаний VPN (QuizVPN)",
            customDomain = "SyntexHyperSoftQuiz.OFFICIAL/QuizVPN",
            fasUrl = "fas://SyntexHyperSoftQuiz.OFFICIAL/QuizVPN",
            targetUrl = "https://syntexhypersoft.github.io/Loneliness/VPN_Knowledge_Quiz_Modern.html",
            aliases = listOf(
                "SyntexHyperSoft",
                "SyntexHyperSoftTEST",
                "SyntexHyperSoftQuiz",
                "SyntexHyperSoftQuizVPN",
                "QuizVPN",
                "NewSite",
                "VPN Quiz",
                "quizvpn",
                "SyntexHyperSoftQuiz.OFFICIAL/QuizVPN"
            ),
            tags = listOf("QuizVPN", "Тест", "VPN", "Безопасность"),
            icon = Icons.Default.Security,
            accentColor = Color(0xFF00E5FF)
        ),

        // 7. SyntexHyperSoft KillerCasino
        FasSiteEntry(
            id = "casino",
            name = "Killer Casino",
            customDomain = "SyntexHyperSoft_KillerCasino.OFFICIAL",
            fasUrl = "fas://SyntexHyperSoft_KillerCasino.OFFICIAL",
            targetUrl = "https://syntexhypersoft.github.io/MrFeastInfo/KillerCasino.html",
            aliases = listOf(
                "SyntexHyperSoft",
                "KillerCasino",
                "Best",
                "BestCasino",
                "Казино",
                "казино",
                "killercasino",
                "SyntexHyperSoft_KillerCasino.OFFICIAL"
            ),
            tags = listOf("Казино", "Игра", "Casino", "Syntex"),
            icon = Icons.Default.Casino,
            accentColor = Color(0xFFFFD700)
        ),

        // 8. SyntexHyperSoft NeonStar
        FasSiteEntry(
            id = "neon_star",
            name = "Neon Star Clicker",
            customDomain = "SyntexHyperSoft_NeonStar.OFFICIAL",
            fasUrl = "fas://SyntexHyperSoft_NeonStar.OFFICIAL",
            targetUrl = "https://syntexhypersoft.github.io/NeonStar/NeonStar.html",
            aliases = listOf(
                "SyntexHyperSoft",
                "SyntexHyperSoftNeonStar",
                "NeonStar",
                "Clicker",
                "ClickerGame",
                "clickergame",
                "clicker",
                "star",
                "neonstar",
                "neon",
                "Кликер",
                "кликер",
                "SyntexHyperSoft_NeonStar.OFFICIAL"
            ),
            tags = listOf("NeonStar", "Кликер", "Clicker", "Игры"),
            icon = Icons.Default.Star,
            accentColor = Color(0xFFFF007F)
        ),

        // 9. SyntexHyperSoft 3D Car Game
        FasSiteEntry(
            id = "3d_car",
            name = "Syntex 3D Car Auto",
            customDomain = "SyntexHyperSoft.3DCar.OFFICIAL",
            fasUrl = "fas://SyntexHyperSoft.3DCar.OFFICIAL",
            targetUrl = "https://syntexhypersoft.github.io/Mini-Games",
            aliases = listOf(
                "SyntexHyperSoft",
                "3D",
                "Game",
                "3DGame",
                "3d",
                "game",
                "3dgame",
                "auto",
                "AUTO",
                "car",
                "CAR",
                "Car",
                "Машина",
                "Авто",
                "машина",
                "авто",
                "гонки",
                "SyntexHyperSoft.3DCar.OFFICIAL"
            ),
            tags = listOf("3D", "Авто", "Гонки", "Игры"),
            icon = Icons.Default.DirectionsCar,
            accentColor = Color(0xFFFF6600)
        ),

        // 10. Airevolution
        FasSiteEntry(
            id = "airevolution",
            name = "AI Revolution",
            customDomain = "AiRevolution.com",
            fasUrl = "fas://AiRevolution.com",
            targetUrl = "https://multicraft004-svg.github.io/Ai-revol3/",
            aliases = listOf(
                "Airevolution",
                "airevolution",
                "clickergame",
                "AiRevolution.com",
                "https://AiRevolution.com",
                "AiRevolution"
            ),
            tags = listOf("AI Revolution", "Кликер", "AI", "Игры"),
            icon = Icons.Default.TouchApp,
            accentColor = Color(0xFF00FF88)
        )
    )

    /**
     * Resolves user query (whether "fas://...", a custom domain like "SyntexHyperSoftBank.OFFICIAL",
     * or any alias like "Банк", "Читы", "Динозаврик") to the actual target URL.
     * Returns null if the query is not recognized as part of the FAS ecosystem.
     */
    fun resolveInput(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return null

        val lower = trimmed.lowercase().trimEnd('/')

        // System aliases
        if (lower in setOf("fas://quadroos.sys", "fas://quadro.sys", "quadroos", "quadro")) {
            return "https://mrfeastproject.github.io/QuadroOS-Telegram-Mini-App-Mini-OS-/System.html"
        }
        if (lower in setOf("fas://xferwen.browse.com/home", "fas://home", "xferwen://home", "home")) {
            return "about:blank"
        }

        // Check if query starts with fas://
        if (trimmed.startsWith("fas://", ignoreCase = true)) {
            val body = trimmed.substring(6).trim().trimEnd('/')
            val bodyLower = body.lowercase()

            // Exact customDomain match
            val matchedDomain = SITES.firstOrNull {
                it.customDomain.equals(body, ignoreCase = true) ||
                        it.customDomain.trimEnd('/').equals(body, ignoreCase = true) ||
                        bodyLower.startsWith(it.customDomain.lowercase())
            }
            if (matchedDomain != null) {
                // If subpath is attached (e.g. fas://SyntexHyperSoftQuiz.OFFICIAL/Loneliness)
                return matchedDomain.targetUrl
            }

            // Check if body matches any alias
            val matchedAlias = SITES.firstOrNull { site ->
                site.aliases.any { it.equals(body, ignoreCase = true) }
            }
            if (matchedAlias != null) {
                return matchedAlias.targetUrl
            }
        }

        // Check without fas:// prefix (e.g. "SyntexHyperSoftBank.OFFICIAL" or "Банк" or "MrFeastProject.studio.com")
        val cleanInput = trimmed.removePrefix("https://").removePrefix("http://").trimEnd('/')

        // Exact match with customDomain
        val matchedByDomain = SITES.firstOrNull {
            it.customDomain.equals(cleanInput, ignoreCase = true) ||
                    it.customDomain.trimEnd('/').equals(cleanInput, ignoreCase = true) ||
                    cleanInput.equals(it.customDomain.replace("https://", ""), ignoreCase = true)
        }
        if (matchedByDomain != null) {
            return matchedByDomain.targetUrl
        }

        // Match with aliases
        val matchedByAlias = SITES.firstOrNull { site ->
            site.aliases.any { it.equals(cleanInput, ignoreCase = true) || it.equals(trimmed, ignoreCase = true) }
        }
        if (matchedByAlias != null) {
            return matchedByAlias.targetUrl
        }

        return null
    }

    /**
     * Converts an opened real URL (e.g. "https://syntexhypersoft.github.io/MrFeastProjectBank-Web/")
     * into its proprietary FAS display URL (e.g. "fas://SyntexHyperSoftBank.OFFICIAL").
     */
    fun toFasDisplayUrl(realUrl: String): String? {
        if (realUrl.isBlank()) return null
        if (realUrl.startsWith("fas://", ignoreCase = true)) return realUrl
        if (realUrl == "about:blank") return "about:blank"

        val normalized = realUrl.trimEnd('/')

        // Check system QuadroOS
        if (normalized.contains("QuadroOS-Telegram-Mini-App-Mini-OS-/System.html", ignoreCase = true)) {
            return "fas://quadroos.sys"
        }

        for (site in SITES) {
            val targetNorm = site.targetUrl.trimEnd('/')
            if (normalized.equals(targetNorm, ignoreCase = true) ||
                normalized.startsWith(targetNorm, ignoreCase = true)
            ) {
                return site.fasUrl
            }
            // Also match by specific path/host signature
            try {
                val siteUri = URL(site.targetUrl)
                val realUri = URL(realUrl)
                if (siteUri.host.equals(realUri.host, ignoreCase = true) &&
                    realUri.path.startsWith(siteUri.path.trimEnd('/')) &&
                    siteUri.path.length > 1
                ) {
                    return site.fasUrl
                }
            } catch (_: Exception) {}
        }
        return null
    }

    /**
     * Finds a site entry by either its custom domain or fasUrl.
     */
    fun findSite(query: String): FasSiteEntry? {
        val clean = query.trim().removePrefix("fas://").trimEnd('/')
        return SITES.firstOrNull {
            it.customDomain.equals(clean, ignoreCase = true) ||
                    it.fasUrl.equals(query.trim().trimEnd('/'), ignoreCase = true) ||
                    it.aliases.any { a -> a.equals(clean, ignoreCase = true) }
        }
    }

    /**
     * Parses Database.txt text into FasSiteEntry elements, supporting:
     * Tag1/Tag2-[https://url]
     * Domen:custom.domain.OFFICIAL
     */
    fun parseDatabaseText(text: String): List<FasSiteEntry> {
        val result = mutableListOf<FasSiteEntry>()
        val lines = text.lines()
        var currentEntry: FasSiteEntry? = null

        val siteRegex = Regex("""^(.*?)-\[(https?://[^\]]+)\]\s*$""", RegexOption.IGNORE_CASE)
        val domenRegex = Regex("""^Domen:\s*(https?://)?([^\s]+)\s*$""", RegexOption.IGNORE_CASE)

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#")) continue

            val siteMatch = siteRegex.find(line)
            if (siteMatch != null) {
                if (currentEntry != null) {
                    result.add(currentEntry)
                    currentEntry = null
                }
                val tagsRaw = siteMatch.groupValues[1]
                val url = siteMatch.groupValues[2]
                val aliases = tagsRaw.split("/").map { it.trim() }.filter { it.isNotEmpty() }
                val defaultHost = try { URL(url).host } catch (_: Exception) { url }
                val name = aliases.firstOrNull() ?: defaultHost
                val customDomain = "$defaultHost.OFFICIAL"

                currentEntry = FasSiteEntry(
                    id = "site_${result.size}_${name.hashCode()}",
                    name = name,
                    customDomain = customDomain,
                    fasUrl = "fas://$customDomain",
                    targetUrl = url,
                    aliases = aliases,
                    tags = aliases.take(5),
                    icon = when {
                        name.contains("банк", ignoreCase = true) -> Icons.Default.AccountBalance
                        name.contains("студи", ignoreCase = true) -> Icons.Default.Brush
                        name.contains("дино", ignoreCase = true) -> Icons.Default.Gamepad
                        name.contains("тест", ignoreCase = true) -> Icons.Default.Psychology
                        name.contains("чит", ignoreCase = true) -> Icons.Default.Code
                        name.contains("казино", ignoreCase = true) -> Icons.Default.Casino
                        name.contains("авто", ignoreCase = true) || name.contains("car", ignoreCase = true) -> Icons.Default.DirectionsCar
                        name.contains("клик", ignoreCase = true) || name.contains("star", ignoreCase = true) -> Icons.Default.Star
                        else -> Icons.Default.Language
                    }
                )
                continue
            }

            val domenMatch = domenRegex.find(line)
            if (domenMatch != null && currentEntry != null) {
                val parsedDomain = domenMatch.groupValues[2].trim()
                currentEntry = currentEntry.copy(
                    customDomain = parsedDomain,
                    fasUrl = "fas://$parsedDomain"
                )
                result.add(currentEntry)
                currentEntry = null
            }
        }

        if (currentEntry != null) {
            result.add(currentEntry)
        }

        return result
    }
}
