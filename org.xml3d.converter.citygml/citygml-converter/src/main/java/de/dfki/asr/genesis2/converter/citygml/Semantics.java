package de.dfki.asr.genesis2.converter.citygml;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A class for extracting and storing semantic information to specific buildings from the gml source
 * file. This information will be given to the xmlWriter to create an appropriate div tag for each information stored.
 * @author Daniel
 */
public class Semantics {

    private String id;
    Boolean createAddress = false;      //If the buildings address is provided, set this flag to true to create an additional address tag
    private HashMap<String, String> semanticList = new HashMap<String, String>();                   //List of semantic information: <property, value>

    
    public Boolean hasAddress()
    {
        return this.createAddress;
    }
    
    public void setAdressFlag(Boolean setAddress)
    {
        this.createAddress = setAddress;
    }
    public Semantics() {
    }

    /**
     * Add information read from the gml file to the semantic's list of information. The stored value pairs will be 
     * written to the xml file in the format "key: value".
     * @param key Type of information to be stored (city name, country, street name...)
     * @param value Specific information to be stored.
     */
    public void addInformation(String key, String value) {
        this.semanticList.put(key, value);
    }

    public HashMap<String, String> getSemanticList() {
        return this.semanticList;
    }
}
