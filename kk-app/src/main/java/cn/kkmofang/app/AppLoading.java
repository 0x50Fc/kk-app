package cn.kkmofang.app;

import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cn.kkmofang.http.HttpOptions;
import cn.kkmofang.view.value.V;

/**
 * Created by zhanghailong on 2018/6/6.
 */

public class AppLoading {

    public final static Charset UTF8 = Charset.forName("UTF-8");

    public interface OnLoad {
        void onLoad(String url,String path,AppLoading loading);
    }

    public interface OnProgress {
        void onProgress(String url,String path,int count,int totalCount);
    }

    public interface OnError {
        void onError(String url,Exception ex);
    }

    public interface OnAppInfo {
        void onAppInfo(String url,Object appInfo);
    }

    public interface Http {
        void send(HttpOptions options);
        String encodeJSON(Object object);
        Object decodeJSON(String text);
    }

    private final Http _http;
    private final String _url;
    private final String _path;
    private final String _key;
    private final Handler _handler;
    private boolean _canceled = false;

    public OnLoad onload;
    public OnProgress onprogress;
    public OnError onerror;
    public OnAppInfo onappinfo;
    public Object query;

    public AppLoading(String url,String path,Http http) {
        _http = http;
        _url = url;
        _path = path;
        _key = HttpOptions.cacheKey(url);
        _handler = new Handler();
    }

    protected void onError(Exception ex) {
        Log.d("kk","[APP] [ERROR] " + Log.getStackTraceString(ex));
        if(onerror != null) {
            onerror.onError(_url,ex);
        }
    }

    protected void onLoad(String path) {
        Log.d("kk","[APP] [OK] " + _url + " " + path);
        if(onload != null) {
            onload.onLoad(_url,path,this);
        }
    }

    protected void onProgress(int count,int totalCount) {
        if(onprogress != null) {
            onprogress.onProgress(_url,_path,count,totalCount);
        }
    }

    protected void onAppInfo(Object data) {

        if(onappinfo != null) {
            onappinfo.onAppInfo(_url,data);
        }

        String version = V.stringValue(V.get(data,"version"),"_");
        String ver = V.stringValue(V.get(data,"ver"),"_");

        Object appInfo = _http.decodeJSON(FileResource.getString(new File(new File(_path),"app.json")));
        Map<String,String> vers = null;

        Object items = V.get(data,"res");

        if(items != null && !(items instanceof List)) {
            items = null;
        }

        if(items == null) {
            items = V.get(data,"items");
            if(items != null && !(items instanceof List)) {
                items = null;
            }
        }

        if(V.get(appInfo,"md5") != null
                && version.equals(V.stringValue(V.get(appInfo,"version"),null))) {

            String ver1 = V.stringValue(V.get(appInfo,"ver"),null);
            String ver2 = V.stringValue(V.get(data,"ver"),null);

            if(ver1 != null && ver2 != null && ver1.equals(ver2)) {
                onLoad((new File(new File(_path),version)).getAbsolutePath());
                return;
            }

            Object its = V.get(appInfo,"res");

            if(its != null && !(its instanceof List)) {
                its = null;
            }

            if(its == null) {
                its = V.get(appInfo,"items");
                if(its != null && !(its instanceof List)) {
                    its = null;
                }
            }

            if(its != null && its instanceof List) {

                vers = new TreeMap<>();

                for(Object item : (List) its) {
                    if(item instanceof Map) {
                        String v = V.stringValue(V.get(item,"ver"),null);
                        String path = V.stringValue(V.get(item,"path"),null);
                        if(v != null && path != null) {
                            vers.put(path,v);
                        }
                    }
                }
            }

        }

        File basePath = ((new File(new File(_path),version)));
        File tPath = new File(_path + "_" + version + "_" + ver);

        itemLoad(0,(List<Object>) items,data,vers,basePath,tPath);

    }

