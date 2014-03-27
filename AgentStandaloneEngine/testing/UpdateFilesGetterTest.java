package testing;

import static org.junit.Assert.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.junit.Before;
import org.junit.Test;

import dimes.util.properties.PropertiesBean;
import dimes.util.update.UpdateFilesGetter;

public class UpdateFilesGetterTest {

	File inputZIP;
	UpdateFilesGetter getter;
	@Before
	public void setUp() throws Exception {
		PropertiesBean.init("C:\\Program Files\\DIMES\\Agent\\Classes\\Base\\conf\\properties.xml");
		inputZIP=new File("C:\\Program Files\\DIMES\\Agent\\update\\updatetest1.zip");
		String filename="updatetest1.zip";
		int filesize = (int)inputZIP.length();
		String md5= getMD5Hash(inputZIP);
		getter = new UpdateFilesGetter("1",filename,"C:\\Program Files\\DIMES\\Agent",filesize,md5);
	}	
	
	@Test
	public void testrun(){
		getter.run();
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
	/*		@Test
	public void testunzipFile(){
		assertTrue(dimes.util.update.UpdateFilesGetter.unzipFile(inputJAR, "c:\\test"));
		fail("fail");
	}

@Test
	public void testGetMD5Hash() {
//		byte[] ba = dimes.util.update.UpdateFilesGetter.getMD5Hash(inputJAR);
//		for (int i=0;i<ba.length;i++)
//			System.out.print((char)ba[i]);
//
//		assertNotNull(ba);
		fail("Not yet implemented");
	}*/

}
