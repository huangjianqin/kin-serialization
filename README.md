# **kin-serialization**
java 序列化相关工具

## **目前流行的java序列化框架性能对比**
![](doc/performance/total.jpg)
![](doc/performance/serialize.jpg)
![](doc/performance/deserialize.jpg)
![](doc/performance/size.jpg)
![](doc/performance/size+dfl.jpg)
![](doc/performance/create.jpg)

### jsonb
以二进制形式序列化json, 达到压缩序列化后字节数的效果, 目前不严谨测试, jsonb比json字节数少一半, 与protobuf相当

#### jsonb设计
* 和JSON格式对应，能完全表示JSON
* 紧凑，数据不留空洞
* 对常用整数-16~63做紧凑设计
* 对null/true/false做紧凑设计
* 对0~15长度的ARRAY 做紧凑设计
* 对0~47长度的ascii编码的字符串做紧凑设计
* 对OBJECT类型的Name做短编码的支持
* 支持完整的Java序列化和反序列化

## **实现**
### **java内置+hessian+protobuf**
缓存并复用`ByteArrayOutputStream`, 减少轻微的创建`ByteArrayOutputStream`带来性能消耗. 细节请看`OutputStreams`

### **protobuf+kryo**
底层使用基于`ByteBuffer`, `DirectByteBuffer`和`Unsafe`提升序列化和反序列化性能. 细节请看`Inputs`和`Outputs`

## **注意事项**
* 目前, 除了kinbuffer, 原生protobuf和kryo外, 其余序列化仅支持序列化和反序列一个对象实例, 其原因归咎于无法区分每个对象序列化后的边界
* 目前, 仅有kinbuffer, json和jsonb支持(反)序列化Object[], 原则上原生protobuf也支持, 但我们一般不会这么使用