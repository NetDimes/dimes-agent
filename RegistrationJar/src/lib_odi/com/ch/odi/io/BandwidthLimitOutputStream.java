package lib_odi.com.ch.odi.io;

import java.io.IOException;
import java.io.OutputStream;
/**
 * Description: This in an output stream that limits throughput
 * Copyright:    Copyright (c) 2001
 * @author Ortwin Gl?ck
 * @version 1.0
 */

public class BandwidthLimitOutputStream extends java.io.PrintStream {
  private int time_limit = 500;
  private int bytes_limit = 2000;

  private int bytesWritten;
  private long statsBaseTime;

  /**
   * Creates a Stream
   *  The default limit is 9600 bytes per second
   *  @param out The underlying output stream
   */
  public BandwidthLimitOutputStream(OutputStream out, int rate) {
   super(out);
   bytesWritten=0;
   statsBaseTime=System.currentTimeMillis();
   
   if (rate > 1)//minimum
       this.bytes_limit = rate*this.time_limit;
  }
//check  
/*  public BandwidthLimitOutputStream(OutputStream out) {
      super(out);
      bytesWritten=0;
      statsBaseTime=System.currentTimeMillis();
      this.bytes_limit=2000;
      this.time_limit=500;
     }
*/  

  /**
   *  Sets the bandwidth limit
   *  Sample: bytes=300, time=500 means you can not write more than 300 bytes in 500 milliseconds
   */
  public void setLimit(int bytes, int time) {
   this.bytes_limit=bytes;
   this.time_limit=time;
  }


  public void write(int b) {
   waitForBytes(1);
   bytesWritten++;
   super.write(b);
  }

  public void write(byte[] b) throws IOException {
   write(b,0,b.length);
  }

  public void write(byte[] b, int off, int len)  {
   int avail=availableCapacity();
   if (avail>=len) {
    bytesWritten+=len;
    super.write(b, off, len);
   } else {
    int offset=0;
    while (offset<len) {
     bytesWritten+=avail;
     super.write(b,off+offset,avail);
     offset+=avail;
     waitForBytes(bytes_limit);
     avail=availableCapacity();
     if (avail>len-offset) avail=len-offset;
    }
   }
  }

  private void waitForBytes(int n) {
   long endTime=System.currentTimeMillis()+getTimeToWrite(n);
   while (endTime>System.currentTimeMillis()) {
    try {
     long wait=endTime-System.currentTimeMillis();
     Thread.sleep(wait);
    } catch (InterruptedException e) {  }
   }
  }

  /**
   * Returns the time it takes to write the specified amount of bytes
   * If the underlying stream blocks it will take longer!
   * @param no_bytes The number of bytes
   * @returns the time to wait in seconds.
   */
  private int getTimeToWrite(int no_bytes) {
   long remain=0;
   //bytes we can write now
   int bytesToWaitFor=no_bytes - availableCapacity();
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

  private int availableCapacity() {
   long timeSpent=System.currentTimeMillis()-statsBaseTime;
   if (timeSpent>=time_limit) resetStats();
   int a=bytes_limit-bytesWritten;
   if (a<0) a=0;
   return a;
  }

  private void resetStats() {
   statsBaseTime=System.currentTimeMillis();
   bytesWritten=0;
  }

}