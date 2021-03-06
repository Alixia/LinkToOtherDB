package org.getalp.lexsema.wsd.parameters.bat;

import org.getalp.lexsema.io.document.loader.CorpusLoader;
import org.getalp.lexsema.similarity.Document;
import org.getalp.lexsema.wsd.configuration.Configuration;
import org.getalp.lexsema.wsd.method.BatAlgorithmDisambiguator;
import org.getalp.lexsema.wsd.method.Disambiguator;
import org.getalp.lexsema.wsd.method.StopCondition;
import org.getalp.lexsema.wsd.parameters.method.Parameters;
import org.getalp.lexsema.wsd.parameters.method.ParametersScorer;
import org.getalp.lexsema.wsd.score.ConfigurationScorer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BatParametersScorer implements ParametersScorer
{
    private ConfigurationScorer scorer; 
    
    private CorpusLoader dl;
    
    private int iterationsOutside;
    
    private StopCondition stopCondition;
    
    private ExecutorService threadPool;

    public BatParametersScorer(ConfigurationScorer scorer, CorpusLoader dl, int iterationsOutside, StopCondition stopCondition)
    {
        this.scorer = scorer;
        this.dl = dl;
        this.iterationsOutside = iterationsOutside;
        this.stopCondition = stopCondition;
        int nbThreads = Runtime.getRuntime().availableProcessors();
        threadPool = Executors.newFixedThreadPool(nbThreads);
    }
    
    public double[] computeScore(BatParameters params)
    {
        ArrayList<IntermediateScorer> scorers = new ArrayList<IntermediateScorer>();
        for (int i = 0 ; i < iterationsOutside ; i++)
        {
            scorers.add(new IntermediateScorer(params));
        }

        ArrayList<Double> res = new ArrayList<Double>();
        try
        {
            List<Future<Double>> intermediateScores = threadPool.invokeAll(scorers);
            for (Future<Double> intermediateScore : intermediateScores)
            {
                res.add(intermediateScore.get());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return toDoubleArray(res);
    }

    public double[] computeScore(Parameters configuration)
    {
        return computeScore((BatParameters) configuration);
    }
    
    private class IntermediateScorer implements Callable<Double>
    {
        private BatParameters params;
        
        public IntermediateScorer(BatParameters params)
        {
            this.params = params;
        }

        public Double call() throws Exception
        {
            Disambiguator batDisambiguator = new BatAlgorithmDisambiguator(
                    stopCondition, 
                    (int) params.batsNumber.currentValue, 
                    params.minFrequency.currentValue,
                    params.maxFrequency.currentValue,
                    params.minLoudness.currentValue,
                    params.maxLoudness.currentValue + 1,
                    params.alpha.currentValue,
                    params.gamma.currentValue,
                    scorer, false);
            double tmpres = 0;
            int nbTexts = 0;
            for (Document d : dl)
            {
                Configuration c = batDisambiguator.disambiguate(d);
                tmpres += scorer.computeScore(d, c);
                nbTexts++;
            }
            batDisambiguator.release();
            return (tmpres / ((double) nbTexts));
        }
    }
    
    public void finalize()
    {
        threadPool.shutdown();
    }
    
    private static double[] toDoubleArray(ArrayList<Double> list)
    {
        double[] ret = new double[list.size()];
        for (int i = 0 ; i < ret.length ; i++) ret[i] = list.get(i).doubleValue();
        return ret;
    }
}
