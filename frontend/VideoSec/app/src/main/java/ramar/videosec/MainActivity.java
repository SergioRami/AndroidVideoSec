package ramar.videosec;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends Activity {

    public static final String TAG_EMAIL = "email";
    private String mEmail;
    private int mId;

    /*Parte relacionada con las notificaciones push y GCM*/

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;


    /*Parte del codigo que implementa la vista que hace posible la reproduccion de un
    * stream MJPEG proyecto */

    private MjpegView mjpegView = null;

    /*Parte del codigo que envia el audio al servidor*/

    private Socket mSocket;
    private AudioRecord mRecorder;
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private AudioTrack mAudioTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Intent intent = getIntent();
        mEmail = intent.getStringExtra(TAG_EMAIL);
        mId = intent.getIntExtra("id", 0);

        mjpegView = new MjpegView(this);
        setContentView(mjpegView);

        new DoRead(this).execute(Internet.SERVER_URL);

        try{
            IO.Options opts = new IO.Options();
            opts.forceNew = true;
            opts.reconnection = false;
            opts.sslContext = Internet.getSSLContext(this);
            opts.secure=true;
            mSocket = IO.socket(Internet.SERVER_URL,
                    opts);
            mSocket.connect();
            mSocket.on("audioserv", onAudio);
        } catch (URISyntaxException | IOException | NoSuchAlgorithmException | CertificateException | KeyManagementException | KeyStoreException e){
            e.printStackTrace();
        }
        startRecording();
        final int  bufferSize = AudioTrack.getMinBufferSize(RECORDER_SAMPLERATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                RECORDER_SAMPLERATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM);
        mAudioTrack.play();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
        mjpegView.stopPlayback();
    }

    public class DoRead extends AsyncTask<String, Void, MjpegInputStream> {

        private Context mContext;

        private DoRead(Context context){
            super();
            mContext = context;
        }

        protected MjpegInputStream doInBackground(String... path) {
            try {
                HttpsURLConnection conex = Internet.http(mContext, "/streaming");
                conex.setRequestMethod("POST");
                conex.setDoInput(true);

                HashMap<String, String> param = new HashMap<>();
                param.put("email", mEmail);
                param.put("id",Integer.toString(mId));

                Internet.URLEncoder(param, conex);

                if(conex.getResponseCode() != 200){
                    this.cancel(true);
                    return null;
                }
                return new MjpegInputStream((InputStream)conex.getContent());
            } catch (CertificateException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(MjpegInputStream result) {
            mjpegView.setSource(result);
            mjpegView.setDisplayMode(MjpegView.SIZE_BEST_FIT);
            mjpegView.showFps(true);
        }
    }

    private void startRecording() {
        final int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING);
        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING,
                bufferSize );
        mRecorder.startRecording();

        final Thread recordingThread = new Thread(new Runnable() {
            public void run() {
                byte buffer[] = new byte[bufferSize];
                while (mRecorder.read(buffer, 0, bufferSize) > 0) {
                    mSocket.emit("audio", buffer);
                }
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    private Emitter.Listener onAudio = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    byte[] buffer = (byte[]) args[0];
                    mAudioTrack.write(buffer, 0, buffer.length);
                }
            });
        }
    };

    /*Fin de la parte que envia el audio al servidor*/

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.are_you_sure)
                .setPositiveButton(android.R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                stopRecording();
                                finish();
                            }
                        })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }
}