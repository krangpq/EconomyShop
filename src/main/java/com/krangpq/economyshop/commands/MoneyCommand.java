package com.krangpq.economyshop.commands;

import com.krangpq.economyshop.EconomyShop;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /돈 명령어 - 잔고 확인
 */
public class MoneyCommand implements CommandExecutor {

    private final EconomyShop plugin;

    public MoneyCommand(EconomyShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("player-only"));
            return true;
        }

        Player player = (Player) sender;

        // /돈 [플레이어] - 다른 플레이어 잔고 확인
        if (args.length > 0) {
            if (!player.hasPermission("economyshop.admin")) {
                player.sendMessage(plugin.getPrefix() + plugin.getMessage("no-permission"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(plugin.getPrefix() + plugin.getMessage("player-not-found"));
                return true;
            }

            double balance = plugin.getEconomyManager().getBalance(target.getUniqueId());
            player.sendMessage(plugin.getPrefix() + "§e" + target.getName() + "님의 잔고: §6" +
                    plugin.getEconomyManager().format(balance));
            return true;
        }

        // /돈 - 자신의 잔고 확인
        double balance = plugin.getEconomyManager().getBalance(player.getUniqueId());
        String msg = plugin.getMessage("balance-check")
                .replace("{balance}", plugin.getEconomyManager().format(balance));
        player.sendMessage(plugin.getPrefix() + msg);

        return true;
    }
}