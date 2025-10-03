package com.krangpq.economyshop.data;

import java.util.UUID;

/**
 * 플레이어 계정 정보
 */
public class PlayerAccount {

    private final UUID playerId;
    private double balance;
    private long lastLogin;

    public PlayerAccount(UUID playerId, double balance) {
        this(playerId, balance, System.currentTimeMillis());
    }

    public PlayerAccount(UUID playerId, double balance, long lastLogin) {
        this.playerId = playerId;
        this.balance = balance;
        this.lastLogin = lastLogin;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void updateLastLogin() {
        this.lastLogin = System.currentTimeMillis();
    }
}