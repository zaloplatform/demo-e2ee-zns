/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import java.io.IOException;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 *
 * @author hienhh
 */
public class HttpRequestService {
    
    public static String getE2EEKey(String accessTok, String phone) throws IOException {
        HttpClient httpclient = HttpClientBuilder.create().build();

        HttpPost httppost = new HttpPost("https://business.openapi.zalo.me/e2ee/key");

        httppost.setHeader("access_token", accessTok);

        JSONObject body = new JSONObject();
        body.put("phone", phone);

        httppost.setEntity(new StringEntity(body.toString(), ContentType.APPLICATION_JSON));

        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        return EntityUtils.toString(entity); 
    }
    
    public static String sendE2EEZNSMessage(String accessTok, String phone, String templateId, Map<String, String> templateData) throws IOException {
        HttpClient httpclient = HttpClientBuilder.create().build();

        HttpPost httppost = new HttpPost("https://business.openapi.zalo.me/message/e2ee");

        httppost.setHeader("access_token", accessTok);
        
        JSONObject templateDataJson = new JSONObject();
        if (templateData.keySet().size() > 0){
            for (String key : templateData.keySet()){
                templateDataJson.put(key, templateData.get(key));
            }
        }

        JSONObject body = new JSONObject();
        body.put("phone", phone);
        body.put("template_id", templateId);
        body.put("template_data", templateDataJson);
        
        System.err.println(body.toString());

        httppost.setEntity(new StringEntity(body.toString(), ContentType.APPLICATION_JSON));

        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        return EntityUtils.toString(entity); 
    }
}
