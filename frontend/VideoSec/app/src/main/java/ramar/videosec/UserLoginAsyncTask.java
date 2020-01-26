package ramar.videosec;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

/**
 * Tarea asíncrona para el intento de inicio de sesion
 */
public class UserLoginAsyncTask extends AsyncTask<Void, Void, Integer> {

    private Context mContext;
    private final String mEmail;
    private final String mPassword;
    private final EditText mPasswordView;
    private final View mProgressView;
    private final View mLoginFormView;
    private final boolean mRememberMe;

    UserLoginAsyncTask(Context context, String email, String password, EditText passwordView, View progressView, View loginView, boolean rememberMe) {
        mContext = context;
        mEmail = email;
        mPassword = password;
        mPasswordView = passwordView;
        mProgressView = progressView;
        mLoginFormView = loginView;
        mRememberMe = rememberMe;
    }

    @Override
    protected void onPreExecute(){
        showProgress(true);
    }

    @Override
    protected Integer doInBackground(Void... params) {
        HttpsURLConnection conex = null;
        try {
            InstanceID instanceID = InstanceID.getInstance(mContext);
            String token = instanceID.getToken(Internet.gcm_desfaultSenderID,
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            conex = Internet.http(mContext, "/login");
            conex.setRequestMethod("POST");
            conex.setDoInput(true);

            HashMap<String, String> param = new HashMap<>();
            param.put("email",mEmail);
            param.put("password", mPassword);
            if(checkPlayServices()) {
                param.put("token", token);
            }

            Internet.URLEncoder(param,conex);

            return conex.getResponseCode();
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
        } finally{
            if(conex != null) {
                conex.disconnect();
            }
        }
        return -1;
    }

    @Override
    protected void onPostExecute(Integer responseCode) {
        showProgress(false);

        if (responseCode == HttpsURLConnection.HTTP_OK) {
            if (mRememberMe) {
                SharedPreferences settings = mContext.getSharedPreferences(SplashScreenActivity.PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("email", mEmail);
                editor.commit();
            }
            /*Intent intent = new Intent(mContext, MainActivity.class);
            intent.putExtra(MainActivity.TAG_EMAIL, mEmail);
            mContext.startActivity(intent);*/
            Intent intent = new Intent(mContext, CameraActivity.class);
            intent.putExtra(MainActivity.TAG_EMAIL, mEmail);
            mContext.startActivity(intent);

        }else if(responseCode == HttpsURLConnection.HTTP_UNAUTHORIZED ){
            if (mRememberMe) {
                SharedPreferences settings = mContext.getSharedPreferences(SplashScreenActivity.PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("email", mEmail);
                editor.commit();
            }
            Intent intent = new Intent(mContext, CodeActivity.class);
            intent.putExtra(CodeActivity.TAG_EMAIL, mEmail);
            mContext.startActivity(intent);
        } else if(responseCode == HttpsURLConnection.HTTP_NOT_FOUND ){
            if (mRememberMe) {
                SharedPreferences settings = mContext.getSharedPreferences(SplashScreenActivity.PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("email", mEmail);
                editor.commit();
            }
            Intent intent = new Intent(mContext, BidiActivity.class);
            intent.putExtra(BidiActivity.TAG_EMAIL, mEmail);
            mContext.startActivity(intent);
        } else {
            mPasswordView.setError(mContext.getString(R.string.error_incorrect_password));
            mPasswordView.requestFocus();
        }
    }

    @Override
    protected void onCancelled() {
        showProgress(false);
    }

    /**
     * Shows the progress UI and hides the login form.
     * CODIGO AUTOGENERADO
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = mContext.getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(mContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog((Activity) mContext, resultCode, Internet.PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            }
            return false;
        }
        return true;
    }
}

