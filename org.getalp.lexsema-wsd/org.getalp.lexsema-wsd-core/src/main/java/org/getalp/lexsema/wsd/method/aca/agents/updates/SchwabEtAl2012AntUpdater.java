package org.getalp.lexsema.wsd.method.aca.agents.updates;

import cern.jet.random.engine.MersenneTwister;
import org.getalp.lexsema.similarity.measures.SimilarityMeasure;
import org.getalp.lexsema.similarity.signatures.SemanticSignature;
import org.getalp.lexsema.similarity.signatures.symbols.SemanticSymbol;
import org.getalp.lexsema.wsd.method.aca.agents.Ant;
import org.getalp.lexsema.wsd.method.aca.environment.Environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SchwabEtAl2012AntUpdater implements AntUpdater {

    private final MersenneTwister mersenneTwister;
    private final SimilarityMeasure similarityMeasure;

    private final double depositPheromone;
    private final double takeEnergy;
    private final double depositedComponentsRatio;

    public SchwabEtAl2012AntUpdater(MersenneTwister mersenneTwister, SimilarityMeasure similarityMeasure, double depositPheromone, double takeEnergy, double depositedComponentsRatio) {
        this.mersenneTwister = mersenneTwister;
        this.similarityMeasure = similarityMeasure;
        this.depositPheromone = depositPheromone;
        this.takeEnergy = takeEnergy;
        this.depositedComponentsRatio = depositedComponentsRatio;
    }

    @SuppressWarnings("FeatureEnvy")
    @Override
    public void update(Ant ant, Environment environment) {
        int position = ant.getPosition();
        int selectedTarget;

        if (position == ant.getHome() && !ant.isNewborn()) {
            double currentEnergy = environment.getEnergy(position);
            environment.setEnergy(position,currentEnergy + ant.getEnergyCarried());
        }
        if (environment.isNest(position) && position != ant.getHome()) {
            selectedTarget = ant.getHome();
            if(!environment.isBridge(position,ant.getHome())) {
                createBridge(ant, position, environment);
            }
        } else {
            List<Integer> neighbours = environment.getOutgoingNodes(position);
            int neighbourhoodSize = neighbours.size();

            List<Double> nodeScores;
            List<Double> edgeScores;

            if (ant.isReturning()) {
                nodeScores = returnNodeScore(ant, neighbours, environment);
                edgeScores = returnEdgeScore(position, neighbours, environment);
            } else {
                nodeScores = roamingNodeScores(neighbours, environment);
                edgeScores = roamingEdgeScores(position, neighbours, environment);
            }

            boolean nestInNeighbourhood = isThereANestInTheNeighborhood(neighbours, environment, ant);

            List<Double> scores = new ArrayList<>();
            if (nestInNeighbourhood) {
                //If there is a friendNestNode in the neighbourhood, we ignore pheromone in the selection of the destination
                //Evalf(N) only
                scores.addAll(nodeScores);
            } else {
                //Evalf(N) + Evalf(A)
                //noinspection resource
                IntStream.range(0, neighbourhoodSize).forEach(i -> scores.add(nodeScores.get(i) + edgeScores.get(i)));
            }

            //Σ Evalf(N,A)
            double totalScore = getSum(scores);

            //P(Ni,Aj) = Evalf(Ni,Aj)/ΣEvalf(Nk,Al) //Page 9 in the paper
            List<Double> probabilities = scores.stream().map(score -> score / totalScore).collect(Collectors.toList());

            int targetIndex = selectDestination(probabilities);
            selectedTarget = neighbours.get(targetIndex);
        }

        moveAnt(ant, selectedTarget);
        depositPheromone(environment, position, selectedTarget);
        takeEnergy(ant, environment, selectedTarget);

        if (!environment.isNest(selectedTarget)) {
            depositSignatureComponents(ant.getSemanticSignature(), environment, selectedTarget);
        }
        ant.decrementLives();
        decideWhetherToReturn(ant);

    }

    private boolean isThereANestInTheNeighborhood(Iterable<Integer> neighbours, Environment environment, Ant ant) {
        for (int neighbour : neighbours) {
            if (environment.isFriendNest(neighbour, ant.getHome())) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("all")
    private void createBridge(Ant ant, int position, Environment environment) {
        environment.createBridge(position, ant.getHome());
    }

    private void depositSignatureComponents(SemanticSignature semanticSignature, Environment environment, int target) {
        List<SemanticSymbol> symbols = new ArrayList<>();
        Collection<Integer> selectedComponents = new ArrayList<>();
        int max = (int) (semanticSignature.size() * depositedComponentsRatio);
        List<SemanticSymbol> signatureSymbols = semanticSignature.getSymbols();
        for (int i = 0; i < max; i++) {
            int draw = drawNext(selectedComponents, signatureSymbols.size());
            if(draw>0) {
                symbols.add(signatureSymbols.get(draw));
            }
        }
        environment.depositSignature(symbols, target, mersenneTwister);
    }

    private int drawNext(Collection<Integer> selectedComponents, int signatureSize) {
        int draw;
        do {
            try {
                draw = Math.abs(mersenneTwister.nextInt()) % signatureSize - 1;
                if (!selectedComponents.contains(draw) && draw > 0) {
                    selectedComponents.add(draw);
                    //noinspection BreakStatement
                    break;
                }
            } catch (ArrayIndexOutOfBoundsException ignored){
            }
            draw = 0;
        } while (selectedComponents.contains(draw));
        return draw;
    }

    private void moveAnt(Ant ant, int targetPosition) {
        ant.moveTo(targetPosition);
    }

    private void depositPheromone(Environment environment, int position, int target) {
        double currentPheromone = environment.getPheromone(position, target);
        environment.setPheromone(position, target, currentPheromone + depositPheromone);
    }

    private void takeEnergy(Ant ant, Environment environment, int targetPosition) {
        double targetEnergy = environment.getEnergy(targetPosition);
        double energyTaken = ant.takeEnergy(takeEnergy, targetEnergy);
        environment.setEnergy(targetPosition, targetEnergy - energyTaken);
    }

    private int selectDestination(List<Double> probabilities) {
        double rand = mersenneTwister.raw();
        Double cumulativeDistribution = 0d;
        int i;
        Collections.sort(probabilities, (o1, o2) -> {
            int compare = o1.compareTo(o2);
            if(compare==1){
                return -1;
            } else if(compare==-1){
                return 1;
            } else {
                return 0;
            }
        });
        for (i = 0; i < probabilities.size() && cumulativeDistribution < rand; i++) {
            cumulativeDistribution += probabilities.get(i);
        }
        return i - 1;
    }

    private double getSum(Collection<Double> energies) {
        return energies.stream().mapToDouble(e -> e).sum();
    }

    @SuppressWarnings("FeatureEnvy")
    private void decideWhetherToReturn(Ant ant){
        double maxEnergy = ant.getMaximumEnergy();
        double energy = ant.getEnergyCarried();
        double rand = mersenneTwister.raw();
        if(rand> energy/maxEnergy){
            ant.initiateReturn();
        }
    }

    private List<Double> roamingNodeScores(Collection<Integer> neighbours, Environment environment) {
        List<Double> energy = getNeighboursEnergy(neighbours, environment);
        double sum = getSum(energy);
        return energy
                .stream()
                .map(e -> e / sum)
                .collect(Collectors.toList());
    }

    private List<Double> getNeighboursEnergy(Collection<Integer> neighbours, final Environment environment) {
        return neighbours.stream().map(environment::getEnergy).collect(Collectors.<Double>toList());
    }

    private List<Double> roamingEdgeScores(int position, Collection<Integer> targets, Environment environment) {
        return targets.stream()
                .map(target -> 1d - environment.getPheromone(position, target))
                .collect(Collectors.toList());
    }

    private List<Double> returnNodeScore(Ant ant, Collection<Integer> neighbours, Environment environment) {
        return neighbours.stream()
                .map(position -> similarityMeasure.compute(environment.getNodeSignature(position), ant.getSemanticSignature()))
                .collect(Collectors.<Double>toList());
    }

    private List<Double> returnEdgeScore(int position, Collection<Integer> targets, Environment environment) {
        return targets
                .stream()
                .map(target -> environment.getPheromone(position, target))
                .collect(Collectors.toList());
    }
}
