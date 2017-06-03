package aqua.mod.updater;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.webkit.*;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class VersionLoader {
    static String lastVersionFile;
    static String lastVersion;

    // return true when update is available
    public static boolean checkUpdate(String currentVer, String versionURL){
        String phoneModel = Tools.getProperty("ro.meizu.carrier.model");
        versionURL = versionURL.replace("PHONE_MODEL", phoneModel);
        StringBuilder sb = new StringBuilder();
        try {
            ReadableByteChannel channel = Channels.newChannel(new URL(versionURL).openStream());
            ByteBuffer buf = ByteBuffer.allocate(48);
            while (channel.read(buf) > 0) {
                buf.flip();
                while (buf.hasRemaining()) {
                    sb.append((char) buf.get());
                }
                buf.clear();
            }
            channel.close();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        lastVersionFile = sb.toString();
        lastVersion = getParsedVersion(sb.toString());
        return !currentVer.equals(lastVersion);
    }

    public static String getParsedVersion(String s){
        if (s.startsWith("-version"))
            return s.substring(s.indexOf("-version") + 9, s.indexOf("\n"));
        return null;
    }

    public static String getParsedChangelogForVersion(String fullFile, String version){
        if (fullFile.startsWith("-version")) {
            int changelogStartIndex = fullFile.indexOf("-version" + version) + "-version ".length() + version.length() + 2 + "-changelog\n".length();
            int changelogEndIndex = fullFile.indexOf("-version", changelogStartIndex);
            if (changelogEndIndex == -1)
                changelogEndIndex = fullFile.length()-1;
            return fullFile.substring(changelogStartIndex, changelogEndIndex);
        }
        return null;
    }
}
