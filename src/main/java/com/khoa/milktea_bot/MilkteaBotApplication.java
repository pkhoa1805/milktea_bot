package com.khoa.milktea_bot;

import com.khoa.milktea_bot.config.BotConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(BotConfig.class)
public class MilkteaBotApplication {

	public static void main(String[] args) {
		loadEnv();
		SpringApplication.run(MilkteaBotApplication.class, args);
	}

	private static void loadEnv() {
		try {
			var dotenv = io.github.cdimascio.dotenv.Dotenv.configure()
					.ignoreIfMissing()
					.load();
			dotenv.entries().forEach(entry ->
					System.setProperty(entry.getKey(), entry.getValue() != null ? entry.getValue() : ""));
		} catch (Exception ignored) {
			// Không có .env hoặc lỗi đọc -> dùng biến môi trường
		}
	}
}