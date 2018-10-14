package pers.fw.tplugin.main;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import pers.fw.tplugin.log.LogUtil;

import java.util.Arrays;

public class MainTest {
    @Test
    public void test() {
        String str = "[ sql ] [ DB ] CONNECT:[ UseTime:0.000761s ] mysql:host=127.0.0.1;port=3306;dbname=wxa_hanse_vrccn;charset=utf8mb4";
//        String str = "123123[qewwq][qwewqeqw]";
        String[] tests = LogUtil.getContent(str);
        System.out.println(Arrays.toString(tests));
    }
}
