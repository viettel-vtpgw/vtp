/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testvertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;

public class MyVerticle extends AbstractVerticle {
    String name;
    public MyVerticle(String name){
        this.name = name;
    }
    
    @Override
    public void start(Future<Void> startFuture) {
        System.out.println("MyVerticle started!");
        
        SharedData sd = vertx.sharedData();
        LocalMap<String, String> map1 = sd.getLocalMap("mymap1");
        
        String val = map1.get("foo");
        int i = 0;
        do{
            System.out.println("MyVerticle " + name + " get value from map " + i + ": " + val);
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                //Logger.getLogger(MemoVerticle.class.getName()).log(Level.SEVERE, null, ex);
            }            
        } while(i < 100);
        
//        if(val == null){
//            System.out.println("Inserting...");
//            map1.put("foo", "bar"); // Strings are immutable so no need to copy
//        }
        
    }

    @Override
    public void stop(Future stopFuture) throws Exception {
        System.out.println("MyVerticle stopped!");
    }

}
