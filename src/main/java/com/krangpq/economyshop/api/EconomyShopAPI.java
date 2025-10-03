package com.krangpq.economyshop.api;

import com.krangpq.economyshop.EconomyShop;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

/**
 * EconomyShop API
 * 다른 플러그인에서 경제 시스템에 접근할 때 사용
 */
public class EconomyShopAPI {

    private static EconomyShopAPI instance;
    private final EconomyShop plugin;

    public EconomyShopAPI(EconomyShop plugin) {
        this.plugin = plugin;
        instance = this;
    }

    public static EconomyShopAPI getInstance() {
        return instance;
    }

    /**
     * 플레이어의 잔고를 확인합니다
     */
    public double getBalance(UUID player) {
        return plugin.getEconomyManager().getBalance(player);
    }

    public double getBalance(OfflinePlayer player) {
        return getBalance(player.getUniqueId());
    }

    /**
     * 플레이어에게 돈을 지급합니다
     */
    public boolean deposit(UUID player, double amount) {
        return plugin.getEconomyManager().deposit(player, amount);
    }

    public boolean deposit(OfflinePlayer player, double amount) {
        return deposit(player.getUniqueId(), amount);
    }

    /**
     * 플레이어의 돈을 차감합니다
     */
    public boolean withdraw(UUID player, double amount) {
        return plugin.getEconomyManager().withdraw(player, amount);
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        return withdraw(player.getUniqueId(), amount);
    }

    /**
     * 플레이어의 잔고를 설정합니다
     */
    public void setBalance(UUID player, double amount) {
        plugin.getEconomyManager().setBalance(player, amount);
    }

    public void setBalance(OfflinePlayer player, double amount) {
        setBalance(player.getUniqueId(), amount);
    }

    /**
     * 플레이어가 특정 금액을 가지고 있는지 확인합니다
     */
    public boolean has(UUID player, double amount) {
        return plugin.getEconomyManager().has(player, amount);
    }

    public boolean has(OfflinePlayer player, double amount) {
        return has(player.getUniqueId(), amount);
    }

    /**
     * 화폐 단위를 반환합니다
     */
    public String getCurrencySymbol() {
        return plugin.getConfig().getString("economy.currency-symbol", "원");
    }

    /**
     * 금액을 포맷팅합니다
     */
    public String format(double amount) {
        return String.format("%,.0f%s", amount, getCurrencySymbol());
    }
}