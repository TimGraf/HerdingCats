import cats.std.all._
import cats.data.XorT
import scala.concurrent.{Future, ExecutionContext}

object UserRepo {

  val userMap: Map[Long, User] = Map(
    0l -> User(0, "Vito", List(1, 2, 3)),
    1l -> User(1, "Michael", List(0, 3, 5)),
    2l -> User(2, "Susan", List.empty[Long]),
    3l -> User(3, "Todd", List(9)),
    4l -> User(4, "Kim", List(0, 2, 5)),
    5l -> User(5, "Steven", List(5, 6, 0, 4)),
    6l -> User(6, "Kevin", List(9)),
    7l -> User(7, "Sara", List(9, 0)),
    8l -> User(8, "Chris", List(9, 7, 6, 5, 4)),
    9l -> User(9, "Alex", List(0, 1, 2, 3, 4, 5, 6, 7, 8))
  )

  def isFriends(userId1: Long, userId2: Long)(implicit ec: ExecutionContext): XorT[Future, Error, Boolean] = {
    for {
      user1 <- getUser(userId1)
      user2 <- getUser(userId2)
    } yield user1.id == userId2 && user2.id == userId1
  }

  def getUser(userId: Long)(implicit ec: ExecutionContext): XorT[Future, Error, User] = {
    userMap.get(userId) match {
      case Some(user) => XorT.right[Future, Error, User](Future.successful(user))
      case None       => XorT.left[Future, Error, User](Future.successful(Error.UserNotFound(userId)))
    }
  }
}
