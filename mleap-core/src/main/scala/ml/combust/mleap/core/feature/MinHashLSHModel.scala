package ml.combust.mleap.core.feature
import ml.combust.mleap.core.types.{StructType, TensorType}
import ml.combust.mleap.tensor.{DenseTensor, Tensor}
import org.apache.spark.ml.linalg.{Vector, Vectors}

/**
  * Created by hollinwilkins on 12/28/16.
  */
object MinHashLSHModel {
  val HASH_PRIME = 2038074743
}

case class MinHashLSHModel(randomCoefficients: Seq[(Int, Int)]) extends LSHModel{
  def apply(features: Vector): Tensor[Double] = predict(features)

  def predict(features: Vector): Tensor[Double] = {
    require(features.numNonzeros > 0, "Must have at least 1 non zero entry.")
    val elemsList = features.toSparse.indices.toList
    val hashValues = randomCoefficients.map { case (a, b) =>
      elemsList.map { elem: Int =>
        ((1 + elem) * a + b) % MinHashLSHModel.HASH_PRIME
      }.min.toDouble
    }

    // TODO: Output vectors of dimension numHashFunctions in SPARK-18450
    DenseTensor(hashValues.toArray, Seq(hashValues.length, 1))
  }

  override def keyDistance(x: Vector, y: Vector): Double = {
    val xSet = x.toSparse.indices.toSet
    val ySet = y.toSparse.indices.toSet
    val intersectionSize = xSet.intersect(ySet).size.toDouble
    val unionSize = xSet.size + ySet.size - intersectionSize
    assert(unionSize > 0, "The union of two input sets must have at least 1 elements")
    1 - intersectionSize / unionSize
  }

  override def hashDistance(x: Seq[Vector], y: Seq[Vector]): Double = {
    // Since it's generated by hashing, it will be a pair of dense vectors.
    // TODO: This hashDistance function requires more discussion in SPARK-18454
    x.zip(y).map(vectorPair =>
      vectorPair._1.toArray.zip(vectorPair._2.toArray).count(pair => pair._1 != pair._2)
    ).min
  }

  override def inputSchema: StructType = StructType("input" -> TensorType.Double()).get

  override def outputSchema: StructType = StructType("output" -> TensorType.Double()).get
}
