package com.krangpq.economyshop;

import com.krangpq.economyshop.api.EconomyShopAPI;
import com.krangpq.economyshop.commands.*;
import com.krangpq.economyshop.gui.GuiSession;
import com.krangpq.economyshop.gui.MainShopGui;        // ← 추가 필요!
import com.krangpq.economyshop.gui.CategoryShopGui;   // ← 추가 필요!
import com.krangpq.economyshop.integration.IntegrationManager;
import com.krangpq.economyshop.managers.EconomyManager;
import com.krangpq.economyshop.managers.ShopManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class EconomyShop extends JavaPlugin {

    private static EconomyShop instance;
    private EconomyManager economyManager;
    private ShopManager shopManager;
    private IntegrationManager integrationManager;
    private GuiSession guiSession;
    private EconomyShopAPI api;
    private MainShopGui mainShopGui;
    private CategoryShopGui categoryShopGui;

    @Override
    public void onEnable() {
        instance = this;

        // 설정 파일 로드
        saveDefaultConfig();
        createShopConfigs();

        // 매니저 초기화
        this.economyManager = new EconomyManager(this);
        this.shopManager = new ShopManager(this);
        this.integrationManager = new IntegrationManager(this);
        this.guiSession = new GuiSession();

        // GUI 초기화 (한 번만!)
        this.mainShopGui = new MainShopGui(this);
        this.categoryShopGui = new CategoryShopGui(this);

        // API 초기화
        this.api = new EconomyShopAPI(this);

        // 명령어 등록
        registerCommands();

        // 연동 확인
        integrationManager.checkIntegrations();

        getLogger().info("EconomyShop 플러그인이 활성화되었습니다!");
        getLogger().info("작성자: KrangPQ");
    }

    // Getter 추가
    public MainShopGui getMainShopGui() {
        return mainShopGui;
    }

    public CategoryShopGui getCategoryShopGui() {
        return categoryShopGui;
    }

    @Override
    public void onDisable() {
        // 모든 계정 저장
        if (economyManager != null) {
            economyManager.saveAllAccounts();
        }

        // GUI 세션 정리
        if (guiSession != null) {
            guiSession.closeAllSessions();
        }

        getLogger().info("EconomyShop 플러그인이 비활성화되었습니다!");
    }

    private void createShopConfigs() {
        File shopsFolder = new File(getDataFolder(), "shops");
        if (!shopsFolder.exists()) {
            shopsFolder.mkdirs();
        }

        File customItemsFolder = new File(getDataFolder(), "custom-items");
        if (!customItemsFolder.exists()) {
            customItemsFolder.mkdirs();
        }

        // 기본 상점 설정 파일 생성
        saveResource("shops/crops.yml", false);
        saveResource("shops/minerals.yml", false);
        saveResource("shops/special.yml", false);
    }

    private void registerCommands() {
        getCommand("상점").setExecutor(new ShopCommand(this));
        getCommand("돈").setExecutor(new MoneyCommand(this));
        getCommand("송금").setExecutor(new PayCommand(this));
        getCommand("경제관리").setExecutor(new AdminEcoCommand(this));
        getCommand("상점등록").setExecutor(new RegisterItemCommand(this));
        getCommand("상점삭제").setExecutor(new DeleteItemCommand(this));
    }

    // Getter 메서드들
    public static EconomyShop getInstance() {
        return instance;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public IntegrationManager getIntegrationManager() {
        return integrationManager;
    }

    public GuiSession getGuiSession() {
        return guiSession;
    }

    public EconomyShopAPI getAPI() {
        return api;
    }

    public String getMessage(String key) {
        String message = getConfig().getString("messages." + key, key);
        return message.replace("{symbol}", getConfig().getString("economy.currency-symbol", "원"));
    }

    public String getPrefix() {
        return getMessage("prefix");
    }
}