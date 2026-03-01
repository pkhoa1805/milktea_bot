-- Thêm cột mới cho đơn hàng (drink_price, toppings, total_price)
-- Chạy script này 1 lần trên database milktea_db nếu bảng milk_tea_orders đã tồn tại từ trước.

ALTER TABLE milk_tea_orders ADD COLUMN IF NOT EXISTS drink_price NUMERIC(12,2);
ALTER TABLE milk_tea_orders ADD COLUMN IF NOT EXISTS toppings_text VARCHAR(500);
ALTER TABLE milk_tea_orders ADD COLUMN IF NOT EXISTS topping_total NUMERIC(12,2) DEFAULT 0;
ALTER TABLE milk_tea_orders ADD COLUMN IF NOT EXISTS total_price NUMERIC(12,2);

-- Gán giá trị cho hàng cũ: coi price cũ là drink_price và total_price
UPDATE milk_tea_orders
SET drink_price = COALESCE(drink_price, price, 0),
    topping_total = COALESCE(topping_total, 0),
    total_price = COALESCE(total_price, price, 0)
WHERE drink_price IS NULL OR total_price IS NULL;

-- Đặt NOT NULL và default (PostgreSQL: chỉ set NOT NULL khi không còn NULL)
ALTER TABLE milk_tea_orders ALTER COLUMN drink_price SET DEFAULT 0;
ALTER TABLE milk_tea_orders ALTER COLUMN topping_total SET DEFAULT 0;
UPDATE milk_tea_orders SET drink_price = 0 WHERE drink_price IS NULL;
UPDATE milk_tea_orders SET topping_total = 0 WHERE topping_total IS NULL;
UPDATE milk_tea_orders SET total_price = 0 WHERE total_price IS NULL;
ALTER TABLE milk_tea_orders ALTER COLUMN drink_price SET NOT NULL;
ALTER TABLE milk_tea_orders ALTER COLUMN topping_total SET NOT NULL;
ALTER TABLE milk_tea_orders ALTER COLUMN total_price SET NOT NULL;
