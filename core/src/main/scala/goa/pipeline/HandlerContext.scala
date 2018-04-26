package goa.pipeline

import java.nio.ByteBuffer

import scala.concurrent.Future

class HandlerContext(val handler: Handler, val pipeline: HandlerPipeline) {

  var prevCtx: HandlerContext = _

  var nextCtx: HandlerContext = _

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

object HandlerContext {

  def apply(handler: Handler, pipeline: HandlerPipeline): HandlerContext = {
    new HandlerContext(handler, pipeline)
  }

}