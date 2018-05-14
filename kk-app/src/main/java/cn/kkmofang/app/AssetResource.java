package cn.kkmofang.app;

import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zhanghailong on 2018/3/21.
 */

public class AssetResource implements IResource{

    private final String _basePath;
    private final AssetManager _asset;

    public String basePath() {
        return _basePath;
    }

    public AssetResource(AssetManager asset,String basePath) {
        _asset = asset;
        _basePath = basePath;
    }

    @Override
    public String getString(String name) {

        try {
            InputStream in = open(name);
            try {
                return FileResource.getString(in);
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
        String path;
        if(name.startsWith("./")) {
            path = _basePath + name.substring(2);
        } else {
            path = _basePath + name;
        }
        return _asset.open(path);
    }

    @Override
    public Drawable getDrawable(String name) {

        try {

            InputStream in = open(name);

            try {
                return Drawable.createFromStream(in,"asset://" + name);
            }
            finally {
                in.close();
            }
        } catch (IOException e) {
            Log.d(Context.TAG,Log.getStackTraceString(e));
        }
        return null;
    }

}
