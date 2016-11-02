package ml.combust.mleap.json

import java.nio.charset.Charset

import ml.combust.mleap.runtime.LeapFrame
import ml.combust.mleap.runtime.serialization.{BuiltinFormats, FrameWriter}
import JsonSupport._
import spray.json._

/**
  * Created by hollinwilkins on 11/2/16.
  */
class DefaultFrameWriter() extends FrameWriter {
  override def toBytes[LF <: LeapFrame[LF]](frame: LF, charset: Charset = BuiltinFormats.charset): Array[Byte] = {
    frame.toJson.prettyPrint.getBytes(charset)
  }
}
