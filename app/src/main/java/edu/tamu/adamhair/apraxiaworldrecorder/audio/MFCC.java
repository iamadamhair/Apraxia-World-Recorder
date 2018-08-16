package edu.tamu.adamhair.apraxiaworldrecorder.audio;

import android.util.Log;

/*  MFCC generator class
    Adam Hair 2018

    Usage:
    1. Initialize an MFCC instance (Returns new MFCC instance)
    2. Generate the filter bank (Returns nothing)
    3. Generate the MFCCs (Returns double[] of length noCoeffs)
    4. Apply mean cepstral normalization, if desired (Returns double[] of length noCoeffs)

 */

public class MFCC {

    private double[][] filterBank;
    private double mfccConstant = 1000/Math.log1p(1000/700);
    private int noCoeffs;

    public double[] generateMfccs(double[] frame) {

        double[] melFreqs = new double[noCoeffs];
        for (int i = 0; i < noCoeffs; i++) {

            melFreqs[i] = 0;
            for (int j = 0; j < frame.length / 2; j++) {
                melFreqs[i] += filterBank[i][j] * frame[j];
            }
            melFreqs[i] = Math.log(melFreqs[i]);
        }
        return applyDct2(melFreqs);
    }

    public void generateFilterBank(int noCoeffs, int frameLength, int fs) {
        this.noCoeffs = noCoeffs;

        double minMel = 0;
        double maxMel = freqToMel(fs / 2);
        double deltaMel = (maxMel - minMel)/(noCoeffs + 1);
        double deltaFreq = fs / (double) frameLength;

        double[] melPoints = new double[frameLength / 2];
        for (int i = 0; i < frameLength / 2; i++) {
            melPoints[i] = freqToMel(i * deltaFreq);
        }

        double[] melCenters = new double[noCoeffs];
        for (int i = 0; i < noCoeffs; i++) {
            melCenters[i] = minMel + (i + 1) * deltaMel;
        }

        filterBank = new double[noCoeffs][melPoints.length];
        for (int i = 0; i < noCoeffs; i++) {
            double left = melCenters[i] - deltaMel;
            double right = melCenters[i] + deltaMel;

            for (int j = 0; j < melPoints.length; j++) {
                if (left <= melPoints[j] && melPoints[j] < melCenters[i]) {
                    filterBank[i][j] = (melPoints[j] - left) / (melCenters[i] - left);
                } else if (melPoints[j] <= left) {
                    filterBank[i][j] = 0;
                }

                if (melCenters[i] == melPoints[j]) {
                    filterBank[i][j] = 1.0;
                }

                if (melCenters[i] < melPoints[j] && melPoints[j] <= right) {
                    filterBank[i][j] = (right - melPoints[j]) / (right - melCenters[i]);
                } else if (right < melPoints[j]) {
                    filterBank[i][j] = 0;
                }
            }
        }
    }

    private double[] applyDct2(double[] melFreqs) {
        double[] mfccs = new double[melFreqs.length];
        double constant = Math.PI/melFreqs.length;

        for (int i = 0; i < melFreqs.length; i++) {

            mfccs[i] = 0;
            for (int j = 0; j < melFreqs.length; j++) {
                mfccs[i] += melFreqs[j]*Math.cos(constant*(j + 0.5)*i);
            }
            mfccs[i] /= melFreqs.length;
        }
        return mfccs;
    }

    public static double[][] applyMeanCepstralNormalization(double[][] mfcc) {
        double[] means = new double[mfcc[0].length];
        double[][] normalizedMfcc = new double[mfcc.length][mfcc[0].length];

        for (int i = 0; i < means.length; i++) {
            means[i] = 0;
        }

        for (int i = 0; i < mfcc.length; i++) {
            for (int j = 0; j < mfcc[0].length; j++) {
                means[j] += mfcc[i][j];
            }
        }

        for (int i = 0; i < mfcc.length; i++) {
            for (int j = 0; j < mfcc[0].length; j++) {
                normalizedMfcc[i][j] = mfcc[i][j] - means[j]/mfcc.length;
            }
        }

        return normalizedMfcc;
    }

    private double freqToMel(double freq) {
        return Math.log1p(freq/700)*mfccConstant;
    }
}
