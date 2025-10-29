package com.example.imagedecoder.irimia.iulia;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;

import javax.net.ssl.HttpsURLConnection;

public class DownloadAsyncTaskImage extends AsyncTask<String, Void, String> {



    @Override
    protected String doInBackground(String... strings) {
        String urlStr = strings[0];
        HttpsURLConnection connection = null;
        String imgStr="";
        String lineFromFile = "";
        try {
            URL url = new URL(urlStr);
            connection = (HttpsURLConnection) url.openConnection();
            InputStream inputStream = connection.getInputStream();
            BufferedReader buf = new BufferedReader(new InputStreamReader(inputStream));

            while (lineFromFile != null) {
                lineFromFile = buf.readLine();

                if (lineFromFile != null)
                {
                    imgStr += lineFromFile;
                }
            }
            return imgStr;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
