#ifndef _ETHERNET_UTILITIES_H
#define _ETHERNET_UTILITIES_H

/***************************************************************************
 *   Copyright (C) 2006 by Ohad Serfaty , DIMES Team                       *
 *   ohad@eng.tau.ac.il  , support@netdimes.org                            *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 ***************************************************************************/
#ifdef DIMES_WINDOWS
#include <winsock2.h>
#include <windows.h>
#else
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <string.h>
#endif

#define ETH_HDR_LEN	14
#define ETH_ADDR_LEN	6

#define ETH_TYPE_PUP	0x0200		/* PUP protocol */
#define ETH_TYPE_IP	0x0800		/* IP protocol */
#define ETH_TYPE_ARP	0x0806		/* address resolution protocol */
#define ETH_TYPE_REVARP	0x8035		/* reverse addr resolution protocol */
#define ETH_TYPE_8021Q	0x8100		/* IEEE 802.1Q VLAN tagging */
#define ETH_TYPE_IPV6	0x86DD		/* IPv6 protocol */
#define ETH_TYPE_MPLS	0x8847		/* MPLS */
#define ETH_TYPE_MPLS_MCAST	0x8848	/* MPLS Multicast */
#define ETH_TYPE_PPPOEDISC	0x8863	/* PPP Over Ethernet Discovery Stage */
#define ETH_TYPE_PPPOE	0x8864		/* PPP Over Ethernet Session Stage */
#define ETH_TYPE_LOOPBACK	0x9000	/* used to test interfaces */

#define IP_ADDR_LEN	4		/* IP address length */
#define IP_ADDR_BITS	32		/* IP address bits */

#define IP_HDR_LEN	20		/* base IP header length */
#define IP_OPT_LEN	2		/* base IP option length */
#define IP_OPT_LEN_MAX	40
#define IP_HDR_LEN_MAX	(IP_HDR_LEN + IP_OPT_LEN_MAX)

#define IP_LEN_MAX	65535
#define IP_LEN_MIN	IP_HDR_LEN

