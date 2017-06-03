package aqua.mod.updater;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InstallActivity extends AppCompatActivity {

    TextView output;
    String fileLocation;
    String newFolderLocation;  // Folder with extracted files
    Handler mHandler;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install);
        mHandler = new Handler(Looper.getMainLooper());
        fileLocation = getIntent().getStringExtra("fileLocation");
        newFolderLocation = Environment.getExternalStorageDirectory().getPath() + "/Aqua/update/";
        output = (TextView) findViewById(R.id.output);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        startInstalling();
    }

    private void startInstalling(){
        File newFolder = new File(newFolderLocation);
        if (newFolder.exists())
            Tools.removeDirectory(newFolder);
        if (!newFolder.mkdirs()) {
            Log.e("startInstalling()", "Cannot create folder");
            return;
        }
        output.setText("Start\n");
        new Thread(()->{
            try {
                mHandler.post(()->{
                    output.setText(output.getText() + "Unzipping update file" + "\n");
                });
                ZipUtils.extract(new File(fileLocation), newFolder);
                //String script = readScriptFromFile(new File(newFolderLocation + "script.sh"));
                executeScript(newFolderLocation + "script.sh");
            } catch (IOException ex) {
                Log.e(InstallActivity.class.getName(), ex.toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void executeScript(String scriptLocation) throws IOException, InterruptedException {
        mHandler.post(()->{
            output.setText(output.getText() + "Running install script" + "\n");
        });
        Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "/system/bin/sh", scriptLocation});
        try(DataOutputStream outputStream = new DataOutputStream(p.getOutputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            mHandler.post(()->{
                output.setText(output.getText() + "Please wait..." + "\n");
            });
            // Grab the results
            StringBuilder log = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line).append("\n");
            }
            mHandler.post(()->{
                output.setText(output.getText() + log.toString() + "\n");
                progressBar.setVisibility(View.GONE);
            });
            p.waitFor();
        }
    }
}
