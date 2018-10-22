thinkPhp5 plugin
------------------------------

## 概览

基于[Haehnchen/idea-php-laravel-plugin](https://github.com/Haehnchen/idea-php-laravel-plugin)

用于thinkphp5.0的视图,配置,路由,数据库,模型智能提示和跳转(快捷键Ctrl+B或者Ctrl+click), 及模型函数(model)返回类型分析


## 安装

在线安装: 在phpstorm的插件中心(File->Setting->Plugins), 搜索thinkphp5 plugin安装

离线安装: 下载release文件或者根目录下的plugin.zip, 在phpstorm的插件中心,点击install plugin from disk,选择下载文件安装

## 使用

配置
    
    提示方法config(),Config::get(),Config::set(),
    扫描/config和/application目录下的配置文件进行提示, 可跳转到配置文件源位置
![img](https://github.com/fw6669998/Thinkphp5-Plugin/blob/master/img/config.gif)

视图

    提示方法$this->fetch(), view(),
    可跳转到页面(html)位置
![img](https://github.com/fw6669998/Thinkphp5-Plugin/blob/master/img/view.gif)
    
路由
    
    使用Route::get/post/put/delete/any在任何位置提示, 
    使用return ['test' => ['in', ['method' => 'post']]]格式,在application和config目录下文件名带有route的文件中进行提示
![img](https://github.com/fw6669998/Thinkphp5-Plugin/blob/master/img/route.gif)
    
模型
    
    提示方法model(),Loader::model(),
    使用model()方法会对分析返回值类型进行赋给变量,最终类型为实际模型类型,而不是Model类型
![img](https://github.com/fw6669998/Thinkphp5-Plugin/blob/master/img/model.gif)

数据库

[配置phpstorm数据库连接](https://jingyan.baidu.com/article/0a52e3f4cee074bf62ed7208.html)

    首先配置请数据库连接,如果不会请点击上面的衔接
    
    提示衔接:
    如果只有一条数据库连接,则该数据库为提示连接, 
    如果有多条连接将需要提示的数据库连接命名为包含work的命名的连接, //重命名连接, 连接->右键->rename
     
    数据库会在方法的代码中收集表进行字段提示

![img](https://github.com/fw6669998/Thinkphp5-Plugin/blob/master/img/db.gif)

## 常见异常
如果模型,配置,和路由不能正常提示和跳转, 请点击File -> invalidate caches/restart 

## 搭建

选择sdk:

    选择phpstrom安装目录为sdk,
    添加jar包: 
        PhpStorm\plugins\DatabaseTools\lib\database-impl.jar,database-open.jar
        PhpStorm\plugins\php\lib\php.jar, php-openapi.jar
    附加源码:
        intellij platform平台源码地址: https://github.com/JetBrains/intellij-community
    开发文档
        intellij platform开发文档: http://www.jetbrains.org/intellij/sdk/docs/welcome.html
        
![img](https://github.com/fw6669998/Thinkphp5-Plugin/blob/master/img/dajian.png)
    
## 联系方式

邮箱: fw6669998@163.com