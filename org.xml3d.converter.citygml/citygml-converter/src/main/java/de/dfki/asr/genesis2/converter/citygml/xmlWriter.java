package de.dfki.asr.genesis2.converter.citygml;

import java.io.*;

import org.w3c.dom.*;

import javax.xml.parsers.*;

import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;




import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
/**
 *
 * Generates the xml3D-File from the stored gml-information
 * and writes it to disc
 * 
 */
public class xmlWriter {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder xmlBuilder;
    Document xmlFile;
    String outputPath;
    Element htmlRoot;
    Element xml3d;
    Element defs;
    
    FileWriter xmlWriter;
    
    xmlWriter(String filename) throws Exception
    {
        this.xmlBuilder = this.factory.newDocumentBuilder();
        this.xmlFile = this.xmlBuilder.newDocument();
        this.xmlWriter = new FileWriter(filename);
        this.outputPath = filename;
    }
    
    void generateFramework(){
        /*
         * Generates a basic framework for the final xmlFile, divided into
         * defs and group-section, surrounding html-tag and necessary namespaces
         */
        this.htmlRoot=xmlFile.createElement("html");
        xml3d = xmlFile.createElement("xml3d");
        defs = xmlFile.createElement("defs");
        xml3d.appendChild(defs);
        htmlRoot.appendChild(xml3d);
        System.out.println(xmlFile.toString());
        xmlFile.appendChild(htmlRoot);
        
    }
    void processShader(Map shaderMap)
    {
        Iterator shaderIter = shaderMap.keySet().iterator();
        while(shaderIter.hasNext())
        {
            
        }
    }
    void processBuildings(Map buildingMap, Map shaderMap, Map polygonMap) {
        /*
         * Reads the buildings from the building map and generates xml3d-Tags
         * for geometry and shaders
         */
        Iterator buildingIter = buildingMap.keySet().iterator();
        while(buildingIter.hasNext())
        {
            Building building = (Building)buildingMap.get((buildingIter.next())); 
            Element buildingGroup = xmlFile.createElement("group");
            buildingGroup.setAttribute("id", building.getID()+"_id");
            buildingGroup.setAttribute("class", building.getID());
            buildingGroup.setAttribute("onmouseover", "handleOnMouseOver(this);");
            buildingGroup.setAttribute("onmousemove", "handleOnMouseMove(this);");
            buildingGroup.setAttribute("onmouseout", "handleOnMouseOut(this);");
            xml3d.appendChild(buildingGroup);
            
            //Now that the group is created, get list of polygons, create a data node for aech of them,
            //look up the applied shader and add a group node to the building node
            ArrayList<XmlPoly> poly = building.getPolyList();
            Iterator<XmlPoly> polyIter = poly.iterator();
            while(polyIter.hasNext())
            {
                XmlPoly currentPoly = polyIter.next();
                Element polyData = xmlFile.createElement("data");
                Element polyGroup = xmlFile.createElement("group");
                
                Element positions = xmlFile.createElement("float3");
                Element normals = xmlFile.createElement("float3");
                
                Element meshElement = xmlFile.createElement("mesh");
                meshElement.setAttribute("src", "#"+currentPoly.getID());
                meshElement.setAttribute("type", "triangles");
                
                positions.setAttribute("name", "position");
                normals.setAttribute("name", "normal");
                
                polyGroup.setAttribute("class", building.getID());
                polyGroup.setAttribute("id", building.getID() +"_child_"+poly.indexOf(currentPoly));
                
                if(polygonMap.keySet().contains("#"+currentPoly.getID()))
                    polyGroup.setAttribute("shader", "#"+((GenericShader)polygonMap.get("#"+currentPoly.getID())).getID());
                //Read positions and normals from corresponding lists, add to text nodes, apply to elements
                Text posValues = xmlFile.createTextNode("");
                Text normValues = xmlFile.createTextNode("");
                
                //Positions
                Iterator<Vec3D> posIter = currentPoly.getPositions().iterator();
                
                while(posIter.hasNext())
                {
                    Vec3D currentPos = posIter.next();
                    posValues.appendData(currentPos.coordString());
                    posIter.remove();
                }
                
                //Normals
                Iterator<Vec3D> normIter = currentPoly.getNormals().iterator();
                while(normIter.hasNext())
                {
                    Vec3D currentNorm = normIter.next();
                    normValues.appendData(currentNorm.coordString());
                    normIter.remove();
                }
                
                //Append Text- and other nodes to parent elements
                positions.appendChild(posValues);
                normals.appendChild(normValues);
                
                polyData.setAttribute("id", currentPoly.getID());
                polyData.appendChild(positions);
                polyData.appendChild(normals);
                
                polyGroup.appendChild(meshElement);
                
                defs.appendChild(polyData);
                buildingGroup.appendChild(polyGroup);
                
            }
            buildingIter.remove();
        }
    }
    
    void createLight(gmlParser gmlTree)
    {
        /*
         * Creates a single light source with standard light shader, position is
         * based on bounding box of the scene
         */
        
        //parent node node for shader
        Element lightShader = xmlFile.createElement("lightshader");
        lightShader.setAttribute("id", "pointlight1");
        lightShader.setAttribute("script", "urn:xml3d:lightshader:point");
        
        //light intensity
        Element intensityNode = xmlFile.createElement("float3");
        intensityNode.setAttribute("name", "intensity");
        intensityNode.setTextContent("1.0 1.0 1.0");
        
        //Attenuation
        Element attenuationNode = xmlFile.createElement("float3");
        attenuationNode.setAttribute("name", "attenuation");
        attenuationNode.setTextContent("1 0 0");
        
        //cast Shadows?
        Element shadowNode = xmlFile.createElement("bool");
        shadowNode.setAttribute("name", "castShadow");
        shadowNode.setTextContent("true");
        
        //append children to parent node
        lightShader.appendChild(intensityNode);
        lightShader.appendChild(attenuationNode);
        lightShader.appendChild(shadowNode);
        
        defs.appendChild(lightShader);
        
        //get coordinates of upper boundary box corner for light position
        String upperPos = gmlTree.getSceneBoundary("upper");
        
        
    }
    void writeToFile() throws Exception
    {
        /*
         * Transforms the created xml-Tree to a string and
         * writes the result to the given filename
         */
     try{
        //Set up a transformer to create awell formatted string from the dom tree
        TransformerFactory transfac = TransformerFactory.newInstance();
        transfac.setAttribute("indent-number", new Integer(2));
        Transformer optimusPrime = transfac.newTransformer();
        //optimusPrime.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        optimusPrime.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        optimusPrime.setOutputProperty(OutputKeys.INDENT, "yes");
        
        
        //create string from xml tree
        StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
        DOMSource source = new DOMSource(xmlFile);
        optimusPrime.transform(source, result);
        String xmlString = sw.toString();
        
            //print xml
        //System.out.println("Here's the xml:\n\n" + xmlString);
        try
        {
        xmlWriter.write(xmlString);
        xmlWriter.close();
        } catch (Exception e)
        {
            System.out.println("Could not write to file: " + e );
        }
     }
     catch(Exception e)
     {
         System.out.println("EXCEPTION!! " + e);
     }

    
    } 
    
} 
