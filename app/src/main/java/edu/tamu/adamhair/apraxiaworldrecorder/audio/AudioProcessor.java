package edu.tamu.adamhair.apraxiaworldrecorder.audio;

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

    public AudioProcessor() {

    }

    public AudioProcessor(int fs, int frameLength, int frameOverlap) {
        /* frameLength and frameOverlap are in samples */
        this.fs = fs;
        this.frameLength = frameLength;
        this.frameOverlap = frameOverlap;
    }

    public void setAudioData(float[] audioData) {
        this.audioData = audioData;
    }

    public Complex[][] computeSpectrogram() {
        /* Computes a spectrogram using the abs of the fft */
        int noFrames = (int) Math.ceil((audioData.length - frameOverlap) / (frameLength - frameOverlap));
        Complex[][] frame = new Complex[noFrames][frameLength];
        for (int i = 0; i < noFrames; i++) {
            for (int j = 0; j < frameLength; j++) {
                int audioIdx = i*(frameLength - frameOverlap) + j;
                frame[i][j] = new Complex(applyHammingWindow((double) audioData[audioIdx], j), 0);
            }
            InplaceFFT.fft(frame[i]);
        }
        return frame;
    }

    public double[][] apacheChommonsFftResponse() {
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

    public double[][] computeFftResponse() {
        /* Computes a spectrogram using the abs of the fft */
        int noFrames = (int) Math.ceil((audioData.length - frameOverlap) / (frameLength - frameOverlap));
        Complex[][] frame = new Complex[noFrames][frameLength];
        for (int i = 0; i < noFrames; i++) {
            for (int j = 0; j < frameLength; j++) {
                int audioIdx = i*(frameLength - frameOverlap) + j;
                frame[i][j] = new Complex(applyHammingWindow((double) audioData[audioIdx], j), 0);
            }
            InplaceFFT.fft(frame[i]);
        }

        double[][] fftResponse = new double[noFrames][frameLength];
        for (int i = 0; i < noFrames; i++) {
            for (int j = 0; j < frameLength; j++) {
                fftResponse[i][j] = frame[i][j].abs();
            }
        }

        return fftResponse;
    }

    public double[][] computeMfccs(int noCoeffs) {
        int noFrames = (int) Math.ceil((audioData.length - frameOverlap) / (frameLength - frameOverlap));
//        double[][] fftResponse = computeFftResponse();
        double[][] fftResponse = apacheChommonsFftResponse();
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

    private double fix(double value) {
        /* Meant to replicate the Matlab fix function. Round towards zero */
        if (value < 0) {
            return Math.ceil(value);
        } else {
            return Math.floor(value);
        }
    }


}
