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
import org.springframework.beans.factory.annotation.Value;
import java.io.IOException;

@SpringBootApplication(scanBasePackages = "com.trading.engine")
public class TradingEngineApplication {

    @Value("${trading.bot.enabled:false}")
    private boolean botEnabled;

    public static void main(String[] args) {
        fixChronicleClasspath();
        SpringApplication.run(TradingEngineApplication.class, args);
    }

    private static void fixChronicleClasspath() {
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            java.util.Set<String> paths = new java.util.LinkedHashSet<>();
            
            while (cl != null) {
                if (cl instanceof java.net.URLClassLoader) {
                    for (java.net.URL url : ((java.net.URLClassLoader) cl).getURLs()) {
                        try {
                            paths.add(new java.io.File(url.toURI()).getAbsolutePath());
                        } catch (Exception e) {
                            paths.add(url.getPath());
                        }
                    }
                } else {
                    try {
                        java.lang.reflect.Method getUrlsMethod = cl.getClass().getMethod("getURLs");
                        java.net.URL[] urls = (java.net.URL[]) getUrlsMethod.invoke(cl);
                        if (urls != null) {
                            for (java.net.URL url : urls) {
                                try {
                                    paths.add(new java.io.File(url.toURI()).getAbsolutePath());
                                } catch (Exception e) {
                                    paths.add(url.getPath());
                                }
                            }
                        }
                    } catch (NoSuchMethodException ignored) {
                    } catch (Exception e) {
                        // ignore reflection errors
                    }
                }
                cl = cl.getParent();
            }
            
            if (!paths.isEmpty()) {
                String currentCp = System.getProperty("java.class.path");
                String separator = java.io.File.pathSeparator;
                StringBuilder newCp = new StringBuilder(currentCp == null ? "" : currentCp);
                for (String path : paths) {
                    if (newCp.length() > 0) {
                        newCp.append(separator);
                    }
                    newCp.append(path);
                }
                System.setProperty("java.class.path", newCp.toString());
                System.out.println("[INFO] Dynamic Classpath Resolver: Appended " + paths.size() + " entries to java.class.path.");
            }
        } catch (Exception e) {
            System.err.println("[WARNING] Failed to automatically fix Chronicle Map classpath: " + e.getMessage());
        }
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
            Thread.ofVirtual().name("gRPC-Server").start(() -> {
                try {
                    Server server = ServerBuilder.forPort(9090)
                            .addService(new TradingServiceImpl(engine, marketDataService))
                            .build();

                    System.out.println("Starting gRPC server on port 9090...");
                    server.start();

                    if (botEnabled) {
                        Thread.ofVirtual().name("Bot-Starter").start(() -> {
                            try {
                                // Wait 1 second for the server to be fully ready
                                Thread.sleep(1000);
                                System.out.println("Auto-starting Liquidity Provider Bot inside Java 21 Virtual Threads...");
                                LiquidityProviderBot bot = new LiquidityProviderBot("localhost", 9090);
                                bot.start();
                            } catch (Exception ex) {
                                System.err.println("Failed to auto-start trading bot: " + ex.getMessage());
                            }
                        });
                    }

                    server.awaitTermination();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        };
    }
}

