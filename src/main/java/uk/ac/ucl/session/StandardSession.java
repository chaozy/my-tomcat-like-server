package uk.ac.ucl.session;

import lombok.Getter;
import lombok.Setter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.*;

@Getter@Setter
public class StandardSession implements HttpSession {
    private Map<String, Object> attributeMap;
    private String id;
    private long creationTime;
    private long lastAccessedTime;
    private ServletContext servletContext;
    private int maxInactiveInterval;

    public StandardSession(String sid, ServletContext servletContext) {
        this.id = sid;
        this.servletContext = servletContext;
        this.creationTime = System.currentTimeMillis();
        this.attributeMap = new HashMap<>();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }


    @Override
    public HttpSessionContext getSessionContext() {
        return null;
    }

    @Override
    public Object getAttribute(String s) {
        return this.attributeMap.get(s);
    }

    @Override
    public Object getValue(String s) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        Set<String> keys = this.attributeMap.keySet();
        return Collections.enumeration(keys);
    }

    @Override
    public String[] getValueNames() {
        return new String[0];
    }

    @Override
    public void setAttribute(String s, Object o) {
        this.attributeMap.put(s, o);
    }

    @Override
    public void putValue(String s, Object o) {

    }

    @Override
    public void removeAttribute(String s) {

    }

    @Override
    public void removeValue(String s) {

    }

    @Override
    public void invalidate() {
        attributeMap.clear();
    }

    @Override
    public boolean isNew() {
        return creationTime == lastAccessedTime;
    }
}
