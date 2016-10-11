package org.getalp.lexsema.similarity;


import org.getalp.lexsema.util.Language;

public interface DocumentSimilarityFactory {
    Document createDocument();
    Document createDocument(Language language);
    Document nullDocument();
    Sentence createSentence(String id);
    Sentence nullSentence();
    Text createText(Language language);
    Text nullText();
    Word createWord(String id, String lemma, String surfaceForm, String pos);
    Word createWord(String id, String lemma, String surfaceForm, String pos, int begin, int end);
    Word nullWord();
    Sense createSense(String id);
    Sense nullSense();


}
