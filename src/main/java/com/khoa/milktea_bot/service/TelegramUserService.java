package com.khoa.milktea_bot.service;

import com.khoa.milktea_bot.entity.TelegramUser;
import com.khoa.milktea_bot.repository.TelegramUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service xử lý logic user: tìm hoặc tạo user từ thông tin Telegram.
 */
@Service
@RequiredArgsConstructor
public class TelegramUserService {

    private final TelegramUserRepository telegramUserRepository;

    /**
     * Tìm user theo telegramId; nếu chưa có thì tạo mới và lưu DB.
     */
    @Transactional
    public TelegramUser findOrCreate(Long telegramId, String username, String firstName, String lastName) {
        return telegramUserRepository.findByTelegramId(telegramId)
                .orElseGet(() -> {
                    TelegramUser newUser = TelegramUser.builder()
                            .telegramId(telegramId)
                            .username(username)
                            .firstName(firstName)
                            .lastName(lastName)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return telegramUserRepository.save(newUser);
                });
    }

    public TelegramUser findByTelegramId(Long telegramId) {
        return telegramUserRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new com.khoa.milktea_bot.exception.UserNotFoundException(telegramId));
    }
}
