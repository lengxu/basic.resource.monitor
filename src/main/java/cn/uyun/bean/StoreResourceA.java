package cn.uyun.bean;/**
 * @author wuhan
 * @date 2018-11-22
 */

import lombok.Getter;
import lombok.Setter;

/**
 *@author wuhan
 *@date 2018-11-22
 */
@Getter
@Setter
public class StoreResourceA {
    private String field;

    private String operator = "EQ";

    private String value;
}
