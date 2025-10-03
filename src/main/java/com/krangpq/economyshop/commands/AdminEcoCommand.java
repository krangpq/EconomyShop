package com.krangpq.economyshop.commands;

import com.krangpq.economyshop.EconomyShop;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * /경제관리 명령어 - 관리자 전용
 */
public class AdminEcoCommand implements CommandExecutor {

    private final EconomyShop plugin;

    public AdminEcoCommand(EconomyShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 권한 확인
        if (!sender.hasPermission("economyshop.admin")) {
            sender.sendMessage(plugin.getPrefix() + plugin.getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "지급":
            case "give":
                return handleGive(sender, args);

            case "차감":
            case "take":
                return handleTake(sender, args);

            case "설정":
            case "set":
                return handleSet(sender, args);

            case "확인":
            case "check":
                return handleCheck(sender, args);

            case "리로드":
            case "reload":
                return handleReload(sender);

            default:
                sendHelp(sender);
                return true;
        }
    }

    /**
     * 돈 지급
     */
    private boolean handleGive(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.getPrefix() + "§c사용법: /경제관리 지급 <플레이어> <금액>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

        try {
            double amount = Double.parseDouble(args[2]);

            if (amount <= 0) {
                sender.sendMessage(plugin.getPrefix() + "§c0보다 큰 금액을 입력해주세요.");
                return true;
            }

            if (plugin.getEconomyManager().deposit(target.getUniqueId(), amount)) {
                sender.sendMessage(plugin.getPrefix() + "§a" + target.getName() + "님에게 " +
                        plugin.getEconomyManager().format(amount) + "을(를) 지급했습니다.");

                if (target.isOnline()) {
                    target.getPlayer().sendMessage(plugin.getPrefix() + "§a관리자로부터 " +
                            plugin.getEconomyManager().format(amount) + "을(를) 받았습니다.");
                }
            } else {
                sender.sendMessage(plugin.getPrefix() + "§c지급에 실패했습니다. (최대 잔고 초과)");
            }

        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getPrefix() + plugin.getMessage("invalid-amount"));
        }

        return true;
    }

    /**
     * 돈 차감
     */
    private boolean handleTake(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.getPrefix() + "§c사용법: /경제관리 차감 <플레이어> <금액>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

        try {
            double amount = Double.parseDouble(args[2]);

            if (amount <= 0) {
                sender.sendMessage(plugin.getPrefix() + "§c0보다 큰 금액을 입력해주세요.");
                return true;
            }

            if (plugin.getEconomyManager().withdraw(target.getUniqueId(), amount)) {
                sender.sendMessage(plugin.getPrefix() + "§a" + target.getName() + "님의 잔고에서 " +
                        plugin.getEconomyManager().format(amount) + "을(를) 차감했습니다.");

                if (target.isOnline()) {
                    target.getPlayer().sendMessage(plugin.getPrefix() + "§c관리자에 의해 " +
                            plugin.getEconomyManager().format(amount) + "이(가) 차감되었습니다.");
                }
            } else {
                sender.sendMessage(plugin.getPrefix() + "§c차감에 실패했습니다. (잔고 부족)");
            }

        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getPrefix() + plugin.getMessage("invalid-amount"));
        }

        return true;
    }

    /**
     * 잔고 설정
     */
    private boolean handleSet(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.getPrefix() + "§c사용법: /경제관리 설정 <플레이어> <금액>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

        try {
            double amount = Double.parseDouble(args[2]);

            if (amount < 0) {
                sender.sendMessage(plugin.getPrefix() + "§c0 이상의 금액을 입력해주세요.");
                return true;
            }

            plugin.getEconomyManager().setBalance(target.getUniqueId(), amount);
            sender.sendMessage(plugin.getPrefix() + "§a" + target.getName() + "님의 잔고를 " +
                    plugin.getEconomyManager().format(amount) + "(으)로 설정했습니다.");

            if (target.isOnline()) {
                target.getPlayer().sendMessage(plugin.getPrefix() + "§e잔고가 " +
                        plugin.getEconomyManager().format(amount) + "(으)로 변경되었습니다.");
            }

        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getPrefix() + plugin.getMessage("invalid-amount"));
        }

        return true;
    }

    /**
     * 잔고 확인
     */
    private boolean handleCheck(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getPrefix() + "§c사용법: /경제관리 확인 <플레이어>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        double balance = plugin.getEconomyManager().getBalance(target.getUniqueId());

        sender.sendMessage(plugin.getPrefix() + "§e" + target.getName() + "님의 잔고: §6" +
                plugin.getEconomyManager().format(balance));

        return true;
    }

    /**
     * 플러그인 리로드
     */
    private boolean handleReload(CommandSender sender) {
        plugin.reloadConfig();
        plugin.getShopManager().reload();

        sender.sendMessage(plugin.getPrefix() + "§a플러그인이 리로드되었습니다!");

        return true;
    }

    /**
     * 도움말 표시
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6§l━━━━━ 경제 관리 명령어 ━━━━━");
        sender.sendMessage("§e/경제관리 지급 <플레이어> <금액> §7- 돈 지급");
        sender.sendMessage("§e/경제관리 차감 <플레이어> <금액> §7- 돈 차감");
        sender.sendMessage("§e/경제관리 설정 <플레이어> <금액> §7- 잔고 설정");
        sender.sendMessage("§e/경제관리 확인 <플레이어> §7- 잔고 확인");
        sender.sendMessage("§e/경제관리 리로드 §7- 플러그인 리로드");
        sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━");
    }
}