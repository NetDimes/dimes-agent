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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

 /**
 * Titre : NetGraph
 * Manipulation d'une adresse IP
 * @version=0.1
 * Copyright : Copyright (c) 2000
 * @author=Cyrille Morvan (morvan@fiifo.u-psud.fr)
 */
public final class GraphInetAddress {

  /** les quatres chiffres d'une adresse IP*/
  private short[] tabIP = new short[4];


  /**
   * Transforme une String en une GraphInetAddress
   * @param txt le texte a convertir
   * @return nouvelle IP
   * @exception UnknownHostException si impossible de retrouver une IP dans txt
   */
   public static GraphInetAddress ToGraphInetAddress(String txt) throws UnknownHostException {
    GraphInetAddress ip = new GraphInetAddress();
    try {
      int i=0;
      for (StringTokenizer t = new StringTokenizer(txt, ".") ; t.hasMoreTokens() ; i++ ) {
        ip.tabIP[i]= Short.parseShort(t.nextToken());
        if(ip.tabIP[i]>255 || ip.tabIP[i]<0) throw new Exception();
      }
      if(i!=4) throw new Exception();
    } catch(Exception e) {
        throw new UnknownHostException();
    }
    return ip;
   }

   /**
    * Augmenter de 1 l'addresse IP
    * Souleve une erreur si l'ip est trop grande
    * @execption Si il est impossible d'atteindre l'IP suivante
    */
   public void Next()  throws Exception {
    for(int i=3;i>=0;i--){
      if(this.tabIP[i]<255) {
         this.tabIP[i]++;
         return;
      }
      else this.tabIP[i]=0;
    }
    throw new Exception("Impossible d'atteindre l'IP suivante");
   }

   /**
    * Convertit en String l'ip
    * @return du texte
    */
   public String toString() {
    return tabIP[0]+"."+ tabIP[1] + "." + tabIP[2]+"."+ tabIP[3];
   }
   /**
    * Compare deux Addresse IP en partant du debut
    * @return true si la premiere address est inferrieur a la deuxieme
    */
   public boolean inferrieur_egal( GraphInetAddress  ip2) {
    int i=0;
    do {
      if(this.tabIP[i]<ip2.tabIP[i])
        return true;
      else if (this.tabIP[i]>ip2.tabIP[i])
        return false;
      else i++;
    } while(i<4);
    /* Les deux IP sont egales */
    return true;
  }


 /**
  * Convertir une chaine de caractere qui contient une adresse IP
  * en une adresse IP, l'adresse IP doit etre entre <br>
  * - crochet [] <br>
  * - parenthese () <br>
  * - espace <br>
  * sinon retourne null
  * @param host la chaine de caractere
  * @return une InetAddress qui contient l'IP
  */
  public static java.net.InetAddress getAllBySpecialName(String host) {
    final int lng=5;
    String[] CrochetOuvrant={"[","("," "," "," "};
    String[] CrochetFermant={"]",")"," ","\n","\r"};

    /* Pour chaque type de separateur */
    for(int i=0;i<lng;i++) {
      int pos1,pos2=0;
      /* Pour chaque bloc de texte separe par les separateurs*/
      do {
        pos1=host.indexOf(CrochetOuvrant[i],pos2);
        if(pos1!=-1) {
          pos2=host.indexOf(CrochetFermant[i],pos1+1);
          if(pos2!=-1) {
            /* On stocke le bout de texte que l'on vient de trouver*/
            String  tmpHost = host.substring(pos1+1,pos2);
            /* On a trouve crochet ouvrant et crochet fermant*/
            /* On regarde si on a un truc qui ressemble a une IP*/
            if( (!CrochetOuvrant[i].equals(" ")) || (Compte(tmpHost ,".")==3 ) ) {
              /* On a peut etre une IP, on la convertit*/
              try {
                return InetAddress.getByName(tmpHost);
              } catch(Exception e) {}
            }
          } else break;  //On n'a pas trouve le crochet fermant
        } else break; //On n'a pas trouve le crochet ouvrant
      }while(true); //while((pos1!=-1) && (pos2!=-1));
    }

    return null;
  }

 /**
  * Compte le nombre de String dans une autre String
  * @param txt la chaine de caractere la plus longue
  * @param el la chaine de caractere a compter
  * @return le nombre de fois que el a ete trouve dans txt (0 si pas trouve)
  */
  public static int Compte(String txt,String el) {
    int pos1=0;int nbpoint=0;
    while( (pos1=txt.indexOf(el,pos1+1)) != -1)  nbpoint++;
    return nbpoint;
  }

  /**
   * Obtenir la classe du reseau A, B, C, D, E
   */
  public char getIpClass() {
   short i = tabIP[0];

   //Un petit decalage vers la droite
   i >>=4;
   if( i == 15 ) return 'E'; //  1111
   i>>=1;
   if( i == 7 ) return 'D'; // 111
   i>>=1;
   if( i == 3 ) return 'C'; // 11
   i>>=1;
   if( i == 1 ) return 'B'; // 1

   return 'A';   // 0

  }

  /** Savoir si le reseau est prive
   * @return vrai si le reseau est prive
   */
  public boolean isPrivate() {
    switch (getIpClass()) {
      case 'A' : return tabIP[0]==10;
      case 'B' : return tabIP[0]==172 && (tabIP[1]>=16 && tabIP[1]<=31);
      case 'C' : return tabIP[0]==192 && tabIP[1]==168;
    }
    return false;
  }

  /** Obtenir une string complete, IP classe, prive, localhost
   * @return du texte, de la forme : %d.%d.%d.%d ( classe %S - private )
   */
  public String toCompleteString() {
    String ip = toString();
    if(ip.equalsIgnoreCase("127.0.0.1") )
      return ip +" ( localhost )";
    else
      return ip + " ( classe "+getIpClass() +" )";
  }



}
