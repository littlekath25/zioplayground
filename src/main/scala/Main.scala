package zioplayground

import zio._
import java.io.IOException
import scala.io.AnsiColor._

object Main extends scala.App {
  Runtime.default.unsafeRunSync(program)

  val program =
    for (
      _ <- Console.printLine("What is your name?");
      name <- Console.readLine;
      _ <- Console.printLine(s"Hello $name!")
    ) yield ()
}
