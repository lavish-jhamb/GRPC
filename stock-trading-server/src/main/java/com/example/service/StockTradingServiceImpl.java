package com.example.service;

import com.example.OrderSummary;
import com.example.StockOrder;
import com.example.StockRequest;
import com.example.StockResponse;
import com.example.StockTradingServiceGrpc;
import com.example.TradeStatus;
import com.example.repository.StockDB;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.grpc.server.service.GrpcService;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@GrpcService
public class StockTradingServiceImpl extends StockTradingServiceGrpc.StockTradingServiceImplBase {

    @Autowired
    private StockDB stockDB;

    /*
     * Unary request (req-res model)
     */
    @Override
    public void getStockPrice(StockRequest request, StreamObserver<StockResponse> responseObserver) {
        // Use case : request -> stock_symbol -> DB -> map response -> return

        // get the stock name from request
        String stockName = request.getStockSymbol();

        // filter out stock by stock name
        StockDB.Stock stock = stockDB.getStocksFromDB().stream()
                .filter(s -> s.name.equalsIgnoreCase(stockName)).findAny().get();

        // Use builder class for response provided by grpc
        StockResponse stockResponse = StockResponse.newBuilder()
                .setStockSymbol(stock.name)
                .setPrice(stock.price)
                .setTimestamp(stock.timestamp)
                .build();

        // send stock response to StreamObserver<>
        responseObserver.onNext(stockResponse);
        responseObserver.onCompleted();

    }

    /*
     * Server streaming request - server sends multiple responses back to client - real time updates
     */
    @Override
    public void subscribeStockPrice(StockRequest request, StreamObserver<StockResponse> responseObserver) {
        String stockName = request.getStockSymbol();
        try {
            for (int i = 0; i < 10; i++) {
                StockResponse response = StockResponse.newBuilder()
                        .setStockSymbol(stockName)
                        .setPrice(new Random().nextDouble(200))
                        .setTimestamp(Instant.now().toString())
                        .build();
                responseObserver.onNext(response);
                TimeUnit.SECONDS.sleep(1);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    /*
     * Client Streaming request - client send multiple request to server and server processes it respond back with single response.
     */
    @Override
    public StreamObserver<StockOrder> bulkStockOrder(StreamObserver<OrderSummary> responseObserver) {
        return new StreamObserver<StockOrder>() {

            private int totalOrders = 0;
            private int totalAmount = 0;
            private int successCount = 0;

            // Client will send multiple StockOrder request to server.
            @Override
            public void onNext(StockOrder stockOrder) {
                totalOrders++;
                totalAmount += stockOrder.getPrice() * stockOrder.getPrice();
                successCount++;
                System.out.println("Received order: " + stockOrder);
            }

            // If there is any error while processing the request from client then this method will be called.
            @Override
            public void onError(Throwable throwable) {
                System.out.println("Server unable to process request: " + throwable.getMessage());
            }

            // This method will be called when client has sent all the StockOrder request to server.
            @Override
            public void onCompleted() {
                OrderSummary summary = OrderSummary.newBuilder()
                        .setTotalOrders(totalOrders)
                        .setSuccessCount(successCount)
                        .setTotalAmount(totalAmount)
                        .build();
                responseObserver.onNext(summary); // Send the summary response back to client
                responseObserver.onCompleted(); // Complete the response observer
            }
        };
    }

    /*
     * Bidirectional Streaming request - both client and server can send multiple request and response to each other.
     * NOTE: Input and output are both streams, so client can send multiple StockOrder requests (INPUT) and server can respond with multiple TradeStatus responses (OUTPUT).
     */
    @Override
    public StreamObserver<StockOrder> liveTrading(StreamObserver<TradeStatus> responseObserver) {
        return new StreamObserver<StockOrder>() {
            @Override
            public void onNext(StockOrder stockOrder) {
                System.out.println("Received live trade order: " + stockOrder);

                String statusMessage = "Order for " + stockOrder.getStockSymbol() + " processed successfully.";
                String message = "Order ID: " + stockOrder.getOrderId() + ", Status: " + statusMessage;

                if (stockOrder.getPrice() <= 0) {
                    statusMessage = "Failed - Invalid Price";
                    message = "Order ID: " + stockOrder.getOrderId() + ", Status: " + statusMessage;
                }
                TradeStatus tradeStatus = TradeStatus.newBuilder()
                        .setOrderId(stockOrder.getOrderId())
                        .setStatus(statusMessage)
                        .setMessage(message)
                        .build();

                responseObserver.onNext(tradeStatus);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error in live trading: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
