package ml.combust.mleap.runtime.transformer.feature

import ml.combust.mleap.core.feature.BinarizerModel
import ml.combust.mleap.runtime.function.UserDefinedFunction
import ml.combust.mleap.runtime.transformer.Transformer
import ml.combust.mleap.tensor.Tensor
import ml.combust.mleap.core.util.VectorConverters._
import ml.combust.mleap.runtime.transformer.builder.TransformBuilder
import ml.combust.mleap.runtime.types.{DataType, DoubleType, StructField, TensorType}

import scala.util.{Failure, Success, Try}

/**
  * Created by fshabbir on 12/1/16.
  */
case class Binarizer(override val uid: String = Transformer.uniqueName("binarizer"),
                     inputCol: String,
                     inputDataType: Option[DataType],
                     outputCol: String,
                     outputDataType: Option[DataType],
                     model: BinarizerModel) extends Transformer {
  val execTensor: UserDefinedFunction = (value: Tensor[Double]) => model(value): Tensor[Double]
  val execDouble: UserDefinedFunction = (value: Double) => model(value): Double

  override def transform[TB <: TransformBuilder[TB]](builder: TB): Try[TB] = {
    builder.schema.getField(inputCol).map(_.dataType).map {
      case DoubleType(false) =>
        builder.withOutput(outputCol, inputCol)(execDouble)
      case tt: TensorType if tt.base == DoubleType() && !tt.isNullable =>
        builder.withOutput(outputCol, inputCol)(execTensor)
    }.getOrElse(Failure(new IllegalArgumentException("Input column must be double or double tensor")))
  }

  override def getFields(): Try[Seq[StructField]] = {
    if (inputDataType == None || outputDataType == None) {
      return Failure(new RuntimeException(s"Cannot determine schema for transformer ${this.uid}"))
    }

    Success(Seq(StructField(inputCol, inputDataType.get),
                StructField(outputCol, outputDataType.get)))
  }
}
