package com.khoa.milktea_bot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity đơn hàng.
 * Nhiều đơn hàng thuộc về một user (Many-to-One).
 */
@Entity
@Table(name = "milk_tea_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private TelegramUser user;

    @Column(name = "drink_name", nullable = false)
    private String drinkName;

    @Enumerated(EnumType.STRING)
    @Column(name = "size", nullable = false)
    private OrderSize size;

    /** Giá món nước (theo size) */
    @Column(name = "drink_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal drinkPrice;

    /** Tên topping cách nhau bởi dấu phẩy, null nếu không có */
    @Column(name = "toppings_text", length = 500)
    private String toppingsText;

    /** Tổng tiền topping */
    @Column(name = "topping_total", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal toppingTotal = BigDecimal.ZERO;

    /** Tổng tiền đơn = drink_price + topping_total */
    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    /** Cột cũ tương thích DB: ghi cùng giá trị totalPrice khi save (nếu bảng vẫn có cột price) */
    @Column(name = "price", precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.CONFIRMED;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
