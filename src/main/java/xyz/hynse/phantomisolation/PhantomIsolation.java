package xyz.hynse.phantomisolation;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class PhantomIsolation extends JavaPlugin implements Listener {

    private File playerDataFile;
    private YamlConfiguration playerDataConfig;

    @Override
    public void onEnable() {
        // Load the player data from the config file
        playerDataFile = new File(getDataFolder(), "playerdata.yml");
        playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);

        // Register events and run tasks
        getServer().getPluginManager().registerEvents(this, this);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : getServer().getOnlinePlayers()) {
                    boolean isEnabled = playerDataConfig.getBoolean("players." + player.getName(), true);
                    if (isEnabled) {
                        int timeSinceRest = player.getStatistic(Statistic.TIME_SINCE_REST);
                        if (timeSinceRest >= 24000) {
                            player.setStatistic(Statistic.TIME_SINCE_REST, 0);
                        }
                    } else {
                        player.setStatistic(Statistic.TIME_SINCE_REST, 0);
                    }
                }
            }
        }.runTaskTimerAsynchronously(this, 0, 20);
    }

    @Override
    public void onDisable() {
        for (Player player : getServer().getOnlinePlayers()) {
            player.setStatistic(Statistic.TIME_SINCE_REST, 0);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("piso")) {
            if (args.length == 0) {
                sender.sendMessage(Component.text("Phantom spawning is currently " + (playerDataConfig.getBoolean("players." + sender.getName(), true) ? "enabled" : "disabled") + ".", playerDataConfig.getBoolean("players." + sender.getName(), true) ? NamedTextColor.GREEN : NamedTextColor.RED));
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("true")) {
                    if (sender instanceof Player player) {
                        playerDataConfig.set("players." + player.getName(), true);
                        savePlayerData();
                        player.sendMessage(Component.text("Phantom spawning enabled.", NamedTextColor.GREEN));
                    } else {
                        sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
                    }
                } else if (args[0].equalsIgnoreCase("false")) {
                    if (sender instanceof Player player) {
                        playerDataConfig.set("players." + player.getName(), false);
                        savePlayerData();
                        player.sendMessage(Component.text("Phantom spawning disabled.", NamedTextColor.RED));
                    } else {
                        sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
                    }
                } else {
                    sender.sendMessage(Component.text("Invalid argument. Usage: /piso true|false", NamedTextColor.RED));
                }
            } else {
                sender.sendMessage(Component.text("Invalid number of arguments. Usage: /piso [true|false]", NamedTextColor.RED));
            }
            return true;
        }
        return false;
    }


    private void savePlayerData() {
        // Save the player data to the config file
        try {
            playerDataConfig.save(playerDataFile);
        } catch (IOException e) {
            getLogger().warning("Failed to save player data: " + e.getMessage());
        }
    }
}