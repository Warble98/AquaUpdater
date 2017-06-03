package aqua.mod.updater;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Handler mHandler;
    FloatingActionButton refreshButton;
    TextView isUpdateAvailable, newVersion,
            currentVersion, flymeVersion, androidVersion, lastCheck;
    CardView updateCard;
    static String update_aqua_version, update_aqua_changelog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler(Looper.getMainLooper());
        initViews();
        fillViews();
    }

    private void initViews() {
        refreshButton = (FloatingActionButton) findViewById(R.id.check_update_button);
        isUpdateAvailable = (TextView) findViewById(R.id.update_is_available);
        updateCard = (CardView) findViewById(R.id.updateCard);
        newVersion = (TextView) findViewById(R.id.newVersion);
        currentVersion = (TextView) findViewById(R.id.current_version);
        flymeVersion = (TextView) findViewById(R.id.flyme_version);
        androidVersion = (TextView) findViewById(R.id.android_version);
        lastCheck = (TextView) findViewById(R.id.last_checked_date);
    }

    private void fillViews() {
        currentVersion.setText(Tools.getProperty("ro.aqua.version"));  //TODO  Tools.getProperty("ro.aqua.version")
        flymeVersion.setText(Tools.getProperty("ro.build.display.id"));
        androidVersion.setText(String.format("Android %s", Tools.getProperty("ro.build.version.release")));

        checkUpdate();

        /*//check if we've already downloaded info about new version
        final String updateVersion = Tools.getInfoFromSharedPrefs("update_aqua_version", this);
        if (updateVersion != null){
            newVersion.setText(updateVersion);
            updateCard.setVisibility(View.VISIBLE);
            isUpdateAvailable.setText(R.string.update_available);
        }*/

        refreshButton.setOnClickListener(l ->{
            refreshButton.setAnimation(Tools.getRotateAnimation());
            checkUpdate();
        });

        updateCard.setOnClickListener(l ->{
            Intent intent = new Intent(this, DownloadActivity.class);
            intent.putExtra("update_aqua_version", update_aqua_version);
            intent.putExtra("update_aqua_changelog", update_aqua_changelog);
            startActivity(intent);
        });
    }

    private void checkUpdate(){
        DateFormat df = new SimpleDateFormat("dd-MM-yyy HH:mm", Locale.getDefault());
        String now = df.format(new Date());
        lastCheck.setText(now);
        new Thread(()->{
            String versionURL = getString(R.string.version_file_url);
            boolean updateAvailable = VersionLoader.checkUpdate(currentVersion.getText().toString(), versionURL);
            mHandler.post(()->{
                refreshButton.setAnimation(null);
                if (updateAvailable){
                    isUpdateAvailable.setText(R.string.update_available);
                    newVersion.setText(VersionLoader.lastVersion);
                    updateCard.setVisibility(View.VISIBLE);
                    update_aqua_version = VersionLoader.lastVersion;
                    update_aqua_changelog = VersionLoader.getParsedChangelogForVersion(
                            VersionLoader.lastVersionFile, VersionLoader.lastVersion);
                }else {
                    isUpdateAvailable.setText(R.string.update_not_available);
                    updateCard.setVisibility(View.GONE);
                }
            });
        }).start();
    }
}