/*
 * Carrot2 project.
 *
 * Copyright (C) 2002-2015, Dawid Weiss, Stanisław Osiński.
 * All rights reserved.
 *
 * Refer to the full license file "carrot2.LICENSE"
 * in the root folder of the repository checkout or at:
 * http://www.carrot2.org/carrot2.LICENSE
 */

package org.getalp.lexsema.ml.matrix;

import cern.colt.function.tdouble.DoubleFunction;
import cern.colt.function.tdouble.IntIntDoubleFunction;
import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import cern.jet.math.tdouble.DoubleFunctions;
import com.carrotsearch.hppc.sorting.IndirectComparator;
import com.carrotsearch.hppc.sorting.IndirectSort;
import org.jblas.DoubleMatrix;
import org.jblas.Solve;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.PrintWriter;
import java.util.Arrays;


/**
 * A set of {@code DoubleMatrix2D} shorthands and utility methods.
 */
@SuppressWarnings("deprecation")
public final class Matrices {
    private Matrices() {
    }

    /**
     * Normalizes column vectors of matrix {@code A} so that their L2 norm (Euclidean
     * distance) is equal to 1.0.
     *
     * @param A    matrix to normalize
     * @param work a temporary array of {@code A.columns()} doubles that will be
     *             overwritten with column's original L2 norms. Supply a non-null pointer
     *             to avoid continuous allocation/freeing of memory when doing calculations
     *             in a loop. If this parameter is {@code null}, a new array will be
     *             allocated every time this method is called.
     * @return A with length-normalized columns (for convenience only)
     */
    public static DoubleMatrix2D normalizeColumnL2(DoubleMatrix2D A, double... work) {
        work = prepareWork(A, work);

        // Calculate the L2 norm for each column
        for (int r = 0; r < A.rows(); r++) {
            for (int column = 0; column < A.columns(); column++) {
                work[column] += A.getQuick(r, column) * A.getQuick(r, column);
            }
        }

        // Take the square root
        for (int c = 0; c < A.columns(); c++) {
            work[c] = Math.sqrt(work[c]);
        }

        // Normalize
        normalizeColumns(A, work);

        return A;
    }

    /**
     * Normalizes column vectors of a sparse matrix {@code A} so that their L2 norm
     * (Euclidean distance) is equal to 1.0.
     *
     * @param A    matrix to normalize
     * @param work a temporary array of {@code A.columns()} doubles that will be
     *             overwritten with column's original L2 norms. Supply a non-null pointer
     *             to avoid continuous allocation/freeing of memory when doing calculations
     *             in a loop. If this parameter is {@code null}, a new array will be
     *             allocated every time this method is called.
     * @return A with length-normalized columns (for convenience only)
     */
    public static DoubleMatrix2D normalizeSparseColumnL2(final DoubleMatrix2D A,
                                                         final double... work) {
        final double[] w = prepareWork(A, work);

        A.forEachNonZero((row, column, value) -> {
            w[column] += value * value;
            return value;
        });

        // Take the square root
        for (int column = 0; column < A.columns(); column++) {
            w[column] = Math.sqrt(w[column]);
        }

        // Normalize
        A.forEachNonZero((row, column, value) -> {
            A.setQuick(row, column, value / w[column]);
            return 0;
        });

        return A;
    }

    /**
     * Normalizes column vectors of matrix {@code A} so that their L1 norm is equal
     * to 1.0.
     *
     * @param A    matrix to normalize
     * @param work a temporary array of {@code A.columns()} doubles that will be
     *             overwritten with column's original L1 norms. Supply a non-null pointer
     *             to avoid continuous allocation/freeing of memory when doing calculations
     *             in a loop. If this parameter is {@code null}, a new array will be
     *             allocated every time this method is called.
     * @return A with L1-normalized columns (for convenience only)
     */
    public static DoubleMatrix2D normalizeColumnL1(DoubleMatrix2D A, double... work) {
        work = prepareWork(A, work);

        // Calculate the L1 norm for each column
        for (int r = 0; r < A.rows(); r++) {
            for (int c = 0; c < A.columns(); c++) {
                work[c] += A.getQuick(r, c);
            }
        }

        // Normalize
        normalizeColumns(A, work);

        return A;
    }

