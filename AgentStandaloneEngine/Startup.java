import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Vector;

import dimes.platform.PlatformDependencies;
import dimes.util.JarExtractor;

/*
 * Created on 25/07/2004
 */

/**
 * @author anat
 */
public class Startup
{
    public static final String[] JARs = 
    {"resources/dom4j-full.jar", "resources/MySystray4j.jar",
            "resources/skinlf.jar" , "resources/IDW.jar" , 
            "resources/toxicthemepack.zip", "resources/commons-io-1.3.2.jar",
            "resources/BrowserLauncher2-1_3.jar"
              };
    public static Vector extractedFiles = new Vector();//after a run of main(), keeps Files that were extracted
    
    public static void main(String[] args)
    {
        System.err.println("Performing Startup Tasks -- 1");//debug
        
        //do nothing for now
        String dest = args[0];
        String libraryExtensions = PlatformDependencies.currentLibraryExtension;
        System.out.println("Adding : *."+libraryExtensions+" , dom4jfull.jar , MySystray4j.jar , \nIDW.jar , skinlf.jar , \n*."+libraryExtensions+" , *.jpg , *.txt , *.gif");
        JarExtractor jarExtract = new JarExtractor();

        jarExtract.setIncludeFilters(JARs);
        jarExtract.addAllIncludeFilter( new String[]
              { "resources/.*"+libraryExtensions }); 

        String jarFileName = jarExtract.getContainingJarName();

        // extract the resources dir :
        Startup.extractedFiles = jarExtract.extract(jarFileName, dest);
        
        // extract the new properties file :
        String confDirName = dest+File.separator+"conf";
        File oldPropertiesFile = new File(confDirName+File.separator + "properties.xml");
        File renamedToOldPropertiesFile = new File (confDirName+File.separator + "properties.xml.old");
        
        boolean renameSuccess = oldPropertiesFile.renameTo(renamedToOldPropertiesFile);
        if (!renameSuccess)
           System.out.println("Couldn't rename " +oldPropertiesFile.getAbsolutePath()+"" +
           		" into " + renamedToOldPropertiesFile.getAbsolutePath() + ". overwriting it.");
            
        // extract without adding a timestamp :
        jarExtract.setIncludeFilters(new String[] {
                "resources/DimesSplash.jpg",  
                "conf/properties.xml"   , 
                "resources/layouts.meta",
                "resources/layouts.bin" }
        );
        Startup.extractedFiles.addAll(jarExtract.extract(jarFileName, dest,false));
        
    }
    
    
    
//	nextVer - move to versionHistory dir, for rollback option
//	tomove to Startup, call using reflection
	public static void handleOldVersions(Object[] params)
	{
	    Vector oldFiles = (Vector)params[0];
	    for (int i=0; i<oldFiles.size(); ++i)
	    {
	        File aFile = (File)oldFiles.get(i);
            if (aFile.delete() == true)
                System.out.println("Deleted "+aFile.getAbsolutePath());//debug
            else//todo - log
                System.out.println("Couldn't delete "+aFile.getAbsolutePath());//debug
	    }	    
	}

	/*
	 * resourcesFromStartup - if true, means that resource list is taken from Startup class.
	 * Should be used when new resources were extracted by Startup. Note that only those
	 * resources will be used, if true.
	 * If false, resources will be found in this.agentResourcePath.
	 * nextVer - maybe try combined approach - some resources can change, while others don't, so
	 * resources should be taken both from Startup and from agentResourcePath. 
	 */
//	tomove to Startup, call using reflection
	public static Vector getLatestResources(Object[] params)
	{
		final String libraryExtensions = PlatformDependencies.currentLibraryExtension;
	    Boolean resourcesFromStartup = (Boolean)params[0];
	    String agentResourcesPath = (String)params[1];

	    if (resourcesFromStartup.booleanValue() == true)
	    {
	        return Startup.getExtractedResources();
	    }
	    Vector resources = new Vector();
	    File resourcesDir = new File(agentResourcesPath);
	    
	    File[] allResources = resourcesDir.listFiles(
		        new FileFilter()
				{public boolean accept(File pathname) 
				{return pathname.getName().endsWith(".jar") || pathname.getName().endsWith("."+libraryExtensions) || pathname.getName().endsWith(".zip");}
			});
	   
	    Arrays.sort(allResources);
	    String prevName = "";
	    String currName = "";
	    for (int i=allResources.length-1; i>=0 ; --i)
	    {
	        File currFile = allResources[i];
	        String fileName = currFile.getName();
	        int lastIndex = fileName.lastIndexOf("_");
	        if (lastIndex == -1)
	            currName = fileName.substring(0, fileName.lastIndexOf("."));
	        else
	            currName = fileName.substring(0, lastIndex);
//            System.out.println("currName: "+currName+"   prevName: "+prevName);
	        if (prevName.compareTo(currName) != 0)
            {
                resources.add(currFile);
            }
	        prevName = currName;
	    }
	 
	    resources.trimToSize();
	    return resources;
	}

	//todo - should scan dirs: resources, JARs / get dir as param
//	tomove to Startup, call using reflection
	private static Vector getExtractedResources()
	{
	    return Startup.extractedFiles;
	}
	
	
    
    
    
}
