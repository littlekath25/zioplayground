package zioplayground

import zio._

object MyApp {
  final case class Callable[+S](execute: () => S) { self =>
    def orElse[S1 >: S](that: Callable[S1]): Callable[S1] =
      Callable(() =>
        try self.execute()
        catch { case _: Throwable => that.execute() }
      )

    def flatMap[S2](onSuccess: S => Callable[S2]): Callable[S2] = andThen(
      onSuccess
    )

    def map[S2](transform: S => S2): Callable[S2] =
      Callable(() => transform(self.execute()))

    def andThen[S2](onSuccess: S => Callable[S2]): Callable[S2] = {
      Callable { () =>
        val result = self.execute()
        onSuccess(result).execute()
      }
    }
  }

  val hello = Callable(() => println("hello"))
  val bye = Callable(() => println("bye"))

  val askName = Callable(() => println("What is your name?"))
  val getName = Callable(() => scala.io.StdIn.readLine())
  def printName(name: String): Callable[Unit] =
    Callable(() => println(s"Hello ${name}"))

  val wholeProgram: Callable[Unit] =
    askName.andThen(_ => getName.andThen(name => printName(name)))

  def callable[S](s: => S) = Callable(() => s)

  def retry[S](statements: Callable[S], n: Int): Callable[S] = ???

  def parallelize[S](statements: Callable[S]): Callable[S] = ???

  def fourth(x: Double) = {
    val x_squared = x * x

    x_squared * x_squared
  }

  /*
  1. Extract variable
  2. Inline variable
  3. Extract method
  4. Inline method
   */

  final case class Rule[S](condition: () => Boolean, statements: Callable[S])

  def main(args: Array[String]): Unit = wholeProgram.execute()
}
