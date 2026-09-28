#!/usr/bin/env python3
"""
Console-Safe Command Tree Catalog for Apexsions Panel.
Excludes WorldEdit and player-only in-game actions (GUI openers, movement, line-of-sight).
Tailored strictly for Server Console / Terminal execution where target player is required.
"""

CONSOLE_COMMAND_TREES = {
    # --- LuckPerms ---
    "lp": {
        "syntax": "/lp <user|group|editor|sync|reload|info>",
        "args": [
            [("user", "Manage user permissions"), ("group", "Manage group permissions"), ("editor", "Open web permission editor"), ("sync", "Sync permissions with database"), ("reload", "Reload LuckPerms"), ("info", "View LuckPerms info")],
            {
                "user": "<player>",
                "group": [("ancestor", "Tier V Owner"), ("architect", "Tier IV Authority"), ("overseer", "Tier IV Authority"), ("warden", "Tier III Admin"), ("herald", "Tier III Staff"), ("sions", "Tier II Apex Donator"), ("emperor", "Tier II Donator 4"), ("sovereign", "Tier II Donator 3"), ("archon", "Tier II Donator 2"), ("ascendant", "Tier II Donator 1"), ("wanderer", "Tier I Default")]
            },
            [("permission", "Permission operations"), ("parent", "Parent group operations"), ("info", "Show permission tree"), ("meta", "Metadata and prefixes"), ("editor", "Open targeted editor")]
        ]
    },
    "luckperms": {
        "syntax": "/luckperms <user|group|editor|sync|reload|info>",
        "args": [
            [("user", "Manage user permissions"), ("group", "Manage group permissions"), ("editor", "Open web editor"), ("sync", "Sync permissions"), ("reload", "Reload LuckPerms"), ("info", "System info")]
        ]
    },
    # --- WorldGuard ---
    "rg": {
        "syntax": "/rg <remove|info|list|flag|addmember|removemember|addowner|removeowner|reload> [args...]",
        "args": [
            [("remove", "Delete region"), ("info", "Display region info"), ("list", "List all regions in world"), ("flag", "Set or remove region flag"), ("addmember", "Add member to region"), ("removemember", "Remove member"), ("addowner", "Add owner to region"), ("removeowner", "Remove owner"), ("reload", "Reload WorldGuard")],
            {
                "remove": [("spawn", "Spawn Region"), ("warzone", "Warzone Region"), ("pvp", "PvP Arena")],
                "info": [("spawn", "Spawn Region"), ("warzone", "Warzone Region"), ("pvp", "PvP Arena")],
                "flag": [("spawn", "Spawn Region"), ("warzone", "Warzone Region"), ("pvp", "PvP Arena")],
                "addmember": [("spawn", "Spawn Region"), ("warzone", "Warzone Region")],
                "removemember": [("spawn", "Spawn Region"), ("warzone", "Warzone Region")],
                "addowner": [("spawn", "Spawn Region"), ("warzone", "Warzone Region")],
                "removeowner": [("spawn", "Spawn Region"), ("warzone", "Warzone Region")]
            },
            {
                "flag": [("pvp", "Player vs Player damage"), ("mob-spawning", "Natural mob spawns"), ("creeper-explosion", "Creeper block damage"), ("entry", "Entry access permission"), ("farewell", "Farewell message"), ("greeting", "Greeting message"), ("sleep", "Allow sleeping"), ("tnt", "TNT block damage"), ("invincible", "Godmode flag"), ("blocked-cmds", "Blacklist commands"), ("allowed-cmds", "Whitelist commands")],
                "addmember": "<player>",
                "removemember": "<player>",
                "addowner": "<player>",
                "removeowner": "<player>"
            },
            [("allow", "Allow flag"), ("deny", "Deny flag"), ("none", "Reset flag")]
        ]
    },
    "region": {
        "syntax": "/region <remove|info|list|flag|addmember|removemember|reload>",
        "args": [
            [("remove", "Delete region"), ("info", "Region info"), ("list", "List regions"), ("flag", "Set flag"), ("addmember", "Add member"), ("reload", "Reload config")]
        ]
    },
    "worldguard": {
        "syntax": "/worldguard <reload|version|report>",
        "args": [
            [("reload", "Reload WorldGuard"), ("version", "Show version"), ("report", "Generate debug report")]
        ]
    },
    # --- Multiverse ---
    "mv": {
        "syntax": "/mv <list|tp|create|import|remove|delete|clone|setspawn|reload> [args...]",
        "args": [
            [("list", "List all loaded worlds"), ("tp", "Teleport player to world"), ("create", "Create a new world"), ("import", "Import world folder"), ("remove", "Unload world from memory"), ("delete", "Permanently delete world"), ("clone", "Duplicate world"), ("setspawn", "Set world spawn point"), ("reload", "Reload Multiverse")],
            {
                "tp": "<player>",
                "create": [("normal", "Standard Overworld"), ("nether", "Nether Dimension"), ("end", "The End Dimension")],
                "import": [("world", "Overworld"), ("world_nether", "Nether"), ("world_the_end", "The End")],
                "remove": [("world", "Overworld"), ("world_nether", "Nether"), ("world_the_end", "The End")],
                "delete": [("world_temp", "Temporary World")]
            },
            [("world", "Overworld"), ("world_nether", "Nether"), ("world_the_end", "The End")]
        ]
    },
    "multiverse": {
        "syntax": "/multiverse <list|tp|reload>",
        "args": [
            [("list", "List worlds"), ("tp", "Teleport player to world"), ("reload", "Reload config")]
        ]
    },
    # --- DecentHolograms ---
    "dh": {
        "syntax": "/dh <delete|line|list|reload> [args...]",
        "args": [
            [("delete", "Delete hologram"), ("line", "Manage hologram lines"), ("list", "List all holograms"), ("reload", "Reload DecentHolograms")],
            {
                "line": [("add", "Append new line"), ("set", "Replace line"), ("remove", "Delete line")],
                "delete": [("spawn_welcome", "Welcome Hologram"), ("leaderboard_level", "Level Leaderboard"), ("leaderboard_eco", "Balance Leaderboard")]
            }
        ]
    },
    "decentholograms": {
        "syntax": "/decentholograms <delete|list|reload>",
        "args": [
            [("delete", "Delete hologram"), ("list", "List holograms"), ("reload", "Reload")]
        ]
    },
    # --- Citizens ---
    "npc": {
        "syntax": "/npc <create|select|remove|list|skin|rename|reload> [args...]",
        "args": [
            [("create", "Create NPC"), ("select", "Select NPC by ID"), ("remove", "Delete selected NPC"), ("list", "List all server NPCs"), ("skin", "Change NPC skin"), ("rename", "Change NPC display name"), ("reload", "Reload Citizens")],
            {
                "skin": "<player>",
                "create": [("Steve", "Human NPC"), ("Banker", "Banker NPC"), ("Trader", "Royal Trader NPC"), ("Angler", "Fishing Master NPC")]
            }
        ]
    },
    # --- MythicMobs ---
    "mm": {
        "syntax": "/mm <mobs|items|skills|reload> [args...]",
        "args": [
            [("mobs", "Manage custom Mythic mobs"), ("items", "Manage custom Mythic items"), ("skills", "Test custom skills"), ("reload", "Reload all MythicMobs configs")],
            {
                "mobs": [("spawn", "Spawn custom mob at coords"), ("kill", "Kill specific mob"), ("killall", "Kill all active Mythic mobs"), ("list", "List registered mob types"), ("info", "Display mob details")],
                "items": [("give", "Give custom item to player"), ("list", "List registered custom items")]
            },
            {
                "give": "<player>",
                "spawn": [("1", "Spawn 1 mob"), ("3", "Spawn 3 mobs"), ("5", "Spawn 5 mobs")]
            }
        ]
    },
    "mythicmobs": {
        "syntax": "/mythicmobs <mobs|items|skills|reload>",
        "args": [
            [("mobs", "Manage mobs"), ("items", "Manage items"), ("skills", "Test skills"), ("reload", "Reload config")]
        ]
    },
    # --- TAB ---
    "tab": {
        "syntax": "/tab <player|group|reload|parse> [args...]",
        "args": [
            [("player", "Configure player TAB display"), ("group", "Configure group TAB display"), ("reload", "Reload TAB configuration"), ("parse", "Test placeholder parsing for player")],
            {
                "player": "<player>",
                "parse": "<player>",
                "group": [("ancestor", "Owner"), ("warden", "Admin"), ("herald", "Staff"), ("sions", "Donator"), ("wanderer", "Default")]
            },
            [("tabprefix", "Change tablist prefix"), ("tabsuffix", "Change tablist suffix"), ("tagprefix", "Change nametag prefix"), ("tagsuffix", "Change nametag suffix")]
        ]
    },
    # --- Chunky ---
    "chunky": {
        "syntax": "/chunky <start|pause|continue|cancel|radius|world|shape|progress>",
        "args": [
            [("start", "Start world pre-generation task"), ("pause", "Pause running pre-generation"), ("continue", "Resume paused pre-generation"), ("cancel", "Cancel and abort task"), ("radius", "Set block radius"), ("world", "Set target world"), ("shape", "Set generation shape"), ("progress", "Display generation percentage and ETA")],
            {
                "radius": [("1000", "1.000 blocks"), ("2500", "2.500 blocks"), ("5000", "5.000 blocks"), ("10000", "10.000 blocks")],
                "world": [("world", "Overworld"), ("world_nether", "Nether Dimension"), ("world_the_end", "The End Dimension")],
                "shape": [("circle", "Circular shape"), ("square", "Square boundary"), ("star", "Star shape")]
            }
        ]
    },
    # --- SkinsRestorer ---
    "skin": {
        "syntax": "/skin <set|clear|update|drop> <player> [skin]",
        "args": [
            [("set", "Set custom skin for player"), ("clear", "Clear custom skin"), ("update", "Force update skin from Mojang"), ("drop", "Drop skin database entries")],
            "<player>"
        ]
    },
    # --- BlueMap ---
    "bluemap": {
        "syntax": "/bluemap <render|pause|resume|status|freeze|unfreeze|reload> [world]",
        "args": [
            [("render", "Render map tiles for world"), ("pause", "Pause web map render"), ("resume", "Resume web map render"), ("status", "View render progress and queue"), ("freeze", "Freeze map tiles"), ("unfreeze", "Unfreeze map tiles"), ("reload", "Reload BlueMap config")],
            [("world", "Overworld"), ("world_nether", "Nether"), ("world_the_end", "The End")]
        ]
    },
    # --- AuthMe ---
    "authme": {
        "syntax": "/authme <register|unregister|changepassword|reload|purge> <player> [password]",
        "args": [
            [("register", "Manually register player account"), ("unregister", "Unregister player account"), ("changepassword", "Change player password"), ("reload", "Reload AuthMe configs"), ("purge", "Purge old inactive accounts")],
            "<player>"
        ]
    },
    # --- FastLogin ---
    "fastlogin": {
        "syntax": "/fastlogin <auto|premium|crack> <player>",
        "args": [
            [("auto", "Toggle auto login state"), ("premium", "Enable Mojang premium check"), ("crack", "Disable Mojang premium check")],
            "<player>"
        ]
    },
    # --- Geyser ---
    "geyser": {
        "syntax": "/geyser <reload|dump|version>",
        "args": [
            [("reload", "Reload Geyser Bedrock bridge"), ("dump", "Generate Geyser debug dump"), ("version", "Check Geyser build version")]
        ]
    },
    # --- ViaVersion ---
    "viaversion": {
        "syntax": "/viaversion <list|pps|dontbugme|dump>",
        "args": [
            [("list", "List online players with client protocols"), ("pps", "Check packets per second per player"), ("dontbugme", "Toggle update notifications"), ("dump", "Generate ViaVersion dump")]
        ]
    },
    # --- SimpleVoiceChat ---
    "voicechat": {
        "syntax": "/voicechat <test|reload> [player]",
        "args": [
            [("test", "Run voice chat connection test"), ("reload", "Reload voice chat configuration")],
            "<player>"
        ]
    },
    # --- ajLeaderboards ---
    "ajlb": {
        "syntax": "/ajlb <add|remove|list|reload> [args...]",
        "args": [
            [("add", "Register new leaderboard board"), ("remove", "Delete registered board"), ("list", "List all registered boards"), ("reload", "Reload ajLeaderboards config")]
        ]
    },
    # --- ApexsionsCore Console Admin ---
    "kingdom": {
        "syntax": "/kingdom <info|list|war> [args...]",
        "args": [
            [("info", "View kingdom status"), ("list", "List all civilizations"), ("war", "View war status")],
            {
                "war": [("status", "View active war scores & combat tag")]
            }
        ]
    },
    "war": {
        "syntax": "/war status [kingdom]",
        "args": [
            [("status", "View active war scores & combat tags")]
        ]
    },
    "rtp": {
        "syntax": "/rtp <player>",
        "args": ["<player>"]
    },
    "wild": {
        "syntax": "/wild <player>",
        "args": ["<player>"]
    },
    "level": {
        "syntax": "/level <set|add|reset|info> <player> [amount]",
        "args": [
            [("set", "Set player civilization level"), ("add", "Add civilization level"), ("reset", "Reset progression to Level 1"), ("info", "Check player level and perk stats")],
            "<player>",
            [("5", "Level 5"), ("10", "Level 10"), ("25", "Level 25"), ("50", "Level 50"), ("100", "Level 100 (Max)")]
        ]
    },
    "ranks": {
        "syntax": "/ranks [list|info]",
        "args": [
            [("list", "Display official server rank hierarchy"), ("info", "View perk details for each civilization tier")]
        ]
    },
    "vanish": {
        "syntax": "/vanish <player>",
        "args": ["<player>"]
    },
    # --- EssentialsX Console Targeting ---
    "heal": {
        "syntax": "/heal <player>",
        "args": ["<player>"]
    },
    "feed": {
        "syntax": "/feed <player>",
        "args": ["<player>"]
    },
    "god": {
        "syntax": "/god <player>",
        "args": ["<player>"]
    },
    "ungod": {
        "syntax": "/ungod <player>",
        "args": ["<player>"]
    },
    "fly": {
        "syntax": "/fly <player>",
        "args": ["<player>"]
    },
    "speed": {
        "syntax": "/speed <1-10> <player>",
        "args": [
            [("1", "Normal Speed (1)"), ("2", "Fast Speed (2)"), ("3", "Turbo Speed (3)"), ("5", "Hyper Speed (5)"), ("10", "Maximum Speed (10)")],
            "<player>"
        ]
    },
    "spawn": {
        "syntax": "/spawn <player>",
        "args": ["<player>"]
    },
    "warp": {
        "syntax": "/warp <name> <player>",
        "args": [
            [("spawn", "Main Spawn"), ("market", "Kingdom Market"), ("pvp", "Warzone Arena"), ("crates", "Crate Sanctuary"), ("fishing", "Royal Fishing Pond")],
            "<player>"
        ]
    },
    "delwarp": {
        "syntax": "/delwarp <name>",
        "args": [
            [("spawn", "Main Spawn"), ("market", "Kingdom Market"), ("pvp", "Warzone Arena")]
        ]
    },
    "broadcast": {
        "syntax": "/broadcast <message>",
        "args": []
    },
    "bc": {
        "syntax": "/bc <message>",
        "args": []
    },
    "sudo": {
        "syntax": "/sudo <player> <command>",
        "args": ["<player>"]
    },
    "whois": {
        "syntax": "/whois <player>",
        "args": ["<player>"]
    },
    "realname": {
        "syntax": "/realname <player>",
        "args": ["<player>"]
    },
    "seen": {
        "syntax": "/seen <player>",
        "args": ["<player>"]
    },
    "tempban": {
        "syntax": "/tempban <player> <time> [reason]",
        "args": [
            "<player>",
            [("1h", "1 Hour"), ("1d", "1 Day"), ("7d", "7 Days"), ("30d", "30 Days")]
        ]
    },
    "mute": {
        "syntax": "/mute <player> [time]",
        "args": [
            "<player>",
            [("10m", "10 Minutes"), ("1h", "1 Hour"), ("1d", "1 Day"), ("permanent", "Permanent Mute")]
        ]
    },
    "unmute": {
        "syntax": "/unmute <player>",
        "args": ["<player>"]
    },
    "jail": {
        "syntax": "/jail <player> <jail> [time]",
        "args": [
            "<player>",
            [("dungeon", "Royal Dungeon Jail"), ("cell1", "Solitary Cell 1")]
        ]
    },
    "unjail": {
        "syntax": "/unjail <player>",
        "args": ["<player>"]
    },
    "ptime": {
        "syntax": "/ptime <day|night|reset> <player>",
        "args": [
            [("day", "Lock player time to Day"), ("night", "Lock player time to Night"), ("reset", "Sync with server time")],
            "<player>"
        ]
    },
    "pweather": {
        "syntax": "/pweather <clear|rain|reset> <player>",
        "args": [
            [("clear", "Lock player weather to Clear"), ("rain", "Lock player weather to Rain"), ("reset", "Sync with server weather")],
            "<player>"
        ]
    },
    # --- Apexsions Economy Console ---
    "balance": {
        "syntax": "/balance <player>",
        "args": ["<player>"]
    },
    "bal": {
        "syntax": "/bal <player>",
        "args": ["<player>"]
    },
    # --- Apexsions Crates Console ---
    "key": {
        "syntax": "/key <give|take|set> <player> <crate> [amount]",
        "args": [
            [("give", "Give crate key"), ("take", "Take crate key"), ("set", "Set key balance")],
            "<player>",
            [("common", "Common Crate"), ("rare", "Rare Crate"), ("epic", "Epic Crate"), ("legendary", "Legendary Crate"), ("mythic", "Mythic Crate"), ("ancient", "Ancient Crate")],
            [("1", "1 Key"), ("3", "3 Keys"), ("5", "5 Keys"), ("10", "10 Keys")]
        ]
    },
    # --- Apexsions Media Console ---
    "banner": {
        "syntax": "/banner <reload|status>",
        "args": [
            [("reload", "Reload banner assets"), ("status", "View banner raytrace status")]
        ]
    },
    "logo": {
        "syntax": "/logo <reload|status>",
        "args": [
            [("reload", "Reload logo assets"), ("status", "View logo render status")]
        ]
    }
}
