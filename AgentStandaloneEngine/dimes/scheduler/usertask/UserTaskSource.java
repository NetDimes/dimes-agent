/*
 * Created on 25/08/2005
 *
 */
package dimes.scheduler.usertask;

/**
 * @author Ohad Serfaty
 *
 * This interface represents a source of a user task.
 * basicly , it parses commands from a source (e.g file, url etc)
 * and provides an iterator over the tasks to create a script,
 * parseable by the parser.
 * 
 */
public interface UserTaskSource
{

	public void parse() throws UserTaskPerserException;
	public String getCommandsString();
	/**
	 * @return
	 */
	public String getScriptID();

}