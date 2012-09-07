package de.dfki.asr.genesis2.converter.citygml;

/**
 * The xmlProcessor class is used to process the stored data read from the gml files
 * to push them to the xml3DWriter package.
 * @author Daniel
 */
import de.dfki.asr.genesis2.converter.xmlWriter.XML3DGroup;
import de.dfki.asr.genesis2.converter.xmlWriter.XML3DMesh;
import de.dfki.asr.genesis2.converter.xmlWriter.XML3DShader;

import de.dfki.asr.genesis2.converter.xmlWriter.XML3DCamera;

import java.util.Map;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.*;
import org.jboss.logging.Logger;


import de.dfki.asr.genesis2.converter.xmlWriter.XML3DWriter;
import javax.xml.transform.stream.StreamResult;

public class xmlProcessor {

    private XML3DWriter writer;
    private static final Logger log = Logger.getLogger(xmlProcessor.class.getName());
    /**
     * Standard constructor, initializes the xml3dWriter to be used.
     */
    public xmlProcessor() {
        try {
            this.writer = new XML3DWriter();
        } catch (Exception e) {
            log.error("Exception encountered while creating xml3DWriter: " + e);
        }

    }

    /**
     * Constructor using a file name, initializes the xml3dWriter to be used.
     * Additionally, the debug mode of the XML3DWriter is enabled and generated code
     * is written to disk
     */
    public xmlProcessor(String fileName) {
        try {
            this.writer = new XML3DWriter(fileName);
            this.writer.enableDebug();
            this.writer.setOutputPath(fileName);
            

        } catch (Exception e) {
            log.error("Exception encountered while creating xml3DWriter: " + e);

        }

    }
    

    /**
     * The processBuildings method iterates through the list of stored buildings and
     * creates meshes, groups and subgroups as needed by the xml3DWriter class.
     * @param buildingMap The list of buildings to be processed. For each building, a parent group will be generated. Subgroups referencing building polygons are
     * added as children to the parent building group
     * @param shaderMap List of shaders defined in the xml document. If one of the buildings polygons appears as key in the shader map (thus, is using this shader), 
     * that shader will be used as attribute for the corresponding polygon.
     * @param polygonMap Lit of polygons this building consists of. Polygons will be added as children to the building's parent group.
     */
    public void processBuildings(Map buildingMap, Map shaderMap, Map polygonMap, Map semanticMap) {
        Iterator buildingIter = buildingMap.keySet().iterator();
        while (buildingIter.hasNext()) {
            Building currentBuilding = (Building) buildingMap.get(buildingIter.next());
            XML3DGroup buildingParent = new XML3DGroup(currentBuilding.getID() + "_id");    //get next building from list and initialize empty parent group

            buildingParent.addAttribute("class", currentBuilding.getID());

            //add mouse over handlers to parent group
            buildingParent.addAttribute("onmouseover", "handleOnMouseOver(this);");
            buildingParent.addAttribute("onmousemove", "handleOnMouseMove(this);");
            buildingParent.addAttribute("onmouseout", "handleOnMouseOut(this);");

            //get list of polygons defining the building, create a mesh for each polygon found and add
            //the corresponding subgroup to the building currently processed

            ArrayList<XmlPoly> poly = currentBuilding.getPolyList();
            Iterator<XmlPoly> polyIter = poly.iterator();
            while (polyIter.hasNext()) {
                XmlPoly currentPoly = polyIter.next();

                //create data element for the current polygon
                XML3DMesh newMesh = new XML3DMesh(currentPoly.getID());

                //positions
                String posString = "";
                Iterator<Vec3D> posIter = currentPoly.getPositions().iterator();

                while (posIter.hasNext()) {
                    Vec3D currentPos = posIter.next();
                    posString = posString + (currentPos.coordString()) + " ";
                    posIter.remove();
                }
                newMesh.setPositions(posString);

                //normals
                String normalString = "";
                Iterator<Vec3D> normIter = currentPoly.getNormals().iterator();
                while (normIter.hasNext()) {
                    Vec3D currentNorm = normIter.next();
                    normalString = normalString + (currentNorm.coordString()) + " ";
                    normIter.remove();
                }
                newMesh.setNormals(normalString);
                //texture coordinates
                String texCoordString = "";
                if (polygonMap.keySet().contains("#" + currentPoly.getID())) {
                    xmlShader myShader = (xmlShader) polygonMap.get("#" + currentPoly.getID());
                    String myTexCoords = myShader.getTexCoords();
                    if (myShader.getShaderType().equals("Texture")) {
                        newMesh.setTexCoords(myTexCoords);
                    }

                }
                newMesh.setIndex(currentPoly.getIndexString());

                writer.appendMesh(newMesh);
                //Create a group for the currently processed polygon as child of the parent building group
                XML3DGroup polyGroup = new XML3DGroup(currentBuilding.getID() + "_child_" + poly.indexOf(currentPoly), buildingParent);
                XML3DGroup meshReference = new XML3DGroup(polyGroup);
                meshReference.makeMesh();         //reference to mesh definition in the data part
                meshReference.addAttribute("type", "triangles");
                meshReference.addAttribute("src", "#" + currentPoly.getID());
                polyGroup.addAttribute("class", currentBuilding.getID());
                //check whether a shader for the current polygon exists and add the corresponding attribute, if necessary
                if (polygonMap.keySet().contains("#" + currentPoly.getID())) {
                    polyGroup.addAttribute("shader", "#" + ((xmlShader) polygonMap.get("#" + currentPoly.getID())).getID());
                }
            }

            //check whether semantic information for the current building is avaiable and add the corresponding tags if so.
            if (semanticMap.keySet().contains(currentBuilding.getID())) {

                buildingSemantics(buildingParent, (Semantics) semanticMap.get(currentBuilding.getID()), currentBuilding.getID());
            }
            
            writer.appendGroup(buildingParent);
        }
    }

