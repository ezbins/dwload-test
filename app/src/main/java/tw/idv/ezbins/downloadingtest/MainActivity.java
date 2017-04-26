package tw.idv.ezbins.downloadingtest;

import android.app.Activity;
import android.app.DownloadManager;
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
   /* public void dwload(View v) {
        *//*Uri audioPath = Uri.parse(file_localtion);
        fileDownLoadId = DownloadData();*//*
        upToQuery = new UpToQuery();
        upToQuery.execute("http://192.168.1.172:8080/iRobot/first");
    }*/

    private long DownloadData(String url) {
        Uri audioPath = Uri.parse(url);
        long downloadReference;
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
        String str = String.valueOf(downloadReference);
        Toast.makeText(this,str,Toast.LENGTH_LONG).show();
        return downloadReference;
    }

    private void DownloadStatus(Cursor cursor ,  long downloadId) {
        //column for download  status
        int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
        int status = cursor.getInt(columnIndex);
        //column for reason code if the download failed or paused
        int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
        int reason = cursor.getInt(columnReason);
        //get the download filename
        int filenameIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
        String filename = cursor.getString(filenameIndex);

        String statusText="";
        String reasonText="";

        switch(status){
            case DownloadManager.STATUS_FAILED:
                statusText = "STATUS_FAILED";
                switch(reason) {
                    case DownloadManager.ERROR_CANNOT_RESUME:
                        reasonText = "ERROR_CANNOT_RESUME";
                        Toast.makeText(getApplicationContext(),reasonText,Toast.LENGTH_LONG).show();
                        break;
                    case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                        reasonText = "ERROR_DEVICE_NOT_FOUND";
                        Toast.makeText(getApplicationContext(),reasonText,Toast.LENGTH_LONG).show();
                        break;
                    case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                        reasonText = "ERROR_FILE_ALREADY_EXISTS";
                        Toast.makeText(getApplicationContext(),reasonText,Toast.LENGTH_LONG).show();
                        break;
                    case DownloadManager.ERROR_FILE_ERROR:
                        reasonText = "ERROR_FILE_ERROR";
                        break;
                    case DownloadManager.ERROR_HTTP_DATA_ERROR:
                        reasonText = "ERROR_HTTP_DATA_ERROR";
                        break;
                    case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                        reasonText = "ERROR_INSUFFICIENT_SPACE";
                        break;
                    case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                        reasonText = "ERROR_TOO_MANY_REDIRECTS";
                        break;
                    case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                        reasonText = "ERROR_UNHANDLED_HTTP_CODE";
                        break;
                    case DownloadManager.ERROR_UNKNOWN:
                        reasonText = "ERROR_UNKNOWN";
                        Toast.makeText(getApplicationContext(),reasonText,Toast.LENGTH_LONG).show();
                        break;
                }
            case DownloadManager.STATUS_PAUSED:
                statusText="Status_Paused";
                switch(reason){
                    case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                        reasonText = "PAUSED_QUEUED_FOR_WIFI";
                        Toast.makeText(getApplicationContext(),reasonText,Toast.LENGTH_LONG).show();
                        break;
                    case DownloadManager.PAUSED_UNKNOWN:
                        reasonText = "PAUSED_UNKNOWN";
                        break;
                    case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                        reasonText = "PAUSED_WAITING_FOR_NETWORK";
                        Toast.makeText(getApplicationContext(),reasonText,Toast.LENGTH_LONG).show();
                        break;
                    case DownloadManager.PAUSED_WAITING_TO_RETRY:
                        reasonText = "PAUSED_WAITING_TO_RETRY";
                        break;
                }
                break;
                    case DownloadManager.STATUS_PENDING:
                        statusText = "STATUS_PENDING";
                        break;
                    case DownloadManager.STATUS_RUNNING:
                        statusText = "STATUS_RUNNING";
                        break;
                    case DownloadManager.STATUS_SUCCESSFUL:
                        statusText = "STATUS_SUCCESSFUL";
                        reasonText = "Filename:\n" + filename;
                        break;
        }
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
            }else {
                Toast.makeText(getApplicationContext(),"Not Download yet",Toast.LENGTH_LONG).show();
            }
        }
    }
}
