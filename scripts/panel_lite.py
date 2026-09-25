#!/usr/bin/env python3
"""
Apexsions Panel — Ultra-Lightweight Pterodactyl Desktop Client & Server Manager.
Uses ~25 MB of RAM compared to Chrome's 800+ MB.
Features:
- Console: Real-time live console stream via WebSocket, ANSI colors, reliable command execution,
           historical backlog on connect, live resource meters (CPU, RAM, Disk, Network, Uptime).
- Files: Real Pterodactyl File Manager (Browse, Read, Edit & Save, Upload, Download, Rename, Delete, New Folder/File).
- Network: Server Allocations, IP:Port connection string, Copy button, Primary status, Notes management.
- Startup & Details: Server details (UUID, Node, SFTP, Limits), Java runtime, Startup command, Environment variables.
- SFTP Deploy: One-click build & deploy of Apexsions plugins.
"""

import os
import re
import sys
import json

# Force UTF-8 encoding on Windows console
if sys.platform == "win32":
    try:
        sys.stdout.reconfigure(encoding="utf-8")
        sys.stderr.reconfigure(encoding="utf-8")
    except Exception:
        pass
import time
import queue
import urllib.request
import urllib.parse
import urllib.error
import threading
import ssl
import uuid
from pathlib import Path

# GUI Libraries (Python Built-in)
import tkinter as tk
from tkinter import ttk, messagebox, simpledialog, filedialog

try:
    import websocket
except ImportError:
    print("❌ Error: websocket-client is required. Run 'pip install websocket-client'")
    sys.exit(1)

ROOT_DIR = Path(__file__).resolve().parent.parent
CONFIG_PATH = ROOT_DIR / "panel-config.json"
SFTP_CONFIG_PATH = ROOT_DIR / "sftp-config.json"

# Regex for stripping ANSI escape codes
ANSI_REGEX = re.compile(r'\x1B(?:[@-Z\\-_]|\[[0-?]*[ -/]*[@-~])')

def load_config():
    if not CONFIG_PATH.exists():
        example = {
            "panel_url": "https://stellar.jagoanhosting.id",
            "api_key": "ptlc_YOUR_KEY_HERE",
            "server_id": "27e4a2f6"
        }
        with open(CONFIG_PATH, "w", encoding="utf-8") as f:
            json.dump(example, f, indent=2)
        return example
    try:
        with open(CONFIG_PATH, "r", encoding="utf-8") as f:
            return json.load(f)
    except Exception as e:
        print(f"Error loading {CONFIG_PATH}: {e}")
        return {}

def format_bytes(size_bytes: int or float) -> str:
    if size_bytes is None:
        return "-"
    try:
        size = float(size_bytes)
    except (ValueError, TypeError):
        return "-"
    if size < 1024:
        return f"{int(size)} B"
    elif size < 1024 ** 2:
        return f"{size / 1024:.1f} KB"
    elif size < 1024 ** 3:
        return f"{size / (1024 ** 2):.1f} MB"
    else:
        return f"{size / (1024 ** 3):.2f} GB"

def format_uptime(uptime_ms: int or float) -> str:
    try:
        total_seconds = int(uptime_ms) // 1000
    except (ValueError, TypeError):
        return "--"
    days, rem = divmod(total_seconds, 86400)
    hours, rem = divmod(rem, 3600)
    minutes, seconds = divmod(rem, 60)
    if days > 0:
        return f"{days}d {hours}h {minutes}m"
    elif hours > 0:
        return f"{hours}h {minutes}m {seconds}s"
    else:
        return f"{minutes}m {seconds}s"


class PterodactylAPI:
    """Complete Client API Wrapper for Pterodactyl Panel."""
    def __init__(self, base_url: str, api_key: str, server_id: str):
        self.base_url = base_url.rstrip("/")
        self.api_key = api_key
        self.server_id = server_id
        self.headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Accept": "application/json",
            "Content-Type": "application/json"
        }
        self.ssl_ctx = ssl.create_default_context()
        self.ssl_ctx.check_hostname = False
        self.ssl_ctx.verify_mode = ssl.CERT_NONE

    def _request(self, method: str, endpoint: str, data: dict = None, custom_headers: dict = None, raw_body: bytes = None, expect_json: bool = True):
        url = f"{self.base_url}/api/client{endpoint}"
        headers = dict(self.headers)
        if custom_headers:
            headers.update(custom_headers)

        body = raw_body
        if data is not None and body is None:
            body = json.dumps(data).encode("utf-8")

        req = urllib.request.Request(url, data=body, headers=headers, method=method)
        try:
            with urllib.request.urlopen(req, context=self.ssl_ctx, timeout=20) as resp:
                if resp.status == 204:
                    return {}
                raw = resp.read().decode("utf-8", errors="replace")
                if expect_json:
                    return json.loads(raw) if raw else {}
                return raw
        except urllib.error.HTTPError as e:
            err_msg = e.read().decode("utf-8", errors="replace")
            raise RuntimeError(f"HTTP {e.code}: {err_msg}")
        except Exception as e:
            raise RuntimeError(str(e))

    # --- Core & Power ---
    def get_server_info(self):
        return self._request("GET", f"/servers/{self.server_id}")

    def get_resources(self):
        return self._request("GET", f"/servers/{self.server_id}/resources")

    def send_power_signal(self, signal: str):
        return self._request("POST", f"/servers/{self.server_id}/power", {"signal": signal})

    def send_command(self, command: str):
        return self._request("POST", f"/servers/{self.server_id}/command", {"command": command})

    def get_websocket_credentials(self):
        return self._request("GET", f"/servers/{self.server_id}/websocket")

    # --- File Manager ---
    def list_files(self, directory: str = "/"):
        enc_dir = urllib.parse.quote(directory)
        return self._request("GET", f"/servers/{self.server_id}/files/list?directory={enc_dir}")

    def get_file_contents(self, file_path: str):
        enc_file = urllib.parse.quote(file_path)
        return self._request(
            "GET",
            f"/servers/{self.server_id}/files/contents?file={enc_file}",
            custom_headers={"Accept": "text/plain"},
            expect_json=False
        )

    def write_file_contents(self, file_path: str, content: str):
        enc_file = urllib.parse.quote(file_path)
        raw = content.encode("utf-8")
        return self._request(
            "POST",
            f"/servers/{self.server_id}/files/write?file={enc_file}",
            custom_headers={"Content-Type": "text/plain"},
            raw_body=raw,
            expect_json=False
        )

    def create_folder(self, root: str, name: str):
        return self._request("POST", f"/servers/{self.server_id}/files/create-folder", {"root": root, "name": name})

    def rename_file(self, root: str, old_name: str, new_name: str):
        return self._request("PUT", f"/servers/{self.server_id}/files/rename", {
            "root": root,
            "files": [{"from": old_name, "to": new_name}]
        })

    def delete_files(self, root: str, filenames: list):
        return self._request("POST", f"/servers/{self.server_id}/files/delete", {
            "root": root,
            "files": filenames
        })

    def get_download_url(self, file_path: str):
        enc_file = urllib.parse.quote(file_path)
        res = self._request("GET", f"/servers/{self.server_id}/files/download?file={enc_file}")
        return res.get("attributes", {}).get("url")

    def get_upload_url(self):
        res = self._request("GET", f"/servers/{self.server_id}/files/upload")
        return res.get("attributes", {}).get("url")

    def upload_file(self, directory: str, filename: str, file_bytes: bytes):
        upload_endpoint = self.get_upload_url()
        if not upload_endpoint:
            raise RuntimeError("Could not retrieve signed upload URL from panel.")

        boundary = "----ApexsionsBoundary" + uuid.uuid4().hex
        body = bytearray()
        body.extend(f"--{boundary}\r\n".encode("utf-8"))
        body.extend(f'Content-Disposition: form-data; name="files"; filename="{filename}"\r\n'.encode("utf-8"))
        body.extend(b"Content-Type: application/octet-stream\r\n\r\n")
        body.extend(file_bytes)
        body.extend(f"\r\n--{boundary}--\r\n".encode("utf-8"))

        target_url = f"{upload_endpoint}&directory={urllib.parse.quote(directory)}"
        req = urllib.request.Request(
            target_url,
            data=bytes(body),
            headers={"Content-Type": f"multipart/form-data; boundary={boundary}"},
            method="POST"
        )
        with urllib.request.urlopen(req, context=self.ssl_ctx, timeout=120) as resp:
            return resp.status in (200, 204)

    # --- Network / Allocations ---
    def get_allocations(self):
        return self._request("GET", f"/servers/{self.server_id}/network/allocations")

    def set_primary_allocation(self, allocation_id: int):
        return self._request("POST", f"/servers/{self.server_id}/network/allocations/{allocation_id}/primary")

    def set_allocation_notes(self, allocation_id: int, notes: str):
        return self._request("POST", f"/servers/{self.server_id}/network/allocations/{allocation_id}", {"notes": notes})

    # --- Startup & Details ---
    def get_startup_details(self):
        return self._request("GET", f"/servers/{self.server_id}/startup")


