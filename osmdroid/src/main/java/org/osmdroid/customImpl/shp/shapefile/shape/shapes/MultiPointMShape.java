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
 * Represents a MultiPointM Shape object, as defined by the ESRI Shape file
 * specification.
 */
public class MultiPointMShape extends AbstractMultiPointShape {

    private static final int BASE_CONTENT_LENGTH = (4 + 8 * 4 + 4 + 8 * 2) / 2;

    private double minM;
    private double maxM;
    private double[] m;

    public MultiPointMShape(final ShapeHeader shapeHeader,
                            final ShapeType shapeType, final InputStream is,
                            final ValidationPreferences rules) throws IOException,
            InvalidShapeFileException {
        super(shapeHeader, shapeType, is, rules);

        if (!rules.isAllowBadContentLength()) {
            int expectedLength = BASE_CONTENT_LENGTH
                    + (this.numberOfPoints * (8 * 2 + 8)) / 2;
            if (this.header.getContentLength() != expectedLength) {
                throw new InvalidShapeFileException("Invalid " + getShapeTypeName()
                        + " shape header's content length. " + "Expected " + expectedLength
                        + " 16-bit words (for " + this.numberOfPoints + " points)"
                        + " but found " + this.header.getContentLength() + ". "
                        + Const.PREFERENCES);
            }
        }

        this.minM = ISUtil.readLeDouble(is);
        this.maxM = ISUtil.readLeDouble(is);

        this.m = new double[this.numberOfPoints];
        for (int i = 0; i < this.numberOfPoints; i++) {
            this.m[i] = ISUtil.readLeDouble(is);
        }

    }

    @Override
    protected String getShapeTypeName() {
        return "MultiPointM";
    }

    // Getters

    public double getMinM() {
        return minM;
    }

    public double getMaxM() {
        return maxM;
    }

    public double[] getM() {
        return m;
    }

}
