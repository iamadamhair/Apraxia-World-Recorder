package edu.tamu.adamhair.apraxiaworldrecorder.audio;

import android.util.Log;

import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class AudioProcessor {

    private int fs;
    private int frameLength;
    private int frameOverlap;
    private float[] audioData;

    public AudioProcessor(int fs, int frameLength, int frameOverlap) {
        /* frameLength and frameOverlap are in samples */
        this.fs = fs;
        this.frameLength = frameLength;
        this.frameOverlap = frameOverlap;
    }

    public void setAudioData(float[] audioData) {
        this.audioData = audioData;
    }

    public double[][] computeFftResponse() {
        int noFrames = (int) Math.ceil((audioData.length - frameOverlap) / (frameLength - frameOverlap));
        double[][] fftResponse = new double[noFrames][frameLength];
        for (int i = 0; i < noFrames; i++) {
            double[][] frame = new double[2][frameLength];
            for (int j = 0; j < frameLength; j++) {
                int audioIdx = i*(frameLength - frameOverlap) + j;
                frame[0][j] = applyHammingWindow((double) audioData[audioIdx], j);
                frame[1][j] = 0;
            }

            FastFourierTransformer.transformInPlace(frame, DftNormalization.STANDARD, TransformType.FORWARD);

            for (int j = 0; j < frameLength; j++) {
                fftResponse[i][j] = Math.hypot(frame[0][j], frame[1][j]);
            }
        }

        return fftResponse;
    }

    public double[][] computeMfccs(int noCoeffs) {
        int noFrames = (int) Math.ceil((audioData.length - frameOverlap) / (frameLength - frameOverlap));
//        double[][] fftResponse = computeFftResponse();
        double[][] fftResponse = computeFftResponse();
        MFCC mfcc = new MFCC();
        mfcc.generateFilterBank(noCoeffs, this.frameLength, this.fs);

        double[][] mfccs = new double[noFrames][noCoeffs];
        for (int i = 0; i < noFrames; i++) {
            mfccs[i] = mfcc.generateMfccs(fftResponse[i]);
        }

//        return mfccs;
        return mfcc.applyMeanCeptralNormalization(mfccs);
    }

    private double applyHammingWindow(double data, int position) {
        return data * (0.54 - 0.46*Math.cos(2*Math.PI * position/(frameLength - 1)));
    }

    public static float[] getWavAudioData(String path) {
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(path));

            /* Get audio data length from the file we wrote previously */
            byte[] dataSizeBuffer = new byte[4];
            bufferedInputStream.skip(40);
            bufferedInputStream.read(dataSizeBuffer, 0, 4);
            long audioDataSize = (long) (dataSizeBuffer[0] & 0xff);
            audioDataSize += (long) ((dataSizeBuffer[1] & 0xff) << 8);
            audioDataSize += (long) ((dataSizeBuffer[2] & 0xff) << 16);
            audioDataSize += (long) ((dataSizeBuffer[3] & 0xff) << 24);

            /* Ignore the first 44 bytes since they're the wav header */
            byte[] buffer = new byte[(int) audioDataSize];
            bufferedInputStream.read(buffer, 0, (int) audioDataSize);
            bufferedInputStream.close();

            return WavRecorder.shortToFloat(WavRecorder.byteToShort(buffer));

        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public double computeEffectSize(double[] correctScores, double[] incorrectScores) {
        double correctMean = 0;
        double correctVar = 0;
        double incorrectMean = 0;
        double incorrectVar = 0;

        // Compute mean of correct scores
        for (int i = 0; i < correctScores.length; i++) {
            correctMean += correctScores[i];
        }
        correctMean /= correctScores.length;

        // Compute variance of correct scores
        for (int i = 0; i < correctScores.length; i++) {
            correctVar += Math.pow(correctScores[i]-correctMean, 2);
        }
        correctVar /= correctScores.length;

        // Compute mean of incorrect scores
        for (int i = 0; i < incorrectScores.length; i++) {
            incorrectMean += incorrectScores[i];
        }
        incorrectMean /= incorrectScores.length;

        // Compute variance of correct scores
        for (int i = 0; i < incorrectScores.length; i++) {
            incorrectVar += Math.pow(incorrectScores[i]-incorrectMean, 2);
        }
        incorrectVar /= incorrectScores.length;

        double pooledStdDev = (correctScores.length - 1)*correctVar + (incorrectScores.length - 1)*incorrectVar;
        pooledStdDev /= correctScores.length + incorrectScores.length - 2;
        pooledStdDev = Math.sqrt(pooledStdDev);

        return Math.abs((correctMean - incorrectMean) / pooledStdDev);
    }


    private double fix(double value) {
        /* Meant to replicate the Matlab fix function. Round towards zero */
        if (value < 0) {
            return Math.ceil(value);
        } else {
            return Math.floor(value);
        }
    }


}
