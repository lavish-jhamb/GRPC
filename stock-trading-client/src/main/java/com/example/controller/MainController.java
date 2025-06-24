package com.example.controller;

import com.example.StockResponse;
import com.example.service.StockTradingClient;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    @Autowired
    private StockTradingClient client;

    // Use Protobuf JSON Format for Serialization - convert a Protobuf object into a JSON string, which can then be sent as the response body.
    @GetMapping(value = "/stock-price/{name}", produces = MediaType.APPLICATION_PROTOBUF_VALUE)
    public String getStockPrice(@PathVariable("name") String name) throws InvalidProtocolBufferException {
        StockResponse stockResponse = client.getStockPrice(name);
        String jsonResponse = JsonFormat.printer().print(stockResponse);
        return jsonResponse;
    }

    @GetMapping("/stock/subscribe/{name}")
    public void subscribeStock(@PathVariable("name") String name) {
        client.subscribeStockPrice(name);
    }

    @GetMapping("/stock/order")
    public void placeBulkOrder() {
        client.placeBulkStockOrder();
    }

    @GetMapping("stock/live")
    public void liveStockUpdates() throws InterruptedException {
        client.startTrading();
    }


}
