package com.apexsions.customenchants;

import com.apexsions.customenchants.commands.AceAdminCommand;
import com.apexsions.customenchants.commands.CustomEnchantsCommand;
import com.apexsions.customenchants.enchant.EnchantmentRegistry;
import com.apexsions.customenchants.group.GroupRegistry;
import com.apexsions.customenchants.gui.CustomEnchantsGUIListener;
import com.apexsions.customenchants.items.EnchantBookManager;
import com.apexsions.customenchants.items.MagicDustManager;
import com.apexsions.customenchants.items.ScrollManager;
import com.apexsions.customenchants.listener.CustomItemApplyListener;
import com.apexsions.customenchants.listener.EnchantEventListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Main plugin class for ApexsionsCustomEnchants.
 */
public class ApexsionsCustomEnchantsPlugin extends JavaPlugin {

    private static ApexsionsCustomEnchantsPlugin instance;

    private GroupRegistry groupRegistry;
    private EnchantmentRegistry enchantmentRegistry;
    private EnchantBookManager enchantBookManager;
    private MagicDustManager magicDustManager;
    private ScrollManager scrollManager;
    private com.apexsions.customenchants.presets.PresetManager presetManager;
    private com.apexsions.customenchants.items.ItemRenameManager itemRenameManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save default configs
        saveDefaultConfig();

        // Initialize Registries
        this.groupRegistry = new GroupRegistry(this);
        this.enchantmentRegistry = new EnchantmentRegistry(this);

        // Initialize Item Managers
        this.enchantBookManager = new EnchantBookManager(this);
        this.magicDustManager = new MagicDustManager(this);
        this.scrollManager = new ScrollManager(this);
        this.presetManager = new com.apexsions.customenchants.presets.PresetManager(this);
        this.itemRenameManager = new com.apexsions.customenchants.items.ItemRenameManager(this);

        // Register Event Listeners
        getServer().getPluginManager().registerEvents(new CustomItemApplyListener(this), this);
        getServer().getPluginManager().registerEvents(new EnchantEventListener(this), this);
        getServer().getPluginManager().registerEvents(new CustomEnchantsGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new com.apexsions.customenchants.listener.ToolSetBonusListener(this), this);
        getServer().getPluginManager().registerEvents(this.itemRenameManager, this);

        // Register Commands
        CustomEnchantsCommand ceCmd = new CustomEnchantsCommand(this);
        PluginCommand ce = getCommand("ce");
        if (ce != null) {
            ce.setExecutor(ceCmd);
            ce.setTabCompleter(ceCmd);
        }

        AceAdminCommand aceCmd = new AceAdminCommand(this);
        PluginCommand ace = getCommand("ace");
        if (ace != null) {
            ace.setExecutor(aceCmd);
            ace.setTabCompleter(aceCmd);
        }

        PluginCommand presets = getCommand("presets");
        if (presets != null) {
            presets.setExecutor((sender, cmd, lbl, args) -> {
                if (!sender.hasPermission("apexsions.admin") && !sender.hasPermission("apexsions.ace.admin")) {
                    sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize("<red>Anda tidak memiliki izin untuk menggunakan perintah preset.</red>"));
                    return true;
                }
                if (sender instanceof org.bukkit.entity.Player player) {
                    new com.apexsions.customenchants.gui.AdminPresetsGUI(this, player, null).open();
                } else {
                    sender.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize("<red>Perintah preset hanya bisa dijalankan oleh pemain.</red>"));
                }
                return true;
            });
        }

        getLogger().info("==================================================");
        getLogger().info("  ApexsionsCustomEnchants v" + getDescription().getVersion() + " Enabled");
        getLogger().info("  Total Enchants Loaded: " + enchantmentRegistry.getAllEnchantments().size());
        getLogger().info("  Total Groups Loaded: " + groupRegistry.getAllGroups().size());
        getLogger().info("==================================================");
    }

    @Override
    public void onDisable() {
        getLogger().info("ApexsionsCustomEnchants disabled.");
    }

    public void reload() {
        reloadConfig();
        if (groupRegistry != null) {
            groupRegistry.load();
        }
        if (enchantmentRegistry != null) {
            enchantmentRegistry.load();
        }
        getLogger().info("ApexsionsCustomEnchants reloaded successfully.");
    }

    public static ApexsionsCustomEnchantsPlugin getInstance() {
        return instance;
    }

    public GroupRegistry getGroupRegistry() {
        return groupRegistry;
    }

    public EnchantmentRegistry getEnchantmentRegistry() {
        return enchantmentRegistry;
    }

    public EnchantBookManager getEnchantBookManager() {
        return enchantBookManager;
    }

    public MagicDustManager getMagicDustManager() {
        return magicDustManager;
    }

    public ScrollManager getScrollManager() {
        return scrollManager;
    }

    public double getSpecificBookMultiplier() {
        return getConfig().getDouble("settings.specific-book-price-multiplier", 3.0);
    }

    public void setSpecificBookMultiplier(double multiplier) {
        getConfig().set("settings.specific-book-price-multiplier", Math.max(1.0, multiplier));
        saveConfig();
    }

    public double getSpecificBookSuccessChance() {
        return getConfig().getDouble("settings.specific-book-success-chance", 50.0);
    }

    public void setSpecificBookSuccessChance(double chance) {
        getConfig().set("settings.specific-book-success-chance", Math.max(1.0, Math.min(100.0, chance)));
        saveConfig();
    }

    public double getSpecificBookDestroyChance() {
        return getConfig().getDouble("settings.specific-book-destroy-chance", 30.0);
    }

    public com.apexsions.customenchants.presets.PresetManager getPresetManager() {
        return presetManager;
    }

    public com.apexsions.customenchants.items.ItemRenameManager getItemRenameManager() {
        return itemRenameManager;
    }
}
