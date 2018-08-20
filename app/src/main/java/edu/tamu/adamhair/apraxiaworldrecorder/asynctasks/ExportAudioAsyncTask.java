package edu.tamu.adamhair.apraxiaworldrecorder.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.List;

import edu.tamu.adamhair.apraxiaworldrecorder.FileManager;
import edu.tamu.adamhair.apraxiaworldrecorder.audio.AudioProcessor;
import edu.tamu.adamhair.apraxiaworldrecorder.audio.LibrosaStyleDTW;
import edu.tamu.adamhair.apraxiaworldrecorder.audio.MFCC;
import edu.tamu.adamhair.apraxiaworldrecorder.database.Recording;
import edu.tamu.adamhair.apraxiaworldrecorder.database.Repetition;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.RecordingViewModel;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.WordViewModel;

public class ExportAudioAsyncTask extends AsyncTask<Void, Void, Void> {
    private Context context;
    private FrameLayout exportOverlay;
    private FrameLayout confirmationOverlay;
    private int userId;
    private String username;
    private List<Repetition> repetitionList;
    private RecordingViewModel recordingViewModel;
    private WordViewModel wordViewModel;

    public ExportAudioAsyncTask(Context context, FrameLayout exportOverlay, FrameLayout confirmationOverlay, int userId, String username,
                         List<Repetition> repetitionList, RecordingViewModel recordingViewModel, WordViewModel wordViewModel) {
        this.context = context;
        this.exportOverlay = exportOverlay;
        this.confirmationOverlay = confirmationOverlay;
        this.userId = userId;
        this.username = username;
        this.repetitionList = repetitionList;
        this.recordingViewModel = recordingViewModel;
        this.wordViewModel = wordViewModel;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        AlphaAnimation inAnimation = new AlphaAnimation(0f, 1f);
        inAnimation.setDuration(200);
        this.exportOverlay.setAnimation(inAnimation);
        this.exportOverlay.setVisibility(View.VISIBLE);
    }

