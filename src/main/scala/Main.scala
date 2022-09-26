package zioplayground

import zio._

object MyApp {
  final case class Callable[+S](execute: () => S) { self => 
    def orElse[S1 >: S](that: Callable[S1]): Callable[S1] =
      Callable(() => try self.execute() catch { case _ : Throwable => that.execute()}) 

    def orElseEither[S2](that: Callable[S2]): Callable[Either[S, S2]] = ???

    def andThen[S2](onSuccess: S => Callable[S2]): Callable[S2] = ???
  }

  val hello = Callable(() => println("hello"))
  val bye = Callable(() => println("bye"))

  val askName = Callable(() => println("What is your name?"))
  val getName = Callable(() => scala.io.StdIn.readLine())
  def printName(name: String): Callable[Unit] = Callable(() => println(s"Hello ${name})"))

  val helloOrBye = hello.orElse(bye)

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

  def main(args: Array[String]): Unit = helloOrBye.execute()
}
