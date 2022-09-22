package com.bytemiracle.resourcesurvey.modules.trajectory;

import android.graphics.drawable.Drawable;

import com.bytemiracle.base.framework.component.BaseActivity;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.cache_greendao.PrjTrajectoryDao;
import com.bytemiracle.resourcesurvey.cache_greendao.TrajectoryPointDao;
import com.bytemiracle.resourcesurvey.common.basecompunent.TViewProxy;
import com.bytemiracle.resourcesurvey.common.database.GreenDaoManager;
import com.bytemiracle.resourcesurvey.common.date.AppTime;
import com.bytemiracle.resourcesurvey.common.dbbean.DBProject;
import com.bytemiracle.resourcesurvey.common.global.GlobalObjectHolder;
import com.bytemiracle.resourcesurvey.common.renderstyle.OverlayRenderStyleUtils;
import com.bytemiracle.resourcesurvey.giscommon.location.LocationDataChangedListener;
import com.bytemiracle.resourcesurvey.giscommon.location.utils.NavigationUtils;
import com.bytemiracle.resourcesurvey.modules.trajectory.bean.PrjTrajectory;
import com.bytemiracle.resourcesurvey.modules.trajectory.bean.TrajectoryConfig;
import com.bytemiracle.resourcesurvey.modules.trajectory.bean.TrajectoryPoint;
import com.bytemiracle.resourcesurvey.modules.trajectory.service.TrajectoryComponent;
import com.bytemiracle.resourcesurvey.osmdroid.overlay.MapElementsHolder;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;
import org.osmdroid.overlay.bean.PackageOverlayInfo;
import org.osmdroid.overlay.bean.options.OsmRenderOption;
import org.osmdroid.overlay.render.OsmdroidMapRender;
import org.osmdroid.overlay.utils.MapConstant;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 类功能：轨迹业务类
 *
 * @author gwwang
 * @date 2021/7/19 11:17
 */
public class TrajectoryBizz {
    private static final String TAG = "TrajectoryBizz";

    //异常数据条件
    //数据量小于10(单位：个)
    public static final int MIN_LIMIT_POINT_COUNT = Integer.parseInt(FixTrajectoryRecordDialog.CONFIG_MIN_COUNT_ITEMS[0]);
    //移动距离小于10(单位：米)
    public static final int MIN_LIMIT_MOVE_DISTANCE = Integer.parseInt(FixTrajectoryRecordDialog.CONFIG_MIN_DISTANCE_ITEMS[0]);

    private WeakReference<BaseActivity> activity;
    private WeakReference<MapView> mapView;
    private TViewProxy viewTrajectory;
    private long currentTrajectoryRecordId;

    //轨迹点数据
    private List<GeoPoint> trajectoryPoints = new ArrayList<>();

    private OsmdroidMapRender trajectoryOverlayRender;
    private OsmdroidMapRender myPointOverlayRender;
    private Drawable myPointDrawable;

    public TrajectoryBizz(BaseActivity activity, MapView mapView) {
        this.activity = new WeakReference(activity);
        this.mapView = new WeakReference<>(mapView);
        loadMyPointMarkerSymbol();
    }

    private void loadMyPointMarkerSymbol() {
        myPointDrawable = activity.get().getResources().getDrawable(R.drawable.ic_mylocation);
        OsmRenderOption renderOption = OverlayRenderStyleUtils.getDefaultRenderOption();
        MapView mapView = MapElementsHolder.getMapView();
        trajectoryOverlayRender = new OsmdroidMapRender(mapView, renderOption, MapConstant.TAG_TRAJECTORY, PackageOverlayInfo.Category.GRAPHIC, PackageOverlayInfo.OSMGeometryType.POINT);
        OsmRenderOption renderOption2 = OverlayRenderStyleUtils.getDefaultRenderOption();
        myPointOverlayRender = new OsmdroidMapRender(mapView, renderOption2, MapConstant.TAG_MY_POINT, PackageOverlayInfo.Category.GRAPHIC, PackageOverlayInfo.OSMGeometryType.POINT);
    }

    /**
     * 是否正在记录轨迹
     *
     * @return
     */
    public boolean isTrajectoryRunning() {
        return TrajectoryComponent.locationServiceIsRunning;
    }

