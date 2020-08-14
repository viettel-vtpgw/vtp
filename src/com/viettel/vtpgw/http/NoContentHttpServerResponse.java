package com.viettel.vtpgw.http;

import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;

public class NoContentHttpServerResponse extends AbstractHttpServerResponseWrapper implements HttpServerResponse {
	private static final Logger LOG = LogManager.getLogger(NoContentHttpServerResponse.class);
	boolean ended;
	MultiMap headers;
	HttpServerRequestWrapper request;

	public NoContentHttpServerResponse(HttpServerRequestWrapper request){
		this.request = request;
	}

	@Override
	public HttpServerResponse bodyEndHandler(Handler<Void> handler) {
		return this;
	}

	@Override
	public long bytesWritten() {
		return 0;
	}

	@Override
	public void close() {
		//EMPTY
	}

	@Override
	public boolean closed() {
		return false;
	}

	@Override
	public HttpServerResponse closeHandler(Handler<Void> handler) {
		return this;
	}

	@Override
	public HttpServerResponse drainHandler(Handler<Void> handler) {
		return this;
	}

	@Override
	public void end() {
		this.request.finishTransaction();
		this.request=null;
		ended=true;
	}

	@Override
	public void end(Buffer buff) {
		this.request.finishTransaction();
		this.request=null;
		ended=true;
	}

	@Override
	public void end(String buff) {
		this.request.finishTransaction();
		this.request=null;
		ended=true;
	}
	@Override
	public void end(String buff, String charset) {
		this.request.finishTransaction();
		this.request=null;
		ended=true;
	}
	@Override
	public boolean ended() {
		return ended;
	}

	@Override
	public HttpServerResponse exceptionHandler(Handler<Throwable> handler) {
		return this;
	}

	@Override
	public int getStatusCode() {
		return 204;
	}

	@Override
	public String getStatusMessage() {
		return HttpResponseStatus.NO_CONTENT.reasonPhrase();
	}

	@Override
	public MultiMap headers() {
		if(headers==null)headers = MultiMap.caseInsensitiveMultiMap();
		return headers;
	}

	@Override
	public HttpServerResponse headersEndHandler(Handler<Void> handler) {
		return this;
	}
	@Override
	public boolean headWritten() {
		return true;
	}

	@Override
	public boolean isChunked() {
		return false;
	}

	public void merge(Message<Buffer> msg) {
		MultiMap headers = msg.headers();
		String code = headers.get("-c");
		if (code != null) {
			setStatusCode(Integer.parseInt(code));
		}
		MultiMap target = headers();
		String alertSubject=null;
		String issue = null;

		for(Entry<String,String> e: headers.entries()){
			String key = e.getKey();
			switch(key){
			case "-s":
				alertSubject = e.getValue();
				break;
			case "-pl":
				issue = e.getValue();
				break;
			case "-l":
				try {

				} catch (Exception ex) {
					LOG.error("Invalid alert level", ex);
				}
				break;
			case "-c":
				try {
					setStatusCode(Integer.parseInt(e.getValue(),16));
				} catch (Exception ex) {
					LOG.error("Invalid status code", ex);
				}
				break;
			case"-e":
				try {
					setExternalCall(new Long(e.getValue()));
				} catch (Exception ex) {
					LOG.error("Invalid external call duration", ex);
				}
				break;
			case "-f":
				setFunc(e.getValue());
				break;
			case "-ps":
				setParams(e.getValue());
				break;
			default:
				if (!key.startsWith("-")) {
					target.set(key, e.getValue());
				}
			}
		}
                
		if(alertSubject!=null){

		}
		end(msg.body());
	}

	@Override
  public NoContentHttpServerResponse push(HttpMethod method, String path, Handler<AsyncResult<HttpServerResponse>> handler) {
  	return this;
  }

	@Override
  public NoContentHttpServerResponse push(HttpMethod method, String path, MultiMap headers,
      Handler<AsyncResult<HttpServerResponse>> handler) {
  	return this;
  }

	/*Vertx 3.3.x [[*/
  @Override
  public NoContentHttpServerResponse push(HttpMethod method, String host, String path,
      Handler<AsyncResult<HttpServerResponse>> handler) {
  	return this;
  }

	@Override
  public NoContentHttpServerResponse push(HttpMethod method, String host, String path, MultiMap headers,
      Handler<AsyncResult<HttpServerResponse>> handler) {
  	return this;
  }

	@Override
	public NoContentHttpServerResponse putHeader(CharSequence name, CharSequence value) {
		return this;
	}

	@Override
	public NoContentHttpServerResponse putHeader(CharSequence name, Iterable<CharSequence> values) {
		return this;
	}

	@Override
	public NoContentHttpServerResponse putHeader(String name, Iterable<String> values) {
		return this;
	}

	@Override
	public NoContentHttpServerResponse putHeader(String name, String value) {
		return this;
	}

	@Override
	public NoContentHttpServerResponse putTrailer(CharSequence name, CharSequence value) {
		return this;
	}

	@Override
	public NoContentHttpServerResponse putTrailer(CharSequence name, Iterable<CharSequence> values) {
		return this;
	}

	@Override
	public NoContentHttpServerResponse putTrailer(String name, Iterable<String> values) {
		return this;
	}

	@Override
	public NoContentHttpServerResponse putTrailer(String name, String value) {
		return this;
	}

	@Override
  public void reset(long code) {
  }

	@Override
	public NoContentHttpServerResponse sendFile(String filename, long offset, long length) {
		return this;
	}

	@Override
	public NoContentHttpServerResponse sendFile(String filename, long offset, long length, Handler<AsyncResult<Void>> handler) {
		return this;
	}

	@Override
	public NoContentHttpServerResponse setChunked(boolean chunked) {
		return this;
	}

	@Override
  protected void setExternalCall(Long externalCall) {
    request.setExternalCall(externalCall);
  }

	@Override
  protected void setFunc(String func) {
  	request.setFunc(func);
  }

	@Override
  protected void setParams(String params) {
  	request.setParams(params);
  }

  @Override
	public NoContentHttpServerResponse setStatusCode(int code) {
		return this;
	}
  @Override
	public NoContentHttpServerResponse setStatusMessage(String msg) {
		return this;
	}
  @Override
	public NoContentHttpServerResponse setWriteQueueMaxSize(int size) {
		return this;
	} 
  @Override
  public int streamId() {
  	return 0;
  }
  @Override
	public MultiMap trailers() {		
		if(headers==null){
			headers = MultiMap.caseInsensitiveMultiMap();
		}
		return headers;
	}
  @Override
	public NoContentHttpServerResponse write(Buffer buff) {
		return this;
	}
  @Override
	public NoContentHttpServerResponse write(String buff) {
		return this;
	}
  @Override
	public NoContentHttpServerResponse write(String buff, String charset) {
		return this;
	}
  @Override
	public NoContentHttpServerResponse writeContinue() {
		return this;
	}
  @Override
  public NoContentHttpServerResponse writeCustomFrame(int type, int flags, Buffer payload) {
  	return this;
  }
  /*]]Vertx 3.3.x*/
  @Override
	public boolean writeQueueFull() {
		return false;
	}   

    @Override
    public HttpServerResponse endHandler(Handler<Void> hndlr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
