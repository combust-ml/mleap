package ml.combust.mleap.runtime.transformer.feature

import ml.combust.mleap.runtime.types.{DoubleType, StringType, StructField}
import org.scalatest.FunSpec

class ReverseStringIndexerSpec extends FunSpec {

  describe("#getFields") {
    it("has the correct inputs and outputs") {
      val transformer = new ReverseStringIndexer("transformer", "input", "output", null)
      assert(transformer.getFields().get ==
        Seq(StructField("input", DoubleType()),
          StructField("output", StringType())))
    }
  }
}