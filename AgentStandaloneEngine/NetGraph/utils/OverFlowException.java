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

package NetGraph.utils;

/** Exception lorsque il y a trop d'element dans le graph
 * @author Cyrille Morvan <morvan@fiifo.u-psud.fr>
 */
public class OverFlowException  extends Exception {

  private int overflowCloseness=0;

public OverFlowException(String txt) {
    super(txt);
  }
  
  public OverFlowException(String txt, int anOverflowClossness) {
    super(txt);
    overflowCloseness=anOverflowClossness;
  }

/**
 * @return a number indicating how many more nodes/edges can be 
 * inserted to the graph.
 * 
 * when the return value is 0 - one cannot add another
 * edge/node into the graph.
 * 
 */
public int getOverflowClossness() {
	return overflowCloseness;
}

} 