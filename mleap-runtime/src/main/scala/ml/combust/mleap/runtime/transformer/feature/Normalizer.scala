package ml.combust.mleap.runtime.transformer.feature

import ml.combust.mleap.core.feature.NormalizerModel
import ml.combust.mleap.core.types.NodeShape
import ml.combust.mleap.runtime.function.UserDefinedFunction
import ml.combust.mleap.tensor.Tensor
import ml.combust.mleap.core.util.VectorConverters._
import ml.combust.mleap.runtime.frame.{SimpleTransformer, Transformer}

/**
  * Created by hollinwilkins on 9/24/16.
  */
case class Normalizer(override val uid: String = Transformer.uniqueName("normalizer"),
                      override val shape: NodeShape,
                      override val model: NormalizerModel) extends SimpleTransformer {
  override val exec: UserDefinedFunction = (value: Tensor[Double]) => model(value): Tensor[Double]
}
