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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.IOException;
import java.io.InputStream;

import cn.kkmofang.app.IResource;
import cn.kkmofang.app.NinePatchChunkUtil;
import cn.kkmofang.script.ScriptContext;
import cn.kkmofang.view.ImageCallback;
import cn.kkmofang.view.ImageStyle;
import cn.kkmofang.view.ImageTask;

/**
 * Created by hailong11 on 2018/3/21.
 */

public class ViewContext extends cn.kkmofang.app.ViewContext {


    //支持的图片格式
    private static final String[] extArray = {".png", ".jpg", "bmp", ".gif", ".webp"};

    public ViewContext(Context context, IResource resource) {
        super(context, resource);
    }

    @Override
    public ImageTask getImage(String url, ImageStyle style, final ImageCallback callback) {

        if (TextUtils.isEmpty(url) || url.startsWith("http://") || url.startsWith("https://")){
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

        if(vs.length > 1) {
            style.capLeft = (int) (ScriptContext.intValue(vs[1],0));
        }

        if(vs.length > 2) {
            style.capTop = (int) (ScriptContext.intValue(vs[2],0));
        }

        String ext = "";

        int i = path.lastIndexOf(".");

        if(i >=0){
            ext = path.substring(i);
            path = path.substring(0,i);
        }

        System.out.println(style.toString() + "   " + url);



        for (String mExt : extArray) {
            int scale = 3;
            while(scale > 0) {

                String name = scale > 1 ? path + "@" + scale + "x" + (TextUtils.isEmpty(ext)?mExt:ext) :
                        path  + (TextUtils.isEmpty(ext)?mExt:ext);

                try {

                    InputStream in = _resource.open(name);

                    try {

                        BitmapFactory.Options opt = new BitmapFactory.Options();

                        opt.inJustDecodeBounds = false;
                        opt.inDensity = scale;

                        Bitmap bitmap = BitmapFactory.decodeStream(in,null,opt);

                        if(style.capLeft >0 || style.capTop > 0) {

                            int capLeft = Math.round(style.capLeft * scale  );
                            int capTop = Math.round(style.capTop * scale );

                            byte[] chunk = NinePatchChunkUtil.getChunk(capLeft, capTop, 1);

                            return new NinePatchDrawable(Resources.getSystem(),
                                    bitmap, chunk, new Rect(), name);

                        } else {
                            return new BitmapDrawable(_context.getResources(),bitmap);
                        }
                    } finally {
                        try {
                            in.close();
                        } catch (IOException e) {}
                    }

                } catch (IOException e) {
                    scale --;
                }
            }
            if (!TextUtils.isEmpty(ext))break;
        }

        return null;
    }

}
