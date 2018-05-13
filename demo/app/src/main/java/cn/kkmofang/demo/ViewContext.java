package cn.kkmofang.demo;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.IOException;
import java.io.InputStream;

import cn.kkmofang.app.IResource;
import cn.kkmofang.app.NinePatchChunkUtil;
import cn.kkmofang.image.Image;
import cn.kkmofang.image.ImageStyle;
import cn.kkmofang.script.ScriptContext;
import cn.kkmofang.view.ImageCallback;
import cn.kkmofang.view.ImageTask;

/**
 * Created by zhanghailong on 2018/3/21.
 */

public class ViewContext extends cn.kkmofang.app.ViewContext {

    public ViewContext(Context context, IResource resource) {
        super(context, resource);
    }

    @Override
    public ImageTask getImage(String url, final ImageStyle style, final ImageCallback callback) {

        if (TextUtils.isEmpty(url) || url.startsWith("http://") || url.startsWith("https://")){
            Glide.with(_context)
                    .load(url)
                    .into(new SimpleTarget<GlideDrawable>() {
                        @Override
                        public void onResourceReady(final GlideDrawable glideDrawable, GlideAnimation<? super GlideDrawable> glideAnimation) {

                            if(glideDrawable instanceof GlideBitmapDrawable) {
                                callback.onImage(new Image(new Image.BitmapProvider() {
                                    @Override
                                    public Bitmap getBitmap() {
                                        return ((GlideBitmapDrawable) glideDrawable).getBitmap();
                                    }
                                }, style));
                            } else {
                                callback.onImage(glideDrawable);
                            }
                        }
                    });
        }else {
            callback.onImage(getImage(url, style));
        }

        return null;
    }

    @Override
    public Drawable getImage(String url, ImageStyle style) {

        if(url == null) {
            return null;
        }

        if(url.startsWith("http://") || url.startsWith("https://")) {
            return null;
        }

        String[] vs = url.split(" ");

        String path = vs[0];

        String ext = "";

        int i = path.lastIndexOf(".");

        if(i >=0){
            ext = path.substring(i);
            path = path.substring(0,i);
        }

        int capLeft = 0;
        int capTop = 0;

        if(vs.length  ==3) {
            capLeft = ScriptContext.intValue(vs[1],0);
            capTop = ScriptContext.intValue(vs[2],0);
        }

        int scale = 3;

        while(scale > 0) {

            String name = scale > 1 ? path + "@" + scale + "x" + ext :
                    path  + ext;

            try {

                InputStream in = _resource.open(name);

                try {

                    final Drawable v = Drawable.createFromStream(in,name);

                    if(v instanceof BitmapDrawable) {

                        style.capLeft = capLeft * scale;
                        style.capTop = capTop * scale;
                        style.capSize = scale;
                        style.scale = scale;

                        return new Image(new Image.BitmapProvider() {
                            @Override
                            public Bitmap getBitmap() {
                                return ((BitmapDrawable) v).getBitmap();
                            }
                        }, style);
                    }

                    return null;

                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {}
                }

            } catch (IOException e) {
                scale --;
            }
        }

        return null;
    }

}
