import zio._
import java.io.IOException
import scala.io.AnsiColor._

object ourConsole {
  def printLine(line: String) = {
    ZIO.succeed(println(line))
  }

  val readLine =
    ZIO.succeed(scala.io.StdIn.readLine)
}

object Main extends scala.App {
  val program =
    for (
      _ <- ourConsole.printLine("What is your name?");
      name <- ourConsole.readLine;
      _ <- ourConsole.printLine(s"Hello $name!")
    ) yield ()

  val runtime: Runtime[Any] = Runtime.default

  Unsafe.unsafe { implicit unsafe =>
    runtime.unsafe
      .run(program)
      .getOrThrowFiberFailure()
  }
}
