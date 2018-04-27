package goa.nio2

import java.net.{InetSocketAddress, Socket}
import java.util.concurrent.atomic.AtomicInteger

import goa.BaseTests

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

}
