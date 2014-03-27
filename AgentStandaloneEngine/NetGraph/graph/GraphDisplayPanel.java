  package NetGraph.graph;
  import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Vector;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import NetGraph.graph.components.discovery.ComponentDiscoveryDetails;
import NetGraph.utils.OverFlowException;
import dimes.util.logging.Loggers;
  
  

/*****************
 * 
 * @author Ohad and the NetGraph team.
 * 
 * a wrapper class for jpanel and GrpahDisplay 
 * 
 *
 */
  public class GraphDisplayPanel extends JPanel implements GraphicContext{
  	
 
  	GraphDisplay displayObject;
  	FontMetrics fontMetrics;
  	
  	private Logger logger;
  	
  	/** Constructeur
  	 * @param ecranprincipal Pointeur vers la fenetre qui utilise le panel
  	 */
  	public GraphDisplayPanel(JFrame aFrame , Image localhostImage , boolean graphHidden) {
  		this.logger = Loggers.getLogger(this.getClass());
  		displayObject = new GraphDisplay(this,localhostImage,graphHidden);
  	}
  	
  	
  	/** Trouver un noeud, ajoute le noeud si pas trouve
  	 * @param IP l'addresse IP du noeud
  	 * @return un entier correspondant a la position dans le tableau
  	 */
  	public int findNode(InetAddress IP,Dimension frameSize) {  		
  		return displayObject.findNode(IP,frameSize);	
  	}

  	
  	public void centerLocalhost(Dimension frameSize)
  	{
  	  displayObject.centerLocalhost(frameSize);  	
  	}
  	
  	/** Change tous les labels de toutes les nodes
  	 * en fonction de nb
  	 * @param nb un chiffre pour indiquer si on affiche plutot
  	 * le nom de la machine, le dns ou l'ip, voir node.java
  	 */
  	public void ChangeLabel(int nb) {
  	    displayObject.ChangeLabel(nb);
  	}
  	
  	
  	/** Ajouter un arc entre deux IP, si une des IP n'existe pas, elle est cree
  	 * @param to IP destination
  	 * @param from IP origine de l'arc
  	 * @param len distance de l'arc entre les deux IP
  	 * @param info info retourne par le traceroute
  	 */
  	public void addEdge(InetAddress from,InetAddress to,int len,
  	        ComponentDiscoveryDetails discoveryDetails)
  	throws OverFlowException {
  	    displayObject.addEdge(from,to,len,discoveryDetails);
  	}
  	
  	
  	/**
  	 * Nettoye le graph .... enleve tout sauf la machine local
  	 */
  	public void Clear() {
  		displayObject.Clear();
  	}
  	
  	/**
  	 * this function clears the graph in a soft way, meaning it
  	 * prunes it until only half the maximum nodes remain.
  	 * 
  	 * note that in some cases the graph is too tightly-connected, and pruning 
  	 * is impossible. when this sittuation occurs a special function
  	 * is called in order to  prune edges that are interfearing with the 
  	 * graph being a DAG.
  	 * The author, beeing a proud hebrew, named this special function "killTheFishes".
  	 */
  	public  void softClear() {
  	  displayObject.softClear();
  	}  	
  	
  	public  void startRelaxingByCountry(){
  	  displayObject.startRelaxingByCountry();
  		}
  	
  	public  void startRelaxingByISP(){
  	  displayObject.startRelaxingByISP();
  	}
  	
  	public  void startRelaxingByDefault(){
  	  displayObject.startRelaxingByDefault();
  	}
  	  	
  	/**
  	 * @return
  	 */
  	public boolean isHidden()
  	{
  		return displayObject.isHidden();
  	}
  	
  	/**
  	 * @param b
  	 */
  	public void setHidden(boolean aHidden)
  	{
  		displayObject.setHidden(aHidden);
  	}
  	
  	
  	/** Utiliser pour imprimer */
  	public void paint(Graphics g)  {
  		displayObject.paint(g);
  	}
  	  	
  	/** Mise a jour de l'affichage
  	 * @param g graphics
  	 */
  	public void update(Graphics g) {
  		displayObject.update(g);
  	}
  	
  	/** Trouver une node sous le curseur de la souris
  	 * @param x coordonnee X de la souris
  	 * @param y coordonnee Y de la souris
  	 */
  	int findNode(int x,int y) {
  		return displayObject.findNode(x,y);
  	}
  	
  	/** Souris appuye
  	 * @param e MouseEvent
  	 */
  	public void this_mousePressed(MouseEvent e) {
  	    displayObject.this_mousePressed(e);

  	}
  	

  	
  	public void makeAllCollapse(){  		
  		displayObject.makeAllCollapse();
  	}
  	

  	
  	public void startCollapse(int nodeNumber){
  		displayObject.startCollapse(nodeNumber);
  	}
  	

  	
  	/** Souris relachee
  	 * @param e MouseEvent
  	 */
  	public void this_mouseReleased(MouseEvent e) {
  	    displayObject.this_mouseReleased(e);
  	}
  	
  	/** Cliquer deplacer
  	 * @param e MouseEvent
  	 */
  	public void this_mouseDragged(MouseEvent e) {
  		displayObject.this_mouseDragged(e);
  	}
  	
  	
  	
  	/** Si la souris est bougee, on affiche le nom dans le barre de statut
  	 * @param e MouseEvent
  	 */
  	void this_mouseMoved(MouseEvent e) {  	    
  		int i = findNode(e.getX(),e.getY());
  		
  		if (i!=-1 && !displayObject.data.nodes[i].isFiltered){
  			this.setToolTipText("<html>" + displayObject.data.nodes[i].IP.getHostAddress()+
  					"<br>"
  					+displayObject.data.nodes[i].machinename+
					// Excluded for now : for debug purposes :
//					"<br>InDegree="+
//					nodes[i].inDegree+
//					"<br>OutDegree="+nodes[i].outDegree+
//					"<br>index=" + i +
					(displayObject.data.nodes[i].isRetreived?"<br>"+displayObject.data.nodes[i].country+
							"<br>"+displayObject.data.nodes[i].ISPName:"")+									
  					"</html>");
  		}
  		else
  			this.setToolTipText(null);
  		this.displayObject.relaxManager.updateMousePosition(e.getX(),e.getY());
  		this.displayObject.paintManager.updateMousePosition(e.getX(),e.getY());
  	}
  	
  	/** Demarrage du graphe */
  	public void start() {
	  	displayObject.start();
  	}
  	
  	/** Arret */
  	public void stop() 
  	{
  		displayObject.stop();
  	}
  	

  	/** Initialisation du panel*/
  	public  void jbInit() {  		
  		this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
  			public void mouseDragged(MouseEvent e) { this_mouseDragged(e); }
  			public void mouseMoved(MouseEvent e) { this_mouseMoved(e); }
  		});
  		this.addMouseListener(new java.awt.event.MouseAdapter() {
  			public void mousePressed(MouseEvent e) { this_mousePressed(e); }
  			public void mouseReleased(MouseEvent e) { this_mouseReleased(e); }
  		});
  		
  		
  		this.createToolTip();
  		this.setToolTipText(null);
  	}
  	
  	
  	/**
  	 * this function performs a permanent one-level prune, meaning,
  	 * it clears the leaves of every trace - the nodes which 
  	 * have inDegree=1 and outDegree=0;
  	 * 
  	 * 
  	 */
  	private void permanentPrune() {
  		displayObject.permanentPrune();
  	}
  	
  	/**
  	 * a function which returns all the nodes that haven't been fetched
  	 * and retreived by the server ( Meaning - the information about ASNumber,
  	 * ISPName, country etc isnt available yet).
  	 * 
  	 * @return a vector containing all unretrieved IPs.
  	 */
  	public Vector getUnretreivedIPs() {
  		return displayObject.getUnretreivedIPs();
  	}
  	
  	/**
  	 * a function which updates the AS-Country-etc infromation
  	 * for a specific IP in the graph.
  	 * 
  	 * @param ip - the IP to be updated.
  	 * @param countryName - the country where the IP is located.
  	 * @param anISPName - the name of the ISP the IP belongs to.
  	 * @param asNumber - the AS number - not neccesary?
  	 */
  	public void updateIPInfo(String ip, String countryName, String anISPName, int asNumber) {
  		displayObject.updateIPInfo(ip,countryName, anISPName, asNumber);
  	}
  	
  	/**
  	 * 
  	 */
  	public  void killTheFishes() {
  		displayObject.killTheFishes();
  	}
  	
  	/**
  	 * @param pruningLevel
  	 */
  	public void prune(int pruningLevel) {
  		displayObject.prune(pruningLevel);
  	}
  	
  	/**********************
  	 * 
  	 * @param aColorDisplay - an indication which color is to be displayed
  	 * meaning ISP_COLOR_DISPLAY etc.
  	 */
  	public synchronized void setColorDisplay(int aColorDisplay){
  		displayObject.setColorDisplay(aColorDisplay);
  	}

	/**
	 * un-collapse the entire graph.
	 */
	public void makeAllExpand() {
		displayObject.makeAllExpand();
	}

	public synchronized BufferedImage getGraphImage(){
	    return displayObject.getGraphImage();
	}
	
	/**
	 * saves the graph into a jpg
	 * TODO : check and add fault toleration.
	 */
	public synchronized void saveGraph() {

		JFileChooser chooser = new JFileChooser();
		chooser.setApproveButtonText("Save");
		chooser.setDialogTitle("Save");
		boolean saveOk = false;
		
		// continue while save is ok :
		while (!saveOk)
		{
		chooser.addChoosableFileFilter(new FileFilter(){

			public boolean accept(File file) {
				return (file.getName().endsWith(".jpg")
						|| file.isDirectory());
			}

			public String getDescription() {
				return ("JPG & GIF Images");
			}
			
		});
		
	    int returnVal = chooser.showOpenDialog(this);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
	        /*System.out.println*/logger.finest("You chose to open this file: " +
	            chooser.getSelectedFile().getAbsolutePath());
	    }
	    else 
	    	return;
	    
	    // check if the file exists :
	    if (chooser.getSelectedFile().exists())
	    {
	        int val = JOptionPane.showConfirmDialog(this,"Are you sure you want to overwrite "+ chooser.getSelectedFile().getName() + " ?",
	                "File already exists", JOptionPane.YES_NO_OPTION);
	        if (val == JOptionPane.YES_OPTION)
	        {
	            saveOk = true;
	        }
	    }
	    else
	        saveOk = true;
		}
	    
	        
	    
	    String fileName =  chooser.getSelectedFile().getAbsolutePath();
	    if (!fileName.endsWith(".jpg"))
	    	fileName = fileName+".jpg";
				
	    BufferedImage img = this.getGraphImage();
	    /*System.out.println*/logger.finest("Saving file ..." + fileName);
		try 
		{
		ImageIO.write(img,"JPEG",new File(fileName));
		img.flush();//check - free image memory
		}
		catch (FileNotFoundException e) {
		    /*System.out.println*/logger.finest(e.toString());
		}
		catch (IOException e) {
		    /*System.out.println*/logger.finest("writeJpegFile: Got an IOException");
		}
		/*System.out.println*/logger.finest("done!");	
	}

	/**
	 * a function used to avoid problems of an invalid xml files
	 * received from the server.
	 * all the nodes are declared "retrieved" and thus wouldn't be
	 * sent again to the server for resolving.
	 * 
	 * @param unretreivedIPs
	 * 
	 */
	public void markAllRetrieved() {
		displayObject.markAllRetrieved();
	}

	
	/**
	 * @param selectedScripts
	 */
	public void setScriptDisplayFilter(String selectedScripts) {
		displayObject.setScriptDisplayFilter(selectedScripts);
	}


    /**
     * @param expString
     */
    public void setExperimentDisplayFilter(String expString) {
        displayObject.setExperimentDisplayFilter(expString);
        
    }
    
    /**
     * @param operationString
     */
    public void setOperationDisplayFilter(String operationString) {
        displayObject.setOperationDisplayFilter(operationString);
        
    }
    
    /**
     * @param protocol
     */
    public void setProtocolDisplayFilter(String protocol) {
        displayObject.setProtocolDisplayFilter(protocol);
        
    }


    /**
     * @param priority
     */
    public void setPriorityDisplayFilter(String priority) {
        displayObject.setPriorityDisplayFilter(priority);    
    }


    /**
     * 
     */
    public void resetDisplayFilter() {
        displayObject.resetDisplayFilter();
    }


    /**
     * @return
     */
    public String export() {
        return displayObject.export();
    }


    /* (non-Javadoc)
     * @see NetGraph.graph.GraphicContext#removeMouseListeners()
     */
    public void removeMouseListeners() {
		MouseListener[] mouseListeners = this.getMouseListeners();
		for (int i=0; i< mouseListeners.length; ++i)
			this.removeMouseListener(mouseListeners[i]);
		MouseMotionListener[] mouseMotionListeners = this.getMouseMotionListeners();
		for (int i=0; i< mouseMotionListeners.length; ++i)
			this.removeMouseMotionListener(mouseMotionListeners[i]);
    }


    /* (non-Javadoc)
     * @see NetGraph.graph.GraphicContext#getFontMetrics()
     */
    public FontMetrics getFontMetrics() {
       
        if (fontMetrics==null)
        {
            Graphics g = this.getGraphics();
            try
            {
                
                g.setFont(new Font("Arial", Font.BOLD, 13));
                fontMetrics = g.getFontMetrics();
                
            }
            finally
            {
                g.dispose();
            }
        }
//        System.out.println("returning "+fontMetrics);
        return fontMetrics;
    }


    /**
     * @param d
     */
    public void setDampingFactor(double d) {
       this.displayObject.relaxManager.setDampingConstane(d);
    }


    /**
     * @param relax_strict_drag
     */
    public void setRelaxManner(int relax_strict_drag) {
       this.displayObject.setRelaxManner(relax_strict_drag);
    }


    /**
     * @param magnifyState
     */
    public void setMagnify(boolean magnifyState) {
        this.displayObject.setMagnify(magnifyState);
    }



	
  }
  
  
  
  
  
  
  