    /**
     * 开始记录轨迹
     */
    public void startRecordTrajectory() {
        if (isTrajectoryRunning()) {
            return;
        }
        updateTrajectoryButtonState(true);
        DBProject openingProject = GlobalObjectHolder.getOpeningProject();
        TrajectoryConfig config = TrajectoryConfigUtils.getConfig(openingProject);
        //做前置条件适配
        long locationInterval;
        double locationMinMoveDistance;
        //当采样方式为距离采样时,将时间间隔置为1s
        switch (config.getType()) {
            case TrajectoryConfig.TYPE_BY_TIME:
                //当采样方式为时间采样时,将距离判断置为0.2m
                locationInterval = config.getSamplingRate() * 1000;
                locationMinMoveDistance = 0.2;
                break;
            case TrajectoryConfig.TYPE_BY_DISTANCE:
            default:
                //当采样方式为距离采样时,将时间间隔置为1s
                locationInterval = 1 * 1000;
                locationMinMoveDistance = config.getSamplingRate();
                break;
        }
        trajectoryPoints.clear();
        int configColor = TrajectoryConfigUtils.getConfig(openingProject).getColor();
        int lineWidth = TrajectoryConfigUtils.getConfig(openingProject).getLineWidth();
        currentTrajectoryRecordId = createNewTrajectoryRecord(openingProject.getId());
        TrajectoryPointDao trajectoryPointDao = GreenDaoManager.getInstance().getDaoSession().getTrajectoryPointDao();
        TrajectoryComponent.startService(activity.get(), locationInterval, locationMinMoveDistance, new LocationDataChangedListener() {
            @Override
            public void onProceedChanged(GeoPoint oldPoint, GeoPoint newPoint, double accuracy, double headDirection) {
                if (newPoint.getLongitude() != 0 && newPoint.getLatitude() != 0) {
                    insertNewPoint(trajectoryPointDao, newPoint);
                    trajectoryPoints.add(newPoint);
                    drawTrajectoryLine(configColor, lineWidth, trajectoryPoints, false);
                    drawMyPointMarker(newPoint, headDirection);
                }
            }
        });
    }

    /**
     * 绘制轨迹
     */
    public void drawTrajectoryLine(int lineColor, int lineWidth, List<GeoPoint> trajectoryPoints, boolean autoZoom) {
        trajectoryOverlayRender.removeItems();

        Polyline polyline = new Polyline();
        polyline.setPoints(trajectoryPoints);
        polyline.getOutlinePaint().setColor(lineColor);
        polyline.getOutlinePaint().setStrokeWidth(lineWidth);
        trajectoryOverlayRender.addOverlay(polyline);
        mapView.get().invalidate();

        if (autoZoom) {
            BoundingBox bounds = polyline.getBounds();
            mapView.get().zoomToBoundingBox(bounds, true, MapConstant.DEFAULT_BOX_PADDING);
        }
    }

    //绘制我的位置，且移动地图
    private void drawMyPointMarker(GeoPoint myPoint, double direct) {
        MapView mapView = MapElementsHolder.getMapView();

        Marker marker = new Marker(mapView);
        marker.setOnMarkerClickListener(null);
        marker.setIcon(myPointDrawable);
        marker.setPosition(myPoint);
        marker.setRotation((float) direct);

        myPointOverlayRender.removeItems();
        myPointOverlayRender.addOverlay(marker);
        mapView.invalidate();

        //移动地图
        mapView.setExpectedCenter(myPoint);
    }

    /**
     * 停止记录轨迹
     */
    public void stopRecordTrajectory() {
        if (!isTrajectoryRunning()) {
            return;
        }
        //移除轨迹
        trajectoryOverlayRender.removeSelfOverlay();
        //移除我的位置
        myPointOverlayRender.removeSelfOverlay();
        updateTrajectoryButtonState(false);
        TrajectoryComponent.stopService(activity.get());
        completeNewTrajectoryRecord();
    }

    /**
     * 绑定巡查按钮
     *
     * @param viewTrajectory
     */
    public void bindTrajectoryButton(TViewProxy viewTrajectory) {
        this.viewTrajectory = viewTrajectory;
    }

