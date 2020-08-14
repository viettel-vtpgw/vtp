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


public class EventBusSenderVerticle extends AbstractVerticle {

    public void start(Future<Void> startFuture) {
        // send message to all verticles
        vertx.eventBus().publish("anAddress", "message 2");
        
        // send message to only one verticle
        vertx.eventBus().send   ("anAddress", "message 1");
    }
}