package dimes.util.debug;

import java.io.IOException;

import dimes.util.logging.RotatingAnnouncingFileHandler;

public class DebugFileHandler extends RotatingAnnouncingFileHandler {

	public DebugFileHandler(String dir, String suffix, String name)
			throws IOException, SecurityException {
		super(dir, suffix, name);
		// TODO Auto-generated constructor stub
	}

}
