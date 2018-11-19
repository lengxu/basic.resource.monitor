package cn.uyun.service.impl;

import cn.uyun.bean.ResourceParam;
import cn.uyun.service.ResourceQueryService;
import cn.uyun.utils.DateTool;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;

/**
 * @author wuhan
 * @Description: 基础资源查询服务层
 * @date 2018年11月08日 上午9:33:15
 */
@Service
public class ResourceServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(ResourceServiceImpl.class);
    private static final String apiKey = "e10adc3949ba59abbe56e057f2gg88dd";
    private HashMap<String, HashMap<String, Object>> time = new HashMap<>();
    private HashMap<String, String> tags = new HashMap<>();
    private HashMap<String, ArrayList<String>> group_by = new HashMap<>();
    private static final Long miniteInterval = 601000L;   //10分钟,store文档上写的默认单位为秒，实际测试为毫秒.多设置1ms是为了防止汇聚粒度=结束时间-开始时间偶尔会出现返回多个指标的情况
    private static final Long dayInterval = 86400000L;   //10分钟,store文档上写的默认单位为秒，实际测试为毫秒
    private static final long threshold1 = Double.doubleToLongBits(80);   //一级阈值 小于一级阈值为1
    private static final long threshold2 = Double.doubleToLongBits(95);  //二级阈值   一级阈值到二级阈值之间为2， 大于一级阈值为3
    private static final long diskThreshold = Double.doubleToLongBits(95);    //磁盘阈值，超过阈值则异常，返回0

    @Autowired
    private ResourceQueryService resourceQueryService;

    /**
    *@Description: 查询系统信息，1.通过ip查询resourceId。2.通过resourceId查询系统信息、
    *@Param:
    *@return:
    */
    public String querySystemInfo(String ip){
        String resourceId = queryResourceId(ip, "id");
        String info = resourceQueryService.systemInfoQuery(apiKey, resourceId);
        return info;
    }

    /**
     * @Description: 查询主机是否在线
     * @Param: ip
     * @return: true或者false
     */
    public String queryHostState(String ip) {
        return queryResourceId(ip,"online_state");
    }

    /**
    *@Description: 获取resourceId
    *@Param:
    *@return:
    */
    public String queryResourceId(String ip, String key){
        String result = resourceQueryService.hostStateQuery(apiKey, ip);
        System.out.println(result);
        JSONArray jsonArray = JSON.parseArray(result);
        JSONObject jsonObject = JSON.parseObject(jsonArray.get(0).toString());
        String val = jsonObject.get(key).toString();
        return val;
    }

    /**
     * @Description: 查询cpu信息
     * @Param:
     * @return:
     */
    public Object queryCpuInfo(String ip, Long currentTime, boolean detail) {
        if(detail){
            //查询cpu的系统和用户使用率 TODO cpu详情
            HashMap result = new HashMap();
            Object user = queryDetail(ip, currentTime, "system.cpu.user", "avg", null, "cpu");
            Object system = queryDetail(ip, currentTime, "system.cpu.system", "avg", null, "cpu");
            result.put("system.cpu.user", user);
            result.put("system.cpu.system", system);
            return result;
        }else {
            return queryPandect(ip, currentTime, "system.cpu.pct_usage", "last", null, "cpu");
        }
    }

    /**
     * @Description: 查询内存信息
     * @Param: 平均使用率
     * @return:
     */
    public Object queryMemInfo(String ip, Long currentTime, boolean detail) {
        if(detail){
            //查询内存使用率
            return queryDetail(ip, currentTime, "system.mem.pct_usage", "avg", null, "mem");
        }else {
            return queryPandect(ip, currentTime, "system.mem.pct_usage", "last", null, "mem");
        }
    }

    /**
     *@Description: TODO 查询网络流量
     *@Param:
     *@return:
     */
    public Object queryNetwork(String ip, Long currentTime, boolean detail){
        HashMap result = new HashMap();
        Object rceive = queryDetail(ip, currentTime, "system.net.bytes_rcvd", "avg", null, "network");
        Object send = queryDetail(ip, currentTime, "system.net.bytes_sent", "avg", null, "network");
        result.put("system.net.bytes_rcvd", rceive);
        result.put("system.net.bytes_sent", send);
        return result;
    }

    /**
     * @Description: 查询磁盘信息
     * @Param: disk_free：磁盘使用量、disk_pctUsage：磁盘使用率
     * @return:
     */
    public Object queryDiskInfo(String ip, Long currentTime, boolean detail, String type) {
        //String indictor = type != null && type.equals("disk_free") ? "system.disk.free" : "system.disk.pct_usage";
        if(detail){
            if(type.equals("disk_free")){
                //查询磁盘使用量
                return queryDetail(ip, currentTime, "system.disk.free", "last", "device", "disk_free");
            }else {
                //查询磁盘使用率
                return queryDetail(ip, currentTime, "system.disk.pct_usage", "last", "device", "disk_pctUsage");
            }
        }else {
            return queryPandect(ip, currentTime, "system.disk.pct_usage", "last", "device", "disk");
        }
    }

    /**
     * @Description: 查询主机进程状态
     * @Param: detail为true代表查询的是详情界面，否则查询总览界面
     * @return: -1 store接口返回内容为空
     * 1:存活。0异常
     */
    public Object queryProcessInfo(String ip, Long currentTime, boolean detail) {
        if(detail){
            return queryDetail(ip, currentTime, "system.processes.status", "last", "process_name", "process");
        }else {
            return queryPandect(ip, currentTime, "system.processes.status", "last", "process_name", "process");
        }
    }

    /**
     * @Description: 封装调用store接口所需要的消息体
     * @Param: [code]：指标编码、ip、currentTime：询的当前时刻、aggregator：汇聚方式、tag：过滤标签
     * @return: ResourceParam
     */
    public ResourceParam generateParam(String code, String ip, Long currentTime, String aggregator, String tag, boolean detail) {
        ResourceParam resourceParam = new ResourceParam();
        resourceParam.setMetric(code);

        HashMap<String, Object> map = new HashMap<>();
        long interval = detail ? dayInterval : miniteInterval;
        map.put("start", currentTime - interval);
        map.put("end", currentTime);
        map.put("interval", miniteInterval);
        map.put("aggregator", aggregator);
        resourceParam.setTime(map);

        tags.put("ip", ip);
        resourceParam.setTags(tags);

        if (tag != null) {
            ArrayList<String> tag_keys = new ArrayList<>();
            tag_keys.add(tag);
            group_by.put("tag_keys", tag_keys);
            resourceParam.setGroup_by(group_by);
        }
        return resourceParam;
    }

    /**
     * @Description: 查询总览数据
     * @Param:
     * @return:
     */
    public Object queryPandect(String ip, Long currentTime, String indictorCode, String aggregator, String groupBy_tag, String type) {
        Object obj = null;
        try {
            ResourceParam resourceParam = generateParam(indictorCode, ip, currentTime, aggregator, groupBy_tag, false);
            String result = resourceQueryService.indictorQuery(apiKey, resourceParam);
            JSONArray jsonArray = JSON.parseArray(result);
            HashMap<String, Object> map = new HashMap<>();
            ArrayList<HashMap<String, Object>> arrayList = new ArrayList<>();
            String point = "-1";
            for (int i = 0; i < jsonArray.size(); i++) {    //cpu和内存只会循环一次，进程和磁盘会循环多次
                JSONObject jsonObject = JSON.parseObject(jsonArray.get(i).toString());
                JSONObject groups = JSON.parseObject(jsonObject.get("group").toString());
                if (groups.size() == 0) { //代表监控项为空
                    logger.warn("ip为：" + ip + "的主机未监控 "+type);
                    return null;
                }
                JSONObject points = JSON.parseObject(jsonObject.get("points").toString());
                if (points.size() == 0) { //代表当前查询时间范围没有值
                    if ("cpu".equals(type) || "mem".equals(type)) {
                        map.put("point", "-1");
                        continue;
                    } else {
                        return -1;
                    }
                }
                Iterator<Map.Entry<String, Object>> iterator = points.entrySet().iterator();    //总览
                Map.Entry<String, Object> next = iterator.next();

                String str = next.getValue().toString();
                switch (type) {
                    case "process":
                        int val = Double.valueOf(str).intValue();
                        int newState = Double.valueOf(point).intValue();
                        newState = newState == -1 ? val : newState & val;
                        point = String.valueOf(newState);

                        if (i == jsonArray.size() - 1) {
                            //最后一条数据
                            return point;
                        }
                        break;
                    case "cpu":
                    case "mem":
                        if (map.get("point") == null) {
                            Double valueOf = Double.valueOf(str);
                            //判断cpu的使用率是否超过阈值
                            long t = Double.doubleToLongBits(valueOf);
                            if (t < threshold1) {
                                point = "1";
                            } else if (t > threshold1 && t < threshold2) {
                                point = "2";
                            } else if (t > threshold2) {
                                point = "0";
                            }
                        }
                        map.put("val", str);
                        map.put("point", point);
                        break;
                    case "disk":
                        Double valueOf = Double.valueOf(str);
                        //判断cpu的使用率是否超过阈值
                        long t = Double.doubleToLongBits(valueOf);
                        if (t > diskThreshold)
                            return "0";
                        if (i == jsonArray.size() - 1)
                            return "1";
                        break;
                    default:
                        break;
                }
            }
            obj = map;
        } catch (Exception e) {
            logger.error("获取 IP 为：" + ip + " 的" + type + "总览数据失败。");
        }
        return obj;
    }

    /**
     * @Description: 查询详情
     * @Param:
     * @return:
     */
    public Object queryDetail(String ip, Long currentTime, String indictorCode, String aggregator, String groupBy_tag, String type) {
        Object obj = null;
        try {
            ResourceParam resourceParam = generateParam(indictorCode, ip, currentTime, aggregator, groupBy_tag, true);
            String result = resourceQueryService.indictorQuery(apiKey, resourceParam);
            JSONArray jsonArray = JSONArray.parseObject(result.getBytes(), JSONArray.class, Feature.OrderedField);
            ArrayList<HashMap<String, Object>> arrayList = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {    //cpu和内存只会循环一次，进程和磁盘会循环多次
                HashMap<String, Object> map = new HashMap<>();
                JSONObject jsonObject = JSON.parseObject(jsonArray.get(i).toString(), Feature.OrderedField);
                JSONObject groups = JSON.parseObject(jsonObject.get("group").toString());
                if (groups.size() == 0) { //代表该ip没有监控进程
                    return null;
                }
                JSONObject points = JSON.parseObject(jsonObject.get("points").toString(), Feature.OrderedField);
                if (points.size() == 0) { //代表当前查询时间范围没有值
                    if ("cpu".equals(type) || "mem".equals(type)) {
                        return null;    //如果为cpu或内存，直接返回空，前台的图显示的值是空的
                    } else {
                        //为进程或磁盘，代表其中的某一个为空，则跳过
                        continue;
                    }
                }
                Iterator<Map.Entry<String, Object>> iterator = points.entrySet().iterator();

                switch (type) {
                    case "process":
                        Map.Entry<String, Object> next = iterator.next();
                        String date = next.getKey();

                        String str = next.getValue().toString();
                        String point = "";
                        if(type.equals("process")){
                            String processName = groups.get("process_name").toString();
                            map.put("processName", processName);
                            point = String.valueOf(Double.valueOf(str).intValue());
                        }else if(type.equals("disk_pctUsage")){
                            String deviceName = groups.get("device").toString();
                            map.put("deviceName", deviceName);
                            point = str;
                        }
                        map.put("point", point);
                        map.put("time", DateTool.dateToString(new Date(object2Bigdecimal(date).longValue()), "yyyy-MM-dd HH:mm:ss"));
                        arrayList.add(map);
                        break;
                    case "cpu":
                    case "mem":
                    case "network":
                        while(iterator.hasNext()){
                            map = new HashMap<>();
                            Map.Entry<String, Object> entry = iterator.next();
                            String key = entry.getKey();
                            BigDecimal ret = object2Bigdecimal(key);
                            String time = DateTool.dateToString(new Date(ret.longValue()), "yyyy-MM-dd HH:mm:ss");
                            Object value = entry.getValue();
                            map.put("time", time);
                            map.put("number", value);
                            arrayList.add(map);
                        }
                        break;
                    case "disk_free":
                    case "disk_pctUsage":
                        map = new HashMap<>();
                        HashMap m = new HashMap();
                        Map.Entry<String, Object> entry = iterator.next();
                        String key = entry.getKey();
                        String val = entry.getValue().toString();   //kb
                        String device = groups.get("device").toString();
                        System.out.println("val:"+val);
                        m.put("point", type.equals("disk_free") ? kb2gb(val) : val);
                        m.put("time", DateTool.dateToString(new Date(object2Bigdecimal(key).longValue()), "yyyy-MM-dd HH:mm:ss"));
                        map.put(device, m);
                        arrayList.add(map);
                        break;
                    default:
                        break;
                }
            }
            obj = arrayList;
        } catch (Exception e) {
            logger.error("获取 IP 为：" + ip + " 的" + type + "详情数据失败。");
        }
        return obj;
    }

    @SuppressWarnings("all")
    public static BigDecimal object2Bigdecimal(Object value) {
        BigDecimal ret = null;
        if (value != null) {
            if (value instanceof BigDecimal) {
                ret = (BigDecimal) value;
            } else if (value instanceof String) {
                ret = new BigDecimal((String) value);
            } else if (value instanceof BigInteger) {
                ret = new BigDecimal((BigInteger) value);
            } else if (value instanceof Number) {
                ret = new BigDecimal(((Number) value).doubleValue());
            } else {
                throw new ClassCastException("Not possible to coerce [" + value + "] from class " + value.getClass() + " into a BigDecimal.");
            }
        } else {
            logger.error("从momitor获取的时间戳为空！");
        }
        return ret;
    }

    /**
     * 针对数字字符串，将kb转换为mb,并保留小数点后2位
     * in：待转换字符串
     * return：转换后字符串
     */
    public String kb2mb(String kbData){
        return kb2Other(kbData, 1);
    }

    /**
     * 针对数字字符串，将kb转换为gb,并保留小数点后2位
     * in：待转换字符串
     * return：转换后字符串
     */
    public String kb2gb(String kbData){
        return kb2Other(kbData, 2);
    }

    /**
    *@Description: 将kb数据转换为其他数据，比如mb、gb、tb
    *@Param: kbData：待转换的数据。flag：取值为1、2、3，分别对应转换后的数据为mb、gb、tb
    *@return:
    */
    public String kb2Other(String kbData, int flag){
        if(StringUtils.isNotBlank(kbData) && (flag == 1 || flag == 2 || flag == 3)){
            BigDecimal decimal = new BigDecimal(kbData);
            BigDecimal _1024 = new BigDecimal(1024);
            while(flag > 0){
                decimal = decimal.divide(_1024);
                flag--;
            }
            String result = decimal.setScale(2, RoundingMode.UP).toString();
            return result;
        }
        return "";
    }
}
