package ml.combust.mleap.core.feature

import org.apache.spark.ml.linalg.mleap.BLAS
import org.apache.spark.ml.linalg.{Vector, Vectors}

/**
  * Created by hollinwilkins on 12/28/16.
  */
case class WordToVectorModel(wordIndex: Map[String, Int],
                             wordVectors: Array[Double]) {
  val numWords: Int = wordIndex.size
  val vectorSize: Int = wordVectors.length / numWords
  val vectors: Map[String, Vector] = {
    wordIndex.map { case (word, ind) =>
      (word, wordVectors.slice(vectorSize * ind, vectorSize * ind + vectorSize))
    }
  }.mapValues(Vectors.dense).map(identity)

  def apply(sentence: Seq[String]): Vector = {
    val d = vectorSize
    if (sentence.isEmpty) {
      Vectors.sparse(d, Array.empty[Int], Array.empty[Double])
    } else {
      val sum = Vectors.zeros(d)
      sentence.foreach { word =>
        vectors.get(word).foreach { v =>
          BLAS.axpy(1.0, v, sum)
        }
      }
      BLAS.scal(1.0 / sentence.size, sum)
      sum
    }
  }
}
