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

JNIEnv* JavaLog::env;
jobject JavaLog::obj;
jmethodID JavaLog::mid;

void JavaLog::setJavalogEnviroment(JNIEnv *myEnv,jobject callbackObj)
	{
		env = myEnv;
		obj = callbackObj;
		jclass cls = env->GetObjectClass(obj);
		mid = env->GetMethodID(cls, "log", "(Ljava/lang/String;Ljava/lang/String;)V");
	}

	void JavaLog::javalogf(int logLevel , char* logFormat , ...){
		char message[1024];
		va_list args;
		va_start(args, logFormat);		
		sprintf(message , logFormat, va_pass(args));
		javalog(logLevel , message);
		va_end(args);
	}

	void JavaLog::javalog(int logLevel , char* logMessage)
	{		
		if (mid != 0) 
		{
			char* logLevelString;
			switch(logLevel){
				case LEVEL_FINEST: logLevelString = "FINEST"; break;
				case LEVEL_FINER: logLevelString = "FINER"; break;
				case LEVEL_FINE: logLevelString = "FINE"; break;
				case LEVEL_INFO: logLevelString = "INFO"; break;
				case LEVEL_WARNING: logLevelString = "WARNING"; break;
				case LEVEL_SEVERE: logLevelString = "SEVERE"; break;
				default: return;
			}

			jstring logLevelStr = env->NewStringUTF(logLevelString); 
			jstring msg = env->NewStringUTF(logMessage); 
			env->CallVoidMethod( obj, mid, logLevelStr,msg);
		}
		else
			printf("%s\n",logMessage);
	}
