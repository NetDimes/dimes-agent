 package NetGraph.graph;
  import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.util.Vector;
import java.util.logging.Logger;

import NetGraph.graph.components.Edge;
import NetGraph.graph.components.Node;
import NetGraph.graph.components.discovery.ComponentDiscoveryDetails;
import NetGraph.graph.util.CollapseManager;
import NetGraph.graph.util.GraphData;
import NetGraph.graph.util.GraphPainter;
import NetGraph.graph.util.GraphRelaxer;
import NetGraph.graph.util.PaintFilterManager;
import NetGraph.graph.util.PruningManager;
import NetGraph.utils.NodeInfo;
import NetGraph.utils.OverFlowException;
import dimes.util.logging.Loggers;
  
  

/*****************
 * 
 * @author Ohad and the NetGraph team.
 * 
 * 
 *
 */
  public class GraphDisplay  implements Runnable {
  	
  	private Logger logger;
  	// utility modules :
  	GraphData data = new GraphData(); 	
  	CollapseManager collapser = new CollapseManager();
  	
  	PruningManager pruner = new PruningManager();
  	GraphRelaxer relaxManager = new GraphRelaxer();
  	PaintFilterManager paintFilter = new PaintFilterManager();
  	
  	// Graphical context:
  	GraphicContext context;
  	 	
  	//  etc.
  	private boolean hidden = false; //signals whether to repaint the graph every time it's relaxed
	private boolean shouldStop = false;//changed by Anat
	private Node pick;
	
  	boolean pickfixed;
  	int activeNode=-1;
  	
  	// relaxing thread :
  	Thread relaxer;

  	// Graphics enviroment :
  	Image offscreen;
  	Dimension offscreensize;
  	Graphics offgraphics;
    public GraphPainter paintManager=null;
  	
  	
  	
  	/** Constructeur
  	 * @param aContext a pointer to a graphical context such as
  	 * a JPanel that implements GraphicContext.
  	 * 
  	 */
  	public GraphDisplay(GraphicContext aContext , Image localhostImage , boolean graphHidden) {
  	    this.logger = Loggers.getLogger(this.getClass());
  	    context = aContext;
  	    this.hidden = graphHidden;
  		paintManager = new GraphPainter(localhostImage);
  		context.jbInit();
  	}
  	
  	
  	/** Trouver un noeud, ajoute le noeud si pas trouve
  	 * @param IP l'addresse IP du noeud
  	 * @return un entier correspondant a la position dans le tableau
  	 */
  	public int findNode(InetAddress IP,Dimension frameSize) {
  		
  		return data.findNode(new NodeInfo(IP,
  		      ComponentDiscoveryDetails.masterFilter() ),frameSize);  		
  	}


  	
  	public void centerLocalhost(Dimension frameSize)
  	{
  		data.centerLocalhost(frameSize);
  		
  	}
  	
  	/** Change tous les labels de toutes les nodes
  	 * en fonction de nb
  	 * @param nb un chiffre pour indiquer si on affiche plutot
  	 * le nom de la machine, le dns ou l'ip, voir node.java
  	 */
  	public synchronized void ChangeLabel(int nb) {
  		paintFilter.ChangeLabel(data,nb);
  	}
  	
  	
  	/** Ajouter un arc entre deux IP, si une des IP n'existe pas, elle est cree
  	 * @param to IP destination
  	 * @param from IP origine de l'arc
  	 * @param len distance de l'arc entre les deux IP
  	 * @param info info retourne par le traceroute
  	 */
  	public synchronized void addEdge(InetAddress from,InetAddress to,int len,
  	        ComponentDiscoveryDetails discoveryDetails)
  	throws OverFlowException {

  		Edge addedEdge = null;
  		OverFlowException overFlow = null;
  		try
		{
  			addedEdge = data.addEdge(from,to,len,discoveryDetails,context.getSize());
		}
  		catch (OverFlowException e){
  			overFlow = e;
  			
  		}
  		if (addedEdge != null){
  		    // adjust the filter :
  		    this.paintFilter.applyFilter(addedEdge);
  		    this.paintFilter.applyFilter(data.nodes[addedEdge.to]);
  		    this.paintFilter.applyFilter(data.nodes[addedEdge.from]);  		    
  		    
  		    // adjust pruning, collapsing and relaxation :
  		    pruner.adjustPruning(data);
  		    collapser.adjustVirtualEdges(data);
  		    collapser.clearCollapseList(data);
  		    collapser.unCollaspeAll(data);
  		    collapser.collapseAll(data);  	
  		    relaxManager.updateRelaxation(data,addedEdge);
  		}
  			
  			
  			if (overFlow != null)
  				throw overFlow;
		
  		
  	}
  	
  	
  	/**
  	 * Nettoye le graph .... enleve tout sauf la machine local
  	 */
  	public synchronized void Clear() {
  		
  		data.clear();
  		data.nodes[0].isCollapsed=false;
  		collapser.clear();
  		
  		
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
  	public synchronized void softClear() {
  		int nodesThatWereCut = 1;
  		
  		// prune until the nodes number is less then half :
  		while ( (data.nnodes>=GraphData.MAX_NODES/2) && (nodesThatWereCut>0)){
  			int prevNNodes = data.nnodes;
  			this.permanentPrune();
  			nodesThatWereCut = prevNNodes-data.nnodes; 
  			/*System.out.println*/logger.finest("nodes that were pruned:"+nodesThatWereCut);
  		}
  		
  		// kill the DAG's in case the previous is insufficiet :
  		nodesThatWereCut = 1;
  		int edgesThatWereCut = 1;
  		while ( (data.nedges>=GraphData.MAX_EDGE/2)  && (data.nnodes>=GraphData.MAX_NODES/2) && 
  				((nodesThatWereCut>0) || (edgesThatWereCut>0))){
  			int prevNNodes = data.nnodes;
  			int prevNEdges = data.nedges;
  			this.killTheFishes();
  			this.permanentPrune();
  			nodesThatWereCut = prevNNodes-data.nnodes;
  			edgesThatWereCut = prevNEdges-data.nedges;
  		}
  		// recalculate everything afterwards :
  		
  		data.adjustInOutDegrees();
  		
 		collapser.clearCollapseList(data);
  		collapser.unCollaspeAll(data);
  		collapser.collapseAll(data);
  	}
  	
  	public void travel(){
  	    this.relaxManager.travel(true);
  	}
  	
  	
  	/** Le thread tourne : calcul la postion des noeuds et les affiche */
  	public void run() {
  		Thread me = Thread.currentThread();
  		while (relaxer == me) 
  		{
  			
  			if (this.shouldStop)
  			{
//  				System.err.println("GraphPanel should stop");//debug
  				break;//changed by Anat
  			}
  			
  			if (!this.isHidden())//don't relax when in tray
  				relax();
  			
  			try {
  				Thread.sleep(100);//was originally 100
  			} catch (InterruptedException e) {
  				break;
  			}
  		}
  		/*System.out.println*/logger.finest("exiting GraphPanel.run");//debug
  	}
  	

  	
  	
  	public synchronized void startRelaxingByCountry(){
  		this.relaxManager.startRelaxingByCountry(data);
  		collapser.adjustVirtualEdges(data);

  	}
  	
  	public synchronized void startRelaxingByISP(){
  		this.relaxManager.startRelaxingByISP(data);
  		collapser.adjustVirtualEdges(data);
  	}
  	
  	public synchronized void startRelaxingByDefault(){
  		this.relaxManager.startRelaxingByDefault(data);
  		collapser.adjustVirtualEdges(data);
  	}
  	
 
  	
  	
  	
  	synchronized void relax() {
  	    
  		relaxManager.relax(data,context.getSize());
 		if (!this.isHidden()) //don't paint when in tray
 		   context.repaint();
  	}
  	
  	/**
  	 * @return
  	 */
  	public boolean isHidden()
  	{
  		return hidden;
  	}
  	
  	/**
  	 * @param b
  	 */
  	public void setHidden(boolean aHidden)
  	{
  		hidden = aHidden;
  	}
  	
  	

  	
  	/** Utiliser pour imprimer */
  	public void paint(Graphics g)  {
  		update(g);
  	}
  	
  
  	
  	AffineTransform at = new AffineTransform();
  	/** Mise a jour de l'affichage
  	 * @param g graphics
  	 */
  	public synchronized void update(Graphics g) {
  	    Graphics2D g2 = (Graphics2D) g;
  		Dimension d = context.getSize();
		
		if ((offscreen == null) || (d.width != offscreensize.width) || (d.height != offscreensize.height)) {
		    // dispose of the older graphics resources :
		    if (offscreen!=null)
		        offscreen.flush();
		    if (offgraphics!=null)
		        offgraphics.dispose();
		    // create a new buffer :
			offscreen = context.createImage(d.width, d.height);
			offscreensize = d;
			offgraphics = offscreen.getGraphics();
			offgraphics.setFont(context.getFont());
		}
		
  		offgraphics.setColor(context.getBackground());
  		offgraphics.fillRect(0, 0, d.width, d.height);
		
		this.paintManager.paintEdges(offgraphics,data);
		this.paintManager.paintVirtualEdges(offgraphics,data);
		this.paintManager.paintNodes(offgraphics,data,pick);
		
//		
//		g2.setTransform(saveXform);
		g2.drawImage(offscreen, 0, 0, null);
  	}
  	
  	/** Trouver une node sous le curseur de la souris
  	 * @param x coordonnee X de la souris
  	 * @param y coordonnee Y de la souris
  	 */
  	int findNode(int x,int y) {
//  		FontMetrics fm = context.getGraphics().getFontMetrics();
		FontMetrics fm = context.getFontMetrics();		
		
  		int h = fm.getHeight() + 4;
  		for (int i = data.nnodes-1 ; i >= 0; i--) {
  			Node n = data.nodes[i];
  			int w=0;
  			if (n.lbl == null)
  				w=16;
  			else
  				w = fm.stringWidth(n.lbl) + 10;
  			if( (x>=(int)n.x-w/2 && x<=(int)n.x +w/2) &&
  					(y>=(int)n.y-h/2 && y<=(int)n.y +h/2) ) {
  				return i;
  			}
  		}
  		return -1;
  	}
  	
  	/** Souris appuye
  	 * @param e MouseEvent
  	 */
  	public void this_mousePressed(MouseEvent e) {
  		
  		// Pas la peine de continuer si il n'y a pas de node
  		if( data.nnodes == 0 ) return;
  		
  		// Si c'est le bouton gauche
  		if( e.getModifiers()==InputEvent.BUTTON1_MASK ) {
  			if(e.getClickCount() == 2) {
  				int i= this.findNode(e.getX(),e.getY());
  				if( i != -1) data.nodes[i].fixed = !data.nodes[i].fixed ;
  			}
  			else  {
  				//addMouseMotionListener(this);
  				int x = e.getX();
  				int y = e.getY();
  				
  				double bestdist = Double.MAX_VALUE;
  				
  				// On cherche la node la plus pres du curseur en partant de
  				// de la derniere (le 31/07/2000)
  				for (int i = data.nnodes-1 ; i >= 0 ; i--) {
  					Node n = data.nodes[i];
  					double dist = (n.x - x) * (n.x - x) + (n.y - y) * (n.y - y);
  					if (dist < bestdist) {
  						pick = n;
  						bestdist = dist;
  					} //fin si
  				} //fin pour
  				
  				//On la fixe, pres a etre deplace
  				pickfixed = pick.fixed;
  				pick.fixed = true;
  				pick.x = x;
  				pick.y = y;
  				context.repaint();
  			}
  		}
  		//Si c'est le bouton droit
  		else if( e.getModifiers()==InputEvent.BUTTON3_MASK )  {
  			//On cherche la node sous le curseur
  			int i= this.findNode(e.getX(),e.getY());
  			
  			
  			if( i != -1) {
  				if (data.nodes[i].isCollapsed || 
  						((data.nodes[i].inDegree == 1)  && (data.nodes[i].outDegree == 1)))
  				  logger.finest("avoiding a collapse of a collapsed/non-collapseable node :" + i);
  				else
  				{  					
  					collapser.addNodeToCollapseList(i);
  					// On affiche le menu deroulant si on a trouve une node
  					collapser.clearCollapseList(data);
  					collapser.unCollaspeAll(data);
  					collapser.collapseAll(data);
  				}
  			}
  			
  		}
  		e.consume();
  	}
  	

  	
  	public synchronized void makeAllCollapse(){
//  	  /*System.out.println*/logger.finest("Collapsing all..");
  		collapser.makeAllCollapse(data);

  	}
  	

  	
  	public synchronized void startCollapse(int nodeNumber){
  		collapser.startCollapse(data,nodeNumber);
  		

  	}
  	

  	
  	/** Souris relachee
  	 * @param e MouseEvent
  	 */
  	public void this_mouseReleased(MouseEvent e) {
  		if( e.getModifiers()==InputEvent.BUTTON1_MASK ) {
  			//removeMouseMotionListener(this);
  			if (pick != null) {
  				pick.x = e.getX();
  				pick.y = e.getY();
  				pick.fixed = pickfixed;
  				pick = null;
  			}
  			context.repaint();
  		}
  		
  		if( e.getModifiers()==InputEvent.BUTTON2_MASK ) {
  			
  		}
  		e.consume();
  		
  	}
  	
  	/** Cliquer deplacer
  	 * @param e MouseEvent
  	 */
  	public void this_mouseDragged(MouseEvent e) {
  		if(pick!= null) {
  			pick.x = e.getX();
  			pick.y = e.getY();
  			context.repaint();
  		}
  		//  e.consume();
  		
  	}
  	
  	
  	
  	/** Si la souris est bougee, on affiche le nom dans le barre de statut
  	 * @param e MouseEvent
  	 */
  	void this_mouseMoved(MouseEvent e) {
  		int i = findNode(e.getX(),e.getY());
  		
  		if (i!=-1 && !data.nodes[i].isFiltered){
  		  context.setToolTipText("<html>" + data.nodes[i].IP.getHostAddress()+
  					"<br>"
  					+data.nodes[i].machinename+
					// Excluded for now : for debug purposes :
//					"<br>InDegree="+
//					nodes[i].inDegree+
//					"<br>OutDegree="+nodes[i].outDegree+
//					"<br>index=" + i +
					(data.nodes[i].isRetreived?"<br>"+data.nodes[i].country+
							"<br>"+data.nodes[i].ISPName:"")+									
  					"</html>");
  		}
  		else
  		  context.setToolTipText(null);
  		
  	}
  	
  	/** Demarrage du graphe */
  	public void start() {
  		relaxer = new Thread(this);
  		relaxer.start();
  	}
  	
  	/** Arret */
  	public void stop() 
  	{
  	  /*System.out.println*/logger.finest("in GraphPanel.stop");//debug
  		shouldStop = true;//changed by Anat
  		relaxer = null;
  		
  		//    changed by Anat
  		context.removeMouseListeners();

  	}
  	

  	/** Initialisation du panel*/
  	private void jbInit() throws Exception {
  	 
  	  
//  		this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
//  			public void mouseDragged(MouseEvent e) { this_mouseDragged(e); }
//  			public void mouseMoved(MouseEvent e) { this_mouseMoved(e); }
//  		});
//  		this.addMouseListener(new java.awt.event.MouseAdapter() {
//  			public void mousePressed(MouseEvent e) { this_mousePressed(e); }
//  			public void mouseReleased(MouseEvent e) { this_mouseReleased(e); }
//  		});
//  		this.createToolTip();
//  		this.setToolTipText(null);
  	}
  	
  	
  	
  	
  	/**
  	 * this function performs a permanent one-level prune, meaning,
  	 * it clears the leaves of every trace - the nodes which 
  	 * have inDegree=1 and outDegree=0;
  	 * 
  	 * 
  	 */
  	synchronized void permanentPrune() {
  		
  		int[] mapNodesToReplacements = pruner.permanentPrune(data);
  		collapser.adjustClearList(mapNodesToReplacements);
 
//  		/*System.out.println*/logger.finest("done...");
  	}
  	
  	/**
  	 * a function which returns all the nodes that haven't been fetched
  	 * and retreived by the server ( Meaning - the information about ASNumber,
  	 * ISPName, country etc isnt available yet).
  	 * 
  	 * @return a vector containing all unretrieved IPs.
  	 */
  	public synchronized Vector getUnretreivedIPs() {
  		return data.getUnretreivedIPs();
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
  	public synchronized void updateIPInfo(String ip, String countryName, String anISPName, int asNumber) {
  		
  		int nodeIndex = data.updateIPInfo(ip,countryName,anISPName,asNumber);
  		if (nodeIndex == -1)
  			return;
  		relaxManager.updateRelaxationByInfo(data,nodeIndex);
 
  		
  	}
  	
  	/**
  	 * 
  	 */
  	public synchronized void killTheFishes() {
  		pruner.unPrune(data);
  		data.killTheFishes();
 
  		pruner.adjustPruning(data);
  		collapser.clearCollapseList(data);
  		collapser.unCollaspeAll(data);
  		collapser.collapseAll(data);
  	}
  	
  	
  	
  	/**
  	 * @param pruningLevel
  	 */
  	public synchronized void prune(int pruningLevel) {
  		pruner.prune(data,pruningLevel);
  		collapser.clearCollapseList(data);
  		collapser.unCollaspeAll(data);
  		collapser.collapseAll(data);
  		}
  	
  	/**********************
  	 * 
  	 * @param aColorDisplay - an indication which color is to be displayed
  	 * meaning ISP_COLOR_DISPLAY etc.
  	 */
  	public synchronized void setColorDisplay(int aColorDisplay){
  		paintManager.setColorDisplay(aColorDisplay);
  	}

	/**
	 * un-collapse the entire graph.
	 */
	public synchronized void makeAllExpand() {
		collapser.makeAllExpand(data);
	}

//	the BufferedImage should be freed by calling method
	public synchronized BufferedImage getGraphImage(){
	    Dimension d=context.getSize();
		BufferedImage img=new BufferedImage(d.width,d.height,BufferedImage.TYPE_INT_RGB);
		Graphics graphics=img.getGraphics();
		this.paint(graphics);
		graphics.dispose();//check - free graphics memory
		return img;
	}
	
	/**
	 * saves the graph into a jpg
	 * TODO : check and add fault toleration.
	 */
	public synchronized void saveGraph() {
	    context.saveGraph();
	    
//		JFileChooser chooser = new JFileChooser();
//		chooser.setApproveButtonText("Save");
//		chooser.setDialogTitle("Save");
//		chooser.addChoosableFileFilter(new FileFilter(){
//
//			public boolean accept(File file) {
//				return (file.getName().endsWith(".jpg")
//						|| file.isDirectory());
//			}
//
//			public String getDescription() {
//				return ("JPG & GIF Images");
//			}
//			
//		});
//		
//		
//	    int returnVal = chooser.showOpenDialog(this);
//	    if(returnVal == JFileChooser.APPROVE_OPTION) {
//	       System.out.println("You chose to open this file: " +
//	            chooser.getSelectedFile().getAbsolutePath());
//	    }
//	    else 
//	    	return;
//	    String fileName =  chooser.getSelectedFile().getAbsolutePath();
//	    if (!fileName.endsWith(".jpg"))
//	    	fileName = fileName+".jpg";
//				
//	    BufferedImage img = this.getGraphImage();
//		System.out.println("Saving file ..." + fileName);
//		try 
//		{
//		ImageIO.write(img,"JPEG",new File(fileName));
//		}
//		catch (FileNotFoundException e) {
//		System.out.println(e);
//		}
//		catch (IOException e) {
//		System.out.println("writeJpegFile: Got an IOException");
//		}
//		System.out.println("done!");
//		
		
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
	public synchronized void markAllRetrieved() {
		data.markAllRetrieved();
	}


//	/**
//	 * @param selectedScripts
//	 */
//	public synchronized void setScriptDisplayFilter(Object[] selectedScripts) {
//		paintFilter.setScriptDisplayFilter(data,selectedScripts);
//	}
	
	/**
	 * @param selectedScripts
	 */
	public synchronized void setScriptDisplayFilter(String selectedScripts) {
		paintFilter.setScriptDisplayFilter(data,selectedScripts);
	}


    /**
     * @param expString
     */
    public synchronized void setExperimentDisplayFilter(String expString) {
        paintFilter.setExperimentDisplayFilter(data,expString);
        
    }
    
    /**
     * @param operationString
     */
    public synchronized void setOperationDisplayFilter(String operationString) {
        paintFilter.setOperationDisplayFilter(data,operationString);
        
    }
    
    /**
     * @param protocol
     */
    public synchronized void setProtocolDisplayFilter(String protocol) {
        paintFilter.setProtocolDisplayFilter(data,protocol);
        
    }


    /**
     * @param priority
     */
    public synchronized void setPriorityDisplayFilter(String priority) {
        paintFilter.setPriorityDisplayFilter(data,priority);
        
    }


    /**
     * 
     */
    public void resetDisplayFilter() {
        paintFilter.resetFilter(data);
        
    }
    
    /**
     * @return
     */
    public synchronized String export() {
        return this.data.export();
    }


    /**
     * @param relax_strict_drag
     */
    public void setRelaxManner(int relax_strict_drag) {
        this.relaxManager.setRelaxManner(relax_strict_drag);
    }


    /**
     * @param magnifyState
     */
    public void setMagnify(boolean magnifyState) {
        this.paintManager.setMagnify(magnifyState);
    }


	
  }
  
  
  
  
  
  
  
