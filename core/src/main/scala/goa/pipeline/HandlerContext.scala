package goa.pipeline

import java.nio.ByteBuffer

import scala.concurrent.Future

class HandlerContext {

  var prevCtx: HandlerContext = _

  var nextCtx: HandlerContext = _

  var handler: Handler = _

  var pipeline: HandlerPipeline = _

  def write(msg: Object): Future[Unit] = {
    if (prevCtx != null) {
      prevCtx.handler.channelWrite(prevCtx, msg)
    } else Future.failed(new Exception)
  }

  def read(): Future[ByteBuffer] = {
    if (prevCtx != null) {
      prevCtx.handler.channelRead(prevCtx)
    } else Future.failed(new Exception(""))
  }

  def next(): Unit = {
    if (nextCtx != null) {
      nextCtx.handler.apply(nextCtx)
    }
  }
}
