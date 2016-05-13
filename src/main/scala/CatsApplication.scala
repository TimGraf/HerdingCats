import cats.std.all._
import cats.data.Xor
import xor.Good

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.concurrent.duration._

/**
  * Simple app to try out Cats Stacking Future and Either
  * http://eed3si9n.com/herding-cats/stacking-future-and-either.html
  */
object CatsApplication extends App {

  val res = for {
    res1 <- UserRepo.isFriends(0, 1)
    res2 <- UserRepo.isFriends(2, 1)
    res3 <- UserRepo.isFriends(2, 10)
  } yield res1 && res2 && res3

  res.value.onComplete {
    case Success(Xor.Right(value)) => println(value)
    case Success(Xor.Left(error))  => println(error)
    case Failure(ex)               => println(ex.getLocalizedMessage)
  }

  Await.result(res.value, 1.second)



  val res2 = for {
    res1 <- UserRepo.isFriends1(0, 1)
    res2 <- UserRepo.isFriends1(4, 5)
    res3 <- UserRepo.isFriends1(4, 0)
  } yield (res1, res2, res3)


  res2.onComplete {
    case Success((Good(b1), Good(b2), Good(b3))) => println(s"$b1 $b2 $b3"); println(b1 && b2 && b3)
    case _ => println(false)
  }

  Await.result(res2, 1.second)
}
