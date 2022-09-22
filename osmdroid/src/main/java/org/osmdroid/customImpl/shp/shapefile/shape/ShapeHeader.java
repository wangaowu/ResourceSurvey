package org.osmdroid.customImpl.shp.shapefile.shape;

import org.osmdroid.customImpl.shp.shapefile.ValidationPreferences;
import org.osmdroid.customImpl.shp.shapefile.exception.DataStreamEOFException;
import org.osmdroid.customImpl.shp.shapefile.exception.InvalidShapeFileException;
import org.osmdroid.customImpl.shp.shapefile.util.ISUtil;

import java.io.IOException;
import java.io.InputStream;

public class ShapeHeader {

    private int recordNumber;
    private int contentLength;

    public ShapeHeader(final InputStream is, final ValidationPreferences rules)
            throws DataStreamEOFException, IOException, InvalidShapeFileException {

        this.recordNumber = ISUtil.readBeIntMaybeEOF(is);
        if (!rules.isAllowBadRecordNumbers()) {
            if (this.recordNumber != rules.getExpectedRecordNumber()) {
                throw new InvalidShapeFileException("Invalid record number. Expected "
                        + rules.getExpectedRecordNumber() + " but found "
                        + this.recordNumber + ".");
            }
        }

        this.contentLength = ISUtil.readBeInt(is);
    }

    public int getRecordNumber() {
        return recordNumber;
    }

    public int getContentLength() {
        return contentLength;
    }

}
