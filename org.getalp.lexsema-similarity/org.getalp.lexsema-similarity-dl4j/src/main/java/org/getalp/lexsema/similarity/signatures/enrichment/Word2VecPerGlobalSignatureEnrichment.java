package org.getalp.lexsema.similarity.signatures.enrichment;

import org.deeplearning4j.berkeley.Counter;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.getalp.lexsema.similarity.signatures.DefaultSemanticSignatureFactory;
import org.getalp.lexsema.similarity.signatures.SemanticSignature;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.ops.transforms.Transforms;

public class Word2VecPerGlobalSignatureEnrichment extends SignatureEnrichmentAbstract {

    public static final int DEFAULT_TOP_N = 10;

    private final Word2Vec word2Vec;
    private final VocabCache vocab;
    private final int topN;


    public Word2VecPerGlobalSignatureEnrichment(Word2Vec word2Vec, VocabCache vocab) {
        this(word2Vec, vocab, DEFAULT_TOP_N);
    }

    public Word2VecPerGlobalSignatureEnrichment(Word2Vec word2Vec, VocabCache vocab, int topN) {
        this.word2Vec = word2Vec;
        this.vocab = vocab;
        this.topN = topN;
    }


    private VocabCache vocab() {
        return vocab;
    }

    @Override
    public SemanticSignature enrichSemanticSignature(SemanticSignature semanticSignature) {
        SemanticSignature newSignature = DefaultSemanticSignatureFactory.DEFAULT.createSemanticSignature();
        INDArray definitionVector = word2Vec.getVectorizer().transform(semanticSignature.toString());
        Counter<String> distances = new Counter<>();
        for (String s : vocab().words()) {
            INDArray otherVec = word2Vec.getWordVectorMatrix(s);
            double sim = Transforms.cosineSim(definitionVector, otherVec);
            distances.incrementCount(s, sim);
        }
        distances.keepTopNKeys(topN);
        for (String s : distances.keySet()) {
            newSignature.addSymbol(s, distances.getCount(s));
        }
        return newSignature;
    }

}
