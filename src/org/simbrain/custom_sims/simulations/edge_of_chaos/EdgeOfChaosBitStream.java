package org.simbrain.custom_sims.simulations.edge_of_chaos;

import javax.swing.JTextField;

import org.simbrain.custom_sims.RegisteredSimulation;
import org.simbrain.custom_sims.helper_classes.ControlPanel;
import org.simbrain.custom_sims.helper_classes.NetBuilder;
import org.simbrain.custom_sims.helper_classes.PlotBuilder;
import org.simbrain.network.connections.AllToAll;
import org.simbrain.network.core.Network;
import org.simbrain.network.core.Synapse;
import org.simbrain.network.groups.NeuronGroup;
import org.simbrain.network.groups.SynapseGroup;
import org.simbrain.network.neuron_update_rules.BinaryRule;
import org.simbrain.network.update_actions.ConcurrentBufferedUpdate;
import org.simbrain.workspace.gui.SimbrainDesktop;

/**
 * Demonstration of representational capacities of recurrent networks based on
 * Bertschinger, Nils, and Thomas Natschläger. "Real-time computation at the
 * edge of chaos in recurrent neural networks." Neural computation 16.7 (2004):
 * 1413-1436.
 */
public class EdgeOfChaosBitStream extends RegisteredSimulation {

    // TODO: Add PCA by default

    // Simulation Parameters
    int NUM_NEURONS = 120;
    static int GRID_SPACE = 25;
    // Since mean is 0, lower variance means lower average weight strength
    private static double variance = .5;
    private double u_bar = .5;

    // References
    Network network;
    SynapseGroup sgRes1, sgRes2;
    NeuronGroup res1, res2, bitStream1, bitStream2;

    @Override
    public void run() {

        // Clear workspace
        sim.getWorkspace().clearWorkspace();

        // Build network
        NetBuilder net = sim.addNetwork(229, 10, 450, 450, "Edge of Chaos");
        network = net.getNetwork();
        buildNetwork();

        // Time series of inputs
        PlotBuilder ts = sim.addTimeSeriesPlot(674, 11, 363, 285, "Difference");
        // TODO: Programatically show difference in inputs and activation
        // vectors.

        // Set up control panel
        controlPanel();
    }

    private void controlPanel() {
        ControlPanel panel = ControlPanel.makePanel(sim, "Controller", 5, 10);
        JTextField input_tf = panel.addTextField("Input strength", "" + u_bar);
        JTextField tf_stdev = panel.addTextField("Weight stdev", "" + variance);
        panel.addButton("Update", () -> {

            // Update variance of weight strengths
            double new_variance = Double.parseDouble(tf_stdev.getText());
            for (Synapse synapse : sgRes1.getAllSynapses()) {
                synapse.setStrength(
                        synapse.getStrength() * (new_variance / variance));
            }
            for (Synapse synapse : sgRes2.getAllSynapses()) {
                synapse.setStrength(
                        synapse.getStrength() * (new_variance / variance));
            }
            variance = new_variance;

            // Update strength of bitstream signals
            // TODO: Complain if input strength set to 0.
            double new_ubar = Double.parseDouble(input_tf.getText());
            for (double[] row : bitStream1.getTestData()) {
                if (row[0] != 0) {
                    row[0] = new_ubar;
                }
            }
            for (double[] row : bitStream2.getTestData()) {
                if (row[0] != 0) {
                    row[0] = new_ubar;
                }
            }
        });
    }

    void buildNetwork() {
        network.setTimeStep(0.5);

        // Make reservoirs
        res1 = EdgeOfChaos.createReservoir(network, 10, 10, NUM_NEURONS);
        res1.setLabel("Reservoir 1");
        res2 = EdgeOfChaos.createReservoir(network, (int) res1.getMaxX() + 100,
                10, NUM_NEURONS);
        res2.setLabel("Reservoir 2");

        // Connect reservoirs
        sgRes1 = EdgeOfChaos.connectReservoir(network, res1);
        sgRes2 = EdgeOfChaos.connectReservoir(network, res2);

        // Set up "bit-stream" inputs
        bitStream1 = buildBitStream(res1);
        bitStream1.setLabel("Bit stream 1");
        bitStream2 = buildBitStream(res2);
        bitStream2.setLabel("Bit stream 2");
        AllToAll connector = new AllToAll();
        connector.connectAllToAll(bitStream1.getNeuronList(),
                res1.getNeuronList());
        connector.connectAllToAll(bitStream2.getNeuronList(),
                res2.getNeuronList());

        // Use concurrent buffered update
        network.getUpdateManager().clear();
        network.getUpdateManager().addAction(ConcurrentBufferedUpdate
                .createConcurrentBufferedUpdate(network));
    }

    private NeuronGroup buildBitStream(NeuronGroup reservoir) {
        // Offset in pixels of input nodes to right of reservoir
        int offset = 200;
        NeuronGroup bitStreamInputs = new NeuronGroup(network, 1);
        bitStreamInputs.setLocation(reservoir.getCenterX(),
                reservoir.getMaxY() + offset);
        BinaryRule b = new BinaryRule(0, u_bar, .5);
        bitStreamInputs.setNeuronType(b);
        bitStreamInputs.setClamped(true);
        bitStreamInputs.setTestData(
                new double[][] { { u_bar }, { 0.0 }, { 0.0 }, { 0.0 }, { 0.0 },
                        { u_bar }, { 0.0 }, { u_bar }, { u_bar }, { 0.0 },
                        { u_bar }, { u_bar }, { 0.0 }, { 0.0 }, { u_bar } });
        bitStreamInputs.setInputMode(true);
        network.addGroup(bitStreamInputs);
        return bitStreamInputs;
    }

    public EdgeOfChaosBitStream(SimbrainDesktop desktop) {
        super(desktop);
    }

    public EdgeOfChaosBitStream() {
        super();
    };

    @Override
    public String getName() {
        return "Edge of Chaos 2";
    }

    @Override
    public EdgeOfChaosBitStream instantiate(SimbrainDesktop desktop) {
        return new EdgeOfChaosBitStream(desktop);
    }

}
