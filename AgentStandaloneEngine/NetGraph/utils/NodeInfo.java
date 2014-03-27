/*
 * Created on 14/02/2005
 *
 */
package NetGraph.utils;

import java.net.InetAddress;

import NetGraph.graph.components.discovery.ComponentDiscoveryDetails;

/**
 * @author Ohad
 *
 */
/** On stocke quelques infos sur une node*/
public class NodeInfo {
	public InetAddress IP;
	public ComponentDiscoveryDetails discoveryDetails;
	/** Constructeur */
	public NodeInfo(InetAddress IP,ComponentDiscoveryDetails details) {  		
		this.IP = IP;
		discoveryDetails = details;
	}
}
