package com.odd.rpc.core.serialize;

/**
 * serializer
 *
 * 模板方法模式：定义一个操作中算法的骨架（或称为顶级逻辑），将一些步骤（或称为基本方法）的执行延迟到其子类中；
 * 基本方法：抽象方法 + 具体方法fianl + 钩子方法
 * Enum 是最好的单例方案；枚举单例会初始化全部实现，此处改为托管Class，避免无效的实例化占用资源
 *
 * @author oddity
 * @create 2023-11-23 17:53
 */
public abstract class Serializer {

    public abstract <T> byte[] serialize(T obj);

    public abstract <T> Object deserialize(byte[] bytes, Class<T> clazz);

    /*public enum SerializeEnum {
		HESSIAN(HessianSerializer.class),
		HESSIAN1(Hessian1Serializer.class);

		private Class<? extends Serializer> serializerClass;
		private SerializeEnum (Class<? extends Serializer> serializerClass) {
			this.serializerClass = serializerClass;
		}

		public Serializer getSerializer() {
			try {
				return serializerClass.newInstance();
			} catch (Exception e) {
				throw new XxlRpcException(e);
			}
		}

		public static SerializeEnum match(String name, SerializeEnum defaultSerializer){
			for (SerializeEnum item : SerializeEnum.values()) {
				if (item.name().equals(name)) {
					return item;
				}
			}
			return defaultSerializer;
		}
	}*/
}
