package org.getalp.lexsema.wsd.method.aca.environment.graph;


import cern.jet.random.engine.MersenneTwister;
import org.getalp.lexsema.similarity.signatures.DefaultSemanticSignatureFactory;
import org.getalp.lexsema.similarity.signatures.SemanticSignature;
import org.getalp.lexsema.similarity.signatures.symbols.SemanticSymbol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class EnvironmentNode extends AbstractNode {

    final List<SemanticSymbol> signatureVector;

    public EnvironmentNode(int position, String id, double energy, int signatureSize) {
        super(position, id, energy);
        signatureVector = new ArrayList<>(signatureSize);
        for(int i=0;i<signatureSize;i++){
            signatureVector.add(null);
        }
    }

    @Override
    public synchronized SemanticSignature getSemanticSignature(){
        SemanticSignature semanticSignature = DefaultSemanticSignatureFactory.DEFAULT.createSemanticSignature();
        semanticSignature.addSymbols(signatureVector.stream().filter(symbol -> symbol != null).collect(Collectors.toList()));
        return semanticSignature;
    }

    @Override
    public synchronized void depositSignature(List<SemanticSymbol> semanticSymbols, MersenneTwister mersenneTwister) {
        int length = signatureVector.size();
        if(length>0) {
            Collection<Integer> selectedIndexes = new ArrayList<>();
            for (SemanticSymbol semanticSymbol : semanticSymbols) {
                int draw = drawNext(selectedIndexes, length, mersenneTwister);
                if(draw>=0) {
                    signatureVector.set(draw, semanticSymbol);
                }
            }
        }
    }

    private int drawNext(Collection<Integer> selectedComponents, int signatureSize, MersenneTwister mersenneTwister){
        int draw;
        do {
            if(signatureSize>0) {
                try {
                    draw = Math.abs(mersenneTwister.nextInt()) % signatureSize;
                    if (!selectedComponents.contains(draw) && draw > 0) {
                        selectedComponents.add(draw);
                        //noinspection BreakStatement
                        break;
                    }
                } catch (ArrayIndexOutOfBoundsException ignored){
                }
                draw =0;
            } else {
                draw = 0;
            }
        } while(selectedComponents.contains(draw));
        return draw;
    }


    @Override
    public boolean isNest() {
        return false;
    }



}
