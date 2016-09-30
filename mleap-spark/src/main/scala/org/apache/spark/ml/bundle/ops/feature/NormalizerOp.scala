package org.apache.spark.ml.bundle.ops.feature

import ml.combust.bundle.dsl._
import ml.combust.bundle.op.{OpModel, OpNode}
import ml.combust.bundle.serializer.BundleContext
import org.apache.spark.ml.feature.Normalizer

/**
  * Created by hollinwilkins on 9/24/16.
  */
object NormalizerOp extends OpNode[Normalizer, Normalizer] {
  override val Model: OpModel[Normalizer] = new OpModel[Normalizer] {
    override def opName: String = Bundle.BuiltinOps.feature.normalizer

    override def store(context: BundleContext, model: Model, obj: Normalizer): Model = {
      model.withAttr(Attribute("p_norm", Value.double(obj.getP)))
    }

    override def load(context: BundleContext, model: Model): Normalizer = {
      new Normalizer(uid = "").setP(model.value("p_norm").getDouble)
    }
  }

  override def name(node: Normalizer): String = node.uid

  override def model(node: Normalizer): Normalizer = node

  override def load(context: BundleContext, node: Node, model: Normalizer): Normalizer = {
    new Normalizer(uid = node.name).copy(model.extractParamMap()).
      setInputCol(node.shape.standardInput.name).
      setOutputCol(node.shape.standardOutput.name)
  }

  override def shape(node: Normalizer): Shape = Shape().withStandardIO(node.getInputCol, node.getOutputCol)
}
