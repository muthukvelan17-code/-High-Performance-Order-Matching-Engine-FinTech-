package com.trading.engine.app;

import com.trading.engine.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;

public class TestClient {
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9099)
                .usePlaintext()
                .build();
                
                try{
                    TradingServiceGrpc.TradingServiceBlockingstub = TradingServiceGrpc.newBlockingStub(channel);

                    OrderRequest request = OrderRequest.newBuilder()
                    .setSymbol("AAPL");
                    .setQuantity(10)
                    .setPrice(150.0)
                    .build();
                }
                   system.out.println("Sending request to TradingServer...");
                //make the actual gRPC remote procedure call
                OrderResponse response = stub.placeOrder(request);
                //Log the response from the server
                System.out.println("Response recieved from server: " + response.toString());
                }
                catch(Exception e) {
                    system.err.println("RPC failed : "+ e.getMessage());
                    e.printStackTrace();
                }finally{
                    //Always clean up and shutdown the channel gracefully
                    try{
                        System.out.println("Shutting down channel..");
                        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
                    } catch(Interrupted Exception e) {
                        system.err.println("channel shutdown interrupted")
                    }
                }
                for (int i=1; i<=5; i++){
                    //Send a Buy Order
                    OrderResponse  buyResponse = stub.submitOrder(OrderRequest.newBuilder())
                    .setOrderId(i)
                    .setSymbol("BTCUSD")
                    .setPrice(50000 + i)
                    .setQuantity(100)
                    .setSide(Orderside.BUY)
                    .setType(OrderType.LIMIT)
                    .setUserId(101)
                    .build();
                    system.out.println("Buy Order" + i+ "status: " + buyResponse.getMessage());
                    //small pause to guarantee the Buy Order hits the order book first 
                    try{
                        Thread.sleep(50);
                    } catch(InterruptedException e)
                    Thread.currentThread().interrupt();
                }
                //send a matching Sell Order
                OrderResponse sellResponse = stub.submitOrder(OrderRequest.newBuilder())
                .setOrderId(i + 100)
                .setSymbol("BTCUSD")
                .setPrice(50000 + i) //Matching Price
                .setQuantity(100) //Matching quantity
                .setSide(OrderSide.SELL)
                .setType(OrderType.LIMIT)
                .setUserId(102)// Different user to prevent self - trading match errors
                .build();
                System.ou.println("Sell Order  " + (i+100) + "status: " + sellResponse.getMessage());

                System.out.println("------------------");
               }
               System.out.println("Test complete. check the server terminal for matching logs!");
               //proper gRPC shudown sequence 
               channel.shutdown();
               try{
                if (!channel.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)){
                    channel.shutdownNow();
                } catch(Interrupted Exception e) {
                    channel.shutdownNow();
                    Thread.currentThread().interrupt();     
             }
               }
               



                
