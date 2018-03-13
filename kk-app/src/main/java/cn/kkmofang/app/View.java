package cn.kkmofang.app;

import android.renderscript.Script;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cn.kkmofang.duktape.Heapptr;
import cn.kkmofang.observer.IObserver;
import cn.kkmofang.observer.Listener;
import cn.kkmofang.observer.Observer;
import cn.kkmofang.script.IScriptFunction;
import cn.kkmofang.script.IScriptObject;
import cn.kkmofang.script.ScriptContext;
import cn.kkmofang.view.Element;
import cn.kkmofang.view.event.Event;
import cn.kkmofang.view.event.EventFunction;

/**
 * Created by hailong11 on 2018/3/13.
 */

public class View {

    private final static Map<String,Class<?>> _elements = new TreeMap<>();

    public static void setElementClass(String name,Class<?> elementClass) {
        _elements.put(name,elementClass);
    }

    public static Element newElement(String name) {

        Class<?> clazz = Element.class;

        if(name != null && _elements.containsKey(name)) {
            clazz = _elements.get(name);
        }

        try {
            return (Element) clazz.newInstance();
        } catch (Throwable e) {
            Log.d(Context.TAG,Log.getStackTraceString(e));
            return new Element();
        }

    }

    public static void addOnEvent(IObserver data,Element e,String name,final String[] keys) {

        final WeakReference<IObserver> v = new WeakReference<IObserver>(data);

        e.on(name, new EventFunction() {
            @Override
            public void onEvent(Event event) {
                if(event instanceof Element.Event) {
                    Object d = ((Element.Event) event).data();
                    IObserver vv = v.get();
                    while(vv != null && vv.parent() != null) {
                        vv = vv.parent();
                    }
                    if(vv != null) {
                        vv.set(keys,d);;
                    }
                }
            }
        });

    }

    public static void addOnAttribute(final Element e,Map<String,Object> attrs,IObserver data) {

        for(String key : attrs.keySet()) {
            String v = ScriptContext.stringValue(attrs.get(key),"");
            if(key.startsWith("kk:")) {
                if(key.equals("kk:text")) {

                    data.on(v, new Listener<Object>() {
                        @Override
                        public void onChanged(IObserver observer, String[] changedKeys, Object value, Object weakObject) {
                            if(value != null) {
                                e.set("#text",ScriptContext.stringValue(value,""));
                            }
                        }

                    },null,Observer.PRIORITY_DESC);

                } else if(key.equals("kk:show")) {

                    data.on(v, new Listener<Object>() {
                        @Override
                        public void onChanged(IObserver observer, String[] changedKeys, Object value, Object weakObject) {
                            if(value != null) {
                                e.set("#text",ScriptContext.booleanValue(value,false) ? "false" : "true");
                            }
                        }

                    },null,Observer.PRIORITY_DESC);

                } else if(key.equals("kk:hide")) {
                    data.on(v, new Listener<Object>() {
                        @Override
                        public void onChanged(IObserver observer, String[] changedKeys, Object value, Object weakObject) {
                            if(value != null) {
                                e.set("#text",ScriptContext.booleanValue(value,false) ? "true" : "false");
                            }
                        }

                    },null,Observer.PRIORITY_DESC);
                } else if(key.startsWith("kk:on")) {
                    addOnEvent(data,e,key.substring(5),v.split("\\."));
                } else if(key.startsWith("kk:emit_")) {

                    final String name = key.substring(8);

                    data.on(v, new Listener<Object>() {
                        @Override
                        public void onChanged(IObserver observer, String[] changedKeys, Object value, Object weakObject) {
                            if(value != null) {
                                Element.Event ev =new Element.Event(e);
                                ev.setData(value);
                                e.emit(name,ev);
                            }
                        }

                    },null,Observer.PRIORITY_DESC);
                } else {
                    final String name = key.substring(3);
                    data.on(v, new Listener<Object>() {
                        @Override
                        public void onChanged(IObserver observer, String[] changedKeys, Object value, Object weakObject) {
                            if(value != null) {
                                e.set(name,ScriptContext.stringValue(value,""));
                            }
                        }

                    },null,Observer.PRIORITY_DESC);
                }
            } else if(key.startsWith("style:")) {
                e.setCSSStyle(v,key.substring(6));
            } else if(key.equals("style")) {
                e.setCSSStyle(v,"");
            } else {
                e.set(key,v);
            }
        }

    }


    private static interface OnForItemLoad {

        void load(int i, Object index,Object item);

    }

