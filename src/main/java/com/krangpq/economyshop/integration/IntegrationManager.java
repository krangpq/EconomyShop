package com.krangpq.economyshop.integration;

import com.krangpq.economyshop.EconomyShop;
import org.bukkit.Bukkit;

/**
 * 다른 플러그인과의 연동을 관리
 * PLUGIN_DEVELOPMENT_STANDARD.md 준수:
 * - 메인 클래스에서 다른 플러그인 API를 직접 import하지 않음
 * - Integration 패키지에서만 다른 플러그인 API 사용
 * - 연동 플러그인이 없어도 정상 작동
 */
public class IntegrationManager {

    private final EconomyShop plugin;
    private ToolEnhancerIntegration toolEnhancerIntegration;

    public IntegrationManager(EconomyShop plugin) {
        this.plugin = plugin;
    }

    /**
     * 연동 가능한 플러그인 확인 및 초기화
     */
    public void checkIntegrations() {
        // ToolEnhancer 연동 확인
        if (Bukkit.getPluginManager().getPlugin("ToolEnhancer") != null) {
            try {
                toolEnhancerIntegration = new ToolEnhancerIntegration();
                if (toolEnhancerIntegration.isEnabled()) {
                    plugin.getLogger().info("ToolEnhancer 연동 활성화!");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("ToolEnhancer 연동 실패: " + e.getMessage());
                toolEnhancerIntegration = null;
            }
        } else {
            plugin.getLogger().info("ToolEnhancer를 찾을 수 없습니다. (선택사항)");
        }
    }

    /**
     * ToolEnhancer 연동 객체 반환
     */
    public ToolEnhancerIntegration getToolEnhancer() {
        if (toolEnhancerIntegration == null) {
            // 연동이 없을 경우 더미 객체 반환 (NullPointerException 방지)
            return new ToolEnhancerIntegration();
        }
        return toolEnhancerIntegration;
    }

    /**
     * ToolEnhancer가 연동되어 있는지 확인
     */
    public boolean hasToolEnhancer() {
        return toolEnhancerIntegration != null && toolEnhancerIntegration.isEnabled();
    }
}