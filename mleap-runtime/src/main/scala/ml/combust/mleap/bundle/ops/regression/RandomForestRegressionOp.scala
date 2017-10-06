package ml.combust.mleap.bundle.ops.regression

import ml.combust.bundle.BundleContext
import ml.combust.mleap.core.regression.{DecisionTreeRegressionModel, RandomForestRegressionModel}
import ml.combust.mleap.runtime.transformer.regression.RandomForestRegression
import ml.combust.bundle.op.OpModel
import ml.combust.bundle.serializer.ModelSerializer
import ml.combust.bundle.dsl._
import ml.combust.mleap.bundle.ops.MleapOp
import ml.combust.mleap.bundle.tree.decision.MleapNodeWrapper
import ml.combust.mleap.runtime.MleapContext

/**
  * Created by hollinwilkins on 8/22/16.
  */
class RandomForestRegressionOp extends MleapOp[RandomForestRegression, RandomForestRegressionModel] {
  implicit val nodeWrapper = MleapNodeWrapper

  override val Model: OpModel[MleapContext, RandomForestRegressionModel] = new OpModel[MleapContext, RandomForestRegressionModel] {
    override val klazz: Class[RandomForestRegressionModel] = classOf[RandomForestRegressionModel]

    override def opName: String = Bundle.BuiltinOps.regression.random_forest_regression

    override def store(model: Model, obj: RandomForestRegressionModel)
                      (implicit context: BundleContext[MleapContext]): Model = {
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
                     (implicit context: BundleContext[MleapContext]): RandomForestRegressionModel = {
      val numFeatures = model.value("num_features").getLong.toInt
      val treeWeights = model.value("tree_weights").getDoubleList

      val models = model.value("trees").getStringList.map {
        tree => ModelSerializer(context.bundleContext(tree)).read().get.asInstanceOf[DecisionTreeRegressionModel]
      }

      RandomForestRegressionModel(numFeatures = numFeatures,
        treeWeights = treeWeights,
        trees = models)
    }
  }

  override def model(node: RandomForestRegression): RandomForestRegressionModel = node.model
}