#define	IP_PROTO_IP		0		/* dummy for IP */
#define IP_PROTO_HOPOPTS	IP_PROTO_IP	/* IPv6 hop-by-hop options */
#define	IP_PROTO_ICMP		1		/* ICMP */
#define	IP_PROTO_IGMP		2		/* IGMP */
#define IP_PROTO_GGP		3		/* gateway-gateway protocol */
#define	IP_PROTO_IPIP		4		/* IP in IP */
#define IP_PROTO_ST		5		/* ST datagram mode */
#define	IP_PROTO_TCP		6		/* TCP */
#define IP_PROTO_CBT		7		/* CBT */
#define	IP_PROTO_EGP		8		/* exterior gateway protocol */
#define IP_PROTO_IGP		9		/* interior gateway protocol */
#define IP_PROTO_BBNRCC		10		/* BBN RCC monitoring */
#define IP_PROTO_NVP		11		/* Network Voice Protocol */
#define	IP_PROTO_PUP		12		/* PARC universal packet */
#define IP_PROTO_ARGUS		13		/* ARGUS */
#define IP_PROTO_EMCON		14		/* EMCON */
#define IP_PROTO_XNET		15		/* Cross Net Debugger */
#define IP_PROTO_CHAOS		16		/* Chaos */
#define	IP_PROTO_UDP		17		/* UDP */
#define IP_PROTO_MUX		18		/* multiplexing */
#define IP_PROTO_DCNMEAS	19		/* DCN measurement */
#define IP_PROTO_HMP		20		/* Host Monitoring Protocol */
#define IP_PROTO_PRM		21		/* Packet Radio Measurement */
#define	IP_PROTO_IDP		22		/* Xerox NS IDP */
#define IP_PROTO_TRUNK1		23		/* Trunk-1 */
#define IP_PROTO_TRUNK2		24		/* Trunk-2 */
#define IP_PROTO_LEAF1		25		/* Leaf-1 */
#define IP_PROTO_LEAF2		26		/* Leaf-2 */
#define IP_PROTO_RDP		27		/* "Reliable Datagram" proto */
#define IP_PROTO_IRTP		28		/* Inet Reliable Transaction */
#define	IP_PROTO_TP		29 		/* ISO TP class 4 */
#define IP_PROTO_NETBLT		30		/* Bulk Data Transfer */
#define IP_PROTO_MFPNSP		31		/* MFE Network Services */
#define IP_PROTO_MERITINP	32		/* Merit Internodal Protocol */
#define IP_PROTO_SEP		33		/* Sequential Exchange proto */
#define IP_PROTO_3PC		34		/* Third Party Connect proto */
#define IP_PROTO_IDPR		35		/* Interdomain Policy Route */
#define IP_PROTO_XTP		36		/* Xpress Transfer Protocol */
#define IP_PROTO_DDP		37		/* Datagram Delivery Proto */
#define IP_PROTO_CMTP		38		/* IDPR Ctrl Message Trans */
#define IP_PROTO_TPPP		39		/* TP++ Transport Protocol */
#define IP_PROTO_IL		40		/* IL Transport Protocol */
#define IP_PROTO_IPV6		41		/* IPv6 */
#define IP_PROTO_SDRP		42		/* Source Demand Routing */
#define IP_PROTO_ROUTING	43		/* IPv6 routing header */
#define IP_PROTO_FRAGMENT	44		/* IPv6 fragmentation header */
#define IP_PROTO_RSVP		46		/* Reservation protocol */
#define	IP_PROTO_GRE		47		/* General Routing Encap */
#define IP_PROTO_MHRP		48		/* Mobile Host Routing */
#define IP_PROTO_ENA		49		/* ENA */
#define	IP_PROTO_ESP		50		/* Encap Security Payload */
#define	IP_PROTO_AH		51		/* Authentication Header */
#define IP_PROTO_INLSP		52		/* Integated Net Layer Sec */
#define IP_PROTO_SWIPE		53		/* SWIPE */
#define IP_PROTO_NARP		54		/* NBMA Address Resolution */
#define	IP_PROTO_MOBILE		55		/* Mobile IP, RFC 2004 */
#define IP_PROTO_TLSP		56		/* Transport Layer Security */
#define IP_PROTO_SKIP		57		/* SKIP */
#define IP_PROTO_ICMPV6		58		/* ICMP for IPv6 */
#define IP_PROTO_NONE		59		/* IPv6 no next header */
#define IP_PROTO_DSTOPTS	60		/* IPv6 destination options */
#define IP_PROTO_ANYHOST	61		/* any host internal proto */
#define IP_PROTO_CFTP		62		/* CFTP */
#define IP_PROTO_ANYNET		63		/* any local network */
#define IP_PROTO_EXPAK		64		/* SATNET and Backroom EXPAK */
#define IP_PROTO_KRYPTOLAN	65		/* Kryptolan */
#define IP_PROTO_RVD		66		/* MIT Remote Virtual Disk */
#define IP_PROTO_IPPC		67		/* Inet Pluribus Packet Core */
#define IP_PROTO_DISTFS		68		/* any distributed fs */
#define IP_PROTO_SATMON		69		/* SATNET Monitoring */
#define IP_PROTO_VISA		70		/* VISA Protocol */
#define IP_PROTO_IPCV		71		/* Inet Packet Core Utility */
#define IP_PROTO_CPNX		72		/* Comp Proto Net Executive */
#define IP_PROTO_CPHB		73		/* Comp Protocol Heart Beat */
#define IP_PROTO_WSN		74		/* Wang Span Network */
#define IP_PROTO_PVP		75		/* Packet Video Protocol */
#define IP_PROTO_BRSATMON	76		/* Backroom SATNET Monitor */
#define IP_PROTO_SUNND		77		/* SUN ND Protocol */
#define IP_PROTO_WBMON		78		/* WIDEBAND Monitoring */
#define IP_PROTO_WBEXPAK	79		/* WIDEBAND EXPAK */
#define	IP_PROTO_EON		80		/* ISO CNLP */
#define IP_PROTO_VMTP		81		/* Versatile Msg Transport*/
#define IP_PROTO_SVMTP		82		/* Secure VMTP */
#define IP_PROTO_VINES		83		/* VINES */
#define IP_PROTO_TTP		84		/* TTP */
#define IP_PROTO_NSFIGP		85		/* NSFNET-IGP */
#define IP_PROTO_DGP		86		/* Dissimilar Gateway Proto */
#define IP_PROTO_TCF		87		/* TCF */
#define IP_PROTO_EIGRP		88		/* EIGRP */
#define IP_PROTO_OSPF		89		/* Open Shortest Path First */
#define IP_PROTO_SPRITERPC	90		/* Sprite RPC Protocol */
#define IP_PROTO_LARP		91		/* Locus Address Resolution */
#define IP_PROTO_MTP		92		/* Multicast Transport Proto */
#define IP_PROTO_AX25		93		/* AX.25 Frames */
#define IP_PROTO_IPIPENCAP	94		/* yet-another IP encap */
#define IP_PROTO_MICP		95		/* Mobile Internet Ctrl */
#define IP_PROTO_SCCSP		96		/* Semaphore Comm Sec Proto */
#define IP_PROTO_ETHERIP	97		/* Ethernet in IPv4 */
#define	IP_PROTO_ENCAP		98		/* encapsulation header */
#define IP_PROTO_ANYENC		99		/* private encryption scheme */
#define IP_PROTO_GMTP		100		/* GMTP */
#define IP_PROTO_IFMP		101		/* Ipsilon Flow Mgmt Proto */
#define IP_PROTO_PNNI		102		/* PNNI over IP */
#define IP_PROTO_PIM		103		/* Protocol Indep Multicast */
#define IP_PROTO_ARIS		104		/* ARIS */
#define IP_PROTO_SCPS		105		/* SCPS */
#define IP_PROTO_QNX		106		/* QNX */
#define IP_PROTO_AN		107		/* Active Networks */
#define IP_PROTO_IPCOMP		108		/* IP Payload Compression */
#define IP_PROTO_SNP		109		/* Sitara Networks Protocol */
#define IP_PROTO_COMPAQPEER	110		/* Compaq Peer Protocol */
#define IP_PROTO_IPXIP		111		/* IPX in IP */
#define IP_PROTO_VRRP		112		/* Virtual Router Redundancy */
#define IP_PROTO_PGM		113		/* PGM Reliable Transport */
#define IP_PROTO_ANY0HOP	114		/* 0-hop protocol */
#define IP_PROTO_L2TP		115		/* Layer 2 Tunneling Proto */
#define IP_PROTO_DDX		116		/* D-II Data Exchange (DDX) */
#define IP_PROTO_IATP		117		/* Interactive Agent Xfer */
#define IP_PROTO_STP		118		/* Schedule Transfer Proto */
#define IP_PROTO_SRP		119		/* SpectraLink Radio Proto */
#define IP_PROTO_UTI		120		/* UTI */
#define IP_PROTO_SMP		121		/* Simple Message Protocol */
#define IP_PROTO_SM		122		/* SM */
#define IP_PROTO_PTP		123		/* Performance Transparency */
#define IP_PROTO_ISIS		124		/* ISIS over IPv4 */
#define IP_PROTO_FIRE		125		/* FIRE */
#define IP_PROTO_CRTP		126		/* Combat Radio Transport */
#define IP_PROTO_CRUDP		127		/* Combat Radio UDP */
#define IP_PROTO_SSCOPMCE	128		/* SSCOPMCE */
#define IP_PROTO_IPLT		129		/* IPLT */
#define IP_PROTO_SPS		130		/* Secure Packet Shield */
#define IP_PROTO_PIPE		131		/* Private IP Encap in IP */
#define IP_PROTO_SCTP		132		/* Stream Ctrl Transmission */
#define IP_PROTO_FC		133		/* Fibre Channel */
#define IP_PROTO_RSVPIGN	134		/* RSVP-E2E-IGNORE */
#define	IP_PROTO_RAW		255		/* Raw IP packets */
#define IP_PROTO_RESERVED	IP_PROTO_RAW	/* Reserved */
#define	IP_PROTO_MAX		255

