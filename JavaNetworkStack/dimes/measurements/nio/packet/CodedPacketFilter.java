package dimes.measurements.nio.packet;

import java.util.Arrays;

/**
 * @author Ohad Serfaty
 *
 *<p>
 * A pre-coded Packet Filter to be used with a Mac level network stack. A filter
 * can be generated by running a tcpdump application (or windump in windows) with the 
 * following structure :
 * <p>
 *   windump -dd [expression]
 *   <br><br>
 *   For example , running windump -dd icmp , will give you an int array that matched the 
 *    CodedPacketFilter.icmpFilterPattern int array.
 *    </p>
 *    <br>
 *    The integer array is actually a bpf , a byte program to perform a fast
 *    packet filtering in the driver.
 * 
 *
 */
public class CodedPacketFilter {

	public final int[][] filterDescription;

	/**
	 * Constructor for a PacketFilter
	 * 
	 * @param filterDescription an int array containing the program to be executed on 
	 * the packet.
	 */
	public CodedPacketFilter(int [][] filterDescription){
		this.filterDescription = filterDescription;
	}

	public static final int[][] icmpFilterPattern = {
			{ 0x28, 0, 0, 0x0000000c },
			{ 0x15, 0, 3, 0x00000800 },
			{ 0x30, 0, 0, 0x00000017 },
			{ 0x15, 0, 1, 0x00000001 },
			{ 0x6, 0, 0, 0x00000060 },
			{ 0x6, 0, 0, 0x00000000 } };
	
	public static final int[][] udpFilterPattern = {
		{ 0x28, 0, 0, 0x0000000c },
		{ 0x15, 0, 2, 0x000086dd },
		{ 0x30, 0, 0, 0x00000014 },
		{ 0x15, 3, 4, 0x00000011 },
		{ 0x15, 0, 3, 0x00000800 },
		{ 0x30, 0, 0, 0x00000017 },
		{ 0x15, 0, 1, 0x00000011 },
		{ 0x6, 0, 0, 0x00000060 },
		{ 0x6, 0, 0, 0x00000000 },
	};
	
	public static final int[][] udpPortFilterPattern = {
		{ 0x28, 0, 0, 0x0000000c },
		{ 0x15, 0, 6, 0x000086dd },
		{ 0x30, 0, 0, 0x00000014 },
		{ 0x15, 0, 15, 0x00000011 },
		{ 0x28, 0, 0, 0x00000036 },
		{ 0x15, 12, 0, 0x00001e61 },		// 5:port = 7777
		{ 0x28, 0, 0, 0x00000038 },
		{ 0x15, 10, 11, 0x00001e61 },		// 7:port = 7777
		{ 0x15, 0, 10, 0x00000800 },
		{ 0x30, 0, 0, 0x00000017 },
		{ 0x15, 0, 8, 0x00000011 },
		{ 0x28, 0, 0, 0x00000014 },
		{ 0x45, 6, 0, 0x00001fff },
		{ 0xb1, 0, 0, 0x0000000e },
		{ 0x48, 0, 0, 0x0000000e },
		{ 0x15, 2, 0, 0x00001e61 },		// 15:port = 7777
		{ 0x48, 0, 0, 0x00000010 },
		{ 0x15, 0, 1, 0x00001e61 },		// 17:port = 7777
		{ 0x6, 0, 0, 0x00000060 },
		{ 0x6, 0, 0, 0x00000000 },
	};
	/*  WinDump.exe  -dd    icmp or udp or tcp
		WinDump.exe: listening on \Device\NPF_GenericDialupAdapter*/
	public static final int[][] tcpUdpIcmpFilterPattern = {

		{ 0x28, 0, 0, 0x0000000c },
		{ 0x15, 0, 3, 0x00000800 },
		{ 0x30, 0, 0, 0x00000017 },
		{ 0x15, 5, 0, 0x00000001 },
		{ 0x15, 4, 3, 0x00000011 },
		{ 0x15, 0, 4, 0x000086dd },
		{ 0x30, 0, 0, 0x00000014 },
		{ 0x15, 1, 0, 0x00000011 },
		{ 0x15, 0, 1, 0x00000006 },
		{ 0x6, 0, 0, 0x00000060 },
		{ 0x6, 0, 0, 0x00000000 },
	};
	
	/*	WinDump.exe  -dd    icmp or udp
		WinDump.exe: listening on \Device\NPF_GenericDialupAdapter */	
	public static final int[][] UdpIcmpFilterPattern = {
		{ 0x28, 0, 0, 0x0000000c },
		{ 0x15, 0, 3, 0x00000800 },
		{ 0x30, 0, 0, 0x00000017 },
		{ 0x15, 4, 0, 0x00000001 },
		{ 0x15, 3, 4, 0x00000011 },
		{ 0x15, 0, 3, 0x000086dd },
		{ 0x30, 0, 0, 0x00000014 },
		{ 0x15, 0, 1, 0x00000011 },
		{ 0x6, 0, 0, 0x00000060 },
		{ 0x6, 0, 0, 0x00000000 },
	};
	

	/**
	 * an Icmp bpf filter
	 */
	public static final CodedPacketFilter ICMP_FILTER = new CodedPacketFilter(icmpFilterPattern);
	
	public static final CodedPacketFilter UDP_FILTER = new CodedPacketFilter(udpFilterPattern);
	
	public static final CodedPacketFilter UDP_ICMP_FILTER = new CodedPacketFilter(UdpIcmpFilterPattern);
	public static final CodedPacketFilter TCP_UDP_ICMP_FILTER = new CodedPacketFilter(tcpUdpIcmpFilterPattern);

	
	public static final CodedPacketFilter getUdpPortFilter(int port){
		int[][] filterPattern = copyIntArray(udpPortFilterPattern);
		filterPattern[5][3] = port;
		filterPattern[7][3] = port;
		filterPattern[15][3] = port;
		filterPattern[17][3] = port;
		return new CodedPacketFilter(filterPattern);
	}
	
	private static int[][] copyIntArray(int[][] array){
		int[][] result = new int[array.length][];
		for (int i=0; i<array.length; i++){
			int[] arrayToCopy = array[i];
			int[] resultInner = new int[arrayToCopy.length];
			result[i] = resultInner;
			for (int j=0; j<arrayToCopy.length; j++)
				result[i][j] = arrayToCopy[j];
		}
		return result;
	}

}
