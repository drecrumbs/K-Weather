package com.kiss.www.kweather.Common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Dre on 12/24/2017.
 */

public class HttpHelper {
    private static String stream = null;

    public HttpHelper() {

    }

    public String getHTTPData(String urlString)
    {

        try{
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while((line = r.readLine())!=null)
                    sb.append(line);
                stream = sb.toString();
                urlConnection.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stream;
    }

}
