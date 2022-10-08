package understandzio

import understandzio.PlayGround.UserDB.UserDBEnv
import understandzio.PlayGround.UserMailer.UserMailerEnv
import zio.{ExitCode, Has, Task, URIO, ZIO, ZLayer}
import zio.console._

object PlayGround extends zio.App {
  case class User(name: String, email: String)

  object UserMailer {
    type UserMailerEnv = Has[UserMailer.Service]

    trait Service {
      def notify(user: User, message: String): Task[Unit]
    }

    val live: ZLayer[Any, Nothing, UserMailerEnv] = ZLayer.succeed(
      new Service {
        override def notify(user: User, message: String): Task[Unit] =
          Task {
            println(
              s"[email mailer] Hello ${user.name}, welcome to our hourly newsletter!\n$message"
            )
          }
      }
    )

    def notify(
        user: User,
        message: String
    ): ZIO[UserMailerEnv, Throwable, Unit] =
      ZIO.accessM(hasService => hasService.get.notify(user, message))
  }

  object UserDB {
    type UserDBEnv = Has[UserDB.Service]
    trait Service {
      def insert(user: User): Task[Unit]
    }

    val live = ZLayer.succeed(new Service {
      override def insert(user: User): Task[Unit] = Task {
        println(s"[Database] A dummy sql statement: ${user.email}")
      }
    })

    def insert(user: User): ZIO[UserDBEnv, Throwable, Unit] =
      ZIO.accessM(_.get.insert(user))
  }

  // Vertical composition
  object UserSubscription {
    type UserSubscriptionEnv = Has[UserSubscription.Service]
    class Service(notifier: UserMailer.Service, insert: UserDB.Service) {
      def subscribe(user: User): Task[User] = for {
        _ <- notifier.notify(user, "I dont know hihi")
        _ <- insert.insert(user)
      } yield (user)
    }

    val live
        : ZLayer[UserDBEnv with UserMailerEnv, Nothing, UserSubscriptionEnv] =
      ZLayer.fromServices[
        UserMailer.Service,
        UserDB.Service,
        UserSubscription.Service
      ] { (UserMailer, UserDB) => new Service(UserMailer, UserDB) }

    // front facing API
    def subscribe(user: User): ZIO[UserSubscriptionEnv, Throwable, User] =
      ZIO.accessM(_.get.subscribe(user))
  }

  val katherine: User = User("Katherine", "katherine-fu@outlook.com")
  val message = "I don't even know man"

  // Horizontal composition
  // ZLayer[In1, Err1, Out1] ++ ZLayer[In2, Err2, Out2] => ZLayer[In1 with In2, super(Err1, Err2), Out1 with Out2]

  import UserDB._
  import UserMailer._
  import UserSubscription._

  val userBackendLayer: ZLayer[Any, Nothing, UserDBEnv with UserMailerEnv] =
    UserDB.live ++ UserMailer.live

  val userSubscriptionLayer: ZLayer[Any, Nothing, UserSubscriptionEnv] =
    userBackendLayer >>> UserSubscription.live

  override def run(args: List[String]): URIO[Any with Console, ExitCode] = {
    UserSubscription
      .subscribe(katherine)
      .provideLayer(userSubscriptionLayer)
      .exitCode
  }
}
