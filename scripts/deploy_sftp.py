#!/usr/bin/env python3
"""
Apexsions SFTP Deployment Tool
Uploads compiled Minecraft plugins directly to your game server hosting via SFTP.
"""

import os
import sys
import json
import time
import argparse
from pathlib import Path

# Force UTF-8 encoding on Windows console
if sys.platform == "win32":
    try:
        sys.stdout.reconfigure(encoding="utf-8")
        sys.stderr.reconfigure(encoding="utf-8")
    except Exception:
        pass

try:
    import paramiko
except ImportError:
    print("❌ Error: 'paramiko' is required. Run 'pip install paramiko' first.")
    sys.exit(1)

ROOT_DIR = Path(__file__).resolve().parent.parent
CONFIG_PATH = ROOT_DIR / "sftp-config.json"
DEFAULT_LIBS_DIR = ROOT_DIR / "Minecraft" / "build" / "libs"

PLUGIN_MAP = {
    "core": "ApexsionsCore",
    "apexsionscore": "ApexsionsCore",
    "chat": "ApexsionsChat",
    "apexsionschat": "ApexsionsChat",
    "economy": "ApexsionsEconomy",
    "apexsionseconomy": "ApexsionsEconomy",
    "battlepass": "ApexsionsBattlepass",
    "apexsionsbattlepass": "ApexsionsBattlepass",
    "shop": "ApexsionsShop",
    "apexsionsshop": "ApexsionsShop",
    "media": "ApexsionsMedia",
    "apexsionsmedia": "ApexsionsMedia",
    "customenchants": "ApexsionsCustomEnchants",
    "apexsionscustomenchants": "ApexsionsCustomEnchants",
    "crates": "ApexsionsCrates",
    "apexsionscrates": "ApexsionsCrates",
    "fishing": "ApexsionsFishing",
    "apexsionsfishing": "ApexsionsFishing",
}

def load_config():
    if not CONFIG_PATH.exists():
        print(f"❌ Configuration file not found at: {CONFIG_PATH}")
        print("💡 Create 'sftp-config.json' with your credentials:")
        example_config = {
            "host": "falcon04.jagoanhosting.id",
            "port": 2022,
            "username": "lcq3449a.27e4a2f6",
            "password": "YOUR_SFTP_PASSWORD_HERE",
            "remote_path": "plugins"
        }
        print(json.dumps(example_config, indent=2))
        sys.exit(1)

    try:
        with open(CONFIG_PATH, "r", encoding="utf-8") as f:
            cfg = json.load(f)
            return cfg
    except Exception as e:
        print(f"❌ Failed to parse '{CONFIG_PATH}': {e}")
        sys.exit(1)

