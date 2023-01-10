# **kinbuffer**
高性能高压缩率的序列化工具

特性:
* 使用变长整形压缩整形
* 支持动态类型处理, 默认写出class name以标识实际类型
  * 支持message id注册, 以使用整形替代字符串表达类信息, 以达到减少序列化后字节数的目的. 
  可以通过`Runtime#registerMessageIdClass(int,Class)`, 或者使用注解`@MessageId`标识message class, 
  然后调用`Runtime#getSchema(Class)`时会自动注册
* 支持使用`@Signed`注解标识有符号整形字段, 框架底层会使用zigzag编码, 以减少有符号整形序列后的字节数
* 支持自定义序列化和反序列化模板, 详情请看`Schema`
* 支持自定义序列化字节流的输入输出, 详情请看`Input`和`Output`
* 支持字段为null, 针对Object类型的字段会多写一个byte标识该字段是否为非null
* 目前暂不支持序列化`Throwable`异常实现类, 使用者可使用异常转字符串替代

## 性能
* 序列化和反序列化性能, 整体上略优于protobuf, 接近于kryo
* 序列化后的字节数大小略小于kryo, 远小于protobuf

### 优化过程
未优化前, 测试过程, 会产生大量短暂存活的对象, 导致大量young gc, 影响了反序列化的性能. 
通过`JProfiler`的`allocation call tree`和`allocation hot spots`, 查看对象创建触发点. 参考`Kryo`和`Protostuff`的实现, 
进而减少了大量`无用`对象创建, 减少young gc, 提高反序列化的性能

主要优化点:
* 使用unsafe处理primitive 字段, 减少其包装类实例创建
* `MessageCollectionSchema`和`MessageMapSchema`线程本地化, 针对动态类型, 一个线程仅仅创建一个schema来执行反序列化逻辑, 以减少schema实例创建
* `MessageArraySchema`, 单个实例即可处理多维数组, 而不用通过嵌套多个`MessageArraySchema`实例来实现多维数组反序列化, 以减少`MessageArraySchema`实例创建
* 反序列化时, 通过`sun.reflect.ReflectionFactory.getReflectionFactory().newConstructorForSerialization(Class,Constructor)`来创建消息类, 
  减少了消息类初始化过程创建的实例(因为反序列化过程会给这些字段赋值, 其实无需默认值, 故这些对象的创建是不必要的), 以减少实例创建

##  注意事项
### 向后兼容
1. 不能修改字段类型, 肯定会出现兼容问题
2. 不能随意修改枚举定义顺序, 底层逻辑是基于定义顺序来唯一标识枚举
3. 基于java类为模板生成序列化和反序列化逻辑, 即序列化结果并不会保存任何字段类型, 因此类继承结构或(父)类字段定义顺序
   * 允许修改字段名
   * `@Deprecated`标识的`Object`类型字段, 不管字段是否有值, 都会被认为null,

## 展望
* 支持序列化`Throwable`异常实现类
* 思考是否可以真正实现schema向前向后兼容