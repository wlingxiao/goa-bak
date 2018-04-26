package goa.pipeline

import java.util.concurrent.atomic.AtomicInteger

import goa.BaseTests

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success

class HandlerPipelineWriteTests extends BaseTests {
  test("为 pipeline 添加一个 Handler，handler 写入消息后 ret 的值将增加，ret 的值将返回 2") {
    val pipeline = new HandlerPipeline()
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

}
