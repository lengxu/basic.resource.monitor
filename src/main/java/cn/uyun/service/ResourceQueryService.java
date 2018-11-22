package cn.uyun.service;

import cn.uyun.bean.ResourceParam;
import cn.uyun.bean.StoreResource;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * @Description: 指标查询接口(以store为主，monitor为辅)
 * @author wuhan
 * @date 2018年11月08日 上午9:33:15
 */
@FeignClient(name = "basicResourceMonitor",url = "http://master.cma.cn")
public interface ResourceQueryService {

    /**
    *@Description: cpu、内存、磁盘、进程状态的store查询接口
    *@Param: apikey、接口所需参数
    *@return: store返回的指标json串
    */
	@RequestMapping(value="/store/openapi/v2/datapoints/query?apikey={id}",method= RequestMethod.POST,consumes="application/json")
    public String indictorQuery(@PathVariable("id") String apikey, @RequestBody ResourceParam resourceParam);//?apikey=e10adc3949ba59abbe56e057f2gg88dd

    /**
    *@Description: 通过store-resource接口查询resourceId
    *@Param: apikey和主机ip
    *@return: monitor返回的json串
    */
    @RequestMapping(value = "/store/openapi/v2/resources/query",method= RequestMethod.POST,consumes="application/json")
    public String queryResourceIdByIp(@RequestParam("apikey") String apikey, @RequestBody StoreResource storeResource);

    /**
     *@Description: 通过store接口查询主机上下线状态
     *@Param: apikey和resourceId
     *@return: monitor返回的json串
     */
    @RequestMapping(value = "/store/openapi/v2/checkperiods/get")
    public String queryHostOnlineState(@RequestParam("apikey") String apikey, @RequestParam("state")String state, @RequestParam("object")String object);

    /**
    *@Description: 通过monitor接口获取主机详情
    *@Param: apikey和主机ip
    *@return: monitor返回的json串
    */
    @RequestMapping(value = "/monitor/openapi/v2/host/get")
    public String systemInfoQuery(@RequestParam("apikey") String apikey, @RequestParam("id") String resourceId);
}
