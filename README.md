thinkPhp3 plugin
------------------------------

## 概述

参考[Haehnchen/idea-php-laravel-plugin](https://github.com/Haehnchen/idea-php-laravel-plugin)

用于thinkphp3的视图,配置,路由,数据库,模型智能提示和跳转(快捷键Ctrl+B或者Ctrl+click), 及模型函数(D)返回类型分析,


## 安装

离线安装: 下载release文件或者根目录下的plugin.zip, 在phpstorm的插件中心,点击Install plugin from disk,选择下载文件安装

## 功能

    模型提示和跳转, 并进行类型分析(返回变量有方法提示), 方法: D
    配置提示和跳转, 方法: C
    视图提示和跳转, 方法:display,T,fetch
    数据库提示,     //必须先在phpstorm中配置数据库连接
        表提示方法: M, join   //表
        字段提示方法: where,聚合函数,field,order等方法
        字段提示变量: $where,$row,$field
    筛选thinkphp日志输出phpstorm,     //需配置tplugin.json
      
配置数据库连接

[配置phpstorm数据库连接](https://jingyan.baidu.com/article/0a52e3f4cee074bf62ed7208.html)
     
配置文件
    
    该文件用于配置某些功能, 不配置也可以没有关系
    将配置文件放在项目下即可生效,  //配置模板及说明:tplugin.json文件