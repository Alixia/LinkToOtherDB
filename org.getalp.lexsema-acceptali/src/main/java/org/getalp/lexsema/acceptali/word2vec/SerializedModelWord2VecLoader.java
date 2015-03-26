package org.getalp.lexsema.acceptali.word2vec;


import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.models.word2vec.wordstore.VocabCache;
import org.deeplearning4j.util.SerializationUtils;

import java.io.File;

public class SerializedModelWord2VecLoader implements Word2VecLoader {

    private Word2Vec word2Vec;
    private VocabCache vocabCache;

    @Override
    public Word2Vec getWord2Vec() {
        return word2Vec;
    }

    @Override
    public VocabCache getCache() {
        return vocabCache;
    }

    @Override
    public void load(File directory) {
        File vecPath = new File(directory, "model.ser");
        File cachePath = new File(directory, "cache.ser");
        word2Vec = SerializationUtils.readObject(vecPath);
        vocabCache = SerializationUtils.readObject(cachePath);
        word2Vec.setVocab(vocabCache);
    }
}