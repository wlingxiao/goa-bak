package goa.util

import java.nio.ByteBuffer

import scala.annotation.tailrec

object BufferUtils {

  def checkEmpty(buffers: Array[ByteBuffer]): Boolean = {
    @tailrec
    def checkEmpty(i: Int): Boolean =
      if (i < 0) true
      else if (!buffers(i).hasRemaining()) checkEmpty(i - 1)
      else false

    checkEmpty(buffers.length - 1)
  }

  def dropEmpty(buffers: Array[ByteBuffer]): Int = {
    val max = buffers.length - 1
    var first = 0
    while (first < max && !buffers(first).hasRemaining()) {
      buffers(first) = emptyBuffer
      first += 1
    }
    first
  }

  val emptyBuffer: ByteBuffer = allocate(0)

  def allocate(size: Int): ByteBuffer = ByteBuffer.allocate(size)

}
