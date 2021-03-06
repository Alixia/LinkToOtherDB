package org.getalp.lexsema.supervised.features.extractors;

import org.getalp.lexsema.similarity.Document;

import java.util.List;


public interface LocalTextFeatureExtractor {
    public List<String> getFeatures(Document d, int currentIndex);
}
