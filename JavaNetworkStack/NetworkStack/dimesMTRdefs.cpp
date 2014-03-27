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
#include "dimesMTRdefs.h"
#ifdef DIMES_WINDOWS
// resourceExpression : Usually like :".\\resources\\shared*.dll"
bool findLatestResource(char* baseDir , char* baseName , char* extension , char* lastResourceBuffer){
	char resourceExpression[1024];
	sprintf(resourceExpression,"%s\\%s*.%s",baseDir,baseName,extension);
	sprintf(lastResourceBuffer,"");
	WIN32_FIND_DATA findData;


	HANDLE hFind=FindFirstFile((resourceExpression),&findData);

	if (hFind == INVALID_HANDLE_VALUE) {
		printf("Could not find files (%s.%s) in directory (%s).\n",baseName,extension , baseDir);
		return false;
	}

	int baseLen = strlen(baseName);
	int extensionLen=strlen(extension);

	int lastResourceLength=0;
	do
	{
		
		int fileNameLength = strlen(findData.cFileName);
		
		int comparison = 	strcmp(lastResourceBuffer,findData.cFileName);
		//printf("comp between %s and %s : %d\n",lastResourceBuffer,findData.cFileName,comparison);
		if (lastResourceLength<fileNameLength || (comparison<0 && lastResourceLength==fileNameLength)){
			sprintf(lastResourceBuffer,"%s\\%s",baseDir,findData.cFileName);
			lastResourceLength = fileNameLength;
		}
	}
	while (FindNextFile(hFind, &findData));
	//printf("last resource : %s \n",lastResourceBuffer);
	FindClose(hFind);

	return true;
}
#endif
