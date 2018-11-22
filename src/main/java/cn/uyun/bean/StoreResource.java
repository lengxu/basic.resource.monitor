package cn.uyun.bean;/**
 * @author wuhan
 * @date 2018-11-22
 */

/**
 *@author wuhan
 *@date 2018-11-22
 */

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/**
 *
 {
     "needCount": true,
     "pageSize": -1,
     "conditions": [
        {
             "field": "ip",
             "operator": "EQ",
             "value": "10.0.86.143"
         },
        {
             "cjt": "OR",
             "items": [
                 {
                     "field": "classCode",
                     "value": "VM"
                 },
                 {
                     "field": "classCode",
                     "value": "PCServer"
                 }
             ]
         }
     ]
 }
 *
*/
@Getter
@Setter
public class StoreResource {
    // 是否需要统计满足查询条件的记录总数
    private boolean needCount = true;

    private int pageSize = -1;

    private ArrayList conditions = new ArrayList();
}
