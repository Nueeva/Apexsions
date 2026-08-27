# ApexionsCore — Rank System Change Request

## Context

The previous ApexionsCore implementation has already been completed and is currently working.

**Do NOT rebuild the plugin from scratch.**

This task is ONLY a change/refinement to the existing **Rank System**, specifically its rank definitions, LuckPerms integration, display metadata, default rank behavior, and related presentation.

All existing systems that are unrelated to ranks must remain intact.

---

# 1. Primary Objective

Update the existing ApexionsCore rank system to use the following exact rank hierarchy and display definitions:

| Internal ID | Display Name | Description | Color | Special |
|---|---|---|---|---|
| `ancestor` | The Ancestor | Owner / special rank | Dark Red | Bold |
| `warden` | Warden | Admin | Dark Blue | — |
| `herald` | Herald | Helper | Pink | — |
| `wanderer` | Wanderer | Default new-player rank | Gray | Default |
| `ascendant` | Ascendant | Normal progression rank | Light Green | — |
| `archon` | Archon | Normal progression rank | Cyan | — |
| `sovereign` | Sovereign | Normal progression rank | Gold | — |
| `emperor` | Emperor | Normal progression rank | Bright Red | — |
| `sions` | Sions | Highest normal rank | Aqua + Gold | Gradient |

The display names above are authoritative.

Do not invent additional ranks.

Do not rename these ranks.

---

# 2. Rank Hierarchy

The intended hierarchy is:

```text
The Ancestor
      │
    Warden
      │
    Herald
      │
    Sions
      │
   Emperor
      │
   Sovereign
      │
   Archon
      │
  Ascendant
      │
   Wanderer
```

However, distinguish carefully between:

1. **Display hierarchy**
2. **LuckPerms inheritance hierarchy**
3. **Permission hierarchy**

Do not automatically assume that every rank should inherit every permission from the rank below it unless the existing implementation already defines that behavior or the project configuration explicitly requires it.

The task is primarily to update rank definitions and presentation.

Do not accidentally grant administrative permissions to normal progression ranks.

---

# 3. LuckPerms Integration

LuckPerms remains the source of truth for player rank/group assignment.

ApexionsCore should continue using the LuckPerms API rather than maintaining a completely separate rank authority.

Required LuckPerms group IDs:

```text
ancestor
warden
herald
wanderer
ascendant
archon
sovereign
emperor
sions
```

Display names are separate from internal IDs.

Example:

```text
LuckPerms group:
ancestor

Display:
The Ancestor
```

---

# 4. Rank Provisioning

The existing automatic rank provisioning system must remain.

The server administrator should NOT need to manually create every group with commands such as:

```text
/lp creategroup ancestor
/lp creategroup warden
/lp creategroup herald
/lp creategroup wanderer
/lp creategroup ascendant
/lp creategroup archon
/lp creategroup sovereign
/lp creategroup emperor
/lp creategroup sions
```

ApexionsCore must automatically ensure these groups exist through the LuckPerms API.

---

# 5. Idempotent Provisioning

This requirement is mandatory.

Rank provisioning must be **idempotent**.

Running provisioning repeatedly must result in the same final state.

For example:

```text
Server start #1
    ↓
Create missing groups

Server restart #2
    ↓
Find existing groups
    ↓
Reuse them

Server restart #3
    ↓
Find existing groups
    ↓
Reuse them
```

Never create:

```text
wanderer2
wanderer3
wanderer_copy
sions2
```

or any other duplicate group.

The provisioning logic must:

```text
Find group by exact internal ID
        ↓
If exists:
    reuse it
        ↓
If missing:
    create it
```

Do not blindly create groups during every startup.

---

# 6. Existing LuckPerms Data Safety

This is an UPDATE to an existing server.

Do NOT:

```text
delete LuckPerms groups
delete player group assignments
reset permissions
reset player ranks
reset player data
```

Before modifying anything, inspect how the current implementation handles rank provisioning.

Preserve existing valid LuckPerms data.

Only modify what is necessary to implement the new rank definitions.

---

# 7. Rank Colors

## The Ancestor

```text
Display Name: The Ancestor
Color: Dark Red
Style: Bold
```

Example:

```text
#8B0000
```

## Warden

```text
Display Name: Warden
Color: Dark Blue
```

