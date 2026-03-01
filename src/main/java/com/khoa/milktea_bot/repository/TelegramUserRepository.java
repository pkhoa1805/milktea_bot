package com.khoa.milktea_bot.repository;

import com.khoa.milktea_bot.entity.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository truy vấn TelegramUser.
 */
@Repository
public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long> {

    Optional<TelegramUser> findByTelegramId(Long telegramId);
}
