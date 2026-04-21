package me.einfachduncan.rtp.gui;

import me.einfachduncan.rtp.config.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class RTPGui {

    public static final int OVERWORLD_SLOT = 11;
    public static final int NETHER_SLOT = 13;
    public static final int END_SLOT = 15;

    private RTPGui() {}

    /**
     * Opens the RTP world-selection GUI for the given player.
     * Buttons are only shown for worlds that actually exist on this server.
     */
    public static void open(Player player, ConfigManager configManager) {
        String guiTitle = configManager.getGuiTitle();

        Inventory inv = Bukkit.createInventory(null, 27,
                Component.text(guiTitle)
                        .color(NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false));

        // Fill all slots with a silent glass-pane filler
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE,
                Component.text(" "), List.of());
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, filler);
        }

        // Overworld button
        World overworld = Bukkit.getWorld(configManager.getOverworldName());
        if (overworld != null) {
            inv.setItem(OVERWORLD_SLOT, createItem(Material.GRASS_BLOCK,
                    Component.text(configManager.getGuiOverworldName())
                            .color(NamedTextColor.GREEN)
                            .decoration(TextDecoration.ITALIC, false),
                    List.of(Component.text(configManager.getGuiOverworldLore())
                            .color(NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false))));
        }

        // Nether button
        World nether = Bukkit.getWorld(configManager.getNetherName());
        if (nether != null) {
            inv.setItem(NETHER_SLOT, createItem(Material.NETHERRACK,
                    Component.text(configManager.getGuiNetherName())
                            .color(NamedTextColor.RED)
                            .decoration(TextDecoration.ITALIC, false),
                    List.of(Component.text(configManager.getGuiNetherLore())
                            .color(NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false))));
        }

        // End button
        World end = Bukkit.getWorld(configManager.getEndName());
        if (end != null) {
            inv.setItem(END_SLOT, createItem(Material.END_STONE,
                    Component.text(configManager.getGuiEndName())
                            .color(NamedTextColor.LIGHT_PURPLE)
                            .decoration(TextDecoration.ITALIC, false),
                    List.of(Component.text(configManager.getGuiEndLore())
                            .color(NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false))));
        }

        player.openInventory(inv);
    }

    private static ItemStack createItem(Material material, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        if (!lore.isEmpty()) {
            meta.lore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }
}
