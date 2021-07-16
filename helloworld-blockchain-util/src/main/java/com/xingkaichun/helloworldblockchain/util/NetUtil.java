package com.xingkaichun.helloworldblockchain.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 网络工具类
 *
 * @author 邢开春 409060350@qq.com
 */
public class NetUtil {

    public static String get(String requestUrl, String requestBody) throws IOException {
        OutputStreamWriter out = null;
        BufferedReader br = null;
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setReadTimeout(3000);
            connection.setConnectTimeout(3000);
            connection.connect();
            out = new OutputStreamWriter(connection.getOutputStream(),StandardCharsets.UTF_8);
            out.append(requestBody);
            out.flush();
            out.close();

            InputStream is;
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                is = connection.getInputStream();
            } else {
                is = connection.getErrorStream();
            }
            StringBuilder data = new StringBuilder();
            br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String line;
            while ( (line = br.readLine()) != null) {
                data.append(line);
            }
            return data.toString();
        } finally {
            try {
                if(out != null){
                    out.close();
                }
            } catch (IOException e) {
                LogUtil.error("close io failed.",e);
            }
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                LogUtil.error("close io failed.",e);
            }
        }
    }

}
