package cn.kkmofang.demo;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.IOException;
import java.io.InputStream;

import cn.kkmofang.view.IViewContext;
import cn.kkmofang.view.ImageCallback;
import cn.kkmofang.view.ImageStyle;
import cn.kkmofang.view.ImageTask;
import cn.kkmofang.view.value.Pixel;

/**
 * Created by hailong11 on 2018/3/21.
 */

public class AssetViewContext implements IViewContext {

    private final Context _context;
    private final AssetManager _asset;
    private final String _basePath;

    public AssetViewContext(Context context,AssetManager asset,String basePath) {
        _context = context;
        _asset = asset;
        _basePath = basePath;
    }

    @Override
    public Context getContext() {
        return _context;
    }

    @Override
    public ImageTask getImage(String url, ImageStyle style, final ImageCallback callback) {

        Glide.with(_context)
                .load(url)
                .bitmapTransform(new RoundCornersTransformation(_context, style.radius,
                        RoundCornersTransformation.CornerType.ALL))
                .into(new SimpleTarget<GlideDrawable>() {
                    @Override
                    public void onResourceReady(GlideDrawable glideDrawable, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        callback.onImage(glideDrawable);
                    }
                });

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

        String path = _basePath + vs[0];

        String ext = "";

        int i = path.lastIndexOf(".");

        if(i >=0){
            ext = path.substring(i);
            path = path.substring(0,i);
        }

        int scale = 3;

        while(scale > 0) {

            String name = scale > 1 ? path + "@" + scale + "x" + ext : path  + ext;

            try {

                InputStream in = _asset.open(name);

                try {
                    return Drawable.createFromStream(in,path);
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
