package ml.combust.mleap.spark.parity.regression

import ml.combust.mleap.spark.parity.SparkParityBase
import org.apache.spark.ml.feature.{StringIndexer, VectorAssembler}
import org.apache.spark.ml.mleap.feature.OneHotEncoder
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.{Pipeline, Transformer}
import org.apache.spark.sql.DataFrame

/**
  * Created by hollinwilkins on 10/30/16.
  */
class LinearRegressionParitySpec extends SparkParityBase {
  override val dataset: DataFrame = baseDataset.select("fico_score_group_fnl", "dti", "loan_amount")
  override val sparkTransformer: Transformer = new Pipeline().setStages(Array(new StringIndexer().
    setInputCol("fico_score_group_fnl").
    setOutputCol("fico_index"),
    new OneHotEncoder().
      setInputCol("fico_index").
      setOutputCol("fico"),
    new VectorAssembler().
      setInputCols(Array("fico", "dti")).
      setOutputCol("features"),
    new LinearRegression().
      setFeaturesCol("features").
      setLabelCol("loan_amount").
      setPredictionCol("prediction"))).fit(dataset)
}
