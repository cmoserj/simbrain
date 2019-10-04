package org.simbrain.network.DL4JSandbox;

import org.piccolo2d.PNode;
import org.piccolo2d.nodes.PPath;
import org.simbrain.network.gui.NetworkPanel;
import org.simbrain.network.gui.nodes.ScreenElement;

import javax.swing.*;

public class DL4JMultiLayerNetworkNode extends ScreenElement {

    private DL4JMultiLayerNetwork net;

    private NetworkPanel networkPanel;

    /**
     * Width in pixels of the main display box for ND4J arrays.
     */
    private final float boxWidth = 150;

    /**
     * Height in pixels of the main display box for ND4J arrays.
     */
    private final float boxHeight = 50;

    private PPath box;


    public DL4JMultiLayerNetworkNode(NetworkPanel networkPanel, DL4JMultiLayerNetwork dl4jNet) {
        super(networkPanel);
        this.net = dl4jNet;
        box = PPath.createRectangle(net.getLocation().getX(), net.getLocation().getY(), boxWidth, boxHeight);
        addChild(box);
        setPickable(true);
        this.setBounds(box.getFullBounds());
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public boolean showSelectionHandle() {
        return true;
    }

    @Override
    public boolean isDraggable() {
        return true;
    }

    @Override
    protected boolean hasToolTipText() {
        return false;
    }

    @Override
    protected String getToolTipText() {
        return null;
    }

    @Override
    protected boolean hasContextMenu() {
        return false;
    }

    @Override
    protected JPopupMenu getContextMenu() {
        return null;
    }

    @Override
    protected boolean hasPropertyDialog() {
        return false;
    }

    @Override
    protected JDialog getPropertyDialog() {
        return null;
    }

    @Override
    public void resetColors() {

    }

    public DL4JMultiLayerNetwork getNet() {
        return net;
    }
}
