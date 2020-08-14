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
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

public class MainVerticle extends AbstractVerticle {
    private static final String SQL_CREATE_PAGES_TABLE = "create table if not exists Pages (Id integer identity primary key, Name varchar(255) unique, Content clob)";
    private static final String SQL_GET_PAGE = "select Id, Content from Pages where Name = ?";
    private static final String SQL_CREATE_PAGE = "insert into Pages values (NULL, ?, ?)";
    private static final String SQL_SAVE_PAGE = "update Pages set Content = ? where Id = ?";
    private static final String SQL_ALL_PAGES = "select Name from Pages";
    private static final String SQL_DELETE_PAGE = "delete from Pages where Id = ?";

    private JDBCClient dbClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);
    
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        //startFuture.complete();
        final String test = "";
        Buffer fullRequestBody = Buffer.buffer();
        Integer val = 1;

        //Future<Void> steps = prepareDatabase().compose(v -> startHttpServer());
        Future<Integer> steps = prepareDatabase().compose(v -> startHttpServer());
        steps.setHandler(ar -> {
            if (ar.succeeded()) {
                startFuture.complete();
                fullRequestBody.appendString("success");
                //test = "1";
                int k = val.intValue();   
                System.out.println("Outside value: " + k);
                
                k = ar.result();
                System.out.println("Async result: " + k);
                
            } else {     
                Throwable e = ar.cause();
                startFuture.fail(ar.cause());
                fullRequestBody.appendString("failed");
                System.out.println("Async result: " + e.getMessage());
            }
        });

        // check success???
    }

    private Future<Void> prepareDatabase() {
        /*
        Future<Void> future = Future.future();
        // (...)
        //future.complete();
        
        future.fail("unknown - error");
        
        return future;
        */
        
        
        Future<Void> future = Future.future();

        dbClient = JDBCClient.createShared(vertx, new JsonObject()
          .put("url", "jdbc:hsqldb:file:db/wiki")
          .put("driver_class", "org.hsqldb.jdbcDriver")
          .put("max_pool_size", 30));

        dbClient.getConnection(ar -> {
          if (ar.failed()) {
            LOGGER.error("Could not open a database connection", ar.cause());
            future.fail(ar.cause());
          } else {
            SQLConnection connection = ar.result();
            connection.execute(SQL_CREATE_PAGES_TABLE, create -> {
              connection.close();
              if (create.failed()) {
                LOGGER.error("Database preparation error", create.cause());
                future.fail(create.cause());
              } else {
                future.complete();
              }
            });
          }
        });

        return future;
        
    }

    private Future<Integer> startHttpServer() {
        Future<Integer> future = Future.future();
        // (...)
        future.complete(1);
        return future;
    }
}
