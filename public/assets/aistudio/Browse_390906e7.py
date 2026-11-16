import sys
import os
import re
import json
import time
import sqlite3
import platform
import subprocess

from PyQt6.QtWidgets import (
    QApplication, QMainWindow, QVBoxLayout, QHBoxLayout,
    QWidget, QPushButton, QLineEdit, QTabWidget, QMessageBox,
    QDialog, QLabel, QListWidget, QListWidgetItem, QProgressBar,
    QCheckBox, QPlainTextEdit, QInputDialog
)
from PyQt6.QtWebEngineWidgets import QWebEngineView
from PyQt6.QtCore import (
    QUrl, Qt, QObject, pyqtSlot, pyqtSignal, QPoint, QThread, QFile, QIODevice, QSize, QEvent,
    QTimer, QPropertyAnimation, QEasingCurve
)
from PyQt6.QtGui import QMouseEvent, QIcon, QDesktopServices, QPainter, QPainterPath, QPen, QColor
from PyQt6.QtNetwork import QNetworkCookie
from PyQt6.QtWebChannel import QWebChannel
from PyQt6.QtWebEngineCore import (
    QWebEngineProfile, QWebEnginePage, QWebEngineSettings,
    QWebEngineScript, QWebEngineUrlRequestInterceptor, QWebEngineUrlRequestInfo,
    QWebEngineDownloadRequest
)

HOME_DISPLAY = "FAS://XFERVEN.browse.com/home"
QUADRO_DISPLAY = "FAS://QuadroOS.sys"
QUADRO_REAL_URL = "https://mrfeastproject.github.io/QuadroOS-Telegram-Mini-App-Mini-OS-/System.html"

def init_paths():
    base = os.path.join(os.environ.get("PROGRAMDATA", "C:\\ProgramData"), "XFERVEN Browse")
    try:
        os.makedirs(base, exist_ok=True)
        t = os.path.join(base, ".rw_test")
        with open(t, "w") as f:
            f.write("1")
        os.remove(t)
    except Exception:
        base = os.path.abspath("xferven_data")
        os.makedirs(base, exist_ok=True)
    
    paths = {
        "base": base,
        "password": os.path.join(base, "Password"),
        "data": os.path.join(base, "Data"),
        "other": os.path.join(base, "Other"),
        "defender": os.path.join(base, "XFERVEN Defender"),
        "downloads": os.path.join(os.path.expanduser("~"), "Downloads"),
        "resources": os.path.join(base, "Data", "Resources"),
    }
    for k in paths: 
        if k != "downloads": os.makedirs(paths[k], exist_ok=True)
    return paths

PATHS = init_paths()
USER_FILE = os.path.join(PATHS["other"], "user.json")

def load_user_name():
    try:
        with open(USER_FILE, "r", encoding="utf-8") as f:
            return (json.load(f).get("name") or "").strip()
    except Exception:
        return ""

def save_user_name(n):
    try:
        with open(USER_FILE, "w", encoding="utf-8") as f:
            json.dump({"name": n}, f)
    except Exception as e:
        print(f"⚠️ Не удалось сохранить имя: {e}")

# ============================================================
# БАЗЫ ДАННЫХ
# ============================================================

BUILTIN_TRACKERS = {
    "google-analytics.com", "googletagmanager.com", "googlesyndication.com",
    "googleadservices.com", "doubleclick.net", "ad.doubleclick.net",
    "adservice.google.com", "analytics.google.com", "pagead2.googlesyndication.com",
    "scorecardresearch.com", "quantserve.com", "quantcount.com",
    "chartbeat.com", "chartbeat.net", "hotjar.com", "mixpanel.com",
    "amplitude.com", "segment.com", "segment.io", "criteo.com", "criteo.net",
    "outbrain.com", "taboola.com", "mgid.com", "adroll.com",
    "mc.yandex.ru", "an.yandex.ru", "connect.facebook.net", "pixel.facebook.com",
    "snap.licdn.com", "px.ads.linkedin.com", "ads.linkedin.com",
    "analytics.twitter.com", "ads-twitter.com", "analytics.tiktok.com",
    "ct.pinterest.com", "amazon-adsystem.com", "aax.amazon-adsystem.com",
    "fls-na.amazon.com", "adnxs.com", "rubiconproject.com", "openx.net",
    "pubmatic.com", "casalemedia.com", "33across.com", "moatads.com",
    "moatpixel.com", "appnexus.com", "tealiumiq.com", "ensighten.com",
    "fullstory.com", "contentsquare.net", "clicktale.net",
    "exoclick.com", "exdynsrv.com", "adsterra.com", "propellerads.com",
    "popcash.net", "popads.net", "revcontent.com", "content.ad",
    "coinhive.com", "coin-hive.com", "authedmine.com", "cryptoloot.pro",
    "platform.twitter.com", "syndication.twitter.com", "widgets.redditmedia.com",
    "assets.pinterest.com", "assets.tumblr.com", "static.addtoany.com",
    "w.sharethis.com", "static.getbutton.io", "share.pluso.ru", "connect.ok.ru",
    "fingerprint.com", "api.fpjs.io", "metrics.fpjs.io", "openfpcdn.io",
    "iovation.com", "online-metrix.net", "h.online-metrix.net"
}

YOUTUBE_WHITELIST = {
    "youtube.com", "www.youtube.com", "m.youtube.com", "youtu.be",
    "googlevideo.com", "ggpht.com", "ytimg.com", "youtube-nocookie.com",
    "youtube-ui.l.google.com", "youtubei.googleapis.com"
}

