package com.khoa.milktea_bot.exception;

/**
 * Ném khi không tìm thấy TelegramUser theo telegramId.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long telegramId) {
        super("User not found for telegramId: " + telegramId);
    }
}
