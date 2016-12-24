package ml.combust.bundle

import com.typesafe.config.{Config, ConfigFactory}
import ml.combust.bundle.op.{OpModel, OpNode}
import ml.combust.bundle.custom.CustomType
import ml.combust.bundle.util.ClassLoaderUtil

import scala.collection.JavaConverters._
import scala.reflect.{ClassTag, classTag}

/** Trait for classes that contain a bundle registry.
  *
  * This is used by methods that require access to a bundle registry.
  */
trait HasBundleRegistry {
  /** Get the bundle registry.
    *
    * @return bundle registry
    */
  def bundleRegistry: BundleRegistry
}

object BundleRegistry {
  def apply(registry: String): BundleRegistry = apply(registry, None)

  def apply(registry: String, cl: Option[ClassLoader]): BundleRegistry = {
    apply(registry, None, cl)
  }

  def apply(registry: String, configOption: Option[Config], clOption: Option[ClassLoader]): BundleRegistry = {
    val cl = clOption.getOrElse(ClassLoaderUtil.findClassLoader(classOf[BundleRegistry].getCanonicalName))
    val config = configOption.getOrElse(ConfigFactory.load(cl))

    val br = config.getStringList(s"ml.combust.bundle.registry.$registry.ops").asScala.foldLeft(Map[String, OpNode[_, _, _]]()) {
      (m, opClass) =>
        val opNode = cl.loadClass(opClass).newInstance().asInstanceOf[OpNode[_, _, _]]
        m + (opNode.Model.opName -> opNode)
    }.values.foldLeft(BundleRegistry(cl)) {
      (br, opNode) => br.register(opNode)
    }

    config.getStringList("ml.combust.bundle.customTypes").asScala.foldLeft(br) {
      (br2, customClass) =>
        br.register(cl.loadClass(customClass).
          getConstructor(classOf[BundleRegistry]).
          newInstance(br).
          asInstanceOf[CustomType[_]])
    }
  }
}

/** Class for storing all supported [[ml.combust.bundle.op.OpNode]] and [[ml.combust.bundle.custom.CustomType]] objects.
  *
  * This is the primary registry for Bundle.ML. It contains all objects
  * required for serializing ML models, graph nodes and custom types.
  *
  * Many serialization calls in Bundle.ML require access to the registry for information
  * on how to serialize custom types or models or nodes.
  *
  * @param classLoader class loader used to create this registry
  */
case class BundleRegistry private (classLoader: ClassLoader) extends HasBundleRegistry {
  var ops: Map[String, OpNode[_, _, _]] = Map()
  var opAlias: Map[String, String] = Map()

  var models: Map[String, OpModel[_, _]] = Map()
  var modelAlias: Map[String, String] = Map()

  var customTypes: Map[String, CustomType[_]] = Map()
  var customTypeAlias: Map[String, String] = Map()

  override def bundleRegistry: BundleRegistry = this

  /** Get an op node by name.
    *
    * @param op name of op
    * @tparam Context context for implementation
    * @tparam N class of op
    * @tparam M model class of op
    * @return op node type class for given name
    */
  def apply[Context, N, M](op: String): OpNode[Context, N, M] = ops(op).asInstanceOf[OpNode[Context, N, M]]

  /** Get an op node for an object.
    *
    * @param obj node object
    * @tparam Context context for implementation
    * @tparam N class of op
    * @tparam M model class of op
    * @return op node type class for given object
    */
  def opForObj[Context, N, M](obj: N): OpNode[Context, N, M] = ops(opAlias(obj.getClass.getCanonicalName)).asInstanceOf[OpNode[Context, N, M]]

  /** Get a model for a name.
    *
    * @param op name of op
    * @tparam Context context for implementation
    * @tparam M model class of op
    * @return model type class for given name
    */
  def model[Context, M](op: String): OpModel[Context, M] = models(op).asInstanceOf[OpModel[Context, M]]

  /** Get a model for an object.
    *
    * @param obj model object
    * @tparam Context context for implementation
    * @tparam M model class
    * @return model type class for given object
    */
  def modelForObj[Context, M](obj: Any): OpModel[Context, M] = model(modelAlias(obj.getClass.getCanonicalName))

  /** Get custom type by name.
    *
    * @param name name of custom object
    * @tparam T type of custom object
    * @return custom object type class
    */
  def custom[T](name: String): CustomType[T] = customTypes(name).asInstanceOf[CustomType[T]]

  /** Get custom type for an object.
    *
    * @param obj custom object
    * @tparam T type of custom object
    * @return custom object type class
    */
  def customForObj[T](obj: Any): CustomType[T] = custom(customTypeAlias(obj.getClass.getCanonicalName))

  /** Get custom type for a class.
    *
    * @tparam T type of custom object
    * @return custom object type class
    */
  def customForClass[T: ClassTag]: CustomType[T] = custom(customTypeAlias(classTag[T].runtimeClass.getCanonicalName)).asInstanceOf[CustomType[T]]

  /** Register an op node for serialization/deserialization.
    *
    * This will register the underlying model type class as well.
    *
    * @param op op node type class
    * @tparam Context context for implementation
    * @tparam N type of the op node
    * @tparam M type of the underlying model
    * @return this
    */
  def register[Context, N, M](op: OpNode[Context, N, M]): this.type = {
    ops = ops + (op.Model.opName -> op)
    opAlias = opAlias + (op.klazz.getCanonicalName -> op.Model.opName)
    models = models + (op.Model.opName -> op.Model)
    modelAlias = modelAlias + (op.Model.klazz.getCanonicalName -> op.Model.opName)
    this
  }

  /** Register a custom type for serialization/deserialization.
    *
    * @param c custom type type class
    * @tparam T type of the custom type
    * @return this
    */
  def register[T](c: CustomType[T]): this.type = {
    customTypes = customTypes + (c.name -> c)
    customTypeAlias = customTypeAlias + (c.klazz.getCanonicalName -> c.name)
    this
  }
}
