/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package scalacl

import com.nativelibs4java.opencl._
import org.bridj.Pointer
import org.bridj.Pointer._
import org.bridj.SizeT
import scala.collection.JavaConversions._

class CLFilteredArray[T](
  val values: CLGuardedBuffer[T],
  initialPresence: CLGuardedBuffer[Boolean]
)(
  implicit t: ClassManifest[T],
  context: ScalaCLContext
) 
extends CLCol[T] 
   with CLUpdatableFilteredCol[T]
   with CLUpdatableCol[T]
{
  type ThisCol[T] = CLFilteredArray[T]
  
  val presence = if (initialPresence == null)
    new CLGuardedBuffer(context.context.createBuffer(CLMem.Usage.InputOutput, classOf[Boolean], values.size))
  else
    initialPresence

  lazy val presencePrefixSum = new CLGuardedBuffer[Long](values.size)

  def this(array: CLGuardedBuffer[T])(implicit t: ClassManifest[T], context: ScalaCLContext) = this(
    array, null)

  //def args = Seq(values.buffer, presence.buffer)

  override def clone =
    new CLFilteredArray(values.clone, presence.clone)
  
  def clone(newStart: Long, newEnd: Long) =
    new CLFilteredArray(values.clone(newStart, newEnd), presence.clone(newStart, newEnd))

  def view: CLView[T, ThisCol[T]] = notImp
  def slice(from: Long, to: Long): CLCol[T] = notImp
  
  def zipWithIndex: ThisCol[(T, Long)] = notImp
  def toCLArray: CLArray[T] = {
    val prefixSum = updatedPresencePrefixSum
    val size = this.size.get
    val out = new CLGuardedBuffer[T](size)
    ScalaCLUtils.copyPrefixed(size, prefixSum, values, out)
    new CLArray(out)
  }

  var prefixSumUpToDate = false
  def updatedPresencePrefixSum = this.synchronized {
    if (!prefixSumUpToDate) {
      ScalaCLUtils.prefixSum(presence, presencePrefixSum)
      prefixSumUpToDate = true
    }
    presencePrefixSum
  }
  def size: CLFuture[Long] = {
    //error("Filtered array size not implemented yet, needs prefix sum implementation")
    val ps = updatedPresencePrefixSum
    ps(values.size - 1)
    //new CLInstantFuture(ps.toArray.last)
  }

  override def update(f: T => T): CLFilteredArray[T] =
    doMap(f, this)

  override def map[V](f: T => V)(implicit v: ClassManifest[V]): CLFilteredArray[V] =
    doMap(f, new CLFilteredArray(new CLGuardedBuffer[V](values.size), presence.clone))
          
  protected def doMap[V](f: T => V, out: CLFilteredArray[V])(implicit v: ClassManifest[V]): CLFilteredArray[V] = {
    println("map should not be called directly, you haven't run the compiler plugin or it failed")
    readBlock {
      val ptr = values.toPointer
      val presencePtr = presence.toPointer

      val newPtr = if (v.erasure.equals(t.erasure))
        ptr.asInstanceOf[Pointer[V]]
      else
        allocateArray(v.erasure.asInstanceOf[Class[V]], values.size).order(context.order)

      var i = 0L
      while (i < values.size) {
        if (presencePtr.get(i).booleanValue) {
          val x = ptr.get(i)
          val y = f(x)
          newPtr.set(i, y)
        }
        i += 1
      }
      out.values.write(evts => {
        assert(evts.forall(_ == null))
        out.values.buffer.write(context.queue, newPtr, false)
      })
    }
    out
  }
  override def update(f: CLFunction[T, T]): CLFilteredArray[T] = {
    this.synchronized {
      prefixSumUpToDate = false
      doMap(f, this)
    }
    this
  }
  override def map[V](f: CLFunction[T, V])(implicit v: ClassManifest[V]): CLFilteredArray[V] = {
    val out = new CLFilteredArray(new CLGuardedBuffer[V](values.size), presence.clone)
    doMap(f, out)
    out
  }

  private val localSizes = Array(1)

  protected def doMap[V](f: CLFunction[T, V], out: CLFilteredArray[V]) = {
    val kernel = f.getKernel(context, this, out)
    assert(values.size <= Int.MaxValue)
    val globalSizes = Array(values.size.asInstanceOf[Int])
    kernel.synchronized {
      kernel.setArgs(new SizeT(values.size), values.buffer, presence.buffer, out.values.buffer)
      // TODO cut size bigger than int into global and local sizes
      presence.read(presenceEvts => {
          if (this == out)
            values.write(evts => {
              kernel.enqueueNDRange(context.queue, globalSizes, localSizes, (evts ++ presenceEvts):_*)
            })
          else
            values.read(readEvts => {
              out.values.write(writeEvts => {
                kernel.enqueueNDRange(context.queue, globalSizes, localSizes, (readEvts ++ writeEvts ++ presenceEvts):_*)
              })
            })
        })
    }
    out
  }

  override def refineFilter(f: T => Boolean): CLFilteredArray[T] =
    doFilter(f, this)

  override def filter(f: T => Boolean): CLFilteredArray[T] =
    doFilter(f, new CLFilteredArray(values.clone, new CLGuardedBuffer[Boolean](values.size)))

  protected def doFilter(f: T => Boolean, out: CLFilteredArray[T]): CLFilteredArray[T] = {
    println("filter should not be called directly, you haven't run the compiler plugin or it failed")
    //val out = new CLFilteredArray(values.clone, new CLGuardedBuffer[Boolean](values.size), start, end)
    
    val ptr = values.toPointer
    val presencePtr = presence.toPointer
    val newPresencePtr = allocateBooleans(values.size).order(context.order)

    var i = 0L
    while (i < values.size) {
      newPresencePtr.set(i, presencePtr.get(i).booleanValue && f(ptr.get(i)))
      i += 1
    }
    out.presence.write(evts => {
      assert(evts.forall(_ == null))
      out.presence.buffer.write(context.queue, newPresencePtr.asInstanceOf[Pointer[Boolean]], false)
    })
    out
  }
  
  override def filter(f: CLFunction[T, Boolean]): CLFilteredArray[T] =
    filter(f, new CLFilteredArray(values.clone, new CLGuardedBuffer[Boolean](values.size)))

  override def refineFilter(f: CLFunction[T, Boolean]): CLFilteredArray[T] =
    this.synchronized {
      prefixSumUpToDate = false
      filter(f, this)
    }


  protected def filter(f: CLFunction[T, Boolean], out: CLFilteredArray[T]): CLFilteredArray[T] = {
    val out = new CLFilteredArray(values.clone, new CLGuardedBuffer[Boolean](values.size))

    val kernel = f.getKernel(context, this, out)
    assert(values.size <= Int.MaxValue)
    val globalSizes = Array(values.size.asInstanceOf[Int])
    kernel.synchronized {
      kernel.setArgs(new SizeT(values.size), values.buffer, presence.buffer, out.presence.buffer)
      // TODO cut size bigger than int into global and local sizes
      values.read(readEvts => {
        out.presence.write(writeEvts => {
          kernel.enqueueNDRange(context.queue, globalSizes, localSizes, (readEvts ++ writeEvts):_*)
        })
      })
    }
    out
  }
}
