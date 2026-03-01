package com.khoa.milktea_bot.service;

import com.khoa.milktea_bot.entity.Order;
import com.khoa.milktea_bot.entity.OrderSize;
import com.khoa.milktea_bot.entity.OrderStatus;
import com.khoa.milktea_bot.entity.TelegramUser;
import com.khoa.milktea_bot.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service tạo đơn hàng và lấy danh sách đơn theo user.
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public Order createOrder(TelegramUser user, String drinkName, OrderSize size,
                             BigDecimal drinkPrice, String toppingsText, BigDecimal toppingTotal, BigDecimal totalPrice) {
        BigDecimal drink = drinkPrice != null ? drinkPrice : BigDecimal.ZERO;
        BigDecimal topTotal = toppingTotal != null ? toppingTotal : BigDecimal.ZERO;
        BigDecimal total = totalPrice != null ? totalPrice : drink.add(topTotal);
        Order order = Order.builder()
                .user(user)
                .drinkName(drinkName)
                .size(size)
                .drinkPrice(drink)
                .toppingsText(toppingsText)
                .toppingTotal(topTotal)
                .totalPrice(total)
                .price(total)
                .status(OrderStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .build();
        return orderRepository.save(order);
    }

    public List<Order> findOrdersByUser(TelegramUser user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user);
    }
}