    /**
     * Get semantic information for the current building from the list of stored semantics
     * and add them to the provided building group node.
     * @param buildingGroup The group node in the generated xml tree that will be used as parent node for semantic tags
     * @param semanticMap Semantic information provided for this building
     */
    public void buildingSemantics(XML3DGroup buildingGroup, Semantics buildingSemantic, String buildingID) {
        //create parent node for semantic information, containing the id and class of this div group
        XML3DGroup semanticParent = new XML3DGroup(buildingGroup);
        XML3DGroup addressParent = new XML3DGroup();
        
        semanticParent.addAttribute("id", buildingID + "_RDFa");
        semanticParent.addAttribute("property", "gml:" + buildingID);
        semanticParent.addAttribute("style", "display:none; position:absolute; border-style: solid; background-color: white; padding: 5px;");
        semanticParent.addAttribute("typeof", "bldg:building");
        semanticParent.addAttribute("xmlns", "http://www.w3.org/1999/xhtml");
        semanticParent.makeDiv();

        //create address parent node when current semantic objects contains address information
        if (buildingSemantic.hasAddress()) {
            semanticParent.addChild(addressParent);
            addressParent.addAttribute("property", "xAL:AddressDetails");
            addressParent.makeDiv();
        }
        Iterator semanticIter = buildingSemantic.getSemanticList().entrySet().iterator();
        while (semanticIter.hasNext()) {
            Map.Entry<String, String> semanticEntry = (Map.Entry<String, String>) semanticIter.next();
            String key = semanticEntry.getKey();
            String value = semanticEntry.getValue();
            if(key.contains("xAL"))
            {
                XML3DGroup newXALSemantic = new XML3DGroup(addressParent);
                newXALSemantic.addAttribute("property", key);
                newXALSemantic.addTextContent(value);
                newXALSemantic.makeDiv();
            }
            else
            {
                XML3DGroup newSemantic = new XML3DGroup(semanticParent);
                newSemantic.addAttribute("property", key);
                newSemantic.addTextContent(value);
                newSemantic.makeDiv();
            }
        }
    }

