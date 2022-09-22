package com.bytemiracle.resourcesurvey.modules.trajectory;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.recyclerview.widget.RecyclerView;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.base.framework.listener.CommonAsyncListener;
import com.bytemiracle.base.framework.listener.QuickListListener;
import com.bytemiracle.base.framework.utils.XToastUtils;
import com.bytemiracle.base.framework.utils.common.ListUtils;
import com.bytemiracle.base.framework.utils.string.NumberUtils;
import com.bytemiracle.base.framework.view.BaseCheckPojo;
import com.bytemiracle.base.framework.view.recyclerTools.QuickAdapter;
import com.bytemiracle.base.framework.view.recyclerTools.QuickList;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.cache_greendao.PrjTrajectoryDao;
import com.bytemiracle.resourcesurvey.cache_greendao.TrajectoryPointDao;
import com.bytemiracle.resourcesurvey.common.basecompunent.BaseDialogFragment;
import com.bytemiracle.resourcesurvey.common.database.GreenDaoManager;
import com.bytemiracle.resourcesurvey.common.global.GlobalObjectHolder;
import com.bytemiracle.resourcesurvey.modules.trajectory.bean.TrajectoryPoint;
import com.bytemiracle.resourcesurvey.modules.trajectory.service.TrajectoryComponent;
import com.bytemiracle.resourcesurvey.modules.trajectory.bean.PrjTrajectory;
import com.scwang.smartrefresh.layout.adapter.SmartViewHolder;

import org.greenrobot.greendao.query.QueryBuilder;
import org.osmdroid.util.GeoPoint;

import java.util.List;
import java.util.stream.Collectors;

import butterknife.BindView;

/**
 * 类功能：轨迹管理
 *
 * @author gwwang
 * @date 2021/5/22 15:04
 */
@FragmentTag(name = "轨迹管理")
public class TrajectoryFragmentDialog extends BaseDialogFragment {
    private static final String TAG = "TrajectoryFragmentDialog";

    //列表布局
    @BindView(R.id.rv_content)
    RecyclerView rvContent;
    //开始轨迹按钮
    @BindView(R.id.btn_start)
    Button btnStart;
    //停止轨迹按钮
    @BindView(R.id.btn_stop)
    Button btnStop;
    //修复异常按钮
    @BindView(R.id.btn_fix)
    Button btnFix;

    private TrajectoryBizz trajectoryBizz;

    public TrajectoryFragmentDialog(TrajectoryBizz trajectoryBizz) {
        this.trajectoryBizz = trajectoryBizz;
    }

    @Override
    protected void initViews(View view) {
        initTopBarListener();
        btnStart.setOnClickListener(v -> {
            if (trajectoryBizz.isTrajectoryRunning()) {
                XToastUtils.info("请先停止正在进行的轨迹");
                return;
            }
            trajectoryBizz.startRecordTrajectory();
            dismiss();
        });
        btnStop.setOnClickListener(v -> {
            if (!trajectoryBizz.isTrajectoryRunning()) {
                XToastUtils.info("请先开始一次轨迹");
                return;
            }
            trajectoryBizz.stopRecordTrajectory();
            readDatabase(prjTrajectories -> setAdapterData(prjTrajectories));
        });
        //修正无效数据按钮
        updateFixButtonState();
        //刷新列表
        readDatabase(prjTrajectories -> setAdapterData(prjTrajectories));
    }

    //修正无效数据按钮
    private void updateFixButtonState() {
        long invalidRecordCount = trajectoryBizz.queryInvalidRecord();
        long invalidPointCount = trajectoryBizz.queryInvalidPoint();
        btnFix.setVisibility((invalidRecordCount + invalidPointCount) > 0 ? View.VISIBLE : View.GONE);
        btnFix.setOnClickListener(v -> {
            FixTrajectoryRecordDialog fixTrajectoryRecordDialog = new FixTrajectoryRecordDialog(
                    trajectoryBizz,
                    needUpdate -> {
                        updateFixButtonState();
                        readDatabase(prjTrajectories -> setAdapterData(prjTrajectories));
                    });
            fixTrajectoryRecordDialog.show(getChildFragmentManager(), "");
        });
    }

