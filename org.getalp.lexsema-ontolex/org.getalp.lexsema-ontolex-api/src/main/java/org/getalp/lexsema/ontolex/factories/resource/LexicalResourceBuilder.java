package org.getalp.lexsema.ontolex.factories.resource;

import org.getalp.lexsema.ontolex.LexicalResource;
import org.getalp.lexsema.ontolex.graph.OntologyModel;
import org.getalp.lexsema.util.Language;

/**
 * Defines the interface for LexicalResource builders that are delegated the role of creating instances
 * of <code>LexicalResource</code> implementation. <code>LexicalResourceBuilder</code> implementations are meant
 * to be loaded automatically by <code>LexicalResourceFactory</code>.
 * <b>IMPORTANT:</b>The naming convention is the following:
 * The builder should be named <b>ResourceNameBuilder</b>, where ResourceName is the name of the implementation of
 * <code>LexicalResource</code> for that particular resource</b>. E.G. OntolexBuilder for the builder of Ontolex objects,
 * where class Ontolex implements LexicalResource.
 */
public interface LexicalResourceBuilder {
    /**
     * Build an instance of a particular <code>LexicalResource</code> implementation
     *
     * @param model The ontology model the resource is located in
     * @return The resulting instance
     */
    public LexicalResource build(OntologyModel model);

    /**
     * Build an instance of a particular <code>LexicalResource</code> implementation
     *
     * @param model    The ontology model the resource is located in
     * @param language the language of the resource
     * @return The resulting instance
     */
    public LexicalResource build(OntologyModel model, Language language);

    /**
     * Build an instance of a particular <code>LexicalResource</code> implementation
     *
     * @param model    The ontology model the resource is located in
     * @param language the language of the resource
     * @param uri      The uri of the resource's graph
     * @return The resulting instance
     */
    public LexicalResource build(OntologyModel model, Language language, String uri);

    /**
     * Build an instance of a particular <code>LexicalResource</code> implementation
     *
     * @param model The ontology model the resource is located in
     * @param uri   The uri of the resource's graph
     * @return The resulting instance
     */
    public LexicalResource build(OntologyModel model, String uri);

    /**
     * Build an instance of a particular <code>LexicalResource</code> implementation
     *
     * @param model     The ontology model the resource is located in
     * @param languages the set of language supported by the resource, if the resource supports only one language, the
     *                  only the first language is considered.
     * @param uri       The uri of the resource's graph
     * @return The resulting instance
     */
    public LexicalResource build(OntologyModel model, String uri, Language... languages);

    /**
     * Build an instance of a particular <code>LexicalResource</code> implementation
     *
     * @param model     The ontology model the resource is located in
     * @param languages the set of language supported by the resource, if the resource supports only one language, the
     *                  only the first language is considered.
     * @return The resulting instance
     */
    public LexicalResource build(OntologyModel model, Language... languages);
}


