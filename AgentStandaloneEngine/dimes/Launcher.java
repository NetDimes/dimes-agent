package dimes;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Vector;

import dimes.platform.PlatformDependencies;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;

public class Launcher {

	/**
	 * @param args
	 */
	private static String[] entryPointArgs = null;
    private static final Class[] parameters = new Class[] {URL.class};

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//TODO:Debug
		//--------------
		int argsNum = args.length;
	    if (argsNum < 1){
	        System.out.println("Usage: java AgentLauncher <jarBase>");
	        System.exit(0);
	        }
	    String aJarBase = args[0];
	    
	    if (argsNum > 1)
	    {
	        entryPointArgs = new String[argsNum-1];
	        for (int i=0; i<argsNum-1; ++i)
	            entryPointArgs[i] = args[i+1];
	    }
	    else
	        entryPointArgs = new String[0];

		
		try {
			Vector<File> resources = getLatestResources(args[0]);
			Vector<File> jarResources = new Vector<File>();
			for (int i=0; i<resources.size(); ++i)
			{
			    File aResource = (File)resources.get(i);
			    if (aResource.getName().endsWith(".jar"))
			        jarResources.add(aResource);
			}
			jarResources.trimToSize();
			
			URL[] urls = new URL[jarResources.size()];//classJar + resources
//			urls[0] = jarURL;
			for (int i=1; i<urls.length; ++i)
			    urls[i] = (((File)jarResources.get(i-1)).toURI()).toURL();//.toURL();
			
	        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
	        Class sysclass = URLClassLoader.class;
	        Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
	      
	        	for(URL u :urls){		            
		            method.invoke(sysloader, new Object[] {u});
	        	}
	        

			
//			classLoader = new URLClassLoader(urls);
			Class app =  Launcher.class.getClassLoader().loadClass("dimes.AppSplash");
			System.out.println("Test load Done \n");
			app.getMethod("main", new Class[] {String[].class})
			.invoke(null, new Object[]{entryPointArgs});
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		//--------------

	}

	/**
	 * resourcesFromStartup - if true, means that resource list is taken from Startup class.
	 * Should be used when new resources were extracted by Startup. Note that only those
	 * resources will be used, if true.
	 * If false, resources will be found in this.agentResourcePath.
	 * nextVer - maybe try combined approach - some resources can change, while others don't, so
	 * resources should be taken both from Startup and from agentResourcePath. 
	 */
//	tomove to Startup, call using reflection
	public static Vector<File> getLatestResources(String agentResourcesPath)
	{
		final String libraryExtensions = PlatformDependencies.currentLibraryExtension;

	    Vector<File> resources = new Vector<File>();
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
}
