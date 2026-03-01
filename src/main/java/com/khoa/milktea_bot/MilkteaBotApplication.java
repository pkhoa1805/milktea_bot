package com.khoa.milktea_bot;

import com.khoa.milktea_bot.config.BotConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(BotConfig.class)
public class MilkteaBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(MilkteaBotApplication.class, args);
	}
}