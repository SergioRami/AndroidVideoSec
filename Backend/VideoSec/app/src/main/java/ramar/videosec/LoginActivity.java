package ramar.videosec;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Actividad para iniciar sesion mediante email / password
 */
public class LoginActivity extends AppCompatActivity  {

    /**
     *Task para poder cancelar un intento de inicio de sesion por peticion
     */
    private UserLoginAsyncTask mAuthTask = null;

    private TextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Intent intent = getIntent();
        String email = intent.getStringExtra(MainActivity.TAG_EMAIL);

        // Iniciacion de los listeners
        mEmailView = (TextView) findViewById(R.id.email);
        mEmailView.setText(email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {

                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        //Boton que sirve para guardar las preferencias compartidas del email

        final Context context = this;
        Button mNotMeButton = (Button) findViewById(R.id.not_me_button);
        mNotMeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences settings = getSharedPreferences(SplashScreenActivity.PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.remove("email");
                editor.apply();
                startActivity(new Intent(context, SignInActivity.class));
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    private void attemptLogin() {
        if (mAuthTask != null && mAuthTask.getStatus() == UserLoginAsyncTask.Status.RUNNING){
            return;
        }

        // Reseteamos los errores en las vistas
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Guardamos los valores en el intento inicio de sesion
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Comprobamos que la password cumple los requisitos
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_incorrect_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Comprobamos que el email cumple los requisitos
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // Si hay un error, cancelas el intento de inicio de sesion y
            // centramos la vista en la vista con error
            focusView.requestFocus();
        } else {
            // Lanzamos la AsyncTask para el incio de sesion
            mAuthTask = new UserLoginAsyncTask(this, email, password,mPasswordView,mProgressView,mLoginFormView, false);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@") && (email.contains(".com") || email.contains(".es"));
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

}

