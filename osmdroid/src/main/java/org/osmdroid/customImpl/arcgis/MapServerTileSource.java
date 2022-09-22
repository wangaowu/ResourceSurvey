package org.osmdroid.customImpl.arcgis;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;

import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.modules.IFilesystemCache;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.tileprovider.modules.TileWriter;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.MapTileIndex;

/**
 * 类功能：加载mapserver
 * 该类逻辑参考： osmdroid--wms
 *
 * @author gwwang
 * @date 2021/12/17 10:39
 */
public class MapServerTileSource extends OnlineTileSourceBase {
    private static final String TAG = "MapServerTileSource";

    private static final int MIN_ZOOM_LEVEL = 1;
    private static final int MAX_ZOOM_LEVEL = 16;
    private static final int TILE_SIZE_PIXELS = 256;
    private static final int DPI = 96;//default is 96

    private IFilesystemCache tileWriter;
    private boolean useCache = true;

    private String layerName;
    private String srs;
    private MapTileProviderBasic tileProvider;

    public MapServerTileSource(Context context, String aName, String baseUrl, String layerName, String srs) {
        this(context, aName, baseUrl, layerName, srs, true);
    }

    public MapServerTileSource(Context context, String aName, String baseUrl, String layerName, String srs, boolean useCache) {
        super(aName + layerName, MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL, TILE_SIZE_PIXELS, "", new String[]{baseUrl}, null);
        this.layerName = layerName;
        this.srs = srs;
        this.useCache = useCache;
        this.tileWriter = Build.VERSION.SDK_INT < 10 ? new TileWriter() : new SqlTileWriter();
        this.tileProvider = new MapTileProviderBasic(context, this, tileWriter);
    }

    /**
     * 获取TileProvider
     *
     * @return
     */
    public MapTileProviderBasic getTileProvider() {
        return tileProvider;
    }

    @Override
    public String getTileURLString(long pMapTileIndex) {
        int x = MapTileIndex.getX(pMapTileIndex);
        int y = MapTileIndex.getY(pMapTileIndex);
        int zoom = MapTileIndex.getZoom(pMapTileIndex);
        BoundingBox boundingBox = tile2boundingBox(x, y, zoom);

        StringBuilder boundingBoxStr = new StringBuilder();
        boundingBoxStr.append(boundingBox.getLonWest()).append(",");
        boundingBoxStr.append(boundingBox.getLatSouth()).append(",");
        boundingBoxStr.append(boundingBox.getLonEast()).append(",");
        boundingBoxStr.append(boundingBox.getLatNorth());

        return getBaseUrl() + "/export?transparent=true&f=image&format=png"
                + "&layers=show:" + layerName
                + "&bbox=" + boundingBoxStr
                + "&bboxSR=" + srs
                + "&imageSR=" + srs
                + "&size=" + getTileSizePixels() + "," + getTileSizePixels()
                + "&dpi=";
    }

    public BoundingBox tile2boundingBox(final int x, final int y, final int zoom) {
        return new BoundingBox(tile2lat(y, zoom), tile2lon(x + 1, zoom), tile2lat(y + 1, zoom), tile2lon(x, zoom));
    }

    public double tile2lon(int x, int z) {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    public double tile2lat(int y, int z) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }
}
