package ml.combust.mleap.runtime.transformer.feature

import ml.combust.mleap.core.feature.MathBinaryModel
import ml.combust.mleap.runtime.function.UserDefinedFunction
import ml.combust.mleap.runtime.transformer.Transformer
import ml.combust.mleap.runtime.transformer.builder.TransformBuilder

import scala.util.Try

/**
  * Created by hollinwilkins on 12/27/16.
  */
case class MathBinary(override val uid: String = Transformer.uniqueName("math_binary"),
                      inputA: Option[String] = None,
                      inputB: Option[String] = None,
                      outputCol: String,
                      model: MathBinaryModel) extends Transformer {
  val execA: UserDefinedFunction = (a: Double) => model(Some(a), None)
  val execB: UserDefinedFunction = (b: Double) => model(None, Some(b))
  val execAB: UserDefinedFunction = (a: Double, b: Double) => model(Some(a), Some(b))
  val execNone: UserDefinedFunction = () => model(None, None)

  override def transform[TB <: TransformBuilder[TB]](builder: TB): Try[TB] = {
    (inputA, inputB) match {
      case (Some(a), Some(b)) => builder.withOutput(outputCol, a, b)(execAB)
      case (Some(a), None) => builder.withOutput(outputCol, a)(execA)
      case (None, Some(b)) => builder.withOutput(outputCol, b)(execB)
      case (None, None) => builder.withOutput(outputCol)(execNone)
    }
  }
}
