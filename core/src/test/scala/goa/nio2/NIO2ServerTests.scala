package goa.nio2

import java.net.{InetSocketAddress, Socket}
import java.nio.ByteBuffer
import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}

import goa.BaseTests
import goa.pipeline.HandlerContext

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}


class NIO2ServerTests extends BaseTests {

  private val host = "localhost"

  private val port = 8083

  test("新建 NIO2Server 并启动，连接到该服务器之后 ret 的值将增加，ret 的值变为 2") {
    val ret = new AtomicInteger(1)
    val server = new NIO2Server({ cb =>
      ret.getAndIncrement()
    })

    server.start(host, port)

    val client = new Socket()
    client.connect(new InetSocketAddress(host, port))
    Thread.sleep(1000L)

    ret.get() shouldEqual 2

    server.stop()
  }

  test("新建 NIO2Server 启动并添加一个 Handler，写入数据后，ret 的值变为 2") {
    val ret = new AtomicInteger(1)
    val server = new NIO2Server({ initializer =>
      initializer.addLast((ctx: HandlerContext) => {
        ret.getAndIncrement()
      })
    })

    server.start(host, port)

    val client = new Socket()
    client.connect(new InetSocketAddress(host, port))
    client.getOutputStream.write("one".getBytes())
    Thread.sleep(1000L)

    ret.get() shouldEqual 2

    server.stop()
  }

  test("新建 NIO2Server 启动并添加一个 Handler，读取写入的数据放入 ret 中，ret 的值为 one") {
    val ret = new AtomicReference[ByteBuffer]()
    val server = new NIO2Server({ initializer =>
      initializer.addLast((ctx: HandlerContext) => {
        ctx.read().onComplete {
          case Success(buf) =>
            ret.set(buf)
          case Failure(t) =>

        }
      })
    })

    server.start(host, port)

    val client = new Socket()
    client.connect(new InetSocketAddress(host, port))
    client.getOutputStream.write("one".getBytes())
    Thread.sleep(1000L)

    ret.get().array() shouldEqual Array('o', 'n', 'e')

    server.stop()
  }

  test("新建 NIO2Server 启动并添加一个 Handler，读取服务器发送的数据，ret 的值为 one") {
    val server = new NIO2Server({ initializer =>
      initializer.addLast((ctx: HandlerContext) => {
        ctx.write(ByteBuffer.wrap("one".getBytes()))
      })
    })

    server.start(host, port)

    val client = new Socket()
    client.connect(new InetSocketAddress(host, port))
    val ret = new Array[Byte](3)
    client.getInputStream.read(ret)
    Thread.sleep(1000L)

    ret shouldEqual Array('o', 'n', 'e')

    server.stop()
  }

  test("新建 NIO2Server 启动并添加一个 Handler，服务器读取客户端数据然后返回该数据后，ret 的值变为 one") {
    val server = new NIO2Server({ initializer =>
      initializer.addLast((ctx: HandlerContext) => {
        ctx.read().onComplete {
          case Success(buf) =>
            ctx.write(buf)
          case Failure(t) =>

        }
      })
    })

    server.start(host, port)

    val client = new Socket()
    client.connect(new InetSocketAddress(host, port))
    val ret = new Array[Byte](3)
    client.getOutputStream.write("one".getBytes())
    client.getInputStream.read(ret)
    Thread.sleep(1000L)

    ret shouldEqual Array('o', 'n', 'e')

    server.stop()
  }

}
