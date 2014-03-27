package dimes.util.update;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import dimes.Agent;
import dimes.NonGuiAgent;
import dimes.comm2server.ConnectionHandlerFactory;
import dimes.comm2server.StandardCommunicator;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;
import dimes.util.XMLUtil;

public class UpdateFilesGetter extends Thread {

	private LinkedList<UpdateOpParams> upParams = new LinkedList<UpdateOpParams>();
	private Agent agent;
	private String agentHeader;
	private String agentTrailer;
	private String agentDir; //As in C:\program files\DIMES\Agent
	private String updateServerRequest;
	private String updateServerURL;
	private String updateID;
	private String updateDir;
	private String incomingZipName;
	private String incomingZipMd5Hash = null;
	private File incomingZipFile = null;
	private File rollbackScriptFile = null;
	private int incomingZipSize;
	private StandardCommunicator updateComm = null;
	
	enum failReason{ FILECORRUPT, XMLPARSEFAIL, UPDATESCRIPTFAIL, ROLLBACKFAIL};
	
	/**Gets the list of update tasks and the update ID and generates the header
	 * and footers for the update requests.
	 * 
	 * A typical update request would look like this:
	 * <agent agentName="agentName">
	 * <header><IP>IP</IP></header>
	 * <UPDATE ID="ID" />	 
	 * </agent> 
	 * 
	 * Agent should respond with a zip file containing the files of the update
	 * 
	 * @param list
	 * @param ID
	 */
	public UpdateFilesGetter(String ID, String filename,String agentDir, int size){

		this.updateID=ID;
		this.incomingZipSize=size;
		this.agentDir=checkDir(agentDir);
		this.incomingZipName = filename;
		this.updateDir = this.agentDir+"update";
		updateServerRequest="<UPDATE ID=\""+ID+"\"/>";
		try {
			agent = NonGuiAgent.getInstance();  //TODO:Add a if statement here to select which Agent type?
			agentHeader = agent.getAgentHeader();
			agentTrailer = agent.getAgentTrailer();
			incomingZipFile = new File(updateDir+File.separator+filename);
//			incomingZipFile = getDefaultIncomingSlot(new File(updateDir));
//			updateServerURL=PropertiesBean.getProperty(PropertiesNames.SECURE_SCRIPT_UPDATE_URL);
//			updateComm = new StandardCommunicator(updateServerURL,ConnectionHandlerFactory.SECURE_CONNECTION);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (NoSuchPropertyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**Optional constructor that allows for md5 hash checking to make
	 * sure the file isn't corrupt. Use this when the update xml has a
	 * md5 hash included. 
	 * 
	 * @param list
	 * @param ID
	 * @param updateDir
	 * @param md5
	 */
	public UpdateFilesGetter( String ID, String filename, String agentDir,int size, String md5){
		this(ID,filename,agentDir,size);
		this.incomingZipMd5Hash=md5;
		this.incomingZipSize=size;
	}
	
	public void run(){
		if (!downloadFile())
			filesReady(false);
		else if(!verifyFile(incomingZipFile, incomingZipSize, incomingZipMd5Hash))
			filesReady(false);
		else if(!unzipFile(incomingZipFile))
			filesReady(false);
		else if (filesReady(true))
			finalizeUpdate();
	}
	
	
	private void finalizeUpdate() {
		try {
			PropertiesBean.setProperty(PropertiesNames.LAST_UPDATE_ID, updateID);
			doDelete(updateDir);
			doDelete(checkDir(agentDir)+"rollback");			
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Update complete");
		
	}

	/** Takes an unzipped update, parses it, and runs it. 
	 * 
	 * @param success is update run successfully. 
	 * @return
	 */
	private boolean filesReady(boolean success){
		if(!success){
			handleUpdateFailure(failReason.FILECORRUPT);
			return false;
		}
		
		if(!parseUpdateXML()) {
			handleUpdateFailure(failReason.XMLPARSEFAIL);
			return false;
		}
		
		if(!preperRollBack()){
			handleUpdateFailure(failReason.ROLLBACKFAIL);
			return false;			
		}
		
		if(!runUpdateScript()){
			handleUpdateFailure(failReason.UPDATESCRIPTFAIL);
			return false;
		}
		return true;
	}
	
	
	private boolean preperRollBack() {
		File rollbackDir = new File (checkDir(agentDir)+"rollback");
		if(rollbackDir.exists()) doDelete(rollbackDir.getPath());
		rollbackDir.mkdir();
		rollbackScriptFile = new File(checkDir(rollbackDir.getAbsolutePath())+"rollback.xml");
		String header = "<ROLLBACK ID=\""+updateID+"\">";
		try {
				rollbackScriptFile.createNewFile();
				if(!rollbackScriptFile.canWrite()) rollbackScriptFile.setWritable(true); //maybe required for some systems			
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		appendRollbackEntry(header);
		return true;
	}

	private boolean runUpdateScript() {
		agent.pauseAllTimers();
		String filename;
		for (UpdateOpParams uop:upParams){
			filename=checkDir(uop.location)+uop.name;
			switch(uop.op){
			case ERROR:
				return false;
			case DELETE:
				saveRollbackFile(filename);
				if(doDelete(filename)) break;
				else return false;
			case DELETEEMPTY:
				saveRollbackFile(filename);
				if(doDeleteEmpty(new File(filename))) break;
				else return false;
			case NEW:
				appendRollbackEntry("<DIR path=\""+filename+"\" remove=\"true\" />");
				if(doNew(filename, uop.isDir)) break;
				else return false;
			case UPDATE:  //This means update an existing file, not the update process as a whole
				saveRollbackFile(filename);
				if(doUpdate(checkDir(uop.location),uop.name)) break;
				else return false;
			}			
		}
		appendRollbackEntry("</ROLLBACK>");  //Update is done. Closing the XML means success. 
		return true;
	}

	private boolean parseUpdateXML() {
		File xmlFile= new File(checkDir(updateDir)+"update.xml");
		UpdateOpParamsBuilder uopb = new UpdateOpParamsBuilder(agentDir);
		Element root = null; 
		try{
			root = XMLUtil.getRootElement(xmlFile); 
		}catch (IOException ioe){
			System.out.println("Problem loading update.xml");
			ioe.printStackTrace();
		}
		NodeList list = XMLUtil.getChildNodesNoWS(root);//.getChildNodes();

		Element[] updateElements = new Element[list.getLength()];
		
		for (int i=0;i<list.getLength();i++)
			updateElements[i]=(Element)list.item(i);
		
		for(Element e:updateElements){
			boolean dir=false;
			UpdateOpParams thisOp;
			if (e.getTagName().equalsIgnoreCase("DIR")) dir=true;
			thisOp=uopb.buildUpdateOpParamsObj(e.getAttribute("location"),e.getAttribute("name"),e.getAttribute("action"), dir);
			if (null!=thisOp)
				 upParams.add(thisOp);	
			else return false;
		}
		
		return true;
	}

	private void handleUpdateFailure(failReason failResone) {
		//TODO: I'm here
		System.out.println("faliure: "+failResone);
		switch(failResone){
		case FILECORRUPT: //If the file is corrupt try downloading it one more time
			incomingZipFile.delete();
			if (downloadFile()){
				if(verifyFile(incomingZipFile, incomingZipSize, incomingZipMd5Hash)){
					if(unzipFile(incomingZipFile)){
						if (filesReady(true)){
							finalizeUpdate();
						}else break;
					}else break;
				}else break;
			}
			else break;
		case XMLPARSEFAIL:
			if(!parseUpdateXML())
			break;
		case ROLLBACKFAIL:
			if(!preperRollBack())
			break;
		case UPDATESCRIPTFAIL:
			if(!runUpdateScript())
			break;
		default:
			finalizeUpdate();
			break;
		}
/*		if(verifyFile(incomingZipFile, incomingZipSize, incomingZipMd5Hash))
			if(unzipFile(incomingZipFile))
				if (filesReady(true))
					finalizeUpdate();*/
		
	}

	private boolean downloadFile(){return true;}//TODO: re-enable this method.  
	
/*	private boolean downloadFile(){
		BufferedWriter outWriter = null;
		BufferedReader outReader = null;
		try {
		StringReader strReader = new StringReader(updateServerRequest);
		outReader = new BufferedReader(strReader);
		outWriter = new BufferedWriter(new FileWriter(incomingZipFile));
		updateComm.exchangeFiles(outReader, outWriter, agentHeader, agentTrailer);
		} catch (Exception e) {
			if( incomingZipFile != null && incomingZipFile.exists() ) {
				incomingZipFile.delete();
			incomingZipMd5Hash=null;
			return false;
			}
		}finally {
			IOUtils.closeQuietly(outWriter);
			IOUtils.closeQuietly(outReader);
		}
		return true;
	}*/
	
	private boolean verifyFile(File file, int size, String md5){
		if((size == (int)file.length())&&(md5.equals(getMD5Hash(file)))) return true;
		else return false;
	}
	
	private boolean unzipFile(File infile){
		final int BUFFER = 2048;
		File file =infile;
		ZipFile zf = null;
		ZipArchiveEntry entry;
		BufferedOutputStream dest = null;
		try {
			zf = new ZipFile(infile);
			Enumeration<ZipArchiveEntry> e = zf.getEntries();
			while(e.hasMoreElements()){
				entry=e.nextElement();
				InputStream content = zf.getInputStream(entry);
				File outFile = new File(checkDir(updateDir)+entry.getName());
				if(outFile.exists()) doDelete(outFile.getCanonicalPath());
				if(0==entry.getSize()){
					if (entry.getName().endsWith("/"))outFile.mkdir();
					else outFile.createNewFile();
				}else{
					 System.out.println("Extracting: " +entry.getName());
			            int count;
			            byte data[] = new byte[BUFFER];
			            // write the files to the disk
			            FileOutputStream fos = new FileOutputStream(checkDir(updateDir)+entry.getName());
			            dest = new BufferedOutputStream(fos, BUFFER);
			            while ((count = content.read(data, 0, BUFFER))!= -1) {
			               dest.write(data, 0, count);
			            }
			            dest.flush();
			            dest.close();
					}
			}
			 
		} catch (IOException e1) {
			e1.printStackTrace();
		}
/*		final int BUFFER = 2048;
		ZipInputStream zipIn=null;
		BufferedOutputStream dest = null;
		
		try {
			zipIn = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));
			ZipEntry currentEntry = zipIn.getNextEntry();
			while (!(null==currentEntry)){
			File outFile = new File(checkDir(updateDir)+currentEntry.getName());
			if(outFile.exists()) outFile.delete();
			if(0==currentEntry.getSize()){
				if (currentEntry.getName().endsWith("/"))outFile.mkdir();
				else outFile.createNewFile();
			}else{
				 System.out.println("Extracting: " +currentEntry);
		            int count;
		            byte data[] = new byte[BUFFER];
		            // write the files to the disk
		            FileOutputStream fos = new FileOutputStream(checkDir(updateDir)+currentEntry.getName());
		            dest = new BufferedOutputStream(fos, BUFFER);
		            while ((count = zipIn.read(data, 0, BUFFER))!= -1) {
		               dest.write(data, 0, count);
		            }
		            dest.flush();
		            dest.close();
				}
				currentEntry=zipIn.getNextEntry();
							
			}	
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}*/ finally{
			try {
				zf.close();
				dest.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		File[] uDirFiles = (new File(updateDir)).listFiles();
		if(uDirFiles.length>= 2)
			for (int i=0;i<uDirFiles.length;i++)
				if("update.xml".equalsIgnoreCase(uDirFiles[i].getName())){
					incomingZipFile.setWritable(true);
					incomingZipFile=null;
					return true;
					}
		return false;
	}

	private File getIncomingFileSlot(File incomingDir) throws IOException {
	if (!incomingDir.exists())
		incomingDir.mkdirs();
	return this.getDefaultIncomingSlot(incomingDir);
	}
	
	private File getDefaultIncomingSlot(File incomingDir) throws IOException {
		File inFile = new File(incomingDir, incomingZipName);// check
		if (inFile.exists())
			inFile.delete();
		if (!inFile.createNewFile())
			throw new IOException("could not create new file: "
					+ inFile.getAbsolutePath());
		return inFile;
	}

	private String getMD5Hash(File incoming){
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(incoming));

			byte[] buffer = new byte[8192]; // read buffer.
			int read = 0;
			
		     while( (read = bis.read(buffer)) > 0) {
		         md.update(buffer, 0, read);
		     }
		     
		     byte[] md5sum = md.digest();
		     BigInteger bigInt = new BigInteger(1, md5sum);
		     
		    return bigInt.toString(16);        
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private boolean saveRollbackFile(String filename){
		File file = new File(filename);
		String dotName = processDotName(filename);
		if(file.isDirectory()){
			if(file.listFiles().length>0){
				for (File f:file.listFiles()){
					if (f.isFile()) {
						copyfile(f.getAbsolutePath(),checkDir(agentDir)+"rollback"+processDotName(f.getAbsolutePath()) );
						appendRollbackEntry("<FILE path=\""+f.getAbsolutePath()+"\" />");
					}
					else saveRollbackFile(f.getAbsolutePath());
				}
			}
			appendRollbackEntry("<DIR path=\""+file.getAbsolutePath()+"\" />");
		}else{
			if (file.exists()){
				copyfile(file.getAbsolutePath(),checkDir(agentDir)+"rollback"+processDotName(file.getAbsolutePath()) );
				appendRollbackEntry("<FILE path=\""+file.getAbsolutePath()+"\" />");
			}else
				appendRollbackEntry("<FILE path=\""+file.getAbsolutePath()+"remove=\"true\" />");
		}
		return true;
	}
	
	private boolean doDelete(String filename){
		File file = new File(filename);
		try {
			FileUtils.forceDelete(file);
		} catch (IOException e) {
			try {
				FileUtils.forceDeleteOnExit(file);
			} catch (IOException e1) {
				e1.printStackTrace();
				return false;
			}
		}
/*		File file = new File(filename);
		boolean test ;
		if (!file.exists()) return true; //If the file doesn't exist, assume it's been deleted
		if(file.isDirectory() && (file.list().length >0)){			
				try {
					for(String s:file.list()){
						
						test = doDelete(checkDir(file.getCanonicalPath())+s);
					}
					return file.delete();
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
		}else {
			test = file.canWrite();
			test= file.delete();
			return true;
		}*/
		return true;
	}
	
	private boolean doNew(String filename, boolean dir){
		File file = new File(filename);
			try {
				if (file.exists()) doDelete(file.getCanonicalPath());
				if (dir) {
/*					if (file.isDirectory() && file.listFiles().length ==0) return true;
					if (file)*/return file.mkdir();
					}
				else return file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
	}
	
	private boolean doUpdate(String location, String file){
		File oldFile = new File (checkDir(location)+file);
		File newFile = new File (checkDir(updateDir)+file);
		if (!(newFile.exists()))return false;
		else{
			try {
				doDelete(oldFile.getCanonicalPath()); //no effect if oldFile doesn't exist
				copyfile(newFile.getCanonicalPath(),checkDir(location)+file);
				
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	private boolean doDeleteEmpty(File file){
		if (!file.isDirectory()) return false; //redundent really, as we check this in parsing. 
		file.delete();
		return true;
		//return file.isDirectory()?file.delete():false;
	}
	
	private boolean copyfile(String srFile, String dtFile){  
	     File f1 = new File(srFile);
	      File f2 = new File(dtFile);
	      InputStream in = null; 
	      OutputStream out = null; 
		try{
		in	= new FileInputStream(f1);
		out = new FileOutputStream(f2);

	      byte[] buf = new byte[8192];
	      int len;
	      while ((len = in.read(buf)) > 0){
	        out.write(buf, 0, len);
	      }
	    }
	    catch(FileNotFoundException ex){
	    	ex.printStackTrace();
		    return false;
	    }
	    catch(IOException e){
	      e.printStackTrace();
	      return false;
	    }finally{
	        try {
				if (null != in) in.close();
				if (null != out) out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		     
	    }
	    return true;
	  }
	
	private String checkDir(String dir){
		return dir.endsWith(File.separator)?dir:dir+File.separator;
	}
	
	/**This method takes a single string for the rollback XML and writes it  straight to file.
	 * NOTE: This is not an efficient way to construct the XML! A better way is to create the 
	 * whole XML in Memory and only then write to file. Writing directly to file in this case is
	 * done because the Agent might crash during the update, and the rollback script must include
	 * a list of all action until that point. 
	 * 
	 * @param entry
	 */
	private void appendRollbackEntry(String entry){
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter( new FileWriter(rollbackScriptFile, true)); //set append to true
 			bw.write(entry);
			bw.flush();
		} catch (IOException e) {
			System.out.println("Rollback script write error");
		} finally{
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
	
	/**A method to go between dot notation and file paths 
	 * 
	 * @param in
	 * @return
	 */
	private String processDotName(String in){
//		return in.replaceAll(java.util.regex.Matcher.quoteReplacement("\\"), File.separator );
	    final StringBuilder result = new StringBuilder();
	    final StringCharacterIterator iterator = new StringCharacterIterator(in);
	    char character =  iterator.current();
	    while (character != CharacterIterator.DONE ){
	     
	      if (character == File.separatorChar) {
	         result.append("^");
	      }
	       else  if ("^".equals(String.valueOf(character)) ) {
		         result.append(File.separator);
		      }      
	       else{
	        result.append(character);
	      }

	      
	      character = iterator.next();
	    }
	    return result.toString();

	}
}
