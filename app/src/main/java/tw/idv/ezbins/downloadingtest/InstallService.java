package tw.idv.ezbins.downloadingtest;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;

import java.io.File;


public class InstallService extends IntentService {
    private Handler handler;

    public InstallService() {
        super("InstallService");
    }

   /* @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler = new Handler();
        return super.onStartCommand(intent, flags, startId);

    }*/

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Intent instIntent = new Intent(Intent.ACTION_VIEW);
            instIntent.setAction(Intent.ACTION_INSTALL_PACKAGE);
            instIntent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() +"/Download/"+ "justJava.apk")),
                    "application/vnd.android.package-archive");
            instIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(instIntent);
        }
    }


}
