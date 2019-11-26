package org.apache.spark.ml.mleap.feature

import ml.combust.mleap.core.annotation.SparkCode
import org.apache.hadoop.fs.Path

import org.apache.spark.SparkException
import org.apache.spark.annotation.{Experimental, Since}
import org.apache.spark.ml.{Estimator, Model}
import org.apache.spark.ml.param._
import org.apache.spark.ml.param.shared.{HasInputCol, HasOutputCol}
import org.apache.spark.ml.util._
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.sql.{DataFrame, Dataset}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql._

/**
  * Params for [[Imputer]] and [[ImputerModel]].
  */
@SparkCode(uri = "https://github.com/hhbyyh/spark/blob/imputer/mllib/src/main/scala/org/apache/spark/ml/feature/Imputer.scala")
private[feature] trait ImputerParams extends Params with HasInputCol with HasOutputCol {

  /**
    * The imputation strategy.
    * If "mean", then replace missing values using the mean value of the feature.
    * If "median", then replace missing values using the approximate median value of the feature.
    * Default: mean
    *
    * @group param
    */
  val strategy: Param[String] = new Param(this, "strategy", "strategy for imputation. " +
    "If mean, then replace missing values using the mean value of the feature. " +
    "If median, then replace missing values using the median value of the feature.",
    ParamValidators.inArray[String](Imputer.supportedStrategyNames.toArray))

  /** @group getParam */
  def getStrategy: String = $(strategy)

  /**
    * The placeholder for the missing values. All occurrences of missingValue will be imputed.
    * Note that null values are always treated as missing.
    * Default: Double.NaN
    *
    * @group param
    */
  val missingValue: DoubleParam = new DoubleParam(this, "missingValue",
    "The placeholder for the missing values. All occurrences of missingValue will be imputed")
  setDefault(missingValue, Double.NaN)

  /** @group getParam */
  def getMissingValue: Double = $(missingValue)

  /** Validates and transforms the input schema. */
  protected def validateAndTransformSchema(schema: StructType): StructType = {
    val inputType = schema($(inputCol)).dataType
    SchemaUtils.checkColumnTypes(schema, $(inputCol), Seq(DoubleType, FloatType))
    SchemaUtils.appendColumn(schema, $(outputCol), inputType)
  }
}

/**
  * :: Experimental ::
  * Imputation estimator for completing missing values, either using the mean or the median
  * of the column in which the missing values are located. The input column should be of
  * DoubleType or FloatType. Currently Imputer does not support categorical features yet
  * (SPARK-15041) and possibly creates incorrect values for a categorical feature.
  *
  * Note that the mean/median value is computed after filtering out missing values.
  * All Null values in the input column are treated as missing, and so are also imputed.
  */
@Experimental
class Imputer @Since("2.1.0")(override val uid: String)
  extends Estimator[ImputerModel] with ImputerParams with DefaultParamsWritable {

  @Since("2.1.0")
  def this() = this(Identifiable.randomUID("imputer"))

  /** @group setParam */
  @Since("2.1.0")
  def setInputCol(value: String): this.type = set(inputCol, value)

  /** @group setParam */
  @Since("2.1.0")
  def setOutputCol(value: String): this.type = set(outputCol, value)

  @Since("2.1.0")
  val imputeValue: DoubleParam =
    new DoubleParam(this, "imputeValue", "Value which will be used to impute missing values.")

  /**
    * Imputation strategy. Available options are ["mean", "median"].
    *
    * @group setParam
    */
  @Since("2.1.0")
  def setStrategy(value: String): this.type = set(strategy, value)

  /** @group setParam */
  @Since("2.1.0")
  def setMissingValue(value: Double): this.type = set(missingValue, value)

  setDefault(strategy -> "mean", missingValue -> Double.NaN)

  @Since("2.1.0")
  def getImputeValue: Double = $(imputeValue)

  override def fit(dataset: Dataset[_]): ImputerModel = {
    transformSchema(dataset.schema, logging = true)
    val inputColumn = col($(inputCol))

    val filtered = dataset.select(inputColumn.cast(DoubleType))
      .filter(inputColumn.isNotNull && inputColumn =!= $(missingValue))
      .filter(!inputColumn.isNaN)

    if(filtered.count() == 0) {
      throw new SparkException(s"surrogate cannot be computed. " +
        s"All the values in ${$(inputCol)} are Null, Nan or missingValue ($missingValue)")
    }

    val surrogate = $(strategy) match {
      case "mean" => filtered.select(avg($(inputCol))).first().getDouble(0)
      case "median" => filtered.stat.approxQuantile($(inputCol), Array(0.5), 0.001)(0)
    }

    copyValues(new ImputerModel(uid, surrogate).setParent(this))

  }

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }

  override def copy(extra: ParamMap): Imputer = defaultCopy(extra)
}

