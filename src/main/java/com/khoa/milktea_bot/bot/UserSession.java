package com.khoa.milktea_bot.bot;

import com.khoa.milktea_bot.entity.OrderSize;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Session tạm của user khi đang đặt hàng: state + thông tin đơn đang chọn.
 */
@Getter
@Setter
@Builder
public class UserSession {

    private BotState state;
    private String pendingCategory;
    private String pendingDrinkName;
    private OrderSize pendingSize;
    private BigDecimal pendingDrinkPrice;
    /** Tên các topping đã chọn */
    @Builder.Default
    private List<String> pendingToppingNames = new ArrayList<>();
    private BigDecimal pendingToppingTotal;
    /** Tổng tiền = drink + toppings (set khi xong chọn topping) */
    private BigDecimal pendingTotalPrice;

    public static UserSession main() {
        return UserSession.builder().state(BotState.MAIN).build();
    }
}
