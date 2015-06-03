package org.getalp.lexsema.similarity.measures.crosslingual;

import org.getalp.lexsema.similarity.measures.SimilarityMeasure;
import org.getalp.lexsema.similarity.signatures.IndexedSemanticSignature;
import org.getalp.lexsema.similarity.signatures.StringSemanticSignature;
import org.getalp.lexsema.similarity.signatures.StringSemanticSignatureImpl;
import org.getalp.lexsema.similarity.signatures.enrichment.SignatureEnrichment;
import org.getalp.lexsema.translation.Translator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;

public class TranslatorCrossLingualSimilarity implements SimilarityMeasure {

    private static Logger logger = LoggerFactory.getLogger(TranslatorCrossLingualSimilarity.class);

    private final SimilarityMeasure similarityMeasure;
    private Translator translator;
    private SignatureEnrichment enrichment;

    public TranslatorCrossLingualSimilarity(final SimilarityMeasure similarityMeasure, final Translator translator, final SignatureEnrichment enrichment) {
        this.similarityMeasure = similarityMeasure;
        this.translator = translator;
        this.enrichment = enrichment;
    }

    public TranslatorCrossLingualSimilarity(final SimilarityMeasure similarityMeasure, final Translator translator) {
        this(similarityMeasure, translator, null);
    }

    @Override
    public String toString() {
        return "Translation based cross-lingual similarity with " + similarityMeasure.toString() + " and " + translator.toString();
    }

    @Override
    public double compute(StringSemanticSignature sigA, StringSemanticSignature sigB, Map<String, StringSemanticSignature> relatedSignaturesA, Map<String, StringSemanticSignature> relatedSignaturesB) {
        if(!sigA.getLanguage().equals(sigB.getLanguage())) {
            String definitionB = sigB.toString();
            String translatedDefinitionB = translator.translate(definitionB, sigB.getLanguage(), sigA.getLanguage());

            StringSemanticSignature translatedSignature = new StringSemanticSignatureImpl();
            translatedSignature.addSymbolString(Arrays.asList(translatedDefinitionB.split(" ")));

            StringSemanticSignature enrichedA = sigA;
            StringSemanticSignature enrichedTranslated = translatedSignature;

            if (enrichment != null) {
                enrichedA = enrichment.enrichSemanticSignature(sigA, sigA.getLanguage());
                enrichedTranslated = enrichment.enrichSemanticSignature(translatedSignature, sigA.getLanguage());
            }

            logger.info(String.format("%s || %s", sigA.toString(), translatedDefinitionB));
            return similarityMeasure.compute(enrichedA, enrichedTranslated, null, null);
        } else {
            return similarityMeasure.compute(sigA,sigB, null, null);
        }
    }

    @Override
    public double compute(IndexedSemanticSignature sigA, IndexedSemanticSignature sigB, Map<String, IndexedSemanticSignature> relatedSignaturesA, Map<String, IndexedSemanticSignature> relatedSignaturesB) {
        return 0;
    }

    @Override
    public double compute(StringSemanticSignature sigA, StringSemanticSignature sigB) {
        return compute(sigA,sigB,null,null);
    }
    @Override
    public double compute(IndexedSemanticSignature sigA, IndexedSemanticSignature sigB) {
        return compute(sigA,sigB,null, null);
    }
}