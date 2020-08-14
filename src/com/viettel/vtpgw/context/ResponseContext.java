package com.viettel.vtpgw.context;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;

/**
 * Created by dinhnn on 5/15/16.
 */
public abstract class ResponseContext {

  public abstract void end();

  public abstract void end(Buffer buff);

  public abstract void end(String body);

  public abstract boolean ended();

  public abstract MultiMap headers();

  public void merge(Message<Buffer> msg) {
  }

  public abstract ResponseContext putHeader(CharSequence header, CharSequence value);

  protected abstract void setExternalCall(Long externalCall);

  protected abstract void setFunc(String func);

  protected abstract void setParams(String params);

  public abstract ResponseContext setStatusCode(int code);

}