def print_progress(transferred, total, start_time):
    percent = (transferred / total) * 100 if total > 0 else 0
    elapsed = max(0.001, time.time() - start_time)
    speed_kb = (transferred / 1024) / elapsed
    bar_len = 30
    filled = int(bar_len * transferred // total) if total > 0 else 0
    bar = "=" * filled + "-" * (bar_len - filled)
    sys.stdout.write(f"\r  [{bar}] {percent:5.1f}% ({transferred / 1024 / 1024:.2f}/{total / 1024 / 1024:.2f} MB) @ {speed_kb:.1f} KB/s")
    sys.stdout.flush()

def upload_file(sftp, local_file: Path, remote_folder: str):
    filename = local_file.name
    # Normalise remote path
    remote_folder = remote_folder.strip("/")
    remote_dir = f"/{remote_folder}" if remote_folder else ""
    remote_file = f"{remote_dir}/{filename}"

    # Ensure remote directory exists
    try:
        sftp.chdir(remote_dir)
    except IOError:
        print(f"  📁 Remote directory '{remote_dir}' not found, creating...")
        try:
            sftp.mkdir(remote_dir)
        except Exception as e:
            print(f"  ⚠️ Could not create '{remote_dir}': {e}")

    file_size = local_file.stat().st_size
    print(f"\n🚀 Uploading {filename} ({file_size / 1024 / 1024:.2f} MB) -> {remote_file}")
    
    start_time = time.time()
    try:
        sftp.put(str(local_file), remote_file, callback=lambda tr, tot: print_progress(tr, tot, start_time))
        sys.stdout.write("\n")
        duration = round(time.time() - start_time, 2)
        print(f"  ✅ [SUCCESS] {filename} uploaded in {duration}s!")
        return True
    except Exception as e:
        sys.stdout.write("\n")
        print(f"  ❌ [FAILED] Error uploading {filename}: {e}")
        return False

def main():
    parser = argparse.ArgumentParser(description="Upload Apexsions plugins to SFTP hosting server.")
    parser.add_argument("target", nargs="?", default="", help="Plugin name (e.g. Core, Fishing, Chat) or 'all'")
    parser.add_argument("--all", action="store_true", help="Upload all official Apexsions plugins")
    parser.add_argument("--file", type=str, help="Specific file path to upload")
    parser.add_argument("--test", action="store_true", help="Test connection only")

    args = parser.parse_args()
    config = load_config()

    host = config.get("host")
    port = int(config.get("port", 2022))
    username = config.get("username")
    password = config.get("password")
    remote_path = config.get("remote_path", "plugins")

    if not host or not username or not password or password == "YOUR_SFTP_PASSWORD_HERE":
        print("❌ Error: SFTP host, username, or password is not properly configured in 'sftp-config.json'.")
        sys.exit(1)

    print("==================================================")
    print("      🚀 APEXSIONS SFTP DEPLOYMENT TOOL")
    print("==================================================")
    print(f"🌐 Host:     {host}:{port}")
    print(f"👤 User:     {username}")
    print(f"📂 Remote:   /{remote_path.strip('/')}")
    print("--------------------------------------------------")

    # Connect to SSH/SFTP
    print("🔌 Connecting to server...")
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())

    try:
        ssh.connect(
            hostname=host,
            port=port,
            username=username,
            password=password,
            timeout=15,
            look_for_keys=False,
            allow_agent=False
        )
        sftp = ssh.open_sftp()
        print("✅ SFTP Connected successfully!\n")
    except Exception as e:
        print(f"❌ Connection failed: {e}")
        sys.exit(1)

    try:
        if args.test:
            print("🔍 Listing files in remote directory...")
            try:
                files = sftp.listdir(f"/{remote_path.strip('/')}")
                print(f"Found {len(files)} files in /{remote_path.strip('/')}:")
                for f in sorted(files)[:15]:
                    print(f"  - {f}")
                if len(files) > 15:
                    print(f"  ... and {len(files) - 15} more.")
            except Exception as e:
                print(f"Notice: Root files: {sftp.listdir('.')}")
            return

        files_to_upload = []

        if args.file:
            target_path = Path(args.file)
            if not target_path.exists():
                print(f"❌ File not found: {target_path}")
                sys.exit(1)
            files_to_upload.append(target_path)
        elif args.all or args.target.lower() == "all":
            for canonical in sorted(set(PLUGIN_MAP.values())):
                jar_path = DEFAULT_LIBS_DIR / f"{canonical}-1.0.0.jar"
                if jar_path.exists():
                    files_to_upload.append(jar_path)
                else:
                    print(f"⚠️ Warning: {jar_path.name} not found in {DEFAULT_LIBS_DIR}. Build it first!")
        elif args.target:
            target_norm = args.target.lower().strip()
            canonical = PLUGIN_MAP.get(target_norm)
            if not canonical:
                print(f"❌ Unknown plugin target '{args.target}'. Valid targets: {', '.join(sorted(set(PLUGIN_MAP.values())))} or 'all'.")
                sys.exit(1)
            jar_path = DEFAULT_LIBS_DIR / f"{canonical}-1.0.0.jar"
            if not jar_path.exists():
                print(f"❌ JAR file not found: {jar_path}")
                print(f"💡 Run build first: powershell -ExecutionPolicy Bypass -File .\\Minecraft\\build.ps1 {canonical}")
                sys.exit(1)
            files_to_upload.append(jar_path)
        else:
            # Default: look for any recently modified jars in DEFAULT_LIBS_DIR
            print("💡 No target specified. Options:")
            print("   python scripts/deploy_sftp.py <PluginName>  (e.g. Core, Fishing)")
            print("   python scripts/deploy_sftp.py --all")
            print("   python scripts/deploy_sftp.py --test")
            return

        if not files_to_upload:
            print("❌ No files selected for upload.")
            return

        print(f"📦 Starting deployment of {len(files_to_upload)} plugin(s)...")
        success_count = 0
        for f in files_to_upload:
            if upload_file(sftp, f, remote_path):
                success_count += 1

        print("\n==================================================")
        if success_count == len(files_to_upload):
            print(f"🎉 ALL {success_count} PLUGIN(S) DEPLOYED SUCCESSFULLY!")
        else:
            print(f"⚠️ {success_count}/{len(files_to_upload)} plugins deployed. Check logs above.")
        print("==================================================")

    finally:
        sftp.close()
        ssh.close()
        print("🔒 SFTP Connection closed.")

if __name__ == "__main__":
    main()