@Since("2.1.0")
object Imputer extends DefaultParamsReadable[Imputer] {

  /** Set of strategy names that Imputer currently supports. */
  private[ml] val supportedStrategyNames = Set("mean", "median")

  @Since("2.1.0")
  override def load(path: String): Imputer = super.load(path)
}

/**
  * :: Experimental ::
  * Model fitted by [[Imputer]].
  *
  * @param surrogateValue value to replace missing values with
  */
@Experimental
class ImputerModel private[ml](override val uid: String,
                               val surrogateValue: Double)
  extends Model[ImputerModel] with ImputerParams with MLWritable {

  import ImputerModel._

  /** @group setParam */
  def setInputCol(value: String): this.type = set(inputCol, value)

  /** @group setParam */
  def setOutputCol(value: String): this.type = set(outputCol, value)

  /** @group setParam */
  @Since("2.1.0")
  def setMissingValue(value: Double): this.type = set(missingValue, value)

  /** @group setParam */
  @Since("2.1.0")
  def setStrategy(value: String): this.type = set(strategy, value)

  override def transform(dataset: Dataset[_]): DataFrame = {
    transformSchema(dataset.schema, logging = true)
    val inputType = dataset.schema($(inputCol)).dataType
    val ic = col($(inputCol))

    dataset.withColumn($(outputCol), when(ic.isNull, surrogateValue)
      .when(ic === $(missingValue), surrogateValue)
      .otherwise(ic)
      .cast(inputType))
  }

  override def transformSchema(schema: StructType): StructType = {
    validateAndTransformSchema(schema)
  }

  override def copy(extra: ParamMap): ImputerModel = {
    copyValues(new ImputerModel(uid, surrogateValue), extra).setParent(parent)
  }

  @Since("2.1.0")
  override def write: MLWriter = new ImputerModelWriter(this)
}


@Since("2.1.0")
object ImputerModel extends MLReadable[ImputerModel] {

  private[ImputerModel] class ImputerModelWriter(instance: ImputerModel) extends MLWriter {

    private case class Data(imputeValue: Double, missingValue: Double, strategy: String)

    override protected def saveImpl(path: String): Unit = {
      DefaultParamsWriter.saveMetadata(instance, path, sc)
      val data = Data(instance.surrogateValue, instance.getMissingValue, instance.getStrategy)
      val dataPath = new Path(path, "data").toString
      sparkSession.createDataFrame(Seq(data)).repartition(1).write.parquet(dataPath)
    }
  }

  private class ImputerReader extends MLReader[ImputerModel] {

    private val className = classOf[ImputerModel].getName

    override def load(path: String): ImputerModel = {
      val metadata = DefaultParamsReader.loadMetadata(path, sc, className)
      val dataPath = new Path(path, "data").toString
      val data = sparkSession.read.parquet(dataPath)

      val Row(surrogateValue: Double, missingValue: Double, strategy: String) = MLUtils.convertVectorColumnsToML(data, "imputeValue", "missingValue", "strategy")
        .select("surrogateValue", "missingValue", "strategy")
        .head()

      val model = new ImputerModel(metadata.uid, surrogateValue).
        setMissingValue(missingValue).
        setStrategy(strategy)
      metadata.getAndSetParams(model)
      model
    }
  }

  @Since("2.1.0")
  override def read: MLReader[ImputerModel] = new ImputerReader

  @Since("2.1.0")
  override def load(path: String): ImputerModel = super.load(path)
}