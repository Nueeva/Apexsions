package com.apexsions.chat.nick;

import com.apexsions.chat.ApexsionsChatPlugin;
import com.apexsions.chat.database.NicknameRepository;
import com.apexsions.chat.moderation.ModerationResult;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Core Service managing player nicknames, token transactions, style selection, and physical vouchers.
 */
public class NicknameService {

    private static final Pattern SAFE_NICK_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");

    private final ApexsionsChatPlugin plugin;
    private final NicknameRepository repository;
    private final NamespacedKey tokenKey;
    private final Map<UUID, NicknameData> cache = new ConcurrentHashMap<>();
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public NicknameService(ApexsionsChatPlugin plugin, NicknameRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
        this.tokenKey = new NamespacedKey(plugin, "nick_token");
    }

    public CompletableFuture<NicknameData> loadPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        return repository.load(uuid).thenApply(opt -> {
            NicknameData data = opt.orElseGet(() -> NicknameData.createDefault(uuid, player.getName()));
            data.setPlayerName(player.getName());
            cache.put(uuid, data);
            Bukkit.getScheduler().runTask(plugin, () -> applyToPlayer(player, data));
            return data;
        });
    }

    public void unloadPlayer(UUID uuid) {
        NicknameData data = cache.remove(uuid);
        if (data != null) {
            repository.save(data);
        }
    }

    public NicknameData getNicknameData(UUID uuid) {
        return cache.computeIfAbsent(uuid, id -> {
            Player p = Bukkit.getPlayer(id);
            return NicknameData.createDefault(id, p != null ? p.getName() : "Unknown");
        });
    }

    public int getTokens(UUID uuid) {
        return getNicknameData(uuid).getTokens();
    }

    public void addTokens(UUID uuid, String playerName, int amount) {
        NicknameData data = getNicknameData(uuid);
        if (playerName != null) data.setPlayerName(playerName);
        data.addTokens(amount);
        repository.save(data);
    }

    /**
     * Creates a physical Name Tag voucher that grants rename tokens when right-clicked.
     */
    public ItemStack createTokenItem(int amount) {
        amount = Math.max(1, amount);
        ItemStack item = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize("<gradient:#ffeaa7:#55efc4><bold>✦ VOUCHER TOKEN GANTI NAMA ✦</bold></gradient>"));

            List<Component> lore = new ArrayList<>();
            lore.add(miniMessage.deserialize("<gray>Gunakan voucher ini untuk menambahkan</gray>"));
            lore.add(miniMessage.deserialize("<gold><bold>+" + amount + " Token Ganti Nama</bold></gold> <gray>ke akunmu.</gray>"));
            lore.add(miniMessage.deserialize(""));
            lore.add(miniMessage.deserialize("<gray>Satu token dapat dipakai untuk mengganti</gray>"));
            lore.add(miniMessage.deserialize("<gray>nama panggilanmu melalui perintah <yellow>/nick <nama></yellow>.</gray>"));
            lore.add(miniMessage.deserialize(""));
            lore.add(miniMessage.deserialize("<yellow>⚡ Klik Kanan untuk menukarkan voucher!</yellow>"));
            meta.lore(lore);

            meta.getPersistentDataContainer().set(tokenKey, PersistentDataType.INTEGER, amount);
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isTokenItem(ItemStack item) {
        if (item == null || item.getType() != Material.NAME_TAG || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(tokenKey, PersistentDataType.INTEGER);
    }

    public boolean redeemTokenItem(Player player, ItemStack item) {
        if (!isTokenItem(item)) return false;
        Integer amount = item.getItemMeta().getPersistentDataContainer().get(tokenKey, PersistentDataType.INTEGER);
        if (amount == null || amount <= 0) amount = 1;

        item.setAmount(item.getAmount() - 1);
        addTokens(player.getUniqueId(), player.getName(), amount);

        int total = getTokens(player.getUniqueId());
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        player.sendMessage(miniMessage.deserialize(
                "<gradient:#ffeaa7:#55efc4><bold>✦ TOKEN BERHASIL DIKLAIM! ✦</bold></gradient>\n" +
                "<green>Kamu mendapatkan <yellow><bold>+" + amount + " Token Ganti Nama</bold></yellow>!</green>\n" +
                "<gray>Total Token saat ini: <gold><bold>" + total + " Token</bold></gold>.</gray>\n" +
                "<gray>Gunakan perintah <yellow>/nick <nama_baru></yellow> untuk mengganti nama.</gray>"
        ));
        return true;
    }

    /**
     * Executes player nickname change. Checks tokens, validates length, format, and profanity.
     */
    public NicknameResult setNickname(Player player, String rawName) {
        if (rawName == null || rawName.isBlank()) {
            return NicknameResult.error("<red>Penggunaan: <yellow>/nick <nama_baru></yellow></red>");
        }

        rawName = rawName.trim();

        // 1. Validate length and safe characters
        if (rawName.length() < 3 || rawName.length() > 16) {
            return NicknameResult.error("<red>Panjang nama panggilan harus antara 3 hingga 16 karakter!</red>");
        }

        if (!SAFE_NICK_PATTERN.matcher(rawName).matches()) {
            return NicknameResult.error("<red>Nama panggilan hanya boleh mengandung huruf, angka, dan garis bawah (_)!</red>");
        }

        // 2. Check profanity / moderation engine
        if (plugin.getModerationEngine() != null && plugin.getModerationEngine().getProfanityChecker() != null) {
            ModerationResult modRes = plugin.getModerationEngine().getProfanityChecker().check(player, rawName);
            if (modRes.isBlocked()) {
                return NicknameResult.error("<red>✖ Nama panggilan mengandung kata terlarang atau tidak pantas!</red>");
            }
        }

        // 3. Token check (admin bypass)
        NicknameData data = getNicknameData(player.getUniqueId());
        boolean isAdmin = player.hasPermission("apexsions.nick.admin") || player.isOp();

        if (!isAdmin && data.getTokens() <= 0) {
            return NicknameResult.error(
                    "<red>Kamu membutuhkan <yellow><bold>1 Token Ganti Nama</bold></yellow> untuk mengubah nama!</red>\n" +
                    "<gray>Token dapat diperoleh melalui Hadiah Level Kerajaan, BattlePass, atau event server.</gray>"
            );
        }

        // 4. Consume token and set nickname
        if (!isAdmin) {
            data.consumeToken();
        }

        data.setNicknameRaw(rawName);
        repository.save(data);
        applyToPlayer(player, data);

        int remainingTokens = data.getTokens();
        return NicknameResult.success(
                "<green>✓ Berhasil mengganti nama panggilan menjadi <yellow><bold>" + rawName + "</bold></yellow>!</green>\n" +
                "<gray>Sisa Token: <gold>" + remainingTokens + " Token</gold>.</gray>\n" +
                "<yellow>⚡ Sekarang buka <aqua>/nick color</aqua> untuk memilih warna/gradien namamu!</yellow>"
        );
    }

    /**
     * Admin forced nickname update without token cost.
     */
    public NicknameResult setOtherNickname(Player target, String rawName) {
        if (rawName == null || rawName.isBlank()) {
            return NicknameResult.error("<red>Penggunaan: <yellow>/nick setother <player> <nama_baru></yellow></red>");
        }

        rawName = rawName.trim();
        if (!SAFE_NICK_PATTERN.matcher(rawName).matches()) {
            return NicknameResult.error("<red>Nama panggilan hanya boleh huruf, angka, dan garis bawah (_)!</red>");
        }

        NicknameData data = getNicknameData(target.getUniqueId());
        data.setNicknameRaw(rawName);
        repository.save(data);
        applyToPlayer(target, data);

        return NicknameResult.success("<green>✓ Berhasil mengubah nama panggilan " + target.getName() + " menjadi <yellow>" + rawName + "</yellow>!</green>");
    }

    public NicknameResult setColorStyle(Player player, NickColorStyle style) {
        NicknameData data = getNicknameData(player.getUniqueId());
        if (!data.hasNickname()) {
            return NicknameResult.error("<red>Kamu belum mengatur nama panggilan! Gunakan <yellow>/nick <nama></yellow> terlebih dahulu.</red>");
        }

        if (!style.hasPermission(player)) {
            return NicknameResult.error("<red>🔒 Kamu tidak memiliki izin untuk menggunakan warna ini! (" + style.getRequiredRank() + ")</red>");
        }

        data.setColorStyleId(style.getId());
        repository.save(data);
        applyToPlayer(player, data);

        return NicknameResult.success("<green>✓ Berhasil menerapkan gaya warna <yellow>" + style.getDisplayName() + "</yellow>!</green>");
    }

    public NicknameResult resetNickname(Player player) {
        NicknameData data = getNicknameData(player.getUniqueId());
        if (!data.hasNickname()) {
            return NicknameResult.error("<yellow>Nama panggilanmu sudah menggunakan nama asli akun Minecraft!</yellow>");
        }

        data.setNicknameRaw(null);
        data.setColorStyleId("default");
        repository.save(data);
        applyToPlayer(player, data);

        return NicknameResult.success("<green>✓ Nama panggilan berhasil direset kembali ke <yellow>" + player.getName() + "</yellow> (Gratis)!</green>");
    }

    public NicknameResult resetOtherNickname(Player target) {
        NicknameData data = getNicknameData(target.getUniqueId());
        data.setNicknameRaw(null);
        data.setColorStyleId("default");
        repository.save(data);
        applyToPlayer(target, data);

        return NicknameResult.success("<green>✓ Berhasil mereset nama panggilan " + target.getName() + " kembali ke nama asli!</green>");
    }

    public void applyToPlayer(Player player, NicknameData data) {
        if (player == null || !player.isOnline()) return;

        Component formattedName;
        if (data != null && data.hasNickname()) {
            NickColorStyle style = NickColorStyle.fromId(data.getColorStyleId());
            formattedName = style.apply(data.getNicknameRaw());
        } else {
            formattedName = Component.text(player.getName());
        }

        player.displayName(formattedName);
        player.playerListName(formattedName);
        player.customName(formattedName);
        player.setCustomNameVisible(true);
    }

    public Component getFormattedNickname(Player player) {
        NicknameData data = getNicknameData(player.getUniqueId());
        if (data != null && data.hasNickname()) {
            NickColorStyle style = NickColorStyle.fromId(data.getColorStyleId());
            return style.apply(data.getNicknameRaw());
        }
        return Component.text(player.getName());
    }

    public CompletableFuture<Optional<String>> findRealName(String nicknameQuery) {
        if (nicknameQuery == null || nicknameQuery.isBlank()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        String clean = nicknameQuery.trim();

        // 1. Check online cache
        for (NicknameData data : cache.values()) {
            if (data.hasNickname() && data.getNicknameRaw().equalsIgnoreCase(clean)) {
                return CompletableFuture.completedFuture(Optional.of(data.getPlayerName()));
            }
        }

        // 2. Check repository
        return repository.findByNickname(clean).thenApply(opt -> opt.map(NicknameData::getPlayerName));
    }

    public record NicknameResult(boolean success, String message) {
        public static NicknameResult success(String msg) { return new NicknameResult(true, msg); }
        public static NicknameResult error(String msg) { return new NicknameResult(false, msg); }
    }
}
