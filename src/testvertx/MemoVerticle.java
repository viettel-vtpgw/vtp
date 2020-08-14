/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testvertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author anPV
 */
public class MemoVerticle extends AbstractVerticle {
    String name;
    public MemoVerticle(String name){
        this.name = name;
    }
    
    @Override
    public void start(Future<Void> startFuture) {
        System.out.println("MemoVerticle started!");
        
        SharedData sd = vertx.sharedData();
        LocalMap<String, String> map1 = sd.getLocalMap("mymap1");
        
        String val = map1.get("foo");
        if(val == null){
            //System.out.println("MemoVerticle Inserting...");
            System.out.println("MemoVerticle " + name + " Inserting... ");
            map1.put("foo", "bar"); // Strings are immutable so no need to copy
            
            //val = map1.get("foo");
            //System.out.println("MemoVerticle get value2 from map: " + val);            
        }
        else{
            int i = 0;
            do{
                System.out.println("MemoVerticle " + name + " get value from map " + i + ": " + val);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    //Logger.getLogger(MemoVerticle.class.getName()).log(Level.SEVERE, null, ex);
                }            
            } while(i < 5);
        }
    }

    @Override
    public void stop(Future stopFuture) throws Exception {
        System.out.println("MemoVerticle stopped!");
    }
}
