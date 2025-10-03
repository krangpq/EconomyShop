package com.krangpq.economyshop.data;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * 상점 판매 아이템 정보
 */
public class ShopItem {

    private final String id;
    private final Material material;
    private final String name;
    private final double buyPrice;
    private final double sellPrice;
    private final List<String> lore;

    public ShopItem(String id, String materialName, String name, double buyPrice, double sellPrice, List<String> lore) {
        this.id = id;
        this.material = Material.valueOf(materialName.toUpperCase());
        this.name = name;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.lore = lore != null ? lore : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public Material getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public List<String> getLore() {
        return new ArrayList<>(lore);
    }

    /**
     * 구매 가능 여부
     */
    public boolean canBuy() {
        return buyPrice > 0;
    }

    /**
     * 판매 가능 여부
     */
    public boolean canSell() {
        return sellPrice > 0;
    }

    /**
     * GUI에 표시할 아이템 생성
     */
    public ItemStack toGuiItem() {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);

            List<String> itemLore = new ArrayList<>();
            itemLore.add("§7━━━━━━━━━━━━━━━━");

            if (!lore.isEmpty()) {
                itemLore.addAll(lore);
                itemLore.add("§8");
            }

            if (canBuy()) {
                itemLore.add("§e구매가: §6" + String.format("%,.0f원", buyPrice));
                itemLore.add("§a좌클릭: §71개 구매");
                itemLore.add("§a우클릭: §764개 구매");
            }

            if (canSell()) {
                itemLore.add("§8");
                itemLore.add("§e판매가: §6" + String.format("%,.0f원", sellPrice));
                itemLore.add("§aShift+좌클릭: §7해당 슬롯 판매");
                itemLore.add("§aShift+우클릭: §7전체 판매");
            }

            itemLore.add("§7━━━━━━━━━━━━━━━━");

            meta.setLore(itemLore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * 실제 아이템 생성 (구매시)
     */
    public ItemStack createItem(int amount) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(name);
            if (!lore.isEmpty()) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }

        return item;
    }
}