LIST_URLS = {
    "adguard_base": ("https://filters.adtidy.org/android/filters/2_optimized.txt", "ad"),
    "adguard_spyware": ("https://filters.adtidy.org/android/filters/3_optimized.txt", "tracker"),
    "dandelion_malware": ("https://raw.githubusercontent.com/DandelionSprout/adfilt/master/Alternate%20versions%20Anti-Malware%20List/AntiMalwareAdGuard.txt", "malware"),
    "ublock_badware": ("https://raw.githubusercontent.com/uBlockOrigin/uAssets/master/filters/badware.txt", "malware"),
    "scamblocklist": ("https://raw.githubusercontent.com/durablenapkin/scamblocklist/master/adguard.txt", "phishing"),
    "phishing_filter": ("https://malware-filter.gitlab.io/malware-filter/phishing-filter-ag.txt", "phishing"),
    "hosts": ("https://raw.githubusercontent.com/StevenBlack/hosts/master/hosts", "ad"),
}

def parse_list(raw, cat):
    hosts, paths, malware = set(), {}, set()
    is_threat = cat in ("malware", "phishing")
    for line in raw.splitlines():
        line = line.strip()
        if not line or line.startswith(("!", "#", "[")): continue
        if line.startswith(("0.0.0.0", "127.0.0.1")):
            parts = line.split()
            if len(parts) >= 2 and parts[1] != "localhost":
                h = parts[1].lower()
                hosts.add(h)
                if is_threat: malware.add(h)
        elif line.startswith("||"):
            body = line[2:].split("$")[0].rstrip("^")
            if "*" in body.split("/")[0]: continue
            if "/" in body:
                d, p = body.split("/", 1)
                d = d.lower()
                paths.setdefault(d, []).append("/" + p.split("^")[0])
                if is_threat: malware.add(d)
            else:
                d = body.lower()
                hosts.add(d)
                if is_threat: malware.add(d)
    return hosts, paths, malware

class ListsDownloader(QThread):
    done = pyqtSignal(dict)
    def run(self):
        import urllib.request
        out = {}
        for name, (url, cat) in LIST_URLS.items():
            try:
                req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
                with urllib.request.urlopen(req, timeout=60) as r:
                    out[name] = (r.read().decode("utf-8", "ignore"), cat)
            except: out[name] = ("", cat)
        self.done.emit(out)

class Defender(QObject):
    def __init__(self):
        super().__init__()
        self.enabled = True
        self.hosts = set()
        self.malware_hosts = set()
        self.whitelist = set()
        
        db_file = os.path.join(PATHS["defender"], "Phishing URL DataBase.txt")
        if os.path.exists(db_file):
            try:
                with open(db_file, "r", encoding="utf-8") as f:
                    for l in f:
                        if l.startswith("||"): self.hosts.add(l[2:].split("^")[0].lower())
            except: pass

    def update_malware(self, hosts):
        self.malware_hosts = hosts
        print(f"🛡 Defender: {len(hosts)} malware/phishing domains loaded")

    def is_phishing(self, url):
        h = (url.host() or "").lower()
        if not h or h in self.whitelist: return False
        parts = h.split(".")
        for i in range(len(parts)):
            d = ".".join(parts[i:])
            if d in self.hosts or d in self.malware_hosts: return True
        return False

class AdBlockInterceptor(QWebEngineUrlRequestInterceptor):
    blocked = pyqtSignal(str, str)
    def __init__(self, defender):
        super().__init__()
        self.defender = defender
        self.enabled = True
        self.hosts = set(BUILTIN_TRACKERS)
        self.cats = {h: "tracker" for h in BUILTIN_TRACKERS}
        self.paths = {}
        self.stats = {}
        self.cache = os.path.join(PATHS["other"], "cache.json")
        
        # ИСПРАВЛЕНИЕ: dl_thread вместо thread
        self.dl_thread = ListsDownloader()
        self.dl_thread.done.connect(self.apply_lists)

        if os.path.exists(self.cache):
            try:
                with open(self.cache, "r") as f: d = json.load(f)
                self.hosts.update(d.get("hosts", []))
                self.cats.update(d.get("cats", {}))
                self.paths = d.get("paths", {})
                self.defender.update_malware(set(d.get("malware", [])))
            except: self.download()
        else: self.download()

    def download(self): 
        self.dl_thread.start()

    def apply_lists(self, data):
        new_hosts, new_paths, new_malware = set(), {}, set()
        for name, (raw, cat) in data.items():
            if not raw: continue
            h, p, m = parse_list(raw, cat)
            new_hosts.update(h)
            for k, v in p.items(): new_paths.setdefault(k, []).extend(v)
            new_malware.update(m)
            for x in h: self.cats[x] = cat
        
        self.hosts.update(new_hosts)
        self.paths.update(new_paths)
        self.defender.update_malware(new_malware)
        
        try:
            with open(self.cache, "w") as f:
                json.dump({"hosts": list(self.hosts), "cats": self.cats, "paths": self.paths, "malware": list(new_malware)}, f)
        except: pass
        print(f"🛡 Lists updated: {len(self.hosts)} hosts, {len(new_malware)} threats")

    def interceptRequest(self, info):
        if not self.enabled: return
        url = info.requestUrl()
        host = url.host()
        if not host: return
        
        if any(host == y or host.endswith("." + y) for y in YOUTUBE_WHITELIST): return

        parts = host.split(".")
        for i in range(len(parts)):
            d = ".".join(parts[i:])
            if d in self.defender.malware_hosts or d in self.defender.hosts:
                info.block(True)
                self._hit(d, "malware")
                return

        is_main = info.resourceType() == QWebEngineUrlRequestInfo.ResourceType.ResourceTypeMainFrame
        if not is_main:
            for i in range(len(parts)):
                d = ".".join(parts[i:])
                if d in self.hosts:
                    info.block(True)
                    self._hit(d, self.cats.get(d, "ad"))
                    return
            path = url.path().lower()
            for i in range(len(parts)):
                d = ".".join(parts[i:])
                if d in self.paths:
                    for p in self.paths[d]:
                        if path.startswith(p):
                            info.block(True)
                            self._hit(d, "ad")
                            return

    def _hit(self, d, c):
        self.stats.setdefault(d, [0, c])[0] += 1
        self.blocked.emit(d, c)