/*
 * Option types (opt_type) - http://www.iana.org/assignments/ip-parameters
 */
#define IP_OPT_CONTROL		0x00		/* control */
#define IP_OPT_DEBMEAS		0x40		/* debugging & measurement */
#define IP_OPT_COPY		0x80		/* copy into all fragments */
#define IP_OPT_RESERVED1	0x20
#define IP_OPT_RESERVED2	0x60

#define IP_OPT_EOL	  0			/* end of option list */
#define IP_OPT_NOP	  1			/* no operation */
#define IP_OPT_SEC	 (2|IP_OPT_COPY)	/* DoD basic security */
#define IP_OPT_LSRR	 (3|IP_OPT_COPY)	/* loose source route */
#define IP_OPT_TS	 (4|IP_OPT_DEBMEAS)	/* timestamp */
#define IP_OPT_ESEC	 (5|IP_OPT_COPY)	/* DoD extended security */
#define IP_OPT_CIPSO	 (6|IP_OPT_COPY)	/* commercial security */
#define IP_OPT_RR	  7			/* record route */
#define IP_OPT_SATID	 (8|IP_OPT_COPY)	/* stream ID (obsolete) */
#define IP_OPT_SSRR	 (9|IP_OPT_COPY)	/* strict source route */
#define IP_OPT_ZSU	 10			/* experimental measurement */
#define IP_OPT_MTUP	 11			/* MTU probe */
#define IP_OPT_MTUR	 12			/* MTU reply */
#define IP_OPT_FINN	(13|IP_OPT_COPY|IP_OPT_DEBMEAS)	/* exp flow control */
#define IP_OPT_VISA	(14|IP_OPT_COPY)	/* exp access control */
#define IP_OPT_ENCODE	 15			/* ??? */
#define IP_OPT_IMITD	(16|IP_OPT_COPY)	/* IMI traffic descriptor */
#define IP_OPT_EIP	(17|IP_OPT_COPY)	/* extended IP, RFC 1385 */
#define IP_OPT_TR	(18|IP_OPT_DEBMEAS)	/* traceroute */
#define IP_OPT_ADDEXT	(19|IP_OPT_COPY)	/* IPv7 ext addr, RFC 1475 */
#define IP_OPT_RTRALT	(20|IP_OPT_COPY)	/* router alert, RFC 2113 */
#define IP_OPT_SDB	(21|IP_OPT_COPY)	/* directed bcast, RFC 1770 */
#define IP_OPT_NSAPA	(22|IP_OPT_COPY)	/* NSAP addresses */
#define IP_OPT_DPS	(23|IP_OPT_COPY)	/* dynamic packet state */
#define IP_OPT_UMP	(24|IP_OPT_COPY)	/* upstream multicast */
#define IP_OPT_MAX	 25

