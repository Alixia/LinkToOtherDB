package org.getalp.lexsema.similarity.measures.word2vec;


import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.getalp.lexsema.similarity.measures.SimilarityMeasure;
import org.getalp.lexsema.similarity.signatures.SemanticSignature;
import org.getalp.lexsema.similarity.signatures.symbols.SemanticSymbol;
import org.getalp.lexsema.ml.matrix.Matrices;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class Word2VecGlossCosineSimilarity implements SimilarityMeasure {



    private static final Logger logger = LoggerFactory.getLogger(Word2VecGlossCosineSimilarity.class);
    private final WordVectors wordVectors;
    private final boolean useCentroids;

    public Word2VecGlossCosineSimilarity(WordVectors wordVectors, boolean useCentroids) {
        this.wordVectors = wordVectors;
        this.useCentroids = useCentroids;
    }

    @Override
    public double compute(SemanticSignature sigA, SemanticSignature sigB, Map<String, SemanticSignature> relatedSignaturesA, Map<String, SemanticSignature> relatedSignaturesB) {
        INDArray sigASignatureMatrix = generateSignatureMatrix(sigA);
        INDArray sigBSignatureMatrix = generateSignatureMatrix(sigB);

        double totalSim = 0d;
        if (useCentroids) {
            INDArray averageA = Matrices.getColumnWiseSumVector(sigASignatureMatrix);
            INDArray averageB = Matrices.getColumnWiseSumVector(sigBSignatureMatrix);
            double fin = Transforms.cosineSim(averageA, averageB);
            if (Double.isNaN(fin)) {
                fin = -1;
            }
            totalSim = 1 - Math.acos(fin) / Math.PI;
        } else {
            for (int i = 0; i < sigASignatureMatrix.rows(); i++) {
                INDArray row1 = sigASignatureMatrix.getRow(i);
                deNanVector(row1);
                for (int j = 0; j < sigBSignatureMatrix.rows(); j++) {
                    INDArray row2 = sigBSignatureMatrix.getRow(j);
                    deNanVector(row2);
                    double fin = Transforms.cosineSim(row1, row2);
                    if (Double.isNaN(fin)) {
                        fin = -1;
                    }
                    totalSim += 1 - Math.acos(fin) / Math.PI;
                }
            }
            totalSim /= sigASignatureMatrix.rows() + sigBSignatureMatrix.rows();
        }
        return totalSim;
    }

    private void deNanVector(INDArray toDenan){
        for(int i=0;i<toDenan.columns();i++){
            Double v = toDenan.getDouble(i);
            if(v.isNaN()){
                toDenan.putScalar(i,0d);
            }
        }
    }

    private INDArray generateSignatureMatrix(SemanticSignature semanticSignature){
        INDArray sigASignatureMatrix = null;
        int size= semanticSignature.size();
        int currentRow = 0;
        for(SemanticSymbol symbol : semanticSignature) {
            double[] vec = wordVectors.getWordVector(symbol.getSymbol());
            INDArray vector = wordVectors.getWordVectorMatrix(symbol.getSymbol());
            /*for(int i=0; i<vector.rows();i++){
                logger.info(String.valueOf(vector.getDouble(i)));
            }*/
            if(sigASignatureMatrix ==null){
                sigASignatureMatrix = Nd4j.create(size,vector.columns());
            }
            try {
                sigASignatureMatrix.putRow(currentRow, vector);
            } catch (RuntimeException e){
                if(logger.isDebugEnabled()) {
                    logger.debug(e.getMessage());
                }
            }
            currentRow++;
        }
        return  sigASignatureMatrix;
    }

    @Override
    public double compute(SemanticSignature sigA, SemanticSignature sigB) {
        return compute(sigA,sigB,null,null);
    }

}
