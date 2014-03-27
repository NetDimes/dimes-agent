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
 * Un noeud du graphe
 * @version=0.1
 * Copyright : Copyright (c) 2000
 * @author=Cyrille Morvan (morvan@fiifo.u-psud.fr)
 */
import java.awt.Color;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import NetGraph.utils.Conf;
import NetGraph.utils.GraphInetAddress;

public class Node extends FilteredGraphComponent implements Runnable{
	public double x;
	public double y;

	public double dx;
	public double dy;
	
	public static final int TRANSPARENCY_ALPHA_CONSTANT = 80;

    /** Savoir si le noeud est fixe (bord bleu)*/
    public boolean fixed;

    /** Savoir si le label a ete modifie */
    public boolean labelModif = false;
  
    
    public int inDegree=0;
    public int outDegree=0;
//    public boolean isInProgress = false;
    
    public boolean isCollapsed = false;
//    public boolean isFiltered = false;
    public boolean isRetreived=false;
    public boolean isPruned=false;
    private static final Color LOCALHOST_COLOR = Color.CYAN;
   

    //Toutes ces valeurs sont publiques : c'est pour le module
    //d'export csv  (le 29/8/2000)

    /** L'addresse IP : unique pour chaque node */
    public InetAddress IP;
    /** la label de l'etiquette par defaut*/
    public String lbl;
    /** Le numero d'ip*/
    public String IPtexte="??";
    /** le dns (peut etre identique a l'IP*/
    public String dns="??";
    /** Le nom de la machine */
    public String machinename = "??";

//    public final String dot = " ";

    // information retreived from Server:
    public String country="Get info";
    public String ISPName="Get info";
    public int ASNumber=-1;
  
    
    /** Les informations retournees par le traceroute */
    public String information;

    /** La moyenne des pings ....*/
    public  int itime= 0;
    public  String stime = "0";
	
	public  Color asColor = new Color(250, 220, 100);
	public  Color countryColor = new Color(250, 220, 100);
	
	public Color asColorFiltered = new Color(250, 220, 100 , TRANSPARENCY_ALPHA_CONSTANT );
	public  Color countryColorFiltered = new Color(250, 220, 100 , TRANSPARENCY_ALPHA_CONSTANT);
//	private boolean isDot=false;
    public boolean isAgent=false;
    public int collapseSource=-1;
    public int collapseDest=-1;
    public int virtualSource=-1;
    public int virtualDest=-1;
    public double distFromSource=-1;
    
	
	public Node()
	{
	    super();
	}

    /** Thread pour recuperer le DNS de l'IP
     * @param pardefault texte en attendant que l'on trouve le DNS
     */
	public void setLabel(String pardefault) {
      this.lbl=pardefault;
      if(IP!=null) {
        Thread T =new Thread(this);
        T.setPriority(Thread.MIN_PRIORITY + 2);
        T.start();
      }
    }
    /** Recupere le DNS du noeud */
    public void run() {
        IPtexte =  IP.getHostAddress();
        /* On recupere le temps du ping et on fait une moyenne*/
        AffecteTexteTemps(information);
        /* On recupere les informations DNS */
        AffecteTexteDns(IP.getHostName());
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        /* On modifie le label, en fonction des choix de
         * l'utilisateur */
//        System.out.println("setting with " +Conf.getConf().get_int(Conf.getConf().NodeLabel));
//        isInProgress = false;
        
        ChangeLabel();
    }
    
    public boolean isCollapseable(){
    	return (!isCollapsed || (!(
					(inDegree == 1)  && (outDegree == 1))));
    }

    /** Change le label en fonction de I, si le label n'a pas ete
     * modifie a la main
     * @param i <br>
     * Pour 0 nom de la machine<br>
     * Pour 1 l'IP<br>
     * Pour 2 le dns<br>
     * Pour autre, change pas
     */
    public void ChangeLabel(int i) {
      if( ! labelModif) {
        if(i==0) lbl = machinename;
        else if(i==1) lbl = IPtexte;
        else if(i==2) lbl = dns;
        else if(i==3) 
        	{
        	lbl = null;
        	}
        else if(i==4) lbl = this.ISPName;
		else if(i==5) lbl = this.country;
       }
    }

   
    public void ChangeLabel() {
    	 ChangeLabel(Conf.getConf().get_int(Conf.getConf().NodeLabel));
    }

    /** On attribue le dns au variable
     * @param txt [ %d.%d.%d.%d ] ou [ %s.%s.%s.[..].%s ] ou [ %s ]
     */
    private void AffecteTexteDns(String name) {
      int point = name.indexOf(".");
    /*    Modification .... pour faire plaisir a Loic  (le 8/8/2000)
     * if(Character.isDigit(name.charAt(0)) && Character.isDigit(name.charAt(point+1)) )
     * //On stoke l'ip a la place du nom de la machine
     *   machinename=name;
     * else
     * // On stocke le nom de la machine
     *   machinename=name.substring(0,(point==-1) ? name.length() : point);
     * dns = name ;
     */

      // On affecte le dns
      dns = name ;
      // On affecte le nom de la machine
      try {
        // On regarde si name est une IP (si il n'y a pas d'erreur a la conversion)
        GraphInetAddress.ToGraphInetAddress(name);
        //name est une IP (sur et certain)
        machinename=name;
      } catch(UnknownHostException err) {
        //name est un dns
        machinename=name.substring(0,(point==-1) ? name.length() : point);
      }
    }

    /**
     * Trouver a partir des informations du traceroute
     * le temps de chacun des pings
     * @param tmps les informations traceroute de la forme : <br>
     * [ %d %d ms %d ms %d ms %ip ] ou [ %d %ip %d ms %d ms %d ms ]<br>
     * le premier %d est ignore (c'est la position du noeud)
     */
    private void AffecteTexteTemps(String tmps) {
      if(tmps!=null) {
        StringTokenizer t = new StringTokenizer(tmps, " ");
        int i = 0;
        float total= 0.0f;
        t.nextToken(); //on ignore la premiere valeur, c'est un chiffre
                       //dans tous les traceroutes correspondant a la position
                       //du noeud
        /* Pour chaque morceau*/
        while(t.hasMoreTokens()) {
          try {
            String s = t.nextToken();
            /* Si il y a un point dans le texte, c'est peut etre un float*/
            if (GraphInetAddress.Compte(s,".")<=1) {
              /* Avec le traceroute Windows, si il y a un <, on ignore*/
              if(s.startsWith("<")) s= s.substring(1);
              /* On additionne les valeurs des pings */
              total+= Float.valueOf(s).floatValue();
              /* On incremente le compteur du nombre de ping */
              i++;
            }
          } catch (Exception err) { } // Ce n'est pas un float ... tans pis
        }
        // On affiche -1 comme message d'erreur si aucun chiffre n'a ete trouve
        itime = (int) ( (i==0) ? -1 : total / i );
        stime = Integer.toString(itime);
      }
    }

//    /**
//     * 
//     */
//    public void setColorInProgress(boolean progress) {
////        isInProgress = progress;    
//        if (progress==true)
//        {
//            Thread T = new Thread(this);
//            T.setPriority(Thread.MIN_PRIORITY + 2);
//            T.start();
//        }
//    }

  public void setAsOrigin(){
      asColor = Node.LOCALHOST_COLOR;
      countryColor = Node.LOCALHOST_COLOR;
      this.isAgent=true;
  }
    
 
    
}