# ============================================================
# ХРАНИЛИЩА
# ============================================================
class HistoryDB:
    def __init__(self):
        self.conn = sqlite3.connect(os.path.join(PATHS["other"], "history.db"))
        self.conn.execute("CREATE TABLE IF NOT EXISTS v(id INTEGER PRIMARY KEY, u TEXT, t TEXT, ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP)")
        self.conn.commit()
    def add(self, u, t): self.conn.execute("INSERT INTO v(u,t) VALUES(?,?)", (u, t)); self.conn.commit()
    def get(self): return self.conn.execute("SELECT u,t FROM v ORDER BY id DESC LIMIT 200").fetchall()
    def clear(self): self.conn.execute("DELETE FROM v"); self.conn.commit()

class PasswordStore:
    def __init__(self):
        self.file = os.path.join(PATHS["password"], "pass.json")
        self.key_file = os.path.join(PATHS["password"], "key.key")
        self.fernet = None
        try:
            from cryptography.fernet import Fernet
            if os.path.exists(self.key_file):
                with open(self.key_file, "rb") as f: k = f.read()
            else:
                k = Fernet.generate_key()
                with open(self.key_file, "wb") as f: f.write(k)
            self.fernet = Fernet(k)
        except: pass
        self.data = {}
        if os.path.exists(self.file):
            try:
                with open(self.file) as f: self.data = json.load(f)
            except: pass

    def save(self, h, u, p):
        self.data[h] = {"u": u, "p": self.fernet.encrypt(p.encode()).decode() if self.fernet else p}
        with open(self.file, "w") as f: json.dump(self.data, f)
    
    def get(self, h):
        d = self.data.get(h)
        if not d: return None
        try: p = self.fernet.decrypt(d["p"].encode()).decode() if self.fernet else d["p"]
        except: p = d["p"]
        return (d["u"], p)

    def items(self): return [(h, d["u"]) for h, d in self.data.items()]
    def delete(self, h): self.data.pop(h, None); self.save()
    def clear(self): self.data = {}; self.save()

class SettingsStore:
    def __init__(self):
        self.file = os.path.join(PATHS["other"], "settings.json")
        self.vals = {"defender": True, "adblock": True, "pass": True, "hist": True}
        if os.path.exists(self.file):
            try:
                with open(self.file) as f: self.vals.update(json.load(f))
            except: pass
    def get(self, k): return self.vals.get(k, True)
    def set(self, k, v): self.vals[k] = v; open(self.file, "w").write(json.dumps(self.vals))

# ============================================================
# DOWNLOAD TOAST
# ============================================================
class DownloadToast(QWidget):
    def __init__(self, item, manager):
        super().__init__(None, Qt.WindowType.FramelessWindowHint | Qt.WindowType.WindowStaysOnTopHint | Qt.WindowType.Tool)
        self.setAttribute(Qt.WidgetAttribute.WA_TranslucentBackground)
        self.item, self.manager = item, manager
        self.setFixedSize(360, 118)
        self.recv, self.total, self.t0 = 0, -1, time.time()
        
        self.icon = QLabel("📥", self); self.icon.setGeometry(14, 14, 46, 46); self.icon.setAlignment(Qt.AlignmentFlag.AlignCenter)
        self.name = QLabel("file", self); self.name.setGeometry(72, 12, 274, 20); self.name.setStyleSheet("color:#fff;font-weight:bold;")
        self.meta = QLabel("", self); self.meta.setGeometry(72, 32, 274, 16); self.meta.setStyleSheet("color:#8b7bb8;font-size:11px;")
        self.size_lbl = QLabel("0 B", self); self.size_lbl.setGeometry(72, 74, 190, 16); self.size_lbl.setStyleSheet("color:#8b7bb8;font-size:11px;")
        self.speed_lbl = QLabel("0 B/s", self); self.speed_lbl.setGeometry(262, 74, 84, 16); self.speed_lbl.setStyleSheet("color:#a78bfa;font-weight:bold;font-size:11px;")
        self.bar = QProgressBar(self); self.bar.setGeometry(72, 96, 274, 8); self.bar.setTextVisible(False)
        self.bar.setStyleSheet("QProgressBar{background:#241238;border-radius:4px} QProgressBar::chunk{background:qlineargradient(x1:0,y1:0,x2:1,y2:0,stop:0 #7c3aed,stop:1 #ec4899);border-radius:4px}")

        fname = item.downloadFileName() or "file"
        ext = fname.split(".")[-1].lower() if "." in fname else ""
        icons = {"mp3":"🎵", "mp4":"🎬", "zip":"📦", "exe":"⚙️", "pdf":"📄", "png":"🖼️", "jpg":"🖼️"}
        self.icon.setText(icons.get(ext, "📥"))
        self.name.setText(fname)
        self.meta.setText(f".{ext} file" if ext else "file")

        try: item.downloadProgress.connect(self.on_prog)
        except: pass
        try: item.isFinishedChanged.connect(self.on_finish)
        except: 
            try: item.stateChanged.connect(self.on_state)
            except: pass

        self.timer = QTimer(self); self.timer.timeout.connect(self.tick); self.timer.start(1000)

    def paintEvent(self, e):
        p = QPainter(self); p.setRenderHint(QPainter.RenderHint.Antialiasing)
        path = QPainterPath(); path.addRoundedRect(0, 0, self.width(), self.height(), 18, 18)
        p.fillPath(path, QColor(18, 10, 31, 240)); p.strokePath(path, QPen(QColor(124, 58, 237, 100), 1))

    def on_prog(self, r, t):
        self.recv, self.total = r, t
        if t > 0: self.bar.setValue(int(r*100/t)); self.size_lbl.setText(f"{r//1024} KB / {t//1024} KB")
        else: self.size_lbl.setText(f"{r//1024} KB")

    def tick(self):
        now = time.time(); dt = now - self.t0
        if dt > 0: self.speed_lbl.setText(f"{int((self.recv - getattr(self, 'last_r', 0))/dt)} B/s")
        self.last_r, self.t0 = self.recv, now

    def on_finish(self): self.close_anim()
    def on_state(self, s):
        if s == QWebEngineDownloadRequest.DownloadState.DownloadCompleted: self.close_anim()
        elif s in (QWebEngineDownloadRequest.DownloadState.DownloadCancelled, QWebEngineDownloadRequest.DownloadState.DownloadInterrupted): self.close()

    def close_anim(self):
        self.timer.stop()
        self.speed_lbl.setText("✓ Done")
        self.bar.setValue(100)
        QTimer.singleShot(2000, self.close)

    def show_anim(self):
        self.show(); self.raise_()
        try:
            target = self.pos()
            self.move(target.x(), target.y() + 50)
            anim = QPropertyAnimation(self, b"pos")
            anim.setDuration(300); anim.setStartValue(self.pos()); anim.setEndValue(target)
            anim.setEasingCurve(QEasingCurve.Type.OutCubic)
            self.anim = anim; anim.start()
        except: pass

