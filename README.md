#rpc。
	tcp层？原生socket：自己管理连接，字符的编解码，拆包，组包
	netty：

```json
{
  "seq":123456,//序列化
  "param":{}, //参数
  "method":"",//方法
  "className":"" //接口名称
}
```


#目的：做一个远程方法调用。封装底层网络通信和多线程等技术细节，做到调用远程接口方法，就想调用本地接口方法一样。

#需要做的事情：

1、组包(包结构，序列化) --------protobuff做序列化(二进制协议)，请求封包对象为request，应答的封包对象为response ------完成

2、获得远程连接信息 ---------zookeeper做服务调用接口的发布和订阅(zk的配置，集成)----------完成

3、服务的路由和负载 -------------  服务路由信息（支持轮询和随机，权重，动态脚本暂时不做） ------------------ 完成

5、封装服务调用  ------------jdk 动态代理 + spring FactoryBean（cglib或javasiss做动态类生成?）生成客户端调用逻辑  ----------完成

6、根据已有服务端，做软负载  ---------需要另外一个监控子系统，做调用量统计（做动态调用）----------------待做

7、心跳做服务端存活监控 ------服务端挂掉（两分钟内没有收到心跳包，关闭客户端连接），客户端30S发一次心跳，心跳超时3次就会关闭服务端连接   ---------------完成

8、服务实现类和容器的结合 -----支持spring注解服务实现类，支持配置实现 ---------完成

9、优化：
	 服务端收包+业务逻辑处理共用一个线程，需要分开(业务处理线程使用线程池)--------- 完成
	 服务调用超时重试(调用失败，重试其它服务器，最多重试3次) --------------完成
         服务提供节点动态增加减少，客户端自动处理--------完成 
         服务端重启，客户端不需要启动，但是需要重连将---------完成(超过3次心跳失败，进行重连，如果重连两次失败，则放入删除队列)

10、稳定性测试，压力测试

11、抽取程序中写死的配置

问题：
 	1、服务端应用kill调，客户端无法感知网络异常（心跳30S检测一次，超过3次，进行重连接）-------完成

rpc:
理论基础
http://blog.jobbole.com/92290/
前辈的知识
http://my.oschina.net/huangyong/blog/361751
netty知识
http://blog.csdn.net/boonya/article/details/43795325?ref=myread
http://www.infoq.com/cn/articles/netty-million-level-push-service-design-points


