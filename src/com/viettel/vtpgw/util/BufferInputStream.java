package com.viettel.vtpgw.util;

import java.io.IOException;
import java.io.InputStream;

import io.vertx.core.buffer.Buffer;

public class BufferInputStream extends InputStream {
  private final Buffer buff;
  int ofs = 0;

  public BufferInputStream(Buffer buff) {
    this.buff = buff;
    ofs = 0;
  }

  public BufferInputStream(Buffer buff, int ofs) {
    this.buff = buff;
    this.ofs = ofs;
  }

  @Override
  public int read() throws IOException {
    if (ofs >= buff.length())
      return -1;
    return buff.getByte(ofs++) & 0xFF;
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    if (ofs >= buff.length())
      return -1;
    int end = ofs + len;
    if (end > buff.length())
      end = buff.length();
    buff.getBytes(ofs, end, b, off);
    int rs = end - ofs;
    ofs = end;
    return rs;
  }  
}
