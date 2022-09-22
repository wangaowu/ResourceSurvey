package org.osmdroid.customImpl.shp.shapefile.shape.shapes;

import org.osmdroid.customImpl.shp.shapefile.exception.InvalidShapeFileException;
import org.osmdroid.customImpl.shp.shapefile.ValidationPreferences;
import org.osmdroid.customImpl.shp.shapefile.shape.ShapeHeader;
import org.osmdroid.customImpl.shp.shapefile.shape.ShapeType;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a PolylineM Shape object, as defined by the ESRI Shape file
 * specification.
 */
public class PolylineMShape extends AbstractPolyMShape {

    public PolylineMShape(final ShapeHeader shapeHeader,
                          final ShapeType shapeType, final InputStream is,
                          final ValidationPreferences rules) throws IOException,
            InvalidShapeFileException {

        super(shapeHeader, shapeType, is, rules);

    }

    @Override
    protected String getShapeTypeName() {
        return "PolylineM";
    }

}