#define DNET_LIL_ENDIAN		1234
#define DNET_BIG_ENDIAN		4321

// Win32 specific :
# define DNET_BYTESEX		DNET_LIL_ENDIAN
typedef unsigned char u_int8_t;
typedef signed char int8_t;

#ifndef DIMES_WINDOWS
typedef char __int8;
typedef int __int16;
#endif

typedef u_char	uint8_t;
  typedef u_short	uint16_t;
  typedef u_int		uint32_t;
typedef uint32_t	ip_addr_t;

struct ICMPHeader {
  __int8 type;
  __int8 code;
  __int16 checksum;
  unsigned short id;
  short sequence;
};


struct ip_hdr {
#if DNET_BYTESEX == DNET_BIG_ENDIAN
	uint8_t		ip_v:4,		/* version */
			ip_hl:4;	/* header length (incl any options) */
#elif DNET_BYTESEX == DNET_LIL_ENDIAN
	uint8_t		ip_hl:4,
			ip_v:4;
#else
# error "need to include <dnet.h>"	
#endif
	uint8_t		ip_tos;		/* type of service */
	uint16_t	ip_len;		/* total length (incl header) */
	uint16_t	ip_id;		/* identification */
	uint16_t	ip_off;		/* fragment offset and flags */
	uint8_t		ip_ttl;		/* time to live */
	uint8_t		ip_p;		/* protocol */
	uint16_t	ip_sum;		/* checksum */
	ip_addr_t	ip_src;		/* source address */
	ip_addr_t	ip_dst;		/* destination address */
};


