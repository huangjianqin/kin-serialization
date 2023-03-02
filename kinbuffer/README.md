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
* 每个对象实例序列化前都会写一个byte标识对象是否为null进而支持序列化实例存在null字段
* 支持使用`@MessageId`或者`Runtime.registerClass(...)`支持序列化实例id, 进而当遇到不确定类型时, 仅需写入id, 而不用把class name写入, 进而达到减少字节数的效果
* 支持有限的泛化能力, 即当父类是`Object`和`abstract`支持序列化和反序列化, 而当父类是正常的pojo, 则仅支持序列化和反序列化父类相关字段, 而忽略实际类型定义的字段

##  注意事项
1. 不能修改字段类型, 肯定会出现兼容问题
2. 不能随意修改枚举定义顺序, 底层逻辑是基于定义顺序来唯一标识枚举
3. 基于java类为模板生成序列化和反序列化逻辑, 即序列化结果并不会保存任何字段类型, 因此类继承结构或(父)类字段定义顺序
    * 允许修改字段名
    * `@Deprecated`标识的`Object`类型字段, 不管字段是否有值, 都会被认为null

不能完美支持的原因: 序列化结果不存储类型，无法按类型跳过舍弃的bytes，嵌套类型不存储长度，无法确认边界

### 向后兼容
同时使用`@Version`和`@Since`来支持向后兼容, 序列化class可以不加`@Version`, 一旦修改, 则需要`@Version`来标识, 同时版本号要大于之前定义的版本号, 新增的字段使用`@Since`
来标识哪个版本添加的字段, 同时注意`@Since`的版本一经定义就不能修改, 否则都会出现兼容问题

### 向前兼容思考
* 不打算支持, 老版本不清楚新版本新增的字段类型, 要实现向前兼容, 则需要把类型信息和序列化后bytes大小写入序列化结果, 那么kinbuffer整体性能还不如直接使用protobuf好
* 实现限制主要是因为向前向后兼容主要受限于类型信息和字段序列化后bytes大小没有写入序列化结果
* Chunked Encoding(分块编码): 序列化结果为TLV(Type-Length-Value), 但往往完成对字段数据的序列化后才知道大小, 那么而一开始就定义一个足够大的缓冲区, 来存放序列化后的内容, 完成所有字段序列化后, 
再写数据长度和数据内容, 显然是不可取的. 因为我们不知道数据会有多大, 这个缓存区可能会大得非常不合理. 而且这么做也失去了“流式处理”的特性. 分块编码就是用来解决这个问题的. “分块编码”使用了一个较小的缓存区.
缓存区被塞满时, 就写一次缓存区数据长度, 再写缓存区数据. 这么一次操作的数据被称为“一块数据”. 然后缓冲区会被清空, 并继续执行后续操作, 直到处理完所有数据. 最后再写个数字“0”, 表示已完成所有数据块的处理.

## 展望
* 支持序列化`Throwable`异常实现类
* 思考是否可以真正实现schema向前向后兼容