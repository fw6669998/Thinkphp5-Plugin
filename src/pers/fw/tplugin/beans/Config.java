package pers.fw.tplugin.beans;

import java.util.ArrayList;
import java.util.List;

public class Config {   //配置类
    public String dbPrefix="";      //数据库前缀
    public String[] dbMethod = {};  //数据库提示方法
    public String[] dbArrMethod={}; //数据库数组参数提示方法
    public String[] dbVar ={};      //数据库提示字段提示变量

    public boolean logEnable = false;   //日志开关
    public String[] logPrefix = {};     //显示日志内容的前缀
}
