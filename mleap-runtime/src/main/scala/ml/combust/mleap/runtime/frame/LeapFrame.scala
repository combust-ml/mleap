package ml.combust.mleap.runtime.frame

import java.io.PrintStream

/**
  * Created by hollinwilkins on 10/5/17.
  */
trait LeapFrame[LF <: LeapFrame[LF]] extends FrameBuilder[LF] {
  /** Collect all rows into a Seq
    *
    * @return all rows in the leap frame
    */
  def collect(): Seq[Row]

  /** Print this leap frame to standard out.
    */
  def show(): Unit = show(System.out)

  /** Print this leap frame to a PrintStream.
    *
    * @param out stream to print to
    */
  def show(out: PrintStream): Unit = {
    out.print(LeapFrameShow(this))
  }

  /** Print this leap frame to standard out.
    *
    * @param n number of rows to display
    */
  def show(n: Int): Unit = show(System.out, n)

  /** Print this leap frame to a PrintStream.
    *
    * @param out stream to print to
    * @param n number of rows to show
    */
  def show(out: PrintStream, n: Int): Unit = {
    out.print(LeapFrameShow(this, n))
  }
}
