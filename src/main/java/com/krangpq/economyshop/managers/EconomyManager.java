package com.krangpq.economyshop.managers;

import com.krangpq.economyshop.EconomyShop;
import com.krangpq.economyshop.data.PlayerAccount;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {

    private final EconomyShop plugin;
    private final Map<UUID, PlayerAccount> accounts;
    private final File accountsFolder;

    public EconomyManager(EconomyShop plugin) {
        this.plugin = plugin;
        this.accounts = new HashMap<>();
        this.accountsFolder = new File(plugin.getDataFolder(), "data/accounts");

        if (!accountsFolder.exists()) {
            accountsFolder.mkdirs();
        }

        loadAllAccounts();
    }

    /**
     * 플레이어의 계정을 가져옵니다 (없으면 생성)
     */
    public PlayerAccount getAccount(UUID player) {
        if (!accounts.containsKey(player)) {
            PlayerAccount account = loadAccount(player);
            if (account == null) {
                account = createAccount(player);
            }
            accounts.put(player, account);
        }
        return accounts.get(player);
    }

    /**
     * 새 계정 생성
     */
    private PlayerAccount createAccount(UUID player) {
        double startingBalance = plugin.getConfig().getDouble("economy.starting-balance", 10000);
        PlayerAccount account = new PlayerAccount(player, startingBalance);
        saveAccount(account);
        return account;
    }

    /**
     * 계정 파일에서 로드
     */
    private PlayerAccount loadAccount(UUID player) {
        File accountFile = new File(accountsFolder, player.toString() + ".yml");
        if (!accountFile.exists()) {
            return null;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(accountFile);
        double balance = config.getDouble("balance", 0);
        long lastLogin = config.getLong("last-login", System.currentTimeMillis());

        return new PlayerAccount(player, balance, lastLogin);
    }

    /**
     * 계정 저장
     */
    public void saveAccount(PlayerAccount account) {
        File accountFile = new File(accountsFolder, account.getPlayerId().toString() + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        config.set("balance", account.getBalance());
        config.set("last-login", account.getLastLogin());

        try {
            config.save(accountFile);
        } catch (IOException e) {
            plugin.getLogger().severe("계정 저장 실패: " + account.getPlayerId());
            e.printStackTrace();
        }
    }

    /**
     * 모든 계정 로드
     */
    private void loadAllAccounts() {
        File[] files = accountsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            try {
                String uuidStr = file.getName().replace(".yml", "");
                UUID uuid = UUID.fromString(uuidStr);
                PlayerAccount account = loadAccount(uuid);
                if (account != null) {
                    accounts.put(uuid, account);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("잘못된 계정 파일: " + file.getName());
            }
        }

        plugin.getLogger().info(accounts.size() + "개의 계정을 로드했습니다.");
    }

    /**
     * 모든 계정 저장
     */
    public void saveAllAccounts() {
        for (PlayerAccount account : accounts.values()) {
            saveAccount(account);
        }
        plugin.getLogger().info(accounts.size() + "개의 계정을 저장했습니다.");
    }

    // ===== 경제 기능 =====

    /**
     * 잔고 확인
     */
    public double getBalance(UUID player) {
        return getAccount(player).getBalance();
    }

    /**
     * 돈 지급
     */
    public boolean deposit(UUID player, double amount) {
        if (amount <= 0) return false;

        PlayerAccount account = getAccount(player);
        double maxBalance = plugin.getConfig().getDouble("economy.max-balance", 999999999);

        if (account.getBalance() + amount > maxBalance) {
            return false;
        }

        account.setBalance(account.getBalance() + amount);
        saveAccount(account);
        return true;
    }

    /**
     * 돈 차감
     */
    public boolean withdraw(UUID player, double amount) {
        if (amount <= 0) return false;

        PlayerAccount account = getAccount(player);
        boolean allowNegative = plugin.getConfig().getBoolean("economy.allow-negative", false);

        if (!allowNegative && account.getBalance() < amount) {
            return false;
        }

        account.setBalance(account.getBalance() - amount);
        saveAccount(account);
        return true;
    }

    /**
     * 잔고 설정
     */
    public void setBalance(UUID player, double amount) {
        PlayerAccount account = getAccount(player);
        account.setBalance(amount);
        saveAccount(account);
    }

    /**
     * 특정 금액을 가지고 있는지 확인
     */
    public boolean has(UUID player, double amount) {
        return getBalance(player) >= amount;
    }

    /**
     * 플레이어 간 송금
     */
    public boolean transfer(UUID from, UUID to, double amount) {
        if (amount <= 0) return false;
        if (!has(from, amount)) return false;

        if (withdraw(from, amount)) {
            return deposit(to, amount);
        }

        return false;
    }

    /**
     * 금액 포맷
     */
    public String format(double amount) {
        String symbol = plugin.getConfig().getString("economy.currency-symbol", "원");
        return String.format("%,.0f%s", amount, symbol);
    }
}