package org.apache.spark.ml.parity.feature

import org.apache.spark.ml.parity.SparkParityBase
import org.apache.spark.ml.feature.{MinMaxScaler, VectorAssembler}
import org.apache.spark.ml.{Pipeline, Transformer}
import org.apache.spark.sql.DataFrame

/**
  * Created by hollinwilkins on 10/30/16.
  */
class MinMaxScalerParitySpec extends SparkParityBase {
  override val dataset: DataFrame = baseDataset.select("dti", "loan_amount")
  override val sparkTransformer: Transformer = new Pipeline().setStages(Array(new VectorAssembler().
    setInputCols(Array("dti", "loan_amount")).
    setOutputCol("features"),
    new MinMaxScaler().
      setInputCol("features").
      setOutputCol("scaled_features"))).fit(dataset)

  //TODO support min/max in mleap other than the defaults 0.0 and 1.0
  override val unserializedParams = Set("min", "max")
}
