#ifndef _JNI_CALLBACK_H_
#define _JNI_CALLBACK_H_
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

#include <jni.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <memory.h>

//  Log levels :
	const int LEVEL_FINE=1;
	const int LEVEL_FINER=2;
	const int LEVEL_FINEST=3;
	const int LEVEL_INFO=4;
	const int LEVEL_WARNING=5;
	const int LEVEL_SEVERE=6;

/***********************
 *
 * A Class for making logging calls from native code
 * Please see dimes.measurements.nio.NativeLogger for further instructions.
 *
 */
class JavaLog
{
	// Enviroment variables used for logging :
private:
	static JNIEnv *env;
	static jobject obj;
	static jmethodID mid;

public:
	static void setJavalogEnviroment(JNIEnv *myEnv,jobject callbackObj);	
	// use for general logging :
	static void javalog(int logLevel , char* logMessage);
	// Use as printf :
	static void javalogf(int logLevel , char* logFormat , ...);
};



	// Helper class for passing arguments into javalogf :
	template<unsigned char count>
	struct SVaPassNext{
		SVaPassNext<count-1> big;
		unsigned long dw;
	};
	template<> struct SVaPassNext<0>{};
	//SVaPassNext - is generator of structure of any size at compile time.

	class CVaPassNext{
	public:
		SVaPassNext<15> svapassnext;
		CVaPassNext(va_list & args){
			try{//to avoid access violation
				memcpy(&svapassnext, args, sizeof(svapassnext));
			} catch (...) {}
		}
	};
#define va_pass(valist) CVaPassNext(valist).svapassnext

#endif
