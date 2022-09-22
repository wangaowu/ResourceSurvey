package org.osmdroid.customImpl.shp.shapefile.shape.shapes;

import org.osmdroid.customImpl.shp.shapefile.ValidationPreferences;
import org.osmdroid.customImpl.shp.shapefile.shape.AbstractShape;
import org.osmdroid.customImpl.shp.shapefile.shape.ShapeHeader;
import org.osmdroid.customImpl.shp.shapefile.shape.ShapeType;
import org.osmdroid.customImpl.shp.shapefile.util.ISUtil;

import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractPointShape extends AbstractShape {

    private double x;
    private double y;

    public AbstractPointShape(final ShapeHeader shapeHeader,
                              final ShapeType shapeType, final InputStream is,
                              final ValidationPreferences rules) throws IOException {
        super(shapeHeader, shapeType, is, rules);

        this.x = ISUtil.readLeDouble(is);
        this.y = ISUtil.readLeDouble(is);
    }

    // Getters

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

}
