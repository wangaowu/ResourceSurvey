package org.osmdroid.customImpl.convert.reader;

/**
 * 类功能：wkt解析的片段
 *
 * @author gwwang
 * @date 2022/3/24 15:29
 */
public class WktSplit {
    public enum Case {
        EXTRA, POINT
    }

    private String content;
    private Case case_;

    public WktSplit(String content, Case case_) {
        this.content = content;
        this.case_ = case_;
    }

    public String getContent() {
        return content;
    }

    public Case getCase_() {
        return case_;
    }

    public double[] getPoint() {
        double[] doubles = new double[4]; //4: x,y,z,m
        String[] pointSplits = content.split(" ");
        for (int i = 0; i < pointSplits.length; i++) {
            doubles[i] = new Double(pointSplits[i]).doubleValue();
        }
        return doubles;
    }

    @Override
    public String toString() {
        return "content='" + content + "  case_=" + case_ + "\n";
    }
}
