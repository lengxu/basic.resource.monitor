package cn.uyun.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * @ClassName: ResourceParam
 * @Description:
 * @author wuhan
 * @date 201811月09日 上午12:06:43
 * 
 */
@Setter
@Getter
public class ResourceParam {
	public String ip;

	/**
	 * 接口编码
	 */
	public String metric;
	/**
	 * 查询time
	 */
	public HashMap<String, Object> time;
	/**
	 * 过滤标签
	 */
	public HashMap<String, String> tags;
	/**
	 * 分组条件
	 */
	public HashMap<String, ArrayList<String>> group_by;
}
