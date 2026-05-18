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
    private final java.util.Random random = new java.util.Random();
    private long orderIdCounter = 1000000;

    public LiquidityProviderBot(String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        this.stub = TradingServiceGrpc.newBlockingStub(channel);
    }

    public void start() {
        log.info("Starting Multi-Asset Liquidity Provider Bot (gRPC)...");
        
        // Start BTC/USD Market Maker
        startMarketMaker("BTCUSD", 60500);
        
        // Start ETH/USD Market Maker
        startMarketMaker("ETHUSD", 3200);
    }

    private void startMarketMaker(String symbol, long initialPrice) {
        new Thread(() -> {
            long lastPrice = initialPrice;
            while (true) {
                try {
                    // Randomly drift the price slightly
                    lastPrice += (random.nextInt(11) - 5); 
                    
                    // Place a Buy order slightly below last price
                    submit(symbol, OrderSide.BUY, lastPrice - 1 - random.nextInt(5));
                    
                    // Place a Sell order slightly above last price
                    submit(symbol, OrderSide.SELL, lastPrice + 1 + random.nextInt(5));
                    
                    TimeUnit.MILLISECONDS.sleep(150); // Fast trading
                } catch (Exception e) {
                    log.error("Bot error for {}: {}", symbol, e.getMessage());
                    try { TimeUnit.SECONDS.sleep(1); } catch (InterruptedException ignored) {}
                }
            }
        }, "Bot-" + symbol).start();
    }

    private void submit(String symbol, OrderSide side, long price) {
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
        System.out.println("==================================================");
        System.out.println("  Starting Custom gRPC Trading Bot");
        System.out.println("==================================================");
        LiquidityProviderBot bot = new LiquidityProviderBot("localhost", 9090);
        bot.start();
    }
}
