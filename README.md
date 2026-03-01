# Milk Tea Bot – Telegram đặt đồ uống

Bot Telegram đặt đồ uống (trà sữa, trà trái cây, cà phê, đá xay) theo menu CSV. Khách chọn category → món → size → topping → xác nhận; admin nhận thông báo đơn mới qua Telegram.

## Công nghệ

- **Java 17+**, **Spring Boot 3.2**
- **PostgreSQL**, **Spring Data JPA** (Hibernate)
- **telegrambots-spring-boot-starter** (Long Polling)

## Yêu cầu

- JDK 17 trở lên
- PostgreSQL (tạo sẵn database `milktea_db`)
- Tài khoản Telegram, tạo bot qua [@BotFather](https://t.me/BotFather) để lấy **token** và **username**

## Cấu hình

### 1. Biến môi trường / file `.env`



**Tạo file `.env`** (copy từ `.env.example` nếu có):

```env
# Database
DB_PASSWORD=mật_khẩu_postgres

# Telegram Bot (từ @BotFather)
TELEGRAM_BOT_TOKEN=token_bot
TELEGRAM_BOT_USERNAME=username_bot

# Admin nhận thông báo đơn mới (Chat ID, để trống = tắt)
TELEGRAM_ADMIN_CHAT_ID=
```



### 2. Database

- Tạo database: `milktea_db`
- Trong `application.yml`: `url`, `username` có thể sửa nếu khác (ví dụ host/port). Mật khẩu lấy từ `DB_PASSWORD` trong `.env`.

### 3. Admin nhận đơn

- Đặt `TELEGRAM_ADMIN_CHAT_ID` bằng **Chat ID** Telegram của admin.
- Cách lấy Chat ID: admin nhắn `/start` cho bot (hoặc dùng [@userinfobot](https://t.me/userinfobot)) → dùng số **Id** đó.
- Để trống thì tắt gửi thông báo đơn cho admin.

## Chạy ứng dụng

```bash
# Cài dependency (nếu chưa)
./mvnw clean install

# Chạy
./mvnw spring-boot:run
```

Hoặc chạy class `MilkteaBotApplication` trong IDE (đảm bảo working directory là thư mục gốc project để đọc được `.env`).

**Lưu ý:** Chỉ chạy **một** instance bot (cùng token). Chạy hai process sẽ bị lỗi 409 Conflict.

## Tính năng bot

| Hành động   | Mô tả |
|------------|--------|
| `/start`   | Đăng ký/lưu user, hiện menu chính (Đặt đồ uống / Xem đơn). |
| Đặt đồ uống | Chọn 1 trong 4 category → chọn món → chọn cỡ M/L → chọn topping (có thể nhiều) → Xác nhận / Hủy. |
| Xem đơn    | Xem danh sách đơn đã đặt. |
| Admin      | Khi có đơn mới (khách bấm Xác nhận), bot gửi tin nhắn chi tiết đơn cho admin (nếu đã cấu hình `TELEGRAM_ADMIN_CHAT_ID`). |

Menu đồ uống và topping đọc từ file **`src/main/resources/menu/Menu.csv`** (4 category: Trà Sữa, Trà Trái Cây, Cà Phê, Đá Xay + danh sách topping).

## Cấu trúc project (chính)

```
src/main/java/com/khoa/milktea_bot/
├── MilkteaBotApplication.java   # Entry, load .env
├── entity/                      # TelegramUser, Order, OrderSize, OrderStatus
├── repository/                  # TelegramUserRepository, OrderRepository
├── service/                     # TelegramUserService, OrderService, MenuService
├── bot/                         # MilkteaTelegramBot, BotState, UserSession
├── config/                      # BotConfig (telegram.bot.*)
├── dto/                         # DrinkItem, ToppingItem
└── exception/                   # UserNotFoundException

src/main/resources/
├── application.yml              # Cấu hình (không chứa secret)
└── menu/Menu.csv                # Menu đồ uống + topping
```

Chi tiết kiến trúc: xem [ARCHITECTURE.md](ARCHITECTURE.md)

