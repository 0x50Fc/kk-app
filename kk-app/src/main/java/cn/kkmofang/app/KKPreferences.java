package cn.kkmofang.app;

import android.content.SharedPreferences;

import java.util.Set;

import cn.kkmofang.script.IScriptObject;
import cn.kkmofang.script.ScriptContext;

/**
 * Created by hailong11 on 2018/4/23.
 */

public class KKPreferences implements IScriptObject {

    private final SharedPreferences _preferences;

    public KKPreferences(SharedPreferences preferences) {
        _preferences = preferences;
    }

    @Override
    public String[] keys() {
        Set<String> keySet = _preferences.getAll().keySet();
        return keySet.toArray(new String[keySet.size()]);
    }

    @Override
    public Object get(String key) {
        if(_preferences.contains(key)) {
            return _preferences.getString(key,null);
        }
        return null;
    }

    @Override
    public void set(String key, Object value) {
        if(value == null) {
            _preferences.edit().remove(key).commit();
        } else {
            _preferences.edit().putString(key, ScriptContext.stringValue(value, "")).commit();
        }
    }
}