    /**
     * Prepares a temporary array for normalizing matrix columns.
     */
    private static double[] prepareWork(DoubleMatrix2D A, double[] work) {
        // Colt's dense matrices are stored in a row-major format, so the
        // processor's cache will be better used when the rows counter is in the
        // outer loop. To do that we need a temporary double vector
        if (work == null || work.length != A.columns()) {
            work = new double[A.columns()];
        } else {
            Arrays.fill(work, 0);
        }
        return work;
    }

    /**
     * A common routine for normalizing columns of a matrix.
     */
    private static void normalizeColumns(DoubleMatrix2D A, double[] work) {
        for (int r = A.rows() - 1; r >= 0; r--) {
            for (int c = 0; c < A.columns(); c++) {
                if (work[c] != 0) {
                    A.setQuick(r, c, A.getQuick(r, c) / work[c]);
                }
            }
        }
    }

    /**
     * Computes the orthogonality of matrix A. The orthogonality is computed as a sum of
     * k*(k-1)/2 inner products of A's column vectors, k being the number of columns of A,
     * and then normalized to the 0.0 - 1.0 range.
     *
     * @param A matrix to compute orthogonality for, must be column length-normalized
     * @return orthogonality of matrix A. 0.0 denotes a perfect orthogonality between
     * every pair of A's column. 1.0 indicates that all columns of A are parallel.
     */
    public static double computeOrthogonality(DoubleMatrix2D A) {
        double orthogonality = 0;

        // Compute pairwise inner products
        DoubleMatrix2D cosines = A.zMult(A, null, 1, 0, true, false);

        for (int r = 0; r < cosines.rows(); r++) {
            for (int c = r + 1; c < cosines.columns(); c++) {
                orthogonality += cosines.getQuick(r, c);
            }
        }

        return orthogonality / ((cosines.rows() - 1) * cosines.rows() / 2.0);
    }

    /**
     * Computers sparseness of matrix {@code A} as a fraction of non-zero elements to
     * the total number of elements.
     *
     * @return sparseness of {@code A}, which is a value between 0.0 (all elements
     * are zero) and 1.0 (all elements are non-zero)
     */
    public static double computeSparseness(DoubleMatrix2D A) {
        int count = 0;

        for (int r = 0; r < A.rows(); r++) {
            for (int c = 0; c < A.columns(); c++) {
                if (A.getQuick(r, c) != 0) {
                    count++;
                }
            }
        }

        return count / (double) (A.rows() * A.columns());
    }

    /**
     * Finds the first minimum element in each column of matrix A. When calculating
     * minimum values for each column this version should perform better than scanning
     * each column separately.
     *
     * @param indices   an array of {@code A.columns()} integers in which indices of
     *                  the first minimum element will be stored. If this parameter is
     *                  {@code null} a new array will be allocated.
     * @param minValues an array of {@code A.columns()} doubles in which values of
     *                  each column's minimum elements will be stored. If this parameter is
     *                  {@code null} a new array will be allocated.
     * @return for each column of A the index of the minimum element
     */
    public static int[] minInColumns(DoubleMatrix2D A, int[] indices,
                                     double[] minValues) {
        return inColumns(A, indices, minValues, DoubleComparators.REVERSED_ORDER,
                DoubleFunctions.identity);
    }

    /**
     * Finds the first maximum element in each column of matrix A. When calculating
     * maximum values for each column this version should perform better than scanning
     * each column separately.
     *
     * @param A
     * @param indices   an array of {@code A.columns()} integers in which indices of
     *                  the first maximum element will be stored. If this parameter is
     *                  {@code null} a new array will be allocated.
     * @param maxValues an array of {@code A.columns()} doubles in which values of
     *                  each column's maximum elements will be stored. If this parameter is
     *                  {@code null} a new array will be
     *                  allocated.
     * @return for each column of A the index of the maximum element
     */
    public static int[] maxInColumns(DoubleMatrix2D A, int[] indices,
                                     double[] maxValues) {
        return maxInColumns(A, indices, maxValues, DoubleFunctions.identity);
    }

    public static int[] maxInColumns(DoubleMatrix2D A, int[] indices,
                                     double[] maxValues, DoubleFunction transform) {
        return inColumns(A, indices, maxValues, DoubleComparators.NATURAL_ORDER,
                transform);
    }

