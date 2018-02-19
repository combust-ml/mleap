package ml.combust.bundle.serializer

import java.net.URI

import ml.bundle.Attributes
import ml.combust.bundle.dsl.Bundle
import ml.combust.bundle.test.TestSupport._
import ml.combust.bundle.test._
import ml.combust.bundle.test.ops._
import ml.combust.bundle.{BundleFile, BundleRegistry, TestUtil, dsl}
import org.scalatest.FunSpec
import resource._

import scala.util.Random

/**
  * Created by hollinwilkins on 8/21/16.
  */
sealed trait FSType
case object ZipFS extends FSType
case object DirFS extends FSType

class BundleSerializationSpec extends FunSpec {
  implicit val testContext = TestContext(BundleRegistry("test-registry"))

  it should behave like bundleSerializer("Serializing/Deserializing json a bundle as a dir",
    SerializationFormat.Json,
    DirFS)
  it should behave like bundleSerializer("Serializing/Deserializing json a bundle as a zip",
    SerializationFormat.Json,
    ZipFS)

  it should behave like bundleSerializer("Serializing/Deserializing proto a bundle as a dir",
    SerializationFormat.Protobuf,
    DirFS)
  it should behave like bundleSerializer("Serializing/Deserializing proto a bundle as a zip",
    SerializationFormat.Protobuf,
    DirFS)

  def bundleSerializer(description: String, format: SerializationFormat, fsType: FSType) = {
    val (prefix, suffix) = fsType match {
      case DirFS => ("file", "")
      case ZipFS => ("jar:file", ".zip")
    }

    describe(description) {
      val randomCoefficients = (0 to 100000).map(v => Random.nextDouble())
      val lr = LinearRegression(uid = "linear_regression_example",
        input = "input_field",
        output = "output_field",
        model = LinearModel(coefficients = randomCoefficients,
          intercept = 44.5))
      val si = StringIndexer(uid = "string_indexer_example",
        input = "input_string",
        output = "output_index",
        model = StringIndexerModel(strings = Seq("hey", "there", "man")))
      val pipeline = Pipeline(uid = "my_pipeline", PipelineModel(Seq(si, lr)))

      describe("with a simple linear regression") {
        it("serializes/deserializes the same object") {
          val uri = new URI(s"$prefix:${TestUtil.baseDir}/lr_bundle.$format$suffix")
          for(file <- managed(BundleFile(uri))) {
            lr.writeBundle.name("my_bundle").
              format(format).
              save(file)

            val bundleRead = file.loadBundle().get

            assert(lr == bundleRead.root)
          }
        }
      }

      describe("with a decision tree") {
        it("serializes/deserializes the same object") {
          val node = InternalNode(CategoricalSplit(1, isLeft = true, 5, Seq(1.0, 3.0)),
            InternalNode(ContinuousSplit(2, 0.4), LeafNode(Seq(5.0)), LeafNode(Seq(4.0))),
            LeafNode(Seq(0.4, 5.6, 3.2, 5.7, 5.5)))
          val dt = DecisionTreeRegression(uid = "my_decision_tree",
            input = "my_input",
            output = "my_output",
            model = DecisionTreeRegressionModel(node))

          val uri = new URI(s"$prefix:${TestUtil.baseDir}/dt_bundle.$format$suffix")
          for(file <- managed(BundleFile(uri))) {
            dt.writeBundle.name("my_bundle").
              format(format).
              save(file)

            val bundleRead = file.loadBundle().get

            assert(dt == bundleRead.root)
          }
        }
      }

      describe("with a pipeline") {
        it("serializes/deserializes the same object") {
          val uri = new URI(s"$prefix:${TestUtil.baseDir}/pipeline_bundle.$format$suffix")
          for(file <- managed(BundleFile(uri))) {
            pipeline.writeBundle.name("my_bundle").
              format(format).
              save(file)

            val bundleRead: Bundle[Transformer] = file.loadBundle().get

            assert(pipeline == bundleRead.root)
          }
        }
      }

      describe("with a backslash'd path for a linear regression") {
        it("serializes and deserializes from a path containing '\\' separators") {
          val uri = s"$prefix:${TestUtil.baseDir}/lr_bundle.$format$suffix".replace('/', '\\')
          for(file <- managed(BundleFile(uri))) {
            lr.writeBundle.name("my_bundle").
              format(format).
              save(file)

            val bundleRead = file.loadBundle().get

            assert(lr == bundleRead.root)
          }
        }
      }

      describe("with metadata") {
        it("serializes and deserializes any metadata we want to send along with the bundle") {
          val uri = new URI(s"$prefix:${TestUtil.baseDir}/lr_bundle_meta.$format$suffix")
          val meta = new Attributes().withList(Map(
            "keyA" → dsl.Value.string("valA").value,
            "keyB" → dsl.Value.double(1.2).value,
            "keyC" → dsl.Value.stringList(Seq("listValA", "listValB")).value
          ))
          for(file <- managed(BundleFile(uri))) {
            lr.writeBundle.name("my_bundle").
              format(format).
              meta(meta).
              save(file)

            val bundleRead = file.loadBundle().get

            assert(bundleRead.info.meta.contains(meta))
          }
        }
      }
    }
  }
}
