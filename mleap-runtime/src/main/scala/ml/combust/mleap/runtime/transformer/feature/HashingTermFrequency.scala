package ml.combust.mleap.runtime.transformer.feature

import ml.combust.mleap.core.feature.HashingTermFrequencyModel
import ml.combust.mleap.runtime.function.UserDefinedFunction
import ml.combust.mleap.runtime.transformer.{FeatureTransformer, Transformer}
import ml.combust.mleap.tensor.Tensor
import ml.combust.mleap.core.util.VectorConverters._

/**
  * Created by hwilkins on 12/30/15.
  */
case class HashingTermFrequency(override val uid: String = Transformer.uniqueName("hashing_term_frequency"),
                                override val inputCol: String,
                                override val outputCol: String,
                                model: HashingTermFrequencyModel) extends FeatureTransformer {
  override val exec: UserDefinedFunction = (value: Seq[String]) => model(value): Tensor[Double]
}
