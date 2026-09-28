#!/usr/bin/env python3
import sys

NEW_COMMAND_TREES = {
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
        "syntax": "/rg <define|claim|redefine|remove|info|list|flag|addmember|removemember|addowner|removeowner|reload> [args...]",
        "args": [
            [("define", "Define region from WE selection"), ("claim", "Claim WE selection"), ("redefine", "Update region bounds"), ("remove", "Delete region"), ("info", "Display region info"), ("list", "List all regions"), ("flag", "Set or remove region flag"), ("addmember", "Add member to region"), ("removemember", "Remove member"), ("addowner", "Add owner to region"), ("removeowner", "Remove owner"), ("reload", "Reload WorldGuard")],
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
        "syntax": "/region <define|claim|remove|info|list|flag|addmember|removemember|reload>",
        "args": [
            [("define", "Define region"), ("claim", "Claim selection"), ("remove", "Delete region"), ("info", "Region info"), ("list", "List regions"), ("flag", "Set flag"), ("addmember", "Add member"), ("reload", "Reload config")]
        ]
    },
    "worldguard": {
        "syntax": "/worldguard <reload|version|report>",
        "args": [
            [("reload", "Reload WorldGuard"), ("version", "Show version"), ("report", "Generate debug report")]
        ]
    },
    # --- Multiverse-Core & Portals ---
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
            [("list", "List worlds"), ("tp", "Teleport to world"), ("reload", "Reload config")]
        ]
    },
    "mvp": {
        "syntax": "/mvp <create|select|link|remove|list|reload> [name]",
        "args": [
            [("create", "Create portal from selection"), ("select", "Select portal"), ("link", "Link portal destination"), ("remove", "Delete portal"), ("list", "List all portals"), ("reload", "Reload Portals")]
        ]
    },
    # --- DecentHolograms ---
    "dh": {
        "syntax": "/dh <create|delete|edit|line|move|teleport|list|reload> [args...]",
        "args": [
            [("create", "Create new hologram"), ("delete", "Delete hologram"), ("edit", "Interactive editor"), ("line", "Manage hologram lines"), ("move", "Move hologram here"), ("teleport", "Teleport to hologram"), ("list", "List all holograms"), ("reload", "Reload DecentHolograms")],
            {
                "line": [("add", "Append new line"), ("set", "Replace line"), ("remove", "Delete line"), ("insert", "Insert line")],
                "delete": [("spawn_welcome", "Welcome Hologram"), ("leaderboard_level", "Level Leaderboard"), ("leaderboard_eco", "Balance Leaderboard")],
                "teleport": [("spawn_welcome", "Welcome Hologram"), ("leaderboard_level", "Level Leaderboard"), ("leaderboard_eco", "Balance Leaderboard")]
            }
        ]
    },
    "decentholograms": {
        "syntax": "/decentholograms <create|delete|list|reload>",
        "args": [
            [("create", "Create hologram"), ("delete", "Delete hologram"), ("list", "List holograms"), ("reload", "Reload")]
        ]
    },
    # --- Citizens ---
    "npc": {
        "syntax": "/npc <create|select|remove|list|tp|moveto|path|lookclose|skin|rename|text|equip> [args...]",
        "args": [
            [("create", "Create NPC at position"), ("select", "Select NPC by ID"), ("remove", "Delete selected NPC"), ("list", "List all NPCs"), ("tp", "Teleport to selected NPC"), ("moveto", "Move NPC to coordinates"), ("path", "Record waypoint path"), ("lookclose", "Toggle player tracking"), ("skin", "Change NPC skin"), ("rename", "Change NPC name"), ("text", "Configure dialogue"), ("equip", "Equip armor/hand items")],
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
                "mobs": [("spawn", "Spawn custom mob"), ("kill", "Kill specific mob"), ("killall", "Kill all active Mythic mobs"), ("list", "List mob types"), ("info", "Display mob details")],
                "items": [("give", "Give custom item to player"), ("get", "Get custom item in hand"), ("list", "List custom items"), ("import", "Import item in hand")]
            },
            {
                "spawn": [("1", "Spawn 1 mob"), ("3", "Spawn 3 mobs"), ("5", "Spawn 5 mobs")],
                "give": "<player>"
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
            [("player", "Configure player TAB display"), ("group", "Configure group TAB display"), ("reload", "Reload TAB configuration"), ("parse", "Test placeholder parsing")],
            {
                "player": "<player>",
                "parse": "<player>"
            },
            [("tabprefix", "Change tablist prefix"), ("tabsuffix", "Change tablist suffix"), ("tagprefix", "Change nametag prefix"), ("tagsuffix", "Change nametag suffix"), ("customtabname", "Change custom tabname")]
        ]
    },
    # --- Chunky ---
    "chunky": {
        "syntax": "/chunky <start|pause|continue|cancel|radius|world|shape|progress>",
        "args": [
            [("start", "Start world pre-generation task"), ("pause", "Pause pre-generation"), ("continue", "Resume pre-generation"), ("cancel", "Cancel and abort task"), ("radius", "Set block radius"), ("world", "Set target world"), ("shape", "Set generation shape"), ("progress", "Display progress and ETA")],
            {
                "radius": [("1000", "1.000 blocks"), ("2500", "2.500 blocks"), ("5000", "5.000 blocks"), ("10000", "10.000 blocks")],
                "world": [("world", "Overworld"), ("world_nether", "Nether Dimension"), ("world_the_end", "The End Dimension")],
                "shape": [("circle", "Circular shape"), ("square", "Square boundary"), ("star", "Star shape")]
            }
        ]
    },
    # --- SkinsRestorer ---
    "skin": {
        "syntax": "/skin <set|clear|update|drop> [player] [skin]",
        "args": [
            [("set", "Set custom skin for player"), ("clear", "Clear custom skin"), ("update", "Force update skin from Mojang"), ("drop", "Drop skin database entries")],
            "<player>"
        ]
    },
    "skins": {
        "syntax": "/skins <menu|set|clear>",
        "args": [
            [("menu", "Open interactive skins GUI"), ("set", "Set skin"), ("clear", "Clear skin")]
        ]
    },
    # --- BlueMap ---
    "bluemap": {
        "syntax": "/bluemap <render|pause|resume|status|freeze|unfreeze|reload> [world]",
        "args": [
            [("render", "Render map tiles for world"), ("pause", "Pause web map render"), ("resume", "Resume web map render"), ("status", "View render progress"), ("freeze", "Freeze map tiles"), ("unfreeze", "Unfreeze map tiles"), ("reload", "Reload BlueMap config")],
            [("world", "Overworld"), ("world_nether", "Nether"), ("world_the_end", "The End")]
        ]
    },
    # --- AuthMe ---
    "authme": {
        "syntax": "/authme <register|unregister|changepassword|reload|purge> [args...]",
        "args": [
            [("register", "Manually register player"), ("unregister", "Unregister player account"), ("changepassword", "Change player password"), ("reload", "Reload AuthMe configs"), ("purge", "Purge old inactive accounts")],
            "<player>"
        ]
    },
    # --- FastLogin ---
    "fastlogin": {
        "syntax": "/fastlogin <auto|premium|crack>",
        "args": [
            [("auto", "Toggle auto login state"), ("premium", "Enable Mojang premium check"), ("crack", "Disable Mojang premium check")]
        ]
    },
    # --- Geyser ---
    "geyser": {
        "syntax": "/geyser <reload|dump|version|offhand>",
        "args": [
            [("reload", "Reload Geyser Bedrock bridge"), ("dump", "Generate debug dump"), ("version", "Check Geyser build version"), ("offhand", "Toggle offhand swap key for Bedrock")]
        ]
    },
    # --- ViaVersion ---
    "viaversion": {
        "syntax": "/viaversion <list|pps|dontbugme|dump>",
        "args": [
            [("list", "List online player protocols"), ("pps", "Check packets per second"), ("dontbugme", "Toggle update notifications"), ("dump", "Generate ViaVersion dump")]
        ]
    },
    # --- SimpleVoiceChat ---
    "voicechat": {
        "syntax": "/voicechat <test|reload>",
        "args": [
            [("test", "Run voice chat connection test"), ("reload", "Reload voice chat configuration")]
        ]
    },
    # --- ajLeaderboards ---
    "ajlb": {
        "syntax": "/ajlb <add|remove|list|reload> [args...]",
        "args": [
            [("add", "Register new leaderboard board"), ("remove", "Delete registered board"), ("list", "List all registered boards"), ("reload", "Reload ajLeaderboards config")]
        ]
    },
    # --- ApexsionsCore Extended ---
    "kingdom": {
        "syntax": "/kingdom <create|invite|join|leave|kick|promote|demote|deposit|withdraw|claim|unclaim|war|info|list|map> [args...]",
        "args": [
            [("create", "Found a new kingdom"), ("invite", "Invite player to kingdom"), ("join", "Accept kingdom invitation"), ("leave", "Leave current kingdom"), ("kick", "Expel member"), ("promote", "Promote member rank"), ("demote", "Demote member rank"), ("deposit", "Deposit funds to kingdom vault"), ("withdraw", "Withdraw funds from vault"), ("claim", "Claim current chunk for kingdom"), ("unclaim", "Unclaim current chunk"), ("war", "Kingdom war operations"), ("info", "View kingdom status"), ("list", "List all civilizations"), ("map", "Open kingdom territorial map")],
            {
                "invite": "<player>",
                "kick": "<player>",
                "promote": "<player>",
                "demote": "<player>",
                "deposit": [("10000", "Rp 10.000"), ("50000", "Rp 50.000"), ("100000", "Rp 100.000"), ("500000", "Rp 500.000")],
                "withdraw": [("10000", "Rp 10.000"), ("50000", "Rp 50.000"), ("100000", "Rp 100.000")],
                "war": [("declare", "Declare war on kingdom"), ("accept", "Accept war challenge"), ("surrender", "Surrender war"), ("status", "View war status")]
            }
        ]
    },
    "k": {
        "syntax": "/k <create|invite|join|leave|kick|promote|demote|deposit|withdraw|claim|unclaim|war|info|list|map>",
        "args": [
            [("create", "Found kingdom"), ("invite", "Invite player"), ("join", "Join kingdom"), ("leave", "Leave kingdom"), ("kick", "Expel member"), ("promote", "Promote"), ("demote", "Demote"), ("deposit", "Deposit funds"), ("withdraw", "Withdraw funds"), ("claim", "Claim chunk"), ("unclaim", "Unclaim chunk"), ("war", "War operations"), ("info", "Kingdom info"), ("list", "List kingdoms"), ("map", "Territorial map")]
        ]
    },
    "war": {
        "syntax": "/war <declare|accept|deny|surrender|status|truce> [kingdom]",
        "args": [
            [("declare", "Declare war against civilization"), ("accept", "Accept war declaration"), ("deny", "Decline war challenge"), ("surrender", "Surrender active war"), ("status", "View current war scores & combat tag"), ("truce", "Propose peace treaty")]
        ]
    },
    "rtp": {
        "syntax": "/rtp [player]",
        "args": ["<player>"]
    },
    "wild": {
        "syntax": "/wild [player]",
        "args": ["<player>"]
    },
    "level": {
        "syntax": "/level <set|add|reset|info> [player] [amount]",
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
    "masteradmin": {
        "syntax": "/masteradmin [player]",
        "args": ["<player>"]
    },
    "aadmin": {
        "syntax": "/aadmin [player]",
        "args": ["<player>"]
    },
    "admin": {
        "syntax": "/admin [player]",
        "args": ["<player>"]
    },
    "vanish": {
        "syntax": "/vanish [player]",
        "args": ["<player>"]
    },
    "v": {
        "syntax": "/v [player]",
        "args": ["<player>"]
    },
    # --- EssentialsX Admin & Player Commands ---
    "tpa": {
        "syntax": "/tpa <player>",
        "args": ["<player>"]
    },
    "tpahere": {
        "syntax": "/tpahere <player>",
        "args": ["<player>"]
    },
    "tphere": {
        "syntax": "/tphere <player>",
        "args": ["<player>"]
    },
    "tpall": {
        "syntax": "/tpall",
        "args": []
    },
    "tpaccept": {
        "syntax": "/tpaccept",
        "args": []
    },
    "tpdeny": {
        "syntax": "/tpdeny",
        "args": []
    },
    "heal": {
        "syntax": "/heal [player]",
        "args": ["<player>"]
    },
    "feed": {
        "syntax": "/feed [player]",
        "args": ["<player>"]
    },
    "god": {
        "syntax": "/god [player]",
        "args": ["<player>"]
    },
    "ungod": {
        "syntax": "/ungod [player]",
        "args": ["<player>"]
    },
    "fly": {
        "syntax": "/fly [player]",
        "args": ["<player>"]
    },
    "speed": {
        "syntax": "/speed <1-10> [player]",
        "args": [
            [("1", "Normal Speed (1)"), ("2", "Fast Speed (2)"), ("3", "Turbo Speed (3)"), ("5", "Hyper Speed (5)"), ("10", "Maximum Speed (10)")],
            "<player>"
        ]
    },
    "hat": {
        "syntax": "/hat",
        "args": []
    },
    "setwarp": {
        "syntax": "/setwarp <name>",
        "args": [
            [("spawn", "Main Spawn"), ("market", "Kingdom Market"), ("pvp", "Warzone Arena"), ("crates", "Crate Sanctuary"), ("fishing", "Royal Fishing Pond")]
        ]
    },
    "warp": {
        "syntax": "/warp <name> [player]",
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
    "warps": {
        "syntax": "/warps",
        "args": []
    },
    "sethome": {
        "syntax": "/sethome [name]",
        "args": [
            [("home", "Default Home"), ("base", "Base Camp"), ("farm", "Resource Farm")]
        ]
    },
    "home": {
        "syntax": "/home [name]",
        "args": [
            [("home", "Default Home"), ("base", "Base Camp"), ("farm", "Resource Farm")]
        ]
    },
    "delhome": {
        "syntax": "/delhome [name]",
        "args": [
            [("home", "Default Home"), ("base", "Base Camp"), ("farm", "Resource Farm")]
        ]
    },
    "homes": {
        "syntax": "/homes [player]",
        "args": ["<player>"]
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
    "repair": {
        "syntax": "/repair <hand|all>",
        "args": [
            [("hand", "Repair item in hand"), ("all", "Repair entire inventory and armor")]
        ]
    },
    "fix": {
        "syntax": "/fix <hand|all>",
        "args": [
            [("hand", "Repair item in hand"), ("all", "Repair entire inventory")]
        ]
    },
    "invsee": {
        "syntax": "/invsee <player>",
        "args": ["<player>"]
    },
    "enderchest": {
        "syntax": "/enderchest [player]",
        "args": ["<player>"]
    },
    "ec": {
        "syntax": "/ec [player]",
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
        "syntax": "/ptime <day|night|reset> [player]",
        "args": [
            [("day", "Lock client time to Day"), ("night", "Lock client time to Night"), ("reset", "Sync with server time")],
            "<player>"
        ]
    },
    "pweather": {
        "syntax": "/pweather <clear|rain|reset> [player]",
        "args": [
            [("clear", "Lock client weather to Clear"), ("rain", "Lock client weather to Rain"), ("reset", "Sync with server weather")],
            "<player>"
        ]
    },
    # --- ApexsionsEconomy Extended ---
    "bal": {
        "syntax": "/bal [player]",
        "args": ["<player>"]
    },
    "balance": {
        "syntax": "/balance [player]",
        "args": ["<player>"]
    },
    "pay": {
        "syntax": "/pay <player> <amount>",
        "args": [
            "<player>",
            [("10000", "Rp 10.000"), ("50000", "Rp 50.000"), ("100000", "Rp 100.000"), ("500000", "Rp 500.000"), ("1000000", "Rp 1.000.000")]
        ]
    },
    "ah": {
        "syntax": "/ah [search|sell|expired] [price]",
        "args": [
            [("search", "Search auction house"), ("sell", "List item in hand on AH"), ("expired", "View expired items collect GUI")]
        ]
    },
    "auction": {
        "syntax": "/auction [search|sell|expired]",
        "args": [
            [("search", "Search auctions"), ("sell", "List item"), ("expired", "Expired items")]
        ]
    },
    "trade": {
        "syntax": "/trade <player>",
        "args": ["<player>"]
    },
    "barter": {
        "syntax": "/barter <player>",
        "args": ["<player>"]
    },
    # --- ApexsionsFishing Extended ---
    "fish": {
        "syntax": "/fish <vault|market|stats|menu> [player]",
        "args": [
            [("vault", "Open fishing vault"), ("market", "Open fish delivery market"), ("stats", "Show angler stats"), ("menu", "Open fishing hub")]
        ]
    },
    "vault": {
        "syntax": "/vault [player]",
        "args": ["<player>"]
    },
    # --- ApexsionsCrates Extended ---
    "crate": {
        "syntax": "/crate <open|preview|key|reload>",
        "args": [
            [("open", "Open crate"), ("preview", "Preview crate loot"), ("key", "Key balance"), ("reload", "Reload")]
        ]
    },
    "key": {
        "syntax": "/key <give|take|set> <player> <crate> [amount]",
        "args": [
            [("give", "Give crate key"), ("take", "Take crate key"), ("set", "Set key balance")],
            "<player>",
            [("common", "Common Crate"), ("rare", "Rare Crate"), ("epic", "Epic Crate"), ("legendary", "Legendary Crate"), ("mythic", "Mythic Crate"), ("ancient", "Ancient Crate")],
            [("1", "1 Key"), ("3", "3 Keys"), ("5", "5 Keys"), ("10", "10 Keys")]
        ]
    },
    # --- ApexsionsCustomEnchants Extended ---
    "ce": {
        "syntax": "/ce <enchanter|tinkerer|give|reload> [player]",
        "args": [
            [("enchanter", "Open enchanter"), ("tinkerer", "Open tinkerer"), ("give", "Give custom book"), ("reload", "Reload")]
        ]
    },
    "enchanter": {
        "syntax": "/enchanter [player]",
        "args": ["<player>"]
    },
    "tinkerer": {
        "syntax": "/tinkerer [player]",
        "args": ["<player>"]
    },
    "ace": {
        "syntax": "/ace <menu|give|tinkerer|reload> [player]",
        "args": [
            [("menu", "Open Admin Enchant Hub"), ("give", "Give custom enchant book"), ("tinkerer", "Open Tinkerer GUI"), ("reload", "Reload custom enchants")]
        ]
    },
    # --- ApexsionsBattlepass Extended ---
    "bp": {
        "syntax": "/bp <menu|quests|setlevel|addxp|reload> [player] [amount]",
        "args": [
            [("menu", "Open pass menu"), ("quests", "View quests"), ("setlevel", "Set level"), ("addxp", "Add XP"), ("reload", "Reload")]
        ]
    },
    "quests": {
        "syntax": "/quests [player]",
        "args": ["<player>"]
    },
    "abp": {
        "syntax": "/abp <menu|setlevel|addxp|reset|reload> [player] [amount]",
        "args": [
            [("menu", "Admin BattlePass Menu"), ("setlevel", "Set player pass level"), ("addxp", "Add pass XP"), ("reset", "Reset progression"), ("reload", "Reload BattlePass config")]
        ]
    },
    # --- ApexsionsShop Extended ---
    "sell": {
        "syntax": "/sell <all|hand|gui>",
        "args": [
            [("all", "Sell entire inventory"), ("hand", "Sell item in hand"), ("gui", "Open interactive sell GUI")]
        ]
    },
    "market": {
        "syntax": "/market [trends|events]",
        "args": [
            [("trends", "View market supply/demand trends"), ("events", "View dynamic royal trade events")]
        ]
    },
    # --- ApexsionsChat Extended ---
    "mail": {
        "syntax": "/mail <send|read|clear> [player] [message]",
        "args": [
            [("send", "Send offline mail to player"), ("read", "Read inbox mail messages"), ("clear", "Clear inbox mail")],
            "<player>"
        ]
    },
    "report": {
        "syntax": "/report <player> <reason>",
        "args": [
            "<player>",
            [("Hacking / Cheating", "Cheating report"), ("Griefing / Stealing", "Griefing report"), ("Harassment / Chat Toxicity", "Toxicity report"), ("Bug Abuse / Duplication", "Exploit report")]
        ]
    },
    "reports": {
        "syntax": "/reports [list|view|clear]",
        "args": [
            [("list", "Open Staff Reports Desk"), ("view", "Inspect open reports"), ("clear", "Purge resolved reports")]
        ]
    },
    "profile": {
        "syntax": "/profile [player]",
        "args": ["<player>"]
    },
    # --- ApexsionsMedia Extended ---
    "banner": {
        "syntax": "/banner <spawn|remove|teleport|reload>",
        "args": [
            [("spawn", "Spawn interactive banner"), ("remove", "Remove banner"), ("teleport", "Teleport to banner"), ("reload", "Reload banner")]
        ]
    },
    "logo": {
        "syntax": "/logo <render|clear|reload>",
        "args": [
            [("render", "Render interactive floating logo"), ("clear", "Clear logo"), ("reload", "Reload logo assets")]
        ]
    },
    # --- WorldEdit ---
    "worldedit": {
        "syntax": "/worldedit <version|reload|cui>",
        "args": [
            [("version", "Show WorldEdit version"), ("reload", "Reload WorldEdit configuration"), ("cui", "Toggle CUI handshake")]
        ]
    },
    "wand": {
        "syntax": "//wand",
        "args": []
    },
    "set": {
        "syntax": "//set <block>",
        "args": [
            [("stone", "Stone Block"), ("dirt", "Dirt Block"), ("grass_block", "Grass Block"), ("glass", "Glass Block"), ("air", "Air (Clear)"), ("diamond_block", "Diamond Block"), ("iron_block", "Iron Block"), ("gold_block", "Gold Block"), ("netherite_block", "Netherite Block"), ("oak_planks", "Oak Planks")]
        ]
    },
    "replace": {
        "syntax": "//replace [from_block] <to_block>",
        "args": [
            [("air", "Air"), ("stone", "Stone"), ("dirt", "Dirt"), ("water", "Water"), ("lava", "Lava")],
            [("stone", "Stone"), ("glass", "Glass"), ("air", "Air"), ("dirt", "Dirt")]
        ]
    },
    "copy": {
        "syntax": "//copy",
        "args": []
    },
    "paste": {
        "syntax": "//paste [-a] [-o]",
        "args": [
            [("-a", "Ignore air blocks"), ("-o", "Paste at original location")]
        ]
    },
    "cut": {
        "syntax": "//cut",
        "args": []
    },
    "undo": {
        "syntax": "//undo [steps]",
        "args": [
            [("1", "Undo 1 step"), ("2", "Undo 2 steps"), ("5", "Undo 5 steps")]
        ]
    },
    "redo": {
        "syntax": "//redo [steps]",
        "args": [
            [("1", "Redo 1 step"), ("2", "Redo 2 steps"), ("5", "Redo 5 steps")]
        ]
    },
    "clearhistory": {
        "syntax": "//clearhistory",
        "args": []
    },
    "pos1": {
        "syntax": "//pos1 [x,y,z]",
        "args": []
    },
    "pos2": {
        "syntax": "//pos2 [x,y,z]",
        "args": []
    },
    "hpos1": {
        "syntax": "//hpos1",
        "args": []
    },
    "hpos2": {
        "syntax": "//hpos2",
        "args": []
    },
    "expand": {
        "syntax": "//expand <amount> [direction]",
        "args": [
            [("5", "5 blocks"), ("10", "10 blocks"), ("20", "20 blocks"), ("50", "50 blocks")],
            [("up", "Expand Upwards"), ("down", "Expand Downwards"), ("north", "North"), ("south", "South"), ("east", "East"), ("west", "West")]
        ]
    },
    "contract": {
        "syntax": "//contract <amount> [direction]",
        "args": [
            [("5", "5 blocks"), ("10", "10 blocks"), ("20", "20 blocks")],
            [("up", "Contract Upwards"), ("down", "Contract Downwards"), ("north", "North"), ("south", "South"), ("east", "East"), ("west", "West")]
        ]
    },
    "sphere": {
        "syntax": "//sphere <block> <radius>",
        "args": [
            [("stone", "Stone"), ("glass", "Glass"), ("glowstone", "Glowstone")],
            [("3", "Radius 3"), ("5", "Radius 5"), ("10", "Radius 10"), ("15", "Radius 15")]
        ]
    },
    "hsphere": {
        "syntax": "//hsphere <block> <radius>",
        "args": [
            [("glass", "Glass"), ("stone", "Stone")],
            [("3", "Radius 3"), ("5", "Radius 5"), ("10", "Radius 10")]
        ]
    },
    "cyl": {
        "syntax": "//cyl <block> <radius> [height]",
        "args": [
            [("stone", "Stone"), ("glass", "Glass"), ("quartz_block", "Quartz")],
            [("3", "Radius 3"), ("5", "Radius 5"), ("10", "Radius 10")],
            [("1", "Height 1"), ("5", "Height 5"), ("10", "Height 10")]
        ]
    },
    "hcyl": {
        "syntax": "//hcyl <block> <radius> [height]",
        "args": [
            [("glass", "Glass"), ("stone", "Stone")],
            [("3", "Radius 3"), ("5", "Radius 5"), ("10", "Radius 10")]
        ]
    },
    "pyramid": {
        "syntax": "//pyramid <block> <size>",
        "args": [
            [("sandstone", "Sandstone"), ("stone", "Stone"), ("gold_block", "Gold")],
            [("5", "Size 5"), ("10", "Size 10"), ("20", "Size 20")]
        ]
    },
    "schem": {
        "syntax": "//schem <load|save|list> [name]",
        "args": [
            [("load", "Load schematic from disk"), ("save", "Save clipboard as schematic"), ("list", "List all saved schematics")]
        ]
    },
    "schematic": {
        "syntax": "//schematic <load|save|list> [name]",
        "args": [
            [("load", "Load schematic"), ("save", "Save schematic"), ("list", "List schematics")]
        ]
    }
}

print(f"Validated {len(NEW_COMMAND_TREES)} new command trees successfully!")