    @Override
    protected Void doInBackground(Void... params) {
        int fs = 16000;
        int frameLength = 512;
        int frameOverlap = 512 - 128;
        int padSize = frameLength / 2;

        //FileManager.clearCalibrationAudio(username);

        List<Recording> allRecordings = new ArrayList<>();

        for (int i = 0; i < repetitionList.size(); i++) {
            // Make sure this is a complete word set
            if (repetitionList.get(i).getNumCorrect() > 4 && repetitionList.get(i).getNumIncorrect() > 4) {
                int wordId = repetitionList.get(i).getWordId();
                List<Recording> recordings = recordingViewModel.getRecordingArrayListOfUserAndWord(userId, wordId);
                allRecordings.addAll(recordings);

                /* Get all MFCCS */
                List<float[]> audioData = getAudioData(recordings, frameLength, padSize);

                AudioProcessor audioProcessor = new AudioProcessor(fs, frameLength, frameOverlap);

                List<double[][]> correctMfccs = new ArrayList<>();
                List<double[][]> incorrectMfccs = new ArrayList<>();
                for (int j = 0; j < audioData.size(); j++) {
                    audioProcessor.setAudioData(audioData.get(j));
                    if (recordings.get(j).isCorrect()) {
                        correctMfccs.add(MFCC.applyMeanCepstralNormalization(trimMfccOnEnergy(audioProcessor.computeMfccs(13))));
                    } else {
                        incorrectMfccs.add(MFCC.applyMeanCepstralNormalization(trimMfccOnEnergy(audioProcessor.computeMfccs(13))));
                    }
                }

                /* Get all distances */
                double[] correctDistances = getCorrectDistances(correctMfccs);
                double[] incorrectDistances = getIncorrectDistances(correctMfccs, incorrectMfccs);

                /* Write the data to disk */
                FileManager.recreateDistanceDatFile(correctDistances, incorrectDistances, username, repetitionList.get(i).getWordName(), context);
                int noCorrect = 0;
                int noIncorrect = 0;
                for (int j = 0; j < recordings.size(); j++) {
                    if (recordings.get(j).isCorrect()) {
                        FileManager.recreateMfccFile(correctMfccs.get(noCorrect), username,
                                repetitionList.get(i).getWordName(), recordings.get(j).getRepetitionNumber(), context);
                        noCorrect++;
                    } else {
                        FileManager.recreateMfccFile(incorrectMfccs.get(noIncorrect), username,
                                repetitionList.get(i).getWordName(), recordings.get(j).getRepetitionNumber(), context);
                        noIncorrect++;
                    }
                }
            }
        }

        /* Write to file */
        FileManager.recreateRepetitionDatFile(allRecordings, username, context);
        FileManager.recreateWordsDatFile(wordViewModel.getAllWords(), username, context);

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        this.exportOverlay.setVisibility(View.GONE);
        this.confirmationOverlay.setVisibility(View.VISIBLE);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                AlphaAnimation outAnimation = new AlphaAnimation(1f, 0f);
                outAnimation.setDuration(200);
                confirmationOverlay.setAnimation(outAnimation);
                confirmationOverlay.setVisibility(View.GONE);
            }
        }, 2500);
    }

    private float[] applyPreemphasis(float[] audioData, float alpha) {
        float[] filteredAudio = new float[audioData.length];
        filteredAudio[0] = audioData[0];
        for (int i = 1; i < audioData.length; i++){
            filteredAudio[i] = audioData[i] - alpha*audioData[i-1];
        }
        return filteredAudio;
    }

    private List<float[]> getAudioData(List<Recording> recordings, int frameLength, int padSize) {
        int trimFrames = 2048;

        List<float[]> audioData = new ArrayList<>();
        for (int j = 0; j < recordings.size(); j++) {
            if (recordings.get(j).getFileLocation() != null) {
                float[] unpaddedData = AudioProcessor.getWavAudioData(recordings.get(j).getFileLocation());
                float[] trimmedData = new float[unpaddedData.length - trimFrames];
                for (int k = 0; k < trimmedData.length; k++) {
                    trimmedData[k] = unpaddedData[k + trimFrames];
                }

                // Audio data is padded with frameLength / 2 on each side to center the frame
                float[] paddedData = new float[trimmedData.length + frameLength];
                for (int k = 0; k < padSize; k++) {
                    paddedData[k] = trimmedData[padSize + 1 - k];
                }
                for (int k = frameLength / 2; k < trimmedData.length + padSize; k++) {
                    paddedData[k] = trimmedData[k - padSize];
                }
                for (int k = 0; k < padSize; k++) {
                    paddedData[trimmedData.length + padSize + k] = trimmedData[trimmedData.length - 1 - k];
                }

                audioData.add(applyPreemphasis(paddedData, 0.99f));
            }
        }

        return audioData;
    }

    private double[] getCorrectDistances(List<double[][]> correctMfccs) {
        double[] correctScores = new double[correctMfccs.size() * (correctMfccs.size()-1)];
        // This will have duplicate scores because we compare i to j, then later j to i
        int idx = 0;
        for (int i = 0; i < correctMfccs.size(); i++) {
            for (int j = 0; j < correctMfccs.size(); j++) {
                if (i != j) {
                    LibrosaStyleDTW dtw = new LibrosaStyleDTW(correctMfccs.get(i), correctMfccs.get(j));
//                        LibrosaStyleDTW dtw = new LibrosaStyleDTW(ignoreFirstMfcc(correctMfccs.get(i)), ignoreFirstMfcc(correctMfccs.get(j)));
                    correctScores[idx] = dtw.computeRMSE();
                    idx++;
                }
            }
        }
        return correctScores;
    }

    private double[] getIncorrectDistances(List<double[][]> correctMfccs, List<double[][]> incorrectMfccs) {
        double[] incorrectScores = new double[incorrectMfccs.size() * correctMfccs.size()];
        int idx = 0;
        for (int i = 0; i < correctMfccs.size(); i++) {
            for (int j = 0; j < incorrectMfccs.size(); j++) {
                LibrosaStyleDTW dtw = new LibrosaStyleDTW(correctMfccs.get(i), incorrectMfccs.get(j));
//                    LibrosaStyleDTW dtw = new LibrosaStyleDTW(ignoreFirstMfcc(correctMfccs.get(i)), ignoreFirstMfcc(incorrectMfccs.get(j)));
                incorrectScores[idx] = dtw.computeRMSE();
                idx++;
            }
        }
        return incorrectScores;
    }

    private double[][] trimMfccOnEnergy(double[][] mfcc) {
//            System.out.println(Integer.toString(mfcc.length) + " " + Integer.toString(mfcc[0].length));

        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (int i = 0; i < mfcc.length; i++) {
            stats.addValue(mfcc[i][0]);
        }

        double thirdQuartile = stats.getPercentile(75);
        int successiveFramesOver = 3;
        int framesBeforeAfter = 1;

        // Get first crossing index
        int firstIdx = -1;
        int framesOver = 0;
        for (int i = 0; i < mfcc.length; i++) {
            if (mfcc[i][0] > thirdQuartile) {
                if (framesOver == successiveFramesOver)
                    break;
                else if (firstIdx == -1) {
                    firstIdx = i;
                    framesOver++;
                } else
                    framesOver++;
            } else {
                framesOver = 0;
                firstIdx = -1;
            }
        }

        // Make sure first index is far enough in
        if (firstIdx == -1)
            firstIdx = framesBeforeAfter;
        if (firstIdx - framesBeforeAfter < 0)
            firstIdx = framesBeforeAfter;

        // Get last crossing index
        int lastIdx = -1;
        framesOver = 0;
        for (int i = mfcc.length - 1; i >= 0; i--) {
            if (mfcc[i][0] > thirdQuartile) {
                if (framesOver == successiveFramesOver)
                    break;
                else if (lastIdx == -1) {
                    lastIdx = i;
                    framesOver++;
                } else
                    framesOver++;
            } else {
                framesOver = 0;
                lastIdx = -1;
            }
        }

        // Make sure last index is far enough from end
        if (lastIdx == -1)
            lastIdx = mfcc.length - framesBeforeAfter - 1;
        if (lastIdx + framesBeforeAfter > mfcc.length)
            lastIdx = mfcc.length - framesBeforeAfter - 1;

        // Trim the mfcc
        double[][] newMfcc = new double[lastIdx - firstIdx + 2*framesBeforeAfter][mfcc[0].length];
        for (int i = firstIdx - framesBeforeAfter; i < lastIdx + framesBeforeAfter; i++) {
            for (int j = 0; j < mfcc[0].length; j++) {
                newMfcc[i - firstIdx + framesBeforeAfter][j] = mfcc[i][j];
            }
        }

//            System.out.println(Integer.toString(newMfcc.length) + " " + Integer.toString(newMfcc[0].length));

        return newMfcc;
    }

    private double[][] ignoreFirstMfcc(double[][] mfcc) {
        double[][] newMfcc = new double[mfcc.length][mfcc[0].length-1];
        for (int i = 0; i < mfcc.length; i++) {
            for (int j = 0; j < mfcc[0].length-1; j++) {
                newMfcc[i][j] = mfcc[i][j+1];
            }
        }
        return newMfcc;
    }

}
