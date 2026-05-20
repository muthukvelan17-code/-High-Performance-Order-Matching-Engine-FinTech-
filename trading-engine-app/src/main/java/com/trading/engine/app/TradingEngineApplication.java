package com.trading.engine.app;

import com.trading.engine.core.TradingEngine;
import com.trading.engine.grpc.TradingServiceImpl;
import com.trading.engine.marketdata.MarketDataService;
import com.trading.engine.persistence.ChronicleJournaler;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import java.io.IOException;

@SpringBootApplication(scanBasePackages = "com.trading.engine")
public class TradingEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradingEngineApplication.class, args);
    }

    @Bean
    public ChronicleJournaler chronicleJournaler() throws IOException {
        return new ChronicleJournaler();
    }

    @Bean
    public MarketDataService marketDataService() {
        return new MarketDataService();
    }

    @Bean
    public TradingEngine tradingEngine(ChronicleJournaler chronicleJournaler, MarketDataService marketDataService) {
        return new TradingEngine(chronicleJournaler, marketDataService);
    }

    @Bean
    public CommandLineRunner gRpcServerRunner(TradingEngine engine, MarketDataService marketDataService) {
        return args -> {
            new Thread(() -> {
                try {
                    Server server = ServerBuilder.forPort(9090)
                            .addService(new TradingServiceImpl(engine, marketDataService))
                            .build();

                    System.out.println("Starting gRPC server on port 9090...");
                    server.start();
                    server.awaitTermination();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        };
    }
}

