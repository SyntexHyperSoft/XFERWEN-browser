package com.example.data.security

import com.example.data.model.FasDomainRegistry
import java.net.URLEncoder

/**
 * Generates high-tech Cyber Search Results page for the exclusive "XFERWEN + FAS" search engine.
 */
object FasSearchPage {

    fun getHtml(query: String): String {
        val safeQuery = query.replace("<", "&lt;").replace(">", "&gt;").trim()
        val encodedQuery = try { URLEncoder.encode(query, "UTF-8") } catch (_: Exception) { "" }

        // Find matches in FAS ecosystem
        val matchedSites = if (safeQuery.isNotBlank()) {
            FasDomainRegistry.SITES.filter { site ->
                site.name.contains(safeQuery, ignoreCase = true) ||
                        site.customDomain.contains(safeQuery, ignoreCase = true) ||
                        site.aliases.any { it.contains(safeQuery, ignoreCase = true) } ||
                        site.tags.any { it.contains(safeQuery, ignoreCase = true) }
            }
        } else {
            emptyList()
        }

        val allFasSites = FasDomainRegistry.SITES

        val matchedCardsHtml = if (matchedSites.isNotEmpty()) {
            buildString {
                append("<div class='section-title'>Найдено в экосистеме SyntexHyperSoft / FAS:</div>")
                for (site in matchedSites) {
                    append("""
                        <a href="${site.fasUrl}" class="fas-card">
                            <div class="card-head">
                                <div class="card-title">${site.name}</div>
                                <span class="badge-fas">FAS://</span>
                            </div>
                            <div class="card-domain">${site.fasUrl}</div>
                            <div class="card-tags">
                                ${site.tags.take(3).joinToString("") { "<span class='tag'>$it</span>" }}
                            </div>
                        </a>
                    """.trimIndent())
                }
            }
        } else if (safeQuery.isNotBlank()) {
            """
                <div class="no-direct-match">
                    В каталоге FAS нет прямого совпадения по запросу «<b>$safeQuery</b>».<br>
                    Вы можете продолжить поиск в глобальной сети или открыть сервисы экосистемы ниже.
                </div>
            """.trimIndent()
        } else {
            ""
        }

        val catalogCardsHtml = buildString {
            for (site in allFasSites.take(6)) {
                append("""
                    <a href="${site.fasUrl}" class="catalog-chip">
                        <span class="chip-badge">FAS</span>
                        <span class="chip-name">${site.name}</span>
                    </a>
                """.trimIndent())
            }
        }

        return """
            <!DOCTYPE html>
            <html lang="ru">
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <title>XFERWEN + FAS Поиск: $safeQuery</title>
                <style>
                    * { box-sizing: border-box; margin: 0; padding: 0; }
                    body {
                        background: radial-gradient(circle at 50% 0%, #061129 0%, #02040A 100%);
                        color: #E2E8F0;
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        min-height: 100vh;
                        padding: 16px;
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                    }
                    .container {
                        width: 100%;
                        max-width: 620px;
                        margin: 0 auto;
                    }
                    .header {
                        display: flex;
                        align-items: center;
                        gap: 12px;
                        margin-bottom: 16px;
                        padding: 8px 4px;
                    }
                    .logo-box {
                        width: 44px;
                        height: 44px;
                        border-radius: 12px;
                        background: linear-gradient(135deg, #0066FF, #00D4FF);
                        box-shadow: 0 0 15px rgba(0, 212, 255, 0.4);
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-weight: 900;
                        font-size: 20px;
                        color: #000;
                    }
                    .title-box h1 {
                        font-size: 20px;
                        font-weight: 800;
                        letter-spacing: 2px;
                        color: #FFF;
                    }
                    .title-box span {
                        font-size: 11px;
                        color: #00D4FF;
                        font-weight: 600;
                        letter-spacing: 0.5px;
                    }
                    .search-box {
                        display: flex;
                        background: rgba(10, 18, 40, 0.9);
                        border: 1px solid rgba(0, 212, 255, 0.5);
                        border-radius: 16px;
                        padding: 6px 10px;
                        margin-bottom: 20px;
                        box-shadow: 0 4px 20px rgba(0, 102, 255, 0.2);
                    }
                    .search-input {
                        flex: 1;
                        background: transparent;
                        border: none;
                        outline: none;
                        color: #FFF;
                        font-size: 15px;
                        font-weight: 600;
                        padding: 8px 10px;
                    }
                    .search-btn {
                        background: linear-gradient(135deg, #00D4FF, #0066FF);
                        border: none;
                        border-radius: 10px;
                        color: #000;
                        font-weight: 800;
                        font-size: 12px;
                        padding: 0 18px;
                        cursor: pointer;
                        letter-spacing: 1px;
                    }
                    .section-title {
                        font-size: 13px;
                        color: #A0AEC0;
                        font-weight: 700;
                        text-transform: uppercase;
                        letter-spacing: 1px;
                        margin: 18px 0 10px 4px;
                    }
                    .fas-card {
                        display: block;
                        background: rgba(13, 24, 56, 0.7);
                        border: 1px solid rgba(0, 212, 255, 0.35);
                        border-radius: 14px;
                        padding: 14px 16px;
                        margin-bottom: 10px;
                        text-decoration: none;
                        transition: transform 0.15s, border-color 0.15s;
                    }
                    .fas-card:hover {
                        border-color: #00D4FF;
                        transform: translateY(-2px);
                    }
                    .card-head {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        margin-bottom: 4px;
                    }
                    .card-title {
                        font-size: 16px;
                        font-weight: 700;
                        color: #FFF;
                    }
                    .badge-fas {
                        background: rgba(0, 212, 255, 0.15);
                        border: 1px solid #00D4FF;
                        color: #00D4FF;
                        font-size: 10px;
                        font-weight: 800;
                        padding: 2px 6px;
                        border-radius: 6px;
                    }
                    .card-domain {
                        font-size: 12px;
                        color: #00D4FF;
                        font-weight: 600;
                        margin-bottom: 8px;
                    }
                    .card-tags {
                        display: flex;
                        gap: 6px;
                    }
                    .tag {
                        background: rgba(255, 255, 255, 0.06);
                        border-radius: 999px;
                        padding: 2px 8px;
                        font-size: 10px;
                        color: #94A3B8;
                    }
                    .web-engines-box {
                        background: rgba(10, 18, 40, 0.6);
                        border: 1px solid rgba(255, 255, 255, 0.1);
                        border-radius: 16px;
                        padding: 16px;
                        margin-top: 14px;
                    }
                    .engine-btn {
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        width: 100%;
                        background: rgba(255, 255, 255, 0.04);
                        border: 1px solid rgba(0, 212, 255, 0.2);
                        border-radius: 12px;
                        padding: 12px 14px;
                        margin-bottom: 8px;
                        text-decoration: none;
                        color: #FFF;
                        font-size: 13px;
                        font-weight: 600;
                    }
                    .engine-btn:last-child { margin-bottom: 0; }
                    .engine-arrow {
                        color: #00D4FF;
                        font-weight: 800;
                        font-size: 16px;
                    }
                    .catalog-grid {
                        display: grid;
                        grid-template-columns: repeat(2, 1fr);
                        gap: 8px;
                        margin-top: 8px;
                    }
                    .catalog-chip {
                        display: flex;
                        align-items: center;
                        gap: 6px;
                        background: rgba(10, 18, 40, 0.8);
                        border: 1px solid rgba(0, 212, 255, 0.25);
                        border-radius: 10px;
                        padding: 8px 10px;
                        text-decoration: none;
                        color: #E2E8F0;
                        font-size: 12px;
                        font-weight: 600;
                    }
                    .chip-badge {
                        background: #00D4FF;
                        color: #000;
                        font-size: 8px;
                        font-weight: 900;
                        padding: 1px 4px;
                        border-radius: 4px;
                    }
                    .chip-name {
                        overflow: hidden;
                        text-overflow: ellipsis;
                        white-space: nowrap;
                    }
                    .footer-note {
                        text-align: center;
                        color: #64748B;
                        font-size: 11px;
                        margin-top: 24px;
                        line-height: 1.5;
                    }
                    .no-direct-match {
                        background: rgba(255, 255, 255, 0.03);
                        border: 1px dashed rgba(0, 212, 255, 0.3);
                        border-radius: 12px;
                        padding: 14px;
                        font-size: 13px;
                        color: #94A3B8;
                        line-height: 1.5;
                        margin-bottom: 12px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <!-- Header -->
                    <div class="header">
                        <div class="logo-box">X</div>
                        <div class="title-box">
                            <h1>XFERWEN + FAS</h1>
                            <span>КИБЕР-ПОИСК СИНТЕКС • ЭКОСИСТЕМА</span>
                        </div>
                    </div>

                    <!-- Search Input -->
                    <form class="search-box" action="https://xferwen.search/" method="GET">
                        <input type="text" name="q" class="search-input" value="$safeQuery" placeholder="Поиск в XFERWEN + FAS...">
                        <button type="submit" class="search-btn">НАЙТИ</button>
                    </form>

                    <!-- Matched FAS Sites -->
                    $matchedCardsHtml

                    <!-- Global Web Search Redirection via Milana Shield -->
                    <div class="section-title">Глобальный веб-поиск (без трекеров):</div>
                    <div class="web-engines-box">
                        <a href="https://yandex.ru/search/?text=$encodedQuery" class="engine-btn">
                            <span>🔍 Искать «<b>$safeQuery</b>» в Яндекс (Milana AI Shield)</span>
                            <span class="engine-arrow">→</span>
                        </a>
                        <a href="https://www.google.com/search?q=$encodedQuery" class="engine-btn">
                            <span>🌐 Искать «<b>$safeQuery</b>» в Google</span>
                            <span class="engine-arrow">→</span>
                        </a>
                        <a href="https://duckduckgo.com/?q=$encodedQuery" class="engine-btn">
                            <span>🦆 Искать «<b>$safeQuery</b>» в DuckDuckGo</span>
                            <span class="engine-arrow">→</span>
                        </a>
                    </div>

                    <!-- FAS Ecosystem Catalog -->
                    <div class="section-title">Каталог экосистемы FAS:</div>
                    <div class="catalog-grid">
                        $catalogCardsHtml
                    </div>

                    <!-- Footer -->
                    <div class="footer-note">
                        Поисковая система XFERWEN + FAS • Эксклюзивно для XFERWEN Browser<br>
                        SyntexHyperSoft Architecture • Защищено нейросетью Milana AI
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}
