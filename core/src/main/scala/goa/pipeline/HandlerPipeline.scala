package goa.pipeline

trait HandlerPipeline {

  def addLast(handler: Handler): HandlerPipeline

  def sendInboundCommand(): Unit
}

object HandlerPipeline {

  private class Impl(headHandler: Handler) extends HandlerPipeline {

    private val head: HandlerContext = new HandlerContext(headHandler, this)

    private var tail = head

    def addLast(handler: Handler): HandlerPipeline = {
      val newCtx = new HandlerContext(handler, this)
      tail.nextCtx = newCtx
      newCtx.prevCtx = tail
      tail = newCtx
      this
    }

    def sendInboundCommand(): Unit = {
      head.handler.apply(head)
    }
  }

  def apply(headHandler: Handler): HandlerPipeline = {
    new Impl(headHandler)
  }

}

