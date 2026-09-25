#!/usr/bin/env python3
"""
Apexsions Panel Lite — Ultra-Lightweight Pterodactyl Desktop Client.
Uses ~20 MB of RAM compared to Chrome's 800+ MB.
Features:
- Real-time live console stream via WebSocket
- Command executor with history (Up/Down arrow keys)
- Live server resource monitors (CPU, RAM, Disk, Status)
- Server power controls (Start, Stop, Restart, Kill)
- One-click plugin deployment via SFTP
"""

import os
import re
import sys
import json
import time
import queue
import urllib.request
import urllib.error
import threading
import ssl
from pathlib import Path

# GUI Libraries (Python Built-in)
import tkinter as tk
from tkinter import ttk, messagebox, simpledialog

try:
    import websocket
except ImportError:
    print("❌ Error: websocket-client is required. Run 'pip install websocket-client'")
    sys.exit(1)

ROOT_DIR = Path(__file__).resolve().parent.parent
CONFIG_PATH = ROOT_DIR / "panel-config.json"
SFTP_CONFIG_PATH = ROOT_DIR / "sftp-config.json"

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

class PterodactylAPI:
    def __init__(self, base_url: str, api_key: str, server_id: str):
        self.base_url = base_url.rstrip("/")
        self.api_key = api_key
        self.server_id = server_id
        self.headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Accept": "application/json",
            "Content-Type": "application/json"
        }

    def _request(self, method: str, endpoint: str, data: dict = None):
        url = f"{self.base_url}/api/client{endpoint}"
        body = json.dumps(data).encode("utf-8") if data else None
        req = urllib.request.Request(url, data=body, headers=self.headers, method=method)
        try:
            with urllib.request.urlopen(req, timeout=10) as resp:
                raw = resp.read().decode("utf-8")
                return json.loads(raw) if raw else {}
        except urllib.error.HTTPError as e:
            err_msg = e.read().decode("utf-8")
            raise RuntimeError(f"HTTP {e.code}: {err_msg}")
        except Exception as e:
            raise RuntimeError(str(e))

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

