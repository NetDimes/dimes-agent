#ifdef DIMES_WINDOWS
#include "dimesdriverloader.h"
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

LPCTSTR DIMESRegistryLocation = TEXT("SYSTEM\\CurrentControlSet\\Services\\DIMESPACKETVER1");
LPCTSTR DIMESServiceName = TEXT("DIMESPACKETVER1");
LPCTSTR DIMESServiceDesc = TEXT("dimes Packet Filter");
LPCTSTR DIMESDriverPath = TEXT("c:\\DevelopmentTreeroute\\dimespacket.sys");


DimesDriverLoader::DimesDriverLoader(void)
{}

DimesDriverLoader::~DimesDriverLoader(void)
{
}

void DimesDriverLoader::startDimesService(){

    BOOLEAN Result;
	DWORD error;
	SC_HANDLE svcHandle = NULL;
	SC_HANDLE scmHandle = NULL;
	LONG KeyRes;
	HKEY PathKey;
	SERVICE_STATUS SStat;
	BOOLEAN QuerySStat;
	


	JavaLog::javalog(LEVEL_FINEST , "Loading dimes packet driver...");

	scmHandle = OpenSCManager(NULL, NULL, GENERIC_READ);

	if(scmHandle == NULL)
	{
		error = GetLastError();
		JavaLog::javalog(LEVEL_FINEST ,"OpenSCManager failed! ");
		return;
	}
	else
	{
		// check if the NPF registry key is already present
		// this means that the driver is already installed and that we don't need to call PacketInstallDriver
		KeyRes=RegOpenKeyEx(HKEY_LOCAL_MACHINE,
			DIMESRegistryLocation,
			0,
			KEY_READ,
			&PathKey);

		if(KeyRes != ERROR_SUCCESS)
		{
			Result = installDimesService();
		}
		else
		{
			Result = TRUE;
			RegCloseKey(PathKey);
		}

		if (Result) 
		{

			svcHandle = OpenService(scmHandle, DIMESServiceName, SERVICE_START | SERVICE_QUERY_STATUS );
			if (svcHandle != NULL)
			{
				QuerySStat = QueryServiceStatus(svcHandle, &SStat);

#if defined(_DBG) || defined(_DEBUG_TO_FILE)				
				switch (SStat.dwCurrentState)
				{
				case SERVICE_CONTINUE_PENDING:
					printf("The status of the driver is: SERVICE_CONTINUE_PENDING\n");
					break;
				case SERVICE_PAUSE_PENDING:
					printf("The status of the driver is: SERVICE_PAUSE_PENDING\n");
					break;
				case SERVICE_PAUSED:
					printf("The status of the driver is: SERVICE_PAUSED\n");
					break;
				case SERVICE_RUNNING:
					printf("The status of the driver is: SERVICE_RUNNING\n");
					break;
				case SERVICE_START_PENDING:
					printf("The status of the driver is: SERVICE_START_PENDING\n");
					break;
				case SERVICE_STOP_PENDING:
					printf("The status of the driver is: SERVICE_STOP_PENDING\n");
					break;
				case SERVICE_STOPPED:
					printf("The status of the driver is: SERVICE_STOPPED\n");
					break;

				default:
					printf("The status of the driver is: unknown\n");
					break;
				}
#endif

				if(!QuerySStat || SStat.dwCurrentState != SERVICE_RUNNING)
				{
					printf("Calling startservice\n");
					if (StartService(svcHandle, 0, NULL)==0)
					{ 
						error = GetLastError();
						if(error!=ERROR_SERVICE_ALREADY_RUNNING && error!=ERROR_ALREADY_EXISTS)
						{
							SetLastError(error);
							if (scmHandle != NULL) 
								CloseServiceHandle(scmHandle);
							error = GetLastError();
							printf("PacketOpenAdapterNPF: StartService failed, LastError=%d\n",error);
							SetLastError(error);
							return;
						}
					}				
				}

				CloseServiceHandle( svcHandle );
				svcHandle = NULL;

			}
			else
			{
				error = GetLastError();
				printf("OpenService failed! Error=%d", error);
				SetLastError(error);
			}
		}
		else
		{
			if(KeyRes != ERROR_SUCCESS)
				Result = installDimesService();
			else
				Result = TRUE;

			if (Result) {

				svcHandle = OpenService(scmHandle,DIMESServiceName,SERVICE_START);
				if (svcHandle != NULL)
				{

					QuerySStat = QueryServiceStatus(svcHandle, &SStat);

#if defined(_DBG) || defined(_DEBUG_TO_FILE)				
					switch (SStat.dwCurrentState)
					{
					case SERVICE_CONTINUE_PENDING:
						printf("The status of the driver is: SERVICE_CONTINUE_PENDING\n");
						break;
					case SERVICE_PAUSE_PENDING:
						printf("The status of the driver is: SERVICE_PAUSE_PENDING\n");
						break;
					case SERVICE_PAUSED:
						printf("The status of the driver is: SERVICE_PAUSED\n");
						break;
					case SERVICE_RUNNING:
						printf("The status of the driver is: SERVICE_RUNNING\n");
						break;
					case SERVICE_START_PENDING:
						printf("The status of the driver is: SERVICE_START_PENDING\n");
						break;
					case SERVICE_STOP_PENDING:
						printf("The status of the driver is: SERVICE_STOP_PENDING\n");
						break;
					case SERVICE_STOPPED:
						printf("The status of the driver is: SERVICE_STOPPED\n");
						break;

					default:
						printf("The status of the driver is: unknown\n");
						break;
					}
#endif

					if(!QuerySStat || SStat.dwCurrentState != SERVICE_RUNNING){

						printf("Calling startservice\n");

						if (StartService(svcHandle, 0, NULL)==0){ 
							error = GetLastError();
							if(error!=ERROR_SERVICE_ALREADY_RUNNING && error!=ERROR_ALREADY_EXISTS){
								if (scmHandle != NULL) CloseServiceHandle(scmHandle);
								printf("PacketOpenAdapterNPF: StartService failed, LastError=%d\n",error);
								SetLastError(error);
								return;
							}
						}
					}

					CloseServiceHandle( svcHandle );
					svcHandle = NULL;

				}
				else{
					error = GetLastError();
					printf("OpenService failed! LastError=%d", error);
					SetLastError(error);
				}
			}
		}
	}

	if (scmHandle != NULL) CloseServiceHandle(scmHandle);

}

