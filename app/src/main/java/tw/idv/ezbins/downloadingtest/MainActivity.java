package tw.idv.ezbins.downloadingtest;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity  {
    private DownloadManager downloadManager;
    private long fileDownLoadId;
    private File direct;
    private URL url;
    private HttpURLConnection conn;
    private UpToQuery upToQuery;
    private BufferedReader reader;
    private StringBuilder strBuilder;
    private BroadcastReceiver dwFinishedReceive;
    private Button btn;
    JSONObject outPutData;
    JSONObject inPutData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button) findViewById(R.id.dw_btn);
    }

    @Override
    protected void onStart() {
        super.onStart();
        btn.setOnClickListener(dwListener);
    }
    View.OnClickListener dwListener = (new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            upToQuery = new UpToQuery();
            upToQuery.execute("http://192.168.1.172:8080/iRobot/first");
        }
    });

    private void DownloadData(String url) {
        Uri audioPath = Uri.parse(url);
        final long downloadReference;
        direct = new File(Environment.getExternalStorageDirectory()
                + "/Download");
        if (!direct.exists()) {
            direct.mkdirs();
        }
        downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(audioPath);

        //Setting title of request
        request.setTitle("Data Download");

        //Setting description of request
        request.setDescription("Android Data download using DownloadManager.");

        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"pigs.mp3");

        //Enqueue download and save the referenceId
        downloadReference = downloadManager.enqueue(request);
        /*String str = String.valueOf(downloadReference);
        Toast.makeText(this,str,Toast.LENGTH_LONG).show();*/

        BroadcastReceiver dwFinishedReceive = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(downloadReference);
                    Cursor cursor =  downloadManager.query(query);
                    if(cursor.moveToFirst()) {
                        int index = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if(DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(index)) {
                            Toast.makeText(getApplicationContext(), "Download Finished", Toast.LENGTH_LONG).show();
                        }
                    }

                }
            }
        };
        registerReceiver(dwFinishedReceive,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private class UpToQuery extends AsyncTask<String,Void,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            outPutData = new JSONObject();
            strBuilder = new StringBuilder();
        }

        @Override
        protected String doInBackground(String... params) {
            String isUpdate="";
            String file_location="";
            try {
                String urlStr = params[0];
                url = new URL(urlStr);
                conn =(HttpURLConnection)url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                outPutData.put("request","data");
                OutputStreamWriter outputStream = new OutputStreamWriter(conn.getOutputStream());
                outputStream.write(outPutData.toString());
                outputStream.flush();
                outputStream.close();

                // response data from web
                InputStream is = conn.getInputStream();
                if (conn.getResponseCode() !=200) {
                    //trace http resonpse error
                    //conn.getErrorStream();
                 } else {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    while ( (line=reader.readLine())!=null){
                            strBuilder.append(line);
                    }
                    inPutData = new JSONObject(strBuilder.toString());
                    if (inPutData.getString("isUpdate").equals("yes")) {
                        file_location = inPutData.getString("file_location");
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return file_location;
        }

        @Override
        protected void onPostExecute(String  file_location) {
            super.onPostExecute(file_location);
            if (file_location.length()>0) {
                DownloadData(file_location);
                registerReceiver(dwFinishedReceive,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            }else {
                Toast.makeText(getApplicationContext(),"Non-downloadable",Toast.LENGTH_LONG).show();
            }
        }
    }
}
