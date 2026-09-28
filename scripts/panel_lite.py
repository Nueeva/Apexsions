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
import webbrowser
import subprocess

# GUI Libraries (Python Built-in)
import tkinter as tk
from tkinter import ttk, messagebox, simpledialog, filedialog

try:
    import websocket
except ImportError:
    print("❌ Error: websocket-client is required. Run 'pip install websocket-client'")
    sys.exit(1)

if getattr(sys, "frozen", False):
    exe_dir = Path(sys.executable).resolve().parent
    if (exe_dir / "panel-config.json").exists():
        ROOT_DIR = exe_dir
    elif (exe_dir.parent / "panel-config.json").exists():
        ROOT_DIR = exe_dir.parent
    else:
        ROOT_DIR = exe_dir
else:
    ROOT_DIR = Path(__file__).resolve().parent.parent

CONFIG_PATH = ROOT_DIR / "panel-config.json"
SFTP_CONFIG_PATH = ROOT_DIR / "sftp-config.json"

if sys.platform == "win32":
    try:
        import ctypes
        ctypes.windll.shell32.SetCurrentProcessExplicitAppUserModelID("Apexsions.ServerPanel.1.0")
    except Exception:
        pass

# Regex for stripping ANSI escape codes
ANSI_REGEX = re.compile(r'\x1B(?:[@-Z\\-_]|\[[0-?]*[ -/]*[@-~])')

TARGET_SELECTORS = [
    ("@a", "All Players"),
    ("@p", "Nearest Player"),
    ("@r", "Random Player"),
    ("@s", "Current Target"),
]

COMMON_ITEMS = [
    "diamond", "diamond_sword", "diamond_pickaxe", "diamond_axe", "diamond_chestplate", "diamond_helmet", "diamond_leggings", "diamond_boots",
    "netherite_ingot", "netherite_sword", "netherite_pickaxe", "netherite_axe", "netherite_chestplate", "netherite_helmet", "netherite_leggings", "netherite_boots",
    "iron_ingot", "gold_ingot", "emerald", "coal", "copper_ingot", "lapis_lazuli", "redstone",
    "golden_apple", "enchanted_golden_apple", "totem_of_undying", "elytra", "experience_bottle",
    "ender_pearl", "bow", "arrow", "shield", "trident", "shulker_box",
    "cooked_beef", "bread", "golden_carrot"
]

COMMON_EFFECTS = [
    "speed", "slowness", "haste", "mining_fatigue", "strength", "instant_health", "instant_damage",
    "jump_boost", "nausea", "regeneration", "resistance", "fire_resistance", "water_breathing",
    "invisibility", "blindness", "night_vision", "hunger", "weakness", "poison", "wither",
    "health_boost", "absorption", "glowing", "levitation", "luck", "bad_luck", "slow_falling"
]

COMMON_ENCHANTS = [
    "protection", "fire_protection", "feather_falling", "blast_protection", "projectile_protection",
    "respiration", "aqua_affinity", "thorns", "depth_strider", "frost_walker", "soul_speed",
    "sharpness", "smite", "bane_of_arthropods", "knockback", "fire_aspect", "looting",
    "efficiency", "silk_touch", "unbreaking", "fortune",
    "power", "punch", "flame", "infinity",
    "luck_of_the_sea", "lure", "mending"
]

