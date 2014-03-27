/*
 * Created 12/08/08
 */

package dimes.util;
import java.io.File;


/*
 *<p>
 * directorySearch  recursivly searches through a list of files and directories until it finds the first
 * instance of a file name, returns a string representing the absolut path to the file or null if not found
  * </P> 
 * 
 * @throws:NullPointerException
 *  @author:BoazH
  */
public class DirectorySearch {

    private String location=null;
    private File directoryName=null;
    private File[] fileArray=null;;
    
    public DirectorySearch() throws NullPointerException{
        directoryName = new File(".");
    }
    
    public DirectorySearch(String dirIn) throws NullPointerException{
        directoryName = new File(dirIn);
    }
    
    public DirectorySearch(File dirIn)throws NullPointerException{
        directoryName =dirIn;
    }
    
    public String find(String nameIn){
          fileArray =  directoryName.listFiles();
         return find(nameIn,fileArray);
    }
    
    //private overloaded find function for the recursive search
    private String find(String nameIn,File[] fileArray){
                    
            for (int i=0;i<fileArray.length;i++){
             
                    if(fileArray[i].getName().equalsIgnoreCase(nameIn)){
                        location=fileArray[i].getAbsolutePath();
                        return location;
                     }
                    else{ 
                        
                         if(fileArray[i].isDirectory()) location=find(nameIn, fileArray[i].listFiles());
                        if(location!=null) return location;
                    }
               
            }
             
            return location;
    }
    

}
