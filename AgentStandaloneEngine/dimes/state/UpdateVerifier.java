package dimes.state;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

import dimes.platform.PlatformDependencies;
import dimes.util.FileHandlerBean;
import dimes.util.StreamGobbler;
import dimes.AgentGuiComm.logging.Loggers;
//import dimes.util.logging.Loggers;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

/*
 * Created on 05/07/2004
 */

/**
 * @author anat
 */

public class UpdateVerifier
{
	//	logging
	private static Logger logger = Loggers.getLogger(UpdateVerifier.class);

	private static String myName = "UpdateVerifier";
	
	private static String[] macOSJarsignerVerbosityArray = {
			"s = signature was verified",
			"m = entry is listed in manifest",
			"k = at least one certificate was found in keystore",
			"i = at least one certificate was found in identity scope",
			"jar verified."};

	public static boolean verify(File jarFile) throws IOException,
	/*InterruptedException, */NoSuchPropertyException
	{
		String jarDirPath = PropertiesBean.getProperty(PropertiesNames.JAR_DIR);
		String keyStore = jarDirPath + "/" + "dimesAgent.keystore";

		String jarSignerCommand = jarDirPath + "/" + PlatformDependencies.jarsignerExecutable;

//		String jarPath = PlatformDependencies.
		
		String[] cmd = {jarSignerCommand, "-verify", "-verbose", "-keystore", keyStore, jarFile.getAbsolutePath()};
		String cmdStr = "";//debug
		for (int i = 0; i < cmd.length; ++i)
			//debug
			cmdStr += cmd[i] + (PlatformDependencies.os == PlatformDependencies.MACOSX?" ":"\n");//debug
		logger.info("verify cmd is: " + cmdStr);//debug
		System.out.println("Executing command :" + cmd);
		Process verifyProc = Runtime.getRuntime().exec(cmd);

		File outFile = new FileHandlerBean().getIncomingFileSlot(new File(jarDirPath));
		System.out.println("Output file :" + outFile);
		FileOutputStream fileOutStream = new FileOutputStream(outFile);
		StreamGobbler outputGobbler = new StreamGobbler(verifyProc.getInputStream(), fileOutStream);

		// kick them off
		outputGobbler.start();

		try
		{
			int exitStatus = verifyProc.waitFor();

			outputGobbler.join();//check that actually stops without calling
			// Thread.stop or assigning shouldStop=true

		}
		catch (InterruptedException e)
		{
			logger.warning(e.toString());//debug
		}

		fileOutStream.close();

		FileReader reader = new FileReader(outFile);
		boolean isVerified = UpdateVerifier.verifyProcOutput(reader);
		reader.close();
//		if (!outFile.delete())
//			logger.finer("couldn't delete " + outFile.getAbsolutePath());//debug

		return isVerified;

	}

	/**
	 * @param theReader
	 * @return @throws
	 *         IOException
	 */
	public static boolean verifyProcOutput(FileReader theReader) throws IOException
	{
		boolean isVerified = true;
		BufferedReader bufferedReader = new BufferedReader(theReader);
		boolean firstLine = true;

		for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine())
		{
			System.out.println("Verifying line:"+line);
			line = line.trim();
			if (PlatformDependencies.os == PlatformDependencies.MACOSX)
			{
				 if (isVerboseReader(line) || line.length() == 0)
				continue;
			}
			else
			{
			if (line.length() == 0)
				if (firstLine)
				{//first line can be empty
					firstLine = false;
					continue;
				}
				else
				{//there should not be any empty lines between entries.
					return isVerified;
				}
			}
			String[] parts = line.split("\\s+");//should be \\s
			if (parts.length != 9)//should be 9 parts for a normal signed entry
			{
				String name = parts[parts.length - 1];
				if ((name.endsWith(".MF")))//manifest
					continue;
				if ((name.endsWith(".SF")))//signature file
					continue;
				if ((name.endsWith(".DSA")))//signature block
					continue;
				if ((name.endsWith(".LIST")))//list block
					continue;
				if ((name.endsWith("/")))//dir
					continue;
				//not one of the sig files or dirs, but still less parts than
				// should be
				logger.fine("bad entry:  parts.length = " + parts.length + " --> " + line);//debug
				isVerified = false;
				break;
			}
			String flags = parts[0];
			if (flags.compareTo("smk") != 0)
			{
				logger.severe("couldn't verify " + line);//debug
				isVerified = false; //one unverified entry is enough
				break;
			}
		}

		return isVerified;
	}

	/**
	 * @param line
	 * @return
	 */
	private static boolean isVerboseReader(String line) {
		for (int i=0; i<macOSJarsignerVerbosityArray.length; i++)
		if (line.equals(macOSJarsignerVerbosityArray[i]))
			return true;
		return false;
	}
}