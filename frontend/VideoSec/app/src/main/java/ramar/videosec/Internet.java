package ramar.videosec;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;


public class Internet {

    public final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public final static String SERVER_URL  = "https://piwapi.ddns.net:7777";
    public final static String gcm_desfaultSenderID = "552416609712";

    public static HttpsURLConnection http(Context context, String path) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, KeyManagementException {
        URL url = new URL(SERVER_URL+path);
        HttpsURLConnection conex = (HttpsURLConnection)url.openConnection();
        conex.setSSLSocketFactory(getSSLContext(context).getSocketFactory());
        conex.setConnectTimeout(50000);
        conex.setReadTimeout(50000);
        return conex;
    }

    public static void URLEncoder(HashMap<String, String> param, HttpURLConnection conex) throws IOException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : param.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        OutputStream os = conex.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        writer.write(result.toString());

        writer.flush();
        writer.close();
        os.close();
    }


    public static SSLContext getSSLContext(Context context) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException, IOException, CertificateException {
        char [] passphrase = "qm3f4t".toCharArray();
        KeyStore kstrust = KeyStore.getInstance("BKS");
        kstrust.load(context.getResources().openRawResource(R.raw.pstore), passphrase);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        tmf.init(kstrust);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());
        return sslContext;
    }

    public static String readString(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }
}
