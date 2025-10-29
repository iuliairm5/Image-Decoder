package com.example.imagedecoder.irimia.iulia;

import android.os.AsyncTask;
import android.util.Base64;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

public class DownloadAsyncTask extends AsyncTask<String, Void, ByteBuffer> {


    @Override
    protected ByteBuffer doInBackground(String... strings) {
            String urlStr = strings[0];
            URLConnection connection = null;

            try {
            URL url = new URL(urlStr);
            connection = url.openConnection();
            connection.connect();
            InputStream inputStream = new BufferedInputStream(url.openStream());
            byte[] byteArrayFile = new byte[connection.getContentLength()]; //new byte array of size = the file size from the url
            inputStream.read(byteArrayFile); //write from file in the byte array
            ByteBuffer byteBufferFile = ByteBuffer.wrap(byteArrayFile); //from byte array to byte buffer (content is base64 encoded)

            return byteBufferFile;

            }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

}
