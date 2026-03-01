package com.khoa.milktea_bot.service;

import com.khoa.milktea_bot.dto.DrinkItem;
import com.khoa.milktea_bot.dto.ToppingItem;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Load menu từ CSV (menu/Menu.csv): 4 category đồ uống + danh sách topping.
 */
@Service
public class MenuService {

    private static final String MENU_CSV = "menu/Menu.csv";
    private static final String TOPPING_CATEGORY = "Topping";

    private List<String> drinkCategories;
    private Map<String, List<DrinkItem>> drinksByCategory;
    private List<ToppingItem> toppings;

    @PostConstruct
    public void loadMenu() {
        List<DrinkItem> allDrinks = new ArrayList<>();
        List<ToppingItem> toppingList = new ArrayList<>();

        try (var reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource(MENU_CSV).getInputStream(), StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null || !header.startsWith("category")) {
                throw new IllegalStateException("Menu.csv invalid header");
            }
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length < 7) continue;
                String category = parts[0].trim();
                String itemId = parts[1].trim();
                String name = parts[2].trim();
                String description = parts[3].trim();
                BigDecimal priceM = parsePrice(parts[4]);
                BigDecimal priceL = parsePrice(parts[5]);
                boolean available = "true".equalsIgnoreCase(parts[6].trim());

                if (!available) continue;

                if (TOPPING_CATEGORY.equals(category)) {
                    toppingList.add(new ToppingItem(itemId, name, priceM));
                } else {
                    allDrinks.add(new DrinkItem(category, itemId, name, description, priceM, priceL));
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load " + MENU_CSV, e);
        }

        this.toppings = List.copyOf(toppingList);
        this.drinkCategories = allDrinks.stream()
                .map(DrinkItem::category)
                .distinct()
                .toList();
        this.drinksByCategory = allDrinks.stream()
                .collect(Collectors.groupingBy(DrinkItem::category));
    }

    private static BigDecimal parsePrice(String s) {
        try {
            return new BigDecimal(s.trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    /** 4 category: Trà Sữa, Trà Trái Cây, Cà Phê, Đá Xay */
    public List<String> getDrinkCategories() {
        return drinkCategories;
    }

    public List<DrinkItem> getDrinksByCategory(String category) {
        return drinksByCategory.getOrDefault(category, List.of());
    }

    public List<ToppingItem> getToppings() {
        return toppings;
    }

    public DrinkItem findDrinkByName(String name) {
        if (name == null) return null;
        String n = name.trim();
        return drinksByCategory.values().stream()
                .flatMap(List::stream)
                .filter(d -> d.name().equalsIgnoreCase(n))
                .findFirst()
                .orElse(null);
    }

    public ToppingItem findToppingByName(String name) {
        if (name == null) return null;
        String n = name.trim();
        return toppings.stream()
                .filter(t -> t.name().equalsIgnoreCase(n))
                .findFirst()
                .orElse(null);
    }
}
