package ml.combust.mleap.runtime.transformer.feature

import ml.combust.mleap.core.feature.VectorIndexerModel
import ml.combust.mleap.runtime.function.UserDefinedFunction
import ml.combust.mleap.runtime.transformer.FeatureTransformer
import ml.combust.mleap.tensor.Tensor
import ml.combust.mleap.core.util.VectorConverters._
import ml.combust.mleap.runtime.types.{DoubleType, StructField, TensorType}

import scala.util.{Success, Try}

/**
  * Created by hollinwilkins on 12/28/16.
  */
case class VectorIndexer(override val uid: String,
                         override val inputCol: String,
                         override val outputCol: String,
                         model: VectorIndexerModel) extends FeatureTransformer {
  override val exec: UserDefinedFunction = (features: Tensor[Double]) => model(features): Tensor[Double]

  override def getSchema(): Try[Seq[StructField]] = Success(
    Seq(StructField(inputCol, TensorType(DoubleType())),
      StructField(outputCol, TensorType(DoubleType()))))
}
