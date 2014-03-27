package gui;


public abstract class StateHolder {

	private static boolean connectedGUI=false; //Is the GUI connected to the Agent
	private static boolean conenctedAgent=false;//Is the Agent connected to the server
	private static String lastMeasurement=""; //Address of least measurement
	private static int numberOfMeaurements=0;//Number of measurements since gui was started
	private static boolean shutdownRequested=false; //was shutdown requested by the GUI thread
	
	public static boolean isConnectedGUI() {
		return connectedGUI;
	}
	public static void setConnectedGUI(boolean connectedGUI) {
		StateHolder.connectedGUI = connectedGUI;
	}
	public static boolean isConenctedAgent() {
		return conenctedAgent;
	}
	public static void setConenctedAgent(boolean conenctedAgent) {
		StateHolder.conenctedAgent = conenctedAgent;
	}
	public static String getLastMeasurement() {
		return lastMeasurement;
	}
	public static void setLastMeasurement(String lastMeasurement) {
		StateHolder.lastMeasurement = lastMeasurement;
	}
	public static int getNumberOfMeaurements() {
		return numberOfMeaurements;
	}
	public static void setNumberOfMeaurements(int numberOfMeaurements) {
		StateHolder.numberOfMeaurements = numberOfMeaurements;
	}
	
	public static void incerementResultCount(){
		numberOfMeaurements++;
	}
	public static boolean isShutdownRequested() {
		return shutdownRequested;
	}
	public static void setShutdownRequested(boolean shutdownRequested) {
		StateHolder.shutdownRequested = shutdownRequested;
	}
	
}