typedef u_char	uint8_t;
  typedef u_short	uint16_t;
  typedef u_int		uint32_t;

typedef unsigned char u8;
typedef unsigned short u16;

typedef struct eth_addr {
	uint8_t		data[ETH_ADDR_LEN];
} eth_addr_t;

struct eth_hdr {
	eth_addr_t	eth_dst;	/* destination address */
	eth_addr_t	eth_src;	/* source address */
	uint16_t	eth_type;	/* payload type */
};

#define eth_pack_hdr(h, dst, src, type) do {			\
	struct eth_hdr *eth_pack_p = (struct eth_hdr *)(h);	\
	memmove(&eth_pack_p->eth_dst, &(dst), ETH_ADDR_LEN);	\
	memmove(&eth_pack_p->eth_src, &(src), ETH_ADDR_LEN);	\
	eth_pack_p->eth_type = htons(type);			\
} while (0)



#define	 ip_cksum_carry(x) \
	    (x = (x >> 16) + (x & 0xffff), (~(x + (x >> 16)) & 0xffff))
#define IP_RF		0x8000		/* reserved */
#define IP_DF		0x4000		/* don't fragment */
#define IP_MF		0x2000		/* more fragments (not last frag) */
#define IP_OFFMASK	0x1fff		/* mask for fragment offset */

#define TCP_HDR_LEN	20		/* base TCP header length */
#define TCP_OPT_LEN	2		/* base TCP option length */
#define TCP_OPT_LEN_MAX	40
#define TCP_HDR_LEN_MAX	(TCP_HDR_LEN + TCP_OPT_LEN_MAX)

struct tcp_hdr {
	uint16_t	th_sport;	/* source port */
	uint16_t	th_dport;	/* destination port */
	uint32_t	th_seq;		/* sequence number */
	uint32_t	th_ack;		/* acknowledgment number */
#if DNET_BYTESEX == DNET_BIG_ENDIAN
	uint8_t		th_off:4,	/* data offset */
			th_x2:4;	/* (unused) */
#elif DNET_BYTESEX == DNET_LIL_ENDIAN
	uint8_t		th_x2:4,
			th_off:4;
#else
# error "need to include <dnet.h>"
#endif
	uint8_t		th_flags;	/* control flags */
	uint16_t	th_win;		/* window */
	uint16_t	th_sum;		/* checksum */
	uint16_t	th_urp;		/* urgent pointer */
};


#define UDP_HDR_LEN	8

struct udp_hdr {
	uint16_t	uh_sport;	/* source port */
	uint16_t	uh_dport;	/* destination port */
	uint16_t	uh_ulen;	/* udp length (including header) */
	uint16_t	uh_sum;		/* udp checksum */
};

#define UDP_PORT_MAX	65535


#define ICMP_HDR_LEN	4	/* base ICMP header length */
#define ICMP_LEN_MIN	8	/* minimum ICMP message size, with header */

/*
 * ICMP header
 */
struct icmp_hdr {
	uint8_t		icmp_type;	/* type of message, see below */
	uint8_t		icmp_code;	/* type sub code */
	uint16_t	icmp_cksum;	/* ones complement cksum of struct */
};

/*
 * Types (icmp_type) and codes (icmp_code) -
 * http://www.iana.org/assignments/icmp-parameters
 */
