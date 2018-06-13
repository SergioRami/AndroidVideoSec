package ramar.videosec;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;

/**
 * Actividad de pantalla con la que se inicia la aplicacion y
 * elige a que actividad debe ir.
 */
public class SplashScreenActivity extends AppCompatActivity {

    /*String que guarda las preferencias compartidas*/

    public static final String PREFS_NAME = "MyPrefsFile";

    /* Delay que sufre la actividad cuando se ejecuta
    * La duracion son 2 segundos*/

    private static final int SPLASH_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
        }*/
        setContentView(R.layout.activity_splash_screen);

        /*
        * Recuperamos las preferencias compartidas por si el usuario hubiera guardado su email
        * */

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        final String email = settings.getString("email", null);

        final Context context = this;
        new Handler().postDelayed(new Runnable() {

            /*
            * Una vez inicie la aplicacion, si hubiera un email guardado, le llevamos a LoginActivity
            * sino, ira SignActivity
            * */

            @Override
            public void run() {
                if(email == null){
                    startActivity(new Intent(context, SignInActivity.class));
                }else{
                    Intent intent = new Intent(context, LoginActivity.class);
                    intent.putExtra(MainActivity.TAG_EMAIL, email);
                    startActivity(intent);
                }
            }
        },SPLASH_DELAY);


    }
}

