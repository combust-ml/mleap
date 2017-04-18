package ml.combust.mleap.tensor

import org.scalatest.FunSpec

class TensorSpec extends FunSpec {

  describe("DenseTensor") {
    it("should return false for dense tensors with different base") {
      val tensor1 = Tensor.denseVector(Array(20, 10, 5))
      val tensor2 = Tensor.denseVector(Array(20.0, 10.0, 5.0))
      assert(tensor1 != tensor2)
    }

    it("should return true for empty dense tensors") {
      val tensor1 = Tensor.denseVector(Array())
      val tensor2 = Tensor.denseVector(Array())
      assert(tensor1 == tensor2)
    }

    it("should return true for dense tensors with same elements and dimensions") {
      val tensor1 = Tensor.denseVector(Array(20.0, 10.0, 5.0))
      val tensor2 = Tensor.denseVector(Array(20.0, 10.0, 5.0))
      assert(tensor1 == tensor2)
    }

    it("should return false for dense tensors with different dimensions") {
      val tensor1 = Tensor.denseVector(Array(20.0, 10.0, 5.0, 23.0))
      val tensor2 = Tensor.denseVector(Array(20.0, 10.0, 5.0))
      assert(tensor1 != tensor2)
    }

    it("should return false for dense tensors with same dimension but different elements") {
      val tensor1 = Tensor.denseVector(Array(20.0, 11.0, 5.0))
      val tensor2 = Tensor.denseVector(Array(20.0, 10.0, 5.0))
      assert(tensor1 != tensor2)
    }

    it("should return true for dense tensors with dimension -1 and same elements") {
      val tensor1 = DenseTensor(Array(2.0, 5.0, 34.0), Seq(-1))
      val tensor2 = DenseTensor(Array(2.0, 5.0, 34.0), Seq(-1))
      assert(tensor1 == tensor2)
    }

    it("should return false for dense tensors with dimension -1 but different elements") {
      val tensor1 = DenseTensor(Array(2.0, 1.0, 34.0), Seq(-1))
      val tensor2 = DenseTensor(Array(2.0, 5.0, 34.0), Seq(-1))
      assert(tensor1 != tensor2)
    }
  }

  describe("SparseTensor") {
    it("should return false for sparse tensors with different base") {

      val tensor1 = Tensor.create(Array(22.3, 45.6, 99.3), Seq(5), Some(Seq(Seq(1, 2, 4))))
      val tensor2 = Tensor.create(Array(22, 45, 99), Seq(5), Some(Seq(Seq(1, 2, 4))))
      assert(tensor1 != tensor2)
    }

    it("should return true for empty sparse tensors") {
      val tensor1 = Tensor.create(Array(), Seq(), Some(Seq(Seq())))
      val tensor2 = Tensor.create(Array(), Seq(), Some(Seq(Seq())))
      assert(tensor1 == tensor2)
    }

    it("should return true for sparse tensors with same elements and dimensions") {
      val tensor1 = Tensor.create(Array(22.3, 45.6, 99.3), Seq(5), Some(Seq(Seq(1, 2, 4))))
      val tensor2 = Tensor.create(Array(22.3, 45.6, 99.3), Seq(5), Some(Seq(Seq(1, 2, 4))))
      assert(tensor1 == tensor2)
    }

    it("should return false for sparse tensors with different dimensions") {
      val tensor1 = Tensor.create(Array(22.3, 45.6, 99.3), Seq(4), Some(Seq(Seq(1, 2, 4))))
      val tensor2 = Tensor.create(Array(22.3, 45.6, 99.3), Seq(5), Some(Seq(Seq(1, 2, 4))))
      assert(tensor1 != tensor2)
    }

    it("should return false for sparse tensors with same dimension but different elements") {
      val tensor1 = Tensor.create(Array(22.3, 45.6, 99.3), Seq(4), Some(Seq(Seq(1, 2, 4))))
      val tensor2 = Tensor.create(Array(22.3, 95.6, 99.3), Seq(4), Some(Seq(Seq(1, 2, 4))))
      assert(tensor1 != tensor2)
    }

    it("should return true for sparse tensors with dimension -1 and same elements") {
      val tensor1 = Tensor.create(Array(22.3, 45.6, 99.3), Seq(-1), Some(Seq(Seq(1, 2, 4))))
      val tensor2 = Tensor.create(Array(22.3, 45.6, 99.3), Seq(-1), Some(Seq(Seq(1, 2, 4))))
      assert(tensor1 == tensor2)
    }

    it("should return false for sparse tensors with dimension -1 but different elements") {
      val tensor1 = Tensor.create(Array(22.3, 45.6, 99.3), Seq(-1), Some(Seq(Seq(1, 2, 4))))
      val tensor2 = Tensor.create(Array(22.3, 95.6, 99.3), Seq(-1), Some(Seq(Seq(1, 2, 4))))
      assert(tensor1 != tensor2)
    }
  }
}
