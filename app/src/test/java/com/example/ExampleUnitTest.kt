package com.example

import com.example.data.model.DnsMode
import com.example.data.security.AdBlockEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testAdBlockEngineFiltersKnownAdsAndIncrementsCounter() {
        val engine = AdBlockEngine()
        val result = engine.inspectRequest(
            "https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js",
            DnsMode.MILANA_AI_MAX_SHIELD
        )
        assertEquals(AdBlockEngine.BlockType.AD, result)
        assertEquals(1, engine.stats.value.blockedAds)
    }

    @Test
    fun testAdBlockEngineDoesNotBlockWhitelistedAiServices() {
        val engine = AdBlockEngine()
        val result = engine.inspectRequest(
            "https://chatgpt.com/backend-api/conversation",
            DnsMode.SYNTEXT_BYPASS
        )
        assertEquals(AdBlockEngine.BlockType.NONE, result)
    }

    @Test
    fun testAdBlockEngineBlocksMrFeastBuiltinTrackers() {
        val engine = AdBlockEngine()
        val result = engine.inspectRequest(
            "https://pixel.facebook.com/tr/",
            DnsMode.SYNTEXT_BYPASS
        )
        assertEquals(AdBlockEngine.BlockType.TRACKER, result)
        assertEquals(1, engine.stats.value.blockedTrackers)
    }

    @Test
    fun testYoutubeStreamsAreNeverBlocked() {
        val engine = AdBlockEngine()
        val result = engine.inspectRequest(
            "https://googlevideo.com/videoplayback?expire=123",
            DnsMode.MILANA_AI_MAX_SHIELD
        )
        assertEquals(AdBlockEngine.BlockType.NONE, result)
    }

    @Test
    fun testYandexAnalyticsAndYastaticTrackingAreBlocked() {
        val engine = AdBlockEngine()
        val resultMetrika = engine.inspectRequest(
            "https://mc.yandex.ru/metrika/watch.js",
            DnsMode.SYNTEXT_BYPASS
        )
        assertEquals(AdBlockEngine.BlockType.TRACKER, resultMetrika)

        val resultYastatic = engine.inspectRequest(
            "https://yastatic.net/metrika/tag.js",
            DnsMode.MILANA_AI_MAX_SHIELD
        )
        assertEquals(AdBlockEngine.BlockType.TRACKER, resultYastatic)
    }

    @Test
    fun testWebRtcProtectionScriptStripsIceCandidates() {
        val engine = AdBlockEngine()
        val script = engine.getWebRtcProtectionJsScript()
        assertTrue(script.contains("RTCPeerConnection"))
        assertTrue(script.contains("iceServers"))
    }

    @Test
    fun testMaliciousDomainDetection() {
        val engine = AdBlockEngine()
        assertTrue(engine.isMaliciousDomain("https://steal-google-accounts.top/login"))
        assertTrue(engine.isMaliciousDomain("http://malware-drop-zone.xyz/payload.apk"))
    }

    @Test
    fun testCosmeticAdHidingScriptContainsBrowseRules() {
        val engine = AdBlockEngine()
        val script = engine.getCosmeticAdHidingJsScript()
        assertTrue(script.contains("yandex-rtb"))
        assertTrue(script.contains("display: none !important"))
    }

    @Test
    fun testDohResolverModeUpdate() {
        com.example.data.security.DohResolver.updateMode(DnsMode.SYNTEXT_BYPASS)
        assertEquals("https://eu.geohide.ru/dns-query", com.example.data.security.DohResolver.status.value.activeEndpoint)

        com.example.data.security.DohResolver.updateMode(DnsMode.MILANA_AI_MAX_SHIELD)
        assertEquals("https://dns.adguard-dns.com/dns-query", com.example.data.security.DohResolver.status.value.activeEndpoint)
    }

    @Test
    fun testDohStatusPageGeneration() {
        val html = com.example.data.security.DohStatusPage.getHtml(
            DnsMode.SYNTEXT_BYPASS,
            com.example.data.security.DohResolver.DohStatus(
                activeEndpoint = "https://eu.geohide.ru/dns-query",
                lastLatencyMs = 28,
                totalQueries = 10
            )
        )
        assertTrue(html.contains("GeoHide"))
        assertTrue(html.contains("eu.geohide.ru/dns-query"))
        assertTrue(html.contains("chatgpt.com"))
    }

    @Test
    fun testParseListFromBrowsePyFormat() {
        val engine = AdBlockEngine()
        val sampleList = """
            ! Checksum: abc1234
            ! Version: 2.1.84.45
            ! Last modified: 2026-10-31
            # Some comments
            0.0.0.0 bad-ad-server.com
            127.0.0.1 tracker-analytics.net
            ||malicious-payload.xyz^
            ||cdn.banner-service.com/popups/ad^
        """.trimIndent()

        val (hosts, paths, malware) = engine.parseList(sampleList, "malware")
        assertTrue(hosts.contains("bad-ad-server.com"))
        assertTrue(hosts.contains("tracker-analytics.net"))
        assertTrue(hosts.contains("malicious-payload.xyz"))
        assertTrue(malware.contains("bad-ad-server.com"))
        assertTrue(malware.contains("malicious-payload.xyz"))
        assertTrue(paths.containsKey("cdn.banner-service.com"))
        assertTrue(paths["cdn.banner-service.com"]?.contains("/popups/ad") == true)
    }

    @Test
    fun testUpdatedDnsModeTitlesMilanaFlashAndUltra() {
        assertEquals("Обход блокировок + Милана V3.2 Flash", DnsMode.SYNTEXT_BYPASS.title)
        assertEquals("Максимальная защита с Милана V3.2 Ultra", DnsMode.MILANA_AI_MAX_SHIELD.title)
    }

    @Test
    fun testFasDomainRegistryResolvesAllCustomDomainsAndAliases() {
        val registry = com.example.data.model.FasDomainRegistry

        // 1. Studio
        assertEquals(
            "https://syntexhypersoft.github.io/MrFeastProjectStudio.com/",
            registry.resolveInput("fas://MrFeastProject.studio.com")
        )
        assertEquals(
            "https://syntexhypersoft.github.io/MrFeastProjectStudio.com/",
            registry.resolveInput("MrFeastProject.studio.com")
        )
        assertEquals(
            "https://syntexhypersoft.github.io/MrFeastProjectStudio.com/",
            registry.resolveInput("Студия")
        )

        // 2. Bank
        assertEquals(
            "https://syntexhypersoft.github.io/MrFeastProjectBank-Web/",
            registry.resolveInput("fas://SyntexHyperSoftBank.OFFICIAL")
        )
        assertEquals(
            "https://syntexhypersoft.github.io/MrFeastProjectBank-Web/",
            registry.resolveInput("Банк")
        )
        assertEquals(
            "https://syntexhypersoft.github.io/MrFeastProjectBank-Web/",
            registry.resolveInput("ОнлайнБанк")
        )

        // 3. Dino
        assertEquals(
            "https://syntexhypersoft.github.io/MrFeastInfo/Dino2.html",
            registry.resolveInput("fas://SyntexHyperSoftDinoGO.OFFICIAL")
        )
        assertEquals(
            "https://syntexhypersoft.github.io/MrFeastInfo/Dino2.html",
            registry.resolveInput("Динозаврик")
        )

        // 4. Loneliness Quiz
        assertEquals(
            "https://syntexhypersoft.github.io/Loneliness/",
            registry.resolveInput("fas://SyntexHyperSoftQuiz.OFFICIAL/Loneliness")
        )
        assertEquals(
            "https://syntexhypersoft.github.io/Loneliness/",
            registry.resolveInput("ТестНаОдиночество")
        )

        // 5. Feast Executor
        assertEquals(
            "https://syntexhypersoft.github.io/FeastExecutorOfficialKeySystem/",
            registry.resolveInput("fas://FeastExecutor_SyntexHyperSoft.OFFICIAL")
        )
        assertEquals(
            "https://syntexhypersoft.github.io/FeastExecutorOfficialKeySystem/",
            registry.resolveInput("Читы")
        )
        assertEquals(
            "https://syntexhypersoft.github.io/FeastExecutorOfficialKeySystem/",
            registry.resolveInput("Roblox")
        )

        // 6. Quiz VPN
        assertEquals(
            "https://syntexhypersoft.github.io/Loneliness/VPN_Knowledge_Quiz_Modern.html",
            registry.resolveInput("fas://SyntexHyperSoftQuiz.OFFICIAL/QuizVPN")
        )
        assertEquals(
            "https://syntexhypersoft.github.io/Loneliness/VPN_Knowledge_Quiz_Modern.html",
            registry.resolveInput("QuizVPN")
        )

        // 7. Killer Casino
        assertEquals(
            "https://syntexhypersoft.github.io/MrFeastInfo/KillerCasino.html",
            registry.resolveInput("fas://SyntexHyperSoft_KillerCasino.OFFICIAL")
        )
        assertEquals(
            "https://syntexhypersoft.github.io/MrFeastInfo/KillerCasino.html",
            registry.resolveInput("Казино")
        )

        // 8. Neon Star
        assertEquals(
            "https://syntexhypersoft.github.io/NeonStar/NeonStar.html",
            registry.resolveInput("fas://SyntexHyperSoft_NeonStar.OFFICIAL")
        )
        assertEquals(
            "https://syntexhypersoft.github.io/NeonStar/NeonStar.html",
            registry.resolveInput("NeonStar")
        )

        // 9. 3D Car
        assertEquals(
            "https://syntexhypersoft.github.io/Mini-Games",
            registry.resolveInput("fas://SyntexHyperSoft.3DCar.OFFICIAL")
        )
        assertEquals(
            "https://syntexhypersoft.github.io/Mini-Games",
            registry.resolveInput("3DGame")
        )

        // 10. AI Revolution
        assertEquals(
            "https://multicraft004-svg.github.io/Ai-revol3/",
            registry.resolveInput("fas://AiRevolution.com")
        )
        assertEquals(
            "https://multicraft004-svg.github.io/Ai-revol3/",
            registry.resolveInput("AiRevolution")
        )
    }

    @Test
    fun testFasDisplayUrlMapsCorrectly() {
        val registry = com.example.data.model.FasDomainRegistry

        assertEquals(
            "fas://SyntexHyperSoftBank.OFFICIAL",
            registry.toFasDisplayUrl("https://syntexhypersoft.github.io/MrFeastProjectBank-Web/")
        )
        assertEquals(
            "fas://SyntexHyperSoftBank.OFFICIAL",
            registry.toFasDisplayUrl("https://syntexhypersoft.github.io/MrFeastProjectBank-Web/index.html")
        )
        assertEquals(
            "fas://MrFeastProject.studio.com",
            registry.toFasDisplayUrl("https://syntexhypersoft.github.io/MrFeastProjectStudio.com/")
        )
        assertEquals(
            "fas://SyntexHyperSoftDinoGO.OFFICIAL",
            registry.toFasDisplayUrl("https://syntexhypersoft.github.io/MrFeastInfo/Dino2.html")
        )
        assertEquals(
            "fas://SyntexHyperSoftQuiz.OFFICIAL/Loneliness",
            registry.toFasDisplayUrl("https://syntexhypersoft.github.io/Loneliness/")
        )
        assertEquals(
            "fas://FeastExecutor_SyntexHyperSoft.OFFICIAL",
            registry.toFasDisplayUrl("https://syntexhypersoft.github.io/FeastExecutorOfficialKeySystem/")
        )
    }

    @Test
    fun testFasDatabaseTxtParsingWithDomenLines() {
        val registry = com.example.data.model.FasDomainRegistry
        val dbText = """
            MrFeastProjectStudio/MrFeastProject/МрФист/Студия-[https://syntexhypersoft.github.io/MrFeastProjectStudio.com/]
            Domen:MrFeastProject.studio.com
            
            SyntexHyperSoftBank/Банк/ОнлайнБанк-[https://syntexhypersoft.github.io/MrFeastProjectBank-Web/]
            Domen:SyntexHyperSoftBank.OFFICIAL
        """.trimIndent()

        val parsed = registry.parseDatabaseText(dbText)
        assertEquals(2, parsed.size)
        assertEquals("MrFeastProject.studio.com", parsed[0].customDomain)
        assertEquals("fas://MrFeastProject.studio.com", parsed[0].fasUrl)
        assertEquals("SyntexHyperSoftBank.OFFICIAL", parsed[1].customDomain)
        assertEquals("fas://SyntexHyperSoftBank.OFFICIAL", parsed[1].fasUrl)
    }

    @Test
    fun testXferwenFasSearchEngineIsExclusiveAndDefault() {
        val exclusiveEngine = com.example.data.model.SearchEngine.XFERWEN_FAS
        assertTrue(exclusiveEngine.isExclusiveToXferwen)
        assertTrue(exclusiveEngine.title.contains("XFERWEN + FAS"))
        assertEquals("https://xferwen.search/?q=", exclusiveEngine.searchUrl)

        val html = com.example.data.security.FasSearchPage.getHtml("банк")
        assertTrue(html.contains("XFERWEN + FAS"))
        assertTrue(html.contains("Банк SyntexHyperSoft"))
        assertTrue(html.contains("fas://SyntexHyperSoftBank.OFFICIAL"))
    }
}


