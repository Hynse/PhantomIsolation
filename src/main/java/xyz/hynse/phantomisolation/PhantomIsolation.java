package xyz.hynse.phantomisolation;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PhantomIsolation extends JavaPlugin implements Listener {

    private Scoreboard scoreboard;
    private Objective objective;
    private boolean enabled;

    @Override
    public void onEnable() {
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        scoreboard = scoreboardManager.getMainScoreboard();
        objective = scoreboard.registerNewObjective("pisoUsed", "dummy", "pisoUsed");
        enabled = true;

        getServer().getPluginManager().registerEvents(this, this);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : getServer().getOnlinePlayers()) {
                    Score score = objective.getScore(player.getName());
                    int timeSinceRest = player.getStatistic(Statistic.TIME_SINCE_REST);

                    if (score.getScore() == 1) {
                        enabled = true;
                        player.sendMessage(Component.text("Phantom spawning enabled.", NamedTextColor.GREEN));
                    } else if (score.getScore() == -1) {
                        enabled = false;
                        player.sendMessage(Component.text("Phantom spawning disabled.", NamedTextColor.RED));
                    }

                    if (enabled && timeSinceRest >= 24000) {
                        player.setStatistic(Statistic.TIME_SINCE_REST, 0);
                    }
                }
            }
        }.runTaskTimer(this, 24000L, 20L); // run every day (24000 ticks) with a period of 20 ticks (1 second)
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
                sender.sendMessage(Component.text("Phantom spawning is currently " + (enabled ? "enabled" : "disabled") + ".", enabled ? NamedTextColor.GREEN : NamedTextColor.RED));
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("true")) {
                    if (sender instanceof Player player) {
                        Objects.requireNonNull(scoreboard.getObjective("pisoUsed")).getScore(player.getName()).setScore(1);
                        player.sendMessage(Component.text("Phantom spawning enabled.", NamedTextColor.GREEN));
                    } else {
                        sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
                    }
                } else if (args[0].equalsIgnoreCase("false")) {
                    if (sender instanceof Player player) {
                        Objects.requireNonNull(scoreboard.getObjective("pisoUsed")).getScore(player.getName()).setScore(-1);
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
}