COMMAND_TREE = {
    "gamemode": {
        "syntax": "/gamemode <creative|survival|adventure|spectator> <player>",
        "args": [
            [("creative", "Creative Mode"), ("survival", "Survival Mode"), ("adventure", "Adventure Mode"), ("spectator", "Spectator Mode")],
            "<player>"
        ]
    },
    "difficulty": {
        "syntax": "/difficulty <peaceful|easy|normal|hard>",
        "args": [
            [("peaceful", "Peaceful"), ("easy", "Easy"), ("normal", "Normal"), ("hard", "Hard")]
        ]
    },
    "weather": {
        "syntax": "/weather <clear|rain|thunder> [duration]",
        "args": [
            [("clear", "Clear Skies"), ("rain", "Rainfall"), ("thunder", "Thunderstorm")],
            [("60", "1 Minute"), ("300", "5 Minutes"), ("600", "10 Minutes"), ("1200", "20 Minutes")]
        ]
    },
    "time": {
        "syntax": "/time <set|add|query> <value>",
        "args": [
            [("set", "Set Time"), ("add", "Advance Time"), ("query", "Query Time")],
            {
                "set": [("day", "Day (1000)"), ("noon", "Noon (6000)"), ("night", "Night (13000)"), ("midnight", "Midnight (18000)"), ("0", "Sunrise (0)")],
                "add": [("1000", "+1000 Ticks"), ("6000", "+6000 Ticks"), ("12000", "+12000 Ticks")],
                "query": [("daytime", "Time of Day"), ("gametime", "Total Game Time"), ("day", "Day Count")]
            }
        ]
    },
    "gamerule": {
        "syntax": "/gamerule <rule> [true|false]",
        "args": [
            [
                ("keepInventory", "Keep items on death"),
                ("mobGriefing", "Allow mob destruction"),
                ("doDaylightCycle", "Sun/moon movement"),
                ("doWeatherCycle", "Dynamic weather"),
                ("doMobSpawning", "Natural mob spawns"),
                ("doFireTick", "Fire spreading"),
                ("pvp", "Player vs Player damage"),
                ("naturalRegeneration", "Heal from hunger"),
                ("showDeathMessages", "Broadcast deaths"),
                ("commandBlockOutput", "Log command blocks"),
                ("randomTickSpeed", "Plant growth speed")
            ],
            [("true", "Enable"), ("false", "Disable")]
        ]
    },
    "whitelist": {
        "syntax": "/whitelist <add|remove|list|on|off|reload> [player]",
        "args": [
            [("add", "Add player to whitelist"), ("remove", "Remove player from whitelist"), ("list", "List whitelisted players"), ("on", "Enable whitelist"), ("off", "Disable whitelist"), ("reload", "Reload whitelist.json")],
            {
                "add": "<player>",
                "remove": "<player>"
            }
        ]
    },
    "op": {
        "syntax": "/op <player>",
        "args": ["<player>"]
    },
    "deop": {
        "syntax": "/deop <player>",
        "args": ["<player>"]
    },
    "ban": {
        "syntax": "/ban <player> [reason]",
        "args": [
            "<player>",
            [("Cheating / Hacking", "Ban Reason"), ("Griefing / Stealing", "Ban Reason"), ("Rule Violation", "Ban Reason"), ("Toxicity / Harassment", "Ban Reason")]
        ]
    },
    "pardon": {
        "syntax": "/pardon <player>",
        "args": ["<player>"]
    },
    "unban": {
        "syntax": "/unban <player>",
        "args": ["<player>"]
    },
    "kick": {
        "syntax": "/kick <player> [reason]",
        "args": [
            "<player>",
            [("AFK Timeout", "Kick Reason"), ("Server Restarting", "Kick Reason"), ("Rule Warning", "Kick Reason")]
        ]
    },
    "tp": {
        "syntax": "/tp <target> <destination>",
        "args": ["<player>", "<player>"]
    },
    "teleport": {
        "syntax": "/teleport <target> <destination>",
        "args": ["<player>", "<player>"]
    },
    "kill": {
        "syntax": "/kill <player>",
        "args": ["<player>"]
    },
    "give": {
        "syntax": "/give <player> <item> [amount]",
        "args": [
            "<player>",
            [(item, "Minecraft Item") for item in COMMON_ITEMS],
            [("1", "1 item"), ("16", "16 items (1/4 stack)"), ("32", "32 items (1/2 stack)"), ("64", "64 items (1 stack)")]
        ]
    },
    "clear": {
        "syntax": "/clear <player> [item]",
        "args": [
            "<player>",
            [(item, "Clear specific item") for item in COMMON_ITEMS]
        ]
    },
    "effect": {
        "syntax": "/effect <give|clear> <player> [effect] [seconds] [amplifier]",
        "args": [
            [("give", "Apply potion effect"), ("clear", "Remove potion effect")],
            {
                "give": "<player>",
                "clear": "<player>"
            },
            [(eff, "Potion Effect") for eff in COMMON_EFFECTS],
            [("30", "30 Seconds"), ("60", "1 Minute"), ("300", "5 Minutes"), ("infinite", "Infinite Duration")],
            [("1", "Level 1"), ("2", "Level 2"), ("3", "Level 3"), ("4", "Level 4"), ("5", "Level 5")]
        ]
    },
    "enchant": {
        "syntax": "/enchant <player> <enchantment> [level]",
        "args": [
            "<player>",
            [(ench, "Enchantment") for ench in COMMON_ENCHANTS],
            [("1", "Level 1"), ("2", "Level 2"), ("3", "Level 3"), ("4", "Level 4"), ("5", "Level 5")]
        ]
    },
    "xp": {
        "syntax": "/xp <add|set|query> <player> [amount]",
        "args": [
            [("add", "Add experience"), ("set", "Set experience"), ("query", "Check experience")],
            "<player>",
            [("100", "100 XP points"), ("500", "500 XP points"), ("1000", "1000 XP points"), ("10L", "10 Levels"), ("30L", "30 Levels"), ("50L", "50 Levels")]
        ]
    },
    "experience": {
        "syntax": "/experience <add|set|query> <player> [amount]",
        "args": [
            [("add", "Add experience"), ("set", "Set experience"), ("query", "Check experience")],
            "<player>",
            [("100", "100 points"), ("500", "500 points"), ("10 levels", "10 Levels"), ("30 levels", "30 Levels")]
        ]
    },
    "msg": {
        "syntax": "/msg <player> <message>",
        "args": ["<player>"]
    },
    "tell": {
        "syntax": "/tell <player> <message>",
        "args": ["<player>"]
    },
    "w": {
        "syntax": "/w <player> <message>",
        "args": ["<player>"]
    },
    "spawn": {
        "syntax": "/spawn <player>",
        "args": ["<player>"]
    },
    # Apexsions Custom Plugins Console Admin
    "eco": {
        "syntax": "/eco <give|take|set|balance|reload> <player> [amount]",
        "args": [
            [("give", "Deposit Rupiah balance"), ("take", "Deduct Rupiah balance"), ("set", "Set exact Rupiah balance"), ("balance", "Check balance"), ("reload", "Reload economy config")],
            "<player>",
            [("10000", "Rp 10.000"), ("50000", "Rp 50.000"), ("100000", "Rp 100.000"), ("500000", "Rp 500.000"), ("1000000", "Rp 1.000.000")]
        ]
    },
    "economy": {
        "syntax": "/economy <give|take|set|balance|reload> <player> [amount]",
        "args": [
            [("give", "Deposit Rupiah balance"), ("take", "Deduct Rupiah balance"), ("set", "Set exact Rupiah balance"), ("balance", "Check balance"), ("reload", "Reload config")],
            "<player>",
            [("10000", "Rp 10.000"), ("50000", "Rp 50.000"), ("100000", "Rp 100.000"), ("500000", "Rp 500.000"), ("1000000", "Rp 1.000.000")]
        ]
    },
    "crates": {
        "syntax": "/crates <key|reload> [args...]",
        "args": [
            [("key", "Manage crate keys"), ("reload", "Reload crates config")],
            {
                "key": [("give", "Give crate key"), ("giveall", "Give key to all players"), ("take", "Take crate key"), ("set", "Set key balance")]
            },
            {
                "give": "<player>",
                "giveall": [("common", "Common Key"), ("rare", "Rare Key"), ("epic", "Epic Key"), ("legendary", "Legendary Key"), ("mythic", "Mythic Key"), ("ancient", "Ancient Key")],
                "take": "<player>",
                "set": "<player>"
            },
            [("common", "Common Crate"), ("rare", "Rare Crate"), ("epic", "Epic Crate"), ("legendary", "Legendary Crate"), ("mythic", "Mythic Crate"), ("ancient", "Ancient Crate")],
            [("1", "1 Key"), ("3", "3 Keys"), ("5", "5 Keys"), ("10", "10 Keys")]
        ]
    },
    "battlepass": {
        "syntax": "/battlepass <setlevel|addxp|reset|reload> <player> [amount]",
        "args": [
            [("setlevel", "Set player pass level"), ("addxp", "Add BattlePass XP"), ("reset", "Reset player progression"), ("reload", "Reload config")],
            "<player>",
            [("1", "Level 1"), ("5", "Level 5"), ("10", "Level 10"), ("20", "Level 20"), ("30", "Level 30"), ("50", "Level 50"), ("100", "Level 100 / Max")]
        ]
    },
    "fishing": {
        "syntax": "/fishing <setlevel|addxp|reload> <player> [amount]",
        "args": [
            [("setlevel", "Set angler level"), ("addxp", "Add fishing XP"), ("reload", "Reload config")],
            "<player>",
            [("1", "Level 1"), ("5", "Level 5"), ("10", "Level 10"), ("25", "Level 25"), ("50", "Level 50")]
        ]
    },
    "shop": {
        "syntax": "/shop reload",
        "args": [
            [("reload", "Reload dynamic shop prices & configs")]
        ]
    },
    "chat": {
        "syntax": "/chat <mute|unmute|clear|reload> [player]",
        "args": [
            [("mute", "Mute player in chat"), ("unmute", "Unmute player"), ("clear", "Clear chat history"), ("reload", "Reload chat config")],
            {
                "mute": "<player>",
                "unmute": "<player>"
            }
        ]
    },
    "customenchants": {
        "syntax": "/customenchants <give|reload> <player> [enchant]",
        "args": [
            [("give", "Give custom enchant book"), ("reload", "Reload enchants config")],
            {
                "give": "<player>"
            }
        ]
    },
    "media": {
        "syntax": "/media <reload|status>",
        "args": [
            [("reload", "Reload media assets"), ("status", "View raytrace status")]
        ]
    },
    "apx": {
        "syntax": "/apx <reload|version|inspect|sync|status> [player]",
        "args": [
            [("reload", "Reload all Apexsions modules"), ("version", "Show ecosystem version"), ("inspect", "Inspect 360 player data"), ("sync", "Force WebBridge sync"), ("status", "Check system status")],
            {
                "inspect": "<player>"
            }
        ]
    },
    "apexsions": {
        "syntax": "/apexsions <reload|version|inspect|status> [player]",
        "args": [
            [("reload", "Reload all Apexsions modules"), ("version", "Show ecosystem version"), ("inspect", "Inspect 360 player data"), ("status", "Check system status")],
            {
                "inspect": "<player>"
            }
        ]
    }
}

