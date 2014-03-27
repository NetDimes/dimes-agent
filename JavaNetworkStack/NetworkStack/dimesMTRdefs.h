#ifndef __DIMES_MTR_DEFS_H_
#define __DIMES_MTR_DEFS_H_
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
#include <WinSock2.h>
#include <WS2tcpip.h>
#include <process.h>
#include <io.h> 
#else
#include <sys/types.h>
#include <sys/socket.h>
#endif 

#include <stdio.h>
#include <time.h>
#include <fcntl.h>
#include <ctype.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>
#include <memory.h>
#include <math.h>

#include <sys/types.h>
#include <sys/timeb.h>
#include <sys/stat.h>

#ifdef DIMES_WINDOWS
bool findLatestResource(char* baseDir , char* baseName , char* extension , char* lastResourceBuffer);
#endif

#endif
