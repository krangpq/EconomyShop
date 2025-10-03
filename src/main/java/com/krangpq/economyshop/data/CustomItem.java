package com.krangpq.economyshop.data;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 관리자가 등록한 커스텀 아이템
 */
public class CustomItem {

    private final String id;
    private final ItemStack item;
    private final double price;
    private final String registeredBy;
    private final long registeredAt;

    public CustomItem(String id, ItemStack item, double price, String registeredBy, long registeredAt) {
        this.id = id;
        this.item = item.clone();
        this.price = price;
        this.registeredBy = registeredBy;
        this.registeredAt = registeredAt;
    }

    public String getId() {
        return id;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public double getPrice() {
        return price;
    }

    public String getRegisteredBy() {
        return registeredBy;
    }

    public long getRegisteredAt() {
        return registeredAt;
    }

    /**
     * GUI에 표시할 아이템 생성
     */
    public ItemStack toGuiItem() {
        ItemStack guiItem = item.clone();
        ItemMeta meta = guiItem.getItemMeta();

        if (meta != null) {
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add("§7━━━━━━━━━━━━━━━━");
            lore.add("§e가격: §6" + String.format("%,.0f원", price));
            lore.add("§a좌클릭: §71개 구매");
            lore.add("§a우클릭: §764개 구매");
            lore.add("§7━━━━━━━━━━━━━━━━");
            lore.add("§d[커스텀 아이템]");
            lore.add("§8등록자: " + registeredBy);

            meta.setLore(lore);
            guiItem.setItemMeta(meta);
        }

        return guiItem;
    }

    /**
     * 설정 파일로 저장
     */
    public void saveToFile(File folder) {
        File file = new File(folder, id + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        config.set("item", item);
        config.set("price", price);
        config.set("registered-by", registeredBy);
        config.set("registered-at", registeredAt);

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 설정 파일에서 로드
     */
    public static CustomItem fromConfig(String id, YamlConfiguration config) {
        try {
            ItemStack item = config.getItemStack("item");
            double price = config.getDouble("price");
            String registeredBy = config.getString("registered-by", "Unknown");
            long registeredAt = config.getLong("registered-at", System.currentTimeMillis());

            if (item == null) {
                return null;
            }

            return new CustomItem(id, item, price, registeredBy, registeredAt);
        } catch (Exception e) {
            return null;
        }
    }
}