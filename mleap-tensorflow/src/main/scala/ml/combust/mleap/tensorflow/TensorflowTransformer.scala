package ml.combust.mleap.tensorflow

import ml.bundle.Socket.Socket
import ml.combust.mleap.runtime.function.UserDefinedFunction
import ml.combust.mleap.runtime.transformer.Transformer
import ml.combust.mleap.runtime.transformer.builder.TransformBuilder

import scala.util.Try

/**
  * Created by hollinwilkins on 1/12/17.
  */
case class TensorflowTransformer(override val uid: String = Transformer.uniqueName("tensorflow"),
                                 inputs: Seq[Socket],
                                 outputs: Seq[Socket],
                                 rawOutputCol: Option[String] = None,
                                 model: TensorflowModel) extends Transformer {
  val inputCols: Array[String] = inputs.map(_.name).toArray
  val outputCols: Seq[String] = outputs.map(_.name)

  val actualRawCol = rawOutputCol.getOrElse(uid)
  val exec: UserDefinedFunction = (tensors: Seq[Any]) => model(tensors: _*)
  val outputUdfs: Seq[UserDefinedFunction] = outputCols.zipWithIndex.map {
    case (output, index) =>
      val udf: UserDefinedFunction = (raw: Seq[Any]) => raw(index)
      udf.copy(returnTypes = model.outputs(index)._2)
  }
  private val outputsWithUdfs = outputCols.zip(outputUdfs)

  override def transform[TB <: TransformBuilder[TB]](builder: TB): Try[TB] = {
    val builder2 = builder.withOutput(actualRawCol, inputCols)(exec)
    outputsWithUdfs.foldLeft(builder2) {
      case (b, (name, udf)) => b.flatMap(_.withOutput(name, actualRawCol)(udf))
    }
  }

  override def close(): Unit = { model.close() }
}
