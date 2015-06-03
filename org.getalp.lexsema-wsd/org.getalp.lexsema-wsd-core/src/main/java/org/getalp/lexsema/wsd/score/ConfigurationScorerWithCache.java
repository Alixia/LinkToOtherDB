package org.getalp.lexsema.wsd.score;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.getalp.lexsema.similarity.Document;
import org.getalp.lexsema.similarity.Sense;
import org.getalp.lexsema.similarity.measures.SimilarityMeasure;
import org.getalp.lexsema.wsd.configuration.Configuration;

public class ConfigurationScorerWithCache implements ConfigurationScorer
{
    private SimilarityMeasure similarityMeasure;
    
    private double[][][][] cache;
    
    private Document currentDocument;
    
    private ExecutorService threadPool;

    public ConfigurationScorerWithCache(SimilarityMeasure similarityMeasure)
    {
        this.similarityMeasure = similarityMeasure;
        cache = null;
        currentDocument = null;
        int nbThreads = Runtime.getRuntime().availableProcessors();
        threadPool = Executors.newFixedThreadPool(nbThreads);
    }

    public double computeScore(Document d, Configuration c)
    {
        if (currentDocument != d)
        {
            cache = new double[d.size()][d.size()][][];
            for (int i = 0 ; i < d.size() ; i++)
            {
                for (int j = i + 1 ; j < d.size() ; j++)
                {
                    cache[i][j] = new double[d.getSenses(i).size()][d.getSenses(j).size()];
                    for (int k = 0 ; k < d.getSenses(i).size() ; k++)
                    {
                        for (int l = 0 ; l < d.getSenses(j).size() ; l++)
                        {
                            cache[i][j][k][l] = -1;
                        }
                    }
                }
            }
            currentDocument = d;
        }
        
        ArrayList<IntermediateScorer> scorers = new ArrayList<IntermediateScorer>();
        for (int i = 0 ; i < c.size() ; i++)
        {
            scorers.add(new IntermediateScorer(i, d, c));
        }

        double totalScore = 0;
        try
        {
            List<Future<Double>> intermediateScores = threadPool.invokeAll(scorers);
            for (Future<Double> intermediateScore : intermediateScores)
            {
                totalScore += intermediateScore.get();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return totalScore;
    }
    
    private class IntermediateScorer implements Callable<Double>
    {
        private int i;
        
        private Document d;
        
        private Configuration c;
        
        public IntermediateScorer(int i, Document d, Configuration c)
        {
            this.i = i;
            this.d = d;
            this.c = c;
        }
        
        public Double call()
        {
            double score = 0;
            int k = c.getAssignment(i);
            for (int j = i + 1 ; j < c.size() ; j++)
            {
                int l = c.getAssignment(j);
                double cacheCell = cache[i][j][k][l];
                if (cacheCell != -1)
                {
                    score += cacheCell;
                }
                else
                {
                    Sense senseA = d.getSenses(i).get(k);
                    Sense senseB = d.getSenses(j).get(l);
                    double similarity = senseA.computeSimilarityWith(similarityMeasure, senseB);
                    score += similarity;
                    cache[i][j][k][l] = similarity;
                }
            }
            return score;
        }  
    }

    public void release()
    {
        threadPool.shutdown();
    }
}