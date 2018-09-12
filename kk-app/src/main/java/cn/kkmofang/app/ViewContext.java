package cn.kkmofang.app;

import android.graphics.drawable.Drawable;

import cn.kkmofang.image.ImageStyle;
import cn.kkmofang.view.AbstractViewContext;
import cn.kkmofang.view.AudioElement;
import cn.kkmofang.view.AudioTask;
import cn.kkmofang.view.IViewContext;
import cn.kkmofang.view.ImageCallback;
import cn.kkmofang.view.ImageTask;

/**
 * Created by zhanghailong on 2018/4/8.
 */

public abstract class ViewContext extends AbstractViewContext implements IViewContext {

    protected final IResource _resource;

    public ViewContext(android.content.Context context,IResource resource) {
        super(context);
        _resource = resource;
    }

    public String getAbsolutePath(String path) {
        return _resource.getAbsolutePath(path);
    }

    abstract public ImageTask getImage(String url, ImageStyle style, ImageCallback callback);

    abstract public Drawable getImage(String url, ImageStyle style);

    abstract public AudioTask downLoadFile(String url, AudioElement.IAudioLoadCallback callback);
}
