package ml.combust.mleap.runtime.transformer.clustering

import ml.combust.mleap.core.clustering.LocalLDAModel
import ml.combust.mleap.runtime.converter.VectorConverters._
import ml.combust.mleap.runtime.function.UserDefinedFunction
import ml.combust.mleap.runtime.transformer.Transformer
import ml.combust.mleap.runtime.transformer.builder.TransformBuilder
import ml.combust.mleap.tensor.Tensor

import scala.util.Try

/**
  * Created by mageswarand on 3/3/17.
  */
case class LDAModel(override val uid: String = Transformer.uniqueName("lda"),
                    featureCol: String,
                    topicDistributionCol: String,
                    model: LocalLDAModel) extends Transformer {

  val topicDistribution: UserDefinedFunction = (features: Tensor[Double]) => model.topicDistribution(features) : Tensor[Double]

  override def transform[TB <: TransformBuilder[TB]](builder: TB): Try[TB] = {
    builder.withOutput(topicDistributionCol, featureCol)(topicDistribution)
  }
}
