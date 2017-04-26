package ml.combust.mleap.runtime.transformer.feature

import ml.combust.mleap.core.feature.IDFModel
import ml.combust.mleap.runtime.function.UserDefinedFunction
import ml.combust.mleap.runtime.transformer.{FeatureTransformer, Transformer}
import ml.combust.mleap.tensor.Tensor
import ml.combust.mleap.core.util.VectorConverters._

/**
  * Created by hollinwilkins on 12/28/16.
  */
case class IDF(override val uid: String = Transformer.uniqueName("idf"),
               override val inputCol: String,
               override val outputCol: String,
               model: IDFModel) extends FeatureTransformer {
  override val exec: UserDefinedFunction = (features: Tensor[Double]) => model(features): Tensor[Double]
}
