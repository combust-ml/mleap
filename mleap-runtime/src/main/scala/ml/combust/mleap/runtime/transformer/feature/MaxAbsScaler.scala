package ml.combust.mleap.runtime.transformer.feature

import ml.combust.mleap.core.feature.MaxAbsScalerModel
import ml.combust.mleap.core.types.NodeShape
import ml.combust.mleap.runtime.function.UserDefinedFunction
import ml.combust.mleap.tensor.Tensor
import ml.combust.mleap.core.util.VectorConverters._
import ml.combust.mleap.runtime.frame.{SimpleTransformer, Transformer}

/**
  * Created by mikhail on 9/18/16.
  */
case class MaxAbsScaler(override val uid: String = Transformer.uniqueName("max_abs_scaler"),
                        override val shape: NodeShape,
                        override val model: MaxAbsScalerModel) extends SimpleTransformer {
  override val exec: UserDefinedFunction = (value: Tensor[Double]) => model(value): Tensor[Double]
}
