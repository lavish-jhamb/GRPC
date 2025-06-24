package com.example.service;

import com.example.OrderSummary;
import com.example.StockOrder;
import com.example.StockRequest;
import com.example.StockResponse;
import com.example.StockTradingServiceGrpc;
import com.example.TradeStatus;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class StockTradingClient {

    // unary client
    @GrpcClient("stockServiceBlockingStub")
    private StockTradingServiceGrpc.StockTradingServiceBlockingStub client;

    // server streaming client
    @GrpcClient("stockServiceStub")
    private StockTradingServiceGrpc.StockTradingServiceStub serverStreamingClient;

    // client streaming client
    @GrpcClient("stockServiceClientStreamingStub")
    private StockTradingServiceGrpc.StockTradingServiceStub clientStreamingClient;

    // Bi-directional streaming client
    @GrpcClient("stockServiceBiDirectionalStub")
    private StockTradingServiceGrpc.StockTradingServiceStub biDirectionalClient;

    /*
     * Unary request (req-res model)
     */
    public StockResponse getStockPrice(String stockSymbol) {
        StockRequest request = StockRequest.newBuilder()
                .setStockSymbol(stockSymbol)
                .build();
        return client.getStockPrice(request);
    }

    /*
     * Server streaming request - server sends multiple responses back to client - real time updates
     */
    public void subscribeStockPrice(String stockSymbol) {
        StockRequest request = StockRequest.newBuilder()
                .setStockSymbol(stockSymbol)
                .build();
        serverStreamingClient.subscribeStockPrice(request, new StreamObserver<StockResponse>() {
            @Override
            public void onNext(StockResponse stockResponse) {
                System.out.println("stock name : " + stockResponse.getStockSymbol());
                System.out.println("stock price : " + stockResponse.getPrice());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Stock price stream live updates completed!");
            }
        });
    }

    /**
     * Client streaming request - client sends multiple requests to server
     */
    public void placeBulkStockOrder() {

        StreamObserver<OrderSummary> responseObserver = new StreamObserver<OrderSummary>() {
            @Override
            public void onNext(OrderSummary orderSummary) {
                System.out.println("Order Summary Received from Server:");
                System.out.println("Total Orders: " + orderSummary.getTotalOrders());
                System.out.println("Successful Orders: " + orderSummary.getSuccessCount());
                System.out.println("Total Amount: $" + orderSummary.getTotalAmount());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Order Summary Received error from Server:" + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Stream completed , server is done sending summary !");
            }
        };

        StreamObserver<StockOrder> requestObserver = clientStreamingClient.bulkStockOrder(responseObserver);

        // Client will send multiple StockOrder request to server.

        try {
            requestObserver.onNext(StockOrder.newBuilder()
                    .setOrderId("1")
                    .setStockSymbol("AAPL")
                    .setOrderType("BUY")
                    .setPrice(150)
                    .setQuantity(10)
                    .build());

            requestObserver.onNext(StockOrder.newBuilder()
                    .setOrderId("2")
                    .setStockSymbol("GOOGL")
                    .setOrderType("SELL")
                    .setPrice(2700)
                    .setQuantity(5)
                    .build());

            requestObserver.onNext(StockOrder.newBuilder()
                    .setOrderId("3")
                    .setStockSymbol("TSLA")
                    .setOrderType("BUY")
                    .setPrice(700)
                    .setQuantity(8)
                    .build());

            // Indicate that no more messages will be sent
            requestObserver.onCompleted();
        } catch (Exception ex) {
            requestObserver.onError(ex);
        }
    }

    /*
     * Bi-directional streaming request - client and server can send multiple messages to each other.
     */
    public void startTrading() throws InterruptedException {
        StreamObserver<StockOrder> requestObserver = biDirectionalClient.liveTrading(new StreamObserver<TradeStatus>() {
            @Override
            public void onNext(TradeStatus tradeStatus) {
                System.out.println("Server response: " + tradeStatus);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error in live trading: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Live trading stream completed!");
            }
        });

        // Client can send multiple StockOrder requests to server.
        for (int i = 0; i < 5; i++) {
            StockOrder stockOrder = StockOrder.newBuilder()
                    .setOrderId("order-" + i)
                    .setStockSymbol("AAPL")
                    .setOrderType("BUY")
                    .setPrice(150 + i * 10)
                    .setQuantity(10 + i)
                    .build();
            requestObserver.onNext(stockOrder);
            Thread.sleep(500); // Simulate some delay between orders
        }
        // Indicate that no more messages will be sent
        requestObserver.onCompleted();
    }
}
