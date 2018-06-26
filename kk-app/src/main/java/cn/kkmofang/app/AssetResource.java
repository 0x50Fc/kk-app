package cn.kkmofang.app;

import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import cn.kkmofang.image.ImageCache;

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
    public String getAbsolutePath(String name) {
        return "asset://" + name;
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

        if(name == null) {
            return null;
        }

        String key = "asset://" + name;

        Drawable v = ImageCache.main.getImage(key);

        if(v != null) {
            return v;
        }

        try {

            InputStream in = open(name);

            try {
                return ImageCache.main.getImage(in,key);
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
