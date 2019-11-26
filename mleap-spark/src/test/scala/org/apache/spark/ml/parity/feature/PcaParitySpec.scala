package org.apache.spark.ml.parity.feature

import org.apache.spark.ml.parity.SparkParityBase
import org.apache.spark.ml.feature.{PCA, VectorAssembler}
import org.apache.spark.ml.{Pipeline, Transformer}
import org.apache.spark.sql.DataFrame

/**
  * Created by hollinwilkins on 10/30/16.
  */
class PcaParitySpec extends SparkParityBase {
  override val dataset: DataFrame = baseDataset.select("dti", "loan_amount")
  override val sparkTransformer: Transformer = new Pipeline().setStages(Array(new VectorAssembler().
    setInputCols(Array("dti", "loan_amount")).
    setOutputCol("features"),
    new PCA().
      setInputCol("features").
      setOutputCol("pca_features").
      setK(2))).fit(dataset)

  override val unserializedParams = Set("k")
}
