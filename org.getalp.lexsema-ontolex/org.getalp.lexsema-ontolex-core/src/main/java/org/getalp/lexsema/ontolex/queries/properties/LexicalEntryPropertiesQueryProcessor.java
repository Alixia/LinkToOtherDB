package org.getalp.lexsema.ontolex.queries.properties;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.Var;
import lombok.Data;
import org.getalp.lexsema.ontolex.LexicalResource;
import org.getalp.lexsema.ontolex.Graph;
import org.getalp.lexsema.ontolex.queries.ARQSelectQueryImpl;
import org.getalp.lexsema.ontolex.queries.AbstractQueryProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * This query processor implements a query that retrieves all <code>LexicalSense</code>s for a
 * given <code>LexicalEntry</code>.
 */
public final class LexicalEntryPropertiesQueryProcessor extends AbstractQueryProcessor<LexicalEntryPropertiesQueryProcessor.LexicalEntryProperties> {

    private LexicalResource lexicalResource;
    private String uri;
    private String lemma = "";
    private String pos = "";

    private String WRITTEN_FORM_RESULT_VAR = "v";
    private String LEXINFO_POS_RESULT_VAR = "p";


    public LexicalEntryPropertiesQueryProcessor(Graph graph,
                                                LexicalResource lexicalResource,
                                                String uri, String lemma, String pos) {
        super(graph);
        this.lexicalResource = lexicalResource;
        this.uri = uri;
        this.lemma = lemma;
        this.pos = pos;
        initialize();
    }

    @Override
    protected final void defineQuery() {
        setQuery(new ARQSelectQueryImpl());
        if (lemma == null || lemma.isEmpty()) {
            String LEMMA_CF_VAR = "cf";
            addTriple(NodeFactory.createURI(uri),
                    getNode("lemon:canonicalForm"),
                    Var.alloc(LEMMA_CF_VAR));
            addTriple(Var.alloc(LEMMA_CF_VAR),
                    getNode("lemon:writtenRep"),
                    Var.alloc(WRITTEN_FORM_RESULT_VAR));
            addResultVar(WRITTEN_FORM_RESULT_VAR);
        }
        if (pos == null || pos.isEmpty()) {
            addOptionalTriple(NodeFactory.createURI(uri),
                    getNode("lexinfo:partOfSpeech"),
                    Var.alloc(LEXINFO_POS_RESULT_VAR));
            addResultVar(LEXINFO_POS_RESULT_VAR);
        }

    }


    @Override
    public List<LexicalEntryProperties> processResults() {
        List<LexicalEntryProperties> entries = new ArrayList<>();
        @SuppressWarnings("LocalVariableOfConcreteClass")
        LexicalEntryProperties properties = new LexicalEntryProperties();
        while (hasNextResult()) {
            QuerySolution qs = nextSolution();
            if (lemma == null || lemma.isEmpty()) {
                lemma = qs.get(WRITTEN_FORM_RESULT_VAR).toString().split("@")[0].replace(" ", "_");
                properties.setLemma(lemma);
            }
            if (pos == null || pos.isEmpty()) {
                RDFNode posNode = qs.get(LEXINFO_POS_RESULT_VAR);
                if (posNode != null) {
                    pos = posNode.toString();
                    properties.setPos(pos);
                }
            }
            entries.add(properties);
        }
        return entries;
    }

    private String getResourceGraphURI() {
        return lexicalResource.getResourceGraphURI();
    }

    public static class LexicalEntryProperties {
        private String lemma = "";
        private String pos = "";

        public String getLemma() {
            return lemma;
        }

        public void setLemma(String lemma) {
            this.lemma = lemma;
        }

        public String getPos() {
            return pos;
        }

        public void setPos(String pos) {
            this.pos = pos;
        }
    }
}
