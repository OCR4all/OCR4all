package de.uniwue.feature.pageXML;

public class RegionRefIndexed {

    private String index;
    private String regionRef;

    public RegionRefIndexed(String index, String regionRef) {
        setIndex(index);
        setRegionRef(regionRef);
    }

    public String getIndex() {
        return index;
    }
    
    public void setIndex(String index) {
        this.index = index;
    }
    
    public String getRegionRef() {
        return regionRef;
    }

    public void setRegionRef(String regionRef) {
        this.regionRef = regionRef;
    }
}