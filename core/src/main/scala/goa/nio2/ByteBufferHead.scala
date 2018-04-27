package goa.nio2

import java.lang.{Long => JLong}
import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousSocketChannel, CompletionHandler}
import java.util.concurrent.TimeUnit

import goa.pipeline.{Handler, HandlerContext}
import goa.util.BufferUtils

import scala.concurrent.{Future, Promise}

private[nio2] final class ByteBufferHead(channel: AsynchronousSocketChannel, bufferSize: Int) extends Handler {


  override def apply(ctx: HandlerContext): Unit = {
    ctx.next()
  }


  override def channelRead(ctx: HandlerContext): Future[ByteBuffer] = {
    readRequest(-1)
  }

  override def channelWrite(ctx: HandlerContext, msg: Object): Future[Unit] = {
    writeRequest(msg.asInstanceOf[ByteBuffer])
  }

  @volatile
  private[this] var closeReason: Throwable = null
  private[this] val buffer = ByteBuffer.allocateDirect(bufferSize)

  def writeRequest(data: ByteBuffer): Future[Unit] =
    writeRequest(data :: Nil)

  def writeRequest(data: Seq[ByteBuffer]): Future[Unit] = {
    val reason = closeReason
    if (reason != null) Future.failed(reason)
    else if (data.isEmpty) Future.successful(())
    else {
      val p = Promise[Unit]
      val srcs = data.toArray

      def go(index: Int): Unit =
        channel.write[Null](
          srcs,
          index,
          srcs.length - index,
          -1L,
          TimeUnit.MILLISECONDS,
          null: Null,
          new CompletionHandler[JLong, Null] {
            def failed(exc: Throwable, attachment: Null): Unit = {
              p.tryFailure(exc)
              ()
            }

            def completed(result: JLong, attachment: Null): Unit =
              if (!BufferUtils.checkEmpty(srcs)) go(BufferUtils.dropEmpty(srcs))
              else {
                p.success(())
                ()
              }
          }
        )

      go(0)
      p.future
    }
  }

  def readRequest(size: Int): Future[ByteBuffer] = {
    val p = Promise[ByteBuffer]
    buffer.clear()

    if (size >= 0 && size < bufferSize) {
      buffer.limit(size)
    }

    channel.read(
      buffer,
      null: Null,
      new CompletionHandler[Integer, Null] {
        def failed(exc: Throwable, attachment: Null): Unit = {
          p.failure(exc)
          ()
        }

        def completed(i: Integer, attachment: Null): Unit = i.intValue match {
          case 0 =>
            p.success(BufferUtils.emptyBuffer)
            ()
          case i if i < 0 =>
          case i =>
            buffer.flip()
            val b = ByteBuffer.allocate(buffer.remaining)
            b.put(buffer).flip()
            p.success(b)
            ()
        }
      }
    )
    p.future
  }
}
