/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.asr.genesis2.converter.citygml;

import java.util.ArrayList;
import org.w3c.dom.*;
import java.util.Map;
import java.util.HashMap;



/**
 * Class for storing cityGML building information. The information stored herein will later be used
 * to generate xml3d compatible data and send this to the xml3DWriter object
 * @author Daniel
 */
public class Building {

    private String id;                                              //building id, retrieved either from gml file or uniquely generated
    private ArrayList<XmlPoly> poly = new ArrayList<XmlPoly>();     //list of polygons that define the building
    private Element buildingNode;                                   //building Node in the gmlTree 

    /**
     * Get the positions defining a given polygon from the gml file and add store them
     * as 3D vector
     * @param polygon The polygon to be processed
     * @return List of positions defining this polygon
     */
    private ArrayList<Vec3D> getPositions(Element polygon) {
        //Get pos or PosList-Tag from the gml file, store each vertex as 3D-Vector and add to list
        ArrayList<Vec3D> positions = new ArrayList<Vec3D>();        //initialize empty vector for storing positions
        NodeList posTags = polygon.getElementsByTagName("gml:pos");
        if (posTags.getLength() > 0) {
            for (int i = 0; i < posTags.getLength(); i++) {
                if (posTags.item(i).getNodeName() != null && posTags.item(i).getNodeName().equals("gml:pos")) {
                    //Found vertex definitions as single pos elements: Store each one as a single vector
                    String vertexPos = posTags.item(i).getTextContent();
                    String splitPos[] = vertexPos.split("\\s");

                    Vec3D posVector = new Vec3D(Double.parseDouble(splitPos[0]), Double.parseDouble(splitPos[1]), Double.parseDouble(splitPos[2]));
                    positions.add(posVector);
                }
            }

            return positions;                                   //As positions are read from single pos tags, there is no need to check for posList-Tags: return
        }

        NodeList posListTags = polygon.getElementsByTagName("gml:posList");
        if (posListTags.getLength() > 0) {
            for (int i = 0; i < posListTags.getLength(); i++) {
                String vertexPos = posListTags.item(i).getTextContent();
                String splitPos[] = vertexPos.split("\\s");

                for (int j = 0; j < (splitPos.length / 3); j++) {
                    Vec3D posVector = new Vec3D(Double.parseDouble(splitPos[j * 3]), Double.parseDouble(splitPos[(j * 3) + 1]), Double.parseDouble(splitPos[(j * 3) + 2]));
                    positions.add(posVector);
                }
            }

            return positions;
        }

        return positions;   //just in case something went wrong, place a return statement here.
    }

    /**
     * Create a new building object using the provided node as root node of the building definition in the gml file
     * @param buildingNode The root node of the building
     */
    Building(Element buildingNode) {
        this.buildingNode = buildingNode;
    }

    Building() {
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getID() {
        return id;
    }

    /**
     * Get the list of polygons defining the current building
     * @return 
     */
    public ArrayList<XmlPoly> getPolyList() {
        return poly;
    }

    /**
     * Get all polygon definitions for the current building from the gml file and
     * store them in the building's list of polygons.
     */
    public void gatherPolygons() {

        NodeList children = buildingNode.getElementsByTagName("gml:Polygon");

        for (int i = 0; i < children.getLength(); i++) //iterate over children and create a list of polygons
        {
            if (children.item(i).getNodeName() != null) {
                if (children.item(i).getNodeName().equals("gml:Polygon")) {
                    //For each polygon found, retrieve positions and id to store in the polygon
                    Element polyElement = (Element) children.item(i);
                    String id = polyElement.getAttribute("gml:id");
                    ArrayList<Vec3D> positions = getPositions(polyElement);
                    XmlPoly foundPoly = new XmlPoly();
                    foundPoly.setId(id);

                    foundPoly.setPositions(positions);
                    foundPoly.computeNormals();

                    foundPoly.simpleTriangulation(); //perform a simple triangulation step
                    poly.add(foundPoly);
                }
            }

        }


    }
}
