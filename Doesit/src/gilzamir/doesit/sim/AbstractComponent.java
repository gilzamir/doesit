package gilzamir.doesit.sim;

import com.jme3.math.Vector3f;
import java.util.HashMap;
import java.util.Map;

public class AbstractComponent implements IComponent {
    
    private Map<String, Float> pfloat = new HashMap<String, Float>();
    private Map<String, Integer> pInteger = new HashMap<String, Integer>();
    private Map<String, Vector3f> pVector3f = new HashMap<String, Vector3f>();
    private Map<String, Object> pObject = new HashMap<String, Object>();
    private Map<String, String> pString = new HashMap<String, String>();
    private Map<String, Boolean> pBoolean = new HashMap<String, Boolean>();
    
    
    public void setFloat(String p, Float v) {
        pfloat.put(p, v);
    }

    public Float getFloat(String p) {
        return pfloat.get(p);
    }

    public void setInteger(String p, Integer v) {
        pInteger.put(p, v);
    }

    public Integer getInteger(String p) {
        return pInteger.get(p);
    }

    public void setString(String p, String n) {
        pString.put(p, n);
    }

    public String getString(String p) {
        return pString.get(p);
    }

    public void setVector3f(String n, Vector3f v) {
        pVector3f.put(n, v);
    }

    public Vector3f getVector3f(String n) {
        return pVector3f.get(n);
    }

    public void setObject(String n, Object obj) {
        pObject.put(n, obj);
    }

    public Object getObject(String n) {
        return pObject.get(n);
    }

    public void setBoolean(String n, Boolean v) {
        pBoolean.put(n, v);
    }

    public Boolean getBoolean(String n) {
        return pBoolean.get(n);
    }
}
