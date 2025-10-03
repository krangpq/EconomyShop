package com.krangpq.economyshop.commands;

import com.krangpq.economyshop.EconomyShop;
import com.krangpq.economyshop.data.CustomItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * /상점삭제 명령어 - 커스텀 아이템 삭제
 */
public class DeleteItemCommand implements CommandExecutor {

    private final EconomyShop plugin;

    public DeleteItemCommand(EconomyShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 권한 확인
        if (!sender.hasPermission("economyshop.admin")) {
            sender.sendMessage(plugin.getPrefix() + plugin.getMessage("no-permission"));
            return true;
        }

        // /상점삭제 <아이템ID>
        if (args.length == 0) {
            // 등록된 커스텀 아이템 목록 표시
            List<CustomItem> items = plugin.getShopManager().getCustomItems();

            if (items.isEmpty()) {
                sender.sendMessage(plugin.getPrefix() + "§c등록된 커스텀 아이템이 없습니다.");
                return true;
            }

            sender.sendMessage("§6§l━━━━━ 커스텀 아이템 목록 ━━━━━");
            for (CustomItem item : items) {
                String itemName = item.getItem().hasItemMeta() && item.getItem().getItemMeta().hasDisplayName()
                        ? item.getItem().getItemMeta().getDisplayName()
                        : item.getItem().getType().name();

                sender.sendMessage("§eID: §f" + item.getId() + " §7| " + itemName +
                        " §7| §6" + plugin.getEconomyManager().format(item.getPrice()));
            }
            sender.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━");
            sender.sendMessage("§7사용법: /상점삭제 <아이템ID>");

            return true;
        }

        String itemId = args[0];

        // 아이템 삭제
        if (plugin.getShopManager().deleteCustomItem(itemId)) {
            String msg = plugin.getMessage("item-deleted");
            sender.sendMessage(plugin.getPrefix() + msg);
            sender.sendMessage(plugin.getPrefix() + "§7ID: " + itemId);
        } else {
            sender.sendMessage(plugin.getPrefix() + "§c해당 ID의 아이템을 찾을 수 없습니다.");
            sender.sendMessage(plugin.getPrefix() + "§7/상점삭제 §f명령어로 목록을 확인하세요.");
        }

        return true;
    }
}