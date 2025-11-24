package com.halmber;

import com.halmber.config.ApplicationConfig;
import com.halmber.config.ConsoleInputHandler;
import com.halmber.service.order.StatisticsService;

public class Main {

    public static void main(String[] args) {
        ConsoleInputHandler inputHandler = new ConsoleInputHandler();


        ApplicationConfig config = inputHandler.getConfiguration();
        StatisticsService orderService = new StatisticsService(config);

        orderService.processStatistics();

        System.out.println("\nStatistics processing completed successfully");

    }
}