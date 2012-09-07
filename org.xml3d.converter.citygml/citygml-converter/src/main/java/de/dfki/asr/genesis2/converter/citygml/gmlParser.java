package de.dfki.asr.genesis2.converter.citygml;

import java.io.InputStream;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import org.jboss.logging.*;
/**
 * The gmlParser reads building, shader, ground and semantic information from the gml file and stores the values
 * found therein in a specific object for each type.
 * These objects will be handed to an xmlProcessor in a second step to prepare the stored information for xml3D compatible
 * output
 * @author Daniel Spieldenner
 */
public class gmlParser {

    static private Document gmlTree;                   //gmlTree from provided gml-file
    private Node root;                          //rootNode of the Tree
    private Map<String, String> namespaces;     //list of namespace prefixes with their URIs

    /**
     * Constructor. Initialize a new gmlParser object to process the given gml file
     * @param filename The gml file to be used
     * @throws Exception 
     */
    gmlParser(String filename) throws Exception {
        //set up document builder to read gml-Files
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        gmlTree = builder.parse(filename);
        gmlTree.normalize();
        root = gmlTree.getDocumentElement();
        namespaces = new HashMap<String, String>();
        setUris(namespaces);

    }
    
    /**
     * Constructor for a new gmlParser using a InputStream as source for the gml data to be parsed
     * @param is Input stream to be used
     * @throws Exception 
     */
    gmlParser(InputStream is) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        gmlTree = builder.parse(is);
        gmlTree.normalize();
        root = gmlTree.getDocumentElement();
        namespaces = new HashMap<String, String>();
        setUris(namespaces);
    }

    //setter and getter functions
    public Document getTree() {
        return gmlTree;
    }

    /**
     * Get one boundary corner of the current scene.
     * @param Either "lower" or "upper"
     * @return minimum or maximum corner, depending on input argument
     */
    public String getSceneBoundary(String position) {
        String upperString = new String();
        String lowerString = new String();
        NodeList boundingBox = gmlTree.getElementsByTagName("gml:boundedBy");
        for (int bb = 0; bb < boundingBox.getLength(); bb++) {

            Element boxElement = (Element) boundingBox.item(bb);
            NodeList upperCornerList = boxElement.getElementsByTagName("gml:upperCorner");
            NodeList lowerCornerList = boxElement.getElementsByTagName("gml:lowerCorner");
            for (int lc = 0; lc < lowerCornerList.getLength(); lc++) {
                Node currentCorner = lowerCornerList.item(lc);
                lowerString = currentCorner.getTextContent();
            }
            for (int uc = 0; uc < upperCornerList.getLength(); uc++) {
                Node currentCorner = upperCornerList.item(uc);
                upperString = currentCorner.getTextContent();
            }

        }
        if (position.equals("upper")) {
            return upperString;
        }
        if (position.equals("lower")) {
            return lowerString;
        }
        return "ERROR: No corner of boundary box specified";
    }

    public void setUris(Map uriMap) {
        /*
         * Set namespaces and store URI for each prefix
         */
        uriMap.put("bldg", gmlTree.lookupNamespaceURI("xmlns"));

    }

    /**
     * Parse the buildings in the current gmlFile and store all found buildings in the provided list.
     * While parsing the building, also check for semantic information and store all information found in the
     * semantic map.
     * @param buildingMap List of buildings defined in the gmlFile
     * @param semanticMap List of containers storing semantic information for specific buildings
     */
    public void parseBuildings(Map buildingMap, Map semanticMap) {

        NodeList buildings = gmlTree.getElementsByTagName("bldg:Building");
        for (int i = 0; i < buildings.getLength(); i++) //iterate over list of buildings
        {
            if (buildings.item(i) != null) {
                Building building = new Building((Element) buildings.item(i));
                building.gatherPolygons();
                parseSemantics((Element) buildings.item(i), semanticMap);

                building.setId(buildings.item(i).getAttributes().getNamedItem("gml:id").getNodeValue());
                buildingMap.put(buildings.item(i).getAttributes().getNamedItem("gml:id").getNodeValue(), building);
            }
        }

    }

    /**
     * Read the appearance tags in the gml file and create a texture or material shader depending on the exact gml definition.
     * Store the target polygons of the given shaders to be able to assign them correctly afterwards.
     * @param shaderMap List of shaders defined in the gml file.
     * @param polygonMap List of gml polygons used as targets for the shaders.
     */
    public void parseShader(Map shaderMap, Map polygonMap) {

        NodeList shaders = gmlTree.getElementsByTagName("app:Appearance");
        for (int i = 0; i < shaders.getLength(); i++) {
            ShaderFactory shader = new ShaderFactory((Element) shaders.item(i));
            //xmlShader shader = new xmlShader((Element)shaders.item(i));
            shader.getMaterial(shaderMap, polygonMap);
            shader.getTextures(shaderMap, polygonMap);
        }

        //generate standard shaders to be used by buildings with no shader information
        ShaderFactory standardShaders = new ShaderFactory();
        standardShaders.generateStandardShaders(shaderMap);
    }

    /**
     * Read the ground object definition from the gmlFile and store each object in a list for further processing.
     * @param groundMap The list of ground objects defined in the gml file.
     */
    public void parseGround(Map groundMap, Map buildingMap) {

        NodeList groundDefs = gmlTree.getElementsByTagName("dem:ReliefFeature");                         //Get relieef feature usually used to define ground objects
        NodeList landUseDefs = gmlTree.getElementsByTagName("luse:LandUse");                           //Also consider triangle definition in landUse tags
        for (int i = 0; i < groundDefs.getLength(); i++) {

            if (groundDefs.item(i) != null) //relief feature found: look for triangulated surfaces / multi point
            {
                Element groundElem = (Element) groundDefs.item(i);
                Ground groundObject = new Ground(groundElem);
                NodeList triangulatedSurfs = groundElem.getElementsByTagName("gml:TriangulatedSurface");    //check for triangulated surfaces
                for (int t = 0; t < triangulatedSurfs.getLength(); t++) {
                    if (triangulatedSurfs.item(t) != null) {
                        Element surface = (Element) triangulatedSurfs.item(t);          //get triangulated surface as element to extract polygons
                        groundObject.setId(triangulatedSurfs.item(t).getAttributes().getNamedItem("gml:id").getNodeValue());
                        if (groundObject.getID().isEmpty()) {
                            groundObject.setId("groundObject_" + groundMap.size());
                        }
                        groundObject.gatherTriangles();

                    }
                }

                groundMap.put(groundObject.getID(), groundObject);


            }
        }
        
        for(int lu = 0; lu < landUseDefs.getLength(); lu++)
        {
            Element currentLandUse = (Element)landUseDefs.item(lu);
            Building groundObject = new Building(currentLandUse);
            groundObject.gatherPolygons();
            
            groundObject.setId(currentLandUse.getAttributes().getNamedItem("gml:id").getNodeValue());
            buildingMap.put(currentLandUse.getAttributes().getNamedItem("gml:id").getNodeValue(), groundObject);
        }

    }

    /**
     * Get the semantic information provided for a given building. Starting from a given
     * building node, it is ensured that only information relevant to that building is
     * extracted from the gml file.
     * @param buildingNode Root node for finding semantic information
     */
    public void parseSemantics(Element buildingNode, Map semanticMap) {
        Semantics newSemantic = new Semantics();
        String buildingId = buildingNode.getAttribute("gml:id"); //corresponding building as key for the map

        //get general information about the current building
        NodeList buildingDescription = buildingNode.getElementsByTagName("gml:description");
        NodeList buildingName = buildingNode.getElementsByTagName("gml:name");
        NodeList yearOfConst = buildingNode.getElementsByTagName("bldg:yearOfConstruction");
        NodeList height = buildingNode.getElementsByTagName("bldg:measuredHeight");
        NodeList storeysAbove = buildingNode.getElementsByTagName("bldg:storeysAboveGround");
        NodeList storeysBelow = buildingNode.getElementsByTagName("bldg:storeysBelowGround");

        if (!semanticAddInformation(buildingDescription).isEmpty()) {
            newSemantic.addInformation("gml:description", semanticAddInformation(buildingDescription));
        }
        if (!semanticAddInformation(buildingName).isEmpty()) {
            newSemantic.addInformation("gml:description", semanticAddInformation(buildingName));
        }
        if (!semanticAddInformation(yearOfConst).isEmpty()) {
            newSemantic.addInformation("bldg:yearOfConstruction", semanticAddInformation(yearOfConst));
        }
        if (!semanticAddInformation(storeysAbove).isEmpty()) {
            newSemantic.addInformation("bldg:storeysAboveGround", semanticAddInformation(storeysAbove));
        }
        if (!semanticAddInformation(storeysBelow).isEmpty()) {
            newSemantic.addInformation("bldg:storeysBelowGround", semanticAddInformation(storeysBelow));
        }
        if (!semanticAddInformation(height).isEmpty()) {
            newSemantic.addInformation("bldg:measuredHeight", semanticAddInformation(height));
        }

        //get address information for the current building
        NodeList addressNodes = buildingNode.getElementsByTagName("bldg:address");
        if (addressNodes.getLength() > 0) {
            Element myAddress = (Element) addressNodes.item(0);
            NodeList country = myAddress.getElementsByTagName("xAL:CountryName");
            NodeList locality = myAddress.getElementsByTagName("xAL:Locality");
            NodeList thoroughfare = myAddress.getElementsByTagName("xAL:Thoroughfare");
            NodeList postalCode = myAddress.getElementsByTagName("xAL:PostalCodeNumber");

            //For each node list, check whether such a node exists in the current document and get the information contained therein
            for (int c = 0; c < country.getLength(); c++) {
                Element currentCountry = (Element) country.item(c);
                String countryString = currentCountry.getTextContent();
                if (!countryString.isEmpty()) {
                    newSemantic.addInformation("xAL:CountryName", countryString);
                    newSemantic.setAdressFlag(true);
                }
            }


            for (int l = 0; l < locality.getLength(); l++) {
                Element currentLocality = (Element) locality.item(l);
                if (currentLocality.getAttribute("Type").equals("Town")) {
                    NodeList localityName = currentLocality.getElementsByTagName("xAL:LocalityName");
                    for (int ln = 0; ln < localityName.getLength(); ln++) {
                        Element currentName = (Element) localityName.item(ln);
                        String townString = currentName.getTextContent();
                        if (!townString.isEmpty()) {
                            newSemantic.addInformation("xAL:LocalityName", townString);
                            newSemantic.setAdressFlag(true);
                        }
                    }
                }
            }

            //Before adding information to the semantic list, extract street name and number
            //from the thoroughfare node.
            for (int t = 0; t < thoroughfare.getLength(); t++) {
                Element currentThoroughfare = (Element) thoroughfare.item(t);
                if (currentThoroughfare.getAttribute("Type").equals("Street")) {
                    NodeList streetName = currentThoroughfare.getElementsByTagName("xAL:ThoroughfareName");
                    NodeList number = currentThoroughfare.getElementsByTagName("xAL:ThoroughfareNumber");
                    for (int sn = 0; sn < streetName.getLength(); sn++) {
                        Element currentStreetName = (Element) streetName.item(sn);
                        String streetNameString = currentStreetName.getTextContent();
                        if (!streetNameString.isEmpty()) {
                            newSemantic.addInformation("xAL:StreetName", streetNameString);
                            newSemantic.setAdressFlag(true);
                        }
                    }

                    for (int tn = 0; tn < number.getLength(); tn++) {
                        Element currentThoroughfareNumber = (Element) number.item(tn);
                        String numberString = currentThoroughfareNumber.getTextContent();
                        if (!numberString.isEmpty()) {
                            newSemantic.addInformation("xAL:ThoroughfareNumber", numberString);
                            newSemantic.setAdressFlag(true);
                        }
                    }
                }
            }

            for (int pc = 0; pc < postalCode.getLength(); pc++) {
                Element currentPostalCode = (Element) postalCode.item(pc);
                String postalString = currentPostalCode.getTextContent();
                if (!postalString.isEmpty()) {
                    newSemantic.addInformation("xAL:PostalCodeNumber", postalString);
                    newSemantic.setAdressFlag(true);
                }
            }
        }

        //If some information was read from the gml file, add the generated semantic object to the list.
        if (!newSemantic.getSemanticList().isEmpty()) {
            semanticMap.put(buildingId, newSemantic);
        }
    }

    /**
     * Get the node containiing semantic information from the provided nodeList
     * and extract the string
     * @param semantic
     * @param semanticNode 
     */
    private String semanticAddInformation(NodeList semanticNode) {
        String semanticString = new String();
        for (int nl = 0; nl < semanticNode.getLength(); nl++) {
            Element currentNode = (Element) semanticNode.item(nl);
            semanticString = currentNode.getTextContent();
        }

        return semanticString;
    }
}
