## 一、stub 和 skeleton
在RPC（远程过程调用）中，"stub" 和 "skeleton" 是两个关键概念，它们代表着不同的角色和功能：

1. **Stub**:
- Stub 是客户端用于代表远程服务的本地代理对象。
- 在客户端实现，作为客户端程序调用远程服务的方式。
- Stub 模拟了远程服务的接口，并通过网络将方法调用传递到实际的远程服务端。
- 客户端调用 Stub 对象的方法时，实际上是在发送请求到远程服务器，并接收处理远程服务器返回的结果。
2. **Skeleton**:
- Skeleton 是远程服务在服务器端的代理对象。
- 在服务器端实现，用于接收客户端请求、解析请求并调用本地服务的方式。
- Skeleton 接收来自客户端的请求，解包请求并将请求转发到本地服务的实际实现。
- 它提供了远程服务接口的本地实现，通过网络接收来自客户端的调用请求，并将其转发到实际的服务对象。

总的来说，Stub 位于客户端，是客户端用来调用远程服务的本地代理对象； 而 Skeleton 位于服务器端，是服务器用来处理客户端请求并实际执行远程服务的代理对象。 Stub 主要用于客户端编程，Skeleton 则用于服务端编程，它们分别代表了在客户端和服务端对远程服务进行调用和处理的方式。
## 二、Netty 和 NettyHttp 两种RPC协议的区别
### Netty
Netty底层使用Hessian2序列化协议，在channel中直接传输RpcRequest和RpcResponse对象，但是入站和出站需要经过NettyEncoder和NettyDecoder编码和解码（序列化/反序列化），相当于是TCP协议。
### NettyHttp
NettyHttp底层使用Hessian2序列化协议，在channel中传输之前需要将RpcRequest和RpcResonse对象装箱和拆箱为Http报文格式（请求头：host+keepAlive+contentLenth 和 请求体），入站和出站需要经过Netty包下的HttpClientCodec和HttpObjectAggregator编码和解码（装箱/拆箱+序列化/反序列化），相当于是HTTP协议。
### http协议可以不用json序列化而用hessian2序列化方式么？

是的，HTTP 请求和响应的数据可以使用不同的序列化方式，包括 Hessian2 格式。Hessian2 是一种二进制序列化协议，与 JSON 格式相比，它更加紧凑，适合在网络传输中使用。
要在 HTTP 请求和响应中使用 Hessian2 序列化方式，你需要做以下工作：

1. **服务端和客户端协商使用 Hessian2 序列化**：确保客户端和服务端都能理解并使用 Hessian2 序列化协议。你需要在服务端和客户端的代码中配置和实现对应的序列化和反序列化逻辑。
2. **设置合适的请求头和响应头**：在 HTTP 请求和响应的头部信息中设置合适的 Content-Type。Hessian2 格式的 Content-Type 通常是 **application/x-hessian2**。这样可以告知服务端和客户端使用 Hessian2 格式进行序列化和反序列化。
### 整体流程
### [https://www.cnblogs.com/ssskkk/p/14891061.html](https://www.cnblogs.com/ssskkk/p/14891061.html)
