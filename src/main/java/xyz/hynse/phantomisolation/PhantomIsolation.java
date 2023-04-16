package xyz.hynse.phantomisolation;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
        AsyncScheduler scheduler = getServer().getAsyncScheduler();

            // Schedule the task with the AsyncScheduler
            scheduler.runAtFixedRate(this, task -> {
                for (Player player : getServer().getOnlinePlayers()) {
                    boolean isEnabled = playerDataConfig.getBoolean("players." + player.getName(), true);
                    if (isEnabled) {
                        int timeSinceRest = player.getStatistic(Statistic.TIME_SINCE_REST);
                        if (timeSinceRest >= 24000) {
                            player.setStatistic(Statistic.TIME_SINCE_REST, 0);
                        }
                    } else {
                        getLogger().info(player.getName() + " will get phontom spawn.");
                    }
                }
            }, 300, 20, TimeUnit.SECONDS);
        }

    @Override
    public void onDisable() {
        for (Player player : getServer().getOnlinePlayers()) {
            player.setStatistic(Statistic.TIME_SINCE_REST, 0);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("phantomisolation")) {
            if (args.length == 0) {
                sender.sendMessage(Component.text("PhantomIsolation is " + (playerDataConfig.getBoolean("players." + sender.getName(), true) ? "enabled" : "disabled") + ".", playerDataConfig.getBoolean("players." + sender.getName(), true) ? NamedTextColor.GREEN : NamedTextColor.RED));
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("disable")) {
                    if (sender instanceof Player player) {
                        playerDataConfig.set("players." + player.getName(), false);
                        savePlayerData();
                        player.sendMessage(Component.text("PhantomIsolation is disabled", NamedTextColor.RED));
                    } else {
                        sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
                    }
                } else if (args[0].equalsIgnoreCase("enable")) {
                    if (sender instanceof Player player) {
                        playerDataConfig.set("players." + player.getName(), true);
                        savePlayerData();
                        player.sendMessage(Component.text("PhantomIsolation is enabled", NamedTextColor.GREEN));
                    } else {
                        sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
                    }
                } else {
                    sender.sendMessage(Component.text("Invalid argument. Usage: /phantomisolation <enable/disable>", NamedTextColor.RED));
                }
            } else {
                sender.sendMessage(Component.text("Invalid number of arguments. Usage: /phantomisolation <enable/disable>", NamedTextColor.RED));
            }
            return false;
        }
        return true;
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