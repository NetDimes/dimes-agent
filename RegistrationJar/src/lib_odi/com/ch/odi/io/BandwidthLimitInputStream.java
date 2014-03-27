package lib_odi.com.ch.odi.io;

import java.io.IOException;
import java.io.InputStream;
/**
 * Description: This is an input stream that limits throughput.
 * Copyright:    Copyright (c) 2001
 * @author Ortwin Gl?ck
 * @version 1.0
 */

public class BandwidthLimitInputStream extends java.io.InputStream {

  private InputStream in;
  private int time_limit = 2000;
  private int bytes_limit = 500;

  private int bytesRead;
  private long statsBaseTime;

  /**
   *  Creates a Stream
   *  The default limit is 9600 bytes per second
   *  @param in The underlying input stream
   */
  public BandwidthLimitInputStream(InputStream in, int rate) {
   this.in=in;
   bytesRead=0;
   statsBaseTime=System.currentTimeMillis();

   if (rate > 1)//minimum
       this.bytes_limit = rate*this.time_limit;
  }
  //check
/*  public BandwidthLimitInputStream(InputStream in) {
      this.in=in;
      bytesRead=0;
      statsBaseTime=System.currentTimeMillis();
      this.bytes_limit=2000;
      this.time_limit=500;
     }
*/
  /**
   *  Sets the bandwidth limit
   *  Sample: bytes=300, time=500 means you can not read more than 300 bytes within 500 milliseconds
   */
  public void setLimit(int bytes, int time) {
   this.bytes_limit=bytes;
   this.time_limit=time;
  }

  public boolean markSupported() {
   return in.markSupported();
  }

  public void mark(int readlimit) {
   in.mark(readlimit);
  }

  public void reset() throws IOException {
   in.reset();
  }

  public int read() throws IOException {
   int b=in.read();
   waitForBytes(1);
   bytesRead++;
   return b;
  }

  public int read(byte[] b) throws IOException  {
   int r=in.read(b);
   if (r!=-1) waitForBytes(r);
   bytesRead+=r;
   return r;
  }

  public int read(byte[] b, int off, int len)  throws IOException  {
   int r=in.read(b,off, len);
   if (r!=-1) waitForBytes(r);
   bytesRead+=r;
   return r;
  }

  private void waitForBytes(int n) {
   long endTime=System.currentTimeMillis()+getTimeToRead(n);
   while (endTime>System.currentTimeMillis()) {
    try {
     long wait=endTime-System.currentTimeMillis();
     Thread.sleep(wait);
    } catch (InterruptedException e) {  }
   }
  }


  private void resetStats() {
   statsBaseTime=System.currentTimeMillis();
   bytesRead=0;
  }

  public int available() {
   long timeSpent=System.currentTimeMillis()-statsBaseTime;
   if (timeSpent>time_limit) resetStats();
   int a=bytes_limit-bytesRead;
   if (a<0) a=0;
   return a;
  }

  /**
   *  Returns the time it takes <em>at least</em> to read the specified amount of bytes.
   *  If the underlying stream blocks it will take longer!
   *  @param no_bytes The number of bytes
   *  @returns the time to wait in seconds.
   */
  private int getTimeToRead(int no_bytes) {
   long remain=0;
   int bytesToWaitFor=no_bytes - available();
   if (bytesToWaitFor<0) {
    bytesToWaitFor=0;
   } else {
    //time to wait till next chunk
    remain=time_limit-(System.currentTimeMillis()-statsBaseTime);
    if (remain<0) remain=0;
    bytesToWaitFor-=bytes_limit;
    if (bytesToWaitFor<0) bytesToWaitFor=0;
   }
   return (int) (time_limit * bytesToWaitFor / bytes_limit + remain);
  }

  public long skip(long n) throws IOException {
   return in.skip(n);
  }

  public void close() throws IOException {
   in.close();
   in=null;
  }
}