class ToastManager:
    def __init__(self): self.toasts = []
    def add(self, item):
        t = DownloadToast(item, self)
        self.toasts.append(t); self.layout(); t.show_anim()
    def layout(self):
        try:
            geo = QApplication.primaryScreen().availableGeometry()
            y = geo.bottom() - 20
            for t in reversed(self.toasts):
                y -= 130; t.move(geo.right() - 380, y)
        except: pass

# ============================================================
# HANDLERS
# ============================================================
class Handler(QObject):
    def __init__(self, win):
        super().__init__(); self.win = win
    
    @pyqtSlot(str)
    def navigateTo(self, url):
        if url.startswith("http://"): url = "https://" + url[7:]
        self.win.current_view().setUrl(QUrl(url))

    @pyqtSlot()
    def goHome(self):
        v = self.win.current_view()
        if v:
            pid = id(v.page())
            if pid in self.win.quadro_pages:
                self.win.quadro_pages.discard(pid)
                if self.win.isFullScreen(): self.win.showNormal(); self.win.tabs.tabBar().show(); self.win.nav_bar.show()
            v.setUrl(QUrl(self.win.home_url))

    @pyqtSlot()
    def goBack(self): 
        if self.win.current_view(): self.win.current_view().back()
    @pyqtSlot()
    def goForward(self): 
        if self.win.current_view(): self.win.current_view().forward()
    @pyqtSlot()
    def reload(self): 
        if self.win.current_view(): self.win.current_view().reload()

    @pyqtSlot(result=str)
    def getUserName(self): return self.win.user_name or "User"

    @pyqtSlot(str, str, str)
    def saveCredentials(self, url, user, pwd):
        if not self.win.settings.get("pass"): return
        h = QUrl(url).host()
        if h and user and pwd and not self.win.passwords.get(h):
            self.win.passwords.save(h, user, pwd)

    @pyqtSlot(result=str)
    def getSystemInfo(self):
        gpu, cpu = "Unknown", "Unknown"
        try:
            if platform.system() == "Windows":
                r = subprocess.run(["wmic", "path", "win32_VideoController", "get", "name"], capture_output=True, text=True, timeout=2)
                gpu = [l.strip() for l in r.stdout.split("\n") if l.strip() and "Name" not in l][0] if r.returncode == 0 else "Unknown"
                cpu = platform.processor()
        except: pass
        return json.dumps({"gpu": gpu, "cpu": cpu})

    @pyqtSlot(result=str)
    def getStats(self): return json.dumps(self.win.adblock.stats)
    
    @pyqtSlot(result=str)
    def getHistory(self): return json.dumps([{"u": u, "t": t} for u, t in self.win.history.get()])
    
    @pyqtSlot(result=str)
    def getPasswords(self): return json.dumps([{"h": h, "u": u} for h, u in self.win.passwords.items()])
    
    @pyqtSlot(result=str)
    def getSettings(self): return json.dumps(self.win.settings.vals)

    @pyqtSlot()
    def clearHistory(self): self.win.history.clear()
    @pyqtSlot()
    def clearPasswords(self): self.win.passwords.clear()
    @pyqtSlot()
    def clearCookies(self): self.win.profile.cookieStore().deleteAllCookies(); self.win.profile.clearHttpCache()
    
    @pyqtSlot(str, bool)
    def setSetting(self, k, v):
        self.win.settings.set(k, v)
        if k == "defender": self.win.defender.enabled = v
        if k == "adblock": self.win.adblock.enabled = v; self.win.update_shield()

    @pyqtSlot(str)
    def deletePassword(self, h): self.win.passwords.delete(h)
    
    @pyqtSlot(str)
    def openExternal(self, url): QDesktopServices.openUrl(QUrl(url))

