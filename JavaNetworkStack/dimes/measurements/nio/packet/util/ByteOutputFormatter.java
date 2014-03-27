package dimes.measurements.nio.packet.util;

import java.nio.ByteBuffer;

/**
 * @author Ohad Serfaty
 *
 * A utility for presenting primitive types in a hex formatted string. 
 * 
 */
public class ByteOutputFormatter {

	/**
	 * Convert a byte array into a hex-formatted string (as 01-A7-98...)
	 * 
	 * @param byteArray a byte array to be converted to a string
	 * @return a string hex representation
	 */
	public static String toHexString ( byte[] byteArray )
	   {
	   StringBuffer sb = new StringBuffer( byteArray.length * 2 );
	   for ( int i=0; i<byteArray.length; i++ )
	      {
	      // look up high nibble char
	      sb.append( hexChar [( byteArray[i] & 0xf0 ) >>> 4] );

	      // look up low nibble char
	      sb.append( hexChar [byteArray[i] & 0x0f] );
	      
	      if (i<byteArray.length-1)
	    	  sb.append('-');
	      }
	   return sb.toString();
	   }

	
//	 table to convert a nibble to a hex char.
	static char[] hexChar = {
	   '0' , '1' , '2' , '3' ,
	   '4' , '5' , '6' , '7' ,
	   '8' , '9' , 'a' , 'b' ,
	   'c' , 'd' , 'e' , 'f'};


	/**
	 * convert a short to a hex string
	 * 
	 * @param s short parameter
	 * @return a two character string with the hex representation of the short value.
	 */
	public static String toHexString(short s) {
		ByteBuffer buf = ByteBuffer.allocate(2);
		buf.putShort(s);
		return toHexString(buf.array());
	}
	
	

}
