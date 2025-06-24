package com.example.repository;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StockDB {

    public List<Stock> stocks = List.of(
            new Stock(1, "Tata", 20, "1324"),
            new Stock(2, "Lavish Motors", 50, "5757")
    );

    public List<Stock> getStocksFromDB() {
        return stocks;
    }

    public static class Stock {
        public int id;
        public String name;
        public int price;
        public String timestamp;

        public Stock(int id, String name, int price, String timestamp) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.timestamp = timestamp;
        }
    }
}
