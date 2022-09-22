package org.osmdroid.customImpl.shp.shapefile.shape;

import org.osmdroid.customImpl.shp.shapefile.ValidationPreferences;

import java.io.InputStream;

public abstract class AbstractShape {

    protected ShapeHeader header;
    protected ShapeType shapeType;

    public AbstractShape(final ShapeHeader shapeHeader,
                         final ShapeType shapeType, final InputStream is,
                         final ValidationPreferences rules) {
        this.header = shapeHeader;
        this.shapeType = shapeType;
    }

    // Getters

    public final ShapeHeader getHeader() {
        return header;
    }

    public ShapeType getShapeType() {
        return shapeType;
    }

}
