package cn.kkmofang.app;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Created by hailong11 on 2018/3/21.
 */

public class FileResource implements IResource{

    public final static Charset UTF8 = Charset.forName("UTF-8");

    private final File _basePath;

    public File getBasePath() {
        return _basePath;
    }

    public FileResource(File basePath) {
        _basePath = basePath;
    }

    public static String getString(File file) {

        try {
            FileInputStream in = new FileInputStream(file);
            try {
                return getString(in);
            }
            finally {
                in.close();
            }
        } catch (IOException e) {
            Log.d(Context.TAG,Log.getStackTraceString(e));
        }
        return null;
    }
    public static String getString(InputStream in) {

        if(in != null) {
            InputStreamReader rd = new InputStreamReader(in, UTF8);
            char[] data = new char[40960];
            StringBuffer sb = new StringBuffer();
            int n ;
            try {
                while((n = rd.read(data)) > 0) {
                    sb.append(data,0,n);
                }
            } catch (IOException e) {
                Log.d(Context.TAG,Log.getStackTraceString(e));
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.d(Context.TAG,Log.getStackTraceString(e));
                }
            }
            return sb.toString();
        }

        return null;
    }

    @Override
    public String getString(String name) {

        try {
            FileInputStream in = new FileInputStream(new File(_basePath,name));
            try {
                return getString(in);
            }
            finally {
                in.close();
            }
        } catch (IOException e) {
            Log.d(Context.TAG,Log.getStackTraceString(e));
        }

        return null;
    }

    @Override
    public InputStream open(String name) throws IOException {
        return new FileInputStream(new File(_basePath,name));
    }

}
