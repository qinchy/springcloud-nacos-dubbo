[Jmeter Java Request （十一）](https://www.cnblogs.com/mengyu/p/13307226.html)
--------------------------------------------------------------------------

实际过程中采用Jmeter压测Http接口比较多，但是实际工作中也可能会遇见一些无法通过http 来实现的，之前工作中遇见通过SDK来获取token，然后拿token再去请求其他的接口，需要借助java request来实现逻辑，下面来看下如何来实现java Request请求;

**一、实现JAVA请求代码（Java Request 核心内容）**

1、创建Maven工程；

2、pom文件添加依赖 ApacheJMeter_core 和 pacheJMeter_java；

> ```
> <dependency>
>   <groupId>org.apache.jmeter</groupId>
>   <artifactId>ApacheJMeter_core</artifactId>
>   <version>5.1.1</version>
> </dependency>
> <dependency>
>   <groupId>org.apache.jmeter</groupId>
>   <artifactId>ApacheJMeter_java</artifactId>
>   <version>5.1.1</version>
> </dependency>
> ```

3、继承AbstractJavaSamplerClient，并重写runTest()方法；

4、JAVA Request 请求例子（例子中主要模拟判断账号和密码是否一致做出不同接口，账号和密码与预期一致登录成功，设置结果为通过，账号和密码与预期不一致登录失败，设置结果为失败）；

```
package com.cfilmcloud.example;
 
 
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
 
/**
 * @Author liuqiang_cl@163.com
 * @Date 2020/7/14 16:56
 */
public class JmeterJavaRequestExample extends AbstractJavaSamplerClient {
    public void setupTest(JavaSamplerContext context) {
        //可选，测试前执行，做一些初始化工作；
        System.out.println("setupTest");
    }
 
    public void teardownTest(JavaSamplerContext context) {
        // 可选，测试结束时调用；
 
        System.out.println("teardownTest");
    }
 
    @Override
    public Arguments getDefaultParameters() {
        // 设置可用参数
        Arguments jMeterProperties = new Arguments();
        jMeterProperties.addArgument("userName", "");
        jMeterProperties.addArgument("userPassword", "");
        return jMeterProperties;
    }
 
    @Override
    public SampleResult runTest(JavaSamplerContext javaSamplerContext) {
        //必选，实现自定义请求，请求逻辑；
        SampleResult sampleResult = new SampleResult();
        sampleResult.setSampleLabel("Jmeter Java Request"); // 设置请求名称
        try {
            sampleResult.sampleStart(); //开始统计响应时间标记
            String userName = javaSamplerContext.getParameter("userName");
            String userPassword = javaSamplerContext.getParameter("userPassword");
            if (userName.equals("admin") && userPassword.equals("admin")) {
                System.out.println("登录成功");
                sampleResult.setSuccessful(true); // 设置结果成功
                sampleResult.setResponseData("登录成功", "utf-8");
 
            } else {
                System.out.println("登录失败");
                sampleResult.setSuccessful(false);  // 设置结果失败
 
            }
        } finally {
 
            sampleResult.sampleEnd();// 结束统计响应时间标记
 
        }
 
        return sampleResult;
    }
 
}
```

5、通过打包生成jar包

6、将jar包放在jmeter lib/ext目录下，重新启动Jmeter

**二、Jmeter 配置 Java Request请求**

1、新建Java Request请求

![](:/5de32142b1274ee88b1c31b5a95c366d)

2、选择要执行请求的类和配置请求参数

![](:/f1a982f93f944d58804f659f9c1dc01f)

到此Java Request请求配置完成，后续请求执行阶段与Http请求执行一致，而Jmeter request 请求的核心步骤不在于Jmeter的配置，关键在于Java 代码的实现内容，主要逻辑存放在Java代码，而Jmeter主要是来执行该代码

**三、JMeter Java Sampler介绍**

1、常用方法

public void setupTest(JavaSamplerContext context){};  // 测试启动时调用，主要做一些初始化工作；
public void teardownTest(JavaSamplerContext context){}; // 测试结束时调用，主要做一些资源回收工作；
public Arguments getDefaultParameters(){return null;}; //设置可用参数及参数默认值,主要用于Jmeter界面与代码参数之间的交互操作；
public SampleResult runTest(JavaSamplerContext javaSamplerContext) {return null;}; // 必选，实现自定义请求

代码中的方法执行的先后顺序：

**getDefaultParameters**() --> **setupTest**(JavaSamplerContext context) --> **runTest**(JavaSamplerContext context) --> **teardownTest**(JavaSamplerContext context)

2、SampleResult中常用属性

[![](:/05356a694d13475c91f5fe08f000125a)
](javascript:void(0); "复制代码")

sampleResult.setSampleLabel("Jmeter Java Request"); // 设置请求名称
sampleResult.sampleStart(); //开始统计响应时间标记
sampleResult.setSuccessful(true); // 设置请求结果成功
sampleResult.setSuccessful(false);  // 设置请求结果失败
sampleResult.sampleEnd();// 结束统计响应时间标记
sampleResult.setResponseData("登录成功", "utf-8"); // 设置Response,如果返回内容包含中文，需要指定字符串，否则会出现乱码

[![](:/05356a694d13475c91f5fe08f000125a)
](javascript:void(0); "复制代码")

3、jmeter命令行启动方式
D:\Dev_Tools\apache-jmeter-5.5\bin
λ jmeter.bat -n -t d:\数研所\JavaRequestSample.jmx -l d:\数研所\JavaRequestSample.result -e -o d:\数研所\JavaRequestSample\
Creating summariser <summary>
Created the tree successfully using d:\数研所\JavaRequestSample.jmx
Starting standalone test @ September 13, 2023 10:20:09 AM CST (1694571609140)
Waiting for possible Shutdown/StopTestNow/HeapDump/ThreadDump message on port 4445
summary +  50124 in 00:00:20 = 2463.8/s Avg:    38 Min:     0 Max:  2156 Err:     0 (0.00%) Active: 100 Started: 100 Finished: 0
summary +  57238 in 00:00:30 = 1908.8/s Avg:    53 Min:     1 Max:  3291 Err:     0 (0.00%) Active: 100 Started: 100 Finished: 0
summary = 107362 in 00:00:50 = 2133.2/s Avg:    46 Min:     0 Max:  3291 Err:     0 (0.00%)
summary + 110980 in 00:00:30 = 3699.3/s Avg:    26 Min:     1 Max:   819 Err:     0 (0.00%) Active: 100 Started: 100 Finished: 0
summary = 218342 in 00:01:20 = 2718.1/s Avg:    36 Min:     0 Max:  3291 Err:     0 (0.00%)
summary + 101862 in 00:00:30 = 3395.4/s Avg:    29 Min:     1 Max:   519 Err:     0 (0.00%) Active: 100 Started: 100 Finished: 0
summary = 320204 in 00:01:50 = 2902.2/s Avg:    34 Min:     0 Max:  3291 Err:     0 (0.00%)
summary + 111540 in 00:00:30 = 3717.4/s Avg:    26 Min:     0 Max:   639 Err:     0 (0.00%) Active: 100 Started: 100 Finished: 0
summary = 431744 in 00:02:20 = 3076.5/s Avg:    32 Min:     0 Max:  3291 Err:     0 (0.00%)
summary +  90567 in 00:00:30 = 3019.4/s Avg:    33 Min:     1 Max:   760 Err:     0 (0.00%) Active: 100 Started: 100 Finished: 0
summary = 522311 in 00:02:50 = 3066.5/s Avg:    32 Min:     0 Max:  3291 Err:     0 (0.00%)

文中代码提取链接: https://pan.baidu.com/s/182pOzVR4S2Q0jEhjnN5wjA 提取码: imgc 
