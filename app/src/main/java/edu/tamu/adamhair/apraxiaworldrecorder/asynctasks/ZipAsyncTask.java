package edu.tamu.adamhair.apraxiaworldrecorder.asynctasks;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import edu.tamu.adamhair.apraxiaworldrecorder.FileManager;
import edu.tamu.adamhair.apraxiaworldrecorder.database.Recording;
import edu.tamu.adamhair.apraxiaworldrecorder.database.Word;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.RecordingViewModel;
import edu.tamu.adamhair.apraxiaworldrecorder.viewmodels.WordViewModel;

public class ZipAsyncTask extends AsyncTask<String, Void, Void> {
    /*
        Zip function taken from SO answer:
        https://stackoverflow.com/questions/6683600/zip-compress-a-folder-full-of-files-on-android
         */
    final int BUFFER = 2048;
    private Context mContext;
    private String destinationPath;
    private StorageReference storageReference;
    private RecordingViewModel recordingViewModel;
    private WordViewModel wordViewModel;
    private int userId;
    private String username;
    private FrameLayout overlay;
    private FrameLayout confirmationOverlay;
    private Boolean shouldDeleteOnCompletion;

    public ZipAsyncTask(Context context, StorageReference reference, RecordingViewModel recordingViewModel,
                 WordViewModel wordViewModel, int userId, String username, FrameLayout overlay,
                        FrameLayout confirmationOverlay, Boolean shouldDeleteOnCompletion) {
        mContext = context;
        storageReference = reference;
        this.recordingViewModel = recordingViewModel;
        this.userId = userId;
        this.username = username;
        this.wordViewModel = wordViewModel;
        this.overlay = overlay;
        this.confirmationOverlay = confirmationOverlay;
        this.shouldDeleteOnCompletion = shouldDeleteOnCompletion;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        AlphaAnimation inAnimation = new AlphaAnimation(0f, 1f);
        inAnimation.setDuration(200);
        overlay.setAnimation(inAnimation);
        overlay.setVisibility(View.VISIBLE);
    }

    @Override
    protected Void doInBackground(final String... params) {
        // Get Repetitions to write label file
        List<Recording> recordings = recordingViewModel.getRecordingsListByUserId(userId);
        List<Word> words = wordViewModel.getAllWords();

        FileManager.recreateRepetitionDatFile(recordings, username, mContext);
        FileManager.recreateWordsDatFile(words, username, mContext);

        // Param 0 is source, param 1 is destination
        destinationPath = params[1];
        zipFileAtPath(params[0], params[1]);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        Log.d("Zipper", "Files zipped and attempting to upload");
        // Upload zip to Firebase upon zip completion
        Uri file = Uri.fromFile(new File(destinationPath));
        StorageReference zipRef = storageReference.child(username + String.valueOf(System.currentTimeMillis() / 1000) + ".zip");

        zipRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        if (shouldDeleteOnCompletion)
                            deleteDestinationFolder();

                        Log.d("Firebase", "File uploaded");
                        overlay.setVisibility(View.GONE);
                        confirmationOverlay.setVisibility(View.VISIBLE);

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
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firebase", "File not uploaded");
                        overlay.setVisibility(View.GONE);
                        Toast.makeText(mContext, "Files not able to upload!", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean zipFileAtPath(String sourcePath, String destinationPath) {
        File sourceFile = new File(sourcePath);

        try {
            BufferedInputStream origin = null;
            FileOutputStream destination = new FileOutputStream(destinationPath);
            ZipOutputStream outputZip = new ZipOutputStream(new BufferedOutputStream(destination));

            if (sourceFile.isDirectory()) {
                zipSubFolder(outputZip, sourceFile, sourceFile.getParent().length()+1);
            } else {
                byte data[] = new byte[BUFFER];
                FileInputStream fi = new FileInputStream(sourcePath);
                origin = new BufferedInputStream(fi, BUFFER);

                ZipEntry entry = new ZipEntry(getLastPathComponent(sourcePath.substring(sourcePath.lastIndexOf("/"))));
                outputZip.putNextEntry(entry);

                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1 ) {
                    outputZip.write(data, 0, count);
                }
            }
            outputZip.close();
            MediaScannerConnection.scanFile(mContext, new String[] {destinationPath}, null, null);

        } catch (Exception e) {
            e.printStackTrace();;
            return false;
        }
        return true;
    }

    private void deleteDestinationFolder() {
        new File(destinationPath).delete();
    }

    private void zipSubFolder(ZipOutputStream out, File folder, int basePathLength) throws IOException {

        final int BUFFER = 2048;

        File[] fileList = folder.listFiles();
        BufferedInputStream origin = null;
        for (File file : fileList) {
            if (file.isDirectory()) {
                zipSubFolder(out, file, basePathLength);
            } else {
                byte data[] = new byte[BUFFER];
                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath
                        .substring(basePathLength);
                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(relativePath);
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
        }
    }

    private String getLastPathComponent(String filePath) {
        String[] segments = filePath.split("/");
        if (segments.length == 0)
            return "";
        String lastPathComponent = segments[segments.length - 1];
        return lastPathComponent;
    }
}
