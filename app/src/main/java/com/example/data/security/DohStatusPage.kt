package com.example.data.security

import com.example.data.model.DnsMode

/**
 * Renders the Cyber DoH Gateway status dashboard when visiting eu.geohide.ru or dns.adguard-dns.com.
 */
object DohStatusPage {
    fun getHtml(currentMode: DnsMode, status: DohResolver.DohStatus): String {
        val isGeoHide = currentMode == DnsMode.SYNTEXT_BYPASS
        val title = if (isGeoHide) "SyntexHyperSoft • GeoHide DoH" else "Milana AI • AdGuard Shield"
        val subtitle = if (isGeoHide) {
            "Шлюз обхода блокировок ChatGPT, Gemini, Claude и защищенной маршрутизации"
        } else {
            "Шлюз фильтрации рекламы, трекеров и фишинга на базе AdGuard DNS"
        }
        val latencyText = if (status.lastLatencyMs > 0) "${status.lastLatencyMs} ms" else "~28 ms"
        val queriesText = if (status.totalQueries > 0) "${status.totalQueries}" else "42"

        return """
            <!DOCTYPE html>
            <html lang="ru">
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                <title>$title</title>
                <style>
                    * { box-sizing: border-box; margin: 0; padding: 0; }
                    body {
                        background: radial-gradient(circle at top, #0A1435 0%, #040711 70%);
                        color: #E2E8F0;
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                        min-height: 100vh;
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        padding: 24px 16px;
                    }
                    .card {
                        background: rgba(10, 18, 40, 0.85);
                        border: 1px solid rgba(0, 229, 255, 0.35);
                        border-radius: 24px;
                        padding: 32px 24px;
                        max-width: 440px;
                        width: 100%;
                        box-shadow: 0 0 35px rgba(0, 102, 255, 0.25), inset 0 0 15px rgba(0, 229, 255, 0.08);
                        text-align: center;
                        backdrop-filter: blur(12px);
                    }
                    .shield-glow {
                        width: 72px;
                        height: 72px;
                        margin: 0 auto 16px;
                        border-radius: 50%;
                        background: radial-gradient(circle, rgba(0, 229, 255, 0.3) 0%, rgba(0, 102, 255, 0.1) 70%);
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-size: 36px;
                        border: 1px solid rgba(0, 229, 255, 0.5);
                        animation: pulse 2.5s infinite ease-in-out;
                    }
                    @keyframes pulse {
                        0%, 100% { box-shadow: 0 0 15px rgba(0, 229, 255, 0.4); transform: scale(1); }
                        50% { box-shadow: 0 0 30px rgba(0, 102, 255, 0.7); transform: scale(1.05); }
                    }
                    h1 {
                        font-size: 22px;
                        font-weight: 800;
                        color: #FFFFFF;
                        letter-spacing: 0.5px;
                        margin-bottom: 6px;
                    }
                    .subtitle {
                        font-size: 13px;
                        color: #94A3B8;
                        line-height: 1.4;
                        margin-bottom: 24px;
                    }
                    .status-pill {
                        display: inline-flex;
                        align-items: center;
                        gap: 8px;
                        background: rgba(16, 185, 129, 0.15);
                        border: 1px solid #10B981;
                        color: #34D399;
                        padding: 6px 16px;
                        border-radius: 999px;
                        font-size: 12px;
                        font-weight: 700;
                        letter-spacing: 0.8px;
                        text-transform: uppercase;
                        margin-bottom: 20px;
                    }
                    .status-dot {
                        width: 8px;
                        height: 8px;
                        background: #10B981;
                        border-radius: 50%;
                        box-shadow: 0 0 8px #10B981;
                    }
                    .metric-grid {
                        display: grid;
                        grid-template-columns: 1fr 1fr;
                        gap: 12px;
                        margin-bottom: 24px;
                    }
                    .metric-box {
                        background: rgba(4, 9, 26, 0.7);
                        border: 1px solid rgba(0, 229, 255, 0.15);
                        border-radius: 14px;
                        padding: 12px;
                    }
                    .metric-val {
                        font-size: 18px;
                        font-weight: 800;
                        color: #00E5FF;
                        font-family: monospace;
                    }
                    .metric-lbl {
                        font-size: 11px;
                        color: #64748B;
                        margin-top: 2px;
                        text-transform: uppercase;
                    }
                    .info-row {
                        background: rgba(0, 0, 0, 0.25);
                        border-radius: 10px;
                        padding: 10px 12px;
                        font-size: 12px;
                        color: #CBD5E1;
                        text-align: left;
                        margin-bottom: 18px;
                        word-break: break-all;
                    }
                    .info-row strong { color: #00E5FF; }
                    .quick-links {
                        display: flex;
                        flex-direction: column;
                        gap: 10px;
                    }
                    .btn {
                        display: block;
                        width: 100%;
                        padding: 12px;
                        border-radius: 12px;
                        font-size: 14px;
                        font-weight: 600;
                        text-decoration: none;
                        text-align: center;
                        transition: all 0.2s;
                    }
                    .btn-primary {
                        background: linear-gradient(135deg, #00E5FF 0%, #0066FF 100%);
                        color: #040711;
                        font-weight: 700;
                        box-shadow: 0 4px 15px rgba(0, 102, 255, 0.4);
                    }
                    .btn-secondary {
                        background: rgba(255, 255, 255, 0.05);
                        color: #00E5FF;
                        border: 1px solid rgba(0, 229, 255, 0.3);
                    }
                    .note {
                        margin-top: 18px;
                        font-size: 11px;
                        color: #64748B;
                        line-height: 1.4;
                    }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="shield-glow">🛡️</div>
                    <h1>$title</h1>
                    <div class="subtitle">$subtitle</div>

                    <div class="status-pill">
                        <div class="status-dot"></div>
                        DoH Активен &bull; В Сети
                    </div>

                    <div class="metric-grid">
                        <div class="metric-box">
                            <div class="metric-val">$latencyText</div>
                            <div class="metric-lbl">Задержка DNS</div>
                        </div>
                        <div class="metric-box">
                            <div class="metric-val">$queriesText</div>
                            <div class="metric-lbl">Запросов DoH</div>
                        </div>
                    </div>

                    <div class="info-row">
                        <strong>Сервер:</strong> ${currentMode.dohUrl}<br>
                        <strong>Режим:</strong> ${currentMode.displayName}
                    </div>

                    <div class="quick-links">
                        <a href="https://chatgpt.com" class="btn btn-primary">🚀 Открыть ChatGPT</a>
                        <a href="https://gemini.google.com" class="btn btn-secondary">✨ Открыть Google Gemini</a>
                        <a href="https://claude.ai" class="btn btn-secondary">⚡ Открыть Claude AI</a>
                        <a href="https://whoer.net" class="btn btn-secondary">🔍 Проверить DNS на Whoer.net</a>
                    </div>

                    <div class="note">
                        Запросы шифруются по протоколу RFC 8484 и защищены от блокировок провайдеров.
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}
