package ramar.videosec;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Date;

public class CodeActivity extends AppCompatActivity {


    public static final String TAG_EMAIL = "EMAIL";
    /**
     * Actividad para autenticarse mediante un codigo enviado al email
     */
    private UserCodeAsyncTask mAuthTask = null;
    private UserNewCodeAsyncTask mNewCodeTask = null;

    private EditText mCodeView;
    private View mProgressView;
    private View mLoginFormView;
    private Button mValidateButton;
    private Button mNewCodeButton;
    private Date mLastExecution;
    private String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code);

        Intent intent = getIntent();
        mEmail = intent.getStringExtra(TAG_EMAIL);
        mCodeView = (EditText) findViewById(R.id.code);
        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        mValidateButton = (Button) findViewById(R.id.validate);
        mValidateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attempAuth();
            }
        });

        mNewCodeButton = (Button) findViewById(R.id.other_code);
        mNewCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendNewCode();
            }
        });


    }

    private void sendNewCode() {
        if (mAuthTask != null && mAuthTask.getStatus() == UserCodeAsyncTask.Status.RUNNING) {
            return;
        }

        if(mLastExecution == null){
            mLastExecution = new Date();
        }
        else if(new Date().getTime() - mLastExecution.getTime() < (15*60*1000)){
            Toast toast1 =
                    Toast.makeText(getApplicationContext(),
                            "Espera 15 minutos para recibir un nuevo código", Toast.LENGTH_SHORT);

            toast1.show();
            return;
        }

        showProgress(true);
        mNewCodeTask = new UserNewCodeAsyncTask(this, mEmail, mProgressView, mLoginFormView);
        mNewCodeTask.execute((Void) null);
    }

    /**
     * Intento de validar el codigo
     */
    private void attempAuth() {
        if (mAuthTask != null && mAuthTask.getStatus() == UserCodeAsyncTask.Status.RUNNING) {
            return;
        }

        // Reseteamos los errores
        mCodeView.setError(null);

        // Guardamos el valor del codigo
        String code = mCodeView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Comprobamos si no es vacio la vista
        if (TextUtils.isEmpty(code)) {
            mCodeView.setError(getString(R.string.error_field_required));
            focusView = mCodeView;
            cancel = true;
        }

        if (cancel) {

            focusView.requestFocus();
        } else {
            showProgress(true);
            mAuthTask = new UserCodeAsyncTask(this, mEmail, code, mCodeView, mProgressView, mLoginFormView);
            mAuthTask.execute((Void) null);
        }
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
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

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
