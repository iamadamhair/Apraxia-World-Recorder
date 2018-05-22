package edu.tamu.adamhair.apraxiaworldrecorder.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.PipedAudioStream;
import be.tarsos.dsp.io.TarsosDSPAudioInputStream;
import be.tarsos.dsp.io.android.AndroidFFMPEGLocator;
import be.tarsos.dsp.mfcc.MFCC;

public class AudioFeatureExtractor {

    private String filename;
    private int sampleRate;
    private float frameLength = 0.025f; // 25 ms
    private int bufferSize;
    private float overlapLength = 0.005f; // 5 ms
    private int bufferOverlap;
    private List<float[]> mfccList;
    private int noCoefficients = 13;
    private Context context;

    public AudioFeatureExtractor(String filename, final int sampleRate, Context context) {
        this.filename = filename;
        this.sampleRate = sampleRate;
        this.mfccList = new ArrayList<>();
        this.bufferSize = Math.round(this.sampleRate*this.frameLength);
        this.bufferOverlap = 0;//Math.round(this.sampleRate*this.overlapLength);
        this.context = context;
    }

    public void generateFeatures() {
        try {
            new AndroidFFMPEGLocator(context);
            PipedAudioStream f = new PipedAudioStream(this.filename);
            TarsosDSPAudioInputStream inStream = f.getMonoStream(this.sampleRate, 0);
            final AudioDispatcher dispatcher = new AudioDispatcher(
                    inStream, this.bufferSize, this.bufferOverlap);

            // 13 coefficients, 40 mel filters, 133hz lower filter, nyguist upper filter
            final MFCC mfcc = new MFCC(this.bufferSize, this.sampleRate,
                    noCoefficients, 40, 133.33f, ((float)this.sampleRate)/2f);

            dispatcher.addAudioProcessor(mfcc);
            dispatcher.addAudioProcessor(new AudioProcessor() {
                @Override
                public boolean process(AudioEvent audioEvent) {
                    float[] withoutMfccZero = new float[noCoefficients-1];
                    for (int i = 1; i < noCoefficients; i++) {
                        withoutMfccZero[i-1] = mfcc.getMFCC()[i];
                    }
                    mfccList.add(withoutMfccZero);
                    return true;
                }

                @Override
                public void processingFinished() {
                    Log.d("Audio Processor", "Finished processing " + String.valueOf(dispatcher.secondsProcessed()) + " seconds");
                }
            });
            dispatcher.run();
//            new Thread(dispatcher, "MFCC computation").start();
        } catch (Exception e) {
            Log.e("Feature Extraction", "Unable to open file for processing: " + this.filename);
        }
        meanCepstralNormalization();
    }

    public void meanCepstralNormalization() {
        /*
        Remove the mean of each mfcc coefficient
        ie, subtract the mean of all ith coefficients from each ith coefficient
         */

        int noCoeffsToProcess =  mfccList.get(0).length;
        // Initialize empty array of the values to eventually subtract
        float[] meanValues = new float[noCoeffsToProcess];
        for (int i = 0; i < noCoeffsToProcess; i++) {
            meanValues[i] = 0f;
        }
        // Get the sum of the coefficients
        for (int i = 0; i < mfccList.size(); i++) {
            for (int j = 0; j < noCoeffsToProcess; j++) {
                meanValues[j] += mfccList.get(i)[j];
            }
        }
        // Get the mean of the coefficients
        for (int i = 0; i < noCoeffsToProcess; i++) {
            meanValues[i] = meanValues[i] / mfccList.size();
        }
        // Subtract the mean from each coefficient
        for (int i = 0; i < mfccList.size(); i++) {
            for (int j = 0; j < noCoeffsToProcess; j++) {
                mfccList.get(i)[j] = mfccList.get(i)[j] - meanValues[j];
            }
        }
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public List<float[]> getMfccList() {
        return mfccList;
    }
}
