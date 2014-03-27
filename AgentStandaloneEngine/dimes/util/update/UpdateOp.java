package dimes.util.update;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import dimes.measurements.operation.MeasurementOp;
import dimes.scheduler.Task;
import dimes.scheduler.TaskManager;
import dimes.util.properties.PropertiesBean;
import dimes.util.properties.PropertiesNames;
import dimes.util.properties.PropertiesBean.NoSuchPropertyException;
import dimes.util.update.UpdateOpParamsBuilder.ops;

public class UpdateOp extends MeasurementOp {

	LinkedList<UpdateOpParams> updateCommands;
	public static String baseDir;
	int id;
	
	
	public UpdateOp(LinkedList<UpdateOpParams> up, int idP, Task task) {
		super(task);
		updateCommands=up;
		id=idP;
		try {
			
			String temp=PropertiesBean.getProperty(PropertiesNames.BASE_DIR);
			temp = temp.substring(0,temp.indexOf("gent")+4);
			baseDir=(new File(temp)).getCanonicalPath();
		} catch (NoSuchPropertyException e) {
			baseDir=dimes.platform.PlatformDependencies.os==dimes.platform.PlatformDependencies.WINDOWS?"C:\\Program Files\\DIMES\\Agent":null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean execute() {
		String updateXML=makeUpdateXML();
		File outputDir = new File(baseDir+File.separatorChar+"update");
		File outputFile=null ;
		BufferedWriter output=null;
		
		if(!outputDir.exists()) outputDir.mkdir();
		try {
			outputFile = new File(outputDir.getCanonicalPath()+File.separatorChar+"update.xml");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			output=new BufferedWriter(new FileWriter(outputFile));
			output.write(updateXML);
			output.flush();
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		finally{
			try {
				output.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		Thread fileGetter = new UpdateFilesGetter(updateCommands, Integer.toString(id),baseDir+File.separatorChar+"update");
//		fileGetter.start();
		TaskManager.setUpdatePending(true); //only set if all goes well.
		return true;
	}
//--
	
	//--
	@Override
	public boolean isTimedOperation() {
		
		return false;
	}

	private String makeUpdateXML(){
		StringBuilder output= new StringBuilder("<UPDATE id=\""+id+"\">\n");
		
		for(UpdateOpParams uop: updateCommands){
			String type=uop.isDir?"DIR":"File";
			output.append("<"+type+" op=\""+uop.op+"\" location=\""+uop.location+"\" >"+uop.name+"</"+type+">\n");
		}
		output.append("</UPDATE>");
		return output.toString();
	}
	
	public static void main(String[] args){
/*		PropertiesBean.init("C:\\Program Files\\DIMES\\Agent\\Classes\\Base\\conf\\properties.xml");
		baseDir="C:\\Program Files\\DIMES\\Agent\\Classes\\Base\\conf\\properties.xml";
		LinkedList<UpdateOpParams> list = new LinkedList<UpdateOpParams>();
		list.add(new UpdateOpParams("", "test", ops.UPDATE, true));
		list.add(new UpdateOpParams("resources", "moo.gif", ops.DELETE, false));
		list.add(new UpdateOpParams("Classes\\base", "conf",ops.NEW, true));
		UpdateOp op = new UpdateOp(list, 1,null);
		op.execute();*/
		baseDir="C:\\Program Files\\DIMES\\Agent\\Classes\\Base\\conf";
		File noSlash = new File(baseDir);
		File yesSlash = new File(baseDir+File.separator);
		System.out.println("noSlash exit?"+Boolean.toString(noSlash.exists()));
		System.out.println("yesSlash exit?"+Boolean.toString(yesSlash.exists()));
		try{
			System.out.println("noSlash path:"+noSlash.getCanonicalPath());
			System.out.println("yesSlash path:"+yesSlash.getCanonicalPath());
			File noFile = new File(noSlash.getCanonicalPath()+"properties.xml");
			File yesFile = new File(noSlash.getCanonicalPath()+File.separator+"properties.xml");
			System.out.println("noFile exit?"+Boolean.toString(noFile.exists()));
			System.out.println("yesFile exit?"+Boolean.toString(yesFile.exists()));
		}catch(IOException e){
			
		}
		
	}

	public static String getBaseDir() {
		return baseDir;
	}
}
