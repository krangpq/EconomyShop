package com.krangpq.economyshop.gui;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * GUI 세션 관리
 * ToolEnhancer의 GuiSession 패턴 사용
 */
public class GuiSession {

    private final Map<UUID, String> activeSessions;

    public GuiSession() {
        this.activeSessions = new HashMap<>();
    }

    /**
     * 세션 시작
     */
    public boolean startSession(Player player, String guiType) {
        if (hasActiveSession(player)) {
            return false;
        }
        activeSessions.put(player.getUniqueId(), guiType);
        return true;
    }

    /**
     * 세션 종료
     */
    public void endSession(Player player) {
        activeSessions.remove(player.getUniqueId());
    }

    /**
     * 활성 세션 확인
     */
    public boolean hasActiveSession(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    /**
     * 세션 타입 확인
     */
    public String getSessionType(Player player) {
        return activeSessions.get(player.getUniqueId());
    }

    /**
     * 특정 타입 세션인지 확인
     */
    public boolean isSessionType(Player player, String type) {
        return type.equals(getSessionType(player));
    }

    /**
     * 모든 세션 종료
     */
    public void closeAllSessions() {
        activeSessions.clear();
    }
}