package goa.pipeline

import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}

import goa.BaseTests

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

class HandlerPipelineWriteTests extends BaseTests {

  var pipeline: HandlerPipeline = _

  before {
    pipeline = HandlerPipeline(new HeadHandler)
  }

  test("为 pipeline 添加一个 Handler，handler 写入消息后 ret 的值将增加，ret 的值将返回 2") {
    val ret = new AtomicInteger(1)
    pipeline.addLast(ctx => {
      ctx.write("msg").onComplete {
        case Success(s) =>
          ret.incrementAndGet()
        case _ =>
      }
    })

    pipeline.sendInboundCommand()

    Thread.sleep(1000L)
    ret.get() shouldEqual 2
  }

  test("pipeline 添加两个Handler，后一个handler写入消息，ret 的值为 two") {
    val ret = new AtomicReference[String]()
    pipeline.addLast(new Handler {
      override def apply(ctx: HandlerContext): Unit = {
        ctx.next()
      }

      override def channelWrite(ctx: HandlerContext, msg: Object): Future[Unit] = {
        ret.set(msg.asInstanceOf[String])
        ctx.write(msg)
      }
    })

    pipeline.addLast((ctx: HandlerContext) => {
      ctx.write("two")
    })

    pipeline.sendInboundCommand()

    Thread.sleep(1000L)
    ret.get() shouldEqual "two"
  }

}
