
package de.dfki.asr.genesis2.converter.citygml;

/**
 * A Texture shader, containing a path to the image to be used as texture, texture coordinates
 * @author Daniel
 */
public class TextureShader extends GenericShader {
    private String texturePath;         //location of the texture file
    private String texCoords;
    private String wrapMode;
    private float ambient;
    
    void setTexturePath(String texturePath)
    {
        this.texturePath = texturePath;
    }
    
    void setTexCoords(String texCoords)
    {
        this.texCoords = texCoords;
    }
    
    void setWrapMode(String wrapMode)
    {
        this.wrapMode = wrapMode;
    }
    
    public String getID()
    {
        return this.id;
    }
}
