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
#include "EthernetUtilities.h"
#include <stdio.h>

int ip_cksum_add(const void *buf, size_t len, int cksum)
{
	uint16_t *sp = (uint16_t *)buf;
	int n, sn;
	
	sn = (int) len / 2;
	n = (sn + 15) / 16;

	/* XXX - unroll loop using Duff's device. */
	switch (sn % 16) {
	case 0:	do {
		cksum += *sp++;
	case 15:
		cksum += *sp++;
	case 14:
		cksum += *sp++;
	case 13:
		cksum += *sp++;
	case 12:
		cksum += *sp++;
	case 11:
		cksum += *sp++;
	case 10:
		cksum += *sp++;
	case 9:
		cksum += *sp++;
	case 8:
		cksum += *sp++;
	case 7:
		cksum += *sp++;
	case 6:
		cksum += *sp++;
	case 5:
		cksum += *sp++;
	case 4:
		cksum += *sp++;
	case 3:
		cksum += *sp++;
	case 2:
		cksum += *sp++;
	case 1:
		cksum += *sp++;
		} while (--n > 0);
	}
	if (len & 1)
		cksum += htons(*(u_char *)sp << 8);

	return (cksum);
}

// Calculate IP checksum over the buffer :
void ip_checksum(void *buf, size_t len)
{
	struct ip_hdr *ip;
	int hl, off, sum;

	if (len < IP_HDR_LEN)
		return;
	
	ip = (struct ip_hdr *)buf;
	hl = ip->ip_hl << 2;
	ip->ip_sum = 0;
	sum = ip_cksum_add(ip, hl, 0);
	ip->ip_sum = ip_cksum_carry(sum);

	off = htons(ip->ip_off);
	
	if ((off & IP_OFFMASK) != 0 || (off & IP_MF) != 0)
		return;
	
	len -= hl;
	
	if (ip->ip_p == IP_PROTO_TCP) {
		struct tcp_hdr *tcp = (struct tcp_hdr *)((u_char *)ip + hl);
		
		if (len >= TCP_HDR_LEN) {
			tcp->th_sum = 0;
			sum = ip_cksum_add(tcp, len, 0) +
			    htons(ip->ip_p + (unsigned short) len);
			sum = ip_cksum_add(&ip->ip_src, 8, sum);
			tcp->th_sum = ip_cksum_carry(sum);
		}
	} else if (ip->ip_p == IP_PROTO_UDP) {
		struct udp_hdr *udp = (struct udp_hdr *)((u_char *)ip + hl);

		if (len >= UDP_HDR_LEN) {
			udp->uh_sum = 0;
			sum = ip_cksum_add(udp, len, 0) +
			    htons(ip->ip_p + (unsigned short) len);
			sum = ip_cksum_add(&ip->ip_src, 8, sum);
			udp->uh_sum = ip_cksum_carry(sum);
			if (!udp->uh_sum)
				udp->uh_sum = 0xffff;	/* RFC 768 */
		}
	} else if (ip->ip_p == IP_PROTO_ICMP || ip->ip_p == IP_PROTO_IGMP) {
		struct icmp_hdr *icmp = (struct icmp_hdr *)((u_char *)ip + hl);
		
		if (len >= ICMP_HDR_LEN) {
			icmp->icmp_cksum = 0;
			sum = ip_cksum_add(icmp, len, 0);
			icmp->icmp_cksum = ip_cksum_carry(sum);
		}
	}
}
