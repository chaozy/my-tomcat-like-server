package uk.ac.ucl.context;

import uk.ac.ucl.servlet.BaseServletContext;

import java.io.File;
import java.util.*;

public class BaseContext extends BaseServletContext {
    private Map<String, Object> attributesMap;
    private Context context;

    public BaseContext(Context context){
        attributesMap = new HashMap<>();
        this.context = context;
    }

    @Override
    public void setAttribute(String s, Object o) {
        attributesMap.put(s, o);
    }

    @Override
    public Object getAttribute(String s) {
        return attributesMap.get(s);
    }

    @Override
    public void removeAttribute(String s) {
        attributesMap.remove(s);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        Set<String> keys = attributesMap.keySet();
        return Collections.enumeration(keys);
    }

    @Override
    public String getRealPath(String s) {
        return new File(context.getDocBase(), s).getAbsolutePath();
    }
}
