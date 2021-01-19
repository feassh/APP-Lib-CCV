package ceneax.lib.bean;

/**
 * @Description: 比对结果 实体类
 * @Date: 2021/1/19 17:15
 * @Author: ceneax
 */
public class CompareRes {

    // 相似度最高的索引
    private int index;
    // 相似度最高的值
    private double value;

    public CompareRes(int index, double value) {
        this.index = index;
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

}