Example:

```text
#00008B
```

## Herald

```text
Display Name: Herald
Color: Pink
```

Example:

```text
#FF69B4
```

## Wanderer

```text
Display Name: Wanderer
Color: Gray
```

Example:

```text
#808080
```

This is the default rank for normal new players.

## Ascendant

```text
Display Name: Ascendant
Color: Light Green
```

Example:

```text
#90EE90
```

## Archon

```text
Display Name: Archon
Color: Cyan
```

Example:

```text
#00FFFF
```

## Sovereign

```text
Display Name: Sovereign
Color: Gold
```

Example:

```text
#FFD700
```

## Emperor

```text
Display Name: Emperor
Color: Bright Red
```

Example:

```text
#FF0000
```

## Sions

```text
Display Name: Sions
Color: Aqua → Gold
Style: Gradient
```

Example gradient:

```text
#00FFFF → #FFD700
```

Use the project's existing Adventure/MiniMessage/component rendering approach if available.

Do not introduce a new text-rendering system if one already exists.

---

# 8. Wanderer Default Rank

`Wanderer` remains the default rank for a normal player joining the server for the first time.

Expected behavior:

```text
First Join
    ↓
Check existing LuckPerms groups
    ↓
If player has no explicit rank
    ↓
Assign:
wanderer
```

However:

```text
Existing staff/admin/privileged rank
    ↓
DO NOT overwrite
```

Do not accidentally downgrade an existing:

```text
ancestor
warden
herald
```

player to `wanderer`.

---

# 9. The Ancestor

`The Ancestor` is a special owner rank.

It must remain protected.

Do not automatically give it to:

```text
all operators
all admins
all console users
all staff
```

unless the existing implementation explicitly does so.

Use the existing owner UUID/configuration mechanism.

If the project already has something similar to:

```yaml
owner:
  uuid: "..."
```

preserve it.

The owner should resolve to:

```text
ancestor
```

and display as:

```text
The Ancestor
```

with dark-red bold formatting.

Do not identify the owner only by username if UUID-based identification already exists.

---

# 10. Rank Resolution

Inspect the existing rank resolver.

The rank resolver should obtain the player's actual rank from LuckPerms.

Conceptually:

```text
Player
   ↓
LuckPerms
   ↓
Primary / configured group
   ↓
ApexionsCore RankDefinition
   ↓
Display metadata
```

Do not create a second conflicting source of truth.

For example, avoid:

```text
LuckPerms says:
warden

ApexionsCore database says:
wanderer
```

The system must have a deterministic resolution strategy.

---

# 11. Chat Integration

Existing chat functionality must continue working.

The existing chat format is:

```text
[Lv. X LevelTitle][Rank][Region] PlayerName » message
```

The rank component must now use the updated rank definitions.

Examples:

```text
[Lv. 1 Citizen][Wanderer][Region] Player » message
```

```text
[Lv. 37 ...][Sovereign][Region] Player » message
```

```text
[Lv. 100 ...][Sions][Region] Player » message
```

The rank number should NOT be added.

The display should use the rank's configured display name.

---

# 12. Name Tag / Placeholder Integration

Update all existing rank-dependent presentation systems to use the new rank definitions.

This includes any existing:

```text
NameTag
Chat
PlaceholderAPI
TAB integration
Scoreboard
GUI
```

only where the current implementation already consumes ApexionsCore rank data.

Do not modify TAB itself if TAB is intentionally managed separately.

ApexionsCore should expose correct rank information to integrations.

---

# 13. PlaceholderAPI

If the existing implementation exposes rank placeholders, make sure they resolve using the new definitions.

For example, if the project already has placeholders similar to:

```text
%apexionscore_rank%
%apexionscore_rank_name%
%apexionscore_rank_color%
```

ensure they return the updated values.

Do not invent unnecessary placeholders if the project does not already use them.

If a placeholder already exists, preserve backward compatibility.

---

# 14. Configuration

Update the existing configuration instead of introducing a second competing rank configuration.

Preferred structure:

