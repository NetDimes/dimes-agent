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
#include "JavaTime.h"

JavaTime::JavaTime(void)
{
}

JavaTime::~JavaTime(void)
{
}
#ifdef DIMES_WINDOWS

// Auxilery JVM functions :

typedef unsigned __int32 juint;
typedef unsigned __int64 julong;
typedef long jint;

static void set_low(__int64* value, jint low) {
    *value &= (__int64)0xffffffff << 32;
    *value |= (__int64)(julong)(juint)low;
}

static void set_high(__int64* value, jint high) { 
    *value &= (__int64)(julong)(juint)0xffffffff;
    *value |= (__int64)high       << 32;
} 

static __int64 jlong_from(jint h, jint l) {
  __int64 result = 0; // initialization to avoid warning
  set_high(&result, h);
  set_low(&result,  l);
  return result;
}

// Return a __int64 time the same as the JVM does :
__int64 JavaTime::getJavaTime(){
	FILETIME wt;
    GetSystemTimeAsFileTime(&wt);
	//printf("time is : %I64d\n ",wt);
	//fflush(stdout);
	__int64 a = jlong_from(wt.dwHighDateTime, wt.dwLowDateTime);
	return (a - _offset) / 10000;
}

// Get the microseconds using nanoseconds performence query :
int JavaTime::getMicroSecs(__int64 *time , __int64 freqency)
{
	__int64 perfVal;
   QueryPerformanceCounter((LARGE_INTEGER*)&perfVal);
   double time_frac = ((double)perfVal / freqency);
   *time = (__int64)(time_frac*1000000);
   return 0;
}

static bool freqSet = false;
static __int64 performance_frequency;

//  Return the time in nano seconds ( same as JVM 1.5 source code )
__int64 JavaTime::javaTimeNanos()
{
if (!freqSet)
	{
		printf("JavaTimeNanos : adjusting frequeincies...\n");
		 QueryPerformanceFrequency((LARGE_INTEGER*)&performance_frequency);
		 freqSet = true;
	}
	__int64 nanoTime;
	getMicroSecs(&nanoTime , performance_frequency);
	return nanoTime;
}

#else
__int64 JavaTime::javaTimeNanos()
{
	timeval time;
	gettimeofday(&time,NULL);
	jlong usecs = (jlong)time.tv_sec * ( 1000 * 1000) + (jlong)time.tv_usec;
	return usecs;
}

int JavaTime::getMicroSecs(__int64 *time , __int64 freqency)
{
	timeval time2;
	gettimeofday(&time2,NULL);
	*time =  (jlong)time2.tv_sec * 1000 + (jlong)time2.tv_usec / 1000;
	return 0;
}

__int64 JavaTime::getJavaTime(){
	timeval time;
	gettimeofday(&time,NULL);
	return (jlong)time.tv_sec * 1000 + (jlong)time.tv_usec / 1000;
}


#endif



// Get the difference in timeval beteen two time values.
int JavaTime::getMicroSecDiff(__int64 start, __int64 end, timeval *tv)
{
	__int64 diff = end - start;
	if (diff < 0)
	{
	   tv->tv_sec = -1;//make sure we get some epsilon value in listenAndProcess
	   tv->tv_usec = 0;
	}
	else
	{
	   tv->tv_sec = (long)(diff/ 1000000);
	   tv->tv_usec = (long)(diff % 1000000);
	}

//	if (tv->tv_sec > 30)
		//printf("got large selecttime: %d secs.\n", tv->tv_sec);

   return 0;
}
