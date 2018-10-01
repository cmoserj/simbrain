/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2005,2007 The Authors.  See http://www.simbrain.net/credits
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
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
package org.simbrain.network.gui.dialogs.group;

import org.simbrain.network.core.Neuron;
import org.simbrain.network.groups.Group;
import org.simbrain.network.groups.NeuronGroup;
import org.simbrain.network.gui.NetworkPanel;
import org.simbrain.network.gui.dialogs.TestInputPanel;
import org.simbrain.network.gui.dialogs.network.CompetitivePropertiesPanel;
import org.simbrain.network.gui.dialogs.network.SOMPropertiesPanel;
import org.simbrain.network.gui.dialogs.network.WTAPropertiesPanel;
import org.simbrain.network.subnetworks.CompetitiveGroup;
import org.simbrain.network.subnetworks.SOMGroup;
import org.simbrain.network.subnetworks.WinnerTakeAll;
import org.simbrain.util.StandardDialog;
import org.simbrain.util.math.NumericMatrix;
import org.simbrain.util.propertyeditor2.AnnotatedPropertyEditor;
import org.simbrain.util.widgets.ApplyPanel;
import org.simbrain.util.widgets.EditablePanel;
import org.simbrain.util.widgets.ShowHelpAction;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Main tabbed panel for editing neuron groups. Creation handled via
 * {@link NeuronGroupCreationDialog}.
 *
 * @author Jeff Yoshimi
 */
public class NeuronGroupPanel extends GroupPropertiesPanel {

    // TODO: Implement tab resizing as in synapsegroupdialogs
    // TODO: Get apply panels properly set up
    // TODO: Evaluate spiking group stuff at bottom

    /**
     * Parent network panel.
     */
    private NetworkPanel networkPanel;

    /**
     * The parent window for easy resizing.
     */
    private final Window parent;

    /**
     * Neuron group summary.
     */
    private ApplyPanel summaryPanel;

    /**
     * Neuron Group.
     */
    private NeuronGroup neuronGroup;

    /**
     * Layout panel.
     */
    private AnnotatedPropertyEditor layoutPanel;

    /**
     * Panel for editing test input data for this group.
     */
    private TestInputPanel inputDataPanel;

    /**
     * Panel for specific group types. Null for bare neuron group.
     */
    private EditablePanel specificNeuronGroupPanel;


    /**
     * Creates a neuron group panel meant for editing an existing neuron group.
     *
     * @param np     the parent network of the neuron group
     * @param ng     the neuron group the created panel will edit/display
     * @param parent the parent window for easy resizing
     * @return a neuron group panel to edit the given neuron group
     */
    public static NeuronGroupPanel createNeuronGroupPanel(final NetworkPanel np, final NeuronGroup ng, final Window parent) {
        NeuronGroupPanel ngp = new NeuronGroupPanel(np, ng, parent);
        ngp.initializeLayout();
        return ngp;
    }

    /**
     * Constructor for case where an existing neuron group is being edited.
     *
     * @param np     Parent network panel
     * @param ng     neuron group being edited
     * @param parent parent dialog containing this panel.
     */
    private NeuronGroupPanel(final NetworkPanel np, final NeuronGroup ng, final Window parent) {
        networkPanel = np;
        neuronGroup = ng;
        this.parent = parent;
    }

    /**
     * Initialize the panel's layout.
     */
    private void initializeLayout() {

        if (parent instanceof StandardDialog) {
            ((StandardDialog) parent).setTitle("Edit " + neuronGroup.getLabel());
        }

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(500, 400));
        this.add(tabbedPane);

        // Summary Panel
        summaryPanel = ApplyPanel.createApplyPanel(new SummaryPanel(neuronGroup, false));
        tabbedPane.addTab(SummaryPanel.DEFAULT_PANEL_NAME, (JPanel) summaryPanel);

        // Specific neuron group properties
        // TODO: Test when specific groups implemented
        specificNeuronGroupPanel = getSpecificGroupPanel();
        ApplyPanel sngp = null;
        if (specificNeuronGroupPanel != null) {
            sngp = ApplyPanel.createApplyPanel(specificNeuronGroupPanel);
        }
        if (sngp != null) {
            tabbedPane.addTab("Properties", (JPanel) specificNeuronGroupPanel);
        }

        // Layout
        layoutPanel = new AnnotatedPropertyEditor(neuronGroup.getLayout());
        ApplyPanel lp = ApplyPanel.createApplyPanel(layoutPanel);
        tabbedPane.addTab("Layout", lp);
        // TODO: Select layout type
        // TODO: Apply.


        // Input panel
        NumericMatrix matrix = new NumericMatrix() {

            @Override
            public void setData(double[][] data) {
                neuronGroup.setTestData(data);
            }

            @Override
            public double[][] getData() {
                return neuronGroup.getTestData();
            }
        };
        inputDataPanel = TestInputPanel.createTestInputPanel(networkPanel, neuronGroup.getNeuronList(), matrix);
        tabbedPane.addTab("Input Data", inputDataPanel);

        // Set up Help
        if (parent instanceof StandardDialog) {
            Action helpAction;
            if (specificNeuronGroupPanel != null) {
                helpAction = new ShowHelpAction(((GroupPropertiesPanel) specificNeuronGroupPanel).getHelpPath());
            } else {
                helpAction = new ShowHelpAction(this.getHelpPath());
            }
            ((StandardDialog) parent).addButton(new JButton(helpAction));
        }
    }

    /**
     * Gets the specificNeuronGroupPanel based on the underlying group.
     *
     * @return the committable neuron group panel corresponding to the specific
     * type of the neuron group being edited if it has one.
     */
    private EditablePanel getSpecificGroupPanel() {
        if (neuronGroup instanceof CompetitiveGroup) {
            return CompetitivePropertiesPanel.createCompetitivePropertiesPanel(networkPanel, (CompetitiveGroup) neuronGroup);
        } else if (neuronGroup instanceof WinnerTakeAll) {
            return new WTAPropertiesPanel(networkPanel, (WinnerTakeAll) neuronGroup);
        } else if (neuronGroup instanceof SOMGroup) {
            return new SOMPropertiesPanel(networkPanel, (SOMGroup) neuronGroup);
        } else {
            return null;
        }
    }

    @Override
    public void fillFieldValues() {
    }

    @Override
    public boolean commitChanges() {
        // Everything handled via apply panels for panel editing
//        layoutPanel.commitChanges();
//        neuronGroup.applyLayout();
//        networkPanel.repaint();
//        networkPanel.getNetwork().fireGroupUpdated(neuronGroup);
        return true;
    }

    @Override
    public String getHelpPath() {
        return "Pages/Network/groups.html";
    }

    @Override
    public Group getGroup() {
        return neuronGroup;
    }

    //        combinedNeuronInfoPanel = new AnnotatedPropertyEditor(neuronGroup.getNeuronList());
    // TODO: Re-implement?  If all nodes spiking, making it a spiking neuron group
//        ((ApplyPanel) combinedNeuronInfoPanel).addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                SwingUtilities.invokeLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        boolean spiking = true;
//                        for (Neuron n : neuronGroup.getNeuronList()) {
//                            if (!n.getUpdateRule().isSpikingNeuron()) {
//                                spiking = false;
//                                break;
//                            }
//                        }
//                        neuronGroup.setSpikingNeuronGroup(spiking);
//                    }
//                });
//            }
//        });

}