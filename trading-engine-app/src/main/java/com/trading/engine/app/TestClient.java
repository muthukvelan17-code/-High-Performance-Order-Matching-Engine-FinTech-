package com.trading.engine.app;

import com.trading.engine.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class TestClient {
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        TradingServiceGrpc.TradingServiceBlockingStub stub = TradingServiceGrpc.newBlockingStub(channel);

        System.out.println("Sending test orders to the matching engine...");

        for (int i = 1; i <= 5; i++) {
            // Send a Buy Order
            OrderResponse buyResponse = stub.submitOrder(OrderRequest.newBuilder()
                    .setOrderId(i)
                    .setSymbol("BTCUSD")
                    .setPrice(50000 + i)
                    .setQuantity(100)
                    .setSide(OrderSide.BUY)
                    .setType(OrderType.LIMIT)
                    .setUserId(101)
                    .build());
            System.out.println("Buy Order " + i + " status: " + buyResponse.getMessage());

            // Send a matching Sell Order
            OrderResponse sellResponse = stub.submitOrder(OrderRequest.newBuilder()
                    .setOrderId(i + 100)
                    .setSymbol("BTCUSD")
                    .setPrice(50000 + i)
                    .setQuantity(100)
                    .setSide(OrderSide.SELL)
                    .setType(OrderType.LIMIT)
                    .setUserId(102)
                    .build());
            System.out.println("Sell Order " + (i + 100) + " status: " + sellResponse.getMessage());
        }

        System.out.println("Test complete. Check the server terminal for matching logs!");
        channel.shutdown();
    }
}
