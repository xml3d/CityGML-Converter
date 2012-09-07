
package de.dfki.asr.genesis2.converter.citygml;
import java.util.ArrayList;
import org.w3c.dom.*;
import java.util.Map;
import java.util.HashMap;
import java.io.*;
/**
 * Shader definition for shading xml3d polygons;
 * Each shader has a diffuse color, ambient intensity, a distinction between 
 * pure diffuse and textured shaders and a path to the texture file in the latter case.
 * 
 */
public class ShaderFactory {
    private String id;
    private String shaderType;          //diffuse or texture
    private String diffuseColor;
    private float ambient;
    private String texturePath;         //location of the texture file
    private String texCoords;
    private String wrapMode;
    private ArrayList<String> targets = new ArrayList<String>();           //List of polygon-ids using this shader
    
    private Element shaderNode;
    
    ShaderFactory()
    {
        
    }
    /**
     * Create a new shaderFactory for a given shader to be processed
     * @param Current shader to be created either as texture or as diffuse material 
     */
    ShaderFactory(Element shaderNode)
    {
        this.shaderNode = shaderNode;
    }
    
    /**
     * Generate a standard shader for roofs and walls, if no shader for certain buildings
     * is provided in the gmlFile
     * @param shaderMap List of xmlShaders the generated Shaders will be stored in
     */
    public void generateStandardShaders(Map shaderMap)
    {
        //roofShader
        xmlShader roofShader = new xmlShader();
        roofShader.setShaderType("Material");
        roofShader.setID("roofShader");
        roofShader.setDiffuseColor("0.6 0.0 0.0");
        shaderMap.put("roofShader", roofShader);
        
        //wallShader
        xmlShader wallShader = new xmlShader();
        wallShader.setShaderType("Material");
        wallShader.setID("wallShader");
        wallShader.setDiffuseColor("0.8 0.8 0.8");
        shaderMap.put("wallShader", wallShader);
        
    }
    /**
     * Get material information from the gml file, store the shader values found and
     * the list of target polygons of the shaders.
     * @param shaderMap List of shaders defined in the gml file
     * @param polygonMap List of target polygons of the current shader
     */
    public void getMaterial(Map shaderMap, Map polygonMap)
    {
        
        NodeList materialNodes = shaderNode.getElementsByTagName("app:X3DMaterial");
        for(int i = 0; i<materialNodes.getLength(); i++)
        {
            xmlShader matShader = new xmlShader(); 
            matShader.setShaderType("Material");                                           //Assumption: Shaders are either material or texture
            matShader.setID("material_" + i);
            Element material = (Element)materialNodes.item(i);
            
            NodeList diffColors = material.getElementsByTagName("app:diffuseColor");
            NodeList targets = material.getElementsByTagName("app:target");
            
            
            
            for(int j=0; j<diffColors.getLength(); j ++)
            {
                matShader.setDiffuseColor(diffColors.item(j).getFirstChild().getNodeValue());
            }
            
            for(int k=0; k<targets.getLength(); k++)
            {
                matShader.addTarget(targets.item(k).getFirstChild().getNodeValue());
                polygonMap.put(targets.item(k).getTextContent(), matShader);
            }
            shaderMap.put("material"+shaderMap.size(), matShader);
            
        }
        
    }
    /**
     * Get texture information from the gml file, store the shader values found and
     * the list of target polygons of the shaders. \p
     * Additionally, search for georeferenced texture definitions, generate a shader for those and
     * compute the texture coordinates based on the information contained in the corresponding .tfw file
     * @param shaderMap List of shaders defined in the gml file
     * @param polygonMap List of target polygons of the current shader
     */
    public void getTextures(Map shaderMap, Map polygonMap)
    {
        /*
         * get texture definitions from gml-File, including texture path, target,
         * texture coordinates, wrapMode
         */
        NodeList textureNodes = shaderNode.getElementsByTagName("app:ParameterizedTexture");
        NodeList geoTextureNodes = shaderNode.getElementsByTagName("app:GeoreferencedTexture");
        for(int i=0; i<textureNodes.getLength(); i++)
        {
            xmlShader texShader = new xmlShader();
            texShader.setShaderType("Texture");                                            //Assumption: Shader is either material or texture; Texture found -> is texture
            texShader.setID("textureShader_" + i);
            Element texture = (Element)textureNodes.item(i);
            NodeList texturePath = texture.getElementsByTagName("app:imageURI");
            
            NodeList targets = texture.getElementsByTagName("app:target");
            NodeList texCoords = texture.getElementsByTagName("app:textureCoordinates");
            NodeList wrapMode = texture.getElementsByTagName("app:wrapMode");
            
            
            if(texturePath.getLength() > 0)
                texShader.setTexturePath(texturePath.item(0).getFirstChild().getNodeValue());
            
            if(texCoords.getLength() > 0)
                texShader.setTexCoords(texCoords.item(0).getFirstChild().getNodeValue());
            
            if(wrapMode.getLength() > 0)
                texShader.setWrapMode(wrapMode.item(0).getFirstChild().getNodeValue());
            
            for(int j=0; j<targets.getLength(); j++)
            {
                texShader.addTarget(targets.item(j).getAttributes().getNamedItem("uri").getTextContent());
                polygonMap.put(targets.item(j).getAttributes().getNamedItem("uri").getTextContent(), texShader);
            }
            shaderMap.put("texture"+shaderMap.size(), texShader);
           
            
        }
        
        for(int g=0; g<geoTextureNodes.getLength(); g++)
        {   
            String targetPath = new String();
            xmlShader geoShader = new xmlShader();
            geoShader.setShaderType("Texture");
            geoShader.setID("groundTexture" + g);
            Element currentTexture = (Element)geoTextureNodes.item(g);
            
            NodeList imageUri = currentTexture.getElementsByTagName("app:imageURI");
            NodeList target = currentTexture.getElementsByTagName("app:target");
            
            for(int u=0; u<imageUri.getLength(); u++)
            {
                targetPath = imageUri.item(u).getTextContent();
                geoShader.setTexturePath(imageUri.item(u).getTextContent().replace("tif", "jpg"));
            }
            
            for(int t=0; t<target.getLength(); t++)
            {
                geoShader.addTarget(target.item(t).getTextContent());
                polygonMap.put(target.item(t).getTextContent(), geoShader);
            }
            if(targetPath != null)
            
            shaderMap.put("groundTexture"+shaderMap.size(), geoShader);
                
        }
        
        
    }
    
    
    
    public String getID()
    {
        return this.id;
    }
}
