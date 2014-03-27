/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package NetGraph.graph.components;

 /**
 * Titre : NetGraph
 * Un arc du graphe
 * @version=0.1
 * Copyright : Copyright (c) 2000
 * @author=Cyrille Morvan (morvan@fiifo.u-psud.fr)
 */
public class Edge extends FilteredGraphComponent{
    
    public Edge(){
        super();
    }
	
    public int from;
    public int to;    
    public double len;
        
	
//	public boolean isFilteredByScriptSelection = false;
	 
//	private Vector discoveringScripts = new Vector(); 
	
	// Relaxation constans :
	public static final int RELAX_GROUP_BY_ISP = 1;
	public static final int RELAX_GORUP_BY_COUNTRY = 2;
	public static final int RELAX_GROUP_BY_NONE = 0;
	

    /** Regarde si deux arcs sont egaux
     * @param e l'edge a compare
     */
    public boolean equals(Edge e) {
      return from==e.from && to==e.to;
    }
    
//    /*******************
//     * add a discovery for the specific script
//     * 
//     * @param scriptName
//     */
//    public void addDiscoveringScript(String scriptName){
//    	if (!discoveringScripts.contains(scriptName))
//    		discoveringScripts.add(scriptName);
//    }
//    
//    
//    /************
//     * indicates that the Edge was discovered by a particular script
//     * 
//     * @param scriptName - the script name to verify discovery.
//     */
//    public boolean wasDiscoveredBy(String scriptName){
//    	boolean result =  discoveringScripts.contains(scriptName);
//    	return result;
//    }
}
