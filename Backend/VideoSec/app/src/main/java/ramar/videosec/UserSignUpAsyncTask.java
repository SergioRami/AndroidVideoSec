package ramar.videosec;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.AutoCompleteTextView;

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
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class UserSignUpAsyncTask extends AsyncTask<Void, Void, Boolean> {

    private Context mContext;
    private final String mUsername;
    private final String mEmail;
    private final String mPassword;
    private final AutoCompleteTextView mEmailView;
    private final View mProgressView;
    private final View mLoginFormView;

    UserSignUpAsyncTask(Context context,String username, String email, String password,AutoCompleteTextView emailView, View progressView, View loginView) {
        mContext = context;
        mUsername = username;
        mEmail = email;
        mPassword = password;
        mEmailView = emailView;
        mProgressView = progressView;
        mLoginFormView = loginView;
    }

    @Override
    protected void onPreExecute(){
        showProgress(true);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        HttpsURLConnection conex = null;
        try {
            InstanceID instanceID = InstanceID.getInstance(mContext);
            String token = instanceID.getToken(Internet.gcm_desfaultSenderID,
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            conex = Internet.http(mContext, "/signup");
            conex.setRequestMethod("POST");
            conex.setDoInput(true);

            HashMap<String, String> param = new HashMap<>();
            param.put("username",mUsername);
            param.put("email",mEmail);
            param.put("password",mPassword);
            if(checkPlayServices()) {
                param.put("token", token);
            }

            Internet.URLEncoder(param,conex);

            int responseCode=conex.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                return true;
            }else return false;
        } catch (CertificateException | KeyStoreException | NoSuchAlgorithmException | KeyManagementException | IOException e) {
            e.printStackTrace();
        } finally{
            if(conex != null) {
                conex.disconnect();
            }
        }
        return false;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        showProgress(false);

        if (success) {
            Intent intent = new Intent(mContext, CodeActivity.class);
            intent.putExtra(CodeActivity.TAG_EMAIL, mEmail);
            mContext.startActivity(intent);

        } else {
            mEmailView.setError(mContext.getString(R.string.error_incorrect_email));
            mEmailView.requestFocus();
        }
    }

    @Override
    protected void onCancelled() {
        showProgress(false);
    }

    /**
     * Shows the progress UI and hides the login form.
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