    /**
     * 查找有没有异常数据
     * 判断异常条件：
     * 1.数据量小于10
     * 2.结束时间异常（巡查过程中进程中断）
     * 3.移动距离小于10
     */
    public long queryInvalidRecord() {
        PrjTrajectoryDao prjTrajectoryDao = GreenDaoManager.getInstance().getDaoSession().getPrjTrajectoryDao();
        WhereCondition countCondition = PrjTrajectoryDao.Properties.PointNumber.le(MIN_LIMIT_POINT_COUNT);
        WhereCondition endTimeCondition = PrjTrajectoryDao.Properties.EndTime.eq(0);
        WhereCondition distanceCondition = PrjTrajectoryDao.Properties.TrajectoryLength.le(MIN_LIMIT_MOVE_DISTANCE);
        return prjTrajectoryDao.queryBuilder().whereOr(countCondition, endTimeCondition, distanceCondition).count();
    }

    /**
     * 异常点个数
     *
     * @return
     */
    public long queryInvalidPoint() {
        return GreenDaoManager.getInstance().getDaoSession().getTrajectoryPointDao().queryBuilder()
                .whereOr(TrajectoryPointDao.Properties.Latitude.eq(0), TrajectoryPointDao.Properties.Longitude.eq(0))
                .count();
    }

    /**
     * 修正轨迹记录
     *
     * @param configMinLimitCount
     * @param configMinLimitDistance
     */
    public void fixTrajectoryRecord(int configMinLimitCount, int configMinLimitDistance) {
        deleteRecordByCountOrDistance(configMinLimitCount, configMinLimitDistance);
        fixUncompletedRecord();
        fixErrorRecord();
    }

    private void deleteRecordByCountOrDistance(int configMinLimitCount, int configMinLimitDistance) {
        PrjTrajectoryDao prjTrajectoryDao = GreenDaoManager.getInstance().getDaoSession().getPrjTrajectoryDao();
        QueryBuilder<PrjTrajectory> queryBuilder = prjTrajectoryDao.queryBuilder();
        WhereCondition countCondition = PrjTrajectoryDao.Properties.PointNumber.le(configMinLimitCount);
        WhereCondition distanceCondition1 = PrjTrajectoryDao.Properties.TrajectoryLength.le(configMinLimitDistance);
        WhereCondition distanceCondition2 = PrjTrajectoryDao.Properties.TrajectoryLength.notEq(0);
        //取出数量小于x，或者距离小于x但不等于-1(没结束)的条目
        List<PrjTrajectory> list = queryBuilder.whereOr(countCondition, queryBuilder.and(distanceCondition1, distanceCondition2)).list();
        List<Long> recordIds = list.stream().map(prjTrajectory -> prjTrajectory.getId()).collect(Collectors.toList());
        //删除记录条目数据
        prjTrajectoryDao.deleteByKeyInTx(recordIds);
        //删除实际的点数据
        TrajectoryPointDao trajectoryPointDao = GreenDaoManager.getInstance().getDaoSession().getTrajectoryPointDao();
        trajectoryPointDao.queryBuilder()
                .where(TrajectoryPointDao.Properties.BelongId.in(recordIds))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
    }

    //修正没有完成的记录
    //1.时间:使用最后一个点的记录时间
    //2.距离:使用该记录下的所有轨迹点重新求和
    private void fixUncompletedRecord() {
        PrjTrajectoryDao prjTrajectoryDao = GreenDaoManager.getInstance().getDaoSession().getPrjTrajectoryDao();
        TrajectoryPointDao trajectoryPointDao = GreenDaoManager.getInstance().getDaoSession().getTrajectoryPointDao();
        List<PrjTrajectory> list = prjTrajectoryDao.queryBuilder().where(PrjTrajectoryDao.Properties.EndTime.eq(0)).list();
        for (PrjTrajectory prjTrajectory : list) {
            List<TrajectoryPoint> points = trajectoryPointDao.queryBuilder()
                    .where(TrajectoryPointDao.Properties.BelongId.eq(prjTrajectory.getId()))
                    .orderDesc(TrajectoryPointDao.Properties.RecordTime)
                    .list();
            prjTrajectory.setEndTime(points.get(0).getRecordTime());
            List<GeoPoint> geometryPoints = points.stream()
                    .map(trajectoryPoint -> new GeoPoint(trajectoryPoint.getLongitude(), trajectoryPoint.getLatitude()))
                    .collect(Collectors.toList());
            prjTrajectory.setTrajectoryLength(NavigationUtils.getDistance(geometryPoints));
        }
        prjTrajectoryDao.updateInTx(list);
    }

