package org.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
    @Value("${app.telegram.bot.token}")
    private String BOT_TOKEN;
    @Value("${app.telegram.bot.name}")
    private String BOT_USER_NAME;

    @Bean
    public AppointmentNotifyBot appointmentNotifyBot() {
        System.out.println(BOT_TOKEN);
        if (BOT_TOKEN == null) {
            throw new RuntimeException("BOT_TOKEN is null");
        }
        System.out.println(BOT_USER_NAME);
        if (BOT_USER_NAME == null) {
            throw new RuntimeException("BOT_USER_NAME is null");
        }
        return new AppointmentNotifyBot(BOT_TOKEN, BOT_USER_NAME);
//        return new AppointmentNotifyBot("6372449188:AAF7zrhPcAf5aUGpwLoWmjYPSAgP-A9MQV4","cita_notify_bot");
    }
}
