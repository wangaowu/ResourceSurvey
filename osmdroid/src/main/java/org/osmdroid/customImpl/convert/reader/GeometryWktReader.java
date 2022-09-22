package org.osmdroid.customImpl.convert.reader;

import java.util.ArrayList;
import java.util.List;

/**
 * 类功能：wkt解析类
 *
 * @author gwwang
 * @date 2022/3/24 13:31
 */
public class GeometryWktReader {
    private static final char SPACE = ' ';
    private static final char[] ALLOW_NUM = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '-'};
    private static final char COMMA = ',';
    private static final char LEFT_PARENTHESES = '(';
    private static final char RIGHT_PARENTHESES = ')';

    private char[] wktCharacters;
    private int endCharacterIndex;
    private List<WktSplit> wktSplits;

    private int loopIndex;

    /**
     * 构造方法
     *
     * @param wktText wkt内容
     */
    public GeometryWktReader(String wktText) {
        this.wktCharacters = filterSpace(wktText).toCharArray();
        this.loopIndex = 0;
        this.endCharacterIndex = wktCharacters.length - 1;
        this.wktSplits = new ArrayList<>();
        doWhile();
    }

    /**
     * 解析的片段
     *
     * @return
     */
    public List<WktSplit> getWktSplits() {
        return wktSplits;
    }

    private void doWhile() {
        StringBuilder temp = new StringBuilder();
        while (!isAfterEndIndex()) {
            char current = wktCharacters[loopIndex];
            if (isNumberOrSpace(current)) {
                //是数字
                if (isStartNum(loopIndex)) {
                    //是起始数字
                    //1.将之前部分拼接后重置容器
                    wktSplits.add(new WktSplit(temp.toString(), WktSplit.Case.EXTRA));
                    temp = new StringBuilder();
                    temp.append(current);
                } else if (isEndNum(loopIndex)) {
                    //是末尾数字
                    temp.append(current);
                    wktSplits.add(new WktSplit(temp.toString(), WktSplit.Case.POINT));
                    temp = new StringBuilder();
                } else if (isSpace(current)) {
                    //是空格
                    temp.append(current);
                    wktSplits.add(new WktSplit(temp.toString(), WktSplit.Case.POINT));
                } else {
                    //是中间数字
                    temp.append(current);
                }
            } else {
                //非数字
                temp.append(current);
            }
            loopIndex++;
        }
        wktSplits.add(new WktSplit(temp.toString(), WktSplit.Case.EXTRA));
    }

    //是否起始的数字
    private boolean isStartNum(int index) {
        char character = wktCharacters[index];
        if (isNumberOrSpace(character)) {
            char leftCharacter = wktCharacters[index - 1];
            return leftCharacter == LEFT_PARENTHESES || leftCharacter == COMMA;
        }
        return false;
    }

    //是否结束的数字
    private boolean isEndNum(int index) {
        char character = wktCharacters[index];
        if (isNumberOrSpace(character)) {
            char rightCharacter = wktCharacters[index + 1];
            return rightCharacter == RIGHT_PARENTHESES || rightCharacter == COMMA;
        }
        return false;
    }

    //是否数字
    private boolean isNumberOrSpace(char character) {
        for (char c : ALLOW_NUM) {
            if (c == character || c == SPACE) {
                return true;
            }
        }
        return false;
    }

    //是否最后一位末尾
    public boolean isAfterEndIndex() {
        return endCharacterIndex + 1 == loopIndex;
    }

    //是否空格
    private boolean isSpace(char character) {
        return character == SPACE;
    }

    //预处理字串中的无效空格
    private String filterSpace(String content) {
        return content.replaceAll(" \\(", "(")
                .replaceAll("\\( ", "(")
                .replaceAll(" \\)", ")")
                .replaceAll("\\) ", ")")
                .replaceAll(" ,", ",")
                .replaceAll(", ", ",");
    }
}