class PanelLiteApp:
    def __init__(self, root: tk.Tk):
        self.root = root
        self.root.title("⚔️ Apexsions Panel Lite — Server Manager")
        self.root.geometry("1000x680")
        self.root.minsize(800, 520)

        # Styling
        self.bg_dark = "#111317"
        self.card_bg = "#1a1d24"
        self.border_col = "#2a2e39"
        self.text_main = "#f1f5f9"
        self.text_dim = "#94a3b8"
        self.accent_gold = "#f59e0b"
        self.green_col = "#10b981"
        self.red_col = "#ef4444"
        self.blue_col = "#3b82f6"
        self.console_bg = "#0a0c10"

        self.root.configure(bg=self.bg_dark)

        # Load configuration
        self.cfg = load_config()
        self.panel_url = self.cfg.get("panel_url", "https://stellar.jagoanhosting.id")
        self.api_key = self.cfg.get("api_key", "")
        self.server_id = self.cfg.get("server_id", "27e4a2f6")

        self.api = PterodactylAPI(self.panel_url, self.api_key, self.server_id)

        # WebSocket & Threading state
        self.ws = None
        self.ws_thread = None
        self.is_running = True
        self.log_queue = queue.Queue()
        self.cmd_history = []
        self.history_idx = 0

        self.build_ui()
        self.start_websocket()
        self.start_stats_polling()
        self.process_queue()

    def build_ui(self):
        # Top Header Bar
        header = tk.Frame(self.root, bg=self.card_bg, height=60, bd=0, relief="flat", highlightbackground=self.border_col, highlightthickness=1)
        header.pack(fill="x", side="top", padx=10, pady=(10, 5))

        # Title & Badge
        title_box = tk.Frame(header, bg=self.card_bg)
        title_box.pack(side="left", padx=15, pady=8)

        lbl_title = tk.Label(title_box, text="APEXSIONS SERVER", font=("Segoe UI", 13, "bold"), fg=self.accent_gold, bg=self.card_bg)
        lbl_title.pack(side="left")

        self.lbl_status = tk.Label(title_box, text=" ● CONNECTING... ", font=("Segoe UI", 9, "bold"), fg="#ffffff", bg="#64748b", padx=8, pady=2)
        self.lbl_status.pack(side="left", padx=(12, 0))

        # Stats Cards on Header Right
        stats_box = tk.Frame(header, bg=self.card_bg)
        stats_box.pack(side="right", padx=15, pady=6)

        self.lbl_cpu = tk.Label(stats_box, text="CPU: --%", font=("Segoe UI", 9, "bold"), fg=self.text_main, bg=self.card_bg)
        self.lbl_cpu.pack(side="left", padx=10)

        self.lbl_ram = tk.Label(stats_box, text="RAM: -- / 12 GB", font=("Segoe UI", 9, "bold"), fg=self.text_main, bg=self.card_bg)
        self.lbl_ram.pack(side="left", padx=10)

        self.lbl_disk = tk.Label(stats_box, text="DISK: -- / 75 GB", font=("Segoe UI", 9, "bold"), fg=self.text_main, bg=self.card_bg)
        self.lbl_disk.pack(side="left", padx=10)

        # Action / Control Toolbar
        toolbar = tk.Frame(self.root, bg=self.bg_dark)
        toolbar.pack(fill="x", side="top", padx=10, pady=4)

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

        self.btn_deploy = tk.Button(toolbar, text="🚀 Deploy Plugin (SFTP)", bg=self.accent_gold, fg="#111317", activebackground="#fbbf24", activeforeground="#111317", command=self.open_deploy_dialog, **btn_style)
        self.btn_deploy.pack(side="left", padx=6)

        # Right tools on toolbar
        self.btn_clear = tk.Button(toolbar, text="🧹 Clear Logs", bg=self.card_bg, fg=self.text_dim, activebackground=self.border_col, activeforeground="#ffffff", command=self.clear_logs, **btn_style)
        self.btn_clear.pack(side="right", padx=(6, 0))

        self.auto_scroll_var = tk.BooleanVar(value=True)
        chk_scroll = tk.Checkbutton(toolbar, text="Auto-scroll", variable=self.auto_scroll_var, bg=self.bg_dark, fg=self.text_dim, selectcolor=self.card_bg, activebackground=self.bg_dark, activeforeground=self.text_main, font=("Segoe UI", 9))
        chk_scroll.pack(side="right", padx=10)

        # Main Center: Real-Time Console Box
        console_frame = tk.Frame(self.root, bg=self.console_bg, highlightbackground=self.border_col, highlightthickness=1)
        console_frame.pack(fill="both", expand=True, padx=10, pady=5)

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
        cmd_bar = tk.Frame(self.root, bg=self.card_bg, height=45, highlightbackground=self.border_col, highlightthickness=1)
        cmd_bar.pack(fill="x", side="bottom", padx=10, pady=(5, 10))

        lbl_prompt = tk.Label(cmd_bar, text="❯", font=("Consolas", 12, "bold"), fg=self.accent_gold, bg=self.card_bg)
        lbl_prompt.pack(side="left", padx=(12, 6))

        self.cmd_entry = tk.Entry(cmd_bar, font=("Consolas", 11), bg=self.card_bg, fg="#ffffff", insertbackground="#ffffff", bd=0, relief="flat")
        self.cmd_entry.pack(side="left", fill="x", expand=True, padx=6, pady=8)
        self.cmd_entry.bind("<Return>", lambda e: self.send_command())
        self.cmd_entry.bind("<Up>", self.history_up)
        self.cmd_entry.bind("<Down>", self.history_down)

        btn_send = tk.Button(cmd_bar, text="Send ↵", bg=self.accent_gold, fg="#111317", activebackground="#fbbf24", font=("Segoe UI", 9, "bold"), bd=0, relief="flat", padx=15, pady=4, cursor="hand2", command=self.send_command)
        btn_send.pack(side="right", padx=8, pady=6)

        self.cmd_entry.focus()

    def append_log(self, text: str):
        clean_text = ANSI_REGEX.sub('', text)
        tag = None
        lower = clean_text.lower()
        if "warn" in lower:
            tag = "warn"
        elif "err" in lower or "fatal" in lower or "severe" in lower or "exception" in lower:
            tag = "error"
        elif "success" in lower or "connected" in lower or "enabled" in lower:
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

    def send_command(self):
        cmd = self.cmd_entry.get().strip()
        if not cmd:
            return
        self.cmd_entry.delete(0, "end")
        self.cmd_history.append(cmd)
        self.history_idx = len(self.cmd_history)

        self.append_log(f"\n❯ {cmd}")

        def _worker():
            if self.ws and self.ws.sock and self.ws.sock.connected:
                try:
                    payload = json.dumps({"event": "send command", "args": [cmd]})
                    self.ws.send(payload)
                    return
                except Exception:
                    pass
            # Fallback to REST API
            try:
                self.api.send_command(cmd)
            except Exception as e:
                self.log_queue.put(f"[ERROR] Failed to send command: {e}")

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
                self.log_queue.put(f"[PANEL] Power signal '{signal}' sent successfully.")
            except Exception as e:
                self.log_queue.put(f"[PANEL ERROR] Power signal '{signal}' failed: {e}")

        threading.Thread(target=_worker, daemon=True).start()

    def open_deploy_dialog(self):
        plugins = [
            "ApexsionsCore", "ApexsionsChat", "ApexsionsEconomy",
            "ApexsionsBattlepass", "ApexsionsShop", "ApexsionsMedia",
            "ApexsionsCustomEnchants", "ApexsionsCrates", "ApexsionsFishing", "ALL"
        ]
        dialog = tk.Toplevel(self.root)
        dialog.title("Deploy Plugin via SFTP")
        dialog.geometry("380x300")
        dialog.configure(bg=self.card_bg)
        dialog.transient(self.root)
        dialog.grab_set()

        lbl = tk.Label(dialog, text="Select Plugin to Build & Deploy:", font=("Segoe UI", 10, "bold"), fg=self.text_main, bg=self.card_bg)
        lbl.pack(pady=(15, 8))

        var_choice = tk.StringVar(value="ApexsionsCore")
        combo = ttk.Combobox(dialog, values=plugins, textvariable=var_choice, state="readonly", font=("Segoe UI", 10))
        combo.pack(padx=20, pady=8, fill="x")

        chk_build = tk.BooleanVar(value=True)
        cb = tk.Checkbutton(dialog, text="Compile/Build first with build.ps1", variable=chk_build, bg=self.card_bg, fg=self.text_dim, selectcolor=self.bg_dark, activebackground=self.card_bg, activeforeground=self.text_main)
        cb.pack(pady=5)

        lbl_info = tk.Label(dialog, text="JAR will be uploaded directly to /plugins/ on server.", font=("Segoe UI", 8), fg=self.text_dim, bg=self.card_bg)
        lbl_info.pack(pady=8)

        def _start_deploy():
            chosen = var_choice.get()
            do_build = chk_build.get()
            dialog.destroy()
            self.log_queue.put(f"\n==========================================")
            self.log_queue.put(f"🚀 [DEPLOY] Starting deploy process for {chosen}...")
            self.log_queue.put(f"==========================================")

            def _deploy_worker():
                if do_build:
                    self.log_queue.put(f"🔨 [BUILD] Compiling {chosen}...")
                    flag = "-all" if chosen == "ALL" else chosen
                    build_cmd = f'powershell -ExecutionPolicy Bypass -File .\\Minecraft\\build.ps1 {flag}'
                    ret = os.system(build_cmd)
                    if ret != 0:
                        self.log_queue.put(f"❌ [BUILD FAILED] Compilation aborted.")
                        return

                self.log_queue.put(f"📤 [SFTP] Uploading to server...")
                deploy_flag = "--all" if chosen == "ALL" else chosen
                deploy_cmd = f'python "{ROOT_DIR / "scripts" / "deploy_sftp.py"}" {deploy_flag}'
                ret = os.system(deploy_cmd)
                if ret == 0:
                    self.log_queue.put(f"🎉 [DEPLOY SUCCESS] {chosen} deployed to server!")
                else:
                    self.log_queue.put(f"⚠️ [DEPLOY ERROR] SFTP upload failed.")

            threading.Thread(target=_deploy_worker, daemon=True).start()

        btn_run = tk.Button(dialog, text="🚀 Deploy Now", bg=self.accent_gold, fg="#111317", font=("Segoe UI", 10, "bold"), bd=0, relief="flat", padx=15, pady=6, cursor="hand2", command=_start_deploy)
        btn_run.pack(pady=12)

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
                    # Authenticate
                    self.ws.send(json.dumps({"event": "auth", "args": [token]}))
                    self.log_queue.put("🔌 [CONNECT] WebSocket connected to server console.")

                    # Request initial backlog logs
                    self.ws.send(json.dumps({"event": "send logs", "args": [None]}))

                    while self.is_running:
                        raw = self.ws.recv()
                        if not raw:
                            break
                        msg = json.loads(raw)
                        event = msg.get("event")
                        args = msg.get("args", [])

                        if event == "console output":
                            line = args[0] if args else ""
                            self.log_queue.put(line)
                        elif event == "status":
                            st = args[0] if args else "unknown"
                            self.root.after(0, lambda s=st: self.update_status_badge(s))
                        elif event == "stats":
                            if args:
                                try:
                                    s_data = json.loads(args[0]) if isinstance(args[0], str) else args[0]
                                    self.root.after(0, lambda d=s_data: self.update_stats_display(d))
                                except Exception:
                                    pass
                        elif event == "token expiring":
                            # Refresh token
                            new_creds = self.api.get_websocket_credentials()
                            new_token = new_creds.get("data", {}).get("token")
                            if new_token:
                                self.ws.send(json.dumps({"event": "auth", "args": [new_token]}))

                except Exception as e:
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
                    self.root.after(0, lambda s=state: self.update_status_badge(s))

                    res_obj = attrs.get("resources", {})
                    self.root.after(0, lambda d=res_obj: self.update_stats_display(d))
                except Exception:
                    pass
                time.sleep(4)

        threading.Thread(target=_poll_worker, daemon=True).start()

    def update_stats_display(self, data: dict):
        # CPU
        cpu = data.get("cpu_absolute", 0.0)
        self.lbl_cpu.config(text=f"CPU: {cpu:.1f}%")

        # RAM
        ram_bytes = data.get("memory_bytes", 0)
        ram_gb = ram_bytes / (1024 ** 3)
        self.lbl_ram.config(text=f"RAM: {ram_gb:.2f} / 12.00 GB")

        # Disk
        disk_bytes = data.get("disk_bytes", 0)
        disk_gb = disk_bytes / (1024 ** 3)
        self.lbl_disk.config(text=f"DISK: {disk_gb:.1f} / 75.0 GB")

    def process_queue(self):
        try:
            while True:
                msg = self.log_queue.get_nowait()
                self.append_log(msg)
        except queue.Empty:
            pass
        if self.is_running:
            self.root.after(50, self.process_queue)

def main():
    root = tk.Tk()
    app = PanelLiteApp(root)
    root.protocol("WM_DELETE_WINDOW", lambda: (setattr(app, "is_running", False), root.destroy(), sys.exit(0)))
    root.mainloop()

if __name__ == "__main__":
    main()
