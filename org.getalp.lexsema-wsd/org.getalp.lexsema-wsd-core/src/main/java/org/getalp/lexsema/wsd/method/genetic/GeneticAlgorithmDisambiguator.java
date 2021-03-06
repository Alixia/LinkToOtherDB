package org.getalp.lexsema.wsd.method.genetic;

import org.apache.commons.math3.genetics.*;
import org.getalp.lexsema.similarity.Document;
import org.getalp.lexsema.wsd.configuration.Configuration;
import org.getalp.lexsema.wsd.method.Disambiguator;
import org.getalp.lexsema.wsd.method.StopCondition;
import org.getalp.lexsema.wsd.score.ConfigurationScorer;

import java.io.PrintWriter;

public class GeneticAlgorithmDisambiguator implements Disambiguator
{
    public PrintWriter plotWriter = null;

    private StopCondition stopCondition;
    
    private int population;
    
    private double crossoverRate;
    
    private double mutationRate;

    private ConfigurationScorer scorer;
    
    public static double bestScore = 0;
    
    public GeneticAlgorithmDisambiguator(StopCondition stopCondition, int population, double crossoverRate, double mutationRate, ConfigurationScorer scorer)
    {
        this.stopCondition = stopCondition;
        this.population = population;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
        this.scorer = scorer;
    }
    
    public Configuration disambiguate(Document document)
    {
        stopCondition.reset();
        StoppingCondition stoppingCondition = new StoppingCondition()
        {
            public boolean isSatisfied(Population population)
            {
                stopCondition.incrementIterations();
                stopCondition.updateMilliseconds();
                bestScore = population.getFittestChromosome().getFitness();
                return stopCondition.stop();
            }
        };
        
        Population population = new ElitisticListPopulation(this.population, 0.2);
        for (int i = 0 ; i < this.population ; i++)
        {
            population.addChromosome(new GeneticConfigurationChromosome(document, scorer, stopCondition, plotWriter));
        }
        
        GeneticConfigurationCrossoverPolicy crossoverPolicy = new GeneticConfigurationCrossoverPolicy(stopCondition);
        GeneticConfigurationMutationPolicy mutationPolicy = new GeneticConfigurationMutationPolicy();
        TournamentSelection selectionPolicy = new TournamentSelection(2);
        
        GeneticAlgorithm ga = new GeneticAlgorithm(crossoverPolicy, crossoverRate, mutationPolicy, mutationRate, selectionPolicy);
        
        population = ga.evolve(population, stoppingCondition);
        if (plotWriter != null) plotWriter.flush();
        return ((GeneticConfigurationChromosome) population.getFittestChromosome()).configuration;
    }

    public Configuration disambiguate(Document document, Configuration c)
    {
        return disambiguate(document);
    }

    public void release()
    {
        scorer.release();
    }
}