    private void readDatabase(CommonAsyncListener<List<PrjTrajectory>> queryResultListener) {
        GlobalInstanceHolder.newSingleExecutor().execute(() -> {
            Long openingProjectId = GlobalObjectHolder.getOpeningProject().getId();
            PrjTrajectoryDao prjTrajectoryDao = GreenDaoManager.getInstance().getDaoSession().getPrjTrajectoryDao();
            QueryBuilder<PrjTrajectory> prjTrajectoryQueryBuilder = prjTrajectoryDao.queryBuilder();
            List<PrjTrajectory> list = prjTrajectoryQueryBuilder.where(PrjTrajectoryDao.Properties.ProjectId.eq(openingProjectId)).list();
            GlobalInstanceHolder.mainHandler().post(() -> queryResultListener.doSomething(list));
        });
    }

    private void setAdapterData(List<PrjTrajectory> list) {
        if (ListUtils.isEmpty(list)) {
            XToastUtils.info("没有轨迹数据");
            return;
        }
        QuickList.instance().adapter(rvContent, R.layout.item_trajectory_manage, list, new QuickListListener<PrjTrajectory>() {
            @Override
            public void onBindItem(QuickAdapter<PrjTrajectory> quickAdapter, SmartViewHolder h, PrjTrajectory prjTrajectory) {
                h.text(R.id.tv_trajectory_index, "" + list.indexOf(prjTrajectory));
                //轨迹名称
                //1.结束时间未知，且为最后一个时，是正在进行的任务
                //2.结束时间未知，非最后一个时，是异常轨迹数据
                //3.其他情况为正常轨迹数据
                if (prjTrajectory.getEndTime() == 0) {
                    boolean isLastOne = list.indexOf(prjTrajectory) == list.size() - 1;
                    h.text(R.id.tv_trajectory_name, isLastOne ? "进行中..." : "异常轨迹");
                } else {
                    h.text(R.id.tv_trajectory_name, prjTrajectory.getTrajectoryName());
                }

                h.text(R.id.tv_trajectory_number, prjTrajectory.getPointNumber() + "");
                h.text(R.id.tv_trajectory_length, NumberUtils.getDouble(prjTrajectory.getTrajectoryLength(), 2) + "米");
                ((CheckBox) h.findView(R.id.cb_state)).setChecked(prjTrajectory.isChecked());

                h.itemView.setOnClickListener(v -> {
                    //1.选中条目
                    BaseCheckPojo.checkedSingleItem(list, list.indexOf(prjTrajectory));
                    quickAdapter.notifyDataSetChanged();
                    //2.显示该条内的轨迹点
                    int configColor = TrajectoryConfigUtils.getConfig(GlobalObjectHolder.getOpeningProject()).getColor();
                    int lineWidth = TrajectoryConfigUtils.getConfig(GlobalObjectHolder.getOpeningProject()).getLineWidth();
                    TrajectoryPointDao trajectoryPointDao = GreenDaoManager.getInstance().getDaoSession().getTrajectoryPointDao();
                    List<TrajectoryPoint> listInDB = trajectoryPointDao.queryBuilder().where(TrajectoryPointDao.Properties.BelongId.eq(prjTrajectory.getId())).list();
                    List<GeoPoint> trajectoryPoints = listInDB.stream()
                            .map(e -> new GeoPoint(e.getLongitude(), e.getLatitude()))
                            .collect(Collectors.toList());
                    trajectoryBizz.drawTrajectoryLine(configColor, lineWidth, trajectoryPoints, true);
                    //3.关闭弹窗
                    GlobalInstanceHolder.mainHandler().postDelayed(() -> dismiss(), 200);
                });
            }
        });
    }

    private void initTopBarListener() {
        appTitleController.getRightButton().setVisibility(View.VISIBLE);
        appTitleController.getRightButton().setText("轨迹设置");
        appTitleController.getRightButton().setOnClickListener(v -> {
            if (TrajectoryComponent.locationServiceIsRunning) {
                XToastUtils.info("修改配置,请先停止当前的轨迹追踪!");
                return;
            }
            new ConfigTrajectoryDialog().show(getChildFragmentManager(), "");
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_pop_trajectory;
    }
}