# Integrate extended plugin command trees
try:
    from new_command_trees import CONSOLE_COMMAND_TREES
    COMMAND_TREE.update(CONSOLE_COMMAND_TREES)
except Exception:
    pass

# Integrate complete server registered commands catalog
try:
    from server_commands_data import SERVER_COMMANDS
except Exception:
    SERVER_COMMANDS = {}

COMMON_COMMANDS = sorted(set(list(COMMAND_TREE.keys()) + list(SERVER_COMMANDS.keys()) + [
    "help", "version", "plugins", "spark", "timings", "stop", "restart", "reload", "save-all", "save-off", "save-on", "list"
]))

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
        self.website_url = self.cfg.get("website_url", "https://web.apexsions.com")
        self.bridge_secret = self.cfg.get("bridge_secret", "apexsions_bridge_key_live_2026")

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

        # Player & Command Auto-Complete State
        self.online_players = set()
        self.known_players = self.load_known_players()
        self.ac_popup = None
        self.ac_listbox = None
        self.ac_syntax_lbl = None
        self.ac_candidates = []
        self._suppress_autocomplete = False
        self._ac_navigated = False

        # Setup ttk styles for Treeview & Combobox
        self.setup_ttk_styles()

        # Build UI layout
        self.build_ui()

        # Start background workers
        self.start_websocket()
        self.start_stats_polling()
        self.process_queue()
        self.root.after(1500, self.trim_memory)

    def trim_memory(self):
        """Actively trims unused working set memory on Windows to keep RAM footprint ultra-low."""
        if not self.is_running:
            return
        if sys.platform == "win32":
            try:
                import gc
                gc.collect()
                h = ctypes.windll.kernel32.OpenProcess(0x0410, False, os.getpid())
                if h:
                    ctypes.windll.psapi.EmptyWorkingSet(h)
                    ctypes.windll.kernel32.CloseHandle(h)
            except Exception:
                pass
        if self.is_running:
            self.root.after(45000, self.trim_memory)

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

        lbl_host = tk.Label(brand_frame, text="apexsions.com:32348", font=("Segoe UI", 9), fg=self.text_dim, bg=self.card_bg)
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
            ("deploy", "🚀 SFTP Deploy"),
            ("website", "👑 Web Portal")
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
        self.frames["website"] = self.build_website_tab()

        # Default active tab
        self.current_tab = None
        self.switch_tab("console")

    def switch_tab(self, tab_id: str):
        self.hide_autocomplete()
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
        elif tab_id == "website":
            self.refresh_website_health()

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

        # Online players indicator badge
        self.lbl_online_badge = tk.Button(
            toolbar,
            text="👥 0 Online",
            font=("Segoe UI", 9, "bold"),
            bg=self.card_bg,
            fg=self.text_dim,
            activebackground=self.border_col,
            activeforeground="#ffffff",
            bd=0,
            relief="flat",
            padx=8,
            pady=4,
            cursor="hand2",
            command=lambda: self.send_command("list")
        )
        self.lbl_online_badge.pack(side="left", padx=(8, 3))

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

        self.cmd_var = tk.StringVar(tab)
        self.cmd_var.trace_add("write", self.on_cmd_var_change)

        self.cmd_entry = tk.Entry(cmd_bar, textvariable=self.cmd_var, font=("Consolas", 11), bg=self.card_bg, fg="#ffffff", insertbackground="#ffffff", bd=0, relief="flat")
        self.cmd_entry.pack(side="left", fill="x", expand=True, padx=6, pady=8)
        self.cmd_entry.bind("<KeyRelease>", self.on_cmd_keyrelease)
        self.cmd_entry.bind("<Tab>", self.on_cmd_tab)
        self.cmd_entry.bind("<Return>", self.on_cmd_return)
        self.cmd_entry.bind("<Up>", self.on_cmd_up)
        self.cmd_entry.bind("<Down>", self.on_cmd_down)
        self.cmd_entry.bind("<Escape>", lambda e: self.hide_autocomplete())
        self.cmd_entry.bind("<FocusOut>", lambda e: self.root.after(150, self.hide_autocomplete))

        btn_send = tk.Button(cmd_bar, text="Send ↵", bg=self.accent_gold, fg="#111317", activebackground=self.accent_gold_hover, font=("Segoe UI", 9, "bold"), bd=0, relief="flat", padx=16, pady=4, cursor="hand2", command=self.send_command)
        btn_send.pack(side="right", padx=8, pady=6)

        return tab

    def append_log(self, text: str):
        clean_text = ANSI_REGEX.sub('', text)
        self.parse_player_events(clean_text)
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
        self.hide_autocomplete()
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

    # =========================================================================
    # COMMAND AUTOCOMPLETE & PLAYER TRACKING SYSTEM
    # =========================================================================
    def load_known_players(self):
        appdata = os.getenv("APPDATA") or str(Path.home() / "AppData" / "Roaming")
        f = Path(appdata) / "Apexsions" / "known_players.json"
        defaults = ["Rafriel", "Nueeva", "Steve", "Alex"]
        if f.exists():
            try:
                with open(f, "r", encoding="utf-8") as fp:
                    data = json.load(fp)
                    if isinstance(data, list) and data:
                        return set(data)
            except Exception:
                pass
        return set(defaults)

    def save_known_players(self):
        try:
            appdata = os.getenv("APPDATA") or str(Path.home() / "AppData" / "Roaming")
            f = Path(appdata) / "Apexsions" / "known_players.json"
            f.parent.mkdir(parents=True, exist_ok=True)
            with open(f, "w", encoding="utf-8") as fp:
                json.dump(sorted(list(self.known_players)), fp, indent=2)
        except Exception:
            pass

    def update_online_badge(self):
        count = len(self.online_players)
        if hasattr(self, "lbl_online_badge"):
            self.lbl_online_badge.config(
                text=f"👥 {count} Online",
                fg=self.green_col if count > 0 else self.text_dim
            )

    def parse_player_events(self, text: str):
        changed = False
        # 1. Join event: "PlayerName joined the game" or "PlayerName[/IP:port] logged in" or "UUID of player PlayerName is"
        m_join = re.search(r'(\b[a-zA-Z0-9_]{3,16}\b)(?:\[.*?\])?\s+(?:joined the game|logged in with entity id)', text)
        if not m_join:
            m_join = re.search(r'UUID of player (\b[a-zA-Z0-9_]{3,16}\b) is', text)
        if m_join:
            p = m_join.group(1)
            if p not in self.online_players:
                self.online_players.add(p)
                changed = True
            if p not in self.known_players:
                self.known_players.add(p)
                self.save_known_players()

        # 2. Leave event: "PlayerName lost connection" or "PlayerName left the game"
        m_leave = re.search(r'(\b[a-zA-Z0-9_]{3,16}\b)\s+(?:lost connection|left the game)', text)
        if m_leave:
            p = m_leave.group(1)
            if p in self.online_players:
                self.online_players.discard(p)
                changed = True

        # 3. List command response: e.g. "There are 2 of a max of 100 players online: PlayerA, PlayerB"
        m_list = re.search(r'(?:players online:|online players:)\s*([^\n\r]+)', text, re.IGNORECASE)
        if m_list:
            raw_names = m_list.group(1).split(",")
            names = [n.strip() for n in raw_names if re.match(r'^[a-zA-Z0-9_]{3,16}$', n.strip())]
            self.online_players = set(names)
            for n in names:
                self.known_players.add(n)
            self.save_known_players()
            changed = True

        if changed:
            self.safe_after(self.update_online_badge)

    def get_current_word_context(self):
        try:
            cursor_pos = self.cmd_entry.index("insert")
        except Exception:
            cursor_pos = len(self.cmd_entry.get())
        full_text = self.cmd_entry.get()
        before = full_text[:cursor_pos]
        after = full_text[cursor_pos:]

        m = re.search(r'([a-zA-Z0-9_\-\.\/@]+)$', before)
        word = m.group(1) if m else ""
        return word, cursor_pos, full_text, before, after

    def on_cmd_var_change(self, *args):
        if self._suppress_autocomplete:
            return
        self._ac_navigated = False
        self.root.after_idle(self.trigger_autocomplete)

    def trigger_autocomplete(self):
        if self._suppress_autocomplete:
            return
        word, cursor_pos, full_text, before, after = self.get_current_word_context()
        if not before:
            self.hide_autocomplete()
            return

        candidates, syntax_hint = self.get_minecraft_autocomplete(before)
        if candidates:
            self.show_autocomplete(candidates, syntax_hint)
        else:
            self.hide_autocomplete()

    def get_minecraft_autocomplete(self, before: str):
        if not before:
            return [], ""

        has_slash = before.startswith("/")
        clean = before[1:] if has_slash else before
        
        tokens = clean.split(" ")
        arg_idx = len(tokens) - 1
        current_tok = tokens[-1]
        tok_lower = current_tok.lower()
        
        candidates = []
        syntax_hint = ""

        # ARG 0: Command names
        if arg_idx == 0:
            prefix_matches = []
            contains_matches = []
            for cmd in COMMON_COMMANDS:
                prefix = "/" if has_slash else ""
                desc = ""
                if cmd in COMMAND_TREE:
                    syn = COMMAND_TREE[cmd].get("syntax", "")
                    desc = syn.replace(f"/{cmd} ", "") if syn.startswith(f"/{cmd} ") else syn
                elif cmd in SERVER_COMMANDS:
                    desc = SERVER_COMMANDS[cmd]
                
                disp_name = f"{prefix}{cmd}"
                raw_val = f"{prefix}{cmd}"
                disp_label = f"⚡ {disp_name}  [{desc}]" if desc else f"⚡ {disp_name}"
                
                cmd_lower = cmd.lower()
                if not tok_lower:
                    prefix_matches.append((disp_label, raw_val))
                elif cmd_lower.startswith(tok_lower):
                    prefix_matches.append((disp_label, raw_val))
                elif tok_lower in cmd_lower:
                    contains_matches.append((disp_label, raw_val))

            if tok_lower and tok_lower in COMMAND_TREE:
                syntax_hint = COMMAND_TREE[tok_lower].get("syntax", "")
            elif tok_lower and tok_lower in SERVER_COMMANDS:
                syntax_hint = f"/{tok_lower} — {SERVER_COMMANDS[tok_lower]}"
            
            final_cands = prefix_matches if prefix_matches else contains_matches
            return final_cands, syntax_hint

        # ARG >= 1: Arguments for a command
        cmd_name = tokens[0].lower()
        tree_entry = COMMAND_TREE.get(cmd_name)
        if tree_entry:
            syntax_hint = tree_entry.get("syntax", f"/{cmd_name}")
            args_list = tree_entry.get("args", [])
            arg_pos = arg_idx - 1
            
            spec = None
            if arg_pos < len(args_list):
                spec = args_list[arg_pos]
                if isinstance(spec, dict):
                    found = None
                    for prev in reversed(tokens[1:arg_idx]):
                        p_low = prev.lower()
                        if p_low in spec:
                            found = spec[p_low]
                            break
                    spec = found
            else:
                spec = None
        else:
            spec = "<player>"

        raw_candidates = []
        
        def _get_player_candidates():
            res = []
            for sel, desc in TARGET_SELECTORS:
                res.append((f"🎯 {sel}  [{desc}]", sel))
            for p in sorted(self.online_players):
                res.append((f"🟢 {p}  [Online]", p))
            online_lower = {p.lower() for p in self.online_players}
            for p in sorted(self.known_players):
                if p.lower() not in online_lower:
                    res.append((f"⚪ {p}  [Known]", p))
            return res

        if spec == "<player>":
            raw_candidates = _get_player_candidates()
        elif spec == "<item>":
            for item in COMMON_ITEMS:
                raw_candidates.append((f"📦 {item}", item))
        elif spec == "<effect>":
            for eff in COMMON_EFFECTS:
                raw_candidates.append((f"🧪 {eff}", eff))
        elif spec == "<enchant>":
            for ench in COMMON_ENCHANTS:
                raw_candidates.append((f"✨ {ench}", ench))
        elif isinstance(spec, list):
            for item in spec:
                if isinstance(item, tuple) and len(item) == 2:
                    val, desc = item
                    raw_candidates.append((f"⚙️ {val}  [{desc}]", val))
                elif isinstance(item, str):
                    raw_candidates.append((f"⚙️ {item}", item))
        elif spec is None and arg_idx >= 1:
            raw_candidates = _get_player_candidates()

        prefix_matches = []
        contains_matches = []
        for disp, val in raw_candidates:
            val_lower = val.lower()
            if not tok_lower:
                prefix_matches.append((disp, val))
            elif val_lower.startswith(tok_lower):
                prefix_matches.append((disp, val))
            elif tok_lower in val_lower or tok_lower in disp.lower():
                contains_matches.append((disp, val))

        final_cands = prefix_matches if prefix_matches else contains_matches
        return final_cands, syntax_hint

    def show_autocomplete(self, candidates, syntax_hint: str = ""):
        if not candidates:
            self.hide_autocomplete()
            return

        self.ac_candidates = candidates

        if self.ac_popup is None or not self.ac_popup.winfo_exists():
            self.ac_popup = tk.Toplevel(self.root)
            self.ac_popup.wm_overrideredirect(True)
            self.ac_popup.configure(bg=self.border_col)

            hdr = tk.Frame(self.ac_popup, bg=self.card_inner)
            hdr.pack(fill="x", padx=1, pady=(1, 0))

            self.ac_syntax_lbl = tk.Label(
                hdr,
                text="",
                font=("Consolas", 9, "bold"),
                fg=self.accent_gold,
                bg=self.card_inner,
                anchor="w"
            )
            self.ac_syntax_lbl.pack(fill="x", padx=8, pady=(4, 1))

            self.ac_help_lbl = tk.Label(
                hdr,
                text="Tab: Lengkapi • ↑↓: Pilih • Esc: Tutup",
                font=("Segoe UI", 7),
                fg=self.text_dim,
                bg=self.card_inner,
                anchor="w"
            )
            self.ac_help_lbl.pack(fill="x", padx=8, pady=(0, 4))

            body = tk.Frame(self.ac_popup, bg=self.bg_dark)
            body.pack(fill="both", expand=True, padx=1, pady=(0, 1))

            self.ac_listbox = tk.Listbox(
                body,
                bg=self.console_bg,
                fg=self.text_main,
                selectbackground=self.accent_gold,
                selectforeground="#111317",
                font=("Consolas", 10),
                bd=0,
                highlightthickness=0,
                activestyle="none"
            )
            sb = ttk.Scrollbar(body, orient="vertical", command=self.ac_listbox.yview)
            self.ac_listbox.configure(yscrollcommand=sb.set)
            sb.pack(side="right", fill="y")
            self.ac_listbox.pack(side="left", fill="both", expand=True)

            self.ac_listbox.bind("<ButtonRelease-1>", self.on_ac_click)

        # Update syntax hint
        if self.ac_syntax_lbl and self.ac_syntax_lbl.winfo_exists():
            if syntax_hint:
                self.ac_syntax_lbl.config(text=f"📖 {syntax_hint}")
            else:
                self.ac_syntax_lbl.config(text="⚡ MINECRAFT AUTOCOMPLETE")

        self.ac_listbox.delete(0, "end")
        for disp, val in candidates[:20]:
            self.ac_listbox.insert("end", f" {disp}")

        self.ac_listbox.selection_clear(0, "end")
        self.ac_listbox.selection_set(0)
        self.ac_listbox.see(0)

        self.root.update_idletasks()
        try:
            entry_x = self.cmd_entry.winfo_rootx()
            entry_y = self.cmd_entry.winfo_rooty()
            entry_w = self.cmd_entry.winfo_width()

            num_items = min(len(candidates), 8)
            popup_h = 48 + num_items * 22
            popup_w = max(420, min(entry_w, 640))

            pos_x = entry_x
            pos_y = entry_y - popup_h - 4
            if pos_y < 10:
                pos_y = entry_y + self.cmd_entry.winfo_height() + 4

            self.ac_popup.geometry(f"{popup_w}x{popup_h}+{pos_x}+{pos_y}")
            self.ac_popup.deiconify()
            self.ac_popup.lift()
        except Exception:
            pass

    def hide_autocomplete(self):
        if self.ac_popup and self.ac_popup.winfo_exists():
            self.ac_popup.withdraw()
        self.ac_candidates = []
        self._ac_navigated = False

    def apply_autocomplete(self, replacement: str):
        word, cursor_pos, full_text, before, after = self.get_current_word_context()
        has_slash = before.startswith("/")
        clean = before[1:] if has_slash else before
        tokens = clean.split(" ")
        current_token = tokens[-1] if tokens else ""

        if current_token:
            base = before[:-len(current_token)]
        else:
            base = before

        if base == "/" and replacement.startswith("/"):
            base = ""

        clean_after = after.lstrip(" ")
        new_text = base + replacement + " " + clean_after
        new_cursor = len(base + replacement + " ")

        self._suppress_autocomplete = True
        self.cmd_entry.delete(0, "end")
        self.cmd_entry.insert(0, new_text)
        self.cmd_entry.icursor(new_cursor)
        self._suppress_autocomplete = False

        self.root.after_idle(self.trigger_autocomplete)

    def on_cmd_keyrelease(self, event):
        if event.keysym in ("Left", "Right", "Home", "End"):
            self.root.after_idle(self.trigger_autocomplete)

    def on_cmd_tab(self, event):
        if self.ac_popup and self.ac_popup.winfo_exists() and self.ac_popup.winfo_ismapped() and self.ac_candidates:
            sel = self.ac_listbox.curselection()
            idx = sel[0] if sel else 0
            if idx < len(self.ac_candidates):
                disp, val = self.ac_candidates[idx]
                self.apply_autocomplete(val)
            return "break"

        word, cursor_pos, full_text, before, after = self.get_current_word_context()
        candidates, syntax_hint = self.get_minecraft_autocomplete(before)
        if len(candidates) == 1:
            self.apply_autocomplete(candidates[0][1])
        elif len(candidates) > 1:
            self.show_autocomplete(candidates, syntax_hint)
        return "break"

    def on_cmd_return(self, event):
        if self.ac_popup and self.ac_popup.winfo_exists() and self.ac_popup.winfo_ismapped() and self.ac_candidates and self._ac_navigated:
            sel = self.ac_listbox.curselection()
            if sel:
                idx = sel[0]
                if idx < len(self.ac_candidates):
                    disp, val = self.ac_candidates[idx]
                    self.apply_autocomplete(val)
                    return "break"

        self.hide_autocomplete()
        self.send_command()
        return "break"

    def on_cmd_up(self, event):
        if self.ac_popup and self.ac_popup.winfo_exists() and self.ac_popup.winfo_ismapped() and self.ac_candidates:
            self._ac_navigated = True
            sel = self.ac_listbox.curselection()
            cur = sel[0] if sel else 0
            new_idx = max(0, cur - 1)
            self.ac_listbox.selection_clear(0, "end")
            self.ac_listbox.selection_set(new_idx)
            self.ac_listbox.see(new_idx)
            return "break"
        return self.history_up(event)

    def on_cmd_down(self, event):
        if self.ac_popup and self.ac_popup.winfo_exists() and self.ac_popup.winfo_ismapped() and self.ac_candidates:
            self._ac_navigated = True
            sel = self.ac_listbox.curselection()
            cur = sel[0] if sel else 0
            new_idx = min(len(self.ac_candidates) - 1, cur + 1)
            self.ac_listbox.selection_clear(0, "end")
            self.ac_listbox.selection_set(new_idx)
            self.ac_listbox.see(new_idx)
            return "break"
        return self.history_down(event)

    def on_ac_click(self, event):
        sel = self.ac_listbox.curselection()
        if sel:
            idx = sel[0]
            if idx < len(self.ac_candidates):
                disp, val = self.ac_candidates[idx]
                self.apply_autocomplete(val)
        self.cmd_entry.focus_set()

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
    # TAB 6: WEBSITE OPS & QUICK LAUNCHER
    # =========================================================================
    def build_website_tab(self):
        tab = tk.Frame(self.tab_container, bg=self.bg_dark)

        # Header / Status Card
        header_card = tk.Frame(tab, bg=self.card_bg, bd=0, highlightbackground=self.border_col, highlightthickness=1)
        header_card.pack(fill="x", side="top", pady=(0, 10), ipady=4)

        top_row = tk.Frame(header_card, bg=self.card_bg)
        top_row.pack(fill="x", padx=15, pady=(8, 4))

        tk.Label(top_row, text="👑 APEXSIONS WEB PLATFORM", font=("Segoe UI", 12, "bold"), fg=self.accent_gold, bg=self.card_bg).pack(side="left")
        self.lbl_web_url = tk.Label(top_row, text=self.website_url, font=("Segoe UI", 10), fg=self.text_dim, bg=self.card_bg)
        self.lbl_web_url.pack(side="left", padx=12)

        btn_refresh = tk.Button(top_row, text="🔄 Check Health", font=("Segoe UI", 9, "bold"), bg=self.card_inner, fg=self.text_main, activebackground=self.border_col, activeforeground="#ffffff", bd=0, relief="flat", padx=12, pady=4, cursor="hand2", command=self.refresh_website_health)
        btn_refresh.pack(side="right")

        # Badges Row
        badges_row = tk.Frame(header_card, bg=self.card_bg)
        badges_row.pack(fill="x", padx=15, pady=(4, 6))

        def make_badge(parent, label, default_val, default_color):
            f = tk.Frame(parent, bg=self.card_bg)
            f.pack(side="left", padx=(0, 24))
            tk.Label(f, text=label, font=("Segoe UI", 8, "bold"), fg=self.text_dim, bg=self.card_bg).pack(anchor="w")
            lbl = tk.Label(f, text=default_val, font=("Segoe UI", 9, "bold"), fg=default_color, bg=self.card_bg)
            lbl.pack(anchor="w")
            return lbl

        self.lbl_web_status = make_badge(badges_row, "HTTP STATUS", "● Checking...", "#eab308")
        self.lbl_web_latency = make_badge(badges_row, "RESPONSE LATENCY", "-- ms", self.text_main)
        self.lbl_web_ssl = make_badge(badges_row, "SSL / TLS", "🔒 HTTPS Active", self.green_col)
        self.lbl_web_bridge = make_badge(badges_row, "WEBBRIDGE API", "🔗 Standby", self.blue_col)

        # Hero Action Bar: In-App Desktop Launcher & Mode Toggle (No Chrome Needed!)
        hero_bar = tk.Frame(header_card, bg=self.card_inner, bd=0, highlightbackground=self.border_col, highlightthickness=1)
        hero_bar.pack(fill="x", padx=15, pady=(4, 8), ipady=3)

        btn_hero_inapp = tk.Button(
            hero_bar,
            text="🖥️ BUKA IN-APP WEB PORTAL (Aplikasi Desktop Tanpa Chrome)",
            font=("Segoe UI", 10, "bold"),
            bg=self.accent_gold,
            fg="#111317",
            activebackground=self.accent_gold_hover,
            activeforeground="#111317",
            bd=0,
            relief="flat",
            padx=16,
            pady=6,
            cursor="hand2",
            command=lambda: self.launch_in_app_portal(self.website_url)
        )
        btn_hero_inapp.pack(side="left", padx=10, pady=4)

        self.web_mode_inapp = tk.BooleanVar(value=True)
        cb_mode = tk.Checkbutton(
            hero_bar,
            text="Selalu buka rute di Aplikasi Desktop (Bukan Chrome)",
            variable=self.web_mode_inapp,
            bg=self.card_inner,
            fg=self.text_main,
            selectcolor=self.card_bg,
            activebackground=self.card_inner,
            activeforeground=self.accent_gold,
            font=("Segoe UI", 9)
        )
        cb_mode.pack(side="left", padx=10)

        btn_hero_browser = tk.Button(
            hero_bar,
            text="🌐 Buka di Chrome ↗",
            font=("Segoe UI", 9),
            bg=self.card_bg,
            fg=self.text_dim,
            activebackground=self.border_col,
            activeforeground="#ffffff",
            bd=0,
            relief="flat",
            padx=12,
            pady=4,
            cursor="hand2",
            command=lambda: self.open_browser(self.website_url)
        )
        btn_hero_browser.pack(side="right", padx=10)

        # Main Split Content: Left = Quick Launchers, Right = Tools & Console
        content_frame = tk.Frame(tab, bg=self.bg_dark)
        content_frame.pack(fill="both", expand=True)

        # Left Column: Quick Admin & Portal Launchers (Cards)
        left_col = tk.Frame(content_frame, bg=self.card_bg, bd=0, highlightbackground=self.border_col, highlightthickness=1, width=460)
        left_col.pack(side="left", fill="both", padx=(0, 8), pady=0)
        left_col.pack_propagate(False)

        left_header = tk.Frame(left_col, bg=self.card_inner, height=36)
        left_header.pack(fill="x", side="top")
        tk.Label(left_header, text="🚀 QUICK ADMIN & PORTAL LAUNCHERS", font=("Segoe UI", 9, "bold"), fg=self.accent_gold, bg=self.card_inner).pack(side="left", padx=12, pady=8)

        # Scrollable container for launcher buttons
        launchers_canvas = tk.Canvas(left_col, bg=self.card_bg, bd=0, highlightthickness=0)
        launchers_scrollbar = ttk.Scrollbar(left_col, orient="vertical", command=launchers_canvas.yview)
        launchers_inner = tk.Frame(launchers_canvas, bg=self.card_bg)

        launchers_inner.bind("<Configure>", lambda e: launchers_canvas.configure(scrollregion=launchers_canvas.bbox("all")))
        canvas_win = launchers_canvas.create_window((0, 0), window=launchers_inner, anchor="nw")
        launchers_canvas.bind("<Configure>", lambda e: launchers_canvas.itemconfig(canvas_win, width=e.width))
        launchers_canvas.configure(yscrollcommand=launchers_scrollbar.set)

        launchers_scrollbar.pack(side="right", fill="y")
        launchers_canvas.pack(side="left", fill="both", expand=True, padx=6, pady=6)

        launcher_items = [
            ("👑 Executive Dashboard", "/admin/dashboard", "Pusat komando staf, telemetri TPS, RAM & antrean", self.accent_gold),
            ("👥 Player Management 360", "/admin/players", "Pencarian pemain via UUID, saldo, level & sanksi", self.blue_col),
            ("🛡 Moderation & Reports", "/admin/moderation", "Laporan pemain in-game & audit hukuman aktif", self.red_col),
            ("💰 Economy & Ledger", "/admin/economy", "Ledger transaksi Rupiah & Diamond, lelang AH", self.green_col),
            ("⚙️ Server & Maintenance", "/admin/server", "Kontrol pemeliharaan, whitelist, dan status node", "#a855f7"),
            ("📦 Custom Plugins Registry", "/admin/custom-plugins", "Status 9 modul plugin & verifikasi safe actions", "#06b6d4"),
            ("🏆 Leaderboard Hall of Fame", "/leaderboard", "Papan skor Level/EXP & Perbendaharaan publik", self.accent_gold),
            ("🛍 Official Web Store", "/shop", "Etalase donasi kasta, paket & item peradaban", self.green_col),
            ("🌐 Website Homepage", "/", "Halaman utama beranda sinematik Apexsions", self.text_main),
        ]

        for title, path, desc, accent in launcher_items:
            full_url = f"{self.website_url.rstrip('/')}{path}"
            card = tk.Frame(launchers_inner, bg=self.card_inner, bd=0, highlightbackground=self.border_col, highlightthickness=1)
            card.pack(fill="x", pady=4, padx=4)

            top_f = tk.Frame(card, bg=self.card_inner)
            top_f.pack(fill="x", padx=10, pady=(8, 2))

            tk.Label(top_f, text=title, font=("Segoe UI", 9, "bold"), fg=self.text_main, bg=self.card_inner).pack(side="left")

            btn_box = tk.Frame(top_f, bg=self.card_inner)
            btn_box.pack(side="right")

            btn_open = tk.Button(
                btn_box,
                text="🖥️ In-App",
                font=("Segoe UI", 8, "bold"),
                bg=accent,
                fg="#111317",
                activebackground="#ffffff",
                activeforeground="#111317",
                bd=0,
                relief="flat",
                padx=8,
                pady=2,
                cursor="hand2",
                command=lambda u=full_url: self.open_web_route(u, force_browser=False)
            )
            btn_open.pack(side="left", padx=(0, 4))

            btn_chrome = tk.Button(
                btn_box,
                text="↗ Chrome",
                font=("Segoe UI", 8),
                bg=self.card_bg,
                fg=self.text_dim,
                activebackground=self.border_col,
                activeforeground="#ffffff",
                bd=0,
                relief="flat",
                padx=6,
                pady=2,
                cursor="hand2",
                command=lambda u=full_url: self.open_browser(u)
            )
            btn_chrome.pack(side="left")

            bot_f = tk.Frame(card, bg=self.card_inner)
            bot_f.pack(fill="x", padx=10, pady=(0, 8))

            tk.Label(bot_f, text=desc, font=("Segoe UI", 8), fg=self.text_dim, bg=self.card_inner).pack(side="left")
            tk.Label(bot_f, text=path, font=("Consolas", 8), fg=self.accent_gold, bg=self.card_inner).pack(side="right")

        # Right Column: WebBridge Diagnostics & Log Console
        right_col = tk.Frame(content_frame, bg=self.bg_dark)
        right_col.pack(side="right", fill="both", expand=True)

        # Action bar on top of console
        tools_bar = tk.Frame(right_col, bg=self.card_bg, bd=0, highlightbackground=self.border_col, highlightthickness=1)
        tools_bar.pack(fill="x", side="top", pady=(0, 8))

        tk.Label(tools_bar, text="🛠 Tools & Diagnostics:", font=("Segoe UI", 9, "bold"), fg=self.text_main, bg=self.card_bg).pack(side="left", padx=12, pady=8)

        btn_admin_inapp = tk.Button(tools_bar, text="👑 Launch Admin In-App", font=("Segoe UI", 9, "bold"), bg=self.accent_gold, fg="#111317", activebackground=self.accent_gold_hover, activeforeground="#111317", bd=0, relief="flat", padx=10, pady=4, cursor="hand2", command=lambda: self.launch_in_app_portal(f"{self.website_url.rstrip('/')}/admin"))
        btn_admin_inapp.pack(side="left", padx=4)

        btn_probe = tk.Button(tools_bar, text="⚡ Test WebBridge", font=("Segoe UI", 9, "bold"), bg=self.blue_col, fg="#ffffff", activebackground="#2563eb", activeforeground="#ffffff", bd=0, relief="flat", padx=10, pady=4, cursor="hand2", command=self.test_webbridge_probe)
        btn_probe.pack(side="left", padx=4)

        btn_folder = tk.Button(tools_bar, text="📂 Folder", font=("Segoe UI", 9, "bold"), bg=self.card_inner, fg=self.text_main, activebackground=self.border_col, activeforeground="#ffffff", bd=0, relief="flat", padx=10, pady=4, cursor="hand2", command=self.open_website_folder)
        btn_folder.pack(side="left", padx=4)

        btn_copy_admin = tk.Button(tools_bar, text="📋 Copy URL", font=("Segoe UI", 9, "bold"), bg=self.card_inner, fg=self.text_dim, activebackground=self.border_col, activeforeground="#ffffff", bd=0, relief="flat", padx=10, pady=4, cursor="hand2", command=lambda: self.copy_to_clipboard(f"{self.website_url.rstrip('/')}/admin", "Admin URL"))
        btn_copy_admin.pack(side="left", padx=4)


        # Log Text Box
        log_frame = tk.Frame(right_col, bg=self.console_bg, highlightbackground=self.border_col, highlightthickness=1)
        log_frame.pack(fill="both", expand=True)

        self.web_log = tk.Text(
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
        scroll_y = ttk.Scrollbar(log_frame, orient="vertical", command=self.web_log.yview)
        self.web_log.configure(yscrollcommand=scroll_y.set)

        scroll_y.pack(side="right", fill="y")
        self.web_log.pack(side="left", fill="both", expand=True)

        self.web_log_write("==========================================================")
        self.web_log_write("👑 APEXSIONS WEB PLATFORM MANAGER (Azuriom & WebBridge)")
        self.web_log_write(f"🌐 Target URL : {self.website_url}")
        self.web_log_write(f"📁 Local Path : {ROOT_DIR / 'Website'}")
        self.web_log_write("==========================================================\n")

        return tab

    def web_log_write(self, msg: str):
        if hasattr(self, "web_log"):
            t = time.strftime("%H:%M:%S")
            self.web_log.insert("end", f"[{t}] {msg}\n")
            self.web_log.see("end")

    def open_browser(self, url: str):
        try:
            webbrowser.open(url)
            self.web_log_write(f"🚀 [BROWSER] Opened: {url}")
        except Exception as e:
            self.web_log_write(f"❌ [BROWSER ERROR] {e}")

    def launch_in_app_portal(self, url: str):
        self.web_log_write(f"🖥️ [IN-APP PORTAL] Opening native desktop window: {url}")
        try:
            if getattr(sys, "frozen", False):
                cmd = [sys.executable, "--web-portal", url]
            else:
                cmd = [sys.executable, str(Path(__file__).resolve()), "--web-portal", url]

            creationflags = 0
            if sys.platform == "win32":
                creationflags = subprocess.CREATE_NO_WINDOW
            subprocess.Popen(cmd, creationflags=creationflags)
            self.web_log_write("✅ [IN-APP PORTAL] Native desktop window active (No Chrome required)!")
        except Exception as e:
            self.web_log_write(f"⚠️ [LAUNCH FALLBACK] Could not launch native window: {e}")
            self.open_browser(url)

    def open_web_route(self, url: str, force_browser: bool = False):
        if force_browser or (hasattr(self, "web_mode_inapp") and not self.web_mode_inapp.get()):
            self.open_browser(url)
        else:
            self.launch_in_app_portal(url)

    def open_website_folder(self):
        web_dir = ROOT_DIR / "Website"
        try:
            if sys.platform == "win32":
                os.startfile(str(web_dir))
            else:
                import subprocess
                subprocess.Popen(["xdg-open", str(web_dir)])
            self.web_log_write(f"📂 [EXPLORER] Opened local folder: {web_dir}")
        except Exception as e:
            self.web_log_write(f"❌ [FOLDER ERROR] {e}")

    def copy_to_clipboard(self, text: str, label: str):
        self.root.clipboard_clear()
        self.root.clipboard_append(text)
        self.web_log_write(f"📋 [CLIPBOARD] Copied {label}: {text}")
        messagebox.showinfo("Copied", f"{label} copied to clipboard!\n{text}")

    def refresh_website_health(self):
        def _check():
            self.safe_after(lambda: self.lbl_web_status.config(text="● Probing...", fg="#eab308"))
            t0 = time.time()
            try:
                ctx = ssl.create_default_context()
                ctx.check_hostname = False
                ctx.verify_mode = ssl.CERT_NONE
                req = urllib.request.Request(self.website_url, headers={"User-Agent": "ApexsionsPanel/1.0"})
                with urllib.request.urlopen(req, context=ctx, timeout=8) as resp:
                    latency = round((time.time() - t0) * 1000, 1)
                    code = resp.status
                    self.safe_after(lambda: self.lbl_web_status.config(text=f"● {code} OK", fg=self.green_col))
                    self.safe_after(lambda: self.lbl_web_latency.config(text=f"⚡ {latency} ms", fg=self.green_col if latency < 500 else "#eab308"))
                    self.safe_after(lambda: self.web_log_write(f"✅ [HEALTH] {self.website_url} is ONLINE (HTTP {code}, {latency} ms)"))
            except Exception as e:
                self.safe_after(lambda: self.lbl_web_status.config(text="● OFFLINE", fg=self.red_col))
                self.safe_after(lambda: self.lbl_web_latency.config(text="-- ms", fg=self.red_col))
                self.safe_after(lambda: self.web_log_write(f"❌ [HEALTH ERROR] {e}"))

        threading.Thread(target=_check, daemon=True).start()

    def test_webbridge_probe(self):
        def _probe():
            endpoint = f"{self.website_url.rstrip('/')}/api/apexsions-bridge/heartbeat"
            self.safe_after(lambda: self.web_log_write(f"⚡ [PROBE] Sending WebBridge Heartbeat Probe to: {endpoint}"))
            t0 = time.time()
            try:
                ctx = ssl.create_default_context()
                ctx.check_hostname = False
                ctx.verify_mode = ssl.CERT_NONE
                payload = json.dumps({"probe": True, "source": "ApexsionsPanel"}).encode("utf-8")
                req = urllib.request.Request(
                    endpoint,
                    data=payload,
                    headers={
                        "Content-Type": "application/json",
                        "Accept": "application/json",
                        "X-Apexsions-Key": self.bridge_secret,
                        "User-Agent": "ApexsionsPanel/1.0"
                    }
                )
                with urllib.request.urlopen(req, context=ctx, timeout=8) as resp:
                    latency = round((time.time() - t0) * 1000, 1)
                    body = resp.read().decode("utf-8")
                    self.safe_after(lambda: self.lbl_web_bridge.config(text="🔗 Connected (200 OK)", fg=self.green_col))
                    self.safe_after(lambda: self.web_log_write(f"🎉 [PROBE SUCCESS] Response ({latency} ms): {body}"))
            except urllib.error.HTTPError as e:
                err_text = e.read().decode("utf-8", errors="replace")
                self.safe_after(lambda: self.lbl_web_bridge.config(text=f"⚠️ HTTP {e.code}", fg="#eab308"))
                self.safe_after(lambda: self.web_log_write(f"⚠️ [PROBE HTTP {e.code}] {err_text}"))
            except Exception as e:
                self.safe_after(lambda: self.lbl_web_bridge.config(text="❌ Failed", fg=self.red_col))
                self.safe_after(lambda: self.web_log_write(f"❌ [PROBE ERROR] {e}"))

        threading.Thread(target=_probe, daemon=True).start()

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
                            # Auto-query online player list
                            threading.Thread(target=lambda: (time.sleep(1), self.ws_send("send command", ["list"])), daemon=True).start()

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


def run_in_app_web_portal(target_url: str):
    """
    Launches an embedded Microsoft Edge WebView2 native desktop window.
    Completely independent of Google Chrome, using Windows native Edge runtime.
    Maintains user login session, cookies, and local storage in AppData/Apexsions.
    """
    try:
        import webview
    except ImportError:
        import webbrowser
        webbrowser.open(target_url)
        return

    # User data directory for persistent cookies, logins, and cache
    appdata = os.getenv("APPDATA") or str(Path.home() / "AppData" / "Roaming")
    cache_dir = Path(appdata) / "Apexsions" / "WebPortalData"
    cache_dir.mkdir(parents=True, exist_ok=True)

    # Set Windows Process App ID so the window has its own taskbar identity
    if sys.platform == "win32":
        try:
            import ctypes
            ctypes.windll.shell32.SetCurrentProcessExplicitAppUserModelID("Apexsions.WebPortal.1.0")
        except Exception:
            pass

    window = webview.create_window(
        title="Apexsions Web Portal — The Peak Civilizations",
        url=target_url,
        width=1340,
        height=860,
        min_size=(960, 600),
        confirm_close=False,
        text_select=True,
        zoomable=True,
    )
    # Start webview with persistent session storage (no Chrome needed!)
    webview.start(private_mode=False, storage_path=str(cache_dir))


def main():
    if len(sys.argv) > 1 and sys.argv[1] == "--web-portal":
        target = sys.argv[2] if len(sys.argv) > 2 else "https://web.apexsions.com"
        run_in_app_web_portal(target)
        sys.exit(0)

    root = tk.Tk()
    app = PanelLiteApp(root)
    root.protocol("WM_DELETE_WINDOW", lambda: (setattr(app, "is_running", False), root.destroy(), sys.exit(0)))
    root.mainloop()


if __name__ == "__main__":
    main()
