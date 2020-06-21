package com.example.videoeditor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.videoeditor.helpers.PermissionHelper;
import com.example.videoeditor.utils.FileUtils;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import org.florescu.android.rangeseekbar.RangeSeekBar;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_TAKE_GALLERY_VIDEO = 100;
    private VideoView videoView;
    private RangeSeekBar rangeSeekBar;
    private Runnable r;
    private FFmpeg ffmpeg;
    private ProgressDialog progressDialog;
    private Uri selectedFileUri;
    private static final String TAG = "BHUVNESH";
    private static final String POSITION = "position";
    private static final String FILEPATH = "filepath";
    private int choice = 0;
    private int stopPosition;
    private ScrollView mainlayout;
    private TextView tvLeft, tvRight;
    private String inputFilePath, outputFilePath;
    private File inputFile, outputFile;
    private int duration;
    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView uploadVideo = findViewById(R.id.uploadVideo);

        mContext = this;
        videoView = (VideoView) findViewById(R.id.videoView);
        rangeSeekBar = (RangeSeekBar) findViewById(R.id.rangeSeekBar);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(null);
        progressDialog.setCancelable(false);
        loadFFMpegBinary();
        TextView decreaseSpeed = findViewById(R.id.decreaseSpeed);
        uploadVideo.setOnClickListener(v -> {
            PermissionHelper.checkPermissions(new MultiplePermissionsListener() {
                @Override
                public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                    uploadVideo();
                }

                @Override
                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                    //TODO Request permission again
                }
            });
        });


//        decreaseSpeed.setOnClickListener(v -> {
//            PermissionHelper.checkPermissions(new MultiplePermissionsListener() {
//                @Override
//                public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
//                    choice = 7;
//                    if (selectedFileUri != null) {
//                        executeSlowMotionVideoCommand();
//                    } else
//                        Snackbar.make(mainlayout, "Please upload a video", 4000).show();
//                }
//
//
//                @Override
//                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
//
//                }
//            });
//
//        });


        decreaseSpeed.setOnClickListener(v -> {
            PermissionHelper.checkPermissions(new MultiplePermissionsListener() {
                @Override
                public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                    try {
                        FileUtils.createApplicationFolder();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                }
            });
        });
    }


    /**
     * Opening gallery for uploading video
     */
    private void uploadVideo() {
        try {
            Intent intent = new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);
        } catch (Exception e) {
            Log.e(TAG, "uploadVideo: ", e);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                selectedFileUri = data.getData();
                inputFile = new File(selectedFileUri.getPath());
                videoView.setVideoURI(selectedFileUri);
                videoView.start();

                videoView.setOnPreparedListener(mp -> {
                    duration = mp.getDuration() / 1000;

                    mp.setLooping(true);
                    rangeSeekBar.setRangeValues(0, duration);
                    rangeSeekBar.setSelectedMinValue(0);
                    rangeSeekBar.setSelectedMaxValue(duration);
                    rangeSeekBar.setEnabled(true);

                    rangeSeekBar.setOnRangeSeekBarChangeListener((bar, minValue, maxValue) -> videoView.seekTo((int) minValue * 1000));

                    final Handler handler = new Handler();
                    handler.postDelayed(r = () -> {
                        if (videoView.getCurrentPosition() >= rangeSeekBar.getSelectedMaxValue().intValue() * 1000)
                            videoView.seekTo(rangeSeekBar.getSelectedMinValue().intValue() * 1000);
                        handler.postDelayed(r, 1000);
                    }, 1000);

                });

            }
        }
    }

    private void executeSlowMotionVideoCommand() {

        try {
            outputFile = FileUtils.getPath(selectedFileUri);
//            String[] complexCommand = {"-y", "-i", inputFile.getAbsolutePath(),
//                    "-filter_complex",
//                    "[0:v]setpts=2.0*PTS[v];[0:a]atempo=0.5[a]", "-map", "[v]",
//                    "-map", "[a]", "-b:v", "2097k", "-r", "60", "-vcodec", "mpeg4", outputFile.getAbsolutePath()};
//
//
//            execFFmpegBinary(complexCommand);
        } catch (IOException e) {
            Log.d(TAG, "executeSlowMotionVideoCommand: ");
            e.printStackTrace();
        }


    }

    /**
     * Load FFmpeg binary
     */
    private void loadFFMpegBinary() {
        try {
            if (ffmpeg == null) {
                Log.d(TAG, "ffmpeg : era nulo");
                ffmpeg = FFmpeg.getInstance(this);
            }
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    showUnsupportedExceptionDialog();
                }

                @Override
                public void onSuccess() {
                    Log.d(TAG, "ffmpeg : correct Loaded");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            showUnsupportedExceptionDialog();
        } catch (Exception e) {
            Log.d(TAG, "EXception no controlada : " + e);
        }
    }

    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Not Supported")
                .setMessage("Device Not Supported")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.finish();
                    }
                })
                .create()
                .show();

    }

    /**
     * Executing ffmpeg binary
     */
    private void execFFmpegBinary(final String[] command) {
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Log.d(TAG, "FAILED with output : " + s);
                }

                @Override
                public void onSuccess(String s) {
                    Log.d(TAG, "SUCCESS with output : " + s);
                    if (choice == 1 || choice == 2 || choice == 5 || choice == 6 || choice == 7) {
                        Log.d(TAG, "onSuccess: 7");
//                        Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
//                        intent.putExtra(FILEPATH, filePath);
//                        startActivity(intent);
                    } else if (choice == 3) {
                        Log.d(TAG, "onSuccess: 3");

//                        Intent intent = new Intent(MainActivity.this, PreviewImageActivity.class);
//                        intent.putExtra(FILEPATH, filePath);
//                        startActivity(intent);
                    } else if (choice == 4) {
                        Log.d(TAG, "onSuccess: 4");

//                        Intent intent = new Intent(MainActivity.this, AudioPreviewActivity.class);
//                        intent.putExtra(FILEPATH, filePath);
//                        startActivity(intent);
                    }
                }

                @Override
                public void onProgress(String s) {
                    Log.d(TAG, "Started command : ffmpeg " + command);
                    progressDialog.setMessage("progress : " + s);
                    Log.d(TAG, "progress : " + s);
                }

                @Override
                public void onStart() {
                    Log.d(TAG, "Started command : ffmpeg " + command);
                    progressDialog.setMessage("Processing...");
                    progressDialog.show();
                }

                @Override
                public void onFinish() {
                    Log.d(TAG, "Finished command : ffmpeg " + command);
                    progressDialog.dismiss();

                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
            Log.e(TAG, "execFFmpegBinary: ", e);
        }
    }

    private String getTime(int seconds) {
        int hr = seconds / 3600;
        int rem = seconds % 3600;
        int mn = rem / 60;
        int sec = rem % 60;
        return String.format("%02d", hr) + ":" + String.format("%02d", mn) + ":" + String.format("%02d", sec);
    }
}