    /**
     * Common implementation of finding extreme elements in columns.
     */
    private static int[] inColumns(DoubleMatrix2D A, int[] indices, double[] extValues,
                                   DoubleComparator doubleComparator, DoubleFunction transform) {
        if (indices == null) {
            indices = new int[A.columns()];
        }

        if (A.columns() == 0 || A.rows() == 0) {
            return indices;
        }

        if (extValues == null) {
            extValues = new double[A.columns()];
        }

        for (int c = 0; c < A.columns(); c++) {
            extValues[c] = transform.apply(A.getQuick(0, c));
        }
        Arrays.fill(indices, 0);

        for (int r = 1; r < A.rows(); r++) {
            for (int c = 0; c < A.columns(); c++) {
                final double transformed = transform.apply(A.getQuick(r, c));
                if (doubleComparator.compare(transformed, extValues[c]) > 0) {
                    extValues[c] = transformed;
                    indices[c] = r;
                }
            }
        }

        return indices;
    }

    /**
     * Finds the index of the first maximum element in given row of {@code A}.
     *
     * @param A   the matrix to search
     * @param row the row to search
     * @return index of the first maximum element or -1 if the input matrix is
     * {@code null} or has zero size.
     */
    public static int maxInRow(DoubleMatrix2D A, int row) {
        int index = 0;
        double max = A.getQuick(row, index);
        for (int c = 1; c < A.columns(); c++) {
            if (max < A.getQuick(row, c)) {
                max = A.getQuick(row, c);
                index = c;
            }
        }

        return index;
    }

    /**
     * Calculates the sum of rows of matrix {@code A}.
     *
     * @param sums an array to store the results. If the array is {@code null} or
     *             does not match the number of rows in matrix {@code A}, a new array
     *             will be created.
     * @return sums of rows of {@code A}
     */
    public static double[] sumRows(DoubleMatrix2D A, double[] sums) {
        if (sums == null || A.rows() != sums.length) {
            sums = new double[A.rows()];
        } else {
            Arrays.fill(sums, 0);
        }

        for (int r = 0; r < A.rows(); r++) {
            for (int c = 0; c < A.columns(); c++) {
                sums[r] += A.getQuick(r, c);
            }
        }

        return sums;
    }

    /**
     * Calculates the Frobenius norm of a matrix.
     *
     * @see <a href="http://en.wikipedia.org/wiki/Matrix_norm#Frobenius_norm">Frobenius
     * norm</a>
     */
    public static double frobeniusNorm(DoubleMatrix2D matrix) {
        return Math.sqrt(matrix.aggregate(DoubleFunctions.plus, DoubleFunctions.square));
    }

    /**
     * Returns view of the provided matrix with rows permuted according to the order
     * defined by the provided comparator.
     *
     * @param matrix     to permute
     * @param comparator to use
     * @return view of the provided matrix with rows permuted according to the order
     * defined by the provided comparator.
     */
    public static DoubleMatrix2D sortedRowsView(DoubleMatrix2D matrix,
                                                IndirectComparator comparator) {
        return matrix
                .viewSelection(IndirectSort.mergesort(0, matrix.rows(), comparator), null);
    }

    private interface DoubleComparator {
        int compare(double a, double b);
    }

    private static final class DoubleComparators {
        /**
         * Compares {@code int} in their natural order.
         */
        public static final DoubleComparator NATURAL_ORDER = new NaturalOrderDoubleComparator();

        /**
         * Compares {@code int} in their reversed order.
         */
        public static final DoubleComparator REVERSED_ORDER = new ReversedOrderDoubleComparator();

        /**
         * No instantiation.
         */
        private DoubleComparators() {
        }

        /**
         * Natural order.
         */
        private static class NaturalOrderDoubleComparator implements DoubleComparator {
            public int compare(double v1, double v2) {
                return Double.compare(v1, v2);
            }
        }

        /**
         * Reversed order.
         */
        private static class ReversedOrderDoubleComparator implements DoubleComparator {
            public int compare(double v1, double v2) {
                return -Double.compare(v1, v2);
            }
        }
    }


    public static INDArray getRowWiseMeanVector(INDArray m) {
        INDArray mean = Nd4j.create(m.rows());
        for (int i = 0; i < m.rows(); i++) {
            mean.putScalar(i, vectorSum(m.getRow(i)) / m.columns());
        }
        return mean;
    }

