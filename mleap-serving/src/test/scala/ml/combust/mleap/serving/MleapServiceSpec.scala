package ml.combust.mleap.serving

import java.nio.file.NoSuchFileException

import ml.combust.mleap.serving.domain.v1._
import org.scalatest.{AsyncFunSpec, Matchers}

import scala.concurrent.Future
import scala.util.Failure

class MleapServiceSpec extends AsyncFunSpec with Matchers {

  val service = new MleapService()

  describe("MleapService") {
    it("loads the model successfully") {
      val bundlePath = TestUtil.serializeModelInJsonFormatToZipFile
      val modelLoaded = service.loadModel(LoadModelRequest(Some(bundlePath)))
      modelLoaded map { response => response shouldBe a [LoadModelResponse] }
    }

    it("throws NoSuchFileException when it cannot find model to load") {
      val modelLoaded = recoverToExceptionIf[NoSuchFileException] {
        service.loadModel(LoadModelRequest(Some("test/unknown_bundle.json.zip")))
      }
      modelLoaded map { ex => ex shouldBe a [NoSuchFileException] }
    }

    it("throws NoSuchElementException when not provided with model to load") {
      val modelLoaded = recoverToExceptionIf[NoSuchElementException] {
        service.loadModel(LoadModelRequest(None))
      }
      modelLoaded map { ex => ex shouldBe a [NoSuchElementException] }
    }

    it("unloads previously loaded model successfully") {
      val bundlePath = TestUtil.serializeModelInJsonFormatToZipFile
      val modelUnloaded: Future[UnloadModelResponse] =
        for {
          _ <- service.loadModel(LoadModelRequest(Some(bundlePath)))
          unloadModel <- service.unloadModel(UnloadModelRequest())
        } yield unloadModel

      modelUnloaded map { response => response shouldBe a [UnloadModelResponse] }
    }

    it("does not fail when trying to unload the model if no model previously loaded") {
      val modelUnloaded = service.unloadModel(UnloadModelRequest())
      modelUnloaded map { response => response shouldBe a [UnloadModelResponse] }
    }

    it("returns a failure if no model has been loaded when transform request is received") {
      val result = service.transform(TestUtil.getLeapFrame)
      assert(result.isFailure)
      result match {
        case Failure(error) =>
          assert(error.isInstanceOf[IllegalStateException])
          assert(error.getMessage == "no transformer loaded")
        case _ => fail("Expected a failure to be returned")
      }
    }

    it("transforms a leap frame successfully") {
      val bundlePath = TestUtil.serializeModelInJsonFormatToZipFile
      val modelLoaded = service.loadModel(LoadModelRequest(Some(bundlePath)))
      modelLoaded.map(response => {
        response shouldBe a [LoadModelResponse]

        val result = service.transform(TestUtil.getLeapFrame)
        val data = result.get.dataset.toArray
        assert(data(0).getDouble(4) == 24.0)
        assert(data(1).getDouble(4) == 19.0)
        assert(data(2).getDouble(4) == 23.0)
      })
    }
  }
}