BOOLEAN DimesDriverLoader::stopDimesService(){
SC_HANDLE		scmHandle;
    SC_HANDLE       schService;
    BOOL            ret;
    SERVICE_STATUS  serviceStatus;

	JavaLog::javalog(LEVEL_FINEST , "Stopping DimesPacket service...\n");
	scmHandle = OpenSCManager(NULL, NULL, SC_MANAGER_ALL_ACCESS);
	
	if(scmHandle != NULL){
		
		schService = OpenService (scmHandle,
			DIMESServiceName,
			SERVICE_ALL_ACCESS
			);
		
		if (schService != NULL)
		{
			
			ret = ControlService (schService,
				SERVICE_CONTROL_STOP,
				&serviceStatus
				);
			if (!ret)
			{
			}
			
			CloseServiceHandle (schService);
			
			CloseServiceHandle(scmHandle);
			
			return ret;
		}
	}
	
	return FALSE;
}

BOOLEAN DimesDriverLoader::installDimesService()
{
	BOOL result = FALSE;
	ULONG err = 0;
	SC_HANDLE svcHandle;
	SC_HANDLE scmHandle;
	JavaLog::javalog(LEVEL_INFO , "Installing DimesPacket driver.\n");
	
	char buf[1024];
	bool foundLibrary = findLatestResource(".\\Classes\\Base\\resources","dimespacket","sys",buf);
	if (!foundLibrary)
	{
		// search in the resources directory ( for development only ) :
		foundLibrary = findLatestResource(".\\resources","dimespacket","sys",buf);
		if (!foundLibrary)	
		{
			printf("Could not install dimespacket.sys driver\n");
			return FALSE;
		}
	}
	// get the base directory in order to get the full path name :
	DWORD retval=0;
    BOOL success; 
    char buffer[1024]=""; 
	char pathName[1024] = "";
    char * lpPart[1024]={NULL};
    retval = GetFullPathName(".\\",1024,buffer,lpPart);
	sprintf(pathName,"%s%s" , buffer,buf+2);

	printf("Found dimespacket.sys in %s\n" , pathName);
	fflush(stdout);

	scmHandle = OpenSCManager(NULL, NULL, SC_MANAGER_ALL_ACCESS);
	
	if(scmHandle == NULL)
		return FALSE;

	svcHandle = CreateService(scmHandle, 
		DIMESServiceName,
		DIMESServiceDesc,
		SERVICE_ALL_ACCESS,
		SERVICE_KERNEL_DRIVER,
		SERVICE_DEMAND_START,
		SERVICE_ERROR_NORMAL,
		pathName,
		NULL, NULL, NULL, NULL, NULL);
	if (svcHandle == NULL) 
	{
		err = GetLastError();
		if (err == ERROR_SERVICE_EXISTS) 
		{
			//dimespacket.sys already existed
			err = 0;
			result = TRUE;
		}
	}
	else 
	{
		//Created service for dimespacket.sys
		result = TRUE;
	}

	if (svcHandle != NULL)
		CloseServiceHandle(svcHandle);

	if(result == FALSE)
	{
		JavaLog::javalog(LEVEL_SEVERE , "Could not Install DimesPacket driver.");
	}

	CloseServiceHandle(scmHandle);
	SetLastError(err);
	return result;
	
}
#endif
