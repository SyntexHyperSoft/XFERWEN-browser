package com.example.data.security

/**
 * Advanced Anti-Bot & Captcha Cloaking Engine for XFERWEN.
 * Prevents websites (Yandex SmartCaptcha, Cloudflare Turnstile, reCAPTCHA, Google)
 * from flagging the browser as an automated crawler or emulator.
 */
object StealthAntiBot {

    // Clean, genuine Mobile Chrome on Android 14 User-Agent (No emulator or webview flags)
    const val REAL_MOBILE_CHROME_UA =
        "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.200 Mobile Safari/537.36"

    const val REAL_DESKTOP_CHROME_UA =
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.6778.200 Safari/537.36"

    /**
     * Script injected before any page scripts to mask automated/emulator environment.
     */
    fun getStealthJsScript(): String {
        return """
            (function() {
                try {
                    // 1. Remove navigator.webdriver detection (must be false in real Chrome)
                    Object.defineProperty(navigator, 'webdriver', {
                        get: () => false,
                        configurable: true
                    });
                    try {
                        delete navigator.__proto__.webdriver;
                    } catch(_) {}

                    // 1.1 Emulate Client Hints (navigator.userAgentData)
                    if (!navigator.userAgentData) {
                        Object.defineProperty(navigator, 'userAgentData', {
                            get: () => ({
                                brands: [
                                    { brand: "Chromium", version: "131" },
                                    { brand: "Google Chrome", version: "131" },
                                    { brand: "Not_A Brand", version: "24" }
                                ],
                                mobile: true,
                                platform: "Android",
                                getHighEntropyValues: function() {
                                    return Promise.resolve({
                                        architecture: "arm",
                                        bitness: "64",
                                        mobile: true,
                                        model: "Pixel 8 Pro",
                                        platform: "Android",
                                        platformVersion: "14.0.0"
                                    });
                                }
                            }),
                            configurable: true
                        });
                    }

                    // 2. Mock realistic Chrome runtime environment
                    if (!window.chrome) {
                        window.chrome = {
                            app: { isInstalled: false },
                            runtime: {
                                OnInstalledReason: { INSTALL: "install", UPDATE: "update", CHROME_UPDATE: "chrome_update" },
                                PlatformOs: { ANDROID: "android" },
                                PlatformArch: { ARM: "arm", ARM64: "arm64" }
                            },
                            csi: function() {},
                            loadTimes: function() {
                                return {
                                    commitLoadTime: Date.now() / 1000,
                                    connectionInfo: "h2",
                                    finishDocumentLoadTime: Date.now() / 1000,
                                    finishLoadTime: Date.now() / 1000,
                                    firstPaintAfterLoadTime: 0,
                                    firstPaintTime: Date.now() / 1000,
                                    navigationType: "Other",
                                    npnNegotiatedProtocol: "h2",
                                    requestTime: Date.now() / 1000,
                                    startLoadTime: Date.now() / 1000,
                                    wasAlternateProtocolAvailable: false,
                                    wasFetchedViaSpdy: true,
                                    wasNpnNegotiated: true
                                };
                            }
                        };
                    }

                    // 3. Human hardware parameters & multi-touch screen
                    Object.defineProperty(navigator, 'maxTouchPoints', { get: () => 5, configurable: true });
                    Object.defineProperty(navigator, 'hardwareConcurrency', { get: () => 8, configurable: true });
                    Object.defineProperty(navigator, 'deviceMemory', { get: () => 8, configurable: true });
                    Object.defineProperty(navigator, 'platform', { get: () => 'Linux armv8l', configurable: true });
                    Object.defineProperty(navigator, 'languages', { get: () => ['ru-RU', 'ru', 'en-US', 'en'], configurable: true });

                    // 4. Mask WebGL vendor / renderer to realistic mobile GPU (ARM Mali-G715)
                    const getParameterProto = WebGLRenderingContext.prototype.getParameter;
                    WebGLRenderingContext.prototype.getParameter = function(parameter) {
                        if (parameter === 37445) return 'ARM'; // UNMASKED_VENDOR_WEBGL
                        if (parameter === 37446) return 'Mali-G715-Immortalis MC11'; // UNMASKED_RENDERER_WEBGL
                        return getParameterProto.apply(this, arguments);
                    };

                    if (window.WebGL2RenderingContext) {
                        const getParameter2Proto = WebGL2RenderingContext.prototype.getParameter;
                        WebGL2RenderingContext.prototype.getParameter = function(parameter) {
                            if (parameter === 37445) return 'ARM';
                            if (parameter === 37446) return 'Mali-G715-Immortalis MC11';
                            return getParameter2Proto.apply(this, arguments);
                        };
                    }

                    // 5. Clean Permissions query
                    if (navigator.permissions && navigator.permissions.query) {
                        const origQuery = navigator.permissions.query;
                        navigator.permissions.query = function(params) {
                            if (params && params.name === 'notifications') {
                                return Promise.resolve({ state: Notification.permission || 'default' });
                            }
                            return origQuery.apply(this, arguments);
                        };
                    }
                } catch(e) {}
            })();
        """.trimIndent()
    }
}
