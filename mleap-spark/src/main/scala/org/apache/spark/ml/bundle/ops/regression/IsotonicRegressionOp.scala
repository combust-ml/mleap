package org.apache.spark.ml.bundle.ops.regression

import ml.combust.bundle.BundleContext
import ml.combust.bundle.dsl._
import ml.combust.bundle.op.{OpModel, OpNode}
import org.apache.spark.ml.bundle._
import org.apache.spark.ml.linalg.VectorUDT
import org.apache.spark.ml.param.Param
import org.apache.spark.ml.regression.IsotonicRegressionModel
import org.apache.spark.mllib.regression

/**
  * Created by hollinwilkins on 12/27/16.
  */
class IsotonicRegressionOp extends SimpleSparkOp[IsotonicRegressionModel] {
  override val Model: OpModel[SparkBundleContext, IsotonicRegressionModel] = new OpModel[SparkBundleContext, IsotonicRegressionModel] {
    override val klazz: Class[IsotonicRegressionModel] = classOf[IsotonicRegressionModel]

    override def opName: String = Bundle.BuiltinOps.regression.isotonic_regression

    override def store(model: Model, obj: IsotonicRegressionModel)
                      (implicit context: BundleContext[SparkBundleContext]): Model = {
      assert(context.context.dataset.isDefined, BundleHelper.sampleDataframeMessage(klazz))

      var m = model.withValue("boundaries", Value.doubleList(obj.boundaries.toArray.toSeq)).
        withValue("predictions", Value.doubleList(obj.predictions.toArray.toSeq)).
        withValue("isotonic", Value.boolean(obj.getIsotonic))

      if(context.context.dataset.get.schema(obj.getFeaturesCol).dataType.isInstanceOf[VectorUDT]) {
        m = m.withValue("feature_index", Value.long(obj.getFeatureIndex))
      }

      m
    }

    override def load(model: Model)
                     (implicit context: BundleContext[SparkBundleContext]): IsotonicRegressionModel = {
      val oldModel = new regression.IsotonicRegressionModel(boundaries = model.value("boundaries").getDoubleList.toArray,
        predictions = model.value("predictions").getDoubleList.toArray,
        isotonic = model.value("isotonic").getBoolean)
      val m = new IsotonicRegressionModel(uid = "",
        oldModel = oldModel)
      model.getValue("feature_index").foreach(i => m.setFeatureIndex(i.getLong.toInt))

      m
    }
  }

  override def sparkLoad(uid: String, shape: NodeShape, model: IsotonicRegressionModel): IsotonicRegressionModel = {
    val oldModel = new regression.IsotonicRegressionModel(boundaries = model.boundaries.toArray,
      predictions = model.predictions.toArray,
      isotonic = model.getIsotonic)
    new IsotonicRegressionModel(uid = uid, oldModel = oldModel).setFeatureIndex(model.getFeatureIndex)
  }

  override def sparkInputs(obj: IsotonicRegressionModel): Seq[ParamSpec] = {
    Seq("features" -> obj.featuresCol)
  }

  override def sparkOutputs(obj: IsotonicRegressionModel): Seq[SimpleParamSpec] = {
    Seq("prediction" -> obj.predictionCol)
  }
}
