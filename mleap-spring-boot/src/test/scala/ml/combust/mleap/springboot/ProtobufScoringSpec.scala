package ml.combust.mleap.springboot

import java.net.URI

import com.google.protobuf.ByteString
import ml.combust.mleap.pb._
import ml.combust.mleap.runtime.frame.DefaultLeapFrame
import ml.combust.mleap.runtime.serialization.{BuiltinFormats, FrameWriter}
import ml.combust.mleap.springboot.TestUtil.validFrame
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.{HttpEntity, HttpHeaders, ResponseEntity}
import org.springframework.test.context.junit4.SpringRunner

@RunWith(classOf[SpringRunner])
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProtobufScoringSpec extends ScoringBase[Mleap.LoadModelRequest, Mleap.Model, Mleap.BundleMeta, Mleap.TransformFrameRequest, Mleap.TransformFrameResponse] {

  override def createLoadModelRequest(modelName: String, uri: URI, createTmpFile: Boolean): HttpEntity[Mleap.LoadModelRequest] = {
    val request = LoadModelRequest(modelName = modelName,
      uri = TestUtil.getBundle(uri, createTmpFile).toString,
      config = Some(ModelConfig(Some(9000L), Some(9000L))))
    new HttpEntity[Mleap.LoadModelRequest](LoadModelRequest.toJavaProto(request), ProtobufScoringSpec.protoHeaders)
  }

  override def createTransformFrameRequest(modelName: String, frame: DefaultLeapFrame, options: Option[TransformOptions]): HttpEntity[Mleap.TransformFrameRequest] = {
    val request = TransformFrameRequest(modelName = modelName,
      format = BuiltinFormats.binary,
      initTimeout = Some(35000L),
      frame = ByteString.copyFrom(FrameWriter(frame, BuiltinFormats.binary).toBytes().get),
      options = options
    )

    new HttpEntity[Mleap.TransformFrameRequest](TransformFrameRequest.toJavaProto(request),
      ProtobufScoringSpec.protoHeaders)
  }

  override def createTransformFrameRequest(frame: DefaultLeapFrame): HttpEntity[Array[Byte]] =
    new HttpEntity[Array[Byte]](FrameWriter(validFrame, leapFrameFormat()).toBytes().get, ProtobufScoringSpec.protoHeaders)

  override def extractModelResponse(response: ResponseEntity[_ <: Any]): Mleap.Model = response.getBody.asInstanceOf[Mleap.Model]

  override def createEmptyBodyRequest(): HttpEntity[Unit] = ProtobufScoringSpec.httpEntityWithProtoHeaders

  override def extractBundleMetaResponse(response: ResponseEntity[_]): Mleap.BundleMeta = response.getBody.asInstanceOf[Mleap.BundleMeta]

  override def extractTransformResponse(response: ResponseEntity[_]): Mleap.TransformFrameResponse = response.getBody.asInstanceOf[Mleap.TransformFrameResponse]

  override def leapFrameFormat(): String = BuiltinFormats.binary

  override def createInvalidTransformFrameRequest(modelName: String, bytes: Array[Byte]): HttpEntity[Mleap.TransformFrameRequest] = {
    val request = TransformFrameRequest(modelName = modelName,
      format = BuiltinFormats.binary,
      initTimeout = Some(35000L),
      frame = ByteString.copyFrom(bytes),
      options = None
    )

    new HttpEntity[Mleap.TransformFrameRequest](TransformFrameRequest.toJavaProto(request),
      ProtobufScoringSpec.protoHeaders)
  }
}

object ProtobufScoringSpec {
  lazy val httpEntityWithProtoHeaders = new HttpEntity[Unit](protoHeaders)

  lazy val protoHeaders = {
    val headers = new HttpHeaders
    headers.add("Content-Type", "application/x-protobuf")
    headers.add("timeout", "2000")
    headers
  }
}