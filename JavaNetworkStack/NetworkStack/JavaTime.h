#ifndef __JAVA_TIME_H_
#define __JAVA_TIME_H_
#include <jni.h>
#ifdef DIMES_WINDOWS
#include <WinSock2.h>
#else
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/time.h>
#define __int64 jlong
#endif


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


/***********************
 *
 * A Class for receiving accurate time in java terms ( jlong == 64 bit time )
 * Most of the code here was taken from the JVM source code.
 *
 */
class JavaTime
{
public:
	JavaTime(void);
	~JavaTime(void);
	
	#ifdef DIMES_WINDOWS
	const static __int64 _offset   = 116444736000000000;
	#endif

	static int	   getMicroSecs(__int64 *time , __int64 freqency);
	static int     getMicroSecDiff(__int64 start, __int64 end, timeval *tv);
	static __int64 getJavaTime();
	static __int64 javaTimeNanos();
};

#endif


