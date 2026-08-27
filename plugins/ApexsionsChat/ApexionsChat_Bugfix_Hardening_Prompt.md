# ApexionsChat — Bugfix & Hardening Prompt

## Context

`ApexionsChat` has already been implemented from the previous specification.

**Do NOT rebuild the plugin from scratch.**

This is a bugfix, hardening, audit, and regression-testing pass over the existing implementation.

The currently observed defects are:

1. Players can still spam chat.
2. Profanity / inappropriate chat is still getting through.
3. `/showitem` leaks raw MiniMessage/component syntax such as:
   ```text
   (/white)
   ```
   or similar formatting tags into visible chat.
4. The chat pipeline may be processing or rendering user-generated strings incorrectly.

The previous architecture already requires ApexionsChat to own chat formatting/moderation while ApexionsCore remains the source of level, title, region, rank, and progression data. The previous specification also requires a modular moderation pipeline and Adventure Components. fileciteturn6file0L11-L33 fileciteturn6file2L443-L484

---

# 1. Main Instruction

**Inspect the existing ApexionsChat source code first.**

Do not blindly add another listener or another filter.

For each reported defect:

```text
Reproduce
↓
Trace the actual code path
↓
Identify the root cause
↓
Fix the root cause
↓
Add a regression test
↓
Build
↓
Run Paper
↓
Verify in-game
```

Do not claim a fix merely because compilation succeeds.

---

# 2. One Authoritative Chat Pipeline

There must be exactly one authoritative pipeline for normal player chat.

Target:

```text
Player Chat
    ↓
AsyncChatEvent / current Paper chat event
    ↓
Rate Limit
    ↓
Normalize inspection copy
    ↓
Spam detection
    ↓
Advertising detection
    ↓
Profanity detection
    ↓
Discriminatory / hate-related detection
    ↓
Caps / symbol checks
    ↓
If BLOCK → cancel and STOP
    ↓
Mention parsing
    ↓
Safe Adventure Component construction
    ↓
Chat formatting
    ↓
Broadcast
```

A blocked message must never reach rendering/broadcast.

Do not use this broken architecture:

```text
Message
 ↓
Broadcast
 ↓
Moderation afterwards
```

---

# 3. Fix Spam Properly

The current implementation allows players to spam.

Do not solve this with only one simplistic cooldown.

Implement layered protection.

## Rate limit

Track recent chat timestamps per player UUID.

Example configurable structure:

```yaml
moderation:
  spam:
    enabled: true

    rate-limit:
      messages: 4
      window-seconds: 5
      action: WARN

    mute:
      enabled: true
      duration-seconds: 10
```

Treat these as examples only. Inspect the existing configuration before changing values.

Use UUIDs, never player names, as the identity key.

---

# 4. Duplicate Message Detection

Prevent obvious repetition:

```text
hello
hello
hello
hello
```

Normalize a comparison copy:

```text
trim
lowercase
collapse whitespace
normalize obvious punctuation differences
```

Do not replace the player's actual display message with the normalized copy.

---

# 5. Near-Duplicate Spam

Prevent trivial bypasses such as:

```text
hello
hello!
hello!!
HELLO
h e l l o
```

Use a reasonable per-player similarity check against only a small recent-message buffer.

Keep the similarity threshold configurable.

Do not perform expensive comparisons across all players.

---

# 6. Spam Escalation

Use configurable escalation:

```text
first violation
    ↓
warning

repeated violations
    ↓
temporary mute

continued violations
    ↓
staff log/review
```

Do not permanently punish players automatically by default.

---

# 7. Fix Profanity Filtering

The current profanity protection is not reliably stopping inappropriate messages.

Inspect whether the cause is:

```text
filter never called
filter runs after broadcast
wrong chat event
wrong listener priority
case mismatch
Unicode mismatch
punctuation bypass
spacing bypass
obfuscation bypass
regex bug
allow-list bug
component/plain-text mismatch
```

Fix the actual cause rather than adding a second unrelated filter.

---

# 8. Normalization Layer

Create or reuse a dedicated normalization component.

Concept:

