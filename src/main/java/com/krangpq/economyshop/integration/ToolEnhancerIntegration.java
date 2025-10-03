package com.krangpq.economyshop.integration;

import com.krangpq.toolenhancer.api.ToolEnhancerAPI;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

/**
 * ToolEnhancer 플러그인 연동
 *
 * 이 클래스에서만 ToolEnhancer API를 import합니다.
 * 메인 클래스나 다른 곳에서는 절대 import하지 않습니다!
 */
public class ToolEnhancerIntegration {

    private final ToolEnhancerAPI api;
    private final boolean enabled;

    public ToolEnhancerIntegration() {
        // ToolEnhancer 플러그인이 있는지 확인
        if (Bukkit.getPluginManager().getPlugin("ToolEnhancer") != null) {
            try {
                this.api = ToolEnhancerAPI.getInstance();
                this.enabled = true;
            } catch (Exception e) {
                this.api = null;
                this.enabled = false;
            }
        } else {
            this.api = null;
            this.enabled = false;
        }
    }

    /**
     * ToolEnhancer 연동이 활성화되어 있는지 확인
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 강화석 아이템 생성
     */
    public ItemStack getEnhancementStone() {
        if (!enabled || api == null) {
            return null;
        }

        try {
            return api.createEnhancementStone();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 특정 아이템이 강화석인지 확인
     */
    public boolean isEnhancementStone(ItemStack item) {
        if (!enabled || api == null || item == null) {
            return false;
        }

        try {
            return api.isEnhancementStone(item);
        } catch (Exception e) {
            return false;
        }
    }
}