package com.krangpq.economyshop.commands;

import com.krangpq.economyshop.EconomyShop;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * /송금 명령어 - 플레이어 간 송금
 */
public class PayCommand implements CommandExecutor {

    private final EconomyShop plugin;

    public PayCommand(EconomyShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getMessage("player-only"));
            return true;
        }

        Player player = (Player) sender;

        // /송금 <플레이어> <금액>
        if (args.length < 2) {
            player.sendMessage(plugin.getPrefix() + "§c사용법: /송금 <플레이어> <금액>");
            return true;
        }

        // 대상 플레이어 확인
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("player-not-found"));
            return true;
        }

        // 자기 자신에게 송금 불가
        if (target.equals(player)) {
            player.sendMessage(plugin.getPrefix() + "§c자기 자신에게는 송금할 수 없습니다.");
            return true;
        }

        // 금액 파싱
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("invalid-amount"));
            return true;
        }

        // 음수 금액 확인
        if (amount <= 0) {
            player.sendMessage(plugin.getPrefix() + "§c0보다 큰 금액을 입력해주세요.");
            return true;
        }

        // 잔고 확인
        if (!plugin.getEconomyManager().has(player.getUniqueId(), amount)) {
            String msg = plugin.getMessage("insufficient-balance")
                    .replace("{amount}", plugin.getEconomyManager().format(amount));
            player.sendMessage(plugin.getPrefix() + msg);
            return true;
        }

        // 송금 실행
        if (plugin.getEconomyManager().transfer(player.getUniqueId(), target.getUniqueId(), amount)) {
            String senderMsg = plugin.getMessage("transfer-success")
                    .replace("{player}", target.getName())
                    .replace("{amount}", plugin.getEconomyManager().format(amount));
            player.sendMessage(plugin.getPrefix() + senderMsg);

            String receiverMsg = plugin.getMessage("transfer-received")
                    .replace("{player}", player.getName())
                    .replace("{amount}", plugin.getEconomyManager().format(amount));
            target.sendMessage(plugin.getPrefix() + receiverMsg);
        } else {
            player.sendMessage(plugin.getPrefix() + "§c송금에 실패했습니다.");
        }

        return true;
    }
}