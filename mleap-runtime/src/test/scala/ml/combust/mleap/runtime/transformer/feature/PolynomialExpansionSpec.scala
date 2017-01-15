package ml.combust.mleap.runtime.transformer.feature

import ml.combust.mleap.core.feature.PolynomialExpansionModel
import ml.combust.mleap.runtime.{LeapFrame, LocalDataset, Row}
import ml.combust.mleap.runtime.types.{DoubleType, StructField, StructType, TensorType}
import ml.combust.mleap.tensor.Tensor
import org.scalatest.FunSpec

/**
  * Created by mikhail on 10/16/16.
  */
class PolynomialExpansionSpec extends FunSpec {
  val schema = StructType(Seq(StructField("test_vec", TensorType(DoubleType())))).get
  val dataset = LocalDataset(Seq(Row(Tensor.denseVector(Array(2.0, 3.0)))))
  val frame = LeapFrame(schema, dataset)

  val transformer = PolynomialExpansion(inputCol = "test_vec",
    outputCol = "test_expanded",
    model = PolynomialExpansionModel(2))

  describe("#transform") {
    it("Takes 2+ dimensional vector and runs polynomial expansion") {
      val frame2 = transformer.transform(frame).get
      val data = frame2.dataset.toArray
      val expanded = data(0).getTensor[Double](1)
      val expectedVector = Array(2.0, 4.0, 3.0, 6.0, 9.0)

      assert(expanded.toArray.sameElements(expectedVector))
    }
  }
}
