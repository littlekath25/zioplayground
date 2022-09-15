import zio._
import zio.Console._
import java.io.IOException

object Main extends ZIOAppDefault {
  val program: ZIO[Console, IOException, Unit] =
    for (
      _ <- Console.printLine("-" * 100);
      _ <- Console.printLine("Hello World");
      _ <- Console.printLine("-" * 100)
    ) yield ()

  def run: URIO[Any, ExitCode] =
    program.exitCode
}
