package cn.kkmofang.app;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.kkmofang.duktape.Heapptr;
import cn.kkmofang.script.IScriptFunction;
import cn.kkmofang.script.IScriptObject;
import cn.kkmofang.script.ScriptContext;
import cn.kkmofang.view.ViewContext;

/**
 * Created by zhanghailong on 2018/3/13.
 */

public class Context extends cn.kkmofang.duktape.Context implements cn.kkmofang.observer.IObserverContext{

    public final static String TAG = "kk-app";

    private static final Pattern pattern = Pattern.compile("[a-zA-Z][0-9a-zA-Z\\\\._]*");

    @Override
    public String[][] evaluateKeys(String evaluateCode) {

        if(evaluateCode != null) {

            List<String[]> keys = new ArrayList<String[]>();

            String v = evaluateCode
                    .replace("\'","")
                    .replace("\"","")
                    .replace("/'.*?'/g","")
                    .replace("/\".*?\"/g","");
            Matcher matcher = pattern.matcher(v);
            while(matcher.find()) {
                String name = matcher.group();
                if(name.length() >0 && !name.startsWith("_")) {
                    keys.add(name.split("\\."));
                }
            }

            return keys.toArray(new String[keys.size()][]);

        }

        return new String[0][];
    }

    @Override
    public Object evaluate(String evaluateCode) {

        StringBuffer sb = new StringBuffer();

        sb.append("(function(object){ var _G ; try { with(object) { _G = ")
                .append(evaluateCode)
                .append("; } } catch(e) {} return _G; })");

        eval(sb.toString());

        if(isFunction(-1)) {

            Heapptr func = new Heapptr(this,getHeapptr(-1));

            pop();

            return func;

        } else {
            String v = getErrorString(-1);
            Log.d(TAG,v);
            pop();
        }

        return null;
    }

    @Override
    public Object execEvaluate(Object func, Object object) {

        Object r = null;

        ScriptContext.pushContext(this);

        pushValue(func);

        if(isFunction(-1)) {

            pushValue(object);

            if(pcall(1) != DUK_EXEC_SUCCESS) {
                String v = getErrorString(-1);
                Log.d(TAG,v);
                pop();
            } else {
                r = toValue(-1);
                pop();
            }
        } else {
            pop();
        }

        ScriptContext.popContext();

        return r;
    }
}
