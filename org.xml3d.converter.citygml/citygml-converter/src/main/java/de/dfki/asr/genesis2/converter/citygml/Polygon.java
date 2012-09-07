/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.asr.genesis2.converter.citygml;
//import org.geotools.graph.util.delaunay.*;
/**
 *
 * @author Daniel
 */
public class Polygon {
    Vec3D positions[];                      //Vertex positions, stored aas 3D-Vector
    Vec3D normals[];                        //Vertex Normals
    int indices[];                          //indices for triangulation
    float texCoords[];                      //texture coordinates, directly read from gml-File -> float list
    private ShaderFactory shader;           //Shader used for the building
    
}
