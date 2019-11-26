package org.apache.spark.ml.parity.clustering

import org.apache.spark.ml.clustering.BisectingKMeans
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.apache.spark.ml.{Pipeline, Transformer}
import org.apache.spark.ml.parity.SparkParityBase
import org.apache.spark.sql._

/**
  * Created by hollinwilkins on 12/27/16.
  */
class BisectingKMeansParitySpec extends SparkParityBase {
  override val dataset: DataFrame = baseDataset.select("dti", "loan_amount", "fico_score_group_fnl")
  override val sparkTransformer: Transformer = new Pipeline().setStages(Array(new StringIndexer().
    setInputCol("fico_score_group_fnl").
    setOutputCol("fico_index"),
    new VectorAssembler().
      setInputCols(Array("fico_index", "dti")).
      setOutputCol("features"),
    new BisectingKMeans().
      setFeaturesCol("features").
      setPredictionCol("prediction"))).fit(dataset)

  override val unserializedParams = Set("stringOrderType", "k", "maxIter", "seed", "minDivisibleClusterSize")
}
