package gilzamir.doesit.sim;

import com.jme3.math.Vector3f;

public interface IComponent {
    void setFloat(String p, Float v);
    Float getFloat(String p);
    
    void setInteger(String p, Integer v);
    Integer getInteger(String p);
    
    void setString(String p, String n);
    String getString(String p);
    
    void setVector3f(String n, Vector3f v);
    Vector3f getVector3f(String n);
    
    void setObject(String n, Object obj);
    Object getObject(String n);
    
    void setBoolean(String n, Boolean v);
    Boolean getBoolean(String n);
}
