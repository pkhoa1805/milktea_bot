package com.khoa.milktea_bot.dto;

import com.khoa.milktea_bot.entity.OrderSize;

import java.math.BigDecimal;

/**
 * DTO một món trong menu: tên món và giá theo size.
 */
public record MenuItem(
        String drinkName,
        BigDecimal priceM,
        BigDecimal priceL
) {
    public BigDecimal getPriceFor(OrderSize size) {
        return size == OrderSize.M ? priceM : priceL;
    }
}
