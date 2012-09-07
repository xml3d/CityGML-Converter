package de.dfki.asr.genesis2.converter.citygml;

import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.parsers.*;
import org.w3c.dom.Document;
import java.lang.String;
import java.util.Map;
import java.util.HashMap;
import javax.xml.transform.stream.StreamResult;

/**
 * The main class for the gmlConverter. The converter will invoke the parsing methods for
 * reading building, shader, ground and semantic information from the provided gml file, convert the values
 * to a xml3D compatible format and hand the resulting data to a xmlProcessor to generate xml3D compatible output.
 * @author Daniel Spieldenner
 */
public class GmlConverter {

    /**
     * @param args the command line arguments for the command line version: 1.) input file, 2.) output file, [3.) offset (optional)]
     *
     */
    public static void main(String[] args) throws Exception {
        float resX = 1.0f;
        float resY = 1.0f;
        if(args.length == 5)
        {
            resX = Float.valueOf(args[3]);
            resY = Float.valueOf(args[4]);
        }
       
        initializeGmlParser(args[0], args[1], args[2], resX, resY);
        
    }

    /**
     * Start parsing the given gml file and create output at the specified location (command line version)
     * @param inputFile Path to the input file
     * @param outputFile Path to the output file
     */
    public static void initializeGmlParser(String inputFile, String outputFile, String texturePath, float resX, float resY) {
        
        try{
        gmlParser gmlTree = new gmlParser(inputFile);                                             //create gml-Tree by parsing given file
        Map<String, Building> buildingMap = new HashMap<String, Building>();                    //Map storing buildings with their ids
        //Map<String, GenericShader> shaderMap = new HashMap<String, GenericShader>();    
        Map<String, xmlShader> shaderMap = new HashMap<String, xmlShader>();
        Map<String, Ground> groundMap = new HashMap<String, Ground>();                           //Map storing ground objects
        Map<String, xmlShader> polygonMap = new HashMap<String, xmlShader>();                   //Map storing polygons for storing shader information   
        Map<String, Semantics> semanticMap = new HashMap<String, Semantics>();                  //Map storing semantic information with the corresponding buildnig's id
        Ground ground = new Ground();                                                           // Ground object to be used
        String fileName = outputFile;
        xmlProcessor processor = new xmlProcessor(fileName);
        //xmlWriter xmlwriter = new xmlWriter(fileName);

        /*
         * Start parsing the gmlTree here, starting with the buildings
         * to create the underlying class structure
         */


        gmlTree.parseBuildings(buildingMap, semanticMap);
        gmlTree.parseShader(shaderMap, polygonMap);
        gmlTree.parseGround(groundMap, buildingMap);
        processor.processBuildings(buildingMap, shaderMap, polygonMap, semanticMap);
        processor.processGround(texturePath, groundMap, shaderMap, polygonMap, resX, resY);
        processor.processShader(shaderMap);
        processor.addCamera(gmlTree);
        processor.processXML();
        }catch(Exception e)
        {
            System.out.println("Error converting gml to xml3d:" + e);
        }
    }

    /**
     * Start parsing a gml file provided by the given input stream and generate the specified output stream
     * @param in InputStream to be used
     * @param out OutputStream
     */
    public StreamResult initializeGmlParser(InputStream in, String texturePath) {
        StreamResult result = new StreamResult(); 
        Float resX = 0.0f;
        Float resY = 0.0f;
        try{
        gmlParser gmlTree = new gmlParser(in);                                             //create gml-Tree by parsing given file
        Map<String, Building> buildingMap = new HashMap<String, Building>();                    //Map storing buildings with their ids
        //Map<String, GenericShader> shaderMap = new HashMap<String, GenericShader>();    
        Map<String, xmlShader> shaderMap = new HashMap<String, xmlShader>();
        Map<String, Ground> groundMap = new HashMap<String, Ground>();                           //Map storing ground objects
        Map<String, xmlShader> polygonMap = new HashMap<String, xmlShader>();                   //Map storing polygons for storing shader information   
        Map<String, Semantics> semanticMap = new HashMap<String, Semantics>();                  //Map storing semantic information with the corresponding buildnig's id
        Ground ground = new Ground();                                                           // Ground object to be used
        
        xmlProcessor processor = new xmlProcessor();
        //xmlWriter xmlwriter = new xmlWriter(fileName);

        /*
         * Start parsing the gmlTree here, starting with the buildings
         * to create the underlying class structure
         */


        gmlTree.parseBuildings(buildingMap, semanticMap);
        gmlTree.parseShader(shaderMap, polygonMap);
        gmlTree.parseGround(groundMap, buildingMap);
        processor.processBuildings(buildingMap, shaderMap, polygonMap, semanticMap);
        processor.processGround(texturePath, groundMap, shaderMap, polygonMap, resX, resY);
        processor.processShader(shaderMap);
        processor.addCamera(gmlTree);
        result = processor.processXML();
        }catch(Exception e)
        {
            System.out.println("Error converting gml to xml3d:" + e);
        }
        return result;
    }
    
    
}
