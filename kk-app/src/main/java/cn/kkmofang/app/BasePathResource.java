package cn.kkmofang.app;

import android.graphics.drawable.Drawable;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zhanghailong on 2018/4/8.
 */

public class BasePathResource implements IResource{

    private final IResource _parent;
    private final String _basePath;

    public BasePathResource(IResource parent,String basePath) {
        _parent = parent;
        _basePath = basePath.endsWith("/") ? basePath : basePath + "/";
    }

    public String path(String name) {
        if(_basePath == null || "".equals(_basePath)) {
            return name;
        } else if(name.startsWith("./")) {
            return _basePath + name.substring(2);
        }
        return _basePath + name;
    }

    @Override
    public String getAbsolutePath(String name) {
        return _parent.getAbsolutePath(path(name));
    }

    @Override
    public String getString(String name) {
        return _parent.getString(path(name));
    }

    @Override
    public InputStream open(String name) throws IOException {
        return _parent.open(path(name));
    }

    @Override
    public Drawable getDrawable(String name) {
        return _parent.getDrawable(path(name));
    }
}