    public static double vectorSum(INDArray vector){
        double sum = 0d;
        for(int i=0;i<vector.columns();i++){
            sum+=vector.getDouble(i);
        }
        return sum;
    }

    public static INDArray subtractMeanFromRows(INDArray signalMatrix){
        INDArray means = getRowWiseMeanVector(signalMatrix);
        INDArray newSignal = Nd4j.create(signalMatrix.rows(),signalMatrix.columns());
        for(int i=0;i<newSignal.rows();i++){
            for(int j=0;j<newSignal.columns();j++){
                newSignal.putScalar(new int[]{i, j}, signalMatrix.getDouble(i, j) - means.getDouble(i));
            }
        }
        return newSignal;
    }

    public static INDArray subtractMeanFromRowsi(INDArray newSignal){
        INDArray means = getRowWiseMeanVector(newSignal);
        for(int i=0;i<newSignal.rows();i++){
            for(int j=0;j<newSignal.columns();j++){
                newSignal.putScalar(new int[]{i, j}, newSignal.getDouble(i, j) - means.getDouble(i));
            }
        }
        return newSignal;
    }

    public static INDArray covarianceMatrix(INDArray signalMatrix){
        INDArray spd = subtractMeanFromRows(signalMatrix);
        spd = spd.transpose().mmul(spd);
        spd.muli(1d/signalMatrix.columns());
        return spd;
    }

    public static DoubleMatrix toJBlasMatrix(INDArray matrix){
        DoubleMatrix doubleMatrix = new DoubleMatrix(matrix.rows(),matrix.columns());
        for(int i=0;i<matrix.rows();i++){
            for(int j=0;j<matrix.columns();j++){
                doubleMatrix.put(new int[]{i, j}, matrix.getDouble(i, j));
            }
        }
        return  doubleMatrix;
    }

    public static INDArray toINDArray(DoubleMatrix doubleMatrix){
        INDArray matrix = Nd4j.create(doubleMatrix.rows, doubleMatrix.columns);
        for(int i=0;i<matrix.rows();i++){
            for(int j=0;j<matrix.columns();j++){
                matrix.putScalar(new int[]{i, j}, doubleMatrix.get(i, j));
            }
        }
        return matrix;
    }

    public static INDArray inverse(INDArray matrix){
        return toINDArray(Solve.pinv(toJBlasMatrix(matrix)));
    }
    
    public static DoubleMatrix2D toColtMatrix(INDArray matrix){
        DoubleMatrix2D doubleMatrix = new DenseDoubleMatrix2D(matrix.rows(),matrix.columns());
        for(int i=0;i<matrix.rows();i++){
            for(int j=0;j<matrix.columns();j++){
                doubleMatrix.setQuick(i, j, matrix.getDouble(i, j));
            }
        }
        return  doubleMatrix;
    }

    public static INDArray getColumnWiseMeanVector(INDArray array){
        INDArray mean = Nd4j.create(array.columns());
        for (int i = 0; i < array.columns(); i++) {
            mean.putScalar(i, vectorSum(array.getColumn(i)) / array.rows());
        }
        return mean;
    }

    public static INDArray getColumnWiseSumVector(INDArray array){
        if(array.isRowVector()){
            return array;
        } else {
            INDArray mean = Nd4j.create(array.columns());
            for (int i = 0; i < array.columns(); i++) {
                mean.putScalar(i,
                        vectorSum(array.getColumn(i)));
            }
            return mean;
        }
    }

    public static INDArray toINDArray(DoubleMatrix2D doubleMatrix){
        INDArray matrix = Nd4j.create(doubleMatrix.rows(),doubleMatrix.columns());
        for(int i=0;i<matrix.rows();i++){
            for(int j=0;j<matrix.columns();j++){
                matrix.putScalar(new int[]{i, j}, doubleMatrix.getQuick(i, j));
            }
        }
        return matrix;
    }


    public static void matrixCSVWriter(PrintWriter pw, DoubleMatrix2D matrix) {
        for (int row = 0; row < matrix.rows(); row++) {
            for (int col = 0; col < matrix.columns(); col++) {
                if (col < matrix.columns() - 1) {
                    pw.print(String.format("%s,", matrix.getQuick(row, col)));
                } else {
                    pw.println(matrix.getQuick(row, col));
                }
            }
        }
        pw.flush();
    }
}
