package org.osmdroid.customImpl.shp.shapefile.shape.shapes;

import org.osmdroid.customImpl.shp.shapefile.ValidationPreferences;
import org.osmdroid.customImpl.shp.shapefile.exception.InvalidShapeFileException;
import org.osmdroid.customImpl.shp.shapefile.shape.Const;
import org.osmdroid.customImpl.shp.shapefile.shape.ShapeHeader;
import org.osmdroid.customImpl.shp.shapefile.shape.ShapeType;

import java.io.IOException;
import java.io.InputStream;

public abstract class AbstractPolyPlainShape extends AbstractPolyShape {

    private static final int BASE_CONTENT_LENGTH = (4 + 8 * 4 + 4 + 4) / 2;

    public AbstractPolyPlainShape(final ShapeHeader shapeHeader,
                                  final ShapeType shapeType, final InputStream is,
                                  final ValidationPreferences rules) throws IOException,
            InvalidShapeFileException {

        super(shapeHeader, shapeType, is, rules);

        if (!rules.isAllowBadContentLength()) {
            int expectedLength = BASE_CONTENT_LENGTH //
                    + (this.numberOfParts * (4)) / 2 //
                    + (this.numberOfPoints * (8 * 2)) / 2;
            if (this.header.getContentLength() != expectedLength) {
                throw new InvalidShapeFileException("Invalid " + getShapeTypeName()
                        + " shape header's content length. " + "Expected " + expectedLength
                        + " 16-bit words (for " + this.numberOfParts + " parts and "
                        + this.numberOfPoints + " points)" + " but found "
                        + this.header.getContentLength() + ". " + Const.PREFERENCES);
            }
        }

    }

    // Getters

}
