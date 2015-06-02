package org.getalp.lexsema.wsd.experiments.cuckoo.parameters.bat;

import java.util.Random;

import org.getalp.lexsema.wsd.experiments.cuckoo.generic.CuckooSolution;
import org.getalp.lexsema.wsd.experiments.cuckoo.parameters.ScalarParameter;

public class BatParameters implements CuckooSolution
{
    public static final Random random = new Random();
    
    public ScalarParameter batsNumber;
    
    public ScalarParameter minFrequency;
    
    public ScalarParameter maxFrequency;

    public ScalarParameter minLoudness;
    
    public ScalarParameter maxLoudness;

    public ScalarParameter minRate;
    
    public ScalarParameter maxRate;

    public ScalarParameter alpha;
    
    public ScalarParameter gamma;
    
    public BatParameters()
    {
        batsNumber = new ScalarParameter(1, 50);
        minFrequency = new ScalarParameter(0, 100);
        maxFrequency = new ScalarParameter(minFrequency, 100);
        minFrequency.setMaxValueAsScalarParameter(maxFrequency);
        minLoudness = new ScalarParameter(0, 100);
        maxLoudness = new ScalarParameter(minLoudness, 100);
        minLoudness.setMaxValueAsScalarParameter(maxLoudness);
        minRate = new ScalarParameter(0, 1);
        maxRate = new ScalarParameter(minRate, 1);
        minRate.setMaxValueAsScalarParameter(maxRate);
        alpha = new ScalarParameter(0.75, 1);
        gamma = new ScalarParameter(0.75, 1);
    }

    public BatParameters clone()
    {
        BatParameters ret = new BatParameters();
        ret.batsNumber.currentValue = batsNumber.currentValue;
        ret.minFrequency.currentValue = minFrequency.currentValue;
        ret.maxFrequency.currentValue = maxFrequency.currentValue;
        ret.minLoudness.currentValue = minLoudness.currentValue;
        ret.maxLoudness.currentValue = maxLoudness.currentValue;
        ret.minRate.currentValue = minRate.currentValue;
        ret.maxRate.currentValue = maxRate.currentValue;
        ret.alpha.currentValue = alpha.currentValue;
        ret.gamma.currentValue = gamma.currentValue;
        return ret;
    }
    
    public void makeRandomChanges(double distance)
    {
        double[] parameters = new double[9];
        double max = 0;
        for (int i = 0 ; i < 9 ; i++)
        {
            parameters[i] = random.nextGaussian();
            max = Math.max(Math.abs(max), Math.abs(parameters[i]));
        }
        for (int i = 0 ; i < 9 ; i++)
        {
            parameters[i] /= max;
        }
        batsNumber.add(parameters[0] * distance);
        minFrequency.add(parameters[1] * distance);
        maxFrequency.add(parameters[2] * distance);
        minLoudness.add(parameters[3] * distance);
        maxLoudness.add(parameters[4] * distance);
        minRate.add(parameters[5] * distance);
        maxRate.add(parameters[6] * distance);
        alpha.add(parameters[7] * distance);
        gamma.add(parameters[8] * distance);
        /*
        System.out.println("Move batsNumbers : " + parameters[0] * distance * batsNumber.step);
        System.out.println("Move minFrequency : " + parameters[1] * distance * minFrequency.step);
        System.out.println("Move maxFrequency : " + parameters[2] * distance * maxFrequency.step);
        System.out.println("Move minLoudness : " + parameters[3] * distance * minLoudness.step);
        System.out.println("Move maxLoudness : " + parameters[4] * distance * maxLoudness.step);
        System.out.println("Move minRate : " + parameters[5] * distance * minRate.step);
        System.out.println("Move maxRate : " + parameters[6] * distance * maxRate.step);
        System.out.println("Move alpha : " + parameters[7] * distance * alpha.step);
        System.out.println("Move gamma : " + parameters[8] * distance * gamma.step);
        */
    }
    
    public String toString()
    {
        return "bats number = " + (int) batsNumber.currentValue + 
                ", min frequency = " + (int) minFrequency.currentValue + 
                ", max frequency = " + (int) maxFrequency.currentValue +
                ", min loudness = " + (int) minLoudness.currentValue + 
                ", max loudness = " + (int) maxLoudness.currentValue +
                ", min rate = " + minRate.currentValue + 
                ", max rate = " + maxRate.currentValue +
                ", alpha = " + alpha.currentValue +
                ", gamma = " + gamma.currentValue;
    }
}
