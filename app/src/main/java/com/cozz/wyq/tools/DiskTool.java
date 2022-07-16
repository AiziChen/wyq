package com.cozz.wyq.tools;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class DiskTool {
    private static final String APP_BASE_DIR = "wyq";

    public static String getConfigPath() {
        File dir = Environment.getExternalStorageDirectory();
        return dir.getAbsolutePath() + "/" + APP_BASE_DIR;
    }

    public static void appendFile(String text, String path) {
        String configPath = getConfigPath();
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            configFile.mkdirs();
        }
        File urlFile = new File(configPath + "/" + path);
        if (!urlFile.exists()) {
            try {
                urlFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileWriter fw = new FileWriter(urlFile, true);
            fw.write(text);
            fw.write("\r\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean replaceFile(String text, String path) {
        String configPath = getConfigPath();
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            configFile.mkdirs();
        }
        File urlFile = new File(configPath + "/" + path);
        if (!urlFile.exists()) {
            try {
                urlFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        try {
            FileWriter fw = new FileWriter(urlFile);
            fw.write(text);
            fw.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String readFile(String path) {
        String configPath = getConfigPath();
        File urlFile = new File(configPath + "/" + path);
        if (!urlFile.exists()) {
            return "";
        }
        try {
            FileInputStream fis = new FileInputStream(urlFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            br.close();
            fis.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String readFirstLine(String path) {
        String configPath = getConfigPath();
        File urlFile = new File(configPath + "/" + path);
        if (!urlFile.exists()) {
            return "";
        }
        try {
            FileInputStream fis = new FileInputStream(urlFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
            String url = br.readLine();
            br.close();
            fis.close();
            return url;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
