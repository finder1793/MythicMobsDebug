package com.pwing.guilds.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import com.pwing.guilds.PwingGuilds;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ConfigValidator {
    private final PwingGuilds plugin;
    private final List<String> errors = new ArrayList<>();
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

    public ConfigValidator(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    public boolean validate() {
        validateGuildLevels();
        validateEventSchedules();
        validateStorage();
        validateExpSources();
        validateRewards();
        validateLocations();
        validateBuffs();

        if (!errors.isEmpty()) {
            plugin.getLogger().warning("=== Configuration Warnings ===");
            errors.forEach(error -> plugin.getLogger().warning(error));
            plugin.getLogger().warning("Some features may not work as expected. Please review the warnings above.");
        }

        return true; // Always return true to allow plugin to continue loading
    }

    private void validateGuildLevels() {
        ConfigurationSection levels = plugin.getConfig().getConfigurationSection("guild-levels");
        if (levels == null) {
            errors.add("Missing guild-levels section!");
            return;
        }

        int previousExp = 0;
        for (String key : levels.getKeys(false)) {
            int level = Integer.parseInt(key);
            int expRequired = levels.getInt(key + ".exp-required");

            if (expRequired < previousExp) {
                errors.add("Guild level " + level + " exp requirement is lower than previous level!");
            }
            previousExp = expRequired;
        }
    }

    private void validateEventSchedules() {
        ConfigurationSection schedules = plugin.getConfig().getConfigurationSection("event-schedule");
        if (schedules == null) return;

        for (String event : schedules.getKeys(false)) {
            String timeString = schedules.getString(event + ".time");
            try {
                LocalTime.parse(timeString, timeFormat);
            } catch (Exception e) {
                errors.add("Invalid time format for event " + event + ": " + timeString);
            }
        }
    }


    private void validateStorage() {
        ConfigurationSection storage = plugin.getConfig().getConfigurationSection("storage");
        if (storage == null) {
            errors.add("Missing storage configuration section!");
            return;
        }

        String type = plugin.getConfig().getString("storage.type");
        if (type.equalsIgnoreCase("MYSQL")) {
            ConfigurationSection mysql = plugin.getConfig().getConfigurationSection("storage.mysql");
            if (mysql == null) {
                errors.add("MySQL storage selected but configuration is missing!");
                return;
            }

            if (mysql.getString("host", "").isEmpty()) {
                errors.add("MySQL host is not configured!");
            }
            if (mysql.getString("database", "").isEmpty()) {
                errors.add("MySQL database is not configured!");
            }
        }

        ConfigurationSection settings = storage.getConfigurationSection("settings");
        if (settings == null) {
            errors.add("Missing storage settings configuration!");
            return;
        }

        if (!settings.contains("default-rows")) {
            errors.add("Missing default-rows in storage settings!");
        } else {
            int rows = settings.getInt("default-rows");
            if (rows < 1 || rows > 6) {
                errors.add("Storage default-rows must be between 1 and 6!");
            }
        }

        if (!settings.contains("save-interval")) {
            errors.add("Missing save-interval in storage settings!");
        }
    }

    private void validateExpSources() {
        ConfigurationSection expSources = plugin.getConfig().getConfigurationSection("exp-sources");
        if (expSources == null) return;

        for (String source : expSources.getKeys(false)) {
            if (!expSources.getBoolean(source + ".enabled", false)) continue;

            ConfigurationSection values = expSources.getConfigurationSection(source + ".values");
            if (values == null) {
                errors.add("Missing values for exp source: " + source);
            }
        }
    }

    private void validateRewards() {
        ConfigurationSection events = plugin.getConfig().getConfigurationSection("events");
        if (events == null) return;

        for (String event : events.getKeys(false)) {
            ConfigurationSection rewards = events.getConfigurationSection(event + ".rewards");
            if (rewards == null) continue;

            for (String place : rewards.getKeys(false)) {
                List<String> rewardCommands = rewards.getStringList(place);
                for (String command : rewardCommands) {
                    if (!command.contains(" ")) {
                        errors.add("Invalid reward command format in " + event + " " + place + ": " + command);
                        continue;
                    }

                    String[] parts = command.split(" ", 2);
                    if (parts[0].equals("claims")) {
                        try {
                            Integer.parseInt(parts[1]);
                        } catch (NumberFormatException e) {
                            errors.add("Invalid claim amount in " + event + " " + place + ": " + parts[1]);
                        }
                    }
                }
            }
        }
    }

    private void validateLocations() {
        ConfigurationSection bossRaid = plugin.getConfig().getConfigurationSection("events.boss-raid.spawn-location");
        if (bossRaid != null) {
            if (!bossRaid.contains("world")) {
                errors.add("Boss raid spawn location missing world!");
            }
            if (!bossRaid.contains("x") || !bossRaid.contains("y") || !bossRaid.contains("z")) {
                errors.add("Boss raid spawn location missing coordinates!");
            }
        }
    }


    private void validateEvents() {
        ConfigurationSection events = plugin.getConfig().getConfigurationSection("events");
        if (events == null) {
            errors.add("Missing events section!");
            return;
        }

        for (String eventName : events.getKeys(false)) {
            if (!events.contains(eventName + ".enabled")) {
                errors.add("Missing enabled flag for event: " + eventName);
            }

            // Only validate configuration if event is enabled
            if (events.getBoolean(eventName + ".enabled", false)) {
                validateEventConfig(eventName, events.getConfigurationSection(eventName));
            }
        }
    }

    private void validateEventConfig(String eventName, ConfigurationSection config) {
        // Validate event-specific requirements
        switch (eventName) {
            case "boss-raid" -> {
                if (!config.contains("boss-type")) {
                    errors.add("Boss raid event enabled but missing boss-type!");
                }
            }
            case "resource-race" -> {
                if (!config.contains("rewards")) {
                    errors.add("Resource race event enabled but missing rewards!");
                }
            }
        }
    }

    private void validateBuffs() {
        ConfigurationSection buffs = plugin.getConfig().getConfigurationSection("guild-buffs");
        if (buffs == null) return;

        for (String buffKey : buffs.getKeys(false)) {
            ConfigurationSection buff = buffs.getConfigurationSection(buffKey);
            if (buff == null) continue;

        // Validate material
            String materialName = buff.getString("material");
            if (materialName != null) {
                try {
                    Material.valueOf(materialName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    errors.add("Invalid material for buff " + buffKey + ": " + materialName);
                }
            }

            // Validate slot
            int slot = buff.getInt("slot", -1);
            if (slot < 0) {
                errors.add("Invalid slot number for buff " + buffKey + ": " + slot);
            }
        }
    }
}