    protected void itemLoad(final int index, final List<Object> items, final Object appInfo, final Map<String,String> vers, final File basePath, final File tPath) {

        onProgress(index,items != null ? items.size() : 0);

        if(items != null && index < items.size()) {

            final WeakReference<AppLoading> loading = new WeakReference<AppLoading>(this);

            String url = null;

            Object item = items.get(index);

            File topath = null,tpath = null;

            if(item instanceof Map) {

                String path = V.stringValue(V.get(item,"path"),null);
                String ver  = V.stringValue(V.get(item,"ver"), null);

                if(path == null || ver == null || "app.json".equals(path)) {
                    _handler.post(new Runnable() {
                        @Override
                        public void run() {
                            AppLoading v = loading.get();
                            if(v != null) {
                                v.itemLoad(index + 1, items,appInfo,vers,basePath,tPath);
                            }
                        }
                    });
                    return;
                }

                if(path.contains("..")) {
                    FileResource.deleteDir(tPath);
                    onError(new Exception("错误的资源路径"));
                    return;
                }

                topath = new File(basePath,path);
                tpath = new File(tPath,path);

                if(vers == null || ( vers.containsKey(path) && vers.get(path).equals(ver))) {

                    if(topath.exists()) {
                        FileResource.mkdir(tpath);
                        FileResource.copy(topath,tpath);
                        _handler.post(new Runnable() {
                            @Override
                            public void run() {
                                AppLoading v = loading.get();
                                if(v != null) {
                                    v.itemLoad(index + 1, items,appInfo,vers,basePath,tPath);
                                }
                            }
                        });
                        return;
                    }
                }

                if(ver != null) {
                    url = path + "?v=" + ver;
                } else {
                    url = path;
                }

            } else {
                _handler.post(new Runnable() {
                    @Override
                    public void run() {
                        AppLoading v = loading.get();
                        if(v != null) {
                            v.itemLoad(index + 1, items,appInfo,vers,basePath,tPath);
                        }
                    }
                });
            }

            final File f_tpath = tpath;

            HttpOptions options = new HttpOptions();
            options.method = HttpOptions.METHOD_GET;
            options.type = HttpOptions.TYPE_URI;

            int i = _url.lastIndexOf("/");

            if(i >0) {
                url = _url.substring(0,i +1) + url;
            } else {
                url = _url + url;
            }

            options.url = url;

            options.onfail = new HttpOptions.OnFail() {
                @Override
                public void on(Exception error, Object weakObject) {
                    FileResource.deleteDir(tPath);
                    AppLoading v = loading.get();
                    if(v != null) {
                        v.onError(error);
                    }
                }
            };

            options.onload = new HttpOptions.OnLoad() {
                @Override
                public void on(Object data, Exception error, Object weakObject) {
                    if(error != null) {
                        FileResource.deleteDir(tPath);
                        AppLoading v = loading.get();
                        if(v != null) {
                            v.onError(error);
                        }
                    } else {
                        FileResource.mkdir(f_tpath);
                        f_tpath.delete();
                        File f = new File((String) data);
                        f.renameTo(f_tpath);
                        AppLoading v = loading.get();
                        if(v != null) {
                            v.itemLoad(index + 1, items,appInfo,vers,basePath,tPath);
                        }
                    }
                }
            };

            Log.d("kk","[APP] " +options.absoluteUrl());

            _http.send(options);

        } else {
            verify(items,appInfo,basePath,tPath);
        }


    }


    protected void verify(final List<Object> items, final Object appInfo, final File basePath, final File tPath) {

        final String md5 = V.stringValue(V.get(appInfo,"md5"),null);

        final WeakReference<AppLoading> loading = new WeakReference<AppLoading>(this);

        IO.getHandler().post(new Runnable() {
            @Override
            public void run() {

                AppLoading v = loading.get();

                if(v != null) {

                    String mv = null;

                    if(md5 != null) {

                        Log.d("kk","[APP] [VERIFY] " + v._url);

                        MessageDigest m = null;

                        try {
                            m = MessageDigest.getInstance("MD5");
                        } catch (NoSuchAlgorithmException e) {
                            Log.d("kk",Log.getStackTraceString(e));
                        }

                        if(m != null) {

                            byte[] data = new byte[204800];

                            for(Object item : items) {

                                String path = V.stringValue( V.get(item,"path"),null);

                                if(path == null || "app.json".equals(path)) {
                                    continue;
                                }

                                m.update(path.getBytes(UTF8));

                                FileResource.digest(new File(tPath,path),m,data);

                            }

                            byte[] bytes = m.digest();

                            StringBuffer sb = new StringBuffer();

                            for(int i=0;i<bytes.length;i++) {
                                String vv = Integer.toHexString(bytes[i]);
                                if(vv.length() == 1) {
                                    sb.append("0").append(vv);
                                } else if(vv.length() > 2){
                                    sb.append(vv.substring(vv.length()-2));
                                } else {
                                    sb.append(vv);
                                }
                            }

                            mv = sb.toString();
                        }


                    }

                    if(md5 == null || md5.equals(mv)) {

                        String data = v._http.encodeJSON(appInfo);


                        FileResource.mkdir(basePath);
                        FileResource.deleteDir(basePath);
                        tPath.renameTo(basePath);

                        FileResource.setContent(new File(basePath,"app.json"),data);
                        FileResource.setContent(new File(v._path,"app.json"),data);

                        v._handler.post(new Runnable() {
                            @Override
                            public void run() {

                                AppLoading v = loading.get();

                                if(v != null) {
                                    v.onLoad(basePath.getAbsolutePath());
                                }

                            }
                        });

                    } else {

                        FileResource.deleteDir(tPath);

                        v._handler.post(new Runnable() {
                            @Override
                            public void run() {

                                AppLoading v = loading.get();

                                if(v != null) {
                                    v.onError(new Exception("错误的应用包，应用包校验失败"));
                                }
                            }
                        });

                    }
                }
            }
        });

    }

    public void start() {

        final WeakReference<AppLoading> loading = new WeakReference<AppLoading>(this);


        HttpOptions options = new HttpOptions();
        options.method = HttpOptions.METHOD_GET;
        options.type = HttpOptions.TYPE_JSON;
        options.timeout = 10;
        options.url = _url;

        options.onfail = new HttpOptions.OnFail() {
            @Override
            public void on(Exception error, Object weakObject) {

                AppLoading v = loading.get();

                if(v != null) {
                    v.onError(error);
                }
            }
        };

        options.onload =new HttpOptions.OnLoad() {
            @Override
            public void on(Object data, Exception error, Object weakObject) {

                AppLoading v = loading.get();

                if(v != null) {

                    if(error != null) {
                        v.onError(error);
                    } else if(data instanceof Map) {
                        v.onAppInfo(data);
                    } else {
                        v.onError(new Exception("错误的小应用"));
                    }
                }

            }
        };

        Log.d("kk","[APP] [LOADING] " + options.absoluteUrl());

        _http.send(options);

    }

    public boolean isCanceled() {
        return _canceled;
    }

    public void setCanceled(boolean v) {
        _canceled = v;
    }
}
