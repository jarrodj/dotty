package dotty.tools
package dotc
package reporting

import scala.collection.mutable
import core.Positions.Position
import core.Contexts._
import Reporter.Severity.{Value => Severity, _}
import java.io.{ BufferedReader, IOException, PrintWriter }
import scala.reflect.internal.util._

/**
 * This class implements a Reporter that displays messages on a text
 * console.
 */
class ConsoleReporter(
    reader: BufferedReader = Console.in,
    writer: PrintWriter = new PrintWriter(Console.err, true))
  extends Reporter with UniqueMessagePositions {

  /** Whether a short file name should be displayed before errors */
  protected def shortName: Boolean = false

  /** maximal number of error messages to be printed */
  protected def ErrorLimit = 100

  def formatMessage(msg: String, pos: Position)(implicit ctx: Context) = msg // for now

  /** Prints the message. */
  def printMessage(msg: String) { writer.print(msg + "\n"); writer.flush() }

  /** Prints the message with the given position indication. */
  def printMessage(msg: String, pos: Position)(implicit ctx: Context) {
    printMessage(formatMessage(msg, pos))
  }

  def printMessage(msg: String, severity: Severity, pos: Position)(implicit ctx: Context) {
    printMessage(label(severity) + msg, pos)
  }

  /**
   *  @param pos ...

  def printSourceLine(pos: Position) {
    printMessage(pos.lineContent.stripLineEnd)
    printColumnMarker(pos)
  }

  /** Prints the column marker of the given position.
   *
   *  @param pos ...
   */
  def printColumnMarker(pos: Position) =
    if (pos.isDefined) { printMessage(" " * (pos.column - 1) + "^") }
*/

  /** Prints the number of errors and warnings if their are non-zero. */
  def printSummary() {
    if (count(WARNING) > 0) printMessage(countString(WARNING) + " found")
    if (  count(ERROR) > 0) printMessage(countString(ERROR  ) + " found")
  }

  override def report(msg: String, severity: Severity, pos: Position)(implicit ctx: Context) {
    if (severity != ERROR || count(severity) <= ErrorLimit)
      printMessage(msg, severity, pos)
    if (ctx.settings.prompt.value) displayPrompt()
  }

  def displayPrompt(): Unit = {
    writer.print("\na)bort, s)tack, r)esume: ")
    writer.flush()
    if (reader != null) {
      val response = reader.read().asInstanceOf[Char].toLower
      if (response == 'a' || response == 's') {
        (new Exception).printStackTrace()
        if (response == 'a')
          sys exit 1

        writer.print("\n")
        writer.flush()
      }
    }
  }

  override def flush() { writer.flush() }
}