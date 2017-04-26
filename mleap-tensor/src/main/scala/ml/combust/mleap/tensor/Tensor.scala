package ml.combust.mleap.tensor

import java.util

import scala.language.implicitConversions
import scala.reflect.{ClassTag, classTag}

/**
  * Created by hollinwilkins on 1/12/17.
  */
object Tensor {
  val BooleanClass = classOf[Boolean]
  val StringClass = classOf[String]
  val ByteClass = classOf[Byte]
  val ShortClass = classOf[Short]
  val IntClass = classOf[Int]
  val LongClass = classOf[Long]
  val FloatClass = classOf[Float]
  val DoubleClass = classOf[Double]

  def zero[T: ClassTag]: T = (classTag[T].runtimeClass match {
    case BooleanClass => false
    case StringClass => ""
    case ByteClass => 0: Byte
    case ShortClass => 0: Short
    case IntClass => 0: Int
    case LongClass => 0: Long
    case FloatClass => 0.0f
    case DoubleClass => 0.0d
    case _ => throw new IllegalArgumentException(s"unsupported class ${classTag[T].runtimeClass.getName}")
  }).asInstanceOf[T]

  def create[T: ClassTag](values: Array[T],
                          dimensions: Seq[Int],
                          indices: Option[Seq[Seq[Int]]]): Tensor[T] = indices match {
    case Some(is) => SparseTensor(is, values, dimensions)
    case None => DenseTensor(values, dimensions)
  }

  def denseVector[T: ClassTag](values: Array[T]): DenseTensor[T] = DenseTensor(values, Seq(values.length))
}

sealed trait Tensor[T] {
  val dimensions: Seq[Int]
  implicit val base: ClassTag[T]

  def isDense: Boolean = false
  def isSparse: Boolean = false

  def toDense: DenseTensor[T]
  def toArray: Array[T]

  def size: Int = dimensions.product
  def rawSize: Int = rawValues.length
  def rawValues: Array[T]
  def rawValuesIterator: Iterator[T]

  def apply(indices: Int *): T = get(indices: _*)
  def get(indices: Int *): T
}

case class DenseTensor[T](values: Array[T],
                          override val dimensions: Seq[Int])
                         (implicit override val base: ClassTag[T]) extends Tensor[T] {
  override def isDense: Boolean = true

  override def toDense: DenseTensor[T] = this
  override def toArray: Array[T] = values

  override def rawValues: Array[T] = values
  override def rawValuesIterator: Iterator[T] = values.iterator

  override def get(indices: Int *): T = {
    var i = 0
    var dimI = 1
    var n = indices.head
    var tail = indices.tail
    while(i < tail.size) {
      var ti = dimI
      var tn = tail.head
      tail = tail.tail
      while(ti < dimensions.size) {
        tn *= dimensions(ti)
        ti += 1
      }
      dimI += 1
      i += 1
      n += tn
    }

    values(n)
  }



  override def equals(obj: Any): Boolean = obj match {
    case obj: DenseTensor[_] =>
      if(base == obj.base) {
        if (values.isEmpty) {
          if (obj.values.isEmpty) { true }
          else { false }
        } else {
          ((dimensions == obj.dimensions) ||
            ((dimensions.head == -1 || obj.dimensions.head == -1) && dimensions.tail == obj.dimensions.tail)) &&
              (values sameElements obj.asInstanceOf[DenseTensor[T]].values)
        }
      } else { false }
    case _ => false
  }
}

case class SparseTensor[T](indices: Seq[Seq[Int]],
                           values: Array[T],
                           override val dimensions: Seq[Int])
                          (implicit override val base: ClassTag[T]) extends Tensor[T] {
  override def isSparse: Boolean = true

  override def toDense: DenseTensor[T] = {
    DenseTensor(toArray, dimensions)
  }
  override def toArray: Array[T] = {
    val array = new Array[T](dimensions.product)
    var i = 0
    indices.foreach {
      index =>
        array(denseIndex(index)) = values(i)
        i += 1
    }
    array
  }

  override def rawValues: Array[T] = values
  override def rawValuesIterator: Iterator[T] = values.iterator
  override def get(is: Int *): T = {
    val index = util.Arrays.binarySearch(indices.toArray: Array[AnyRef], is)

    if(index >= 0) { values(denseIndex(indices(index))) }
    else if(is.zip(dimensions).exists(v => v._1 >= v._2)) {
      throw new IndexOutOfBoundsException(s"index is out of bounds")
    } else { Tensor.zero[T] }
  }

  private def denseIndex(index: Seq[Int]): Int = {
    var n = index.last
    var r = dimensions
    for(i <- 0 until (index.length - 1)) {
      r = r.tail
      n += index(i) * r.product
    }

    n
  }

  override def equals(obj: Any): Boolean = obj match {
    case obj: SparseTensor[_] =>
      if(base == obj.base) {
        if (values.isEmpty) {
          if (obj.values.isEmpty) { true }
          else { false }
        } else if(indices.length == obj.indices.length && indices == obj.indices) {
          ((dimensions == obj.dimensions) ||
            ((dimensions.head == -1 || obj.dimensions.head == -1) && dimensions.tail == obj.dimensions.tail)) &&
              (values sameElements obj.asInstanceOf[SparseTensor[T]].values)
        } else { false }
      } else { false }
    case _ => false
  }
}
