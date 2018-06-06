package cn.kkmofang.app;

import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.MessageDigest;

import cn.kkmofang.image.ImageCache;

/**
 * Created by zhanghailong on 2018/3/21.
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

    public static void deleteDir(File file) {

        if(file.isDirectory()) {
            for(File f : file.listFiles()) {
                deleteDir(f);
            }
        }

        file.delete();
    }

    public static void mkdir(File file) {

        String v = file.getAbsolutePath();
        int i = v.lastIndexOf(File.separator);
        if(i > 0) {
            v = v.substring(0,i);
        }

        (new File(v)).mkdirs();
    }

    public static void digest(File file ,MessageDigest digest,byte[] data) {

        try {

            FileInputStream in = new FileInputStream(file);

            try {

                int n;

                while((n = in.read(data)) >0 ) {
                    digest.update(data,0,n);
                }

            }
            finally {
                in.close();
            }

        } catch (IOException e) {
            Log.d(Context.TAG,Log.getStackTraceString(e));
        }

    }

    public static void copy(File file,File tofile) {

        try {

            FileInputStream in = new FileInputStream(file);

            try {

                FileOutputStream out = new FileOutputStream(tofile);

                try {
                    byte[] data = new byte[20480];
                    int n;

                    while((n = in.read(data)) >0 ) {
                        out.write(data,0,n);
                    }
                }
                finally {
                    out.close();
                }
            }
            finally {
                in.close();
            }

        } catch (IOException e) {
            Log.d(Context.TAG,Log.getStackTraceString(e));
        }

    }

    public static void setContent(File file,String content) {

        try {

            FileOutputStream out = new FileOutputStream(file);

            try {

                if(content != null) {
                    out.write(content.getBytes(UTF8));
                }

            }
            finally {
                out.close();
            }

        } catch (IOException e) {
            Log.d(Context.TAG,Log.getStackTraceString(e));
        }

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

    @Override
    public Drawable getDrawable(String name) {
        return ImageCache.main.getImage((new File(_basePath,name)));
    }

}
