package dimes.measurements.nio.platform;

/**
 * @author Ohad Serfaty
 *
 * a static class for detecting the os platform.
 *
 */
public class PlatformDetector {
	
	public static final int LINUX = 1;
	public static final int WINDOWS = 2;
	public static final int MACOSX = 3;
	
	public static int platform = -1;
	
	public static int detectPlatform()
	{
		if (platform!=-1)
			return platform;
		System.out.print("detecting Operating system...");
		if (System.getProperty("os.name").startsWith("Linux"))
			return setPlatform(LINUX);
		if (System.getProperty("mrj.version") != null)
			return setPlatform(MACOSX);
		return setPlatform(WINDOWS);
	}

	private static int setPlatform(int platform) {
		switch (platform)
		{
			case PlatformDetector.LINUX:
				System.out.println("LINUX");
				break;
			case PlatformDetector.MACOSX:
				System.out.println("MACOSX");
				break;
			case PlatformDetector.WINDOWS:
				System.out.println("WINDOWS");
				break;	
		}
		PlatformDetector.platform = platform;
		return PlatformDetector.platform;
	}

}
