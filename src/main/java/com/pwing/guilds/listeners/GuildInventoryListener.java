package com.pwing.guilds.listeners;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import com.pwing.guilds.listeners.PlayerJoinQuitListener;
import com.pwing.guilds.listeners.GuildChatListener;
import com.pwing.guilds.listeners.GuildInventoryListener;
// Add the remaining imports as we implement them

/**
 * Handles inventory events for guild storage and shared inventories.
 */
public class GuildInventoryListener implements Listener {
    private final PwingGuilds plugin;

    /**
     * Creates a new inventory listener
     * 
     * @param plugin The plugin instance
     */
    public GuildInventoryListener(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles clicks in guild storage inventories
     * 
     * @param event The click event
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player))
            return;
        if (event.getView().getTitle().contains("Guild Storage")) {
            plugin.getGuildManager().getPlayerGuild(player.getUniqueId()).ifPresent(guild -> {
                if (!guild.getPerks().activatePerk("guild-storage")) {
                    event.setCancelled(true);
                }
            });
        }
    }

    /**
     * Handles closing guild storage inventories
     * 
     * @param event The close event
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getView().getTitle().contains("Guild Storage")) {
            plugin.getStorageManager().saveInventory(event.getInventory());
        }
    }
}
