package ml.combust.mleap.runtime.transformer.feature

import ml.combust.mleap.core.feature.{RegexTokenizerModel, StringIndexerModel}
import ml.combust.mleap.runtime.types.{StringType, StructField, StructType}
import ml.combust.mleap.runtime.{LeapFrame, LocalDataset, Row}
import org.scalatest.FunSpec

class RegexTokenizerSpec extends FunSpec {
  val schema = StructType(Seq(StructField("test_string", StringType()))).get
  val dataset = LocalDataset(Seq(Row("dies isT Ein TEST text te")))
  val frame = LeapFrame(schema, dataset)

  val gapRegexTokenizer = RegexTokenizer(
    inputCol = "test_string",
    outputCol = "test_tokens",
    model = RegexTokenizerModel(
      regex = """\s""".r,
      matchGaps = true,
      tokenMinLength = 3,
      lowercaseText = true
    )
  )

  val wordRegexTokenizer = RegexTokenizer(
    inputCol = "test_string",
    outputCol = "test_tokens",
    model = RegexTokenizerModel(
      regex = """\w+""".r,
      matchGaps = false,
      tokenMinLength = 4,
      lowercaseText = false
    )
  )

  describe("#transform") {
    it("converts input string into tokens by matching the gap") {
      val frame2 = gapRegexTokenizer.transform(frame).get
      val data = frame2.dataset.toArray

      assert(data(0).getSeq(1) == Seq("dies", "ist", "ein", "test", "text"))
    }

    it("converts input string into tokens by matching the words") {
      val frame2 = wordRegexTokenizer.transform(frame).get
      val data = frame2.dataset.toArray

      assert(data(0).getSeq(1) == Seq("dies", "TEST", "text"))
    }
  }
}
