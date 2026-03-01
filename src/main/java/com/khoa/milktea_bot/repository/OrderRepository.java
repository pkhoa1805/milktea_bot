package com.khoa.milktea_bot.repository;

import com.khoa.milktea_bot.entity.Order;
import com.khoa.milktea_bot.entity.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository truy vấn Order.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByCreatedAtDesc(TelegramUser user);
}
