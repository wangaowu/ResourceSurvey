package com.bytemiracle.resourcesurvey.common.view;

import java.util.Date;

public class ItemViewData {
    public String name;
    public String aliasName;
    public Date dateVal;
    public long intVal;
    public double doubleVal;
    public String stringVal;
    public boolean booleanVal;
    public String numberType;
    public String checkPool;
    public int checkType;

    public ItemViewData(String name, int checkType, String checkPool) {
        this.name = name;
        this.checkPool = checkPool;
        this.checkType = checkType;
    }

    public ItemViewData(String name, Date dateVal) {
        this.name = name;
        this.dateVal = dateVal;
    }

    public ItemViewData(String name, long intVal, String numberType) {
        this.name = name;
        this.intVal = intVal;
        this.numberType = numberType;
    }

    public ItemViewData(String name, double doubleVal, String numberType) {
        this.name = name;
        this.doubleVal = doubleVal;
        this.numberType = numberType;
    }

    public ItemViewData(String name, String stringVal) {
        this.name = name;
        this.stringVal = stringVal;
    }

    public ItemViewData(String name, boolean booleanVal) {
        this.name = name;
        this.booleanVal = booleanVal;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getCheckPool() {
        return checkPool;
    }

    public void setCheckPool(String checkPool) {
        this.checkPool = checkPool;
    }

    public int getCheckType() {
        return checkType;
    }

    public void setCheckType(int checkType) {
        this.checkType = checkType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDateVal() {
        return dateVal;
    }

    public void setDateVal(Date dateVal) {
        this.dateVal = dateVal;
    }

    public long getIntVal() {
        return intVal;
    }

    public void setIntVal(long intVal) {
        this.intVal = intVal;
    }

    public double getDoubleVal() {
        return doubleVal;
    }

    public void setDoubleVal(double doubleVal) {
        this.doubleVal = doubleVal;
    }

    public String getStringVal() {
        return stringVal;
    }

    public void setStringVal(String stringVal) {
        this.stringVal = stringVal;
    }

    public boolean isBooleanVal() {
        return booleanVal;
    }

    public void setBooleanVal(boolean booleanVal) {
        this.booleanVal = booleanVal;
    }

    public String getNumberType() {
        return numberType;
    }

    public void setNumberType(String numberType) {
        this.numberType = numberType;
    }

    public static boolean getBol(Object val) {
        if (val == null) {
            return false;
        }
        return (boolean) val;
    }

    public static long getInt(Object val) {
        if (val == null) {
            return 0;
        }
        return (long) val;
    }

    public static double getDouble(Object val) {
        if (val == null) {
            return 0.0;
        }
        return (double) val;
    }
}
