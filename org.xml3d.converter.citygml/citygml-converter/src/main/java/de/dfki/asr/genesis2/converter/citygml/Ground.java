/**
 * Parses the ground mesh, defined as triangulated surface, from
 * the gml-File
 * @author Daniel
 */

package de.dfki.asr.genesis2.converter.citygml;
import java.util.ArrayList;
import org.w3c.dom.*;
import java.util.Map;
import java.util.HashMap;
import org.jboss.logging.Logger;
public class Ground {
    
    private static final Logger log = Logger.getLogger(Ground.class.getName());
    private String id;                                              //ground id, retrieved either from gml file or uniquely generated
    private ArrayList<XmlPoly> poly = new ArrayList<XmlPoly>();     //list of polygons that define the building
    private Element groundNode;                                   //ground Node in the gmlTree 
    
    /**
     * Read the position list from the given polygon and return the values as a list of 3d vectors. The function will both check for
     * polygons defined via a list of "gml:pos" tags and those defined by a single "gml:posList"
     * @param polygon The polygon to be processed
     * @return List of 3D vectors defining the given polygon
     */
    private ArrayList<Vec3D> getPositions(Element polygon)
    {
        //Get pos or PosList-Tag from the gml file, store each vertex as 3D-Vector and add to list
        ArrayList<Vec3D> positions = new ArrayList<Vec3D>();        //initialize empty vector for storing positions
        NodeList posTags = polygon.getElementsByTagName("gml:pos");
        if(posTags.getLength() > 0)
        {
            for(int i=0; i<posTags.getLength(); i++)
            {
                if(posTags.item(i).getNodeName() != null && posTags.item(i).getNodeName().equals("gml:pos"))
                {
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
        if(posListTags.getLength() > 0)
        {
            for(int i=0; i<posListTags.getLength(); i++)
            {
                String vertexPos = posListTags.item(i).getTextContent();
                String splitPos[] = vertexPos.split("\\s+");
                
                for(int j=0; j<(splitPos.length / 3); j++)
                {   
                    try{
                        Vec3D posVector = new Vec3D(Double.parseDouble(splitPos[j*3]), Double.parseDouble(splitPos[(j*3)+1]), Double.parseDouble(splitPos[(j*3)+2]));
                        positions.add(posVector);
                    } catch(Exception e)
                    {
                        log.warn("Encountered an empty position string for object " + this.id);
                    }
                    
                }
            }
            
            return positions;
        }
        
        return positions;   //just in case something went wrong, place a return statement here.
    }
    
    
    public void setId(String id)
    {
        this.id = id;
    }
    /**
     * Find the triangles defining the current ground mesh, using the ground's root node from the gml file as root node
     * for the children
     */
    public void gatherTriangles() {
        NodeList triangles = groundNode.getElementsByTagName("gml:Triangle");
        
        for(int i=0; i<triangles.getLength(); i++)
        {
            if(triangles.item(i) != null)
            {
                if(triangles.item(i).getNodeName().equals("gml:Triangle"))      //found a triangle: generate id, store in polygon-list
                {
                    Element triangleElement = (Element)triangles.item(i);
                    String id = "groundTriangle_"+i; 
                    ArrayList<Vec3D> positions = getPositions(triangleElement);
                    XmlPoly foundPoly = new XmlPoly();
                    foundPoly.setId(id);
                    foundPoly.setPositions(positions);
                    foundPoly.computeNormals();
                    foundPoly.simpleTriangulation();
                    poly.add(foundPoly);
                }
                
            }
        }
    }
    
    
    
    public String getID()
    {
        return this.id;
    }
    
    Ground(Element groundNode)
    {
        this.groundNode = groundNode;
    }
    
    Ground()
    {
        
    }
    /**
     * Return the list of polygons defining the current ground object
     * @return 
     */
    public ArrayList<XmlPoly> getPolyList()
    {
        return poly;
    }
}
