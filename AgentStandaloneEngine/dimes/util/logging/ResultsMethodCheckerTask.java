package dimes.util.logging;

import java.util.TimerTask;
import java.util.logging.Handler;

public class ResultsMethodCheckerTask extends TimerTask {

	
	@Override
	public void run() {
		Handler[] AsHandlers = Loggers.getASInfoLogger().getHandlers();
		for(Handler h:AsHandlers){
			if (h instanceof ASInfoUpdateGraphHandler)
			{
				if (!((ASInfoUpdateGraphHandler)h).isGraphWorking()) Loggers.swapResultsMethod();
			}
		}
//		javax.swing.JOptionPane.showMessageDialog(null, "Results Task Run");
		
	}

}
