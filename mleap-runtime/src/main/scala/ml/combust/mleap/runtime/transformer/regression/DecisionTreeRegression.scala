package ml.combust.mleap.runtime.transformer.regression

import ml.combust.mleap.core.regression.DecisionTreeRegressionModel
import ml.combust.mleap.core.types._
import ml.combust.mleap.runtime.function.UserDefinedFunction
import ml.combust.mleap.tensor.Tensor
import ml.combust.mleap.core.util.VectorConverters._
import ml.combust.mleap.runtime.frame.{SimpleTransformer, Transformer}

/**
  * Created by hwilkins on 11/8/15.
  */
case class DecisionTreeRegression(override val uid: String = Transformer.uniqueName("decision_tree_regression"),
                                  override val shape: NodeShape,
                                  override val model: DecisionTreeRegressionModel) extends SimpleTransformer {
  val exec: UserDefinedFunction = (features: Tensor[Double]) => model(features)
}
