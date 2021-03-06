package org.getalp.lexsema.wsd.method.aca.environment.factories;


import org.getalp.lexsema.similarity.Document;
import org.getalp.lexsema.similarity.Sense;
import org.getalp.lexsema.similarity.Word;
import org.getalp.lexsema.wsd.method.aca.environment.Environment;
import org.getalp.lexsema.wsd.method.aca.environment.EnvironmentImpl;
import org.getalp.lexsema.wsd.method.aca.environment.graph.EnvironmentNode;
import org.getalp.lexsema.wsd.method.aca.environment.graph.NestNode;
import org.getalp.lexsema.wsd.method.aca.environment.graph.Node;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.*;

public class DocumentEnvironmentFactory implements EnvironmentFactory {

    private final Document text;
    private final int initialEnergy;
    private final int vectorLength;
    private final int initialPheromone;

    public DocumentEnvironmentFactory(Document text, int initialEnergy, int initialPheromone, int vectorLength) {
        this.text = text;
        this.initialEnergy = initialEnergy;
        this.vectorLength = vectorLength;
        this.initialPheromone = initialPheromone;
    }

    @Override
    public Environment build() {
        List<Node> nodes = new ArrayList<>();
        List<Node> words = new ArrayList<>();
        Map<Integer,List<Node>> wordSenseIndex = new HashMap<>();
        Map<Integer, Node> nestIndex = new HashMap<>();

        //Creating nodes
        createNodes(nodes, nestIndex,words, wordSenseIndex);

        //Creating adjacency matrix
        INDArray adjacency = Nd4j.create(nodes.size(), nodes.size());
        populateAdjacency(adjacency);

        return new EnvironmentImpl(nodes, nestIndex,words,wordSenseIndex, adjacency,initialPheromone);
    }

    /**
     * Creating nodes
     *  @param nodes     The list that will contain the nodes
     * @param nestIndex A map between nest positions and nest node instances
     * @param wordSenseIndex A mapping between wordNodes and their corresponding sense nodes
     */
    private void createNodes(Collection<Node> nodes, Map<Integer, Node> nestIndex, Collection<Node> words, Map<Integer, List<Node>> wordSenseIndex) {
        int currentPosition = 0;
        int currentWordIndex = 0;

        //Creating text root node
        //The text id is used as the node identifier
        nodes.add(new EnvironmentNode(currentPosition, text.getId(), initialEnergy, vectorLength));
        ++currentPosition;
        for (Word w : text) {
            Node word = new EnvironmentNode(currentPosition, w.getId(), initialEnergy, vectorLength);
            nodes.add(word);
            words.add(word);
            List<Node> senses = new ArrayList<>();
            wordSenseIndex.put(currentPosition,senses);
            currentPosition++;
            for (Sense sense : text.getSenses(currentWordIndex)) {
                Node nestNode = new NestNode(currentPosition, sense.getId(), initialEnergy, sense.getSemanticSignature());
                nodes.add(nestNode);
                nestIndex.put(currentPosition, nestNode);
                senses.add(nestNode);
                currentPosition++;
            }
            currentWordIndex++;
        }

    }

    /**
     * Creating nodes
     *
     * @param adjacency The adjacency matrix
     */
    private void populateAdjacency(INDArray adjacency) {
        int currentPosition = 0;
        int currentWordIndex = 0;

        //Text node
        int textPosition = currentPosition;
        ++currentPosition;
        for (Word ignored : text) {
            //Word to sentence links
            adjacency.put(currentPosition, textPosition, initialPheromone);
            //Sentence to words links
            adjacency.put(textPosition, currentPosition, initialPheromone);

            int wordPosition = currentPosition;
            currentPosition++;
            for (Sense sense : text.getSenses(currentWordIndex)) {
                //Sense to word links
                adjacency.put(currentPosition, wordPosition, initialPheromone);
                //Word to sense links
                adjacency.put(wordPosition, currentPosition, initialPheromone);
                currentPosition++;
            }
            currentWordIndex++;
        }
    }


}
