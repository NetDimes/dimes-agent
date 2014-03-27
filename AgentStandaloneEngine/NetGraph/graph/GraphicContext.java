/*
 * Created on 24/03/2005
 *
 */
package NetGraph.graph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;

/**
 * @author Ohad Serfaty
 *
 */
public interface GraphicContext {

    /**
     * 
     */
    void removeMouseListeners();

    /**
     * @return
     */
    Dimension getSize();

    /**
     * 
     */
    void repaint();

    /**
     * @param width
     * @param height
     * @return
     */
    Image createImage(int width, int height);

    /**
     * @return
     */
    Font getFont();

    /**
     * @return
     */
    Color getBackground();

    /**
     * @return
     */
    FontMetrics getFontMetrics();

    /**
     * @param string
     */
    void setToolTipText(String string);

    /**
     * 
     */
    void jbInit();

    /**
     * 
     */
    void saveGraph();

}
