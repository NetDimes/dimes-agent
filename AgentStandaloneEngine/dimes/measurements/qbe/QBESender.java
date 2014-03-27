/**
 * 
 */
package dimes.measurements.qbe;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;

import dimes.measurements.MeasurementType;
import dimes.measurements.Protocol;
import dimes.measurements.QBEHost;
import dimes.measurements.basicmeasurements.MeasurementCycle;
import dimes.measurements.basicmeasurements.MeasurementReceiver;
import dimes.measurements.nio.RawNetworkStack;
import dimes.measurements.nio.error.MeasurementException;
import dimes.measurements.nio.error.MeasurementInitializationException;
import dimes.measurements.nio.packet.Packet;
import dimes.measurements.nio.packet.PacketBuffer;
import dimes.measurements.nio.packet.builder.PacketBuilder;
import dimes.measurements.nio.packet.header.Header;
import dimes.measurements.nio.packet.header.ICMPv4Header;
import dimes.measurements.nio.packet.header.IPv4Header;
import dimes.measurements.nio.packet.header.Payload;
import dimes.measurements.nio.packet.header.TCPHeader;
import dimes.measurements.nio.packet.header.UDPHeader;
import dimes.measurements.operation.QbeOp;
import dimes.measurements.results.DimesQBEResults;
import dimes.scheduler.QPacketInTrain;
import dimes.scheduler.QPacketTrainOpParams;
import dimes.util.CommUtils;
import dimes.util.time.TimeUtils;

/**
 * @author Ohad Serfaty
 * An implementation of the QBE sender measurement module
 * 
 * Parameters are : 
 * a series of qpt commands to send packets
 * a series of wait commands to wait for a certain time
 * 
 * QPT arguments :
 * QPT <Iterations> <PacketTrainSize> <DelayBetweenPackets> <PerPacketDetails> <Train>
 * PerPacketDetails (comma seperated ): [Packet sizes] [protocols] [tos] [ttls] 
 * Train : [ IP1 , IP2 ,...]
 *
 *
 */
public class QBESender {

	
//	private MacLevelNetworkStack networkStack;
	private RawNetworkStack networkStack; //try this
	private PacketBuilder packetBuilder;
	private boolean firstTcpPacket =true;
	private int TcpPacketCounter = 0;
	private MeasurementReceiver receiver;
	InetAddress localAddress;

	public QBESender(boolean isBidirectional, int trainSize) throws MeasurementInitializationException{
//		networkStack = MacLevelNetworkStack.getInstance();
		networkStack = RawNetworkStack.getInstance(); //try this
//		String ipAddress = PublicIPInquirer.queryMachineIP();

		NetworkInterface iface = null;
		try {
		localAddress=InetAddress.getLocalHost();
		if (localAddress.isLoopbackAddress())
			for(Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();ifaces.hasMoreElements();){
				   iface = (NetworkInterface)ifaces.nextElement();
				   //System.out.println("Interface:"+ iface.getDisplayName());
				   InetAddress ia = null;
				   for(Enumeration<InetAddress> ips =    iface.getInetAddresses();ips.hasMoreElements();){
					   ia = (InetAddress)ips.nextElement();
					   if (ia.isLoopbackAddress()) {
						   continue;
					   }
//					   if (ia instanceof Inet6Address) continue;
					  localAddress = ia;
				   }
			}
		} catch (Exception e) {
			
		}
		
//		String[] temp=new String[]{"127.0.0.1"};//localAddress.getHostAddress()};
		try{
			networkStack.init(new String[]{localAddress.getHostAddress()});
			}catch(MeasurementInitializationException mie){
				networkStack.close();
			}
		packetBuilder = networkStack.getPacketBuilder();
		receiver = isBidirectional ? new MeasurementReceiver((short)trainSize, networkStack) : null;
		if (receiver != null) {
			receiver.setGetAllMode();
		}
	}

