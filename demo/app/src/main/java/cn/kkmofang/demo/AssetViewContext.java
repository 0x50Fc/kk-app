package cn.kkmofang.demo;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import cn.kkmofang.script.ScriptContext;
import cn.kkmofang.view.IViewContext;
import cn.kkmofang.view.ImageCallback;
import cn.kkmofang.view.ImageStyle;
import cn.kkmofang.view.ImageTask;
import ua.anatolii.graphics.ninepatch.Div;
import ua.anatolii.graphics.ninepatch.NinePatchChunk;

/**
 * Created by hailong11 on 2018/3/21.
 */

public class AssetViewContext implements IViewContext {

    private final Context _context;
    private final AssetManager _asset;
    private final String _basePath;
    //支持的图片格式
    private static final String[] extArray = {".png", ".jpg", "bmp", ".gif", ".webp"};

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


        String path = _basePath + vs[0];

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

                    InputStream in = _asset.open(name);

                    try {

                        BitmapFactory.Options opt = new BitmapFactory.Options();
                        opt.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(in, null, opt);
                        int inSampleSize = 1;
                        if (style.height > 0 && style.width > 0){
                            inSampleSize = calcuteInsampleSize(opt, style.width, style.height);
                        }
                        opt.inSampleSize = inSampleSize;
                        opt.inJustDecodeBounds = false;

                        in = _asset.open(name);
                        Bitmap bitmap = BitmapFactory.decodeStream(in,null,opt);

                        if(style.capLeft >0 || style.capTop > 0) {

                            int capLeft = Math.round(style.capLeft * scale / inSampleSize);
                            int capTop = Math.round(style.capTop * scale / inSampleSize);

                            NinePatchChunk chunk = NinePatchChunk.createEmptyChunk();

                            chunk.xDivs.add(new Div(capLeft  ,capLeft + 1));
                            chunk.yDivs.add(new Div(capTop , capTop  + 1));

                            return new NinePatchDrawable(Resources.getSystem(),
                                    bitmap, chunk.toBytes(), chunk.padding, name);

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

    public int calcuteInsampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth){
            int halfWidth = width / 2;
            int halfHeight = height / 2;
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth){
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