```text
Raw message
    ↓
Unicode-safe normalization
    ↓
lowercase
    ↓
whitespace normalization
    ↓
reasonable separator normalization
    ↓
moderation checks
```

Keep:

```text
originalMessage
normalizedMessage
```

separate.

The original should be used for display only after moderation succeeds.

---

# 9. Profanity Configuration

Keep the dictionary configurable:

```yaml
moderation:
  profanity:
    enabled: true
    action: BLOCK

    blocked-words:
      - "..."

    phrases:
      - "..."

    exceptions:
      - "..."

    normalize:
      lowercase: true
      collapse-whitespace: true
      remove-common-separators: true
```

Do not hardcode offensive/slur lists into the implementation.

The server owner supplies the appropriate dictionary.

---

# 10. Commands Are Not Chat

Do not run normal chat moderation against arbitrary command input.

Examples:

```text
/report ...
/mail ...
/showitem
```

are command handling, not ordinary chat.

Do not break legitimate command arguments by treating them as chat messages.

---

# 11. Critical `/showitem` Bug

### Current defect

`/showitem` can display raw syntax such as:

```text
(/white)
```

or other MiniMessage/component tags.

This indicates that a string intended to represent formatting is reaching the client as literal text.

Fix this at the Adventure Component boundary.

---

# 12. Never Send Raw MiniMessage as Plain Text

Incorrect:

```java
Component.text("<white>[Diamond Sword]</white>");
```

This sends the tags literally.

Also do NOT blindly do:

```java
MiniMessage.miniMessage().deserialize(userControlledString);
```

for arbitrary player/item-generated strings.

Clearly distinguish:

```text
Trusted server template
```

from:

```text
User-generated content
```

---

# 13. Rendering Rules

Use:

```text
Trusted MiniMessage template
    → MiniMessage deserialize
    → Adventure Component
```

For untrusted/user-generated content:

```text
User text
    → Component.text(...)
    → safe component
```

For item display components:

```text
Existing ItemStack component
    → preserve/convert safely
    → Adventure Component
```

Avoid unnecessary:

```text
Component
 → String
 → MiniMessage
 → Component
```

round-trips.

---

# 14. `/showitem` Expected Flow

Implement/verify:

```text
/showitem
    ↓
Read held item server-side
    ↓
Validate ItemStack
    ↓
Create immutable showcase snapshot/reference
    ↓
Build Adventure Component
    ↓
Attach hover event
    ↓
Attach internal click action
    ↓
Send chat
```

The resulting client-visible chat must contain **zero raw MiniMessage tags**.

No:

```text
<white>
<gray>
<bold>
<gradient:...>
```

should appear literally.

---

# 15. `/showitem` Item Name

If an item's name already contains rich formatting:

```text
Hex
Gradient
Bold
Custom item components
```

preserve it as a component.

Do not flatten it to a string and reparse it unless necessary.

If flattening is unavoidable, escape untrusted content correctly.

---

# 16. `/showitem` Click Security

Clicking the item showcase should open the existing showcase GUI.

Use an internal/opaque showcase identifier:

```text
chat click
    ↓
showcase ID
    ↓
server-side lookup
    ↓
open GUI
```

Do NOT construct executable commands from arbitrary player/item strings.

The viewer must not be able to:

```text
steal the item
duplicate the item
modify the original item
execute arbitrary commands
```

---

# 17. Audit Every MiniMessage Conversion

Search the whole ApexionsChat codebase for:

```text
MiniMessage
deserialize
serialize
Component.text
ClickEvent
HoverEvent
```

For every conversion, classify the input:

```text
TRUSTED_TEMPLATE
USER_GENERATED
ITEM_COMPONENT
SYSTEM_GENERATED
```

Rules:

```text
TRUSTED_TEMPLATE → MiniMessage allowed
USER_GENERATED    → safe component / escaping
ITEM_COMPONENT    → preserve safely
SYSTEM_GENERATED  → explicit component construction
```

Do not globally "fix" the bug by disabling MiniMessage.

---

# 18. Moderation + Formatting Order

Correct order:

