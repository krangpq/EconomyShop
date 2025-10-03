package com.krangpq.economyshop.commands;

import com.krangpq.economyshop.EconomyShop;
import com.krangpq.economyshop.gui.MainShopGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /상점 명령어
 */
public class ShopCommand implements CommandExecutor {

    private final EconomyShop plugin;

    public ShopCommand(EconomyShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("player-only"));
            return true;
        }

        Player player = (Player) sender;

        // 권한 확인
        if (!player.hasPermission("economyshop.use")) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("no-permission"));
            return true;
        }

        // 메인 상점 GUI 열기 (기존 인스턴스 사용)
        plugin.getMainShopGui().open(player);

        return true;
    }
}