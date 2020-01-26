package ramar.videosec;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

/**
 * AsyncTask que envia el nuevo código
 */
public class UserNewCodeAsyncTask extends AsyncTask<Void, Void, Boolean> {

    private Context mContext;
    private String mEmail;
    private final View mProgressView;
    private final View mLoginFormView;

    UserNewCodeAsyncTask(Context context, String email, View progressView, View loginView) {
        mContext = context;
        mEmail = email;
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
            conex = Internet.http(mContext, "/newcode");
            conex.setRequestMethod("PUT");
            conex.setDoInput(true);

            HashMap<String, String> param = new HashMap<>();
            param.put("email",mEmail);

            Internet.URLEncoder(param,conex);

            int responseCode=conex.getResponseCode();

            return responseCode == HttpsURLConnection.HTTP_OK;
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
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
            Toast toast1 =
                    Toast.makeText(mContext,
                            R.string.result_succeed_newcode, Toast.LENGTH_SHORT);

            toast1.show();

        } else {
            Toast toast1 =
                    Toast.makeText(mContext,
                            R.string.result_failed_newcode, Toast.LENGTH_SHORT);

            toast1.show();
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


}