class DefenderHandler(QObject):
    def __init__(self, win): super().__init__(); self.win = win
    @pyqtSlot()
    def exitPage(self): self.win.exit_defender()
    @pyqtSlot()
    def requestContinue(self): self.win.continue_defender()

# ============================================================
# MAIN WINDOW
# ============================================================
class SynaptexBrowser(QMainWindow):
    def __init__(self, user_name):
        super().__init__()
        self.setWindowTitle("XFERVEN Browser")
        self.resize(1280, 800)
        self.setStyleSheet("background:#0a0514;")
        self.user_name = user_name
        self.quadro_pages = set()
        self.defender_pages = {}
        self.last_defender_page = None
        self.pending_phish_url = ""
        self.toasts = ToastManager()

        self.settings = SettingsStore()
        self.history = HistoryDB()
        self.passwords = PasswordStore()
        self.defender = Defender()
        self.defender.enabled = self.settings.get("defender")
        self.adblock = AdBlockInterceptor(self.defender)
        self.adblock.enabled = self.settings.get("adblock")

        self.handler = Handler(self)
        self.defender_handler = DefenderHandler(self)

        icon_path = os.path.join(PATHS["resources"], "XFERVEN.ico")
        if os.path.exists(icon_path): self.setWindowIcon(QIcon(icon_path))

        central = QWidget(); self.setCentralWidget(central)
        lay = QVBoxLayout(central); lay.setContentsMargins(0,0,0,0); lay.setSpacing(0)

        self.profile = QWebEngineProfile("xferven", self)
        try:
            from PyQt6.QtWebEngineCore import qWebEngineChromiumVersion
            ver = qWebEngineChromiumVersion().split(".")[0]
        except: ver = "120"
        self.profile.setHttpUserAgent(f"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/{ver}.0.0.0 Safari/537.36")
        self.profile.setCachePath(os.path.join(PATHS["data"], "cache"))
        self.profile.setPersistentStoragePath(os.path.join(PATHS["data"], "storage"))
        self.profile.setDownloadPath(PATHS["downloads"])
        self.profile.downloadRequested.connect(self.on_download)
        self.profile.setUrlRequestInterceptor(self.adblock)
        
        try:
            def cookie_filter(info):
                d = info.cookie().domain().lstrip(".").lower()
                f = info.firstPartyUrl().host().lower()
                if "google" in d or "google" in f: return True
                p_d = d.split(".")[-2:] if len(d.split("."))>=2 else d
                p_f = f.split(".")[-2:] if len(f.split("."))>=2 else f
                return ".".join(p_d) == ".".join(p_f)
            self.profile.cookieStore().setCookieFilter(cookie_filter)
        except: pass

        self.home_url = QUrl.fromLocalFile(os.path.abspath("interface.html")).toString()
        self.qwc_js = QFile(":/qtwebchannel/qwebchannel.js")
        if self.qwc_js.open(QIODevice.OpenModeFlag.ReadOnly):
            self.qwc_js = self.qwc_js.readAll().data().decode()
        else: self.qwc_js = ""

        self.nav_bar = QWidget(); self.nav_bar.setFixedHeight(48)
        nav_lay = QHBoxLayout(self.nav_bar); nav_lay.setContentsMargins(8,6,8,6)
        
        def btn(txt, tip):
            b = QPushButton(txt); b.setFixedSize(40, 36); b.setToolTip(tip)
            b.setStyleSheet("QPushButton{background:transparent;color:#c4b5fd;border:none;border-radius:8px;font-size:16px} QPushButton:hover{background:#2d1b4e}")
            return b

        self.back_btn = btn("←", "Back"); self.fwd_btn = btn("→", "Forward")
        self.rel_btn = btn("⟳", "Reload"); self.home_btn = btn("⌂", "Home")
        self.url_input = QLineEdit(); self.url_input.setPlaceholderText("Search or URL...")
        self.url_input.setStyleSheet("QLineEdit{background:#1a0b2e;color:#e9d5ff;border:1px solid #7c3aed;border-radius:12px;padding:6px 14px} QLineEdit:focus{border-color:#c4b5fd}")
        self.url_input.returnPressed.connect(self.on_url_entered)
        self.url_input.installEventFilter(self)
        
        self.shield_btn = btn("🛡 0", "Protection"); self.shield_btn.setFixedWidth(64)
        self.hist_btn = btn("🕘", "History"); self.pass_btn = btn("🔑", "Passwords")
        self.set_btn = btn("⚙️", "Settings")

        for b in [self.back_btn, self.fwd_btn, self.rel_btn, self.home_btn]: nav_lay.addWidget(b)
        nav_lay.addWidget(self.url_input, 1)
        for b in [self.shield_btn, self.hist_btn, self.pass_btn, self.set_btn]: nav_lay.addWidget(b)
        
        self.nav_bar.setStyleSheet("#navBar{background:qlineargradient(x1:0,y1:0,x2:0,y2:1,stop:0 #241238,stop:1 #12081f);border-bottom:2px solid #7c3aed}")
        lay.addWidget(self.nav_bar)

        self.tabs = QTabWidget(); self.tabs.setTabsClosable(True); self.tabs.setMovable(True)
        self.tabs.setStyleSheet("QTabWidget::pane{border:none} QTabBar::tab{background:#160a26;color:#9d8bbd;padding:8px 20px;border-top-left-radius:10px;border-top-right-radius:10px;margin-right:2px} QTabBar::tab:selected{background:#241238;color:#fff}")
        self.tabs.tabCloseRequested.connect(self.close_tab)
        self.tabs.currentChanged.connect(self.on_tab_changed)
        self.plus_tab = QWidget(); self.tabs.addTab(self.plus_tab, "+")
        lay.addWidget(self.tabs)

        self.back_btn.clicked.connect(self.handler.goBack)
        self.fwd_btn.clicked.connect(self.handler.goForward)
        self.rel_btn.clicked.connect(self.handler.reload)
        self.home_btn.clicked.connect(self.handler.goHome)
        self.shield_btn.clicked.connect(lambda: self.open_menu("stats"))
        self.hist_btn.clicked.connect(lambda: self.open_menu("history"))
        self.pass_btn.clicked.connect(lambda: self.open_menu("passwords"))
        self.set_btn.clicked.connect(lambda: self.open_menu("settings"))
        self.adblock.blocked.connect(lambda h, c: self.update_shield())

        self.add_tab()

    def current_view(self):
        w = self.tabs.currentWidget()
        return w if isinstance(w, QWebEngineView) else None

    def add_tab(self, url=None):
        v = QWebEngineView()
        p = QWebEnginePage(self.profile, v)
        p.settings().setAttribute(QWebEngineSettings.WebAttribute.PlaybackRequiresUserGesture, False)
        p.settings().setAttribute(QWebEngineSettings.WebAttribute.LocalContentCanAccessFileUrls, True)
        v.setPage(p)
        
        ch = QWebChannel(p)
        ch.registerObject("pyHandler", self.handler)
        ch.registerObject("defenderHandler", self.defender_handler)
        p.setWebChannel(ch)
        
        bridge = self.qwc_js + """
        (function(){
            function setup(c){
                var h=c.objects.pyHandler;
                window.MrFeastProjectSTATUS=function(cb){h.getSystemInfo(function(i){cb(JSON.parse(i))})};
                window.XFERVEN_GET_STATS=function(cb){h.getStats(function(i){cb(JSON.parse(i))})};
                window.XFERVEN_GET_HIST=function(cb){h.getHistory(function(i){cb(JSON.parse(i))})};
                window.XFERVEN_GET_PASS=function(cb){h.getPasswords(function(i){cb(JSON.parse(i))})};
                window.XFERVEN_GET_SET=function(cb){h.getSettings(function(i){cb(JSON.parse(i))})};
                window.XFERVEN_NAV=function(u){h.navigateTo(u)};
                window.XFERVEN_CLEAR_HIST=function(){h.clearHistory()};
                window.XFERVEN_CLEAR_PASS=function(){h.clearPasswords()};
                window.XFERVEN_CLEAR_COOK=function(){h.clearCookies()};
                window.XFERVEN_SET=function(k,v){h.setSetting(k,v)};
                window.XFERVEN_DEL_PASS=function(h){h.deletePassword(h)};
                window.XFERVEN_OPEN_EXT=function(u){h.openExternal(u)};
                
                document.addEventListener('submit', function(e){
                    var f=e.target; if(f.tagName!=='FORM')return;
                    var p=f.querySelector('input[type="password"]');
                    var u=f.querySelector('input[type="email"],input[type="text"]');
                    if(p&&u&&p.value&&u.value) h.saveCredentials(location.href, u.value, p.value);
                }, true);
            }
            function init(){if(window.QWebChannel&&window.qt)new QWebChannel(qt.webChannelTransport,setup);else setTimeout(init,100)}
            if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',init);else init();
        })();
        """
        script = QWebEngineScript()
        script.setSourceCode(bridge)
        try: script.setInjectionPoint(QWebEngineScript.InjectionPoint.DocumentCreation)
        except: script.setInjectionTime(QWebEngineScript.InjectionTime.DocumentCreation)
        try: script.setWorldId(QWebEngineScript.ScriptWorldId.MainWorld)
        except: pass
        p.scripts().insert(script)

        p.urlChanged.connect(lambda u, pg=p: self.on_url_changed(pg, u))
        p.loadFinished.connect(lambda ok, pg=p: self.on_load_finished(pg, ok))
        
        idx = self.tabs.insertTab(max(self.tabs.count()-1, 0), v, "New Tab")
        self.tabs.setCurrentIndex(idx)
        v.setUrl(QUrl(url or self.home_url))
        return v

    def close_tab(self, i):
        w = self.tabs.widget(i)
        if w is self.plus_tab: return
        if isinstance(w, QWebEngineView): self.quadro_pages.discard(id(w.page()))
        self.tabs.removeTab(i); w.deleteLater()
        if self.tabs.count() <= 1: self.add_tab()

    def on_tab_changed(self, i):
        if self.tabs.currentWidget() is self.plus_tab: self.add_tab()
        elif self.current_view(): self.sync_url(self.current_view().page().url())

    def sync_url(self, url):
        if self.url_input.hasFocus(): return
        pid = id(self.current_view().page()) if self.current_view() else -1
        if pid in self.quadro_pages: self.url_input.setText(QUADRO_DISPLAY); return
        if pid in self.defender_pages: return
        u = url.toString()
        if u == self.home_url or "interface.html" in u: self.url_input.setText(HOME_DISPLAY)
        else: self.url_input.setText(u)
        self.url_input.setStyleSheet("QLineEdit{background:#1a0b2e;color:#e9d5ff;border:1px solid #7c3aed;border-radius:12px;padding:6px 14px}")

    def on_url_changed(self, page, url):
        pid = id(page)
        if pid in self.defender_pages: return
        if self.defender.enabled and url.scheme() in ("http", "https") and self.defender.is_phishing(url):
            page.stop()
            self.pending_phish_url = url.toString()
            self.defender_pages[pid] = url.toString()
            self.last_defender_page = page
            if self.current_view() and self.current_view().page() is page:
                self.url_input.setText(url.toString())
                self.url_input.setStyleSheet("QLineEdit{background:#180000;color:#f87171;border:2px solid #ef4444}")
            page.setHtml(self.get_defender_html(url.host()), QUrl("about:blank"))
            return
        if self.current_view() and self.current_view().page() is page: self.sync_url(url)

    def on_load_finished(self, page, ok):
        if not ok or id(page) in self.defender_pages: return
        if page.url().scheme() not in ("http", "https"): return
        page.runJavaScript("document.title", lambda t, p=page: self.record_visit(p, t))

    def record_visit(self, page, title):
        if self.settings.get("hist"): self.history.add(page.url().toString(), title or page.url().toString())

    def on_url_entered(self):
        txt = self.url_input.text().strip()
        if not txt: return
        norm = txt.lower().strip().rstrip("/")
        if norm in ("fas://quadroos.sys", "fas://quadro.sys"):
            v = self.current_view()
            if not v: v = self.add_tab()
            self.tabs.tabBar().hide(); self.nav_bar.hide(); self.showFullScreen()
            self.quadro_pages.add(id(v.page()))
            self.url_input.setText(QUADRO_DISPLAY)
            v.setUrl(QUrl(QUADRO_REAL_URL))
            return
        if self.current_view(): self.quadro_pages.discard(id(self.current_view().page()))
        if norm in (HOME_DISPLAY.lower(), "fas://xferven.browse.com/home"): self.handler.goHome(); return
        self.handler.navigateTo(txt)

    def on_download(self, item):
        item.accept()
        try: self.toasts.add(item)
        except Exception as e: print(f"Toast error: {e}")

    def update_shield(self):
        self.shield_btn.setText(f"🛡 {sum(v[0] for v in self.adblock.stats.values())}")

    def get_defender_html(self, domain):
        return f"""<!DOCTYPE html><html><head><style>
        body{{background:#050000;color:#fecaca;font-family:sans-serif;text-align:center;padding:50px}}
        .skull{{font-size:80px;animation:p 1s infinite}} @keyframes p{{50%{{transform:scale(1.1)}}}}
        h1{{color:#ef4444;text-shadow:0 0 20px red}} .box{{background:rgba(20,0,0,.8);border:1px solid #7f1d1d;padding:20px;border-radius:15px;margin:20px auto;max-width:600px}}
        button{{margin:10px;padding:10px 30px;border:none;border-radius:8px;cursor:pointer;font-weight:bold}}
        .exit{{background:#7f1d1d;color:white}} .cont{{background:black;color:red;border:1px solid red}}
        </style></head><body>
        <div class="skull">☠️</div>
        <h1>XFERVEN Defender Blocked This Site</h1>
        <div class="box"><b>{domain}</b> is malicious. Connection terminated to protect your data.</div>
        <button class="exit" onclick="D.exitPage()">Exit</button>
        <button class="cont" onclick="D.requestContinue()">Continue (Risk)</button>
        <script>{self.qwc_js}</script>
        <script>new QWebChannel(qt.webChannelTransport,function(c){{window.D=c.objects.defenderHandler}})</script>
        </body></html>"""

    def exit_defender(self):
        p = self.last_defender_page
        if p:
            self.defender_pages.pop(id(p), None)
            if p.history().canGoBack(): p.back()
            else: p.setUrl(QUrl(self.home_url))
        self.last_defender_page = None
        if self.current_view(): self.sync_url(self.current_view().page().url())

    def continue_defender(self):
        if QMessageBox.warning(self, "Warning", "Are you sure?", QMessageBox.StandardButton.Yes|QMessageBox.StandardButton.No) == QMessageBox.StandardButton.Yes:
            p = self.last_defender_page
            if p:
                self.defender.whitelist.add(QUrl(self.pending_phish_url).host())
                self.defender_pages.pop(id(p), None)
                p.setUrl(QUrl(self.pending_phish_url))
            self.last_defender_page = None
            if self.current_view(): self.sync_url(self.current_view().page().url())

    def open_menu(self, name):
        v = self.add_tab()
        html = self.generate_menu_html(name)
        v.setHtml(html, QUrl("about:blank"))
        self.tabs.setTabText(self.tabs.indexOf(v), name.capitalize())

    def generate_menu_html(self, name):
        css = """body{background:#0b0714;color:#e9d5ff;font-family:sans-serif;padding:20px}
        .card{background:#1a0b2e;border:1px solid #7c3aed;border-radius:15px;padding:20px;margin-bottom:20px}
        h2{color:#c4b5fd} .item{display:flex;justify-content:space-between;padding:10px;border-bottom:1px solid #2d1b4e}
        button{background:#7c3aed;color:white;border:none;padding:8px 16px;border-radius:20px;cursor:pointer}
        button:hover{background:#ec4899} input[type=checkbox]{transform:scale(1.5);margin-right:10px}"""
        
        js_init = f"<script>{self.qwc_js}</script><script>new QWebChannel(qt.webChannelTransport,function(c){{window.H=c.objects.pyHandler}})</script>"
        
        if name == "stats":
            return f"""<html><head><style>{css}</style></head><body>
            <div class="card"><h2>🛡 Protection Stats</h2><div id="stats">Loading...</div></div>
            {js_init}
            <script>
            window.onload=function(){{
                XFERVEN_GET_STATS(function(d){{
                    var html='';
                    for(var k in d){{ html+='<div class="item"><span>'+k+'</span><span>'+d[k][0]+' ('+d[k][1]+')</span></div>'; }}
                    document.getElementById('stats').innerHTML=html||'No blocks yet';
                }});
            }}
            </script></body></html>"""
            
        elif name == "history":
            return f"""<html><head><style>{css}</style></head><body>
            <div class="card"><h2>🕘 History</h2><button onclick="XFERVEN_CLEAR_HIST();location.reload()">Clear</button><div id="hist">Loading...</div></div>
            {js_init}
            <script>
            window.onload=function(){{
                XFERVEN_GET_HIST(function(d){{
                    var html='';
                    d.forEach(function(i){{ html+='<div class="item" onclick="XFERVEN_NAV(\\''+i.u+'\\')" style="cursor:pointer"><span>'+i.t+'</span><span style="color:#888;font-size:12px">'+i.u+'</span></div>'; }});
                    document.getElementById('hist').innerHTML=html||'Empty';
                }});
            }}
            </script></body></html>"""

        elif name == "passwords":
            return f"""<html><head><style>{css}</style></head><body>
            <div class="card"><h2>🔑 Passwords</h2><button onclick="XFERVEN_CLEAR_PASS();location.reload()">Clear All</button><div id="pass">Loading...</div></div>
            {js_init}
            <script>
            window.onload=function(){{
                XFERVEN_GET_PASS(function(d){{
                    var html='';
                    d.forEach(function(i){{ html+='<div class="item"><span>'+i.h+' ('+i.u+')</span><button onclick="XFERVEN_DEL_PASS(\\''+i.h+'\\');this.parentElement.remove()">Delete</button></div>'; }});
                    document.getElementById('pass').innerHTML=html||'Empty';
                }});
            }}
            </script></body></html>"""

        elif name == "settings":
            return f"""<html><head><style>{css}</style></head><body>
            <div class="card"><h2>⚙️ Settings</h2>
            <div class="item"><label><input type="checkbox" id="s_def" onchange="XFERVEN_SET('defender',this.checked)"> Defender (Anti-Phishing)</label></div>
            <div class="item"><label><input type="checkbox" id="s_ad" onchange="XFERVEN_SET('adblock',this.checked)"> AdBlock & Trackers</label></div>
            <div class="item"><label><input type="checkbox" id="s_pass" onchange="XFERVEN_SET('pass',this.checked)"> Save Passwords</label></div>
            <div class="item"><label><input type="checkbox" id="s_hist" onchange="XFERVEN_SET('hist',this.checked)"> Save History</label></div>
            <hr style="border-color:#2d1b4e;margin:20px 0">
            <button onclick="XFERVEN_CLEAR_COOK()">Clear Cookies & Cache</button>
            </div>
            {js_init}
            <script>
            window.onload=function(){{
                XFERVEN_GET_SET(function(d){{
                    document.getElementById('s_def').checked=d.defender;
                    document.getElementById('s_ad').checked=d.adblock;
                    document.getElementById('s_pass').checked=d.pass;
                    document.getElementById('s_hist').checked=d.hist;
                }});
            }}
            </script></body></html>"""
        
        return "<html><body>Unknown Menu</body></html>"

    def eventFilter(self, obj, ev):
        if obj is self.url_input:
            if ev.type() == QEvent.Type.FocusIn:
                cur = self.current_view()
                if cur and id(cur.page()) in self.quadro_pages: self.url_input.setText(QUADRO_DISPLAY); self.url_input.selectAll(); return False
                self.url_input.clear()
            elif ev.type() == QEvent.Type.FocusOut:
                if self.current_view(): self.sync_url(self.current_view().page().url())
        return super().eventFilter(obj, ev)

    def keyPressEvent(self, e):
        if e.key() == Qt.Key.Key_F11:
            if self.isFullScreen(): self.showNormal(); self.tabs.tabBar().show(); self.nav_bar.show()
            else: self.showFullScreen(); self.tabs.tabBar().hide(); self.nav_bar.hide()
        elif e.key() == Qt.Key.Key_Escape and self.isFullScreen():
            cur = self.current_view()
            if cur and id(cur.page()) in self.quadro_pages: return
            self.showNormal(); self.tabs.tabBar().show(); self.nav_bar.show()
        elif e.key() == Qt.Key.Key_F4 and e.modifiers() == Qt.KeyboardModifier.AltModifier: self.close()
        elif e.modifiers() == Qt.KeyboardModifier.ControlModifier and e.key() == Qt.Key.Key_T: self.add_tab()

if __name__ == "__main__":
    os.environ["QTWEBENGINE_CHROMIUM_FLAGS"] = "--webrtc-ip-handling-policy=disable_non_proxies --disable-features=WebGPU,WebXR --dns-over-https-mode=secure --dns-over-https-templates=https://dns.quad9.net/dns-query"
    app = QApplication(sys.argv)
    
    user = load_user_name()
    if not user:
        user, ok = QInputDialog.getText(None, "XFERVEN", "Enter your name:")
        if ok and user: save_user_name(user)
        else: user = "User"
        
    win = SynaptexBrowser(user)
    win.show()
    sys.exit(app.exec())