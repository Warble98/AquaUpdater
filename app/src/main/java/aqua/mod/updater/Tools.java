package aqua.mod.updater;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Tools {
    /**
     * No root access required
     * @param property some property from build.prop file
     * @return build.prop property
     */
    public static String getProperty(@NonNull String property){
        Process p;
        String board_platform = "";
        try {
            p = new ProcessBuilder("/system/bin/getprop", property).redirectErrorStream(true).start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line = br.readLine()) != null){
                board_platform = line;
            }
            p.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return board_platform;
    }

    public static void saveInfoToSharedPrefs(@NonNull String key, String value, Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        preferences.edit().putString(key, value).apply();
    }

    @Nullable
    public static String getInfoFromSharedPrefs(@NonNull String key, Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return preferences.getString(key, null);
    }

    /**
     * Unzip it
     * @param zipFileLoc input zip file
     * @param outputFolder zip file output folder
     * @return true if successful
     */
    public static boolean unZipIt(String zipFileLoc, String outputFolder){
        byte[] buffer = new byte[1024];
        try{
            File folder = new File(outputFolder);
            if(folder.exists())
                removeDirectory(folder);
            if(!folder.mkdirs()) {
                Log.e("unZipIt", "Cannot create folder");
                return false;
            }
            //get the zip file content
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFileLoc));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while(ze != null){
                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);
                System.out.println("file unzip : "+ newFile.getAbsoluteFile());
                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                File parent = new File(newFile.getParent());
                parent.mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            System.out.println("Done");
            return true;
        }catch(IOException ex){
            ex.printStackTrace();
            return false;
        }
    }

    public static Animation getRotateAnimation(){
        RotateAnimation rotate = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(1500);
        rotate.setRepeatCount(Animation.INFINITE);
        return rotate;
    }

    public static void removeDirectory(File file){
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                removeDirectory(f);
            }
        }
        file.delete();
    }

    public static boolean createDir(String loc){
        File dir = new File(loc);
        if (!dir.exists())
            return dir.mkdir();
        else
            return false;
    }
}
