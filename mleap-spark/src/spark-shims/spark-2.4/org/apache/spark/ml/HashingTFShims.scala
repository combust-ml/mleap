package org.apache.spark.ml

import org.apache.spark.ml.feature.HashingTF

object HashingTFShims {

  val runtimeVersion = 1

  def createHashingTF(uid: String, numFeatures: Int, binary: Boolean, version: Int): HashingTF = {
    require(version <= runtimeVersion, "Spark cannot load HashingTF transformer saved by higher version spark.")
    new HashingTF(uid = uid).setNumFeatures(numFeatures).setBinary(binary)
  }

}