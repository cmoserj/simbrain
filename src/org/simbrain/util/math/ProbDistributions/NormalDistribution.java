package org.simbrain.util.math.ProbDistributions;

import java.util.concurrent.ThreadLocalRandom;

import org.simbrain.util.UserParameter;

import org.simbrain.util.math.ProbabilityDistribution;
import umontreal.iro.lecuyer.probdist.Distribution;
import umontreal.iro.lecuyer.probdist.NormalDist;

public class NormalDistribution extends ProbabilityDistribution {

    @UserParameter(
            label = "Mean (\u03BC)",
            description = "The expected value of the distribution.",
            defaultValue = "1.0", order = 1)
    private double mean = 1.0;


    @UserParameter(
            label = "Std. Dev. (\u03C3)",
            description = "The average squared distance from the mean.",
            defaultValue = "0.5", order = 2)
    private double standardDeviation = 0.5;


    /**
     * For all but uniform, upper bound is only used in conjunction with
     * clipping, to truncate the distribution. So if clipping is false this
     * value is not used.
     */
    @UserParameter(
            label = "Floor",
            description = "An artificial minimum value set by the user.",
            defaultValue = "" + Double.NEGATIVE_INFINITY, order = 3)
    private double floor = Double.NEGATIVE_INFINITY;

    /**
     * For all but uniform, lower bound is only used in conjunction with
     * clipping, to truncate the distribution. So if clipping is false this
     * value is not used.
     */
    @UserParameter(
            label = "Ceiling",
            description = "An artificial minimum value set by the user.",
            defaultValue = "" + Double.POSITIVE_INFINITY, order = 4)
    private double ceil = Double.POSITIVE_INFINITY;

    @UserParameter(
            label = "Clipping",
            description = "When clipping is enabled, the randomizer will reject outside the floor and ceiling values.",
            defaultValue = "false", order = 5)
    private boolean clipping = false;

    public double nextRand() {
        return clipping(
                (ThreadLocalRandom.current().nextGaussian() * standardDeviation) + mean,
                floor,
                ceil
                );
    }

    public int nextRandInt() {
        return (int) nextRand();
    }

    public Distribution getBestFit(double[] observations, int numObs) {
        return NormalDist.getInstanceFromMLE(observations, numObs);
    }

    public double[] getBestFitParams(double[] observations, int numObs) {
        return NormalDist.getMLE(observations, numObs);
    }

    public String getName() {
        return "Normal";
    }

    @Override
    public String toString() {
        return "Normal";
    }

    @Override
    public NormalDistribution deepCopy() {
        NormalDistribution cpy = new NormalDistribution();
        cpy.mean = this.mean;
        cpy.standardDeviation = this.standardDeviation;
        cpy.ceil = this.ceil;
        cpy.floor = this.floor;
        cpy.clipping = this.clipping;
        return cpy;
    }
}
