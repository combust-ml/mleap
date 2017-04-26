package ml.combust.mleap.bundle.ops.feature

import ml.combust.bundle.BundleContext
import ml.combust.bundle.dsl._
import ml.combust.bundle.op.{OpModel, OpNode}
import ml.combust.mleap.core.feature.InteractionModel
import ml.combust.mleap.runtime.MleapContext
import ml.combust.mleap.runtime.transformer.feature.Interaction

/**
  * Created by hollinwilkins on 4/26/17.
  */
class InteractionOp extends OpNode[MleapContext, Interaction, InteractionModel] {
  override val Model: OpModel[MleapContext, InteractionModel] = new OpModel[MleapContext, InteractionModel] {
    override val klazz: Class[InteractionModel] = classOf[InteractionModel]

    override def opName: String = Bundle.BuiltinOps.feature.interaction

    override def store(model: Model, obj: InteractionModel)
                      (implicit context: BundleContext[MleapContext]): Model = {
      val m = model.withAttr("num_inputs", Value.int(obj.featuresSpec.length))
      obj.featuresSpec.zipWithIndex.foldLeft(m) {
        case (m2, (numFeatures, index)) => m2.withAttr(s"num_features$index", Value.intList(numFeatures))
      }
    }

    override def load(model: Model)
                     (implicit context: BundleContext[MleapContext]): InteractionModel = {
      val numInputs = model.value("num_inputs").getInt
      val spec = (0 until numInputs).map {
        index => model.value(s"num_features$index").getIntList.toArray
      }.toArray

      InteractionModel(spec)
    }
  }

  override val klazz: Class[Interaction] = classOf[Interaction]

  override def name(node: Interaction): String = node.uid

  override def model(node: Interaction): InteractionModel = node.model

  override def load(node: Node, model: InteractionModel)
                   (implicit context: BundleContext[MleapContext]): Interaction = {
    Interaction(uid = node.name,
      inputCols = node.shape.inputs.map(_.name).toArray,
      outputCol = node.shape.standardOutput.name,
      model = model)
  }

  override def shape(node: Interaction): Shape = {
    val s = Shape().withStandardOutput(node.outputCol)
    node.inputCols.zipWithIndex.foldLeft(s) {
      case (s2, (input, index)) => s2.withInput(input, s"input$index")
    }
  }
}

