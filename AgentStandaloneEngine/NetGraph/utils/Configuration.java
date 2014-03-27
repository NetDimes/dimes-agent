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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.ResourceBundle;


 /**
 * Titre : NetGraph
 * Classe de configuration de l'application
 * @version=0.1
 * Copyright : Copyright (c) 2000
 * @author=Cyrille Morvan <morvan@fiifo.u-psud.fr>
 */
 public class Configuration {

 /** Contient toutes les proprietes */
  private Properties properties = null;
//  private ServicesInformation servicesInformation = null; 

  /** Fichier de config */
    public final static  String CONFIG_DIRECTORY =  System.getProperty("user.home") + File.separator + ".netgraph" + File.separator;
    private final static String CONFIG_FILE = CONFIG_DIRECTORY + "netgraph.cfg";
    private final static String HEADER_FILE = "NetGraph "+ Conf.VERSION + " Config file --- please do not edit";


 /** Taille minimun d'ecran et par defaut*/
  private final int   MIN_WIDTH   = 550;
  private final int   MIN_HEIGHT  = 400;

 /** Liste des serveurs WhoIs, separe par des virgules*/
  private final String  WHOIS = "whois.internic.net,rs.internic.net,whois.arin.net,"+
                                "whois.nic.fr,"+
                                "whois.ripe.net,www.apnic.net,nic.ddn.mil,whois.ddn.mil,"+
                                "whois.networksolutions.com,whois.nic.mil";

  /** Nom sous lequel la liste des serveurs Whois est enregistree */
  public final String ServeurWhoIs = "whois.serveur";

  /** Nom sous lequel la version est enregistree */
  public final String NetGraphVersion = "netgraph.version";

  /** Nom sous lequel le nom du prog traceroute est enregistre */
  public final String ProgrammeTraceroute = "netgraph.traceroute";
  /** Nom sous lequel le nom de l'option traceroute est enregistre */
  public final String ProgrammeTracerouteHostList = "netgraph.traceroutehostlist";

  /** Nom sous lequel le nom du prog telnet est enregistre */
  public final String ProgrammeTelnet = "netgraph.telnet";

  /** Nom sous lequel la langue utilise est enregistree */
  public final String LanguageUse = "netgraph.lang";
  
  /** Nom sous lequel on enregistre le label que l'on utilise pour les nodes */
  public final String NodeLabel = "node.label";
  public final String NodeLabelSize = "node.labelsize";

  /** Fichier qui contient la liste de tous les services
   * par exemple /etc/services */
  public final String ServicesFile = "netgraph.services";

  private ResourceBundle lang;

 /**
  * Constructeur : charge le fichier
  */
  public Configuration() {
    Conf.setConf(this);
    this.readProperties();
//    this.readServices();
//    this.setLang();
    this.verifVersion();
  }

  /**
   * Verifie le numero de version
   * remet les valeurs par defaut si la version a augmenter
   */
  public void verifVersion() {
    double ver = this.get_double(NetGraphVersion);
    if(ver<Conf.VERSION) {
//      System.out.println("Nouvelle version : " + Conf.VERSION);
     /* System.out.println("utilisation des valeurs par defaut");*/
      this.put(NetGraphVersion,Conf.VERSION);
/*      this.setDefault();*/
    } else if(ver!=Conf.VERSION)  this.put(NetGraphVersion,Conf.VERSION);
  }

 /**
  * Charge les informations a partir du fichier de config
  */
  private void readProperties() {
     Properties p = new Properties();
    try {
      p.load(new FileInputStream(CONFIG_FILE));
    } catch(Exception e) {
     // System.out.println(e.toString());
      File file = new File(CONFIG_DIRECTORY);
      file.mkdir();
    //      e.printStackTrace();
    }
    properties = p;
  }

//  private void readServices() {
//    servicesInformation  = new ServicesInformation( this.get(ServicesFile,this.getDefaultServicesFileName()));
//  }
//
//  /**
//   * Obtenir des informatons sur un port
//   * @return une description
//   */
//  public String getPortDescription(int port) {
//    try {
//      return servicesInformation.getDescription(new Integer(port));
//    } catch(Exception e) {
//      e.printStackTrace();
//     return "Error"; }
//  }

// /* *
//  * Definir les resources langues en fonction du fichier
//  */
//  private void setLang() {
//    try { //On recupere les textes
//      lang = ResourceBundle.getBundle("NetGraph.res.Lang"+this.get(LanguageUse));
//    }
//    catch (Exception e) {
//      lang = ResourceBundle.getBundle("NetGraph.res.Lang");
//    }
//  }
 /** Obtenir les ressources de textes
   * @return Un pointeur vers les ressources texte de
   * l'application
   */
  public ResourceBundle getLang() {
    return lang;
  }

  /** Reprend toutes les valeurs par defaut */
  public void setDefault() {
    /* Taille d'ecran */
    this.put("netgraph.width",MIN_WIDTH);
    this.put("netgraph.height",MIN_HEIGHT);
    /* le traceroute */
    this.put(ProgrammeTraceroute,this.getDefaultTraceRoute());
    this.put(ProgrammeTracerouteHostList,this.getDefaultTraceRouteHostList());
    /* serveur Whois */
    this.put(ServeurWhoIs,this.WHOIS);
    
  }

 /**
  * Enregistrement de la configuration dans un fichier
  */
  public void saveProperties() {
    try {
      OutputStream out = new FileOutputStream(CONFIG_FILE);
      properties.save(out, HEADER_FILE);
      out.close();
    }
    catch(Exception e) {
      /* Il faudrait peut etre affiche une fenetre */
//      System.err.println("Erreur dans la sauvergarde du fichier");
    }
  }
  /** Obtenir le nom du programme traceroute sur la machine
   * @return tracert ou traceroute
   */
  public String getTraceRoute() {
    return this.get(ProgrammeTraceroute,getDefaultTraceRoute());
  }

  /** Obtenir l'option traceroute qui permette de forcer le passe
   * par un hote donnee
   * @return un truc du genre -j sous Windows
   */
  public String getTraceRouteHostList() {
    return this.get(ProgrammeTracerouteHostList,getDefaultTraceRouteHostList());
  }


   String getDefaultServicesFileName() {
      return CONFIG_DIRECTORY + "services.txt" ;
  }

  private String getDefaultTraceRoute() {
      /* Si on se trouve sur Windows*/
      if(System.getProperty("os.name").indexOf("Windows")!=-1)
       return  "tracert -d";
      /* On se trouve certainement sur Un*x */
      else return "traceroute -n"; //A verifier : si -n est valide sur une majorite d'unix
  }

  private String getDefaultTraceRouteHostList() {
      /* Si on se trouve sur Windows*/
      if(System.getProperty("os.name").indexOf("Windows")!=-1)
       return  "-j";
      /* On se trouve certainement sur Un*x */
      else return "-s"; //A verifier : si -g est valide sur une majorite d'unix
  }
  /** Obtenir le nom du programme telnet sur la machine
   * @return le programme telnet */
  public String getTelnet() {
    return this.get(ProgrammeTelnet,getDefaultTelnet());
  }
  private String getDefaultTelnet() {
      /* Si on se trouve sur Windows*/
      if(System.getProperty("os.name").indexOf("Windows")!=-1)
       return  "telnet";
      /* On se trouve certainement sur Un*x */
      else return "xterm -e telnet"; //a chercher 
  }

  /** Obtenir la taille de l'ecran, il y a une taille minimun et la taille
   *  ne peut pas etre plus grande que la taille de l'ecran
   * @return une dimension contenant la taille de l'ecran
   */
  public Dimension getNetGraphSize() {
    int w = get_int("netgraph.width",MIN_WIDTH);
    int h = get_int("netgraph.height",MIN_HEIGHT);
    Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
    return new Dimension(Math.min(w,screensize.width-20),Math.min(h,screensize.height-50) );
  }

  /** Obtenir la liste des serveurs WhoIs
   * @return chacun des serveurs separes par des virgules
   */
  public String getWhoIsServeur() {
    String tr = this.get(ServeurWhoIs);

    if(this.isNullOrEmpty(tr)) {
      this.put(ServeurWhoIs,this.WHOIS);
      return this.WHOIS;
    } else return tr;

  }

  /** Enregistrer la taille courante de l'ecran
   * @param sz la dimension de l'ecran
   */
  public void setNetGraphSize(Dimension sz) {
    this.put("netgraph.width",sz.width);
    this.put("netgraph.height",sz.height);
  }

 /**
  * Permet d'obtenir la valeur booleen d'une entree dans le fichier de config <br>
  * @param key  nom de la variable
  * @param b la valeur par defaut
  * @return  true si la valeur est yes, false sinon. La valeur par defaut si vide
  */
  public boolean get_bool(String key, boolean b) {
    String tmp = get(key);
    if (this.isNullOrEmpty(tmp) )
      return b;
    else if (tmp.trim().equalsIgnoreCase("yes"))
      return true;
    else return false;
  }

 /**
  * Permet d'obtenir la valeur booleen d'une entree dans le fichier de config <br>
  * @param key  nom de la variable
  * @return  true si la valeur est yes ou vide sinon false
  */
  public boolean get_bool(String key) {
    return get_bool(key,true);
  }

  /**
   * Permet d'obtenir le texte de la variable demande<br>
   * @param texte  nom de la variable
   * @param defaut valeur par defaut
   * @return  le texte de la variable, retourne "" si variable n'existe pas
   */
  public String get(String texte, String defaut) {
      return properties.getProperty(texte,defaut);
  }
  /**
   * Permet d'obtenir le texte de la variable demande<br>
   * @param texte  nom de la variable
   * @return  le texte de la variable, retourne "" si variable n'existe pas
   */
  public String get(String texte) {
    return get(texte,"");
  }


 /**
  * Permet d'obtenir l'entier contenu dans une variable   <br>
  * @param chiffre  nom de la variable qui contient le chiffre
  * @param defaut valeur par defaut donne a la valeur si elle n'existe pas
  * @return  un entier correspond a la valeur de la variable
  */
  public int get_int(String chiffre, String defaut) {
	  String tmp = get(chiffre,defaut);
      Integer intg;
    try {
      intg = new Integer(tmp);
    } catch(Exception e) { intg = new Integer(defaut); }

    return intg.intValue();
  }
 /**
  * Permet d'obtenir l'entier contenu dans une variable   <br>
  * @param chiffre  nom de la variable qui contient le chiffre
  * @return un entier correspond a la valeur de la variable, 0 si n'existe pas
  */
  public int get_int(String chiffre) {
 	  return get_int(chiffre,"0");
  }
 /**
  * Permet d'obtenir l'entier contenu dans une variable   <br>
  * @param chiffre  nom de la variable qui contient le chiffre
  * @param defaut valeur par defaut donne a la valeur si elle n'existe pas
  * @return  un entier correspond a la valeur de la variable
  */
  public int get_int(String chiffre, int defaut) {
    return get_int(chiffre,(new Integer(defaut).toString()) );
  }

 /**
  * Permet d'obtenir le double contenu dans une variable   <br>
  * @param chiffre  nom de la variable qui contient le chiffre
  * @return  retourne un double correspond a la valeur de la variable
  * si la variable n'existe pas, retourne 0.0
  */
  public double get_double(String chiffre) {
	  String tmp = get(chiffre);
  	if (isNullOrEmpty(tmp)) {
	    return 0.0;
	  } else {
	    Double d = new Double(tmp);
	    return d.doubleValue();
  	}
  }


 /**
  * Attribue a une variable du texte
  * @param variable  nom de la variable
  * @param texte   texte a attribuer
  */
  public void put(String variable, String texte) {
    try {
    properties.put(variable,texte);
    } catch (Exception e) {e.printStackTrace(); }
  }

  /**
   * Attribue a une variable un entier
   * @param variable  nom de la variable
   * @param nombre   nombre a attribuer
   */
  public void put(String variable, int nombre) {
    Integer nbre= new Integer(nombre);
    put(variable,nbre.toString());
  }

  /**
   * Attribue a une variable un double
   * @param variable  nom de la variable
   * @param nombre   nombre a attribuer
   */
  public void put(String variable, double nombre) {
    Double nbre= new Double(nombre);
    put(variable,nbre.toString());
  }

  /**
   * Attribue a une variable un booleen <br>
   * Si le booleen est true, il attribue yes<br>
   * sinon il attribue no
   * @param variable  nom de la variable
   * @param b   boolean a attribuer
   */
  public void put(String variable, boolean b) {
    put(variable,(b) ? "yes" : "no");
  }

 /**
  * Texte nulle ou vide
  * @param tmp  texte a tester
  * @return vrai si le texte est vide ou null, sinon false
  */
  private boolean isNullOrEmpty(String tmp) {
    if (tmp == null || (tmp != null && tmp.length() == 0))
      return true;
    else
      return false;
  }

}