class PanelLiteApp:
    def __init__(self, root: tk.Tk):
        self.root = root
        self.root.title("⚔️ Apexsions Panel — Server Manager")
        self.root.geometry("1100x750")
        self.root.minsize(920, 600)

        # Dark theme palette matching Pterodactyl & Apexsions gold branding
        self.bg_dark = "#111317"
        self.card_bg = "#1a1d24"
        self.card_inner = "#21252e"
        self.border_col = "#2d3342"
        self.text_main = "#f8fafc"
        self.text_dim = "#94a3b8"
        self.accent_gold = "#f59e0b"
        self.accent_gold_hover = "#fbbf24"
        self.green_col = "#10b981"
        self.red_col = "#ef4444"
        self.blue_col = "#3b82f6"
        self.console_bg = "#0c0e12"

        self.root.configure(bg=self.bg_dark)

        # Load configuration
        self.cfg = load_config()
        self.panel_url = self.cfg.get("panel_url", "https://stellar.jagoanhosting.id")
        self.api_key = self.cfg.get("api_key", "")
        self.server_id = self.cfg.get("server_id", "27e4a2f6")

        self.api = PterodactylAPI(self.panel_url, self.api_key, self.server_id)

        # WebSocket & State
        self.ws = None
        self.ws_thread = None
        self.ws_lock = threading.Lock()
        self.ws_authenticated = False
        self.is_running = True
        self.log_queue = queue.Queue()
        self.ui_queue = queue.Queue()
        self.cmd_history = []
        self.history_idx = 0

        # File Manager state
        self.current_dir = "/"
        self.files_cache = []

        # Setup ttk styles for Treeview & Combobox
        self.setup_ttk_styles()

        # Build UI layout
        self.build_ui()

        # Start background workers
        self.start_websocket()
        self.start_stats_polling()
        self.process_queue()

    def safe_after(self, fn):
        """Safely schedule a callback on the Tkinter main thread via queue."""
        if self.is_running:
            self.ui_queue.put(fn)

    def setup_ttk_styles(self):
        style = ttk.Style()
        try:
            style.theme_use("clam")
        except Exception:
            pass

        style.configure(
            "Treeview",
            background=self.card_bg,
            foreground=self.text_main,
            fieldbackground=self.card_bg,
            bordercolor=self.border_col,
            rowheight=28,
            font=("Segoe UI", 9)
        )
        style.configure(
            "Treeview.Heading",
            background=self.card_inner,
            foreground=self.accent_gold,
            relief="flat",
            font=("Segoe UI", 9, "bold")
        )
        style.map(
            "Treeview",
            background=[("selected", "#2563eb")],
            foreground=[("selected", "#ffffff")]
        )
        style.map(
            "Treeview.Heading",
            background=[("active", self.border_col)]
        )

    def build_ui(self):
        # Top Header Bar
        header = tk.Frame(self.root, bg=self.card_bg, height=62, bd=0, highlightbackground=self.border_col, highlightthickness=1)
        header.pack(fill="x", side="top", padx=12, pady=(10, 4))

        # Brand / Server Name & Address
        brand_frame = tk.Frame(header, bg=self.card_bg)
        brand_frame.pack(side="left", padx=15, pady=8)

        lbl_title = tk.Label(brand_frame, text="APEXSIONS", font=("Segoe UI", 13, "bold"), fg=self.accent_gold, bg=self.card_bg)
        lbl_title.pack(side="left")

        lbl_host = tk.Label(brand_frame, text="apexsions.my.id:32348", font=("Segoe UI", 9), fg=self.text_dim, bg=self.card_bg)
        lbl_host.pack(side="left", padx=(8, 12))

        self.lbl_status = tk.Label(brand_frame, text=" ● CONNECTING... ", font=("Segoe UI", 9, "bold"), fg="#ffffff", bg="#64748b", padx=8, pady=2)
        self.lbl_status.pack(side="left")

        # Stats Cards on Header Right
        stats_box = tk.Frame(header, bg=self.card_bg)
        stats_box.pack(side="right", padx=15, pady=6)

        def make_stat_item(parent, title):
            f = tk.Frame(parent, bg=self.card_bg)
            f.pack(side="left", padx=10)
            lbl_t = tk.Label(f, text=title, font=("Segoe UI", 7, "bold"), fg=self.text_dim, bg=self.card_bg)
            lbl_t.pack(anchor="w")
            lbl_v = tk.Label(f, text="--", font=("Segoe UI", 9, "bold"), fg=self.text_main, bg=self.card_bg)
            lbl_v.pack(anchor="w")
            return lbl_v

        self.lbl_cpu = make_stat_item(stats_box, "CPU USAGE")
        self.lbl_ram = make_stat_item(stats_box, "MEMORY")
        self.lbl_disk = make_stat_item(stats_box, "DISK")
        self.lbl_net = make_stat_item(stats_box, "NETWORK (RX / TX)")
        self.lbl_uptime = make_stat_item(stats_box, "UPTIME")

        # Modern Navigation Tab Bar
        tab_bar = tk.Frame(self.root, bg=self.bg_dark, height=40)
        tab_bar.pack(fill="x", side="top", padx=12, pady=(4, 6))

        self.tab_buttons = {}
        tabs = [
            ("console", "💻 Console"),
            ("files", "📁 File Manager"),
            ("network", "🌐 Network"),
            ("startup", "⚙️ Startup & Details"),
            ("deploy", "🚀 SFTP Deploy")
        ]

        for tab_id, tab_label in tabs:
            btn = tk.Button(
                tab_bar,
                text=tab_label,
                font=("Segoe UI", 10, "bold"),
                bg=self.card_bg,
                fg=self.text_dim,
                activebackground=self.card_inner,
                activeforeground="#ffffff",
                bd=0,
                relief="flat",
                padx=16,
                pady=6,
                cursor="hand2",
                command=lambda tid=tab_id: self.switch_tab(tid)
            )
            btn.pack(side="left", padx=(0, 6))
            self.tab_buttons[tab_id] = btn

        # Container for Tab Pages
        self.tab_container = tk.Frame(self.root, bg=self.bg_dark)
        self.tab_container.pack(fill="both", expand=True, padx=12, pady=(0, 10))

        # Build each tab frame
        self.frames = {}
        self.frames["console"] = self.build_console_tab()
        self.frames["files"] = self.build_files_tab()
        self.frames["network"] = self.build_network_tab()
        self.frames["startup"] = self.build_startup_tab()
        self.frames["deploy"] = self.build_deploy_tab()

        # Default active tab
        self.current_tab = None
        self.switch_tab("console")

    def switch_tab(self, tab_id: str):
        if self.current_tab == tab_id:
            return
        self.current_tab = tab_id

        # Update button visual states
        for tid, btn in self.tab_buttons.items():
            if tid == tab_id:
                btn.config(bg=self.accent_gold, fg="#111317")
            else:
                btn.config(bg=self.card_bg, fg=self.text_dim)

        # Hide all frames and show active frame
        for tid, frame in self.frames.items():
            if tid == tab_id:
                frame.pack(fill="both", expand=True)
            else:
                frame.pack_forget()

        # Trigger lazy load for non-console tabs
        if tab_id == "files" and not self.files_cache:
            self.refresh_file_list()
        elif tab_id == "network":
            self.refresh_network_list()
        elif tab_id == "startup":
            self.refresh_startup_details()

    # =========================================================================
    # TAB 1: CONSOLE
    # =========================================================================
    def build_console_tab(self):
        tab = tk.Frame(self.tab_container, bg=self.bg_dark)

        # Action / Control Toolbar
        toolbar = tk.Frame(tab, bg=self.bg_dark)
        toolbar.pack(fill="x", side="top", pady=(0, 6))

        btn_style = {"font": ("Segoe UI", 9, "bold"), "bd": 0, "relief": "flat", "padx": 12, "pady": 5, "cursor": "hand2"}

        self.btn_start = tk.Button(toolbar, text="▶ Start", bg=self.green_col, fg="#ffffff", activebackground="#059669", activeforeground="#ffffff", command=lambda: self.trigger_power("start"), **btn_style)
        self.btn_start.pack(side="left", padx=(0, 6))

        self.btn_restart = tk.Button(toolbar, text="🔄 Restart", bg=self.blue_col, fg="#ffffff", activebackground="#2563eb", activeforeground="#ffffff", command=lambda: self.trigger_power("restart"), **btn_style)
        self.btn_restart.pack(side="left", padx=6)

        self.btn_stop = tk.Button(toolbar, text="⏹ Stop", bg="#d97706", fg="#ffffff", activebackground="#b45309", activeforeground="#ffffff", command=lambda: self.trigger_power("stop"), **btn_style)
        self.btn_stop.pack(side="left", padx=6)

        self.btn_kill = tk.Button(toolbar, text="⚡ Kill", bg=self.red_col, fg="#ffffff", activebackground="#dc2626", activeforeground="#ffffff", command=lambda: self.trigger_power("kill"), **btn_style)
        self.btn_kill.pack(side="left", padx=6)

        sep = tk.Frame(toolbar, width=2, height=24, bg=self.border_col)
        sep.pack(side="left", padx=10)

        # Quick Commands buttons
        quick_cmds = ["list", "tps", "version", "apx reload", "save-all"]
        for qc in quick_cmds:
            b = tk.Button(
                toolbar,
                text=qc,
                font=("Consolas", 8, "bold"),
                bg=self.card_bg,
                fg=self.accent_gold,
                activebackground=self.border_col,
                activeforeground="#ffffff",
                bd=0,
                relief="flat",
                padx=8,
                pady=4,
                cursor="hand2",
                command=lambda cmd=qc: self.send_command(cmd)
            )
            b.pack(side="left", padx=3)

        # Right tools on toolbar
        self.btn_clear = tk.Button(toolbar, text="🧹 Clear Logs", bg=self.card_bg, fg=self.text_dim, activebackground=self.border_col, activeforeground="#ffffff", command=self.clear_logs, **btn_style)
        self.btn_clear.pack(side="right", padx=(6, 0))

        self.auto_scroll_var = tk.BooleanVar(value=True)
        chk_scroll = tk.Checkbutton(toolbar, text="Auto-scroll", variable=self.auto_scroll_var, bg=self.bg_dark, fg=self.text_dim, selectcolor=self.card_bg, activebackground=self.bg_dark, activeforeground=self.text_main, font=("Segoe UI", 9))
        chk_scroll.pack(side="right", padx=10)

        # Real-Time Console Window
        console_frame = tk.Frame(tab, bg=self.console_bg, highlightbackground=self.border_col, highlightthickness=1)
        console_frame.pack(fill="both", expand=True, pady=4)

        self.console = tk.Text(
            console_frame,
            bg=self.console_bg,
            fg=self.text_main,
            insertbackground="#ffffff",
            font=("Consolas", 10),
            wrap="char",
            bd=0,
            padx=12,
            pady=10,
            relief="flat"
        )
        self.console_scrollbar = ttk.Scrollbar(console_frame, orient="vertical", command=self.console.yview)
        self.console.configure(yscrollcommand=self.console_scrollbar.set)

        self.console_scrollbar.pack(side="right", fill="y")
        self.console.pack(side="left", fill="both", expand=True)

        # Configure color tags for log levels
        self.console.tag_config("info", foreground="#94a3b8")
        self.console.tag_config("warn", foreground="#f59e0b")
        self.console.tag_config("error", foreground="#f87171")
        self.console.tag_config("success", foreground="#34d399")
        self.console.tag_config("accent", foreground="#38bdf8")

        # Bottom Command Input Bar
        cmd_bar = tk.Frame(tab, bg=self.card_bg, height=45, highlightbackground=self.border_col, highlightthickness=1)
        cmd_bar.pack(fill="x", side="bottom", pady=(6, 0))

        lbl_prompt = tk.Label(cmd_bar, text="❯", font=("Consolas", 12, "bold"), fg=self.accent_gold, bg=self.card_bg)
        lbl_prompt.pack(side="left", padx=(12, 6))

        self.cmd_entry = tk.Entry(cmd_bar, font=("Consolas", 11), bg=self.card_bg, fg="#ffffff", insertbackground="#ffffff", bd=0, relief="flat")
        self.cmd_entry.pack(side="left", fill="x", expand=True, padx=6, pady=8)
        self.cmd_entry.bind("<Return>", lambda e: self.send_command())
        self.cmd_entry.bind("<Up>", self.history_up)
        self.cmd_entry.bind("<Down>", self.history_down)

        btn_send = tk.Button(cmd_bar, text="Send ↵", bg=self.accent_gold, fg="#111317", activebackground=self.accent_gold_hover, font=("Segoe UI", 9, "bold"), bd=0, relief="flat", padx=16, pady=4, cursor="hand2", command=self.send_command)
        btn_send.pack(side="right", padx=8, pady=6)

        return tab

    def append_log(self, text: str):
        clean_text = ANSI_REGEX.sub('', text)
        tag = None
        lower = clean_text.lower()
        if "warn" in lower:
            tag = "warn"
        elif "err" in lower or "fatal" in lower or "severe" in lower or "exception" in lower:
            tag = "error"
        elif "success" in lower or "connected" in lower or "enabled" in lower or "done" in lower:
            tag = "success"
        elif "info" in lower:
            tag = "info"

        if tag:
            self.console.insert("end", clean_text + "\n", tag)
        else:
            self.console.insert("end", clean_text + "\n")

        if self.auto_scroll_var.get():
            self.console.see("end")

    def clear_logs(self):
        self.console.delete("1.0", "end")

    def update_status_badge(self, state: str):
        state_norm = state.lower()
        if state_norm == "running":
            self.lbl_status.config(text=" ● RUNNING ", bg="#059669")
        elif state_norm == "offline":
            self.lbl_status.config(text=" ● OFFLINE ", bg="#dc2626")
        elif state_norm == "starting":
            self.lbl_status.config(text=" ● STARTING ", bg="#d97706")
        elif state_norm == "stopping":
            self.lbl_status.config(text=" ● STOPPING ", bg="#d97706")
        else:
            self.lbl_status.config(text=f" ● {state.upper()} ", bg="#64748b")

    def send_command(self, custom_cmd: str = None):
        cmd = (custom_cmd or self.cmd_entry.get()).strip()
        if not cmd:
            return
        if not custom_cmd:
            self.cmd_entry.delete(0, "end")
            self.cmd_history.append(cmd)
            self.history_idx = len(self.cmd_history)

        self.append_log(f"\n❯ {cmd}")

        def _worker():
            sent = False
            # 1. Try sending via WebSocket if authenticated
            if self.ws_authenticated:
                try:
                    sent = self.ws_send("send command", [cmd])
                except Exception:
                    sent = False

            # 2. Fallback to REST API if WebSocket failed or unauthenticated
            if not sent:
                try:
                    self.api.send_command(cmd)
                except Exception as e:
                    self.log_queue.put(f"❌ [ERROR] Failed to execute command: {e}")

        threading.Thread(target=_worker, daemon=True).start()

    def history_up(self, event):
        if self.cmd_history and self.history_idx > 0:
            self.history_idx -= 1
            self.cmd_entry.delete(0, "end")
            self.cmd_entry.insert(0, self.cmd_history[self.history_idx])
        return "break"

    def history_down(self, event):
        if self.cmd_history and self.history_idx < len(self.cmd_history) - 1:
            self.history_idx += 1
            self.cmd_entry.delete(0, "end")
            self.cmd_entry.insert(0, self.cmd_history[self.history_idx])
        else:
            self.history_idx = len(self.cmd_history)
            self.cmd_entry.delete(0, "end")
        return "break"

    def trigger_power(self, signal: str):
        confirm = True
        if signal in ("kill", "stop"):
            confirm = messagebox.askyesno("Confirm Power Action", f"Are you sure you want to {signal.upper()} the server?")
        if not confirm:
            return

        def _worker():
            try:
                self.api.send_power_signal(signal)
                self.log_queue.put(f"⚡ [POWER] Signal '{signal}' sent successfully.")
            except Exception as e:
                self.log_queue.put(f"❌ [POWER ERROR] Signal '{signal}' failed: {e}")

        threading.Thread(target=_worker, daemon=True).start()

    # =========================================================================
    # TAB 2: FILE MANAGER (REAL PTERODACTYL FILE SYSTEM)
    # =========================================================================
    def build_files_tab(self):
        tab = tk.Frame(self.tab_container, bg=self.bg_dark)

        # Top Toolbar for File Operations
        toolbar = tk.Frame(tab, bg=self.card_bg, highlightbackground=self.border_col, highlightthickness=1)
        toolbar.pack(fill="x", side="top", pady=(0, 6), ipady=4)

        # Path display & Up button
        path_box = tk.Frame(toolbar, bg=self.card_bg)
        path_box.pack(side="left", padx=10)

        btn_up = tk.Button(path_box, text="⬆ Up", font=("Segoe UI", 9, "bold"), bg=self.card_inner, fg=self.text_main, activebackground=self.border_col, activeforeground="#ffffff", bd=0, relief="flat", padx=10, pady=4, cursor="hand2", command=self.file_up_dir)
        btn_up.pack(side="left", padx=(0, 8))

        lbl_dir_label = tk.Label(path_box, text="Location:", font=("Segoe UI", 9, "bold"), fg=self.accent_gold, bg=self.card_bg)
        lbl_dir_label.pack(side="left", padx=(0, 6))

        self.lbl_current_path = tk.Label(path_box, text="/", font=("Consolas", 10), fg="#38bdf8", bg=self.card_bg)
        self.lbl_current_path.pack(side="left")

        # Action Buttons on Right
        btn_box = tk.Frame(toolbar, bg=self.card_bg)
        btn_box.pack(side="right", padx=10)

        btn_style = {"font": ("Segoe UI", 9, "bold"), "bd": 0, "relief": "flat", "padx": 10, "pady": 4, "cursor": "hand2"}

        btn_refresh = tk.Button(btn_box, text="🔄 Refresh", bg=self.card_inner, fg=self.text_main, activebackground=self.border_col, activeforeground="#ffffff", command=self.refresh_file_list, **btn_style)
        btn_refresh.pack(side="left", padx=4)

        btn_new_file = tk.Button(btn_box, text="➕ New File", bg=self.card_inner, fg=self.text_main, activebackground=self.border_col, activeforeground="#ffffff", command=self.file_create_file, **btn_style)
        btn_new_file.pack(side="left", padx=4)

        btn_new_folder = tk.Button(btn_box, text="📁 New Folder", bg=self.card_inner, fg=self.text_main, activebackground=self.border_col, activeforeground="#ffffff", command=self.file_create_folder, **btn_style)
        btn_new_folder.pack(side="left", padx=4)

        btn_upload = tk.Button(btn_box, text="📤 Upload", bg=self.accent_gold, fg="#111317", activebackground=self.accent_gold_hover, command=self.file_upload, **btn_style)
        btn_upload.pack(side="left", padx=4)

        # File List View (Treeview)
        tree_frame = tk.Frame(tab, bg=self.card_bg, highlightbackground=self.border_col, highlightthickness=1)
        tree_frame.pack(fill="both", expand=True)

        columns = ("name", "size", "modified")
        self.file_tree = ttk.Treeview(tree_frame, columns=columns, show="headings", selectmode="browse")

        self.file_tree.heading("name", text="Name", anchor="w")
        self.file_tree.heading("size", text="Size", anchor="e")
        self.file_tree.heading("modified", text="Last Modified", anchor="w")

        self.file_tree.column("name", width=550, anchor="w")
        self.file_tree.column("size", width=120, anchor="e")
        self.file_tree.column("modified", width=220, anchor="w")

        scroll_y = ttk.Scrollbar(tree_frame, orient="vertical", command=self.file_tree.yview)
        self.file_tree.configure(yscrollcommand=scroll_y.set)

        scroll_y.pack(side="right", fill="y")
        self.file_tree.pack(side="left", fill="both", expand=True)

        self.file_tree.bind("<Double-1>", self.on_file_double_click)

        # Bottom Action Bar for Selected File
        bottom_bar = tk.Frame(tab, bg=self.card_bg, highlightbackground=self.border_col, highlightthickness=1)
        bottom_bar.pack(fill="x", side="bottom", pady=(6, 0), ipady=3)

        self.lbl_file_selection = tk.Label(bottom_bar, text="Double-click a folder to open, or double-click a file to edit.", font=("Segoe UI", 9), fg=self.text_dim, bg=self.card_bg)
        self.lbl_file_selection.pack(side="left", padx=12)

        action_box = tk.Frame(bottom_bar, bg=self.card_bg)
        action_box.pack(side="right", padx=10)

        btn_edit = tk.Button(action_box, text="✏️ Edit File", bg=self.blue_col, fg="#ffffff", activebackground="#2563eb", activeforeground="#ffffff", command=self.file_open_editor, **btn_style)
        btn_edit.pack(side="left", padx=4)

        btn_download = tk.Button(action_box, text="📥 Download", bg=self.card_inner, fg=self.text_main, activebackground=self.border_col, activeforeground="#ffffff", command=self.file_download, **btn_style)
        btn_download.pack(side="left", padx=4)

        btn_rename = tk.Button(action_box, text="🏷️ Rename", bg=self.card_inner, fg=self.text_main, activebackground=self.border_col, activeforeground="#ffffff", command=self.file_rename, **btn_style)
        btn_rename.pack(side="left", padx=4)

        btn_delete = tk.Button(action_box, text="🗑️ Delete", bg=self.red_col, fg="#ffffff", activebackground="#dc2626", activeforeground="#ffffff", command=self.file_delete, **btn_style)
        btn_delete.pack(side="left", padx=4)

        return tab

    def refresh_file_list(self):
        self.lbl_current_path.config(text=self.current_dir)
        for row in self.file_tree.get_children():
            self.file_tree.delete(row)

        self.lbl_file_selection.config(text="Loading files from server...")

        def _worker():
            try:
                res = self.api.list_files(self.current_dir)
                data = res.get("data", [])
                self.safe_after(lambda d=data: self._populate_files(d))
            except Exception as e:
                self.safe_after(lambda err=e: self.lbl_file_selection.config(text=f"Error loading files: {err}"))

        threading.Thread(target=_worker, daemon=True).start()

    def _populate_files(self, items: list):
        self.files_cache = items
        for row in self.file_tree.get_children():
            self.file_tree.delete(row)

        # Sort: directories first, then alphabetical
        folders = []
        files = []
        for item in items:
            attr = item.get("attributes", {})
            if attr.get("is_file"):
                files.append(attr)
            else:
                folders.append(attr)

        folders.sort(key=lambda x: x.get("name", "").lower())
        files.sort(key=lambda x: x.get("name", "").lower())

        for f in folders:
            name = "📁  " + f.get("name", "")
            mod = f.get("modified_at", "").replace("T", " ")[:19]
            self.file_tree.insert("", "end", values=(name, "-", mod), tags=("folder",))

        for f in files:
            raw_name = f.get("name", "")
            icon = "📄  "
            if raw_name.endswith((".yml", ".yaml", ".json", ".properties", ".toml", ".txt")):
                icon = "⚙️  "
            elif raw_name.endswith(".jar"):
                icon = "☕  "
            elif raw_name.endswith((".png", ".jpg", ".ico")):
                icon = "🖼️  "
            elif raw_name.endswith((".log", ".gz")):
                icon = "📜  "

            name = icon + raw_name
            size_str = format_bytes(f.get("size", 0))
            mod = f.get("modified_at", "").replace("T", " ")[:19]
            self.file_tree.insert("", "end", values=(name, size_str, mod), tags=("file",))

        self.lbl_file_selection.config(text=f"Showing {len(folders)} folders, {len(files)} files in {self.current_dir}")

    def on_file_double_click(self, event):
        sel = self.file_tree.selection()
        if not sel:
            return
        item_vals = self.file_tree.item(sel[0], "values")
        if not item_vals:
            return
        name_with_icon = item_vals[0]
        # Clean off icon
        name = name_with_icon.split("  ", 1)[-1]

        # Check if folder
        is_folder = False
        for it in self.files_cache:
            attr = it.get("attributes", {})
            if attr.get("name") == name:
                is_folder = not attr.get("is_file")
                break

        if is_folder:
            # Enter directory
            if self.current_dir == "/":
                self.current_dir = f"/{name}"
            else:
                self.current_dir = f"{self.current_dir.rstrip('/')}/{name}"
            self.refresh_file_list()
        else:
            # Open file in editor
            self.file_open_editor(name)

    def file_up_dir(self):
        if self.current_dir == "/" or not self.current_dir:
            return
        parent = str(Path(self.current_dir).parent).replace("\\", "/")
        if not parent.startswith("/"):
            parent = "/" + parent
        self.current_dir = parent if parent else "/"
        self.refresh_file_list()

    def get_selected_filename(self):
        sel = self.file_tree.selection()
        if not sel:
            return None
        item_vals = self.file_tree.item(sel[0], "values")
        if not item_vals:
            return None
        return item_vals[0].split("  ", 1)[-1]

    def file_open_editor(self, specified_filename: str = None):
        filename = specified_filename or self.get_selected_filename()
        if not filename:
            messagebox.showinfo("Select File", "Please select a file to edit.")
            return

        full_path = f"{self.current_dir.rstrip('/')}/{filename}"
        self.open_code_editor_window(full_path, filename)

    def open_code_editor_window(self, file_path: str, filename: str, is_new: bool = False):
        editor = tk.Toplevel(self.root)
        editor.title(f"Editing: {filename} — Apexsions File Editor")
        editor.geometry("900x650")
        editor.minsize(700, 480)
        editor.configure(bg=self.bg_dark)
        editor.transient(self.root)

        # Editor Header
        ed_header = tk.Frame(editor, bg=self.card_bg, height=48, highlightbackground=self.border_col, highlightthickness=1)
        ed_header.pack(fill="x", side="top", padx=10, pady=8)

        lbl_f = tk.Label(ed_header, text=f"File: {file_path}", font=("Consolas", 10, "bold"), fg=self.accent_gold, bg=self.card_bg)
        lbl_f.pack(side="left", padx=15, pady=8)

        lbl_status = tk.Label(ed_header, text="Loading...", font=("Segoe UI", 9), fg=self.text_dim, bg=self.card_bg)
        lbl_status.pack(side="left", padx=10)

        # Main text box
        text_frame = tk.Frame(editor, bg=self.console_bg, highlightbackground=self.border_col, highlightthickness=1)
        text_frame.pack(fill="both", expand=True, padx=10, pady=4)

        text_editor = tk.Text(
            text_frame,
            bg=self.console_bg,
            fg=self.text_main,
            insertbackground="#ffffff",
            font=("Consolas", 11),
            wrap="none",
            bd=0,
            padx=12,
            pady=10,
            relief="flat",
            undo=True
        )
        scroll_y = ttk.Scrollbar(text_frame, orient="vertical", command=text_editor.yview)
        scroll_x = ttk.Scrollbar(text_frame, orient="horizontal", command=text_editor.xview)
        text_editor.configure(yscrollcommand=scroll_y.set, xscrollcommand=scroll_x.set)

        scroll_y.pack(side="right", fill="y")
        scroll_x.pack(side="bottom", fill="x")
        text_editor.pack(side="left", fill="both", expand=True)

        # Editor Footer & Buttons
        footer = tk.Frame(editor, bg=self.card_bg, height=45, highlightbackground=self.border_col, highlightthickness=1)
        footer.pack(fill="x", side="bottom", padx=10, pady=8)

        def _save():
            content = text_editor.get("1.0", "end-1c")
            lbl_status.config(text="Saving...", fg=self.accent_gold)

            def _save_worker():
                try:
                    self.api.write_file_contents(file_path, content)
                    self.safe_after(lambda: lbl_status.config(text="✅ Saved successfully!", fg=self.green_col))
                    self.refresh_file_list()
                except Exception as e:
                    self.safe_after(lambda err=e: lbl_status.config(text=f"❌ Save failed: {err}", fg=self.red_col))

            threading.Thread(target=_save_worker, daemon=True).start()

        def _reload():
            if is_new:
                return
            lbl_status.config(text="Reloading...", fg=self.accent_gold)

            def _reload_worker():
                try:
                    content = self.api.get_file_contents(file_path)
                    def _update_ui():
                        text_editor.delete("1.0", "end")
                        text_editor.insert("1.0", content)
                        lbl_status.config(text="Ready", fg=self.text_dim)
                    self.safe_after(_update_ui)
                except Exception as e:
                    self.safe_after(lambda err=e: lbl_status.config(text=f"❌ Load failed: {err}", fg=self.red_col))

            threading.Thread(target=_reload_worker, daemon=True).start()

        btn_save = tk.Button(footer, text="💾 Save File", font=("Segoe UI", 9, "bold"), bg=self.green_col, fg="#ffffff", activebackground="#059669", activeforeground="#ffffff", bd=0, relief="flat", padx=16, pady=6, cursor="hand2", command=_save)
        btn_save.pack(side="right", padx=10, pady=6)

        btn_rel = tk.Button(footer, text="🔄 Reload", font=("Segoe UI", 9, "bold"), bg=self.card_inner, fg=self.text_main, activebackground=self.border_col, activeforeground="#ffffff", bd=0, relief="flat", padx=14, pady=6, cursor="hand2", command=_reload)
        btn_rel.pack(side="right", padx=6, pady=6)

        btn_close = tk.Button(footer, text="Close", font=("Segoe UI", 9), bg=self.card_inner, fg=self.text_dim, activebackground=self.border_col, activeforeground="#ffffff", bd=0, relief="flat", padx=14, pady=6, cursor="hand2", command=editor.destroy)
        btn_close.pack(side="left", padx=10, pady=6)

        # Initial load
        if not is_new:
            _reload()
        else:
            lbl_status.config(text="New File", fg=self.text_dim)

    def file_create_file(self):
        filename = simpledialog.askstring("New File", "Enter new filename (e.g. config.yml):", parent=self.root)
        if not filename or not filename.strip():
            return
        filename = filename.strip()
        full_path = f"{self.current_dir.rstrip('/')}/{filename}"
        self.open_code_editor_window(full_path, filename, is_new=True)

    def file_create_folder(self):
        folder_name = simpledialog.askstring("New Folder", "Enter folder name:", parent=self.root)
        if not folder_name or not folder_name.strip():
            return
        folder_name = folder_name.strip()

        def _worker():
            try:
                self.api.create_folder(self.current_dir, folder_name)
                self.safe_after(self.refresh_file_list)
            except Exception as e:
                self.safe_after(lambda err=e: messagebox.showerror("Create Folder Error", str(err)))

        threading.Thread(target=_worker, daemon=True).start()

    def file_rename(self):
        filename = self.get_selected_filename()
        if not filename:
            messagebox.showinfo("Select File", "Please select a file or folder to rename.")
            return

        new_name = simpledialog.askstring("Rename", f"Enter new name for '{filename}':", initialvalue=filename, parent=self.root)
        if not new_name or not new_name.strip() or new_name == filename:
            return
        new_name = new_name.strip()

        def _worker():
            try:
                self.api.rename_file(self.current_dir, filename, new_name)
                self.safe_after(self.refresh_file_list)
            except Exception as e:
                self.safe_after(lambda err=e: messagebox.showerror("Rename Error", str(err)))

        threading.Thread(target=_worker, daemon=True).start()

    def file_delete(self):
        filename = self.get_selected_filename()
        if not filename:
            messagebox.showinfo("Select File", "Please select a file or folder to delete.")
            return

        confirm = messagebox.askyesno("Confirm Delete", f"Are you sure you want to PERMANENTLY delete '{filename}'?")
        if not confirm:
            return

        def _worker():
            try:
                self.api.delete_files(self.current_dir, [filename])
                self.safe_after(self.refresh_file_list)
            except Exception as e:
                self.safe_after(lambda err=e: messagebox.showerror("Delete Error", str(err)))

        threading.Thread(target=_worker, daemon=True).start()

    def file_download(self):
        filename = self.get_selected_filename()
        if not filename:
            messagebox.showinfo("Select File", "Please select a file to download.")
            return

        save_path = filedialog.asksaveasfilename(initialfile=filename, parent=self.root)
        if not save_path:
            return

        full_remote_path = f"{self.current_dir.rstrip('/')}/{filename}"
        self.lbl_file_selection.config(text=f"Downloading {filename}...")

        def _worker():
            try:
                d_url = self.api.get_download_url(full_remote_path)
                req = urllib.request.Request(d_url)
                with urllib.request.urlopen(req, context=self.api.ssl_ctx, timeout=60) as resp:
                    with open(save_path, "wb") as f_out:
                        f_out.write(resp.read())
                self.safe_after(lambda: messagebox.showinfo("Download Complete", f"Downloaded to {save_path}"))
                self.safe_after(lambda: self.lbl_file_selection.config(text="Download complete."))
            except Exception as e:
                self.safe_after(lambda err=e: messagebox.showerror("Download Error", str(err)))

        threading.Thread(target=_worker, daemon=True).start()

    def file_upload(self):
        src_path = filedialog.askopenfilename(parent=self.root)
        if not src_path:
            return
        filename = Path(src_path).name
        self.lbl_file_selection.config(text=f"Uploading {filename} to {self.current_dir}...")

        def _worker():
            try:
                with open(src_path, "rb") as f_in:
                    data = f_in.read()
                self.api.upload_file(self.current_dir, filename, data)
                self.safe_after(lambda: messagebox.showinfo("Upload Complete", f"Successfully uploaded {filename}!"))
                self.safe_after(self.refresh_file_list)
            except Exception as e:
                self.safe_after(lambda err=e: messagebox.showerror("Upload Error", str(err)))

        threading.Thread(target=_worker, daemon=True).start()

    # =========================================================================
    # TAB 3: NETWORK / ALLOCATIONS
    # =========================================================================
    def build_network_tab(self):
        tab = tk.Frame(self.tab_container, bg=self.bg_dark)

        # Header bar
        toolbar = tk.Frame(tab, bg=self.card_bg, highlightbackground=self.border_col, highlightthickness=1)
        toolbar.pack(fill="x", side="top", pady=(0, 6), ipady=4)

        lbl = tk.Label(toolbar, text="Server Allocations & Connection Ports", font=("Segoe UI", 11, "bold"), fg=self.accent_gold, bg=self.card_bg)
        lbl.pack(side="left", padx=15)

        btn_refresh = tk.Button(toolbar, text="🔄 Refresh", font=("Segoe UI", 9, "bold"), bg=self.card_inner, fg=self.text_main, activebackground=self.border_col, activeforeground="#ffffff", bd=0, relief="flat", padx=12, pady=4, cursor="hand2", command=self.refresh_network_list)
        btn_refresh.pack(side="right", padx=10)

        # Allocations Table
        tree_frame = tk.Frame(tab, bg=self.card_bg, highlightbackground=self.border_col, highlightthickness=1)
        tree_frame.pack(fill="both", expand=True)

        columns = ("id", "ip", "port", "connect", "primary", "notes")
        self.net_tree = ttk.Treeview(tree_frame, columns=columns, show="headings", selectmode="browse")

        self.net_tree.heading("id", text="ID")
        self.net_tree.heading("ip", text="IP Address")
        self.net_tree.heading("port", text="Port")
        self.net_tree.heading("connect", text="Connection String")
        self.net_tree.heading("primary", text="Type")
        self.net_tree.heading("notes", text="Notes")

        self.net_tree.column("id", width=80, anchor="center")
        self.net_tree.column("ip", width=160, anchor="w")
        self.net_tree.column("port", width=100, anchor="center")
        self.net_tree.column("connect", width=220, anchor="w")
        self.net_tree.column("primary", width=140, anchor="center")
        self.net_tree.column("notes", width=300, anchor="w")

        scroll_y = ttk.Scrollbar(tree_frame, orient="vertical", command=self.net_tree.yview)
        self.net_tree.configure(yscrollcommand=scroll_y.set)

        scroll_y.pack(side="right", fill="y")
        self.net_tree.pack(side="left", fill="both", expand=True)

        # Action Buttons
        bot_bar = tk.Frame(tab, bg=self.card_bg, highlightbackground=self.border_col, highlightthickness=1)
        bot_bar.pack(fill="x", side="bottom", pady=(6, 0), ipady=4)

        btn_style = {"font": ("Segoe UI", 9, "bold"), "bd": 0, "relief": "flat", "padx": 14, "pady": 5, "cursor": "hand2"}

        btn_copy = tk.Button(bot_bar, text="📋 Copy Connection Address", bg=self.accent_gold, fg="#111317", activebackground=self.accent_gold_hover, command=self.net_copy_address, **btn_style)
        btn_copy.pack(side="left", padx=10)

        btn_set_primary = tk.Button(bot_bar, text="⭐ Make Primary", bg=self.blue_col, fg="#ffffff", activebackground="#2563eb", activeforeground="#ffffff", command=self.net_set_primary, **btn_style)
        btn_set_primary.pack(side="left", padx=6)

        btn_notes = tk.Button(bot_bar, text="✏️ Edit Note", bg=self.card_inner, fg=self.text_main, activebackground=self.border_col, activeforeground="#ffffff", command=self.net_edit_notes, **btn_style)
        btn_notes.pack(side="left", padx=6)

        return tab

    def refresh_network_list(self):
        for row in self.net_tree.get_children():
            self.net_tree.delete(row)

        def _worker():
            try:
                res = self.api.get_allocations()
                data = res.get("data", [])
                def _update():
                    for it in data:
                        attr = it.get("attributes", {})
                        a_id = attr.get("id")
                        ip = attr.get("ip") or "-"
                        port = attr.get("port") or "-"
                        conn = f"{ip}:{port}"
                        is_def = attr.get("is_default", False)
                        type_str = "⭐ PRIMARY" if is_def else "SECONDARY"
                        notes = attr.get("notes") or "-"
                        self.net_tree.insert("", "end", values=(a_id, ip, port, conn, type_str, notes))
                self.safe_after(_update)
            except Exception as e:
                self.safe_after(lambda err=e: messagebox.showerror("Network Error", f"Failed to load network: {err}"))

        threading.Thread(target=_worker, daemon=True).start()

    def net_copy_address(self):
        sel = self.net_tree.selection()
        if not sel:
            messagebox.showinfo("Select Allocation", "Please select an allocation row first.")
            return
        conn = self.net_tree.item(sel[0], "values")[3]
        self.root.clipboard_clear()
        self.root.clipboard_append(conn)
        messagebox.showinfo("Copied", f"Copied '{conn}' to clipboard!")

    def net_set_primary(self):
        sel = self.net_tree.selection()
        if not sel:
            messagebox.showinfo("Select Allocation", "Please select an allocation row first.")
            return
        a_id = int(self.net_tree.item(sel[0], "values")[0])

        def _worker():
            try:
                self.api.set_primary_allocation(a_id)
                self.safe_after(self.refresh_network_list)
            except Exception as e:
                self.safe_after(lambda err=e: messagebox.showerror("Error", str(err)))

        threading.Thread(target=_worker, daemon=True).start()

    def net_edit_notes(self):
        sel = self.net_tree.selection()
        if not sel:
            messagebox.showinfo("Select Allocation", "Please select an allocation row first.")
            return
        vals = self.net_tree.item(sel[0], "values")
        a_id = int(vals[0])
        old_note = vals[5] if vals[5] != "-" else ""

        new_note = simpledialog.askstring("Edit Note", f"Enter note for port {vals[2]}:", initialvalue=old_note, parent=self.root)
        if new_note is None:
            return

        def _worker():
            try:
                self.api.set_allocation_notes(a_id, new_note.strip())
                self.safe_after(self.refresh_network_list)
            except Exception as e:
                self.safe_after(lambda err=e: messagebox.showerror("Error", str(err)))

        threading.Thread(target=_worker, daemon=True).start()

    # =========================================================================
    # TAB 4: STARTUP & DETAILS
    # =========================================================================
    def build_startup_tab(self):
        tab = tk.Frame(self.tab_container, bg=self.bg_dark)

        # Top Information Cards
        top_frame = tk.Frame(tab, bg=self.bg_dark)
        top_frame.pack(fill="x", side="top", pady=(0, 8))

        # Server Info Card
        card_server = tk.Frame(top_frame, bg=self.card_bg, highlightbackground=self.border_col, highlightthickness=1)
        card_server.pack(side="left", fill="both", expand=True, padx=(0, 6), ipady=8)

        tk.Label(card_server, text="SERVER CONFIGURATION", font=("Segoe UI", 10, "bold"), fg=self.accent_gold, bg=self.card_bg).pack(anchor="w", padx=12, pady=(4, 6))

        self.lbl_server_name = tk.Label(card_server, text="Name: Apexsions", font=("Segoe UI", 9), fg=self.text_main, bg=self.card_bg)
        self.lbl_server_name.pack(anchor="w", padx=12, pady=2)

        self.lbl_server_node = tk.Label(card_server, text="Node: falcon04", font=("Segoe UI", 9), fg=self.text_main, bg=self.card_bg)
        self.lbl_server_node.pack(anchor="w", padx=12, pady=2)

        self.lbl_server_uuid = tk.Label(card_server, text="UUID: 27e4a2f6-8330-4ff0-bde3-9d5660650e03", font=("Segoe UI", 8), fg=self.text_dim, bg=self.card_bg)
        self.lbl_server_uuid.pack(anchor="w", padx=12, pady=2)

        self.lbl_server_sftp = tk.Label(card_server, text="SFTP: falcon04.jagoanhosting.id:2022", font=("Segoe UI", 9), fg="#38bdf8", bg=self.card_bg)
        self.lbl_server_sftp.pack(anchor="w", padx=12, pady=2)

        # Startup Command Card
        card_cmd = tk.Frame(top_frame, bg=self.card_bg, highlightbackground=self.border_col, highlightthickness=1)
        card_cmd.pack(side="right", fill="both", expand=True, padx=(6, 0), ipady=8)

        tk.Label(card_cmd, text="JAVA RUNTIME & STARTUP COMMAND", font=("Segoe UI", 10, "bold"), fg=self.accent_gold, bg=self.card_bg).pack(anchor="w", padx=12, pady=(4, 6))

        self.lbl_startup_cmd = tk.Label(card_cmd, text="java -Xms128M -Xmx...M -jar server.jar nogui", font=("Consolas", 8), fg=self.green_col, bg=self.card_bg, wraplength=480, justify="left")
        self.lbl_startup_cmd.pack(anchor="w", padx=12, pady=2)

        # Environment Variables Table
        bot_frame = tk.Frame(tab, bg=self.card_bg, highlightbackground=self.border_col, highlightthickness=1)
        bot_frame.pack(fill="both", expand=True)

        tk.Label(bot_frame, text="Server Environment Variables", font=("Segoe UI", 10, "bold"), fg=self.accent_gold, bg=self.card_bg).pack(anchor="w", padx=12, pady=8)

        columns = ("name", "key", "val", "desc")
        self.var_tree = ttk.Treeview(bot_frame, columns=columns, show="headings", selectmode="browse")

        self.var_tree.heading("name", text="Variable Name")
        self.var_tree.heading("key", text="Environment Variable")
        self.var_tree.heading("val", text="Server Value")
        self.var_tree.heading("desc", text="Description")

        self.var_tree.column("name", width=180, anchor="w")
        self.var_tree.column("key", width=180, anchor="w")
        self.var_tree.column("val", width=220, anchor="w")
        self.var_tree.column("desc", width=420, anchor="w")

        scroll_y = ttk.Scrollbar(bot_frame, orient="vertical", command=self.var_tree.yview)
        self.var_tree.configure(yscrollcommand=scroll_y.set)

        scroll_y.pack(side="right", fill="y")
        self.var_tree.pack(side="left", fill="both", expand=True, padx=6, pady=(0, 6))

        return tab

    def refresh_startup_details(self):
        def _worker():
            try:
                # 1. Server info
                info = self.api.get_server_info().get("attributes", {})
                # 2. Startup details
                startup = self.api.get_startup_details()
                meta = startup.get("meta", {})
                vars_list = startup.get("data", [])

                def _update():
                    name = info.get("name", "Apexsions")
                    node = info.get("node", "falcon04")
                    uuid_val = info.get("uuid", "")
                    sftp = info.get("sftp_details", {})
                    sftp_str = f"SFTP Host: {sftp.get('ip')}:{sftp.get('port')}" if sftp else "SFTP: Available"

                    self.lbl_server_name.config(text=f"Name: {name}")
                    self.lbl_server_node.config(text=f"Node: {node}")
                    self.lbl_server_uuid.config(text=f"UUID: {uuid_val}")
                    self.lbl_server_sftp.config(text=sftp_str)

                    cmd = meta.get("startup_command", "java -jar server.jar nogui")
                    self.lbl_startup_cmd.config(text=cmd)

                    for row in self.var_tree.get_children():
                        self.var_tree.delete(row)

                    for v in vars_list:
                        attr = v.get("attributes", {})
                        v_name = attr.get("name", "")
                        v_key = attr.get("env_variable", "")
                        v_val = attr.get("server_value", "")
                        v_desc = attr.get("description", "")
                        self.var_tree.insert("", "end", values=(v_name, v_key, v_val, v_desc))

                self.safe_after(_update)
            except Exception as e:
                print(f"Startup details error: {e}")

        threading.Thread(target=_worker, daemon=True).start()

    # =========================================================================
    # TAB 5: SFTP DEPLOY (APEXSIONS PLUGINS)
    # =========================================================================
    def build_deploy_tab(self):
        tab = tk.Frame(self.tab_container, bg=self.bg_dark)

        # Control Panel
        card = tk.Frame(tab, bg=self.card_bg, highlightbackground=self.border_col, highlightthickness=1)
        card.pack(fill="x", side="top", pady=(0, 8), ipady=8)

        tk.Label(card, text="Apexsions Plugin SFTP Deployment", font=("Segoe UI", 12, "bold"), fg=self.accent_gold, bg=self.card_bg).pack(anchor="w", padx=15, pady=(8, 4))

        tk.Label(card, text="Build local plugin JARs and deploy directly to /plugins/ on server hosting via SFTP.", font=("Segoe UI", 9), fg=self.text_dim, bg=self.card_bg).pack(anchor="w", padx=15, pady=(0, 10))

        controls_box = tk.Frame(card, bg=self.card_bg)
        controls_box.pack(fill="x", padx=15, pady=4)

        tk.Label(controls_box, text="Select Plugin:", font=("Segoe UI", 10, "bold"), fg=self.text_main, bg=self.card_bg).pack(side="left", padx=(0, 10))

        plugins = [
            "ApexsionsCore", "ApexsionsChat", "ApexsionsEconomy",
            "ApexsionsBattlepass", "ApexsionsShop", "ApexsionsMedia",
            "ApexsionsCustomEnchants", "ApexsionsCrates", "ApexsionsFishing", "ALL"
        ]
        self.deploy_var = tk.StringVar(value="ApexsionsCore")
        combo = ttk.Combobox(controls_box, values=plugins, textvariable=self.deploy_var, state="readonly", font=("Segoe UI", 10), width=24)
        combo.pack(side="left", padx=(0, 15))

        self.deploy_chk_build = tk.BooleanVar(value=True)
        cb = tk.Checkbutton(controls_box, text="Compile first with build.ps1", variable=self.deploy_chk_build, bg=self.card_bg, fg=self.text_dim, selectcolor=self.card_inner, activebackground=self.card_bg, activeforeground=self.text_main, font=("Segoe UI", 9))
        cb.pack(side="left", padx=(0, 15))

        btn_deploy = tk.Button(controls_box, text="🚀 Build & Deploy Now", font=("Segoe UI", 10, "bold"), bg=self.accent_gold, fg="#111317", activebackground=self.accent_gold_hover, bd=0, relief="flat", padx=18, pady=5, cursor="hand2", command=self.execute_sftp_deploy)
        btn_deploy.pack(side="left")

        # Deploy Output Log Box
        log_frame = tk.Frame(tab, bg=self.console_bg, highlightbackground=self.border_col, highlightthickness=1)
        log_frame.pack(fill="both", expand=True)

        self.deploy_log = tk.Text(
            log_frame,
            bg=self.console_bg,
            fg=self.text_main,
            font=("Consolas", 10),
            wrap="char",
            bd=0,
            padx=12,
            pady=10,
            relief="flat"
        )
        scroll_y = ttk.Scrollbar(log_frame, orient="vertical", command=self.deploy_log.yview)
        self.deploy_log.configure(yscrollcommand=scroll_y.set)

        scroll_y.pack(side="right", fill="y")
        self.deploy_log.pack(side="left", fill="both", expand=True)

        return tab

    def execute_sftp_deploy(self):
        chosen = self.deploy_var.get()
        do_build = self.deploy_chk_build.get()

        self.deploy_log.delete("1.0", "end")
        self.deploy_log.insert("end", f"==========================================================\n")
        self.deploy_log.insert("end", f"🚀 [DEPLOY] Starting deploy process for {chosen}...\n")
        self.deploy_log.insert("end", f"==========================================================\n")

        def _worker():
            if do_build:
                self.deploy_log.insert("end", f"🔨 [BUILD] Compiling {chosen} via build.ps1...\n")
                flag = "-all" if chosen == "ALL" else chosen
                build_cmd = f'powershell -ExecutionPolicy Bypass -File .\\Minecraft\\build.ps1 {flag}'
                ret = os.system(build_cmd)
                if ret != 0:
                    self.deploy_log.insert("end", f"❌ [BUILD FAILED] Compilation aborted.\n")
                    return

            self.deploy_log.insert("end", f"📤 [SFTP] Uploading to server hosting...\n")
            deploy_flag = "--all" if chosen == "ALL" else chosen
            deploy_cmd = f'python "{ROOT_DIR / "scripts" / "deploy_sftp.py"}" {deploy_flag}'
            ret = os.system(deploy_cmd)
            if ret == 0:
                self.deploy_log.insert("end", f"🎉 [DEPLOY SUCCESS] {chosen} deployed to server successfully!\n")
            else:
                self.deploy_log.insert("end", f"⚠️ [DEPLOY ERROR] SFTP upload failed. Check credentials or logs.\n")

        threading.Thread(target=_worker, daemon=True).start()

    # =========================================================================
    # WEBSOCKET & BACKGROUND STATS WORKERS
    # =========================================================================
    def ws_send(self, event: str, args: list):
        """Thread-safe WebSocket send."""
        with self.ws_lock:
            if self.ws and getattr(self.ws, "connected", False):
                payload = json.dumps({"event": event, "args": args})
                self.ws.send(payload)
                return True
        return False

    def start_websocket(self):
        def _ws_worker():
            while self.is_running:
                try:
                    creds = self.api.get_websocket_credentials()
                    token = creds.get("data", {}).get("token")
                    socket_url = creds.get("data", {}).get("socket")

                    if not token or not socket_url:
                        self.log_queue.put("[ERROR] Could not fetch WebSocket credentials.")
                        time.sleep(5)
                        continue

                    self.ws = websocket.create_connection(
                        socket_url,
                        origin=self.panel_url,
                        sslopt={"cert_reqs": ssl.CERT_NONE},
                        timeout=30
                    )
                    self.ws_authenticated = False

                    # Authenticate
                    with self.ws_lock:
                        self.ws.send(json.dumps({"event": "auth", "args": [token]}))

                    self.log_queue.put("🔌 [CONNECT] WebSocket connected to server. Authenticating...")

                    while self.is_running:
                        raw = self.ws.recv()
                        if not raw:
                            break
                        msg = json.loads(raw)
                        event = msg.get("event")
                        args = msg.get("args", [])

                        if event == "auth success":
                            self.ws_authenticated = True
                            self.log_queue.put("🟢 [AUTHENTICATED] Live console ready.")
                            # CRITICAL: Send request for backlog history now that we are authenticated!
                            self.ws_send("send logs", [None])

                        elif event == "console output":
                            line = args[0] if args else ""
                            self.log_queue.put(line)

                        elif event == "status":
                            st = args[0] if args else "unknown"
                            self.safe_after(lambda s=st: self.update_status_badge(s))

                        elif event == "stats":
                            if args:
                                try:
                                    s_data = json.loads(args[0]) if isinstance(args[0], str) else args[0]
                                    self.safe_after(lambda d=s_data: self.update_stats_display(d))
                                except Exception:
                                    pass

                        elif event == "token expiring":
                            new_creds = self.api.get_websocket_credentials()
                            new_token = new_creds.get("data", {}).get("token")
                            if new_token:
                                self.ws_send("auth", [new_token])

                        elif event == "token expired":
                            break

                except Exception as e:
                    self.ws_authenticated = False
                    self.log_queue.put(f"[DISCONNECTED] Console connection lost ({e}). Reconnecting in 3s...")
                    time.sleep(3)

        self.ws_thread = threading.Thread(target=_ws_worker, daemon=True)
        self.ws_thread.start()

    def start_stats_polling(self):
        def _poll_worker():
            while self.is_running:
                try:
                    res = self.api.get_resources()
                    attrs = res.get("attributes", {})
                    state = attrs.get("current_state", "unknown")
                    self.safe_after(lambda s=state: self.update_status_badge(s))

                    res_obj = attrs.get("resources", {})
                    self.safe_after(lambda d=res_obj: self.update_stats_display(d))
                except Exception:
                    pass
                time.sleep(4)

        threading.Thread(target=_poll_worker, daemon=True).start()

    def update_stats_display(self, data: dict):
        # CPU
        cpu = data.get("cpu_absolute", 0.0)
        self.lbl_cpu.config(text=f"{cpu:.1f}%")

        # RAM
        ram_bytes = data.get("memory_bytes", 0)
        ram_max = data.get("memory_limit_bytes", 12902400000)
        ram_used_gb = ram_bytes / (1024 ** 3)
        ram_max_gb = ram_max / (1024 ** 3)
        self.lbl_ram.config(text=f"{ram_used_gb:.2f} / {ram_max_gb:.1f} GB")

        # Disk
        disk_bytes = data.get("disk_bytes", 0)
        disk_gb = disk_bytes / (1024 ** 3)
        self.lbl_disk.config(text=f"{disk_gb:.1f} / 75.0 GB")

        # Network
        net = data.get("network", {})
        rx = format_bytes(net.get("rx_bytes", 0))
        tx = format_bytes(net.get("tx_bytes", 0))
        self.lbl_net.config(text=f"RX: {rx} | TX: {tx}")

        # Uptime
        uptime_ms = data.get("uptime", 0)
        self.lbl_uptime.config(text=format_uptime(uptime_ms))

    def process_queue(self):
        # Drain UI callbacks on Tkinter main thread
        try:
            while True:
                fn = self.ui_queue.get_nowait()
                try:
                    fn()
                except Exception as e:
                    print(f"UI callback error: {e}")
        except queue.Empty:
            pass

        # Drain log messages
        try:
            while True:
                msg = self.log_queue.get_nowait()
                self.append_log(msg)
        except queue.Empty:
            pass

        if self.is_running:
            self.root.after(30, self.process_queue)


def main():
    root = tk.Tk()
    app = PanelLiteApp(root)
    root.protocol("WM_DELETE_WINDOW", lambda: (setattr(app, "is_running", False), root.destroy(), sys.exit(0)))
    root.mainloop()


if __name__ == "__main__":
    main()
