package cn.uyun.bean;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by 吴晗 on 2018/8/27.
 */
public class ResultExceptionJson {

	//参数不能为空
	public static String paramNotNull(){
		String string = "{\"success\":false, \"error\":\"请求失败，参数不能为空！\"}";
        return JSONObject.parseObject(string).toJSONString();
	}

	//参数非法
	public static String paramIllegal(){
		String string = "{\"success\":false, \"error\":\"请求失败，参数非法！\"}";
		return JSONObject.parseObject(string).toJSONString();
	}

	//参数非法
	public static String serverInnerException(String msg){
		String string = "{\"success\":false, \"error\":\"服务器内部错误\""+msg+"}";
		return JSONObject.parseObject(string).toJSONString();
	}
}