    /**
     * The processGround method reads the ground object (or iterates through the list of ground objects, if the ground is defined via several objects),
     * creates the corresponding xml3D groups and adds the xml3d meshes defining theground object to the main xml3d ground node.
     * @param groundMap List of ground objects to be used
     * @param shaderMap List of shaders to be referenced by the ground object
     * @param polygonMap  List of polygons defining the ground object
     */
    public void processGround(String texturePath, Map groundMap, Map shaderMap, Map polygonMap, float resX, float resY) {
        Iterator groundIter = groundMap.keySet().iterator();
        while (groundIter.hasNext()) {
            Ground currentGround = (Ground) groundMap.get(groundIter.next());
            XML3DGroup groundParent = new XML3DGroup(currentGround.getID() + "_id");
            groundParent.addAttribute("class", currentGround.getID());
            
            String textureFile = new String();
            if(polygonMap.keySet().contains("#" + currentGround.getID()))
            {
                groundParent.addAttribute("shader", "#" + ((xmlShader) polygonMap.get("#" + currentGround.getID())).getID());
                xmlShader myShader = (xmlShader)polygonMap.get("#"+currentGround.getID());
                textureFile = myShader.getTexturePath();
            }
            
            ArrayList<XmlPoly> poly = currentGround.getPolyList();
            Iterator<XmlPoly> polyIter = poly.iterator();

            while (polyIter.hasNext()) {
                XmlPoly currentPoly = polyIter.next();

                //create data element for the current polygon
                XML3DMesh newMesh = new XML3DMesh(currentPoly.getID());

                //positions
                String posString = "";
                Iterator<Vec3D> posIter = currentPoly.getPositions().iterator();

                while (posIter.hasNext()) {
                    Vec3D currentPos = posIter.next();
                    posString = posString + (currentPos.coordString()) + " ";
                    //posIter.remove();
                }
                newMesh.setPositions(posString);
                newMesh.setTexCoords(computeGeoRefCoords(texturePath, textureFile, currentPoly, resX, resY));
                
                
                //normals
                String normalString = "";
                Iterator<Vec3D> normIter = currentPoly.getNormals().iterator();
                while (normIter.hasNext()) {
                    Vec3D currentNorm = normIter.next();
                    normalString = normalString + (currentNorm.coordString()) + " ";
                    normIter.remove();
                }
                newMesh.setNormals(normalString);

                newMesh.setIndex(currentPoly.getIndexString());
                writer.appendMesh(newMesh);
                //Create a group for the currently processed polygon as child of the parent building group
                XML3DGroup polyGroup = new XML3DGroup(currentGround.getID() + "_child_" + poly.indexOf(currentPoly), groundParent);
                XML3DGroup meshReference = new XML3DGroup(polyGroup);
                meshReference.makeMesh();         //use current group as mesh
                meshReference.addAttribute("type", "triangles");
                meshReference.addAttribute("src", "#" + currentPoly.getID());
                polyGroup.addAttribute("class", currentGround.getID());
                //check whether a shader for the current polygon exists and add the corresponding attribute, if necessary
                //additionally, set the texture coordinates for the mesh if shader is a texture
                if (polygonMap.keySet().contains("#" + currentPoly.getID())) {
                    polyGroup.addAttribute("shader", "#" + ((GenericShader) polygonMap.get("#" + currentPoly.getID())).getID());
                    //check for shader type

                }
            }
            writer.appendGroup(groundParent);
        }


    }

