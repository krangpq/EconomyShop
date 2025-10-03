package com.krangpq.economyshop.managers;

import com.krangpq.economyshop.EconomyShop;
import com.krangpq.economyshop.data.CustomItem;
import com.krangpq.economyshop.data.ShopItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class ShopManager {

    private final EconomyShop plugin;
    private final Map<String, List<ShopItem>> shopItems;
    private final List<CustomItem> customItems;

    public ShopManager(EconomyShop plugin) {
        this.plugin = plugin;
        this.shopItems = new HashMap<>();
        this.customItems = new ArrayList<>();

        loadShopItems();
        loadCustomItems();
    }

    /**
     * 상점 아이템 로드
     */
    private void loadShopItems() {
        File shopsFolder = new File(plugin.getDataFolder(), "shops");
        String[] categories = {"crops", "minerals", "special"};

        for (String category : categories) {
            File shopFile = new File(shopsFolder, category + ".yml");
            if (!shopFile.exists()) {
                plugin.getLogger().warning(category + ".yml 파일을 찾을 수 없습니다.");
                continue;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(shopFile);
            ConfigurationSection itemsSection = config.getConfigurationSection("items");

            if (itemsSection == null) {
                continue;
            }

            List<ShopItem> items = new ArrayList<>();

            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                if (itemSection == null) continue;

                try {
                    ShopItem item = new ShopItem(
                            key,
                            itemSection.getString("material"),
                            itemSection.getString("name"),
                            itemSection.getDouble("buy-price", 0),
                            itemSection.getDouble("sell-price", 0),
                            itemSection.getStringList("lore")
                    );
                    items.add(item);
                } catch (Exception e) {
                    plugin.getLogger().warning("아이템 로드 실패: " + category + "/" + key);
                    e.printStackTrace();
                }
            }

            shopItems.put(category, items);
            plugin.getLogger().info(category + " 상점: " + items.size() + "개 아이템 로드");
        }
    }

    /**
     * 커스텀 아이템 로드
     */
    private void loadCustomItems() {
        File customFolder = new File(plugin.getDataFolder(), "custom-items");
        if (!customFolder.exists()) {
            customFolder.mkdirs();
            return;
        }

        File[] files = customFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                CustomItem item = CustomItem.fromConfig(file.getName().replace(".yml", ""), config);
                if (item != null) {
                    customItems.add(item);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("커스텀 아이템 로드 실패: " + file.getName());
                e.printStackTrace();
            }
        }

        plugin.getLogger().info("커스텀 아이템 " + customItems.size() + "개 로드");
    }

    /**
     * 카테고리별 아이템 가져오기
     */
    public List<ShopItem> getItems(String category) {
        return shopItems.getOrDefault(category, new ArrayList<>());
    }

    /**
     * 모든 커스텀 아이템 가져오기
     */
    public List<CustomItem> getCustomItems() {
        return new ArrayList<>(customItems);
    }

    /**
     * 커스텀 아이템 추가
     */
    public void addCustomItem(CustomItem item) {
        customItems.add(item);
    }

    /**
     * 커스텀 아이템 삭제
     */
    public boolean deleteCustomItem(String itemId) {
        boolean removed = customItems.removeIf(item -> item.getId().equals(itemId));

        if (removed) {
            File itemFile = new File(plugin.getDataFolder(), "custom-items/" + itemId + ".yml");
            if (itemFile.exists()) {
                itemFile.delete();
            }
        }

        return removed;
    }

    /**
     * ID로 커스텀 아이템 찾기
     */
    public CustomItem getCustomItem(String itemId) {
        return customItems.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 상점 리로드
     */
    public void reload() {
        shopItems.clear();
        customItems.clear();
        loadShopItems();
        loadCustomItems();
    }
}