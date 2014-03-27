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


/** Cette classe permet d'avoir un acces rapide
 * au fichier de configuration
 * @author Cyrille 
 */
public class Conf {
  /*************************************/
  /** le numero de version du logiciel**/
  /*************************************/  
  public static double VERSION = 0.10;
  public static String DATE_VERSION = "23/9/2000";
  /*************************************/

//  private static EcranPrincipal e;
//  /** Il n'est pas conseille d'utiliser cette methode*/
//  public static EcranPrincipal getE() { return e; }
//  static void setE(EcranPrincipal e) { Conf.e = e; }

  private static Configuration confg;
  /** Obtenir les elements de configuration (a partir du fichier) */
  public static Configuration getConf() {  return confg; }
  public static void setConf(Configuration c) { confg = c; }
}
