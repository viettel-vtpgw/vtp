/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testvertx;

/**
 *
 * @author anPV
 */
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;


public class EventBusReceiverVerticle extends AbstractVerticle {
    private String name = null;
    
    EventBusReceiverVerticle(String name) {
         this.name = name;
    }

    public void start(Future<Void> startFuture) {
        vertx.eventBus().consumer("anAddress", message -> {
            System.out.println(this.name + 
                " - received message: " +
                message.body());
        });
    }
}