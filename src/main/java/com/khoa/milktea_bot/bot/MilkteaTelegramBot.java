package com.khoa.milktea_bot.bot;

import com.khoa.milktea_bot.config.BotConfig;
import com.khoa.milktea_bot.dto.DrinkItem;
import com.khoa.milktea_bot.dto.ToppingItem;
import com.khoa.milktea_bot.entity.OrderSize;
import com.khoa.milktea_bot.entity.TelegramUser;
import com.khoa.milktea_bot.service.MenuService;
import com.khoa.milktea_bot.service.OrderService;
import com.khoa.milktea_bot.service.TelegramUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bot Telegram Long Polling: xử lý /start, đặt trà sữa, xem đơn.
 * Logic nghiệp vụ ủy quyền cho Service layer; bot chỉ điều hướng và gửi tin nhắn.
 */
@Component
public class MilkteaTelegramBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(MilkteaTelegramBot.class);

    private static final String CMD_START = "/start";
    private static final String BTN_ORDER = "Đặt đồ uống";
    private static final String BTN_VIEW_ORDERS = "Xem đơn";
    private static final String BTN_SIZE_M = "Cỡ M";
    private static final String BTN_SIZE_L = "Cỡ L";
    private static final String BTN_DONE_TOPPING = "Xong";
    private static final String BTN_CONFIRM_YES = "Xác nhận";
    private static final String BTN_CONFIRM_CANCEL = "Hủy";

    private final BotConfig botConfig;
    private final TelegramUserService telegramUserService;
    private final OrderService orderService;
    private final MenuService menuService;

    /** ChatId -> UserSession (state + pending order). */
    private final Map<Long, UserSession> userSessions = new ConcurrentHashMap<>();

    public MilkteaTelegramBot(
            BotConfig botConfig,
            TelegramUserService telegramUserService,
            OrderService orderService,
            MenuService menuService
    ) {
        this.botConfig = botConfig;
        this.telegramUserService = telegramUserService;
        this.orderService = orderService;
        this.menuService = menuService;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleMessage(update);
        }
    }

    private void handleMessage(Update update) {
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText().trim();

        try {
            if (CMD_START.equals(text)) {
                handleStart(update, chatId);
                return;
            }

            UserSession session = userSessions.getOrDefault(chatId, UserSession.main());
            userSessions.put(chatId, session);

            switch (session.getState()) {
                case MAIN -> handleMainMenu(update, chatId, text, session);
                case ORDER_CATEGORY -> handleOrderCategory(update, chatId, text, session);
                case ORDER_DRINK -> handleOrderDrink(update, chatId, text, session);
                case ORDER_SIZE -> handleOrderSize(update, chatId, text, session);
                case ORDER_TOPPING -> handleOrderTopping(update, chatId, text, session);
                case ORDER_CONFIRM -> handleOrderConfirm(update, chatId, text, session);
            }
        } catch (Exception e) {
            logger.error("Error handling update for chatId={}", chatId, e);
            sendText(chatId, "Đã xảy ra lỗi. Vui lòng thử lại hoặc gửi /start.");
        }
    }

    /** /start: lưu user (nếu chưa có), gửi welcome + bàn phím Order / View Orders */
    private void handleStart(Update update, Long chatId) {
        var from = update.getMessage().getFrom();
        String username = from.getUserName();
        String firstName = from.getFirstName();
        String lastName = from.getLastName();
        Long telegramId = from.getId();

        TelegramUser user = telegramUserService.findOrCreate(telegramId, username, firstName, lastName);

        String welcome = "Chào " + (firstName != null ? firstName : "bạn") + "! 👋\n\n"
                + "Đây là bot đặt đồ uống. Chọn một trong các lựa chọn bên dưới.";

        ReplyKeyboardMarkup mainKeyboard = buildMainMenuKeyboard();
        sendMessage(chatId, welcome, mainKeyboard);

        userSessions.put(chatId, UserSession.main());
    }

    /** Xử lý khi đang ở menu chính */
    private void handleMainMenu(Update update, Long chatId, String text, UserSession session) {
        if (BTN_ORDER.equals(text)) {
            showCategoryKeyboard(chatId);
            session.setState(BotState.ORDER_CATEGORY);
            return;
        }
        if (BTN_VIEW_ORDERS.equals(text)) {
            Long telegramId = update.getMessage().getFrom().getId();
            showUserOrders(chatId, telegramId);
            return;
        }
        sendText(chatId, "Vui lòng chọn \"Đặt đồ uống\" hoặc \"Xem đơn\".");
    }

    /** Hiển thị 4 category: Trà Sữa, Trà Trái Cây, Cà Phê, Đá Xay */
    private void showCategoryKeyboard(Long chatId) {
        List<String> categories = menuService.getDrinkCategories();
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        for (int i = 0; i < categories.size() && i < 2; i++) {
            row1.add(new KeyboardButton(categories.get(i)));
        }
        rows.add(row1);
        if (categories.size() > 2) {
            KeyboardRow row2 = new KeyboardRow();
            for (int i = 2; i < categories.size(); i++) {
                row2.add(new KeyboardButton(categories.get(i)));
            }
            rows.add(row2);
        }
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setKeyboard(rows);
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);
        sendMessage(chatId, "Chọn loại đồ uống:", keyboard);
    }

    /** User chọn category -> hiển thị danh sách món trong category đó */
    private void handleOrderCategory(Update update, Long chatId, String text, UserSession session) {
        List<DrinkItem> drinks = menuService.getDrinksByCategory(text.trim());
        if (drinks.isEmpty()) {
            sendText(chatId, "Không có category này. Vui lòng chọn lại.");
            return;
        }
        session.setPendingCategory(text.trim());
        session.setState(BotState.ORDER_DRINK);
        showDrinksKeyboard(chatId, drinks);
    }

    private void showDrinksKeyboard(Long chatId, List<DrinkItem> drinks) {
        List<KeyboardRow> rows = new ArrayList<>();
        for (DrinkItem d : drinks) {
            KeyboardRow row = new KeyboardRow();
            row.add(new KeyboardButton(d.name()));
            rows.add(row);
        }
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setKeyboard(rows);
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);
        sendMessage(chatId, "Chọn món:", keyboard);
    }

    /** User chọn món -> chuyển sang chọn size */
    private void handleOrderDrink(Update update, Long chatId, String text, UserSession session) {
        DrinkItem item = menuService.findDrinkByName(text);
        if (item == null || !item.category().equals(session.getPendingCategory())) {
            sendText(chatId, "Không có món này. Vui lòng chọn một món trong danh sách.");
            return;
        }
        session.setPendingDrinkName(item.name());
        session.setState(BotState.ORDER_SIZE);
        showSizeKeyboard(chatId);
    }

    private void showSizeKeyboard(Long chatId) {
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton(BTN_SIZE_M));
        row.add(new KeyboardButton(BTN_SIZE_L));
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setKeyboard(List.of(row));
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(true);
        sendMessage(chatId, "Chọn cỡ: M hay L?", keyboard);
    }

    /** User chọn size -> lưu giá món, chuyển sang chọn topping */
    private void handleOrderSize(Update update, Long chatId, String text, UserSession session) {
        OrderSize size = null;
        if (BTN_SIZE_M.equals(text)) size = OrderSize.M;
        if (BTN_SIZE_L.equals(text)) size = OrderSize.L;
        if (size == null) {
            sendText(chatId, "Vui lòng chọn Cỡ M hoặc Cỡ L.");
            return;
        }
        DrinkItem item = menuService.findDrinkByName(session.getPendingDrinkName());
        if (item == null) {
            sendText(chatId, "Lỗi: món không tồn tại. Vui lòng bắt đầu lại bằng /start.");
            resetSession(chatId);
            return;
        }
        BigDecimal drinkPrice = item.getPriceFor(size);
        session.setPendingSize(size);
        session.setPendingDrinkPrice(drinkPrice);
        session.setPendingToppingNames(new ArrayList<>());
        session.setPendingToppingTotal(BigDecimal.ZERO);
        session.setState(BotState.ORDER_TOPPING);
        showToppingKeyboard(chatId, session);
    }

    /** Bàn phím topping: nút "Xong" + danh sách topping. Mỗi lần bấm topping = thêm vào đơn. */
    private void showToppingKeyboard(Long chatId, UserSession session) {
        List<ToppingItem> allToppings = menuService.getToppings();
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow doneRow = new KeyboardRow();
        doneRow.add(new KeyboardButton(BTN_DONE_TOPPING));
        rows.add(doneRow);
        for (ToppingItem t : allToppings) {
            KeyboardRow row = new KeyboardRow();
            row.add(new KeyboardButton(t.name() + " (+" + t.price() + ")"));
            rows.add(row);
        }
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setKeyboard(rows);
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);
        String msg = "Chọn topping (có thể chọn nhiều). Bấm \"" + BTN_DONE_TOPPING + "\" khi xong.";
        if (!session.getPendingToppingNames().isEmpty()) {
            msg = "Đã thêm: " + String.join(", ", session.getPendingToppingNames()) + "\n\n" + msg;
        }
        sendMessage(chatId, msg, keyboard);
    }

    /** User chọn topping (thêm) hoặc Xong -> nếu Xong thì chuyển sang xác nhận */
    private void handleOrderTopping(Update update, Long chatId, String text, UserSession session) {
        if (BTN_DONE_TOPPING.equals(text)) {
            BigDecimal drinkPrice = session.getPendingDrinkPrice();
            BigDecimal toppingTotal = session.getPendingToppingTotal() != null ? session.getPendingToppingTotal() : BigDecimal.ZERO;
            BigDecimal total = drinkPrice.add(toppingTotal);
            session.setPendingTotalPrice(total);
            session.setState(BotState.ORDER_CONFIRM);
            String toppingsStr = session.getPendingToppingNames().isEmpty()
                    ? "Không"
                    : String.join(", ", session.getPendingToppingNames());
            String confirmText = "Xác nhận đơn:\n"
                    + "• Món: " + session.getPendingDrinkName() + " (" + session.getPendingSize() + ")\n"
                    + "• Giá món: " + drinkPrice + " VNĐ\n"
                    + "• Topping: " + toppingsStr + " (" + toppingTotal + " VNĐ)\n"
                    + "• Tổng cộng: " + total + " VNĐ\n\n"
                    + "Xác nhận đặt hàng?";
            KeyboardRow row = new KeyboardRow();
            row.add(new KeyboardButton(BTN_CONFIRM_YES));
            row.add(new KeyboardButton(BTN_CONFIRM_CANCEL));
            ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
            keyboard.setKeyboard(List.of(row));
            keyboard.setResizeKeyboard(true);
            keyboard.setOneTimeKeyboard(true);
            sendMessage(chatId, confirmText, keyboard);
            return;
        }
        String toppingName = text.replaceAll(" \\(\\+[0-9.]+\\)\\s*$", "").trim();
        ToppingItem topping = menuService.findToppingByName(toppingName);
        if (topping == null) {
            sendText(chatId, "Không có topping này. Chọn trong danh sách hoặc bấm \"" + BTN_DONE_TOPPING + "\".");
            return;
        }
        session.getPendingToppingNames().add(topping.name());
        BigDecimal sum = session.getPendingToppingTotal() != null ? session.getPendingToppingTotal() : BigDecimal.ZERO;
        session.setPendingToppingTotal(sum.add(topping.price()));
        showToppingKeyboard(chatId, session);
    }

    /** User xác nhận Yes -> lưu đơn, gửi thông báo, về menu chính. Cancel -> về menu chính */
    private void handleOrderConfirm(Update update, Long chatId, String text, UserSession session) {
        String choice = text != null ? text.trim() : "";
        if (BTN_CONFIRM_YES.equalsIgnoreCase(choice)) {
            try {
                Long telegramId = update.getMessage().getFrom().getId();
                TelegramUser user = telegramUserService.findByTelegramId(telegramId);
                String drinkName = session.getPendingDrinkName();
                OrderSize size = session.getPendingSize();
                BigDecimal drinkPrice = session.getPendingDrinkPrice() != null ? session.getPendingDrinkPrice() : BigDecimal.ZERO;
                BigDecimal toppingTotal = session.getPendingToppingTotal() != null ? session.getPendingToppingTotal() : BigDecimal.ZERO;
                BigDecimal totalPrice = session.getPendingTotalPrice() != null ? session.getPendingTotalPrice() : drinkPrice.add(toppingTotal);
                String toppingsText = session.getPendingToppingNames() == null || session.getPendingToppingNames().isEmpty()
                        ? null
                        : String.join(", ", session.getPendingToppingNames());

                if (drinkName == null || size == null) {
                    sendText(chatId, "Lỗi: thiếu thông tin đơn. Vui lòng đặt lại từ /start.");
                    return;
                }

                orderService.createOrder(user, drinkName, size, drinkPrice, toppingsText, toppingTotal, totalPrice);
                sendText(chatId, "✅ Đơn hàng đã được ghi nhận. Cảm ơn bạn!");
            } catch (Exception e) {
                logger.error("Lỗi khi lưu đơn hàng chatId={}", chatId, e);
                sendText(chatId, "Không lưu được đơn. Vui lòng thử lại hoặc gửi /start. Lỗi: " + e.getMessage());
                return;
            }
        } else if (BTN_CONFIRM_CANCEL.equalsIgnoreCase(choice)) {
            sendText(chatId, "Đã hủy đơn.");
        } else {
            sendText(chatId, "Vui lòng chọn Xác nhận hoặc Hủy.");
            return;
        }
        resetSession(chatId);
        sendMessage(chatId, "Chọn tiếp:", buildMainMenuKeyboard());
        userSessions.put(chatId, UserSession.main());
    }

    /** Hiển thị danh sách đơn của user (telegramId lấy từ message khi gọi) */
    private void showUserOrders(Long chatId, Long telegramId) {
        try {
            TelegramUser user = telegramUserService.findByTelegramId(telegramId);
            var orders = orderService.findOrdersByUser(user);
            if (orders.isEmpty()) {
                sendText(chatId, "Bạn chưa có đơn hàng nào.");
                return;
            }
            StringBuilder sb = new StringBuilder("📋 Đơn hàng của bạn:\n\n");
            for (var order : orders) {
                sb.append("• ").append(order.getDrinkName()).append(" (").append(order.getSize()).append(")")
                        .append(" - ").append(order.getTotalPrice()).append(" VNĐ");
                if (order.getToppingsText() != null && !order.getToppingsText().isBlank()) {
                    sb.append(" [topping: ").append(order.getToppingsText()).append("]");
                }
                sb.append("\n");
            }
            sendText(chatId, sb.toString());
        } catch (com.khoa.milktea_bot.exception.UserNotFoundException e) {
            sendText(chatId, "Chưa tìm thấy thông tin của bạn. Gửi /start để bắt đầu.");
        }
    }

    private void resetSession(Long chatId) {
        userSessions.remove(chatId);
    }

    private ReplyKeyboardMarkup buildMainMenuKeyboard() {
        KeyboardRow row = new KeyboardRow();
        row.add(new KeyboardButton(BTN_ORDER));
        row.add(new KeyboardButton(BTN_VIEW_ORDERS));
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setKeyboard(List.of(row));
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);
        return keyboard;
    }

    private void sendText(Long chatId, String text) {
        sendMessage(chatId, text, null);
    }

    private void sendMessage(Long chatId, String text, ReplyKeyboardMarkup keyboard) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId.toString());
        msg.setText(text);
        if (keyboard != null) {
            msg.setReplyMarkup(keyboard);
        }
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            logger.error("Failed to send message to chatId={}", chatId, e);
        }
    }
}