```text
RAW CHAT
   ↓
Rate limit
   ↓
Normalize inspection copy
   ↓
Spam
   ↓
Advertising
   ↓
Profanity
   ↓
Discriminatory/hate content
   ↓
Caps/symbol checks
   ↓
BLOCK? → cancel immediately
   ↓
Mentions
   ↓
Safe component construction
   ↓
Chat format
   ↓
Broadcast
```

This is mandatory.

A blocked message must not:

```text
broadcast
trigger mentions
trigger @all
```

---

# 19. Global + Kingdom Chat

Both channels must use the same moderation engine.

There must not be a loophole where:

```text
Global → filtered
Kingdom → unfiltered
```

Kingdom Chat must continue to use the player's region from ApexionsCore rather than maintaining duplicate region data. fileciteturn6file0L213-L254

---

# 20. Mentions

Mentions happen only after moderation succeeds.

Correct:

```text
message
 ↓
moderation
 ↓
allowed
 ↓
mention parsing
 ↓
ActionBar notifications
```

A blocked message must never trigger:

```text
@Player
@all
```

Existing mention requirements include cooldown protection and duplicate-notification prevention. fileciteturn6file2L275-L345

---

# 21. Central ModerationResult

Use one structured moderation result rather than scattered booleans.

Concept:

```java
public record ModerationResult(
    boolean allowed,
    ModerationRule rule,
    ModerationAction action,
    String reason
) {}
```

Adapt this to existing project architecture instead of creating duplicate concepts.

Suggested rules:

```text
NONE
RATE_LIMIT
DUPLICATE_SPAM
SIMILARITY_SPAM
ADVERTISEMENT
PROFANITY
DISCRIMINATORY_CONTENT
EXCESSIVE_CAPS
EXCESSIVE_SYMBOLS
```

Suggested actions:

```text
ALLOW
WARN
BLOCK
TEMP_MUTE
LOG
```

---

# 22. Moderation Architecture

Prefer:

```text
moderation/
├── ModerationEngine
├── ModerationResult
├── RateLimitRule
├── DuplicateSpamRule
├── SimilaritySpamRule
├── AdvertisementRule
├── ProfanityRule
├── HateContentRule
├── CapsRule
├── SymbolSpamRule
└── ModerationAction
```

Do not create one enormous chat listener containing all moderation logic.

---

# 23. No Double Processing

Audit for:

```text
multiple AsyncChatEvent listeners
multiple chat listeners
Bukkit.broadcast
Audience.sendMessage
player.sendMessage
```

Verify that the same message is not:

```text
broadcast twice
filtered twice
rendered twice
sent once before moderation and again afterwards
```

There must be one authoritative delivery path.

---

# 24. Listener/Event Audit

Inspect which Paper chat event the current implementation uses.

Do not assume the event is correct.

Verify:

```text
event type
event priority
event cancellation
message retrieval
message modification
```

If another plugin modifies chat, document the interaction and choose the correct processing point.

---

# 25. Regression Tests

Add automated tests.

## Spam

```text
[ ] rapid messages trigger protection
[ ] cooldown works
[ ] duplicate messages trigger protection
[ ] near-duplicate messages trigger protection
[ ] players have independent limits
[ ] staff bypass works if configured
```

## Profanity

```text
[ ] configured blocked words are detected
[ ] capitalization does not bypass
[ ] common separator insertion does not bypass
[ ] exceptions work
[ ] clean messages pass
[ ] blocked messages are never broadcast
```

## Show Item

```text
[ ] /showitem renders correctly
[ ] no raw MiniMessage tags appear
[ ] no <white>, <gray>, <gradient:...> appears literally
[ ] hover works
[ ] click opens GUI
[ ] viewer cannot take item
[ ] malformed/custom item data does not crash chat
```

## Pipeline

```text
[ ] blocked message cannot broadcast
[ ] blocked message cannot trigger mentions
[ ] blocked message cannot trigger @all
[ ] global uses moderation
[ ] kingdom uses moderation
[ ] chat is not duplicated
```

---

# 26. Manual In-Game Test Matrix

Use at least two player accounts.

## Spam

