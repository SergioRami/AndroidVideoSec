package ramar.videosec;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.Bidi;
import java.util.Date;

public class BidiActivity extends Activity {


    public static final String TAG_EMAIL = "EMAIL";
    private UserBidiAsyncTask mBidiTask = null;
    private static final Integer REQUEST_CAM_CODE = 1;
    private String mEmail;
    private String mBidi;
    private Button mButton;
    private static String contents;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bidi);

        Intent intent = getIntent();
        mEmail = intent.getStringExtra(TAG_EMAIL);
        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                try {
                    Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                    intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                    startActivityForResult(intent, REQUEST_CAM_CODE);
                } catch (Exception e){
                    Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
                    Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
                    startActivity(marketIntent);
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_CAM_CODE) {
            if (requestCode == REQUEST_CAM_CODE ) {
                if (resultCode == RESULT_OK) {
                    String contents = intent.getStringExtra("SCAN_RESULT");
                    mBidi = contents;
                    Log.d("SCAN",mBidi);
                    if (mBidiTask != null && mBidiTask.getStatus() == UserBidiAsyncTask.Status.RUNNING) {
                        return;
                    } else {
                        mBidiTask = new UserBidiAsyncTask(this, mBidi, mEmail);
                        mBidiTask.execute((Void) null);
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    Toast toast1 =
                            Toast.makeText(getApplicationContext(),
                                    R.string.action_scan_error, Toast.LENGTH_SHORT);

                    toast1.show();
                    return;
                }
            }
        }
    }

}