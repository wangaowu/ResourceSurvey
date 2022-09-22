package org.osmdroid.customImpl.shp.shapefile.shape.shapes;

import org.osmdroid.customImpl.shp.shapefile.exception.InvalidShapeFileException;
import org.osmdroid.customImpl.shp.shapefile.ValidationPreferences;
import org.osmdroid.customImpl.shp.shapefile.shape.Const;
import org.osmdroid.customImpl.shp.shapefile.shape.ShapeHeader;
import org.osmdroid.customImpl.shp.shapefile.shape.ShapeType;
import org.osmdroid.customImpl.shp.shapefile.util.ISUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a PointZ Shape object, as defined by the ESRI Shape file
 * specification.
 */
public class PointZShape extends AbstractPointShape {

    private static final int FIXED_CONTENT_LENGTH = (4 + 8 + 8 + 8 + 8) / 2;

    private double z;
    private double m;

    public PointZShape(final ShapeHeader shapeHeader, final ShapeType shapeType,
                       final InputStream is, final ValidationPreferences rules)
            throws IOException, InvalidShapeFileException {
        super(shapeHeader, shapeType, is, rules);

        if (!rules.isAllowBadContentLength()
                && this.header.getContentLength() != FIXED_CONTENT_LENGTH) {
            throw new InvalidShapeFileException(
                    "Invalid PointZ shape header's content length. " + "Expected "
                            + FIXED_CONTENT_LENGTH + " 16-bit words but found "
                            + this.header.getContentLength() + ". " + Const.PREFERENCES);
        }

        this.z = ISUtil.readLeDouble(is);
        this.m = ISUtil.readLeDouble(is);
    }

    // Getters

    public double getZ() {
        return z;
    }

    public double getM() {
        return m;
    }

}
