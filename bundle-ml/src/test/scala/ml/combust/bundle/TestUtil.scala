package ml.combust.bundle

import java.io.File

/**
  * Created by hollinwilkins on 8/23/16.
  */
object TestUtil {
  val baseDir = new File("/tmp/bundle-ml")
  TestUtil.delete(baseDir)
  baseDir.mkdirs()

  def delete(file: File): Array[(String, Boolean)] = {
    Option(file.listFiles).map(_.flatMap(f => delete(f))).getOrElse(Array()) :+ (file.getPath -> file.delete)
  }
}

