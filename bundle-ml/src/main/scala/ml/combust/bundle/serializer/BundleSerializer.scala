package ml.combust.bundle.serializer

import java.io.Closeable
import java.nio.file.Files

import ml.combust.bundle.{BundleContext, BundleFile, HasBundleRegistry}
import ml.combust.bundle.json.JsonSupport._
import ml.combust.bundle.dsl.Bundle
import spray.json._
import resource._

import scala.util.{Failure, Try}

/** Class for serializing/deserializing Bundle.ML [[ml.combust.bundle.dsl.Bundle]] objects.
  *
  * @param context context for implementation
  * @param file bundle file for serialization
  * @param hr bundle registry for custom types and ops
  * @tparam Context context type for implementation
  */
case class BundleSerializer[Context](context: Context,
                                     file: BundleFile)
                                    (implicit hr: HasBundleRegistry) extends Closeable {
  /** Write a bundle to the path.
    *
    * @param bundle bundle to write
    * @return try of the bundle transformer
    */
  def write[Transformer <: AnyRef](bundle: Bundle[Transformer]): Try[Bundle[Transformer]] = Try {
    val bundleContext = bundle.bundleContext(context, hr.bundleRegistry, file.fs, file.path)
    implicit val sc = bundleContext.serializationContext(SerializationFormat.Json)

    Files.createDirectories(file.path)
    NodeSerializer(bundleContext.bundleContext("root")).write(bundle.root).flatMap {
      _ =>
        (for (out <- managed(Files.newOutputStream(bundleContext.file(Bundle.bundleJson)))) yield {
          val json = bundle.info.toJson.prettyPrint.getBytes
          out.write(json)
          bundle
        }).tried
    }
  }.flatMap(identity)

  /** Read a bundle from the path.
    *
    * @return deserialized bundle
    */
  def read[Transformer <: AnyRef](): Try[Bundle[Transformer]] = {
    for(info <- file.readInfo();
        bundleContext = BundleContext(context,
          info.format,
          hr.bundleRegistry,
          file.fs,
          file.path);
        sc = bundleContext.serializationContext(SerializationFormat.Json);
        root <- NodeSerializer(bundleContext.bundleContext("root")).read()) yield {
      Bundle(info, root.asInstanceOf[Transformer])
    }
  }

  override def close(): Unit = file.close()
}
