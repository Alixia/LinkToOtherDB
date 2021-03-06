package org.getalp.lexsema.similarity;


import org.getalp.lexsema.similarity.annotation.Annotations;
import org.getalp.lexsema.util.Language;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

final class NullWord implements Word{

    @Override
    public void addPrecedingInstance(Word precedingNonInstance) {

    }

    @Override
    public Sentence getEnclosingSentence() {
        return new NullSentence();
    }

    @Override
    public void setEnclosingSentence(Sentence enclosingSentence) {
    }


    @Override
    public String getId() {
        return "";
    }

    @Override
    public String getSurfaceForm() {
        return "";
    }

    @Override
    public String getSenseAnnotation() {
        return "";
    }

    @Override
    public void setSemanticTag(String semanticTag) {

    }

    @Override
    public int getBegin() {
        return 0;
    }

    @Override
    public int getEnd() {
        return 0;
    }

    @Override
    public Iterable<Word> precedingNonInstances() {
        return Collections.emptyList();
    }

    @Override
    public void loadSenses(Collection<Sense> senses) {

    }

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public Iterator<Sense> iterator() {
        final List<Sense> emptyList = Collections.emptyList();
        return emptyList.iterator();
    }

    @Override
    public String getLemma() {
        return "";
    }

    @Override
    public void setLemma(String lemma) {

    }

    @Override
    public String getPartOfSpeech() {
        return "";
    }

    @Override
    public void setPartOfSpeech(String partOfSpeech) {

    }

    @Override
    public Language getLanguage() {
        return Language.UNSUPPORTED;
    }

    @Override
    public Annotation getAnnotation(int index) {
        return Annotations.createNullAnnotation();
    }

    @Override
    public void addAnnotation(Annotation annotation) {

    }

    @Override
    public int annotationCount() {
        return 0;
    }

    @Override
    public Collection<Annotation> annotations() {
        return Collections.emptyList();
    }

    @Override
    public Collection<Annotation> annotations(String annotationType) {
        return Collections.emptyList();
    }
}
