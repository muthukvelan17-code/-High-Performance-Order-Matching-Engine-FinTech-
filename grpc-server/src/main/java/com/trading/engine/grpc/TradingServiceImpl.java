package com.trading.engine.grpc;

import com.trading.engine.core.TradingEngine;
import com.trading.engine.core.model.Order;
import com.trading.engine.core.model.OrderSide;
import com.trading.engine.core.model.OrderType;
import com.trading.engine.marketdata.MarketDataService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TradingServiceImpl extends TradingServiceGrpc.TradingServiceImplBase {
    private final TradingEngine engine;
    private final MarketDataService marketDataService;

    @Override
    public void submitOrder(OrderRequest request, StreamObserver<OrderResponse> responseObserver) {
        // Edge Risk Validation
        if (request.getQuantity() <= 0) {
            log.warn("Rejected order {}: Quantity must be > 0", request.getOrderId());
            responseObserver.onNext(OrderResponse.newBuilder()
                    .setSuccess(false)
                    .setOrderId(request.getOrderId())
                    .build());
            responseObserver.onCompleted();
            return;
        }

        if (request.getPrice() <= 0 && request.getType() == com.trading.engine.grpc.OrderType.LIMIT) {
            log.warn("Rejected order {}: Limit price must be > 0", request.getOrderId());
            responseObserver.onNext(OrderResponse.newBuilder()
                    .setSuccess(false)
                    .setOrderId(request.getOrderId())
                    .build());
            responseObserver.onCompleted();
            return;
        }

        Order order = Order.builder()
                .orderId(request.getOrderId())
                .symbol(request.getSymbol())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .remainingQuantity(request.getQuantity())
                .side(request.getSide() == com.trading.engine.grpc.OrderSide.BUY ? OrderSide.BUY : OrderSide.SELL)
                .type(translateType(request.getType()))
                .userId(request.getUserId())
                .timestamp(System.currentTimeMillis())
                .build();

        engine.submitOrder(order);

        responseObserver.onNext(OrderResponse.newBuilder()
                .setSuccess(true)
                .setOrderId(request.getOrderId())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void cancelOrder(CancelOrderRequest request, StreamObserver<OrderResponse> responseObserver) {
        engine.cancelOrder(request.getOrderId(), request.getSymbol());
        
        responseObserver.onNext(OrderResponse.newBuilder()
                .setSuccess(true)
                .setOrderId(request.getOrderId())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void streamMarketData(MarketDataRequest request, StreamObserver<MarketDataUpdate> responseObserver) {
        io.grpc.stub.ServerCallStreamObserver<MarketDataUpdate> serverObserver = 
            (io.grpc.stub.ServerCallStreamObserver<MarketDataUpdate>) responseObserver;
        
        marketDataService.streamMarketData(request.getSymbol())
                .subscribe(update -> {
                    if (serverObserver.isReady()) {
                        serverObserver.onNext(update);
                    }
                }, 
                serverObserver::onError, 
                serverObserver::onCompleted);
    }

    private OrderType translateType(com.trading.engine.grpc.OrderType type) {
        return switch (type) {
            case LIMIT -> OrderType.LIMIT;
            case MARKET -> OrderType.MARKET;
            case IOC -> OrderType.IOC;
            default -> OrderType.LIMIT;
        };
    }
}
