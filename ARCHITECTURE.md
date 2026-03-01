# Kiến trúc Telegram Milk Tea Bot

## Tổng quan

Ứng dụng dùng **kiến trúc phân lớp** (layered): Entity → Repository → Service → Bot. Logic nghiệp vụ nằm ở Service; Bot chỉ nhận update, điều hướng và gọi service.

## Các lớp (layers)

| Lớp | Gói | Vai trò |
|-----|-----|--------|
| **Entity** | `entity` | JPA entities: `TelegramUser`, `MilkTeaOrder`. Quan hệ One-to-Many (1 user, nhiều đơn). |
| **Repository** | `repository` | Spring Data JPA: `TelegramUserRepository`, `MilkTeaOrderRepository`. Truy vấn DB. |
| **Service** | `service` | `TelegramUserService` (findOrCreate), `MilkTeaOrderService` (createOrder, findOrdersByUser), `MenuService` (menu cố định). Constructor injection. |
| **Bot** | `bot` | `MilkteaTelegramBot` extends `TelegramLongPollingBot`: xử lý /start, nút "Order Milk Tea" / "View Orders", chọn món → size → xác nhận. Session in-memory (`UserSession`) theo chatId. |
| **Config** | `config` | `BotConfig`: `@ConfigurationProperties("telegram.bot")` cho token/username. |
| **DTO** | `dto` | `MenuItem` (record) cho menu. |
| **Exception** | `exception` | `UserNotFoundException` khi không tìm thấy user. |

## Luồng chính

1. **/start** → `TelegramUserService.findOrCreate()` lưu user (nếu chưa có) → gửi welcome + ReplyKeyboardMarkup (Order Milk Tea | View Orders).
2. **Order Milk Tea** → Hiện menu 3 món (ReplyKeyboard) → user chọn món → chọn size M/L → xác nhận Yes/Cancel → `MilkTeaOrderService.createOrder()` lưu đơn → về menu chính.
3. **View Orders** → `MilkTeaOrderService.findOrdersByUser()` → gửi danh sách đơn.

## Công nghệ

- **Long Polling**: Bot kéo update từ Telegram (không dùng webhook).
- **Constructor injection**: Tất cả dependency inject qua constructor (không field injection).
- **Jakarta + Spring Data JPA**: Entity dùng `jakarta.persistence.*`.
- **Lombok**: `@Getter`, `@Setter`, `@Builder`, `@RequiredArgsConstructor` cho entity và service.

## Cấu hình chạy

- PostgreSQL: tạo DB `milktea_db`, cấu hình trong `application.yml` (url, username, password).
- Telegram: tạo bot qua @BotFather, set `telegram.bot.token` và `telegram.bot.username` (hoặc biến môi trường `TELEGRAM_BOT_TOKEN`, `TELEGRAM_BOT_USERNAME`).
