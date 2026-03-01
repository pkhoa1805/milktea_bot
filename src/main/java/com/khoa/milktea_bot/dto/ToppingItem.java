package com.khoa.milktea_bot.dto;

import java.math.BigDecimal;

/**
 * Topping theo menu CSV: giá cố định (price_m = price_l).
 */
public record ToppingItem(String itemId, String name, BigDecimal price) {
}