```yaml
ranks:

  ancestor:
    display-name: "The Ancestor"
    color: "#8B0000"
    bold: true
    protected: true

  warden:
    display-name: "Warden"
    color: "#00008B"

  herald:
    display-name: "Herald"
    color: "#FF69B4"

  wanderer:
    display-name: "Wanderer"
    color: "#808080"
    default: true

  ascendant:
    display-name: "Ascendant"
    color: "#90EE90"

  archon:
    display-name: "Archon"
    color: "#00FFFF"

  sovereign:
    display-name: "Sovereign"
    color: "#FFD700"

  emperor:
    display-name: "Emperor"
    color: "#FF0000"

  sions:
    display-name: "Sions"
    gradient:
      enabled: true
      colors:
        - "#00FFFF"
        - "#FFD700"
```

Adapt the exact structure to the existing project's configuration architecture.

Do not duplicate configuration systems.

---

# 15. Compatibility With Existing Systems

This rank change must NOT break:

```text
XP
Level
Level Titles
Region
Region-specific Level Titles
Reward GUI
Reward Claiming
Player Persistence
/lobby
/region
Citizens integration
PlaceholderAPI integration
TAB integration
EssentialsX integration
Vault integration
```

Do not refactor unrelated systems.

If an unrelated bug is discovered, document it rather than expanding this task unnecessarily.

---

# 16. Migration From Previous Rank Definitions

Inspect the currently implemented rank system first.

If the previous rank names differ from the new definitions, determine whether a migration is necessary.

For example, if an old group was:

```text
old_rank
```

and the new system requires:

```text
new_rank
```

do NOT simply delete the old group.

Determine:

```text
Is the old group still used?
Are players assigned to it?
Does it contain permissions?
Does it have inheritance?
```

If migration is required:

```text
Old Rank
   ↓
Migration
   ↓
New Rank
```

Preserve player assignments and permissions where appropriate.

The migration must also be idempotent.

Running it multiple times must not repeatedly modify player data.

---

# 17. Do Not Touch These Systems

Unless a direct dependency requires a minimal change, leave these systems untouched:

```text
XP calculation
XP sources
Level progression
Level titles
Region selection
Region storage
Region teleportation
Reward calculations
Reward claiming
Reward GUI pagination
Milestone GUI positioning
/lobby
/region
Citizens behavior
Mini-games
TAB configuration
Database architecture
```

This task is a **rank-system update**, not a second complete plugin rewrite.

---

# 18. Validation

## LuckPerms

```text
[ ] ancestor exists
[ ] warden exists
[ ] herald exists
[ ] wanderer exists
[ ] ascendant exists
[ ] archon exists
[ ] sovereign exists
[ ] emperor exists
[ ] sions exists
```

## Provisioning

```text
[ ] Missing group is automatically created
[ ] Existing group is reused
[ ] Restart does not duplicate groups
[ ] Provisioning is idempotent
[ ] Existing player assignments are preserved
[ ] Existing permissions are not blindly erased
```

## Default Rank

```text
[ ] New normal player receives Wanderer
[ ] Existing staff rank is not overwritten
```

## Display

```text
[ ] The Ancestor = dark red + bold
[ ] Warden = dark blue
[ ] Herald = pink
[ ] Wanderer = gray
[ ] Ascendant = light green
[ ] Archon = cyan
[ ] Sovereign = gold
[ ] Emperor = bright red
[ ] Sions = aqua → gold gradient
```

## Existing Functionality

```text
[ ] Chat still works
[ ] NameTag integration still works
[ ] PlaceholderAPI integration still works
[ ] TAB integration still works
[ ] Region system still works
[ ] Level system still works
[ ] Reward GUI still works
```

---

# 19. Build and Runtime Test

After modifying the code:

```text
clean build
↓
resolve compilation errors
↓
verify Paper 26.2 compatibility
↓
start server
↓
inspect startup logs
↓
verify LuckPerms provisioning
↓
test first join
↓
test each rank
↓
test chat formatting
↓
test rank presentation
↓
restart server
↓
verify idempotency
```

Do not claim success based only on compilation.

---

# 20. Final Report

After implementation, report:

```text
1. Rank changes made
2. LuckPerms changes
3. Provisioning behavior
4. Migration performed, if any
5. Configuration changes
6. Files modified
7. Files created
8. Tests performed
9. Compatibility concerns
10. Remaining issues
```

Most importantly:

**Do not rebuild ApexionsCore.**

This is an incremental change to the already implemented plugin, specifically to the Rank System and its LuckPerms integration.
