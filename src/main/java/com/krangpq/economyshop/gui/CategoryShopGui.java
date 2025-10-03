package com.krangpq.economyshop.gui;

import com.krangpq.economyshop.EconomyShop;
import com.krangpq.economyshop.data.CustomItem;
import com.krangpq.economyshop.data.ShopItem;
import com.krangpq.economyshop.integration.ToolEnhancerIntegration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 카테고리별 상점 GUI
 */
public class CategoryShopGui implements Listener {

    private final EconomyShop plugin;
    private final Map<String, String> playerCategory; // 플레이어가 보고 있는 카테고리

    public CategoryShopGui(EconomyShop plugin) {
        this.plugin = plugin;
        this.playerCategory = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * 카테고리 상점 열기
     */
    public void open(Player player, String category) {
        // 세션 확인
        if (plugin.getGuiSession().hasActiveSession(player)) {
            player.sendMessage(plugin.getPrefix() + "§c이미 다른 GUI를 사용중입니다.");
            return;
        }

        // 세션 시작
        plugin.getGuiSession().startSession(player, "category_shop");
        playerCategory.put(player.getName(), category);

        // GUI 생성
        String categoryName = plugin.getConfig().getString("shop.categories." + category + ".name", category);
        String title = plugin.getConfig().getString("shop.category-title", "§6§l{category} 상점")
                .replace("{category}", categoryName);

        Inventory inv = Bukkit.createInventory(null, 54, title);

        int slot = 0;

        // 1. 기본 상점 아이템 로드
        for (ShopItem item : plugin.getShopManager().getItems(category)) {
            if (slot >= 45) break; // 하단 9칸은 네비게이션용
            inv.setItem(slot++, item.toGuiItem());
        }

        // 2. 특별 아이템 카테고리인 경우 추가 아이템 표시
        if (category.equals("special")) {
            // 커스텀 아이템
            for (CustomItem custom : plugin.getShopManager().getCustomItems()) {
                if (slot >= 45) break;
                inv.setItem(slot++, custom.toGuiItem());
            }

            // ToolEnhancer 연동 아이템
            if (plugin.getIntegrationManager().hasToolEnhancer()) {
                ToolEnhancerIntegration te = plugin.getIntegrationManager().getToolEnhancer();
                ItemStack stone = te.getEnhancementStone();

                if (stone != null && slot < 45) {
                    double price = plugin.getConfig().getDouble("integrations.toolenhancer.items.enhancement-stone.price", 5000);
                    inv.setItem(slot++, createShopItem(stone, price, true));
                }
            }
        }

        // 하단 네비게이션
        ItemStack back = createItem(Material.ARROW, "§c뒤로 가기", List.of("§7메인 메뉴로 돌아갑니다."));
        inv.setItem(49, back);

        player.openInventory(inv);
    }

    /**
     * ToolEnhancer 강화석을 상점 아이템으로 변환
     */
    private ItemStack createShopItem(ItemStack original, double price, boolean isIntegration) {
        ItemStack item = original.clone();
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add("§7━━━━━━━━━━━━━━━━");
            lore.add("§e가격: §6" + String.format("%,.0f원", price));
            lore.add("§a좌클릭: §71개 구매");
            lore.add("§a우클릭: §764개 구매");
            lore.add("§7━━━━━━━━━━━━━━━━");
            if (isIntegration) {
                lore.add("§d[ToolEnhancer 연동]");
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * 아이템 생성 헬퍼
     */
    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
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
        if (!plugin.getGuiSession().isSessionType(player, "category_shop")) {
            return;
        }

        // 클릭한 인벤토리가 상점 GUI인지 확인
        if (e.getClickedInventory() == null) {
            return;
        }

        // 플레이어 자신의 인벤토리를 클릭한 경우
        if (e.getClickedInventory().equals(player.getInventory())) {
            // Shift+우클릭만 판매 가능
            if (e.isShiftClick() && e.isRightClick()) {
                ItemStack clicked = e.getCurrentItem();
                if (clicked != null && clicked.getType() != Material.AIR) {
                    String category = playerCategory.get(player.getName());
                    if (category != null) {
                        e.setCancelled(true);
                        handleSellAll(player, clicked.getType(), category);
                    }
                }
            }
            // 다른 클릭은 모두 허용 (아이템 이동 가능)
            return;
        }

        // 여기서부터는 상점 GUI 클릭만 처리
        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // 뒤로 가기 버튼
        if (e.getSlot() == 49 && clicked.getType() == Material.ARROW) {
            player.closeInventory();
            plugin.getGuiSession().endSession(player);
            playerCategory.remove(player.getName());

            plugin.getMainShopGui().open(player); // ← 수정
            return;
        }

        // 슬롯 45 이상은 네비게이션 영역 (처리 안 함)
        if (e.getSlot() >= 45) {
            return;
        }

        String category = playerCategory.get(player.getName());
        if (category == null) return;

        // 좌클릭: 1개 구매
        if (e.getClick().toString().equals("LEFT")) {
            handlePurchase(player, clicked, 1, category);
        }
        // 우클릭: 64개 구매
        else if (e.getClick().toString().equals("RIGHT")) {
            handlePurchase(player, clicked, 64, category);
        }
        // Shift+좌클릭: 해당 아이템 전체 판매 (구현 안 함)
        // Shift+우클릭: 상점 GUI에서는 동작 안 함
    }

    /**
     * 구매 처리
     */
    private void handlePurchase(Player player, ItemStack displayItem, int amount, String category) {
        // 상점 아이템 찾기
        ShopItem shopItem = findShopItem(displayItem, category);
        if (shopItem == null) {
            // 커스텀 아이템 또는 연동 아이템 확인
            handleSpecialPurchase(player, displayItem, amount);
            return;
        }

        if (!shopItem.canBuy()) {
            player.sendMessage(plugin.getPrefix() + "§c이 아이템은 구매할 수 없습니다.");
            playSound(player, "error");
            return;
        }

        double totalPrice = shopItem.getBuyPrice() * amount;

        // 잔고 확인
        if (!plugin.getEconomyManager().has(player.getUniqueId(), totalPrice)) {
            String msg = plugin.getMessage("insufficient-balance")
                    .replace("{amount}", plugin.getEconomyManager().format(totalPrice));
            player.sendMessage(plugin.getPrefix() + msg);
            playSound(player, "error");
            return;
        }

        // 인벤토리 공간 확인
        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(plugin.getPrefix() + "§c인벤토리 공간이 부족합니다.");
            playSound(player, "error");
            return;
        }

        // 구매 실행
        if (plugin.getEconomyManager().withdraw(player.getUniqueId(), totalPrice)) {
            player.getInventory().addItem(shopItem.createItem(amount));

            String msg = plugin.getMessage("purchase-success")
                    .replace("{item}", shopItem.getName())
                    .replace("{amount}", String.valueOf(amount))
                    .replace("{price}", plugin.getEconomyManager().format(totalPrice));
            player.sendMessage(plugin.getPrefix() + msg);
            playSound(player, "buy");
        }
    }

    /**
     * 특별 아이템 구매 (커스텀/연동)
     */
    private void handleSpecialPurchase(Player player, ItemStack displayItem, int amount) {
        // 1. 커스텀 아이템 확인 (먼저 체크)
        for (CustomItem custom : plugin.getShopManager().getCustomItems()) {
            ItemStack customItem = custom.getItem();

            // Material과 DisplayName으로 비교
            if (customItem.getType() == displayItem.getType()) {
                if (customItem.hasItemMeta() && displayItem.hasItemMeta()) {
                    if (customItem.getItemMeta().getDisplayName()
                            .equals(displayItem.getItemMeta().getDisplayName())) {

                        double totalPrice = custom.getPrice() * amount;

                        if (!plugin.getEconomyManager().has(player.getUniqueId(), totalPrice)) {
                            String msg = plugin.getMessage("insufficient-balance")
                                    .replace("{amount}", plugin.getEconomyManager().format(totalPrice));
                            player.sendMessage(plugin.getPrefix() + msg);
                            playSound(player, "error");
                            return;
                        }

                        if (plugin.getEconomyManager().withdraw(player.getUniqueId(), totalPrice)) {
                            ItemStack item = custom.getItem().clone();

                            // GUI용 Lore 제거
                            ItemMeta meta = item.getItemMeta();
                            if (meta != null && meta.hasLore()) {
                                List<String> lore = meta.getLore();
                                List<String> cleanLore = new ArrayList<>();

                                for (String line : lore) {
                                    if (!line.contains("가격:") &&
                                            !line.contains("클릭:") &&
                                            !line.contains("━━━") &&
                                            !line.contains("[커스텀 아이템]") &&
                                            !line.contains("등록자:")) {
                                        cleanLore.add(line);
                                    }
                                }

                                meta.setLore(cleanLore.isEmpty() ? null : cleanLore);
                                item.setItemMeta(meta);
                            }

                            item.setAmount(amount);
                            player.getInventory().addItem(item);

                            String itemName = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                                    ? item.getItemMeta().getDisplayName()
                                    : item.getType().name();

                            String msg = plugin.getMessage("purchase-success")
                                    .replace("{item}", itemName)
                                    .replace("{amount}", String.valueOf(amount))
                                    .replace("{price}", plugin.getEconomyManager().format(totalPrice));
                            player.sendMessage(plugin.getPrefix() + msg);
                            playSound(player, "buy");
                        }
                        return; // 구매 완료, 종료
                    }
                }
            }
        }

        // 2. ToolEnhancer 강화석 확인
        if (plugin.getIntegrationManager().hasToolEnhancer()) {
            // 고유 태그로 강화석인지 확인
            if (displayItem.hasItemMeta() && displayItem.getItemMeta().hasLore()) {
                List<String> lore = displayItem.getItemMeta().getLore();
                boolean isEnhancementStone = false;

                for (String line : lore) {
                    if (line.contains("[ENHANCEMENT_STONE]")) {
                        isEnhancementStone = true;
                        break;
                    }
                }

                if (isEnhancementStone) {
                    ToolEnhancerIntegration te = plugin.getIntegrationManager().getToolEnhancer();
                    double price = plugin.getConfig().getDouble("integrations.toolenhancer.items.enhancement-stone.price", 5000);
                    double totalPrice = price * amount;

                    if (!plugin.getEconomyManager().has(player.getUniqueId(), totalPrice)) {
                        String msg = plugin.getMessage("insufficient-balance")
                                .replace("{amount}", plugin.getEconomyManager().format(totalPrice));
                        player.sendMessage(plugin.getPrefix() + msg);
                        playSound(player, "error");
                        return;
                    }

                    if (plugin.getEconomyManager().withdraw(player.getUniqueId(), totalPrice)) {
                        // 순수한 강화석을 다시 생성 (GUI용이 아닌 원본)
                        ItemStack stone = te.getEnhancementStone();

                        // GUI용 Lore 제거 - 원본 강화석만 남김
                        ItemMeta meta = stone.getItemMeta();
                        if (meta != null && meta.hasLore()) {
                            lore = meta.getLore();
                            List<String> cleanLore = new ArrayList<>();

                            // 원본 Lore만 유지 (가격, 클릭 설명 제외)
                            for (String line : lore) {
                                if (!line.contains("가격:") &&
                                        !line.contains("클릭:") &&
                                        !line.contains("━━━") &&
                                        !line.contains("[ToolEnhancer 연동]") &&
                                        !line.contains("[ENHANCEMENT_STONE]")) {
                                    cleanLore.add(line);
                                }
                            }

                            // [ENHANCEMENT_STONE] 태그는 다시 추가 (숨겨진 식별용)
                            cleanLore.add("§8§l[ENHANCEMENT_STONE]");

                            meta.setLore(cleanLore);
                            stone.setItemMeta(meta);
                        }

                        stone.setAmount(amount);
                        player.getInventory().addItem(stone);

                        String msg = plugin.getMessage("purchase-success")
                                .replace("{item}", "§e⚒ 강화석")
                                .replace("{amount}", String.valueOf(amount))
                                .replace("{price}", plugin.getEconomyManager().format(totalPrice));
                        player.sendMessage(plugin.getPrefix() + msg);
                        playSound(player, "buy");
                        return;
                    }
                }
            }
        }

        // 3. 아무것도 매칭되지 않음
        player.sendMessage(plugin.getPrefix() + "§c이 아이템은 구매할 수 없습니다.");
        playSound(player, "error");
    }

    /**
     * 판매 처리
     */
    private void handleSell(Player player, ItemStack item, int amount, String category) {
        ShopItem shopItem = findShopItemByMaterial(item.getType(), category);

        if (shopItem == null || !shopItem.canSell()) {
            player.sendMessage(plugin.getPrefix() + "§c이 아이템은 판매할 수 없습니다.");
            playSound(player, "error");
            return;
        }

        // 플레이어 인벤토리에서 아이템 제거
        int removed = removeItems(player, item.getType(), amount);

        if (removed == 0) {
            player.sendMessage(plugin.getPrefix() + "§c판매할 아이템이 없습니다.");
            playSound(player, "error");
            return;
        }

        double totalPrice = shopItem.getSellPrice() * removed;
        plugin.getEconomyManager().deposit(player.getUniqueId(), totalPrice);

        String msg = plugin.getMessage("sell-success")
                .replace("{item}", shopItem.getName())
                .replace("{amount}", String.valueOf(removed))
                .replace("{price}", plugin.getEconomyManager().format(totalPrice));
        player.sendMessage(plugin.getPrefix() + msg);
        playSound(player, "sell");
    }

    /**
     * 전체 판매 처리
     */
    private void handleSellAll(Player player, Material material, String category) {
        ShopItem shopItem = findShopItemByMaterial(material, category);

        if (shopItem == null || !shopItem.canSell()) {
            player.sendMessage(plugin.getPrefix() + "§c이 아이템은 판매할 수 없습니다.");
            playSound(player, "error");
            return;
        }

        int totalAmount = countItems(player, material);

        if (totalAmount == 0) {
            player.sendMessage(plugin.getPrefix() + "§c판매할 아이템이 없습니다.");
            playSound(player, "error");
            return;
        }

        int removed = removeItems(player, material, totalAmount);
        double totalPrice = shopItem.getSellPrice() * removed;
        plugin.getEconomyManager().deposit(player.getUniqueId(), totalPrice);

        String msg = plugin.getMessage("sell-success")
                .replace("{item}", shopItem.getName())
                .replace("{amount}", String.valueOf(removed))
                .replace("{price}", plugin.getEconomyManager().format(totalPrice));
        player.sendMessage(plugin.getPrefix() + msg);
        playSound(player, "sell");
    }

    /**
     * 상점 아이템 찾기
     */
    private ShopItem findShopItem(ItemStack displayItem, String category) {
        for (ShopItem item : plugin.getShopManager().getItems(category)) {
            if (item.getMaterial() == displayItem.getType()) {
                return item;
            }
        }
        return null;
    }

    /**
     * Material로 상점 아이템 찾기
     */
    private ShopItem findShopItemByMaterial(Material material, String category) {
        for (ShopItem item : plugin.getShopManager().getItems(category)) {
            if (item.getMaterial() == material) {
                return item;
            }
        }
        return null;
    }

    /**
     * 인벤토리에서 아이템 개수 세기
     */
    private int countItems(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    /**
     * 인벤토리에서 아이템 제거
     */
    private int removeItems(Player player, Material material, int amount) {
        int remaining = amount;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                int itemAmount = item.getAmount();

                if (itemAmount <= remaining) {
                    remaining -= itemAmount;
                    player.getInventory().remove(item);
                } else {
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                    break;
                }

                if (remaining == 0) break;
            }
        }

        return amount - remaining;
    }

    /**
     * 사운드 재생
     */
    private void playSound(Player player, String type) {
        String soundName = plugin.getConfig().getString("transaction." + type + "-sound", "");
        if (soundName.isEmpty()) return;

        try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException e) {
            // 사운드 이름이 잘못된 경우 무시
        }
    }

    /**
     * GUI 닫기 이벤트
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player player = (Player) e.getPlayer();

        if (plugin.getGuiSession().isSessionType(player, "category_shop")) {
            plugin.getGuiSession().endSession(player);
            playerCategory.remove(player.getName());
        }
    }
}