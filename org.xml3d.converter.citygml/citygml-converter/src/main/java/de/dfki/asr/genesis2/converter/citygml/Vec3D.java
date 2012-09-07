/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.asr.genesis2.converter.citygml;
import java.lang.Math;
/**
 * Class for simple 3D Vector computations
 * @author Daniel
 */
public class Vec3D {
    // Vector coordinates
    private double x;
    private double y;
    private double z;
    
    //Constructor, takes three floate values to initialize coordinates
    Vec3D(double newx,double newy,double newz){
        x = newx;
        y = newy;
        z = newz;
    }
    //Copy constructor
    Vec3D(Vec3D vector)
    {
        x = vector.x;
        y = vector.y;
        z = vector.z;
    }
    
    //Products: Dot and Cross
    
    static public double dot(Vec3D vec1, Vec3D vec2) {
        return (vec1.x * vec2.x + vec1.y * vec2.y + vec1.z * vec2.z);
    }
    
    static public Vec3D dist(Vec3D vec1, Vec3D vec2) {
        return new Vec3D(vec1.x - vec2.x, vec1.y - vec2.y, vec1.z - vec2.z);
    }
    static public Vec3D cross(Vec3D vec1, Vec3D vec2){
        return new Vec3D(vec1.y*vec2.z - vec1.z*vec2.y, vec1.z*vec2.x - vec1.x*vec2.z, vec1.x*vec2.y - vec1.y*vec2.x);
    }
    
    //Absolute Value
    static public double abs(Vec3D vec){
        return Math.sqrt(Vec3D.dot(vec, vec));
    }
    
    //Simple operations
    
    static public Vec3D div(Vec3D vec, double val)
    {
        return new Vec3D(vec.x/val, vec.y/val, vec.z/val);
    }
    
    //Compute vector normal
    static public Vec3D normal(Vec3D vec1, Vec3D vec2) {
        Vec3D normalVec = new Vec3D(0, 0, 0);
        Vec3D cross = Vec3D.cross(vec1, vec2);
        double vecAbs = Vec3D.abs(cross);
        if(vecAbs != 0)
            normalVec = Vec3D.div(cross, vecAbs);
        
        return normalVec;
    }
    
    public double getCoordinate(String coord)
    {
        if(coord.equals("x"))
            return this.x;
        if(coord.equals("y"))
            return this.y;
        if(coord.equals("z"))
            return this.z;
        
        return Double.MIN_VALUE;
    }
    
    public String coordString()
    {
        String resultString = this.getCoordinate("x")+" "+this.getCoordinate("y")+" "+this.getCoordinate("z")+" ";
        
        return resultString;
    }
}
