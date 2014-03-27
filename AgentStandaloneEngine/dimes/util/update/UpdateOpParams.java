package dimes.util.update;

import dimes.util.update.UpdateOpParamsBuilder.ops;


public class UpdateOpParams {

	public String location;
	public String name;
	public ops op;
	public boolean isDir;
	
	UpdateOpParams (String locationP, String nameP, ops opP, boolean isdirP) {
		op=opP;
		isDir=isdirP;
		location=locationP;
		name=nameP;
	}


}

