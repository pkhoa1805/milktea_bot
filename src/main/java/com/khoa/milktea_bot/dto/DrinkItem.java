package com.khoa.milktea_bot.dto;

import com.khoa.milktea_bot.entity.OrderSize;

import java.math.BigDecimal;

/**
 * Món nước theo menu CSV: category, item_id, name, description, price_m, price_l.
 */
public record DrinkItem(
        String category,
        String itemId,
        String name,
        String description,
        BigDecimal priceM,
        BigDecimal priceL
) {
    public BigDecimal getPriceFor(OrderSize size) {
        return size == OrderSize.M ? priceM : priceL;
    }
}
