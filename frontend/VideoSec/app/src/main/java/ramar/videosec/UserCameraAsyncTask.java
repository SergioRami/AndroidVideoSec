package ramar.videosec;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by crafter on 22/05/16.
 */
public class UserCameraAsyncTask extends AsyncTask<Void, Void, List<Camera>> {

    private Context mContext;
    private CamerasAdapter mAdapter;
    private String mEmail;

    public UserCameraAsyncTask(Context context, CamerasAdapter adapter, String email) {
        mContext = context;
        mAdapter = adapter;
        mEmail = email;
    }

    @Override
    protected List<Camera> doInBackground(Void... params) {
        HttpsURLConnection conex = null;
        try {
            conex = Internet.http(mContext, "/cameras");
            conex.setRequestMethod("POST");
            conex.setDoInput(true);

            HashMap<String, String> param = new HashMap<>();
            param.put("email",mEmail);

            Internet.URLEncoder(param,conex);

            int responseCode=conex.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK){
                InputStream in = conex.getInputStream();
                List<Camera> list = new ArrayList<>();

                JSONArray json = new JSONArray(Internet.readString(in));

                for(int i = 0 ; i < json.length() ; i++){
                    Camera camera = new Camera(json.getJSONObject(i));
                    list.add(camera);
                }

                return list;
            }
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyManagementException | KeyStoreException | JSONException e) {
            e.printStackTrace();
        } finally{
            if(conex != null) {
                conex.disconnect();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(final List<Camera> list) {
        if (list != null) {
            mAdapter.addAll(list);
            mAdapter.notifyDataSetChanged();
        }
    }
}
