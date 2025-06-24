package com.example;

import com.example.service.StockTradingClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StockTradingClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(StockTradingClientApplication.class, args);
    }

}
