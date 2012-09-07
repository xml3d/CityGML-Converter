/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.asr.genesis2.converter.citygml;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import org.w3c.dom.*;
/**
 * Material Shader, defined by its diffuse color
 * @author Daniel
 */
public class MaterialShader extends GenericShader{
    private String diffuseColor;
    private float ambient;
    
    public void setDiffuseColor(String diffuseColor)
    {
        this.diffuseColor = diffuseColor;
    }
    
}
