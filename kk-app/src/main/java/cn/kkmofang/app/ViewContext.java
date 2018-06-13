package cn.kkmofang.app;

import android.graphics.drawable.Drawable;

import java.io.File;

import cn.kkmofang.http.IHttpTask;
import cn.kkmofang.image.ImageStyle;
import cn.kkmofang.view.AudioElement;
import cn.kkmofang.view.AudioTask;
import cn.kkmofang.view.IViewContext;
import cn.kkmofang.view.ImageCallback;
import cn.kkmofang.view.ImageTask;

/**
 * Created by zhanghailong on 2018/4/8.
 */

public abstract class ViewContext implements IViewContext {

    protected final android.content.Context _context;
    protected final IResource _resource;

    public ViewContext(android.content.Context context,IResource resource) {
        _context = context;
        _resource = resource;
    }

    @Override
    public android.content.Context getContext() {
        return _context;
    }

    abstract public ImageTask getImage(String url, ImageStyle style, ImageCallback callback);

    abstract public Drawable getImage(String url, ImageStyle style);

    abstract public AudioTask downLoadFile(String url, AudioElement.IAudioLoadCallback callback);
}
