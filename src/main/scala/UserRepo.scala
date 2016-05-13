import cats.std.all._
import cats.data.XorT
import xor.{Bad, Good, Or}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

object UserRepo {

  val userMap: Map[Long, User] = Map(
    0l -> User(0, "Vito", List(1, 2, 3, 4)),
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
      user1FriendsIds <- getFriendsIds(userId1)
      user2FriendsIds <- getFriendsIds(userId2)
    } yield user1FriendsIds.contains(userId2) && user2FriendsIds.contains(userId1)
  }

  def getFriendsIds(userId: Long)(implicit ec: ExecutionContext): XorT[Future, Error, List[Long]] = {
    userMap.get(userId) match {
      case Some(user) => XorT.right[Future, Error, List[Long]](Future.successful(user.friends))
      case None       => XorT.left[Future, Error, List[Long]](Future.successful(Error.UserNotFound(userId)))
    }
  }

  def isFriends1(userId1: Long, userId2: Long)(implicit ec: ExecutionContext): Future[Error Or Boolean] = {
    val promise = Promise[Error Or Boolean]

    val fres = for {
      user1FriendsIds <- getFriendsIds1(userId1)
      user2FriendsIds <- getFriendsIds1(userId2)
    } yield (user1FriendsIds, user2FriendsIds)

    fres.onComplete {
      case Success((Good(list1), Good(list2))) => promise.success(Good(list2.contains(userId1) && list1.contains(userId2)))
      case Success(_)                          => promise.success(Good(false))
      case Failure(error)                      => promise.failure(error)
    }

    promise.future
  }

  def getFriendsIds1(userId: Long)(implicit ec: ExecutionContext): Future[Error Or List[Long]] = {
    userMap.get(userId) match {
      case Some(user) => Future.successful(Good(user.friends))
      case None       => Future.successful(Bad(Error.UserNotFound(userId)))
    }
  }
}
