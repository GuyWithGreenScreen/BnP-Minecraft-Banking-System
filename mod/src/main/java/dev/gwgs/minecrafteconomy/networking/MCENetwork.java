package dev.gwgs.minecrafteconomy.networking;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MCENetwork {


    private static HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    private static String API_IP;
    private static String API_KEY;

    public static void setupClient(String IP, String KEY) {
        API_IP = IP;
        API_KEY = KEY;
    }

    public static String request(String json) throws IOException, InterruptedException {
        try {
            System.out.println("JAVA RAW JSON >>> " + json);

            Gson gson = new Gson();
            JsonObject request = gson.fromJson(json, JsonObject.class);

            request.getAsJsonObject("dat").addProperty("api_key", API_KEY);

            System.out.println("JAVA RAW JSON >>> " + request.toString());
            System.out.println("API KEY >>> " + API_KEY);
            System.out.println("API IP >>> " + API_IP);

            return client.send(HttpRequest.newBuilder().uri(URI.create(API_IP))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(request.toString()))
                    .build(), HttpResponse.BodyHandlers.ofString()).body();
        } catch (Exception e) {
            System.out.println("ERROR! : " + e);
            return null;
        }
    }
}