package goa.pipeline

import java.nio.ByteBuffer
import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}

import goa.BaseTests

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

class HandlerPipelineReadTests extends BaseTests {

  var pipeline: HandlerPipeline = _

  before {
    pipeline = new HandlerPipeline()
  }

  test("为 pipeline 添加一个 Handler，handler 读取消息后 ret 的值将增加，ret 的值将返回 2") {
    val ret = new AtomicInteger(1)
    pipeline.addLast(ctx => {
      ctx.read().onComplete {
        case Success(s) =>
          ret.incrementAndGet()
        case _ =>
      }
    })

    pipeline.sendInboundCommand()

    Thread.sleep(1000L)
    ret.get() shouldEqual 2
  }

  test("为 pipeline 添加两个 handler，前一个 handler 调用 next 后再执行后一个handler，此时 ret 的值将返回 2") {
    val ret = new AtomicInteger(1)

    pipeline.addLast { ctx =>
      ctx.next()
    }

    pipeline.addLast { ctx =>
      ctx.read().onComplete {
        case Success(s) =>
          ret.incrementAndGet()
        case _ =>
      }
    }

    pipeline.sendInboundCommand()
    Thread.sleep(1000L)
    ret.get() shouldEqual 2

  }

  test("为 pipeline 添加两个 handler，第二个 handler 读取消息，ret 的值为第一个 handler 的返回值 ") {
    val ret = new AtomicReference[ByteBuffer]()

    pipeline.addLast(new Handler {
      override def apply(ctx: HandlerContext): Unit = {
        ctx.next()
      }

      override def channelRead(ctx: HandlerContext): Future[ByteBuffer] = {
        Future.successful(ByteBuffer.wrap("one".getBytes()))
      }
    })

    pipeline.addLast { ctx =>
      ctx.read().onComplete {
        case Success(s) =>
          ret.set(s)
        case _ =>
      }
    }

    pipeline.sendInboundCommand()
    Thread.sleep(1000L)
    ret.get().array() shouldEqual Array('o', 'n', 'e')

  }

  test("为 pipeline 添加三个 handler，最后一个handler读取数据时，结果为前两个 handler 结果的和 ") {
    val ret = new AtomicReference[ByteBuffer]()

    pipeline.addLast(new Handler {
      override def apply(ctx: HandlerContext): Unit = {
        ctx.next()
      }

      override def channelRead(ctx: HandlerContext): Future[ByteBuffer] = {
        Future.successful(ByteBuffer.wrap("one".getBytes()))
      }
    })

    pipeline.addLast(new Handler {
      override def apply(ctx: HandlerContext): Unit = {
        ctx.next()
      }

      override def channelRead(ctx: HandlerContext): Future[ByteBuffer] = {
        val promise = Promise[ByteBuffer]()
        ctx.read().onComplete {
          case Success(b) =>
            val n = ByteBuffer.allocate(6)
            n.put(b.array())
            n.put("tow".getBytes())
            promise.trySuccess(n)
          case Failure(t) =>
        }
        promise.future
      }
    })

    pipeline.addLast { ctx =>
      ctx.read().onComplete {
        case Success(s) =>
          ret.set(s)
        case _ =>
      }
    }

    pipeline.sendInboundCommand()
    Thread.sleep(1000L)
    ret.get().array() shouldEqual "onetow".getBytes()
  }
}
