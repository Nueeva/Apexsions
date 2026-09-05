package com.apexsions.media.creator.gui;

import com.apexsions.media.ApexsionsMediaPlugin;
import com.apexsions.media.creator.model.CreatorClaim;
import com.apexsions.media.creator.model.CreatorProfile;
import com.apexsions.media.creator.model.Platform;
import com.apexsions.media.creator.session.ChatInputSessionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CreatorHubGUI implements InventoryHolder {

    private final ApexsionsMediaPlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private CreatorProfile profile;
    private List<CreatorClaim> claims = new ArrayList<>();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault());

    public CreatorHubGUI(ApexsionsMediaPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, 54, mm.deserialize("<gradient:#e74c3c:#f39c12><bold>✦ CREATOR VERIFICATION HUB ✦</bold></gradient>"));
        loadAndBuild();
    }

    public void loadAndBuild() {
        plugin.getCreatorManager().getProfile(player.getUniqueId(), player.getName()).thenAccept(prof -> {
            this.profile = prof;
            plugin.getCreatorManager().getRepository().getClaims(player.getUniqueId()).thenAccept(claimList -> {
                this.claims = claimList;
                Bukkit.getScheduler().runTask(plugin, this::render);
            });
        });
    }

    private void render() {
        inventory.clear();

        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, "<gray> </gray>", List.of());
        for (int i = 0; i < 54; i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }

        // Slot 4: Player Profile Head
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sm = (SkullMeta) head.getItemMeta();
        if (sm != null) {
            sm.setOwningPlayer(player);
            sm.displayName(mm.deserialize("<gradient:#f1c40f:#e67e22><bold>👤 " + player.getName().toUpperCase() + " CREATOR STATUS</bold></gradient>"));
            List<Component> lore = new ArrayList<>();
            lore.add(mm.deserialize("<gray>Status Penautan Akun:</gray>"));
            lore.add(mm.deserialize(" <dark_gray>•</dark_gray> <red>YouTube:</red> " + (profile.isYouTubeLinked() ? "<green>" + profile.getYoutubeChannelId() + "</green>" : "<red>Belum Tertaut</red>")));
            lore.add(mm.deserialize(" <dark_gray>•</dark_gray> <light_purple>TikTok:</light_purple> " + (profile.isTikTokLinked() ? "<green>@" + profile.getTiktokUsername() + "</green>" : "<red>Belum Tertaut</red>")));
            lore.add(mm.deserialize(""));
            lore.add(mm.deserialize("<gray>Total Video Terverifikasi: <yellow>" + claims.size() + " Video</yellow></gray>"));
            lore.add(mm.deserialize("<gray>Hashtag Wajib: <aqua>" + String.join(", ", plugin.getCreatorManager().getRequiredHashtags()) + "</aqua></gray>"));
            sm.lore(lore);
            head.setItemMeta(sm);
        }
        inventory.setItem(4, head);

        // Slot 20: YouTube Platform Card
        ItemStack ytItem;
        if (profile.isYouTubeLinked()) {
            ytItem = createItem(Material.RED_CONCRETE, "<red><bold>▶ YOUTUBE (TERHUBUNG)</bold></red>", List.of(
                    mm.deserialize("<gray>Channel ID/Handle: <white>" + profile.getYoutubeChannelId() + "</white></gray>"),
                    mm.deserialize("<green>Status: Akun aktif terverifikasi.</green>"),
                    mm.deserialize(""),
                    mm.deserialize("<red><bold>[KLIK KANAN]</bold> untuk memutuskan tautan (Unlink).</red>")
            ));
        } else {
            ytItem = createItem(Material.RED_TERRACOTTA, "<red><bold>▶ TAUTKAN YOUTUBE</bold></red>", List.of(
                    mm.deserialize("<gray>Tautkan channel YouTube kamu untuk mulai</gray>"),
                    mm.deserialize("<gray>klaim hadiah dari video YouTube kamu!</gray>"),
                    mm.deserialize(""),
                    mm.deserialize("<green><bold>[KLIK KIRI]</bold> untuk memulai penautan.</green>")
            ));
        }
        inventory.setItem(20, ytItem);

        // Slot 22: Master Action Submit Video
        ItemStack submitItem = createItem(Material.NETHER_STAR, "<gradient:#2ecc71:#27ae60><bold>🚀 SUBMIT & VERIFIKASI VIDEO 🚀</bold></gradient>", List.of(
                mm.deserialize("<gray>Punya video YouTube / TikTok baru dengan hashtag server?</gray>"),
                mm.deserialize("<gray>Klaim reward views & likes secara otomatis!</gray>"),
                mm.deserialize(""),
                mm.deserialize("<yellow>Syarat:</yellow>"),
                mm.deserialize(" <gray>1. Wajib ada hashtag <aqua>" + String.join(" / ", plugin.getCreatorManager().getRequiredHashtags()) + "</aqua></gray>"),
                mm.deserialize(" <gray>2. Maksimal umur video <yellow>" + plugin.getCreatorManager().getMaxVideoAgeDays() + " hari</yellow></gray>"),
                mm.deserialize(" <gray>3. Mencapai batas minimal Views & Likes Tier.</gray>"),
                mm.deserialize(""),
                mm.deserialize("<green><bold>✦ KLIK UNTUK SUBMIT TAUTAN VIDEO ✦</bold></green>")
        ));
        inventory.setItem(22, submitItem);

        // Slot 24: TikTok Platform Card
        ItemStack ttItem;
        if (profile.isTikTokLinked()) {
            ttItem = createItem(Material.PURPLE_CONCRETE, "<light_purple><bold>♫ TIKTOK (TERHUBUNG)</bold></light_purple>", List.of(
                    mm.deserialize("<gray>Username: <white>@" + profile.getTiktokUsername() + "</white></gray>"),
                    mm.deserialize("<green>Status: Akun aktif terverifikasi.</green>"),
                    mm.deserialize(""),
                    mm.deserialize("<red><bold>[KLIK KANAN]</bold> untuk memutuskan tautan (Unlink).</red>")
            ));
        } else {
            ttItem = createItem(Material.PURPLE_TERRACOTTA, "<light_purple><bold>♫ TAUTKAN TIKTOK</bold></light_purple>", List.of(
                    mm.deserialize("<gray>Tautkan akun TikTok kamu untuk klaim hadiah</gray>"),
                    mm.deserialize("<gray>dari VT yang masuk FYP dengan hashtag server!</gray>"),
                    mm.deserialize(""),
                    mm.deserialize("<green><bold>[KLIK KIRI]</bold> untuk mulai menautkan.</green>")
            ));
        }
        inventory.setItem(24, ttItem);

        // Slot 38: View Tiers & Rewards
        ItemStack tiersItem = createItem(Material.GOLD_BLOCK, "<gradient:#f39c12:#f1c40f><bold>⭐ DAFTAR TIER & REWARDS ⭐</bold></gradient>", List.of(
                mm.deserialize("<gray>Lihat seluruh tingkatan Creator (Bronze, Silver, Gold, dll)</gray>"),
                mm.deserialize("<gray>beserta hadiah Coins, Gems, Ranks, dan Title!</gray>"),
                mm.deserialize(""),
                mm.deserialize("<yellow>Klik untuk melihat daftar tier lengkap.</yellow>")
        ));
        inventory.setItem(38, tiersItem);

        // Slot 40: Claim History
        List<Component> claimLore = new ArrayList<>();
        claimLore.add(mm.deserialize("<gray>Riwayat klaim video terakhir kamu:</gray>"));
        if (claims.isEmpty()) {
            claimLore.add(mm.deserialize("<dark_gray><i>Belum ada video yang pernah diklaim.</i></dark_gray>"));
        } else {
            int count = 0;
            for (CreatorClaim c : claims) {
                if (count++ >= 4) break;
                String dateStr = DATE_FMT.format(Instant.ofEpochMilli(c.getClaimedAt()));
                claimLore.add(mm.deserialize(" <gold>•</gold> <white>" + c.getPlatform().getDisplayName() + "</white> (" + c.getViews() + " Views, " + c.getLikes() + " Likes) - <yellow>" + c.getTierId().toUpperCase() + "</yellow> <dark_gray>[" + dateStr + "]</dark_gray>"));
            }
        }
        ItemStack historyItem = createItem(Material.BOOK, "<gradient:#3498db:#2980b9><bold>📜 RIWAYAT KLAIM VIDEO</bold></gradient>", claimLore);
        inventory.setItem(40, historyItem);

        // Slot 42: Creator Guidelines
        ItemStack helpItem = createItem(Material.KNOWLEDGE_BOOK, "<gradient:#1abc9c:#16a085><bold>💡 PANDUAN & BANTUAN KREATOR</bold></gradient>", List.of(
                mm.deserialize("<gray>Cara menjadi verified content creator:</gray>"),
                mm.deserialize(" <yellow>1.</yellow> Tautkan akun YouTube/TikTok kamu."),
                mm.deserialize(" <yellow>2.</yellow> Buat video/VT tentang server Apexsions."),
                mm.deserialize(" <yellow>3.</yellow> Cantumkan hashtag <aqua>" + String.join(" ", plugin.getCreatorManager().getRequiredHashtags()) + "</aqua>"),
                mm.deserialize(" <yellow>4.</yellow> Masukkan link video ke menu ini untuk verifikasi."),
                mm.deserialize(""),
                mm.deserialize("<gray>Hadiah akan langsung masuk secara otomatis!</gray>")
        ));
        inventory.setItem(42, helpItem);

        // Slot 49: Close Button
        ItemStack closeItem = createItem(Material.BARRIER, "<red><bold>✖ TUTUP MENU</bold></red>", List.of(mm.deserialize("<gray>Klik untuk menutup menu.</gray>")));
        inventory.setItem(49, closeItem);
    }

    public void handleClick(InventoryClickEvent e) {
        e.setCancelled(true);
        int slot = e.getRawSlot();

        if (slot == 49) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            return;
        }

        // Slot 22: Submit Video
        if (slot == 22) {
            plugin.getChatInputSessionManager().startSession(
                    player,
                    ChatInputSessionManager.SessionType.SUBMIT_VIDEO,
                    null,
                    "<yellow>Silakan ketik atau tempelkan <aqua>URL Video YouTube</aqua> atau <light_purple>URL Video TikTok</light_purple> kamu di chat:</yellow>",
                    input -> plugin.getCreatorManager().processVideoSubmission(player, input)
            );
            return;
        }

        // Slot 20: YouTube Platform
        if (slot == 20) {
            if (profile.isYouTubeLinked()) {
                if (e.isRightClick()) {
                    plugin.getCreatorManager().unlinkPlatform(player, Platform.YOUTUBE).thenRun(() -> {
                        player.sendMessage(mm.deserialize("<yellow><b>[Creator]</b> Tautan akun YouTube berhasil diputuskan.</yellow>"));
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                        loadAndBuild();
                    });
                }
            } else {
                plugin.getChatInputSessionManager().startSession(
                        player,
                        ChatInputSessionManager.SessionType.LINK_YOUTUBE,
                        Platform.YOUTUBE,
                        "<yellow>Masukkan <aqua>Channel ID (UC...)</aqua> atau <aqua>Handle (@nama)</aqua> YouTube kamu di chat:</yellow>",
                        input -> {
                            plugin.getCreatorManager().startLinking(player, Platform.YOUTUBE, input).thenAccept(code -> {
                                player.sendMessage(mm.deserialize(
                                        "\n<gradient:#f39c12:#f1c40f><b>✦ VERIFIKASI KEPEMILIKAN YOUTUBE ✦</b></gradient>\n" +
                                        "<gray>Kode Verifikasi: </gray><yellow><bold>" + code + "</bold></yellow>\n" +
                                        "<gray>Silakan masukkan kode di atas ke dalam <b>Deskripsi Channel (About)</b> atau <b>Deskripsi Video Terbarumu</b> di YouTube.</gray>\n" +
                                        "<gray>Setelah ditaruh, ketik perintah: <aqua>/creator verify youtube</aqua></gray>\n"
                                ));
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
                            });
                        }
                );
            }
            return;
        }

        // Slot 24: TikTok Platform
        if (slot == 24) {
            if (profile.isTikTokLinked()) {
                if (e.isRightClick()) {
                    plugin.getCreatorManager().unlinkPlatform(player, Platform.TIKTOK).thenRun(() -> {
                        player.sendMessage(mm.deserialize("<yellow><b>[Creator]</b> Tautan akun TikTok berhasil diputuskan.</yellow>"));
                        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                        loadAndBuild();
                    });
                }
            } else {
                plugin.getChatInputSessionManager().startSession(
                        player,
                        ChatInputSessionManager.SessionType.LINK_TIKTOK,
                        Platform.TIKTOK,
                        "<yellow>Masukkan <light_purple>Username TikTok (@username)</light_purple> kamu di chat:</yellow>",
                        input -> {
                            plugin.getCreatorManager().startLinking(player, Platform.TIKTOK, input).thenCompose(code ->
                                    plugin.getCreatorManager().verifyLinking(player, Platform.TIKTOK)
                            ).thenAccept(success -> {
                                player.sendMessage(mm.deserialize("<green><b>[Creator]</b> Akun TikTok @" + input.replace("@", "") + " berhasil ditautkan!</green>"));
                                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.5f);
                            });
                        }
                );
            }
            return;
        }

        // Slot 38: View Tiers GUI
        if (slot == 38) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            player.openInventory(new CreatorTiersGUI(plugin, player).getInventory());
        }
    }

    private ItemStack createItem(Material material, String name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(mm.deserialize(name));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
