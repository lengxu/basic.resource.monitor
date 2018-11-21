package cn.uyun.controller;

import cn.uyun.bean.ResultExceptionJson;
import cn.uyun.service.impl.ResourceServiceImpl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lihonghua on 2018/5/23.
 * 基础资源监视
 */
@RestController
@RequestMapping(value = "/api/v1/resource")
public class ResourceController {

    private static final Logger logger = LoggerFactory.getLogger(RestController.class);
    @Autowired
    private ResourceServiceImpl resourceServiceImpl;

    /**
     * 基础资源总览界面
     * 查询的所有指标开始时间为当前查询时刻提前10分钟，结束时间为当前时刻，汇聚粒度为10分钟
     * {
     * "10.40.132.21": {
     * "cpu": {
     * "val": "75%",
     * "state": "-1"   //-1：未从store获取到数据。1：小于一级阈值。2：介于一级与二级阈值之间。3：大于二级阈值
     * },
     * "mem": {
     * "val": "75%",
     * "state": "-1"
     * },
     * "online": "true",
     * "process": "-1",     //-1：未从store获取到数据。1：正常。0：异常
     * }
     * }
     */
    @RequestMapping(value = "/pandect", method = RequestMethod.POST)
    public String queryPandectInfo(@RequestBody JSONObject jsonObject) {
        Long currentTime = System.currentTimeMillis();
        ArrayList<String> ips = (ArrayList<String>) jsonObject.get("ip");
        if (ips == null || ips.size() == 0) {
            return JSON.toJSONString(ResultExceptionJson.paramNotNull());
        }
        LinkedHashMap<String, HashMap> result = new LinkedHashMap<>();
        //由于这里使用率多线程，因此单纯通过linkedhashmap不能保证返回的数据是按照前台传递的ip进行排序的
        LinkedHashMap<String, HashMap> sequencerResult = new LinkedHashMap<>();
        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
        for (String ip : ips) {
            cachedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    LinkedHashMap<String, Object> indictors = new LinkedHashMap<>();
                    //查询cpu使用率-system.cpu.pct_usage
                    Object cpu_pctUsage = resourceServiceImpl.queryCpuInfo(ip, currentTime, false);
                    //查询内存使用率-system.mem.pct_usage
                    Object mem_pctUsage = resourceServiceImpl.queryMemInfo(ip, currentTime, false);
                    //查询网络
                    String str = resourceServiceImpl.queryHostState(ip);
                    int online_state = StringUtils.isBlank(str) ? -1 : ("true".equals(str) ? 1 : 0);

                    //查询进程-system.processes.status
                    Object process = resourceServiceImpl.queryProcessInfo(ip, currentTime, false);
                    //查询磁盘使用率-system.disk.pct_usage
                    Object disk_pctUsage = resourceServiceImpl.queryDiskInfo(ip, currentTime, false, null);
                    indictors.put("cpu_pctUsage", cpu_pctUsage);
                    indictors.put("mem_pctUsage", mem_pctUsage);
                    indictors.put("host_state", online_state);
                    indictors.put("process", process);
                    indictors.put("disk_pctUsage", disk_pctUsage);
                    if ((cpu_pctUsage + "").contains("point=0") || (mem_pctUsage + "").toString().contains("point=0") || online_state == 0 || "0".equals(process + "") || "0".equals(disk_pctUsage + "")) {
                        //首先判断各个指标状态种是否有红色，如果有则总状态为红色，没有则跳过检查黄颜色
                        indictors.put("mainState", 0);
                    } else if ((cpu_pctUsage + "").contains("point=2") || (mem_pctUsage + "").contains("point=2")) {
                        //由于上面已经判断完红色了，这里黄颜色作为第二优先级进行判断，如果存在则总状态显示黄色，否则继续坚持绿色
                        indictors.put("mainState", 2);
                    } else if ((cpu_pctUsage + "").contains("point=1") || (mem_pctUsage + "").contains("point=1") || online_state == 1 || "1".equals(process + "") || "1".equals(disk_pctUsage + "")) {
                        //判断第三优先级绿色
                        indictors.put("mainState", 1);
                    } else {
                        //当所有指标种即不包含红色、黄色、绿色，则说明说有指标都是灰色，则总状态为灰色
                        indictors.put("mainState", -1);
                    }
                    result.put(ip, indictors);
                }
            });

        }
        cachedThreadPool.shutdown();
        while (true) {
            if (cachedThreadPool.isTerminated()) {
                long endTime = System.currentTimeMillis();
                System.out.println("共耗时：" + (endTime - currentTime));
                for (String ip : ips) {
                    sequencerResult.put(ip, result.get(ip));
                }
                return JSON.toJSONString(sequencerResult);
            }
        }
    }

    /**
     * @Description: /api/v1/resource/detail?ip=10.20.66.30&indictor=sys_info
     * @Param:
     * @return:
     */
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    public String queryDetailInfo(@RequestParam String ip, @RequestParam String indictor) {
        Long currentTime = System.currentTimeMillis();
        if (StringUtils.isBlank(ip) || StringUtils.isBlank(indictor))
            return JSON.toJSONString(ResultExceptionJson.paramNotNull());
        Object result = null;
        switch (indictor) {
            case "sys_info":
                //系统信息
                return resourceServiceImpl.querySystemInfo(ip);
            case "cpu_info":
                //cpu信息-system.cpu.user与system.cpu.system
                result = resourceServiceImpl.queryCpuInfo(ip, currentTime, true);
                break;
            case "mem_pctUsage":
                //内存使用率-system.mem.pct_usage
                result = resourceServiceImpl.queryMemInfo(ip, currentTime, true);
                break;
            case "net_flow":
                //网络流量-system.net.bytes_rcvd与system.net.bytes_sent
                result = resourceServiceImpl.queryNetwork(ip, currentTime, true);
                break;
            case "disk_pctUsage":
            case "disk_free":
                result = resourceServiceImpl.queryDiskInfo(ip, currentTime, true, indictor);
                break;
            case "process":
                //查询进程
                result = resourceServiceImpl.queryProcessInfo(ip, currentTime, true);
                break;
            default:
                result = ResultExceptionJson.paramIllegal();
                break;
        }
        return JSON.toJSONString(result);
    }

    public static void main(String[] args) {
        String s = String.valueOf(null);
        System.out.println(s);
    }
}
