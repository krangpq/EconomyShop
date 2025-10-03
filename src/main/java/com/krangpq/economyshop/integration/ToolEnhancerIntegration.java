package com.krangpq.economyshop.integration;

import com.krangpq.toolenhancer.api.ToolEnhancerAPI;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public class ToolEnhancerIntegration {

    private final boolean enabled;

    public ToolEnhancerIntegration() {
        boolean temp = false;

        if (Bukkit.getPluginManager().getPlugin("ToolEnhancer") != null) {
            try {
                temp = ToolEnhancerAPI.isEnabled();
            } catch (Exception e) {
                temp = false;
            }
        }

        this.enabled = temp;  // 한 번만 할당
    }

    public boolean isEnabled() {
        return enabled;
    }

    public ItemStack getEnhancementStone() {
        if (!enabled) {
            return null;
        }

        try {
            return ToolEnhancerAPI.createEnhancementStone();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isEnhancementStone(ItemStack item) {
        if (!enabled || item == null) {
            return false;
        }

        try {
            return ToolEnhancerAPI.isEnhanceStone(item);
        } catch (Exception e) {
            return false;
        }
    }
}