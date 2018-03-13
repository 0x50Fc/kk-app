package cn.kkmofang.app;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Created by hailong11 on 2018/3/13.
 */

public class ClassResource implements IResource {

    public final static Charset UTF8 = Charset.forName("UTF-8");

    private final Class<?> _clazz;

    public ClassResource(Class<?> clazz) {
        _clazz = clazz;
    }

    @Override
    public String getString(String name) {

        InputStream in = _clazz.getResourceAsStream(name);

        if(in != null) {
            InputStreamReader rd = new InputStreamReader(in, UTF8);
            char[] data = new char[40960];
            StringBuffer sb = new StringBuffer();
            int n ;
            try {
                while((n = rd.read()) > 0) {
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
}
