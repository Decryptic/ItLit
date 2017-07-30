package io.itlit.ItLit;

import java.net.*;
import java.io.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Network {
    private static String server = "https://www.itlit.io";
    private static String path(String end) {
        return server + end;
    }

    public static String login(String json)      { return post(json, path("/login")); }
    public static String logout(String json)     { return post(json, path("/logout")); }
    public static String register(String json)   { return post(json, path("/register")); }
    public static String activate(String json)   { return post(json, path("/activate")); }
    public static String light(String json)      { return post(json, path("/light")); }
    public static String statusget(String json)  { return post(json, path("/statusget")); }
    public static String status(String json)     { return post(json, path("/status")); }
    public static String move(String json)       { return post(json, path("/move")); }
    public static String delfriend(String json)  { return post(json, path("/delfriend")); }
    public static String setfriends(String json) { return post(json, path("/setfriends")); }
    public static String getfriends(String json) { return post(json, path("/getfriends")); }
    public static String setfriend(String json)  { return post(json, path("/setfriend")); }
    public static String getlit(String json)     { return post(json, path("/getlit")); }
    public static Bitmap getpic(String json)     { return getpicaux(json, path("/getpic")); }
    public static String setpic(String json, Bitmap b) { return setpicaux(json, b, path("/setpic")); }

    private static Bitmap getpicaux(String json, String host) {
        try {
            URL url = new URL(host);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(3000);
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/octet-stream");
            con.setRequestMethod("POST");
            OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
            out.write(json);
            out.flush();
            out.close();

            int code = con.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                return BitmapFactory.decodeStream(con.getInputStream());
            } else throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
            return Const.nullpic;
        }
    }

    private static String setpicaux(String json, Bitmap bm, String host) {
        try {
            URL url = new URL(host);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setConnectTimeout(3000);
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setUseCaches(false);
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Cache-Control", "no-cache");
            con.setRequestProperty("Accept", "application/json");

            String border = "--- BUILD A WALL ---";
            con.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + border);
            DataOutputStream out = new DataOutputStream(con.getOutputStream());

            out.writeBytes("--" + border + "\r\n");
            out.writeBytes("Content-Disposition: form-data; name=\"uname\"\r\n\r\n");
            out.writeBytes(Const.uname);

            out.writeBytes("\r\n--" + border + "\r\n");
            out.writeBytes("Content-Disposition: form-data; name=\"passwd\"\r\n\r\n");
            out.writeBytes(Const.passwd);

            out.writeBytes("\r\n--" + border + "\r\n");
            out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + Const.uname + ".png\"\r\n");
            out.writeBytes("Content-Type: application/octet-stream\r\n\r\n");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
            out.write(stream.toByteArray());
            out.writeBytes("\r\n--" + border + "--\r\n");
            out.flush();
            out.close();

            StringBuilder sb = new StringBuilder();
            int code = con.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), "utf-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                return sb.toString();
            }
            else {
                return "{\"error\":\"Response code " + code + "\"}";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\":\"Please set picture again later\"}";
        }
    }

    private static String post(String json, String host) {
        try {
            URL url = new URL(host);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setConnectTimeout(4000);
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            con.setRequestMethod("POST");
            OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
            out.write(json);
            out.flush();

            StringBuilder sb = new StringBuilder();
            int code = con.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), "utf-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                return sb.toString();
            }
            else {
                return "{\"error\":\"Response code " + code + "\"}";
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return "{\"error\":\"Please try again later\"}";
        }
    }
}