    //处理异常数据
    //1.删除经纬度为0的异常点
    //2.重新计算异常距离
    private void fixErrorRecord() {
        PrjTrajectoryDao prjTrajectoryDao = GreenDaoManager.getInstance().getDaoSession().getPrjTrajectoryDao();
        TrajectoryPointDao trajectoryPointDao = GreenDaoManager.getInstance().getDaoSession().getTrajectoryPointDao();
        //删除经纬度为0的异常点
        List<TrajectoryPoint> zeroPointList = trajectoryPointDao.queryBuilder()
                .whereOr(TrajectoryPointDao.Properties.Latitude.eq(0), TrajectoryPointDao.Properties.Longitude.eq(0))
                .list();
        trajectoryPointDao.deleteInTx(zeroPointList);
        Set<Long> zeroPointRecord = zeroPointList.stream()
                .map(trajectoryPoint -> trajectoryPoint.getBelongId())
                .collect(Collectors.toSet());
        //重新计算含0|0点的记录距离
        for (Long recordId : zeroPointRecord) {
            List<GeoPoint> noZeroPointList = trajectoryPointDao.queryBuilder()
                    .where(TrajectoryPointDao.Properties.BelongId.eq(recordId))
                    .list().stream()
                    .map(e -> new GeoPoint(e.getLongitude(), e.getLatitude()))
                    .collect(Collectors.toList());
            PrjTrajectory record = prjTrajectoryDao.queryBuilder()
                    .where(PrjTrajectoryDao.Properties.Id.eq(recordId))
                    .unique();
            record.setTrajectoryLength(NavigationUtils.getDistance(noZeroPointList));
            prjTrajectoryDao.update(record);
        }
    }

    private void insertNewPoint(TrajectoryPointDao trajectoryPointDao, GeoPoint point) {
        TrajectoryPoint trajectoryPoint = new TrajectoryPoint();
        trajectoryPoint.setBelongId(currentTrajectoryRecordId);
        trajectoryPoint.setLongitude(point.getLongitude());
        trajectoryPoint.setLatitude(point.getLatitude());
        trajectoryPoint.setAltitude(-1);
        trajectoryPointDao.insert(trajectoryPoint);
    }

    private long getPointCount() {
        TrajectoryPointDao trajectoryPointDao = GreenDaoManager.getInstance().getDaoSession().getTrajectoryPointDao();
        return trajectoryPointDao.queryBuilder()
                .where(TrajectoryPointDao.Properties.BelongId.eq(currentTrajectoryRecordId))
                .count();
    }

    private long createNewTrajectoryRecord(Long projectId) {
        PrjTrajectoryDao prjTrajectoryDao = GreenDaoManager.getInstance().getDaoSession().getPrjTrajectoryDao();
        long startTime = System.currentTimeMillis();
        String trajectoryName = AppTime.formatTimestamps(startTime);
        PrjTrajectory prjTrajectory = new PrjTrajectory(null, projectId, trajectoryName, 0, startTime, 0, 0, false);
        return prjTrajectoryDao.insert(prjTrajectory);
    }

    private void completeNewTrajectoryRecord() {
        PrjTrajectoryDao prjTrajectoryDao = GreenDaoManager.getInstance().getDaoSession().getPrjTrajectoryDao();
        PrjTrajectory prjTrajectory = prjTrajectoryDao.queryBuilder().where(PrjTrajectoryDao.Properties.Id.eq(currentTrajectoryRecordId)).unique();
        prjTrajectory.setEndTime(System.currentTimeMillis());
        prjTrajectory.setPointNumber(getPointCount());
        prjTrajectory.setTrajectoryLength(NavigationUtils.getDistance(trajectoryPoints));
        prjTrajectoryDao.update(prjTrajectory);
    }

    private void updateTrajectoryButtonState(boolean isRunning) {
        if (isRunning) {
            viewTrajectory.setButtonState(TViewProxy.STATE.ENABLE);
        } else {
            viewTrajectory.setButtonState(TViewProxy.STATE.CLICKABLE);
        }
    }
}
