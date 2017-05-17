package ml.combust.mleap.runtime.transformer.feature

import ml.combust.mleap.core.feature.MathUnaryModel
import ml.combust.mleap.core.feature.UnaryOperation.Sin
import ml.combust.mleap.runtime.{LeapFrame, LocalDataset, Row}
import ml.combust.mleap.runtime.types._
import org.scalatest.FunSpec

/**
  * Created by hollinwilkins on 12/27/16.
  */
class MathUnarySpec extends FunSpec {
  val schema = StructType(StructField("test_a", DoubleType())).get
  val dataset = LocalDataset(Seq(Row(42.0)))
  val frame = LeapFrame(schema, dataset)

  val transformer = MathUnary(inputCol = "test_a",
    outputCol = "test_out",
    model = MathUnaryModel(Sin))

  describe("#transform") {
    it("transforms the leap frame using the given input and operation") {
      val calc = transformer.transform(frame).get.dataset(0).getDouble(1)
      assert(calc == Math.sin(42.0))
    }
  }

  describe("#getFields") {
    it("has the correct inputs and outputs") {
      assert(transformer.getFields().get ==
        Seq(StructField("test_a", DoubleType()),
          StructField("test_out", DoubleType())))
    }
  }
}