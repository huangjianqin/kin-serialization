# **kinbuffer**
自研发的序列化工具, 仅仅支持在java app之间使用, 即不支持多语言

特性:
* 使用变长整形压缩整形
* 支持动态类型处理, 同时, 可以使用`@MessageId`注解标识message类, 以使用整形替代字符串表达类信息, 以达到减少序列化后字节数的目的
* 使用unsafe替代反射提高性能, 同时, 支持使用bytebuddy代理替换unsafe, 以防后续jdk版本更新移除unsafe
* 支持使用`@Signed`注解标识有符号整形字段, 框架底层会使用zigzag编码, 以减少有符号整形序列后的字节数
* 支持使用者自定义序列化和反序列化模板, 详情请看`org.kin.kinbuffer.runtime.Schema`
* 支持使用者自定义字节流输入输出, 详情请看`org.kin.kinbuffer.io.Input`和`org.kin.kinbuffer.io.Output`
* 目前暂不支持序列化`Throwable`异常实现类, 使用者可使用异常转字符串替代
* 支持field number, 向后(向前)兼容消息定义

## 展望
* 优化field number支持
* 优化底层IO输入输出