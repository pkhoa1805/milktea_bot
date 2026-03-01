package com.khoa.milktea_bot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity lưu thông tin user Telegram.
 * Một user có thể có nhiều đơn hàng (One-to-Many).
 */
@Entity
@Table(name = "telegram_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelegramUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID do Telegram cấp, dùng để nhận diện user */
    @Column(name = "telegram_id", unique = true, nullable = false)
    private Long telegramId;

    @Column(name = "username")
    private String username;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<Order> orders = new ArrayList<>();
}
