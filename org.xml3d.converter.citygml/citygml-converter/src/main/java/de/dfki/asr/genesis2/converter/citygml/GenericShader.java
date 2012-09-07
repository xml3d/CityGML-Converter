
package de.dfki.asr.genesis2.converter.citygml;
import java.util.ArrayList;
import org.w3c.dom.*;
import java.util.Map;
import java.util.HashMap;
/**
 * Parent class for Shaders, containing only its node, id, shader targets and shader type.
 * Diffuse and texture shaders are derived from this base class
 * @author Daniel
 */
public class GenericShader {
    
    protected String id;
    protected ArrayList<String> targets = new ArrayList<String>();
    protected Element shaderNode;
    protected String shaderType;
    
    GenericShader(){
        
    }
    /**
     * Create a new shader instance with the given id and the root node of the shader definition
     * in the gml file
     * @param id The new id to be used for this shader
     * @param shaderNode Root node in the gml file
     */
    GenericShader(String id, Element shaderNode){
        this.id = id;
        this.shaderNode = shaderNode;
    }
    
    void setShaderType(String type)
    {
        this.shaderType = type;
    }
    
    void addTarget(String target)
    {
        this.targets.add(target);
    }
    
    public String getID()
    {
        return this.id;
    }
}
