package de.dfki.asr.genesis2.converter.citygml;

/**
 * A class for defining xml3d shaders. Depending on whether a texture or a diffuse
 * material will be used, the shader will be set to the corresponding type.
 * @author Daniel Spieldenner
 */

import java.util.ArrayList;
import org.w3c.dom.*;
import java.util.Map;
import org.jboss.logging.Logger;

public class xmlShader {
    private static final Logger log = Logger.getLogger(xmlShader.class.getName());
    private String id = new String();
    private String shaderType = new String();          //diffuse or texture
    private String diffuseColor = new String("1.0 1.0 1.0");
    private float ambient = 0.4f;
    private String texturePath = new String();         //location of the texture file
    private String texCoords = new String();
    private String wrapMode = new String();
    private ArrayList<String> targets = new ArrayList<String>();           //List of polygon-ids using this shader
    
    private Element shaderNode;
    
    xmlShader()
    {
        
    }
    
    /**
     * Get texture coordinates for current shader
     * @return texture coordinates as string
     */
    public String getTexCoords()
    {
        return this.texCoords;
    }
    /**
     * Set the ambient intensity of the current shader to the given value
     * @param ambient Ambient intensity to be used
     */
    public void setAmbient(float ambient)
    {
        this.ambient = ambient;
    }
    /**
     * Set the id of the shader to the given one
     * @param id ID to be used
     */
    public void setID(String id)
    {
        this.id = id;
    }
    /**
     * Set the texture coordinates defined for this texture to the given ones
     * @param texCoords Texture coordinates to be used, provided as string
     */
    public void setTexCoords(String texCoords)
    {
        this.texCoords = texCoords;
    }
    /**
     * If the texture needs a specific wrap mode, it can be set via this method.
     * @param wrapMode 
     */
    public void setWrapMode(String wrapMode)
    {
        this.wrapMode = wrapMode;
    }
    /**
     * Set the path to the texture this shader is using
     * @param texturePath The path to be used
     */    
    public void setTexturePath(String texturePath)
    {
        this.texturePath = texturePath;
    }
    /**
     * Adds a polygon to the list of polygons using this shader
     * @param target The new target to be added, provided by its id as string
     */
    public void addTarget(String target)
    {
        this.targets.add(target);
    }
    /**
     * Sets the diffuse color to the given r, g and b values
     * @param diffColor The color to be used as a string containing all components
     */
    public void setDiffuseColor(String diffColor)
    {
        this.diffuseColor = diffColor;
    }
    /**
     * Define the type of the shader
     * @param shaderType The shader type to be used, either "Material" or "Texture"
     */
    public void setShaderType(String shaderType) {
        this.shaderType = shaderType;
    }
    /**
     * Find material definition in the gml file. When a material definition is found, 
     * color information is stored in the shader and its type is set to material.
     * @param shaderMap The list of converted shaders
     */
    public void getMaterial(Map shaderMap, Map polygonMap)
    {
        /*
         * Read material-information from the gml file, including diffuse color
         * and list of target polygons 
         */
        NodeList materialNodes = shaderNode.getElementsByTagName("app:X3DMaterial");
        for(int i = 0; i<materialNodes.getLength(); i++)
        {
            
            this.shaderType = "Material";                                           //Assumption: Shaders are either material or texture
            Element material = (Element)materialNodes.item(i);
            
            NodeList diffColors = material.getElementsByTagName("app:diffuseColor");
            NodeList targets = material.getElementsByTagName("app:target");
            
            
            
            for(int j=0; j<diffColors.getLength(); j ++)
            {
                this.diffuseColor = (diffColors.item(j).getFirstChild().getNodeValue());
            }
            
            for(int k=0; k<targets.getLength(); k++)
            {
                this.targets.add(targets.item(k).getFirstChild().getNodeValue());
            }
            shaderMap.put("material"+shaderMap.size(), this);
            
        }
        
    }
    public String getTexturePath()
    {
        return this.texturePath;
    }
    public float[] getDiffuseColor()
    {
        float colors[] = new float[3];
        try{
        String diffuseString = this.diffuseColor;
        
        float r = Float.valueOf(diffuseString.split("\\s+")[0]);
        float g = Float.valueOf(diffuseString.split("\\s+")[1]);
        float b = Float.valueOf(diffuseString.split("\\s+")[2]);
        colors[0] = r;
        colors[1] = g;
        colors[2] = b;
        
        } catch(Exception e)
        {
            log.error("Could not resolve diffuse color of shader " + this.id +": " +e);
        }
        return colors;
    }
    
        
    /**
     * Find texture definitions in the gml file. When a texture is found, store its path in
     * the shader instance and set its type to "Texture".
     * @param shaderMap
     * @param polygonMap 
     */
    public void getTextures(Map shaderMap, Map polygonMap)
    {
        /*
         * get texture definitions from gml-File, including texture path, target,
         * texture coordinates, wrapMode
         */
        NodeList textureNodes = shaderNode.getElementsByTagName("app:ParameterizedTexture");
        for(int i=0; i<textureNodes.getLength(); i++)
        {
            
            this.shaderType = "Texture";                                            //Assumption: Shader is either material or texture; Texture found -> is texture
            
            Element texture = (Element)textureNodes.item(i);
            NodeList texturePath = texture.getElementsByTagName("app:imageURI");
            
            NodeList targets = texture.getElementsByTagName("app:target");
            NodeList texCoords = texture.getElementsByTagName("app:textureCoordinates");
            NodeList wrapMode = texture.getElementsByTagName("app:wrapMode");
            
            
            if(texturePath.getLength() > 0)
                this.texturePath = (texturePath.item(0).getFirstChild().getNodeValue());
            
            if(texCoords.getLength() > 0)
                this.texCoords = (texCoords.item(0).getFirstChild().getNodeValue());
            
            if(wrapMode.getLength() > 0)
                this.wrapMode = (wrapMode.item(0).getFirstChild().getNodeValue());
            
            for(int j=0; j<targets.getLength(); j++)
            {
                this.targets.add(targets.item(j).getAttributes().getNamedItem("uri").getTextContent());
                polygonMap.put(targets.item(j).getAttributes().getNamedItem("uri").getTextContent(), this);
            }
            shaderMap.put("texture"+shaderMap.size(), this);
           
            
        }
        
        
    }
    
    public String getID()
    {
        return this.id;
    }
    
    public String getShaderType()
    {
        return this.shaderType;
    }
}
