/*
 * Created on 05/05/2005
 *
 */
package dimes.util;

import java.util.HashMap;

/**
 * @author Ohad 
 *
 */
public class ResourceManager
{
	public static final String[] resourcesList = {"resources/toxicthemepack.zip", //0, default
			"resources/dimesTransparent2.gif", "resources/client.keystore", "resources/server.keystore", "resources/network.gif",
			"resources/folder_documents.gif", "resources/fav.gif", //6
			"resources/exec.gif", "resources/hand.gif", "resources/pacman.gif", "resources/helpicon.gif", "resources/about.txt", //11
			"resources/dimesIcon.jpg", "resources/DIMESIcon.ico", "resources/Properties16.gif", "resources/Pause16.gif", "resources/Play16.gif", //16
			"resources/Delete16.gif", "resources/StopRed16.gif", "resources/Help16.gif", "resources/About16.gif", "resources/Save16.gif", //21
			"resources/layouts.meta", "resources/layouts.bin", "resources/redWebComponent.gif", "resources/WebComponent24.gif", "resources/restart2.gif",//26
			"resources/changeLog.txt"

	};
	public static final String TOXIC_THEME_PACK_RSRC = resourcesList[0];

	private static HashMap resourcesMap = new HashMap();
	private static String resourcesDirectory;
	public static final String dimesTransparent2_RSRC = resourcesList[1];
	public static final String CLIENT_KEYSTORE_RSRC = resourcesList[2];
	public static final String SERVER_KEYSTORE_RSRC = resourcesList[3];
	public static final String NETWORK_GIF_RSRC = resourcesList[4];
	public static final String FOLDER_DOCUMENTS_GIF_RSRC = resourcesList[5];
	public static final String FAV_GIF_RSRC = resourcesList[6];
	public static final String EXEC_GIF_RSRC = resourcesList[7];
	public static final String HAND_GIF_RSRC = resourcesList[8];
	public static final String PACMAN_GIF_RSRC = resourcesList[9];
	public static final String HELPICON_GIF_RSRC = resourcesList[10];
	public static final String ABOUT_RSRC = resourcesList[11];
	public static final String DIMESICON_JPG_RSRC = resourcesList[12];
	public static final String DIMESICON_ICO_RSRC = resourcesList[13];
	public static final String propertiesIconName_RSRC = resourcesList[14];
	public static final String pauseIconName_RSRC = resourcesList[15];
	public static final String resumeIconName_RSRC = resourcesList[16];
	public static final String clearAllIconName_RSRC = resourcesList[17];
	public static final String stopIconName_RSRC = resourcesList[18];
	public static final String helpIconName_RSRC = resourcesList[19];
	public static final String aboutIconName_RSRC = resourcesList[20];
	public static final String saveIconName_RSRC = resourcesList[21];
	public static final String layoutsMetaDataFile = resourcesList[22];
	public static final String layoutsBinDataFile = resourcesList[23];
	public static final String BLOCKED_ICON_RSRC = resourcesList[24];
	public static final String NON_BLOCKED_ICON_RSRC = resourcesList[25];
	public static String restartIconName_RSRC = resourcesList[26];
	public static final String CHANGELOG_RSRC = resourcesList[27];

	/*********************************
	 * AS of this version , all resources must be loaded from the jar file like this:
	 * ResourceManager.getClass().getClassLoader().getResource("...");
	 * 
	 * This clas's methods are kept here commented for time when we want to manage 
	 * other resources from outside the JAR
	 *
	 */
	//    /**
	//     * 
	//     * 
	//     * @param dir
	//     */
	//    public static void addResources(String dir) {
	//        resourcesDirectory = dir;
	//        for (int i=0; i<resourcesList.length; i++)
	//        {            
	//            String resourceFileName = resourcesList[i];
	//            if (resourceFileName.endsWith(".dll") || resourceFileName.endsWith(".jar"))
	//                addResource(dir,resourceFileName);           
	//        }        
	//    }
	//    
	//    
	//    public static void addResource(String dir , String baseName){
	//        String containingDir = getContainingDir(baseName);
	//        String fileExtension = getExtension(baseName);
	//        String filePrefix = getFilePrefix(baseName);
	//        if (fileExtension == null || fileExtension=="")
	//        {
	////            System.err.println("File "+baseName+" arrived with no extension.");
	//            return;
	//        }	
	////        System.out.println("dir="+dir + " basename=" + baseName + " fileExtension="+fileExtension);
	//        File correspondingResourceFile = LibraryLoader.getLatestFile(dir,filePrefix , fileExtension);
	//        if (correspondingResourceFile == null)
	//        {
	////            System.err.println("Couldnt find resource corresponding with :" + baseName);
	//            return;
	//        }
	////        System.out.println("Adding to map :" +baseName +"->"+ containingDir+ '/' + correspondingResourceFile.getName());
	//        resourcesMap.put(baseName ,containingDir+ '/' + correspondingResourceFile.getName());
	//    }
	//    
	//	/**
	//     * @param baseName
	//     * @return
	//     */
	//    private static String getContainingDir(String name) {
	//        int index = name.lastIndexOf('/');
	//		String dire;
	//		if (index < 0)
	//		    dire = "";
	//		else
	//		    dire = name.substring(0,index);
	//		return dire;
	//    }
	//
	//
	//    /**
	//     * @param baseName
	//     * @return
	//     */
	//    private static String getFilePrefix(String name) {
	//        int index = name.lastIndexOf('.');
	//		int index0 = name.lastIndexOf('/');
	//        String prefix;		
	//		if (index0 < 0 )
	//			prefix = name;
	//		else
	//		    if (index <0)
	//		        prefix = "";
	//		    else
	//		        prefix = name.substring(index0+1,index);
	//		return prefix;
	//    }
	//
	//
	//    private static String getExtension(String name)
	//	{
	//		int index = name.lastIndexOf('.');
	//		String extension;
	//		if (index < 0)
	//			extension = "";
	//		else
	//			extension = name.substring(index);
	//		return extension;
	//	}
	//    public static String getResourceName(String baseName){
	//        String fileName =  (String) resourcesMap.get(baseName);
	////        System.out.println("Returning resource:"+baseName+"->"+fileName);
	//        return fileName;
	//    }
	//    public static URL getResource(String resourceBaseName , String baseDir){        
	//        URL resourceFromJAR = ResourceManager.class.getClassLoader().getResource(resourceBaseName);
	//        if (resourceFromJAR == null){
	//            URL resourceFromFolder;
	//            try 
	//            {
	//                String resourceName = getResourceName(resourceBaseName);
	//                resourceFromFolder = new URL(baseDir+File.separator+resourceName);
	////                System.out.println(" ----->  returning from disk :" + baseDir+File.separator+resourceName);
	//            }             
	//            catch (MalformedURLException e) 
	//            {
	//                resourceFromFolder = null;
	//                // TODO Auto-generated catch block
	//                e.printStackTrace();
	//            }
	//            return resourceFromFolder;
	//        }
	//        return resourceFromJAR;       
	//    }
	//    
	//    
	//    public static File getResourceFile(String baseName){
	//        return  new File( (String)resourcesMap.get(baseName));  
	//    }
}