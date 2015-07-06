package org.getalp.lexsema.util.rest;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;

public class RestfulQuery {


    public static URLConnection restfulQuery(String uri, Map<String, String> parameters) throws IOException {

        StringBuilder params = new StringBuilder();
        if (!parameters.isEmpty()) {
            params.append("?");
            boolean first= true;
            for (String key : parameters.keySet()) {
                if(!first){
                    params.append("&");
                } else {
                    first = false;
                }
                params.append(key).append("=").append(URLEncoder.encode(parameters.get(key).trim(),"UTF-8"));
            }
        }
        URL url = new URL(uri+params);
        //make connection
        URLConnection urlc = url.openConnection();
        urlc.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        return urlc;
    }

    public static String getRequestOutput(URLConnection urlConnection) throws IOException{
        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection
                .getInputStream()));
        String l = br.readLine();
        String output = "";
        while (l != null) {
            output +=l;
            l = br.readLine();
        }
        br.close();
        return output.trim();
    }
}