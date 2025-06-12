import cats.data.EitherT
import cats.effect.{Concurrent, IO}
import cats.implicits.toBifunctorOps

object Main {
  class MyError(message: String) extends Throwable(message)

  trait Client[F[_]] {
    def foo(bar: String): F[Either[MyError, String]]
  }

  class ClientImpl[F[_] : Concurrent] extends Client[F] {
    override def foo(bar: String): F[Either[MyError, String]] = Concurrent[F].pure(
      Right(s"Hello, $bar!")
    )
  }

  def main(args: Array[String]): Unit = {
    val client = new ClientImpl[IO]
    val _ = for {
      result <- EitherT(client.foo("World").map(
        _.leftMap(error => new MyError(s"Failed with error: ${error.getMessage}"))
      ))

      anotherResult <- EitherT(
        IO.pure(s"$result")
      )
    } yield {
      anotherResult
    }
  }
}

