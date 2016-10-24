package org.getalp.lexsema.similarity;

class SentenceImpl extends DocumentImpl implements Sentence {

    private Text parentText;

    SentenceImpl(String id) {
        super();
        setId(id);
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (Word le : this) {
            final String lemma = le.getLemma();
            output.append(lemma.trim());
            output.append(" ");
        }
        final String s = output.toString();
        return s.trim();
    }

    @Override
    public boolean isNull() {
        super.isNull();
        return false;
    }

    @Override
    public Text getParentText() {
        return parentText;
    }

    @Override
    public void setParentText(Text text) {
        parentText = text;
    }
}