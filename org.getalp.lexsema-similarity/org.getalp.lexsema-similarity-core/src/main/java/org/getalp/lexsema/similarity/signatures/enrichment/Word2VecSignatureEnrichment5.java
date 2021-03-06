package org.getalp.lexsema.similarity.signatures.enrichment;

import org.getalp.lexsema.similarity.signatures.DefaultSemanticSignatureFactory;
import org.getalp.lexsema.similarity.signatures.SemanticSignature;
import org.getalp.lexsema.similarity.signatures.symbols.SemanticSymbol;
import org.getalp.lexsema.util.VectorOperation;
import org.getalp.lexsema.util.word2vec.Word2VecClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Word2VecSignatureEnrichment5 extends SignatureEnrichmentAbstract {

    private final int topN;
    
    public Word2VecSignatureEnrichment5(int topN) {
        this.topN = topN;
    }

    public SemanticSignature enrichSemanticSignature(SemanticSignature semanticSignature, String id) {
        List<double[]> symbolsVectors = new ArrayList<>();
        for (SemanticSymbol semanticSymbol : semanticSignature) {
            double[] symbolVector = Word2VecClient.getWordVector(semanticSymbol.getSymbol());
            if (symbolVector.length > 0) {
                symbolsVectors.add(symbolVector);
            }
        }
        if (symbolsVectors.size() == 0) return semanticSignature;
        double[] sum = VectorOperation.sum(symbolsVectors.toArray(new double[symbolsVectors.size()][]));
        double[] sumNormalized = VectorOperation.normalize(sum);
        String zeword = id.substring(0, id.indexOf('%'));
        Collection<String> nearests = Word2VecClient.getMostSimilarWords(zeword.toLowerCase(), topN, sumNormalized);
        SemanticSignature newSignature = DefaultSemanticSignatureFactory.DEFAULT.createSemanticSignature();
        for (String word : semanticSignature.getStringSymbols()) {
            newSignature.addSymbol(word);
        }
        for (String word : nearests) {
            newSignature.addSymbol(word);
        }
        return newSignature;
    }

    @Override
    public SemanticSignature enrichSemanticSignature(SemanticSignature semanticSignature) {
        return null;
    }

}
