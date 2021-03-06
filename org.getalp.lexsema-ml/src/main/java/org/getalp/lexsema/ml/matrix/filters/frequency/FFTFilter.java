/**
 *
 */
package org.getalp.lexsema.ml.matrix.filters.frequency;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import org.getalp.lexsema.ml.matrix.Matrices;
import org.getalp.lexsema.ml.matrix.filters.Filter;
import org.nd4j.linalg.api.ndarray.INDArray;


/**
 * @author tchechem
 */
public class FFTFilter implements Filter {

    private boolean enabled;

    public FFTFilter() {
        super();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public DoubleMatrix2D apply(DoubleMatrix2D signal) {
        if (!enabled) {
            return null;
        }
        int vc = Integer.highestOneBit(signal.columns() - 1);
        System.err.println(vc);
        for (int i = 0; i < signal.rows(); i++) {
            DoubleFFT_1D fft = new DoubleFFT_1D(signal.columns());
            fft.realForward(signal.viewColumn(i).toArray());
        }
        return signal;
    }

    @Override
    public INDArray apply(INDArray signal) {
        return Matrices.toINDArray(apply(Matrices.toColtMatrix(signal)));
    }


    public enum NormalizationType {
        UNIT_NORM, ZERO_MEAN_VAR
    }

}
