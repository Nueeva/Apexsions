package com.apexsions.crates.animation;

import com.apexsions.crates.ApexsionsCratesPlugin;
import com.apexsions.crates.api.CrateMilestoneEvent;
import com.apexsions.crates.api.CrateRewardWinEvent;
import com.apexsions.crates.crate.Crate;
import com.apexsions.crates.milestone.Milestone;
import com.apexsions.crates.reward.Reward;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectableOpening extends OpeningSession implements InventoryHolder {

    private final Inventory inventory;
    private final Map<Integer, Reward> slotRewards = new HashMap<>();
    private boolean selected = false;
    private static final int[] MYSTERY_SLOTS = {11, 12, 13, 14, 15};

    public SelectableOpening(ApexsionsCratesPlugin plugin, Player player, Crate crate, boolean virtualKey) {
        super(plugin, player, crate, virtualKey);
        this.inventory = Bukkit.createInventory(this, 27, MiniMessage.miniMessage().deserialize(
                "<gradient:#f1c40f:#e67e22><bold>PILIH PETI MISTERI</bold></gradient>"
        ));
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    @Override
    public void start() {
        renderBorders();
        populateMysterySlots();
        player.openInventory(inventory);
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
        player.sendMessage(MiniMessage.miniMessage().deserialize(
                "<yellow>★ Pilih salah satu peti misteri di bawah ini untuk membuka hadiah Anda!</yellow>"
        ));
    }

    private void renderBorders() {
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = border.getItemMeta();
        meta.displayName(Component.empty());
        border.setItemMeta(meta);

        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, border);
        }

        // Header decoration
        ItemStack star = new ItemStack(Material.NETHER_STAR);
        ItemMeta starMeta = star.getItemMeta();
        starMeta.displayName(MiniMessage.miniMessage().deserialize(
                "<gradient:#f1c40f:#e67e22><bold>✦ KARTU MISTERI APEXSIONS ✦</bold></gradient>"
        ));
        starMeta.lore(List.of(
                MiniMessage.miniMessage().deserialize("<gray>Pilih 1 dari 5 peti untuk mengungkapkan hadiah!</gray>")
        ));
        star.setItemMeta(starMeta);
        inventory.setItem(4, star);
    }

    private void populateMysterySlots() {
        int winningSlotIndex = (int) (Math.random() * MYSTERY_SLOTS.length);

        for (int i = 0; i < MYSTERY_SLOTS.length; i++) {
            int slot = MYSTERY_SLOTS[i];
            Reward reward = (i == winningSlotIndex) ? winningReward : crate.rollReward();
            slotRewards.put(slot, reward);

            ItemStack mysteryItem = new ItemStack(Material.CHEST_MINECART);
            ItemMeta meta = mysteryItem.getItemMeta();
            meta.displayName(MiniMessage.miniMessage().deserialize(
                    "<gradient:#f39c12:#f1c40f><bold>Peti Misteri #" + (i + 1) + "</bold></gradient>"
            ));
            meta.lore(List.of(
                    MiniMessage.miniMessage().deserialize("<gray>Status: <yellow>Terkunci</yellow></gray>"),
                    Component.empty(),
                    MiniMessage.miniMessage().deserialize("<green>▶ Klik untuk memilih peti ini!</green>")
            ));
            mysteryItem.setItemMeta(meta);
            inventory.setItem(slot, mysteryItem);
        }
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (selected || completed) return;

        int clickedSlot = event.getRawSlot();
        if (!slotRewards.containsKey(clickedSlot)) return;

        chooseSlot(clickedSlot);
    }

    private void chooseSlot(int clickedSlot) {
        if (selected || completed) return;
        selected = true;

        Reward chosenReward = winningReward;

        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);

        for (int slot : MYSTERY_SLOTS) {
            Reward r = (slot == clickedSlot) ? chosenReward : slotRewards.get(slot);
            ItemStack display = r.createDisplayItem(crate.getRewardChance(r));
            ItemMeta meta = display.getItemMeta();
            List<Component> lore = meta.hasLore() ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            lore.add(Component.empty());
            if (slot == clickedSlot) {
                lore.add(MiniMessage.miniMessage().deserialize("<green><bold>✔ PILIHAN ANDA (MENANG)</bold></green>"));
            } else {
                lore.add(MiniMessage.miniMessage().deserialize("<dark_gray>[Pilihan Lain]</dark_gray>"));
            }
            meta.lore(lore);
            display.setItemMeta(meta);
            inventory.setItem(slot, display);

            if (slot == clickedSlot) {
                ItemStack greenPane = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
                ItemMeta gpMeta = greenPane.getItemMeta();
                gpMeta.displayName(Component.empty());
                greenPane.setItemMeta(gpMeta);
                if (slot - 9 >= 0) inventory.setItem(slot - 9, greenPane);
                if (slot + 9 < 27) inventory.setItem(slot + 9, greenPane);
            }
        }

        deliverReward(chosenReward);
    }

    private void deliverReward(Reward reward) {
        completed = true;
        plugin.unregisterSession(player.getUniqueId());

        // 1. Give reward
        reward.give(player);

        // 2. Stats & Milestones
        plugin.getRepository().incrementCrateOpenCount(player.getUniqueId(), crate.getId()).thenAccept(newCount -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                Milestone ms = crate.getMilestone(newCount);
                if (ms != null) {
                    ms.award(player);
                    player.sendMessage(MiniMessage.miniMessage().deserialize(
                            "<gradient:#3498db:#2ecc71><bold>MILESTONE TERCAPAI!</bold></gradient> <gray>Anda telah membuka </gray><yellow>" +
                                    newCount + "x</yellow> <gray>Peti </gray>" + crate.getName() + "<gray>!</gray>"
                    ));
                    Bukkit.getPluginManager().callEvent(new CrateMilestoneEvent(player, crate, ms, newCount));
                }
            });
        });

        // 3. Event
        Bukkit.getPluginManager().callEvent(new CrateRewardWinEvent(player, crate, reward));

        // 4. Title & Sound
        Component titleComp = MiniMessage.miniMessage().deserialize("<gradient:#f1c40f:#e67e22><bold>SELAMAT!</bold></gradient>");
        Component subComp = MiniMessage.miniMessage().deserialize(reward.getName());
        player.showTitle(Title.title(titleComp, subComp, Title.Times.times(
                Duration.ofMillis(200), Duration.ofMillis(2500), Duration.ofMillis(500)
        )));

        // 5. Firework
        spawnFirework();

        // 6. Broadcast if broadcast enabled or tier >= 5
        if (reward.isBroadcast() || reward.getRarity().getTier() >= 5) {
            String bMsg = plugin.getMessages().getString("jackpot-broadcast",
                    "<gradient:#ff4757:#ffa502><bold>CRATES</bold></gradient> <gray>»</gray> <yellow>%player%</yellow> memenangkan %reward% <gray>dari</gray> %crate%!"
            ).replace("%player%", player.getName())
             .replace("%reward%", reward.getName())
             .replace("%crate%", crate.getName());
            Bukkit.broadcast(MiniMessage.miniMessage().deserialize(bMsg));
        }

        // Auto close after 3 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline() && player.getOpenInventory().getTopInventory().equals(inventory)) {
                    player.closeInventory();
                }
            }
        }.runTaskLater(plugin, 60L);
    }

    @Override
    public void skip() {
        if (completed) return;
        chooseSlot(MYSTERY_SLOTS[2]);
    }

    @Override
    public void cancel() {
        if (completed) return;
        skip();
    }

    private void spawnFirework() {
        try {
            Firework fw = player.getWorld().spawn(player.getLocation().add(0, 1, 0), Firework.class);
            FireworkMeta fwm = fw.getFireworkMeta();
            fwm.addEffect(FireworkEffect.builder()
                    .with(FireworkEffect.Type.BALL_LARGE)
                    .withColor(Color.ORANGE, Color.YELLOW, Color.WHITE)
                    .withFade(Color.PURPLE)
                    .trail(true)
                    .flicker(true)
                    .build());
            fwm.setPower(1);
            fw.setFireworkMeta(fwm);
        } catch (Exception ignored) {}
    }
}
