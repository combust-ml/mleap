package ml.combust.mleap.runtime.function

import ml.combust.mleap.runtime.frame.Row

import scala.language.implicitConversions

/** Trait for a LeapFrame selector.
  *
  * A selector generates values based on other values found
  * in a [[Row]]. The name parameters
  * to a selector specifies which column of the row to get
  * the values from.
  *
  * Currently there are two supported selectors: a field selector and
  * and array selector.
  *
  * [[FieldSelector]] selects the value of a given field.
  * [[StructSelector]] creates an array from the values of a given set of fields.
  */
sealed trait Selector

/** Companion object for selectors.
  *
  * Provides implicit conversions for convenience.
  */
object Selector {
  /** Create a [[FieldSelector]] for a given name.
    *
    * @param name name of field
    * @return field selector
    */
  implicit def apply(name: String): FieldSelector = FieldSelector(name)

  /** Create an [[StructSelector]] for a given list of names.
    *
    * @param names fields names used to construct the array
    * @return array selector
    */
  implicit def apply(names: Seq[String]): StructSelector = StructSelector(names)
}

/** Class for a selector that extracts the value of a field from a [[Row]].
  *
  * @param field name of field to extract
  */
case class FieldSelector(field: String) extends Selector

/** Class for a selector that constructs an array from values in a [[Row]].
  *
  * @param fields names of fields used to construct array
  */
case class StructSelector(fields: Seq[String]) extends Selector