#define		ICMP_CODE_NONE		0	/* for types without codes */
#define	ICMP_ECHOREPLY		0		/* echo reply */
#define	ICMP_UNREACH		3		/* dest unreachable, codes: */
#define		ICMP_UNREACH_NET		0	/* bad net */
#define		ICMP_UNREACH_HOST		1	/* bad host */
#define		ICMP_UNREACH_PROTO		2	/* bad protocol */
#define		ICMP_UNREACH_PORT		3	/* bad port */
#define		ICMP_UNREACH_NEEDFRAG		4	/* IP_DF caused drop */
#define		ICMP_UNREACH_SRCFAIL		5	/* src route failed */
#define		ICMP_UNREACH_NET_UNKNOWN	6	/* unknown net */
#define		ICMP_UNREACH_HOST_UNKNOWN	7	/* unknown host */
#define		ICMP_UNREACH_ISOLATED		8	/* src host isolated */
#define		ICMP_UNREACH_NET_PROHIB		9	/* for crypto devs */
#define		ICMP_UNREACH_HOST_PROHIB	10	/* ditto */
#define		ICMP_UNREACH_TOSNET		11	/* bad tos for net */
#define		ICMP_UNREACH_TOSHOST		12	/* bad tos for host */
#define		ICMP_UNREACH_FILTER_PROHIB	13	/* prohibited access */
#define		ICMP_UNREACH_HOST_PRECEDENCE	14	/* precedence error */
#define		ICMP_UNREACH_PRECEDENCE_CUTOFF	15	/* precedence cutoff */
#define	ICMP_SRCQUENCH		4		/* packet lost, slow down */
#define	ICMP_REDIRECT		5		/* shorter route, codes: */
#define		ICMP_REDIRECT_NET		0	/* for network */
#define		ICMP_REDIRECT_HOST		1	/* for host */
#define		ICMP_REDIRECT_TOSNET		2	/* for tos and net */
#define		ICMP_REDIRECT_TOSHOST		3	/* for tos and host */
#define	ICMP_ALTHOSTADDR	6		/* alternate host address */
#define	ICMP_ECHO		8		/* echo service */
#define	ICMP_RTRADVERT		9		/* router advertise, codes: */
#define		ICMP_RTRADVERT_NORMAL		0	/* normal */
#define		ICMP_RTRADVERT_NOROUTE_COMMON 16	/* selective routing */
#define	ICMP_RTRSOLICIT		10		/* router solicitation */
#define	ICMP_TIMEXCEED		11		/* time exceeded, code: */
#define		ICMP_TIMEXCEED_INTRANS		0	/* ttl==0 in transit */
#define		ICMP_TIMEXCEED_REASS		1	/* ttl==0 in reass */
#define	ICMP_PARAMPROB		12		/* ip header bad */
#define		ICMP_PARAMPROB_ERRATPTR		0	/* req. opt. absent */
#define		ICMP_PARAMPROB_OPTABSENT	1	/* req. opt. absent */
#define		ICMP_PARAMPROB_LENGTH		2	/* bad length */
#define	ICMP_TSTAMP		13		/* timestamp request */
#define	ICMP_TSTAMPREPLY	14		/* timestamp reply */
#define	ICMP_INFO		15		/* information request */
#define	ICMP_INFOREPLY		16		/* information reply */
#define	ICMP_MASK		17		/* address mask request */
#define	ICMP_MASKREPLY		18		/* address mask reply */
#define ICMP_TRACEROUTE		30		/* traceroute */
#define ICMP_DATACONVERR	31		/* data conversion error */
#define ICMP_MOBILE_REDIRECT	32		/* mobile host redirect */
#define ICMP_IPV6_WHEREAREYOU	33		/* IPv6 where-are-you */
#define ICMP_IPV6_IAMHERE	34		/* IPv6 i-am-here */
#define ICMP_MOBILE_REG		35		/* mobile registration req */
#define ICMP_MOBILE_REGREPLY	36		/* mobile registration reply */
#define ICMP_DNS		37		/* domain name request */
#define ICMP_DNSREPLY		38		/* domain name reply */
#define ICMP_SKIP		39		/* SKIP */
#define ICMP_PHOTURIS		40		/* Photuris */
#define		ICMP_PHOTURIS_UNKNOWN_INDEX	0	/* unknown sec index */
#define		ICMP_PHOTURIS_AUTH_FAILED	1	/* auth failed */
#define		ICMP_PHOTURIS_DECOMPRESS_FAILED	2	/* decompress failed */
#define		ICMP_PHOTURIS_DECRYPT_FAILED	3	/* decrypt failed */
#define		ICMP_PHOTURIS_NEED_AUTHN	4	/* no authentication */
#define		ICMP_PHOTURIS_NEED_AUTHZ	5	/* no authorization */
#define	ICMP_TYPE_MAX		40

int ip_cksum_add(const void *buf, size_t len, int cksum);
void ip_checksum(void *buf, size_t len);

#endif
