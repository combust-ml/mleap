package ml.combust.mleap.runtime.transformer.feature

import ml.combust.mleap.core.feature.BinarizerModel
import ml.combust.mleap.runtime.function.UserDefinedFunction
import ml.combust.mleap.runtime.transformer.{FeatureTransformer, Transformer}
import ml.combust.mleap.tensor.Tensor
import ml.combust.mleap.runtime.converter.VectorConverters._

/**
  * Created by fshabbir on 12/1/16.
  */
case class Binarizer(override val uid: String = Transformer.uniqueName("binarizer"),
                     override val inputCol: String,
                     override val outputCol: String,
                     model: BinarizerModel) extends FeatureTransformer {
  override val exec: UserDefinedFunction = (value: Tensor[Double]) => model(value): Tensor[Double]
}
