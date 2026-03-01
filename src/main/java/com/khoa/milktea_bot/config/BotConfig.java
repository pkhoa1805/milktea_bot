package com.khoa.milktea_bot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình bot: token và username lấy từ application.yml (telegram.bot).
 */
@Configuration
@ConfigurationProperties(prefix = "telegram.bot")
public class BotConfig {

    private String token;
    private String username;
    /** Chat ID của admin để bot gửi thông báo đơn mới (để trống = không gửi). */
    private String adminChatId;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getAdminChatId() {
        if (adminChatId == null || adminChatId.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(adminChatId.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void setAdminChatId(String adminChatId) {
        this.adminChatId = adminChatId;
    }
}
