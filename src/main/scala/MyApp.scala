import zio._
import zio.Console._
import java.io.IOException

object MyApp extends ZIOAppDefault {

  def run: ZIO[Any, IOException, Unit] = myAppLogic

  val myAppLogic: ZIO[Any, IOException, Unit] =
    for {
      _    <- printLine("Hello! What is your name?")
      name <- readLine
      _    <- printLine(s"Hello, ${name}, welcome to ZIO!")
    } yield ()
}