	public void sendPackets(QbeOp qbeOperation) throws Exception {
		if (qbeOperation.qptCommands.length-1 != qbeOperation.sleepPeriods.length)
			throw new MeasurementException("Sleep periods don't match with number of QPT commands.");
//		System.out.println("sourceIP (2):"+sourceIP);
		PacketBuffer buffer = new PacketBuffer(networkStack);
		
		// do iterations of QPT : 
		short idx = 1;
		int commandsNumber = qbeOperation.qptCommands.length;
		
		for (int i=0; i<commandsNumber; i++){
			QPacketTrainOpParams qpt = qbeOperation.qptCommands[i];
			
			// build the packets :
			//TcpPacketCounter=0;
			buffer.clear();
			
			InetAddress destIP = null;
			
			// TODO : check .
			for (int j=0; j<qpt.numberOfPacketsInTrain; j++)
			{
				IPv4Header   ipHeader = new IPv4Header();
				UDPHeader    udpHeader = new UDPHeader();
				ICMPv4Header icmpv4Header = new ICMPv4Header();
				// Galina: i create it here to have the size...
				TCPHeader tcpHeader1 = new TCPHeader();
				
				QPacketInTrain packet = (QPacketInTrain) qpt.packetsVector.get(j);
				//packet.setTrainId(runningTrainId);
				
				Payload payload = new Payload();
				
				byte[] proprietary = packet.getProprietary(qbeOperation.experimentUniqueId);
				payload.addLoad(proprietary);
				
				// should we do it or simply take the packetSize as extra size?
				int remainingBytes = packet.packetSize - ipHeader.getPacketLength() - proprietary.length;
				if (packet.protocol == UDPHeader.IP_PROTO_UDP) {
					remainingBytes -= udpHeader.getHeaderLength();
				} else if (packet.protocol == ICMPv4Header.IPv4_PROTO_ICMP) {
					remainingBytes -= icmpv4Header.getHeaderLength();
				} else {
					remainingBytes -= tcpHeader1.getHeaderLength();
				}
				
				if (remainingBytes > 0) {
					byte[] random = new byte[remainingBytes];
					Arrays.fill(random , (byte)0xcd);
					payload.addLoad(random);
				} else {
					remainingBytes = 0;
				}
				
				Packet packetToAdd = null;
				ipHeader.sourceIP = localAddress;
				ipHeader.destIP = InetAddress.getByName(packet.IP);
				
				// Galina : all packets in train go to the same place (so far at least)
				destIP = ipHeader.destIP;
				
				ipHeader.tos= (byte) packet.packetTos;
				ipHeader.ttl= (byte) packet.packetTTL;
				ipHeader.identity = (short) j;
					
				// removesyso    System.out.println("ipHeader.sourceIP "+ipHeader.sourceIP+" ipHeader.destIP"+ipHeader.destIP+"\n");
				if (packet.protocol == UDPHeader.IP_PROTO_UDP)
				{
					udpHeader.sourcePort = (short) packet.sourcePort;
					udpHeader.destPort = (short) packet.destPort;
					packetToAdd = packetBuilder.buildPacket(new Header[]{ipHeader , udpHeader,payload});
					packetToAdd.checkSumPosition = ipHeader.getHeaderLength()+6;
				}
				else if (packet.protocol == TCPHeader.IP_PROTOCOL_TCP){
					if (firstTcpPacket == true || (j%10==0)) {
							/* first packet is Syn packet */
						firstTcpPacket=false;
							byte[] optBtye = null;
							TCPHeader tcpHeader = new TCPHeader(
									/* short sourcePort */(short) packet.sourcePort,
									/* short destinationPort */(short) packet.destPort,
									/* int sequenceNumber */TcpPacketCounter++,
									/* int ackNumber */0x0,
									/* byte dataOffset */(byte) 5,
									/* byte tcpFlags */(byte) (TCPHeader.TCP_FLAG_MASK_SYN/*|TCPHeader.TCP_FLAG_MASK_RST*/),
									/* short window */(short) 101,
									/* short checksum */(short) 0,
									/* short urgentPointer */(short) 0,
									/* byte[] optionBytes */optBtye);

							packetToAdd = packetBuilder
									.buildPacket(new Header[] { ipHeader,
											tcpHeader, payload });
							TcpPacketCounter += packet.packetSize-28;		
						} else 
						{/* not the firts packet */
							/* first packet is Syn packet */
							
							byte[] optBtye = null;
							TCPHeader tcpHeader = new TCPHeader(
									/* short sourcePort */(short) packet.sourcePort,
									/* short destinationPort */(short) packet.destPort,
									/* int sequenceNumber */TcpPacketCounter,
									/* int ackNumber */0x0,
									/* byte dataOffset */(byte) 5,
									/* byte tcpFlags */(byte) 0 , /*TCPHeader.TCP_FLAG_MASK_SYN,*/
									/* short window */(short) 101,
									/* short checksum */(short) 0,
									/* short urgentPointer */(short) 0,
									/* byte[] optionBytes */optBtye);

							packetToAdd = packetBuilder
									.buildPacket(new Header[] { ipHeader,
											tcpHeader, payload });
							TcpPacketCounter += packet.packetSize-28;
						}
					packetToAdd.checkSumPosition = ipHeader.getHeaderLength()+16;
				}
				else if (packet.protocol == ICMPv4Header.IPv4_PROTO_ICMP){
					// removesyso    System.out.println("ICMP packet");
					icmpv4Header.type =ICMPv4Header.ICMP_ECHO_REQUEST  /*ICMPv4Header.ICMP_ECHO_REQUEST*/;
					icmpv4Header.code =0;
					icmpv4Header.checksum =0x0;
					icmpv4Header.identifier=0x0400;
					
					// Galina: set sequence to running index to identify replies
					icmpv4Header.sequence = (short)j;
					//icmpv4Header.sequence=(short)0xe100;
					
					//ipHeader.setIdentity((short)0x7482);
					
					packetToAdd = packetBuilder.buildPacket(new Header[]{ipHeader , icmpv4Header,payload});
					packetToAdd.checkSumPosition = ipHeader.getHeaderLength()+2;
				}
				else
				{
					System.out.println("invalid packet.protocol :" + packet.protocol);
				}
				// 8 last bytes of payload for time stamp
				packetToAdd.timeStampPosition = 
					packetToAdd.getHeadersSize()-remainingBytes-8;
				
				buffer.addPacket(0,packetToAdd);
			}
			
			LinkedList<MeasurementCycle> resList = null;
			if (receiver != null) {
				resList = new LinkedList<MeasurementCycle>();
				qpt.setResults(resList);
			}
			
			int iterations = qpt.iterations;
			for (short k = 1; k<=iterations;k++)
			{
				MeasurementCycle currentMeasurement = null;

				if (receiver != null) {
					currentMeasurement = new MeasurementCycle(idx, qpt.numberOfPacketsInTrain, localAddress, destIP);
					receiver.startNewTrace(currentMeasurement);
					resList.add(currentMeasurement);
				}
				
				//short id = buffer.getTrainID(); // test
				// update train ID
				buffer.setTrainID(k);
			
				// send the packets :
				long[] sendTimes = this.networkStack.send(buffer);
				if (receiver != null) {
					for (short j=0; j<qpt.numberOfPacketsInTrain; j++) {	
						currentMeasurement.addPacket((short) (j+1), sendTimes[j]);
					}
					idx += qpt.numberOfPacketsInTrain;
				}
					
				if (k<iterations) {
					QBESleep(qpt.delayBetweenPT);
					boolean status = buffer.addIdentityOffset((short)qpt.numberOfPacketsInTrain);
					if (status == false) {
						System.out.println("failed update offset/sequence " + k);
					}
				}
			}
			
			// sleep for the time that was scheduled :
			if (i<commandsNumber-1) {
				QBESleep(qbeOperation.sleepPeriods[i].longValue());
			}
		}
		
		QBESleep(1000);
		networkStack.close();
	}
	