    /**
     * Iterates through the list of shaders and creates an appropriate shader element for each one.
     * Depending on whether a texture or a diffuse material is used, a path to the texture file or
     * values for material colors are submitted to the exporter.
     * @param shaderMap The list of shaders to be sent to the exporter
     */
    public void processShader(Map shaderMap) {
        Iterator shaderIter = shaderMap.keySet().iterator();
        while (shaderIter.hasNext()) {
            xmlShader currentShader = (xmlShader) shaderMap.get(shaderIter.next());
            XML3DShader newShader = new XML3DShader(currentShader.getID()); //initialize empty new shader to be added to the writer class.

            if (currentShader.getShaderType().equals("Material")) {
                //The current shader is a material shader: send material to exporter                
                newShader.setDiffuseColor(currentShader.getDiffuseColor()[0], currentShader.getDiffuseColor()[1], currentShader.getDiffuseColor()[2]);
            } else if (currentShader.getShaderType().equals("Texture")) {
                newShader.useAsTexture(true);
                newShader.setTexturePath(currentShader.getTexturePath());
                newShader.setAmbientIntensity(0.4f);
                newShader.setDiffuseColor(1.0f, 1.0f, 1.0f);
            }

            writer.appendShader(newShader);
        }
    }

    /**
     * Adds a camera to the scene, based on the scenes bounding box coordinates
     */
    public void addCamera(gmlParser gmlTree) {
        String lowerCorner = gmlTree.getSceneBoundary("lower");
        String upperCorner = gmlTree.getSceneBoundary("upper");
        //place camera over center of the scene
        String[] lowerSplit = lowerCorner.split("\\s+");
        String[] upperSplit = upperCorner.split("\\s+");

        Float lowerX = Float.valueOf(lowerSplit[0]);
        Float lowerY = Float.valueOf(lowerSplit[1]);

        Float upperX = Float.valueOf(upperSplit[0]);
        Float upperY = Float.valueOf(upperSplit[1]);

        Float targetZ = Float.valueOf(upperSplit[2]) + 100.0f;
        String posString = Float.toString((lowerX + upperX) / 2) + " " + Float.toString((lowerY + upperY) / 2) + " " + Float.toString(targetZ);
        writer.addCamera("defaultView", posString, "0 0 1 0");
    }
    
    
    public String computeGeoRefCoords(String TexturePath, String geoFileName, XmlPoly poly, float resX, float resY)
    {
        ArrayList<String> linesRead = new ArrayList<String>();
        String refFileName = geoFileName.replace("tif", "tfw");
        
        refFileName = refFileName.split("\\.")[0];
        refFileName = refFileName +".tfw";
        String refFilePath = TexturePath + "\\" +refFileName;
        String newCoordinates = new String();
        
        //Read lines from the reference file and add them to the array for further use
        try{
            String currentLine;
            BufferedReader geoReader = new BufferedReader(new FileReader(refFilePath));
            while((currentLine = geoReader.readLine()) != null )
            {
                linesRead.add(currentLine);
            }
        }catch(Exception e)
        {
            log.error("Error reading geo references from file: " + e);
        }
        
        //get size and world coordinates from the lines read from the tfw file
        float sizeX = Float.parseFloat(linesRead.get(0));
        float sizeY = Float.parseFloat(linesRead.get(3));
        
        float texCoordX = Float.parseFloat(linesRead.get(4));
        float texCoordY = Float.parseFloat(linesRead.get(5));
        resX = 2353.0f;
        resY = 1747.0f;
        newCoordinates="";
        for(int p=0; p<poly.getPositions().size(); p++)
        {
            newCoordinates += ((poly.getPositions().get(p).getCoordinate("x") - texCoordX)/(sizeX*resX)) + " " + (1-(poly.getPositions().get(p).getCoordinate("y") - texCoordY)/(sizeY*resY)) +" ";
        }
        return newCoordinates;
    }
    /**
     * This function triggers the startWriting method of the xmlWriter
     */
    public StreamResult processXML() {
        return this.writer.startWriting();

    }
}
