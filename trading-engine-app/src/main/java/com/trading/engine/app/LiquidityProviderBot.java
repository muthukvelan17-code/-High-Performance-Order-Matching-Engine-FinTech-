package com.trading.engine.app;

import com.trading.engine.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LiquidityProviderBot {
    private final TradingServiceGrpc.TradingServiceBlockingStub stub;
    private final Random random = new Random();
    private final String symbol = "BTCUSD";
    private long lastPrice = 50000;
    private long orderIdCounter = 1000000;

    public LiquidityProviderBot(String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.stub = TradingServiceGrpc.newBlockingStub(channel);
    }

    public void start() {
        log.info("Starting Liquidity Provider Bot for {}...", symbol);
        
        new Thread(() -> {
            while (true) {
                try {
                    provideLiquidity();
                    TimeUnit.MILLISECONDS.sleep(100); // 10 orders per second
                } catch (Exception e) {
                    log.error("Bot encountered error: {}", e.getMessage());
                    try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException ignored) {}
                }
            }
        }).start();
    }

    private void provideLiquidity() {
        // Randomly drift the price slightly
        lastPrice += (random.nextInt(11) - 5); 
        
        // Place a Buy order slightly below last price
        submit(OrderSide.BUY, lastPrice - 1 - random.nextInt(5));
        
        // Place a Sell order slightly above last price
        submit(OrderSide.SELL, lastPrice + 1 + random.nextInt(5));
    }

    private void submit(OrderSide side, long price) {
        OrderRequest request = OrderRequest.newBuilder()
                .setOrderId(++orderIdCounter)
                .setSymbol(symbol)
                .setPrice(price)
                .setQuantity(10 + random.nextInt(90))
                .setSide(side)
                .setType(OrderType.LIMIT)
                .setUserId(999) // Bot user ID
                .build();
        
        try {
            stub.submitOrder(request);
        } catch (Exception e) {
            log.warn("Failed to submit bot order: {}", e.getMessage());
        }
    }

    public static void main(String[] args) {
        LiquidityProviderBot bot = new LiquidityProviderBot("localhost", 9090);
        bot.start();
    }
}
