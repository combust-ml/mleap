package org.apache.spark.ml.bundle.ops.regression

import ml.combust.bundle.BundleContext
import ml.combust.bundle.dsl._
import ml.combust.bundle.op.{OpModel, OpNode}
import ml.combust.bundle.serializer.ModelSerializer
import org.apache.spark.ml.bundle.{ParamSpec, SimpleParamSpec, SimpleSparkOp, SparkBundleContext}
import org.apache.spark.ml.param.Param
import org.apache.spark.ml.regression.{DecisionTreeRegressionModel, GBTRegressionModel}

/**
  * Created by hollinwilkins on 9/24/16.
  */
class GBTRegressionOp extends SimpleSparkOp[GBTRegressionModel] {
  override val Model: OpModel[SparkBundleContext, GBTRegressionModel] = new OpModel[SparkBundleContext, GBTRegressionModel] {
    override val klazz: Class[GBTRegressionModel] = classOf[GBTRegressionModel]

    override def opName: String = Bundle.BuiltinOps.regression.gbt_regression

    override def store(model: Model, obj: GBTRegressionModel)
                      (implicit context: BundleContext[SparkBundleContext]): Model = {
      var i = 0
      val trees = obj.trees.map {
        tree =>
          val name = s"tree$i"
          ModelSerializer(context.bundleContext(name)).write(tree).get
          i = i + 1
          name
      }
      model.withValue("num_features", Value.long(obj.numFeatures)).
        withValue("tree_weights", Value.doubleList(obj.treeWeights)).
        withValue("trees", Value.stringList(trees))
    }

    override def load(model: Model)
                     (implicit context: BundleContext[SparkBundleContext]): GBTRegressionModel = {
      val numFeatures = model.value("num_features").getLong.toInt
      val treeWeights = model.value("tree_weights").getDoubleList.toArray

      val models = model.value("trees").getStringList.map {
        tree => ModelSerializer(context.bundleContext(tree)).read().get.asInstanceOf[DecisionTreeRegressionModel]
      }.toArray

      new GBTRegressionModel(uid = "",
        _trees = models,
        _treeWeights = treeWeights,
        numFeatures = numFeatures)
    }
  }

  override def sparkLoad(uid: String, shape: NodeShape, model: GBTRegressionModel): GBTRegressionModel = {
    new GBTRegressionModel(uid = uid,
      _trees = model.trees,
      _treeWeights = model.treeWeights,
      numFeatures = model.numFeatures)
  }

  override def sparkInputs(obj: GBTRegressionModel): Seq[ParamSpec] = {
    Seq("features" -> obj.featuresCol)
  }

  override def sparkOutputs(obj: GBTRegressionModel): Seq[SimpleParamSpec] = {
    Seq("prediction" -> obj.predictionCol)
  }
}
