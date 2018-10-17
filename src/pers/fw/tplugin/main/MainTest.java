package pers.fw.tplugin.main;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import pers.fw.tplugin.log.LogUtil;

import java.util.Arrays;

public class MainTest {
    @Test
    public void test1() {
        String name = "admin_user";
        String tableName = "qh_admin_user";
        if (tableName.contains(name)) {
            String prefix = tableName.replace(name, "");
            if (prefix.equals(tableName.substring(0, prefix.length()))) {
                if (prefix.indexOf("_") == prefix.length() - 1)
                    System.out.println("匹配成功"+tableName);
            }
        }
    }

    @Test
    public void test() {
        String str = "[ sql ] [ DB ] CONNECT:[ UseTime:0.000761s ] mysql:host=127.0.0.1;port=3306;dbname=wxa_hanse_vrccn;charset=utf8mb4";
//        String str = "123123[qewwq][qwewqeqw]";
        String[] tests = LogUtil.getContent(str);
        System.out.println(Arrays.toString(tests));
    }
}