	public LinkedList<DimesQBEResults> getResults(QbeOp qbeOperation) {
		int commandsNumber = qbeOperation.qptCommands.length;
		
		LinkedList<DimesQBEResults> results = new LinkedList<DimesQBEResults>();
		
		for (int i=0; i<commandsNumber; i++){
			QPacketTrainOpParams qpt = qbeOperation.qptCommands[i];
			LinkedList<MeasurementCycle> trains = qpt.getResults();
//			int iteration = 1;
			while (trains.size() > 0) {
//				MeasurementCycle cycle = (MeasurementCycle) trains.removeFirst();
//				LinkedList cycleResults = cycle.getQBEResults(qbeOperation.experimentUniqueId, iteration++);
				if (results != null) {
					// need to add ttl here?
					Iterator<DimesQBEResults> itr = results.iterator();
					while (itr.hasNext()) {
//						QBEHost host = (QBEHost) itr.next(); //BoazH 8.8.12 - this was the original line, I changed it to the one below as part of adding generics support. May have to change it back
						QBEHost host = (QBEHost) itr.next().getResult();
						int packetNum = host.getPacketNumber();
						QPacketInTrain packet = (QPacketInTrain) qpt.packetsVector.get(packetNum-1);
						if (packet != null) {
							host.setTTL(packet.packetTTL);
							try {
								InetAddress destIP = InetAddress.getByName(packet.IP);
								DimesQBEResults qbeResult = new DimesQBEResults(localAddress.getHostName(), localAddress.getHostAddress(), destIP.getHostName(),
										destIP.getHostAddress(), CommUtils.ipToLong(destIP.getHostAddress()), 
										-1, MeasurementType.getName(MeasurementType.DIMES_QBE), Protocol.getName(packet.protocol), TimeUtils.getTimeStamp(), TimeUtils.getLocalTime(), 1);
								
								qbeResult.setResult(host);
								results.add(qbeResult);
								System.out
								.println("QBESender gerResults() results: "
										+ results.size());
							} catch (Exception e) {
								
							}
						}
						System.out.println(host.toXML(""));
					}
				}
			}
		}
		return results;
	}
	
	private void QBESleep(long timeToSleep) throws Exception  {
		if (receiver != null) {
			networkStack.receive(timeToSleep, receiver);
		} else {
			Thread.sleep(timeToSleep);
		}
	}
}
