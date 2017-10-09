package ml.combust.mleap.runtime.transformer.regression

import ml.combust.mleap.core.regression.GBTRegressionModel
import ml.combust.mleap.core.types._
import ml.combust.mleap.runtime.function.UserDefinedFunction
import ml.combust.mleap.tensor.Tensor
import ml.combust.mleap.core.util.VectorConverters._
import ml.combust.mleap.runtime.frame.{SimpleTransformer, Transformer}

/**
  * Created by hollinwilkins on 9/24/16.
  */
case class GBTRegression(override val uid: String = Transformer.uniqueName("gbt_regression"),
                         override val shape: NodeShape,
                         override val model: GBTRegressionModel) extends SimpleTransformer {
  override val exec: UserDefinedFunction = (features: Tensor[Double]) => model(features)
}
