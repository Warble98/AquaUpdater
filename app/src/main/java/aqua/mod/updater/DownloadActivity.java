package aqua.mod.updater;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.File;
import java.util.Locale;

public class DownloadActivity extends AppCompatActivity {

    TextView changelog;
    FloatingActionButton actionButton;
    TextView newVersion;
    String fileName, fileLocation;
    long downloadId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        initToolbar();
        initViews();
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        //registerReceiver(onNotificationClick, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onComplete);
        //unregisterReceiver(onNotificationClick);
    }

    private void initViews() {
        newVersion = (TextView) findViewById(R.id.newVersion);
        newVersion.setText(getIntent().getStringExtra("update_aqua_version"));
        fileName = "aqua_" + newVersion.getText().toString().toLowerCase(Locale.getDefault()).replaceAll(" ", "_") + ".zip";
        fileLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/" + fileName;
        changelog = (TextView) findViewById(R.id.changelog);
        changelog.setText(getIntent().getStringExtra("update_aqua_changelog"));
        actionButton = (FloatingActionButton) findViewById(R.id.action_button);

        if (fileHasAlreadyDownloaded())
            showInstallButton();
        else
            showDownloadButton();
    }

    private void initToolbar(){
        ActionBar bar = getSupportActionBar();
        if (bar != null){
            bar.setDisplayShowTitleEnabled(false);
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setElevation(0);
        }
    }

    private void showDownloadButton(){
        actionButton.hide();
        actionButton.setImageResource(R.drawable.download);
        actionButton.setOnClickListener(l -> {
            actionButton.hide();
            startDownload();
            Snackbar.make(findViewById(R.id.main_layout), R.string.downloading, Snackbar.LENGTH_LONG)
                    .show();
        });
        actionButton.show();
    }

    private void showInstallButton(){
        actionButton.hide();
        actionButton.setImageResource(R.drawable.wrench);
        actionButton.setOnClickListener(l -> {
            Intent intent = new Intent(this, InstallActivity.class);
            intent.putExtra("fileLocation", fileLocation);
            startActivity(intent);
        });
        actionButton.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public BroadcastReceiver onComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) == downloadId)
                showInstallButton();
        }
    };

    public BroadcastReceiver onNotificationClick = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {}
    };

    private void startDownload() {
        String phoneModel = Tools.getProperty("ro.meizu.carrier.model");
        String downloadURL = getString(R.string.update_file_url).replace("PHONE_MODEL", phoneModel);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadURL));
        request.setDescription(getString(R.string.update_downloading));
        request.setTitle(fileName);
        request.allowScanningByMediaScanner();
        request.setVisibleInDownloadsUi(false);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        downloadId = manager.enqueue(request);
    }

    private boolean fileHasAlreadyDownloaded(){
        return new File(fileLocation).exists();
    }

}
