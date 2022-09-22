package org.osmdroid.measure

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.appcompat.widget.SwitchCompat
import org.osmdroid.defaultImpl.R
import org.osmdroid.edit.bean.MeasureMode
import org.osmdroid.views.MapView

/**
 * 类功能：封装好的osmDroid测量组件
 *
 * @author gwwang
 * @date 2022/2/8 14:10
 */
class OsmMeasureLayout(
    context: Context,
    attrs: AttributeSet?,
) : FrameLayout(context, attrs) {

    private var layoutMeasureStep: View
    private var ibUndo: ImageButton
    private var ibAutoClose: ImageButton
    private var swDistance: SwitchCompat
    private var swArea: SwitchCompat
    private lateinit var mapView: MapView
    private lateinit var presenter: OsmMeasurePresenter

    init {
        val content = View.inflate(getContext(), R.layout.layout_osm_measure, this)
        layoutMeasureStep = content.findViewById(R.id.layout_measure_step)
        ibUndo = content.findViewById(R.id.ib_undo)
        ibAutoClose = content.findViewById(R.id.ib_auto_close)
        swDistance = content.findViewById(R.id.sw_distance)
        swArea = content.findViewById(R.id.sw_area)
    }

    /**
     * 设置mapView对象
     */
    open fun attachMapView(mapView: MapView) {
        this.mapView = mapView
        this.presenter = OsmMeasurePresenter(mapView, "")
        initializeViews()
    }

    private fun initializeViews() {
        //测距侧面开关监听
        swDistance.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                if (swArea.isChecked) {
                    presenter.clearGraphicInfo()
                }
                swArea.isChecked = false
                setMeasureStepVisible(true)
                presenter.setMeasureMode(MeasureMode.DISTANCE)
            } else {
                if (!swArea.isChecked) {
                    setMeasureStepVisible(false)
                    presenter.setMeasureMode(MeasureMode.NONE)
                }
            }
        }
        swArea.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                if (swDistance.isChecked) {
                    presenter.clearGraphicInfo()
                }
                swDistance.isChecked = false
                setMeasureStepVisible(true)
                presenter.setMeasureMode(MeasureMode.AREA)
            } else {
                if (!swDistance.isChecked) {
                    setMeasureStepVisible(false)
                    presenter.setMeasureMode(MeasureMode.NONE)
                }
            }
        }

        ibUndo.setOnClickListener {
            presenter.removeEndMeasureNode()
        }
        ibAutoClose.setOnClickListener {
            presenter.autoCloseMeasureEndNode()
        }

        setMeasureStepVisible(false)
    }

    private fun setMeasureStepVisible(visible: Boolean) {
        layoutMeasureStep.visibility = if (visible) View.VISIBLE else View.GONE
    }
}