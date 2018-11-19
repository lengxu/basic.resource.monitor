package cn.uyun.bean;

import java.util.List;

/**
 * Created by lihonghua on 2018/5/22.
 */
public class PostInfo {

    private String beginTime;
    private String endTime;
    private String metric;
    private String group_by;
    private List<Object> tags;

    public List<Object> getTags() {
        return tags;
    }

    public void setTags(List<Object> tags) {
        this.tags = tags;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getGroup_by() {
        return group_by;
    }

    public void setGroup_by(String group_by) {
        this.group_by = group_by;
    }

}
