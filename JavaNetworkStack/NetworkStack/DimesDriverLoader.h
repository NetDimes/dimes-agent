#ifndef _DIMES_DRIVER_LOADER_H
#define _DIMES_DRIVER_LOADER_H
#ifdef DIMES_WINDOWS
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

#include "JNIOutputCallback.h"
#include "dimesMTRdefs.h"

/**********************
 *
 * A static class that loads the dimes packet driver ( and installs it when necessary )
 * PLEASE NOTE THAT : once the dimes driver is installed , it must not be deleted
 * but have to be removed in other means that are not supplyed here.
 * TODO : add a code that uninstalls the service on demand.
 *
 */
class DimesDriverLoader
{
public:
	DimesDriverLoader(void);
	~DimesDriverLoader(void);

	static void startDimesService();
	static BOOLEAN stopDimesService();
	static BOOLEAN installDimesService();

};
#endif
#endif