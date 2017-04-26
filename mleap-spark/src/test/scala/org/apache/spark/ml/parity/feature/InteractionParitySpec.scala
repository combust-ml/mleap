package org.apache.spark.ml.parity.feature

import org.apache.spark.ml.feature.{Interaction, VectorAssembler}
import org.apache.spark.ml.{Pipeline, Transformer}
import org.apache.spark.ml.parity.SparkParityBase
import org.apache.spark.sql.DataFrame

/**
  * Created by hollinwilkins on 4/26/17.
  */
class InteractionParitySpec extends SparkParityBase {
  override val dataset: DataFrame = baseDataset.select("dti", "loan_amount")
  override val sparkTransformer: Transformer = new Pipeline().setStages(Array(new VectorAssembler().
    setInputCols(Array("dti", "loan_amount")).
    setOutputCol("features"),
    new Interaction().setInputCols(Array("dti", "features")).setOutputCol("interacted"))).fit(dataset)
}
