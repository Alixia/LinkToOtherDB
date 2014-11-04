package org.getalp.similarity.semantic.string;

import com.wcohen.ss.AbstractStringDistance;
import com.wcohen.ss.ScaledLevenstein;

public class SubmodularTverskiBuilder {
    private AbstractStringDistance distance = new ScaledLevenstein();
    private boolean computeRatio = true;
    private double alpha = 1d;
    private double beta = 0.5d;
    private double gamma = 0.5d;
    private boolean fuzzyMatching = true;
    private boolean quadraticMatching = false;
    private boolean extendedLesk = false;
    private boolean randomInit = false;
    private boolean regularizeOverlapInput = false;
    private boolean optimizeOverlapInput = false;
    private boolean regularizeRelations = false;
    private boolean optimizeRelations = false;
    private boolean isDistance = false;

    public SubmodularTverskiBuilder distance(AbstractStringDistance distance) {
        this.distance = distance;
        return this;
    }

    public SubmodularTverskiBuilder computeRatio(boolean computeRatio) {
        this.computeRatio = computeRatio;
        return this;
    }

    public SubmodularTverskiBuilder alpha(double alpha) {
        this.alpha = alpha;
        return this;
    }

    public SubmodularTverskiBuilder beta(double beta) {
        this.beta = beta;
        return this;
    }

    public SubmodularTverskiBuilder gamma(double gamma) {
        this.gamma = gamma;
        return this;
    }

    public SubmodularTverskiBuilder fuzzyMatching(boolean fuzzyMatching) {
        this.fuzzyMatching = fuzzyMatching;
        return this;
    }

    public SubmodularTverskiBuilder quadraticWeighting(boolean quadraticMatching) {
        this.quadraticMatching = quadraticMatching;
        return this;
    }

    public SubmodularTverskiBuilder extendedLesk(boolean extendedLesk) {
        this.extendedLesk = extendedLesk;
        return this;
    }

    public SubmodularTverskiBuilder randomInit(boolean randomInit) {
        this.randomInit = randomInit;
        return this;
    }

    public SubmodularTverskiBuilder regularizeOverlapInput(boolean regularizeOverlapInput) {
        this.regularizeOverlapInput = regularizeOverlapInput;
        return this;
    }

    public SubmodularTverskiBuilder optimizeOverlapInput(boolean optimizeOverlapInput) {
        this.optimizeOverlapInput = optimizeOverlapInput;
        return this;
    }

    public SubmodularTverskiBuilder regularizeRelations(boolean regularizeRelations) {
        this.regularizeRelations = regularizeRelations;
        return this;
    }

    public SubmodularTverskiBuilder optimizeRelations(boolean optimizeRelations) {
        this.optimizeRelations = optimizeRelations;
        return this;
    }

    public SubmodularTverskiBuilder isDistance(boolean isDistance) {
        this.isDistance = isDistance;
        return this;
    }

    public SubmodularTverski build() {
        SubmodularTverski st = new SubmodularTverski();
        st.setAlpha(alpha);
        st.setBeta(beta);
        st.setGamma(gamma);
        st.setOptimizeOverlapInput(optimizeOverlapInput);
        st.setOptimizeRelations(optimizeRelations);
        st.setQuadraticMatching(quadraticMatching);
        st.setRandomInit(randomInit);
        st.setRegularizeOverlapInput(regularizeOverlapInput);
        st.setRegularizeRelations(regularizeRelations);
        st.setFuzzyMatching(fuzzyMatching);
        st.setComputeRatio(computeRatio);
        st.setExtendedLesk(extendedLesk);
        st.setIsDistance(isDistance);
        return st;
    }
}