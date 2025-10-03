package com.krangpq.economyshop.commands;

import com.krangpq.economyshop.EconomyShop;
import com.krangpq.economyshop.data.CustomItem;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.UUID;

/**
 * /상점등록 명령어 - 커스텀 아이템 등록
 */
public class RegisterItemCommand implements CommandExecutor {

    private final EconomyShop plugin;

    public RegisterItemCommand(EconomyShop plugin) {
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
        if (!player.hasPermission("economyshop.admin")) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("no-permission"));
            return true;
        }

        // 사용법 확인
        if (args.length < 1) {
            player.sendMessage(plugin.getPrefix() + "§c사용법: /상점등록 <가격>");
            return true;
        }

        // 손에 든 아이템 확인
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("no-item-in-hand"));
            return true;
        }

        // 가격 파싱
        double price;
        try {
            price = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getPrefix() + plugin.getMessage("invalid-amount"));
            return true;
        }

        if (price <= 0) {
            player.sendMessage(plugin.getPrefix() + "§c0보다 큰 가격을 입력해주세요.");
            return true;
        }

        // 고유 ID 생성
        String itemId = UUID.randomUUID().toString().substring(0, 8);

        // 커스텀 아이템 생성
        CustomItem customItem = new CustomItem(
                itemId,
                item.clone(),
                price,
                player.getName(),
                System.currentTimeMillis()
        );

        // 파일로 저장
        File customFolder = new File(plugin.getDataFolder(), "custom-items");
        customItem.saveToFile(customFolder);

        // 매니저에 추가
        plugin.getShopManager().addCustomItem(customItem);

        // 성공 메시지
        String itemName = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                ? item.getItemMeta().getDisplayName()
                : item.getType().name();

        String msg = plugin.getMessage("item-registered")
                .replace("{id}", itemId);
        player.sendMessage(plugin.getPrefix() + msg);
        player.sendMessage(plugin.getPrefix() + "§7아이템: " + itemName);
        player.sendMessage(plugin.getPrefix() + "§7가격: §6" + plugin.getEconomyManager().format(price));

        return true;
    }
}