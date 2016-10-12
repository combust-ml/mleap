package ml.combust.bundle.op

import ml.combust.bundle.serializer.BundleContext
import ml.combust.bundle.dsl.{Node, Shape}

/** Type class for serializing/deserializing Bundle.ML graph nodes.
  *
  * Nodes are used to represent a data processing function in a Bundle.ML graph.
  * Nodes connect data fields being processed to the underlying ML models that
  * know how to transform the data.
  */
trait OpNode[N, M] {
  /** Type class for the underlying model.
    */
  val Model: OpModel[M]

  /** Get the unique name for this node.
    *
    * @param node node object
    * @return unique name of the node
    */
  def name(node: N): String

  /** Get the underlying model of the node.
    *
    * @param node node object
    * @return underlying model object
    */
  def model(node: N): M

  /** Get the shape of the node.
    *
    * @param node node object
    * @return shape of the node
    */
  def shape(node: N): Shape

  /** Get the children of the node.
    *
    * @param node node object
    * @return children of the node
    */
  def children(node: N): Option[Array[Any]] = None

  /** Load a node from Bundle.ML data.
    *
    * @param context bundle context for decoding custom values and getting non-standard params
    * @param node read-only node for attributes
    * @param model deserialized model for the node
    * @return deserialized node object
    */
  def load(context: BundleContext,
           node: Node,
           model: M): N
}