    public static void addOnFor(String evaluate, final String name, final Map<String,Object> attrs, Element parent, final IObserver data, final Heapptr func) {

        if(evaluate == null) {
            return;
        }

        String index = "index";
        String item = "item";
        String evaluateScript = evaluate;

        int idx = evaluate.indexOf(" in ");

        if(idx >=0) {
            item = evaluate.substring(0,idx).trim();
            evaluateScript = evaluate.substring(idx+4);
            idx = item.indexOf(",");
            if(idx >=0) {
                index = item.substring(0,idx).trim();
                item = item.substring(idx +1).trim();
            }
        }

        Element before = new Element();

        parent.append(before);

        final WeakReference<Element> be = new WeakReference<Element>(before);

        final List<Element> elements = new ArrayList<>();
        final List<IObserver> observers = new ArrayList<>();
        final String[] indexKeys = new String[]{index};
        final String[] itemKeys = new String[]{item};

        Listener<Object> reloadData = new Listener<Object>() {
            @Override
            public void onChanged(IObserver observer, String[] changedKeys, Object value, Object weakObject) {

                final Element ee = be.get();

                if(ee == null) {
                    return;
                }

                OnForItemLoad itemLoad = new OnForItemLoad() {

                    @Override
                    public void load(int i, Object index, Object item) {

                        Element e;
                        IObserver obs;

                        if(i < elements.size()) {
                            e = elements.get(i);
                            obs = observers.get(i);
                        } else {
                            e = newElement(name);
                            obs = new Observer(data.context());
                            addOnAttribute(e,attrs,obs);
                            if(func != null) {
                                runFunc(func,e,obs);
                            }
                            ee.before(e);
                            elements.add(e);
                            observers.add(obs);
                            obs.setParent(data);
                        }

                        obs.set(indexKeys,index);
                        obs.set(itemKeys,item);

                    }
                };

                int i = 0;

                if(value != null) {

                    if(value instanceof Collection) {

                        for(Object v : (Collection<Object>) value) {
                            itemLoad.load(i,i,v);
                            i ++;
                        }
                    } else if(value instanceof Map) {
                        Map<String,Object>  m= (Map<String,Object>) value;
                        for(String key : m.keySet()) {
                            itemLoad.load(i,key,m.get(key));
                            i ++;
                        }
                    } else if(value instanceof IScriptObject) {

                        IScriptObject v = (IScriptObject) value;

                        for(String key : v.keys()) {
                            itemLoad.load(i,key,v.get(key));
                            i ++;
                        }
                    } else if(value.getClass().isArray()) {
                        int n = Array.getLength(value);
                        for(int j=0;j<n;j++) {
                            Object v = Array.get(value,j);
                            itemLoad.load(i,j,v);
                            i ++;
                        }
                    } else {
                        for(String key : ScriptContext.keys(value)) {
                            itemLoad.load(i,key,ScriptContext.get(value,key));
                            i ++;
                        }
                    }
                }



                while(i < elements.size()) {
                    Element e = elements.get(i);
                    IObserver obs = observers.get(i);
                    obs.off(new String[]{},null,null);
                    e.remove();
                    elements.remove(i);
                    observers.remove(i);
                }

            }
        };

        data.on(evaluateScript,reloadData,null,Observer.PRIORITY_DESC);

    }

    public static void runFunc(Heapptr func,Element e,IObserver data) {
        cn.kkmofang.duktape.Context ctx = func.context();
        if(ctx != null) {
            ctx.pushHeapptr(func.heapptr());
            ctx.pushObject(e);
            ctx.pushObject(data);
            if(ctx.pcall(2) != cn.kkmofang.duktape.Context.DUK_EXEC_SUCCESS) {
                Log.d(Context.TAG,ctx.getErrorString(-1));
            }
            ctx.pop();
        }
    }

    public static void add(String name, Map<String,Object> attrs, Element parent, IObserver data,Heapptr func) {

        if(attrs.containsKey("kk:for")) {
            String v = ScriptContext.stringValue(attrs.get("kk:for"),"");
            attrs.remove("kk:for");
            addOnFor(v,name,attrs,parent,data,func);
        } else {
            Element e = newElement(name);
            addOnAttribute(e,attrs,data);
            parent.append(e);
            if(func != null) {
                runFunc(func,e,data);
            }
        }

    }

    public static IScriptFunction Func = new IScriptFunction() {
        @Override
        public int call() {

            cn.kkmofang.duktape.Context ctx = (cn.kkmofang.duktape.Context) ScriptContext.currentContext();

            int top = ctx.getTop();

            String name = null;
            Object attrs = null;
            Object parent = null;
            Object data = null;
            Heapptr func = null;

            if(top >0 && ctx.isString(-top)) {
                name = ctx.toString(-top);
            }

            if(top >1 && ctx.isObject(-top +1)) {
                attrs = ctx.toValue(-top +1);
            }

            if(top >2 && ctx.isObject(-top +2)) {
                parent = ctx.toValue(-top +2);
            }

            if(top >3 && ctx.isObject(-top +3)) {
                data = ctx.toValue(-top +3);
            }

            if(top > 4 && ctx.isFunction(-top + 4)) {
                func = new Heapptr(ctx,ctx.getHeapptr(-top + 4));
            }

            if(name != null
                    && (attrs != null && attrs instanceof Map)
                    && (parent != null && parent instanceof Element)
                    && (data != null && data instanceof IObserver)) {
                add(name,(Map<String,Object>) attrs,(Element) parent,(IObserver) data,func);
            }

            return 0;
        }
    };

}
