package ramar.videosec;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by crafter on 9/03/16.
 */
public class UserBidiAsyncTask extends AsyncTask<Void, Void, Integer> {

    private Context mContext;
    private String mBidi;
    private String mEmail;

    UserBidiAsyncTask(Context context, String bidi, String email) {
        mContext = context;
        mBidi = bidi;
        mEmail = email;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        HttpsURLConnection conex = null;
        try {
            conex = Internet.http(mContext, "/bidi");
            conex.setRequestMethod("POST");
            conex.setDoInput(true);

            HashMap<String, String> param = new HashMap<>();
            param.put("email",mEmail);
            param.put("bidi", mBidi);

            Internet.URLEncoder(param,conex);

            return conex.getResponseCode();
        } catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            e.printStackTrace();
        } finally{
            if(conex != null) {
                conex.disconnect();
            }
        }
        return -1;
    }

    protected void onPostExecute(Integer responseCode) {

        if (responseCode == HttpURLConnection.HTTP_OK) {
            /*Intent intent = new Intent(mContext, MainActivity.class);
            intent.putExtra(MainActivity.TAG_EMAIL, mEmail);
            mContext.startActivity(intent);*/
            Intent intent = new Intent(mContext, CameraActivity.class);
            intent.putExtra(MainActivity.TAG_EMAIL, mEmail);
            mContext.startActivity(intent);

        }else{
            Toast toast1 =
                    Toast.makeText(mContext,
                            R.string.action_bidi_error, Toast.LENGTH_SHORT);

            toast1.show();
            Intent intent = new Intent(mContext, BidiActivity.class);
            mContext.startActivity(intent);
        }
    }


}

