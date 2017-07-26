package ml.combust.mleap.core.feature

import ml.combust.mleap.core.Model
import ml.combust.mleap.core.types.{BasicType, ScalarType, StructField, StructType}

/**
  * Created by mikhail on 12/18/16.
  */
case class ImputerModel(surrogateValue: Double,
                        missingValue: Double,
                        strategy: String,
                        nullableInput: Boolean = false) extends Model {
  def predictAny(value: Any): Double = value match {
    case value: Double => apply(value)
    case value: Option[_] => apply(value.asInstanceOf[Option[Double]])
  }

  def apply(value: Double): Double = {
    if(value.isNaN || value == missingValue) surrogateValue else value
  }

  def apply(value: Option[Double]): Double = value match {
    case Some(v) => apply(v)
    case None => surrogateValue
  }

  override def inputSchema: StructType = StructType(StructField("input" -> ScalarType(BasicType.Double, nullableInput))).get

  override def outputSchema: StructType = StructType(StructField("output" -> ScalarType.Double)).get
}