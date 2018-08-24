package org.apache.spark.ml.parity.regression

import org.apache.spark.ml.feature.{OneHotEncoderEstimator, StringIndexer, VectorAssembler}
import org.apache.spark.ml.{Pipeline, Transformer}
import org.apache.spark.ml.parity.SparkParityBase
import org.apache.spark.ml.regression.AFTSurvivalRegression
import org.apache.spark.sql._
import org.apache.spark.sql.functions.lit

/**
  * Created by hollinwilkins on 12/28/16.
  */
class AFTSurvivalRegressionParitySpec extends SparkParityBase {
  override val dataset: DataFrame = baseDataset.select("fico_score_group_fnl", "dti", "loan_amount").withColumn("censor", lit(1.0))
  override val sparkTransformer: Transformer = new Pipeline().setStages(Array(new StringIndexer().
    setInputCol("fico_score_group_fnl").
    setOutputCol("fico_index"),
    new OneHotEncoderEstimator().
      setInputCols(Array("fico_index")).
      setOutputCols(Array("fico")),
    new VectorAssembler().
      setInputCols(Array("fico", "dti")).
      setOutputCol("features"),
    new AFTSurvivalRegression().
      setFeaturesCol("features").
      setLabelCol("loan_amount").
      setQuantilesCol("quant").
      setPredictionCol("prediction"))).fit(dataset)
}
