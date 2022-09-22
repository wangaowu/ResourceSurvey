package com.bytemiracle.resourcesurvey.common.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.bytemiracle.base.framework.listener.CommonAsyncListener;
import com.bytemiracle.base.framework.utils.string.NumberUtils;

/**
 * 类功能：方向监听器
 *
 * @author gwwang
 * @date 2021/4/6 8:42
 */
public class DirectionManager {

    private static DirectionManager instance;

    private SensorManager mSensorManager;
    private SensorListenerImpl sensorUpdateListener;

    /**
     * 构建实例
     *
     * @param context
     * @return
     */
    public static DirectionManager getInstance(Context context) {
        if (instance == null) {
            instance = new DirectionManager(context);
        }
        return instance;
    }

    private DirectionManager(Context context) {
        this.mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    /**
     * 注册方向监听
     *
     * @param direction
     */
    public void registerDirectionListener(CommonAsyncListener<Float> direction) {
        Sensor mSensorOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if (mSensorManager != null && mSensorOrientation != null) {
            mSensorManager.registerListener(sensorUpdateListener = new SensorListenerImpl(direction),
                    mSensorOrientation,
                    SensorManager.SENSOR_DELAY_UI);
        }
    }

    /**
     * 解注册方向监听
     */
    public void unRegisterDirectionListener() {
        if (mSensorManager != null && sensorUpdateListener != null) {
            mSensorManager.unregisterListener(sensorUpdateListener);
        }
    }

    //方向变化监听impl
    static class SensorListenerImpl implements SensorEventListener {
        private CommonAsyncListener<Float> directionUpdateListener;
        private double mAzimuth;

        public SensorListenerImpl(CommonAsyncListener<Float> directionUpdateListener) {
            this.directionUpdateListener = directionUpdateListener;
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                if (Math.abs(mAzimuth - event.values[0]) < 3) {
                    return;
                }
                //更新当前方位角
                directionUpdateListener.doSomething(new Double(mAzimuth = event.values[0]).floatValue());
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }


    /**
     * 格式化数字
     * 备注:因绝对正方向条件过于严苛，左右冗余10度
     *
     * @param direction 以正北为0度顺时针旋转
     * @return
     */
    public static String formatNum(float direction) {
        String formatContent;
        if (direction < 80) {
            formatContent = "北偏东" + NumberUtils.getDouble(direction, 1) + "°";
        } else if (direction > 100 && direction < 170) {
            formatContent = "南偏东" + NumberUtils.getDouble((180 - direction), 1) + "°";
        } else if (direction > 190 && direction < 260) {
            formatContent = "南偏西" + NumberUtils.getDouble((direction - 180), 1) + "°";
        } else if (direction > 280 && direction < 350) {
            formatContent = "北偏西" + NumberUtils.getDouble((360 - direction), 1) + "°";
        } else if (direction <= 10 || direction >= 350) {
            formatContent = "正北";
        } else if (direction >= 80 && direction <= 100) {
            formatContent = "正东";
        } else if (direction >= 170 && direction <= 190) {
            formatContent = "正南";
        } else if (direction >= 260 && direction <= 280) {
            formatContent = "正西";
        } else {
            formatContent = "错误!";
        }
        return formatContent;
    }
}
