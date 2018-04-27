package goa.nio2

import java.net.InetSocketAddress
import java.nio.channels.{AsynchronousCloseException, AsynchronousServerSocketChannel, AsynchronousSocketChannel, CompletionHandler}

import goa.Logging
import goa.pipeline.HandlerPipeline

class NIO2Server(initializer: (HandlerPipeline) => Unit) extends Logging {

  private val server = AsynchronousServerSocketChannel.open()

  def start(host: String, port: Int): Unit = {
    server.bind(new InetSocketAddress(host, port))
    listen()
    log.info(s"Server start on $port")
  }

  private def normalClose(): Unit = {

  }

  def listen(): Unit = {
    val handler = new CompletionHandler[AsynchronousSocketChannel, Null] {
      override def completed(channel: AsynchronousSocketChannel, attachment: Null): Unit = {
        listen()
        val pipeline = HandlerPipeline(new ByteBufferHead(channel, 1024))
        initializer(pipeline)
        pipeline.sendInboundCommand()
      }

      override def failed(exc: Throwable, attachment: Null): Unit = {
        exc match {
          case _: AsynchronousCloseException =>
            normalClose()
          case _ =>
            log.error(s"Error accepting connection", exc)
        }
      }
    }
    server.accept(null, handler)
  }

  def stop(): Unit = {
    server.close()
  }

}
