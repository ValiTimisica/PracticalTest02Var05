package ro.pub.cs.systems.eim.practicaltest02.network;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;
import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.general.Utilities;
import ro.pub.cs.systems.eim.practicaltest02.model.WeatherForecastInformation;

public class CommunicationThread extends Thread {

    private ServerThread serverThread;
    private Socket socket;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                return;
            }
            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (city / information type!");
            String put_get_info = bufferedReader.readLine();
            if (put_get_info == null || put_get_info.isEmpty()) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (city / information type!");
                return;
            }
            HashMap<String, WeatherForecastInformation> data = serverThread.getData();
            WeatherForecastInformation weatherForecastInformation = null;

            String cheie = null;
            String valoare = null;
            String put_or_get = null;

            String[] tokens = put_get_info.split(",");
            put_or_get = tokens[0];
            cheie = tokens[1];
            if(tokens[2] != null) {
                valoare = tokens[1];
            }

            if (data.containsKey(cheie) && put_or_get.contains("put")) {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
                weatherForecastInformation = data.get(cheie);

                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(Constants.WEB_SERVICE_ADDRESS);
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("lat", "45"));
                params.add(new BasicNameValuePair("lng", "25"));
                params.add(new BasicNameValuePair("username", "eim2017"));

                UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                httpPost.setEntity(urlEncodedFormEntity);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String pageSourceCode = httpClient.execute(httpPost, responseHandler);

                String[] lines = pageSourceCode.split(System.getProperty("line.separator"));

                String final_hour = null;
                for(String l : lines) {
                    if (l.contains("time")) {
                        String dataToSplit = l;
                        String[] splitted = dataToSplit.split(" ");
                        String[] hour = splitted[0].split(":");
                        final_hour = hour[1];
                    }
                }

                int dif = Integer.parseInt(final_hour) - Integer.parseInt(weatherForecastInformation.getMinut());

                if(dif < 1) {
                    serverThread.setData(cheie, new WeatherForecastInformation(valoare, final_hour));
                }

            } else if (data.containsKey(cheie) && put_or_get.contains("get")) {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
                weatherForecastInformation = data.get(cheie);

                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(Constants.WEB_SERVICE_ADDRESS);
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("lat", "45"));
                params.add(new BasicNameValuePair("lng", "25"));
                params.add(new BasicNameValuePair("username", "eim2017"));

                UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                httpPost.setEntity(urlEncodedFormEntity);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                String pageSourceCode = httpClient.execute(httpPost, responseHandler);

                String[] lines = pageSourceCode.split(System.getProperty("line.separator"));

                String final_hour = null;
                for(String l : lines) {
                    if (l.contains("time")) {
                        String dataToSplit = l;
                        String[] splitted = dataToSplit.split(" ");
                        String[] hour = splitted[0].split(":");
                        final_hour = hour[1];
                    }
                }

                int dif = Integer.parseInt(final_hour) - Integer.parseInt(weatherForecastInformation.getMinut());

                WeatherForecastInformation info = new WeatherForecastInformation(valoare, final_hour);

                String result = null;

                if(dif < 1) {
                    serverThread.setData(cheie, info);
                    result = info.getName() + "\n";

                } else {
                    result = "none" + "\n";
                }

                printWriter.println(result);
                printWriter.flush();


            } else if (!data.containsKey(cheie) && put_or_get.contains("get")) {
                String result = "none" + "\n";
                printWriter.println(result);
                printWriter.flush();
            }
            if (weatherForecastInformation == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Weather Forecast Information is null!");
                return;
            }
            String result = weatherForecastInformation.toString();

            printWriter.println(result);
            printWriter.flush();
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }  finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }

}
