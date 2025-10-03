package com.krangpq.economyshop.gui;

import com.krangpq.economyshop.EconomyShop;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * 메인 상점 GUI (카테고리 선택)
 */
public class MainShopGui implements Listener {

    private final EconomyShop plugin;

    public MainShopGui(EconomyShop plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * 메인 메뉴 열기
     */
    public void open(Player player) {
        // 세션 확인
        if (plugin.getGuiSession().hasActiveSession(player)) {
            player.sendMessage(plugin.getPrefix() + "§c이미 다른 GUI를 사용중입니다.");
            return;
        }

        // 세션 시작
        plugin.getGuiSession().startSession(player, "main_shop");

        // GUI 생성
        String title = plugin.getConfig().getString("shop.gui-title", "§6§l━━━━━ 상점 ━━━━━");
        Inventory inv = Bukkit.createInventory(null, 27, title);

        // 배경 채우기
        ItemStack glass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ", null);
        for (int i = 0; i < 27; i++) {
            inv.setItem(i, glass);
        }

        // 카테고리 아이콘 배치
        inv.setItem(11, createCategoryIcon("crops"));
        inv.setItem(13, createCategoryIcon("minerals"));
        inv.setItem(15, createCategoryIcon("special"));

        player.openInventory(inv);
    }

    /**
     * 카테고리 아이콘 생성
     */
    private ItemStack createCategoryIcon(String category) {
        String path = "shop.categories." + category + ".";
        String name = plugin.getConfig().getString(path + "name", category);
        String materialName = plugin.getConfig().getString(path + "icon", "STONE");

        Material material = Material.valueOf(materialName);

        return createItem(material, name, Arrays.asList(
                "§7━━━━━━━━━━━━━━━━",
                "§e클릭하여 입장",
                "§7━━━━━━━━━━━━━━━━"
        ));
    }

    /**
     * 아이템 생성 헬퍼
     */
    private ItemStack createItem(Material material, String name, java.util.List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);
            if (lore != null) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * GUI 클릭 이벤트
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();

        // 세션 확인
        if (!plugin.getGuiSession().isSessionType(player, "main_shop")) {
            return;
        }

        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // 배경 아이템 무시
        if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        // 슬롯으로 카테고리 판별
        String category = null;
        switch (e.getSlot()) {
            case 11:
                category = "crops";
                break;
            case 13:
                category = "minerals";
                break;
            case 15:
                category = "special";
                break;
        }

        if (category != null) {
            player.closeInventory();
            plugin.getGuiSession().endSession(player);

            // 카테고리 상점 열기
            CategoryShopGui categoryGui = new CategoryShopGui(plugin);
            categoryGui.open(player, category);
        }
    }

    /**
     * GUI 닫기 이벤트
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player player = (Player) e.getPlayer();

        if (plugin.getGuiSession().isSessionType(player, "main_shop")) {
            plugin.getGuiSession().endSession(player);
        }
    }
}