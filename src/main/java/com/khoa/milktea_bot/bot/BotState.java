package com.khoa.milktea_bot.bot;

/**
 * Trạng thái hội thoại của user với bot.
 */
public enum BotState {
    /** Menu chính: Order Milk Tea / View Orders */
    MAIN,
    /** Chọn 1 trong 4 category */
    ORDER_CATEGORY,
    /** Chọn món trong category */
    ORDER_DRINK,
    /** Chọn size M/L */
    ORDER_SIZE,
    /** Chọn topping (có thể nhiều), bấm Xong để qua bước sau */
    ORDER_TOPPING,
    /** Xác nhận đơn (Yes/Cancel) */
    ORDER_CONFIRM
}
