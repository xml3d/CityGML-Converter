
package de.dfki.asr.genesis2.converter.citygml;
import java.util.ArrayList;

/**
 * XmlPolygon
 * Defines a polygon for an xml3d object, containing a list of positions,
 * the indices defining the triangulation, normals and id
 * @author Daniel
 */
public class XmlPoly {
    private String id;
    private ArrayList<Vec3D> positions = new ArrayList<Vec3D>();
    private ArrayList<Vec3D> normals = new ArrayList<Vec3D>();
    private String textureCoordinateString = new String();
    private int index[];
    private String indexString = new String();
    
    public void setId(String id)
    {
        this.id = id;
    }
    
    public void setPositions(ArrayList<Vec3D> positions)
    {
        positions.remove(positions.size()-1);   //throw away last element os position vector, as this appears twice in the gml file
        this.positions = positions;
        
    }
    
    /**
     * Compute normal at each vertex of the polygon. Compute normals of first and
     * last point seperately to avoid problems with array boundaries
     */
    public void computeNormals()
    {
        
        //initialize list of normals to have the same size as positions
        for(int s=0; s<positions.size();s++)
        {
            normals.add(new Vec3D(0.0, 0.0, 0.0));      
        }
        
        //first point
        {
            Vec3D centerPoint = positions.get(0);                    //center point in which the normal is computed
            Vec3D prePoint = positions.get(positions.size()-1);        //predecessor
            Vec3D sucPoint = positions.get(1);        //successor

            Vec3D vector1 = Vec3D.dist(centerPoint, prePoint);
            Vec3D vector2 = Vec3D.dist(centerPoint, sucPoint);

            normals.set(0, Vec3D.normal(vector1, vector2));
        }
        //inner points
        for(int i=1; i<(positions.size()-1); i++)
        {
            Vec3D centerPoint = positions.get(i);       //center point in which the normal is computed
            Vec3D prePoint = positions.get(i-1);        //predecessor
            Vec3D sucPoint = positions.get(i+1);        //successor
            
            Vec3D vector1 = Vec3D.dist(centerPoint, prePoint);
            Vec3D vector2 = Vec3D.dist(centerPoint, sucPoint);
            
            normals.set(i, Vec3D.normal(vector1, vector2));
        }
        
        //last Point
        {
            Vec3D centerPoint = positions.get(positions.size()-1);                   //center point in which the normal is computed
            Vec3D prePoint = positions.get(positions.size()-2);       //predecessor
            Vec3D sucPoint = positions.get(0);                      //successor

            Vec3D vector1 = Vec3D.dist(centerPoint, prePoint);
            Vec3D vector2 = Vec3D.dist(centerPoint, sucPoint);

            normals.set(normals.size()-1, Vec3D.normal(vector1, vector2));
        }
    }
    
    public ArrayList<Vec3D> getPositions()
    {
        return this.positions;
    }
    
    public ArrayList<Vec3D> getNormals()
    {
        return this.normals;
    }
    
    public String getID()
    {
        return this.id;
    }
    
    public void simpleTriangulation()
    {
        int posLength = this.positions.size();
        this.indexString = "0 1 2 ";
        for(int i=1; i<posLength-2; i++)
        {
            this.indexString += "0 " + (i+1) + " " +(i+2) +" ";
        }
    }
    
    public String getIndexString()
    {
        return this.indexString;
    }
}
