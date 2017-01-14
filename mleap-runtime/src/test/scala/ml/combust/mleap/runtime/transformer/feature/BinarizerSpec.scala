package ml.combust.mleap.runtime.transformer.feature

import ml.combust.mleap.core.Tensor
import ml.combust.mleap.core.feature.BinarizerModel
import ml.combust.mleap.runtime.{LeapFrame, LocalDataset, Row}
import ml.combust.mleap.runtime.types.{DoubleType, StructField, StructType, TensorType}
import org.scalatest.FunSpec

/**
  * Created by fshabbir on 12/1/16.
  */
class BinarizerSpec extends FunSpec {
  val schema = StructType(Seq(StructField("test_vec", TensorType(DoubleType())))).get
  val dataset = LocalDataset(Seq(Row(Tensor.denseVector(Array(0.1, 0.6, 0.7)))))
  val frame = LeapFrame(schema, dataset)

  val binarizer = Binarizer(inputCol = "test_vec",
    outputCol = "test_binarizer",
    model = BinarizerModel(0.6))

  describe("#transform") {
    it("thresholds the input column to 0 or 1") {
      val frame2 = binarizer.transform(frame).get
      val data = frame2.dataset(0).getTensor[Double](1)

      assert(data(0) == 0.0)
      assert(data(1) == 0.0)
      assert(data(2) == 1.0)
    }
  }
}
