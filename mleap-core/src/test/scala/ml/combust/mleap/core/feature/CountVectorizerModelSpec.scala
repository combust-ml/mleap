package ml.combust.mleap.core.feature

import ml.combust.mleap.core.types._
import org.scalatest.FunSpec

class CountVectorizerModelSpec extends FunSpec {

  describe("binarizer with one input") {
    val model = new CountVectorizerModel(Array("1", "2", "3"), true, 2)

    it("has the right input schema") {
      assert(model.inputSchema.fields ==
        Seq(StructField("input", ListType(BasicType.String))))
    }

    it("has the right output schema") {
      assert(model.outputSchema.fields ==
        Seq(StructField("output", TensorType.Double(3))))
    }
  }
}