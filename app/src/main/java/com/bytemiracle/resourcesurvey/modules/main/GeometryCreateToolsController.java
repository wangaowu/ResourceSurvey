package com.bytemiracle.resourcesurvey.modules.main;

import android.view.View;
import android.widget.LinearLayout;

import com.bytemiracle.base.framework.listener.CommonAsyncListener;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.basecompunent.TViewProxy;
import com.bytemiracle.resourcesurvey.common.compunent.ListenableController;
import com.bytemiracle.resourcesurvey.osmdroid.overlay.MapElementsHolder;

import org.osmdroid.create.CreateLineImpl;
import org.osmdroid.create.CreatePointImpl;
import org.osmdroid.create.CreatePolygonImpl;
import org.osmdroid.create.ICreateGeometry;
import org.osmdroid.edit.bean.MeasureMode;
import org.osmdroid.overlay.bean.PackageOverlayInfo;
import org.osmdroid.overlay.utils.MapConstant;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import mil.nga.sf.Geometry;

/**
 * 类功能：TODO:
 *
 * @author gwwang
 * @date 2021/6/25 15:10
 */
public class GeometryCreateToolsController implements ListenableController {

    @BindView(R.id.proxy_complete)
    TViewProxy proxyComplete;
    @BindView(R.id.proxy_undo)
    TViewProxy proxyUndo;
    @BindView(R.id.proxy_redo)
    TViewProxy proxyRedo;

    private LinearLayout llTools;
    private ICreateGeometry createGeometryImpl;
    private final Unbinder bind;

    public GeometryCreateToolsController(LinearLayout llTools) {
        this.llTools = llTools;
        bind = ButterKnife.bind(this, llTools);
        initListeners();
        llTools.setVisibility(View.GONE);
    }

    /**
     * 打开工具
     *
     * @param osmGeometryType     图层类型
     * @param getGeometryListener geometry数据
     */
    public void openTools(PackageOverlayInfo.OSMGeometryType osmGeometryType, CommonAsyncListener<Geometry> getGeometryListener) {
        this.createGeometryImpl = getCreateGeometryImpl(osmGeometryType);
        this.llTools.setVisibility(View.VISIBLE);
        proxyComplete.setOnClickListener(v -> {
            Geometry newGeometry = createGeometryImpl.getCreatedGeometry();
            if (newGeometry != null && !newGeometry.isEmpty()) {
                createGeometryImpl.cancelDrawEventListener();//注意绘制事件会覆盖后续的地图事件监听，必须用时添加，不用时移除
                getGeometryListener.doSomething(newGeometry);
            }
        });
    }

    /**
     * 关闭工具
     */
    public void closeTools() {
        llTools.setVisibility(View.GONE);
        createGeometryImpl.clearGraphicInfo();
    }

    private void initListeners() {
        //撤销
        proxyUndo.setOnClickListener(v -> {
            createGeometryImpl.removeEndMeasureNode();
        });
        //恢复
        proxyRedo.setOnClickListener(v -> {
            createGeometryImpl.redoCachedNodes();
        });
        //完成
        proxyComplete.setOnClickListener(v -> closeTools());
    }

    private ICreateGeometry getCreateGeometryImpl(PackageOverlayInfo.OSMGeometryType osmGeometryType) {
        switch (osmGeometryType) {
            case POINT:
                return new CreatePointImpl(MapElementsHolder.getMapView(), MapConstant.TAG_GEOMETRY);
            case LINESTRING:
                return new CreateLineImpl(MapElementsHolder.getMapView(), MapConstant.TAG_GEOMETRY);
            default:
                // case POLYGON:
                CreatePolygonImpl createPolygonImpl = new CreatePolygonImpl(MapElementsHolder.getMapView(), MapConstant.TAG_GEOMETRY);
                createPolygonImpl.setMeasureMode(MeasureMode.CREATE);
                return createPolygonImpl;
        }
    }

    @Override
    public void onDestroyView() {
        bind.unbind();
    }
}
