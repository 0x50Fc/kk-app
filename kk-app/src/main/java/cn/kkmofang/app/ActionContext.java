package cn.kkmofang.app;

import java.util.Stack;

/**
 * Created by nwangchao15 on 2018/4/2.
 */

public class ActionContext {
    private static volatile ActionContext context;
    private ThreadLocal<Stack<Object>>  actionValue;


    private ActionContext() {
        actionValue = new ThreadLocal<>();
    }

    public static ActionContext getInstance(){
        if (context == null){
            synchronized (ActionContext.class){
                context = new ActionContext();
            }
        }
        return context;
    }

    public void pushContext(Object value) {
        Stack<Object> q = actionValue.get();
        if(q == null) {
            q = new Stack<>();
            actionValue.set(q);
        }
        q.push(value);
    }

    public Object currentContext() {

        Stack<Object> q = actionValue.get();

        if(q != null && !q.isEmpty()) {
            return q.peek();
        }

        return null;
    }

    public Object popContext() {

        Stack<Object> q = actionValue.get();

        if(q != null && !q.isEmpty()) {
            return q.pop();
        }

        return null;
    }
}
