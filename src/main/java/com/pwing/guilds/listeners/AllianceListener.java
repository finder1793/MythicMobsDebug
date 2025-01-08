package com.pwing.guilds.listeners;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.alliance.Alliance;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.events.GuildJoinAllianceEvent;
import com.pwing.guilds.events.GuildLeaveAllianceEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;

public class AllianceListener implements Listener {
    private final PwingGuilds plugin;

    public AllianceListener(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGuildJoinAlliance(GuildJoinAllianceEvent event) {
        Alliance alliance = event.getAlliance();
        Guild guild = event.getGuild();

        alliance.addMember(guild);
        plugin.getAllianceManager().updateGuildAlliance(guild.getName(), alliance);
        plugin.getAllianceManager().saveAlliance(alliance);

        alliance.getMembers().forEach(g ->
            g.broadcastMessage("§a" + guild.getName() + " has joined the alliance!"));
    }

    @EventHandler
    public void onGuildLeaveAlliance(GuildLeaveAllianceEvent event) {
        Alliance alliance = event.getAlliance();
        Guild guild = event.getGuild();

        alliance.removeMember(guild);
        plugin.getAllianceManager().removeGuildAlliance(guild.getName());
        plugin.getAllianceManager().saveAlliance(alliance);

        alliance.getMembers().forEach(g ->
            g.broadcastMessage("§c" + guild.getName() + " has left the alliance."));
    }

    @EventHandler
    public void onAllianceChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (!message.startsWith("@a ")) {
            return;
        }

        event.setCancelled(true);
        plugin.getGuildManager().getPlayerGuild(player.getUniqueId()).ifPresent(guild -> {
            Alliance alliance = guild.getAlliance();
            if (alliance != null) {
                String formattedMessage = "§b[Alliance] " + guild.getName() + " " +
                    player.getName() + ": §f" + message.substring(3);
                alliance.getMembers().forEach(g ->
                    g.broadcastMessage(formattedMessage));
            }
        });
    }
}