Send several messages rapidly.

Expected:

```text
warning/block/mute according to configuration
```

## Duplicate

```text
hello
hello
hello
hello
```

Expected:

```text
anti-spam triggers
```

## Near duplicate

```text
hello
hello!
HELLO!!
h e l l o
```

Expected:

```text
reasonable anti-spam detection
```

## Profanity

Use the server's configured moderation dictionary.

Expected:

```text
blocked/replaced according to configuration
not visible to other players
```

## Show Item

```text
/showitem
```

Expected:

```text
formatted item
no raw syntax
hover works
click works
showcase GUI opens
```

---

# 27. Do Not "Fix" by Disabling Features

Do NOT fix the syntax issue by removing:

```text
Hex
Gradients
Hover
Click
Item components
```

Do NOT fix spam by making chat unusably slow.

Do NOT fix profanity by disabling chat.

Fix the implementation at its source.

---

# 28. Preserve Existing Configuration

Inspect existing configuration before editing it.

Do not blindly overwrite custom values.

When adding new keys:

```text
preserve existing settings
add missing keys
document them
provide sensible defaults
```

If migration is required, implement it safely.

---

# 29. Performance

Do not:

```text
query database for every chat message
make network requests for every message
compare messages against every player
perform expensive MiniMessage serialization repeatedly
```

Use small per-player in-memory buffers for spam detection.

Database logging must not block the Paper main thread.

---

# 30. Thread Safety

Respect Paper's threading model.

If the chat event is asynchronous:

```text
capture immutable data
perform safe/non-blocking processing
do not access unsafe Bukkit/Paper state asynchronously
switch to the correct thread when required
```

Database I/O must not block the main thread.

---

# 31. Acceptance Criteria

The task is NOT complete until:

```text
[ ] Rapid spam is actually blocked/warned
[ ] Duplicate spam is detected
[ ] Near-duplicate spam is detected
[ ] Configured profanity is actually blocked/replaced
[ ] Basic spacing/case/separator bypasses are handled
[ ] Blocked messages never broadcast
[ ] Blocked messages never trigger mentions
[ ] Blocked messages never trigger @all
[ ] /showitem no longer leaks raw MiniMessage syntax
[ ] No <white>, <gray>, <gradient:...> or similar tags appear literally
[ ] Item hover works
[ ] Item click works
[ ] Showcase GUI works
[ ] Viewer cannot steal the item
[ ] Global chat works
[ ] Kingdom chat works
[ ] Hex colors work
[ ] Gradients work
[ ] Rank/level/title/region formatting still works
[ ] No duplicate chat broadcasts
[ ] No duplicate moderation pipeline
[ ] Existing ApexionsCore functionality remains intact
[ ] Existing configuration is preserved
[ ] Regression tests pass
[ ] Paper 26.2 startup is clean
[ ] Manual in-game tests pass
```

---

# 32. Final Agent Report

After the work, report:

```text
## Root Causes

### Spam
- Exact cause
- Affected classes
- Affected event/listener

### Profanity
- Exact cause
- Affected classes
- Affected event/listener

### /showitem syntax leak
- Exact cause
- Affected component/rendering code

## Changes
- Files created
- Files modified
- Files removed/deprecated
- Configuration changes
- Database changes, if any

## Tests
- Automated tests
- Manual server tests
- Paper startup verification

## Remaining Issues
- Anything not fully verified
- Anything intentionally left unchanged
```

Do not simply report:

```text
"Fixed spam."
"Fixed profanity."
"Fixed showitem."
```

Explain the actual root cause and concrete correction.

---

# 33. Final Principle

This is a **hardening pass, not a rewrite**.

Preserve the existing ApexionsChat feature set.

Fix the broken implementation at its source.

The three currently confirmed defects are:

```text
1. Spam protection is not reliably stopping spam.
2. Profanity protection is not reliably blocking inappropriate messages.
3. /showitem leaks raw MiniMessage/component syntax into visible chat.
```

Treat each as a real software defect requiring:

```text
reproduction
→ root-cause analysis
→ code correction
→ regression test
→ Paper server verification
```
