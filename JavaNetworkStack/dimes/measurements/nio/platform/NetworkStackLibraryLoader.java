package dimes.measurements.nio.platform;

import java.io.File;

/**
 * @author Ohad Serfaty
 *
 * a static class for loading the NetworkStack library platform-
 * independently.
 * 
 */
public class NetworkStackLibraryLoader {
	
	public static void loadLibrary(String sourceFolderStr){
		int platform = PlatformDetector.detectPlatform();
		File sourceFolder = new File(sourceFolderStr);
		switch (platform)
		{
			case PlatformDetector.LINUX:
				System.load(sourceFolder.getAbsolutePath() + "/NetworkStack.so");
				break;
			case PlatformDetector.MACOSX:
					System.load(sourceFolder.getAbsolutePath() + "/NetworkStack.jnilib");
				break;
			case PlatformDetector.WINDOWS:
				System.load(sourceFolder.getAbsolutePath() + "/NetworkStack.dll");
				break;	
		}
	}
	
	public static void loadLibrary(){
		loadLibrary("./NetworkStack/Debug");
	}

}
