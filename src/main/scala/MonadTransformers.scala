import scalaz.Scalaz.ToEitherOps
import scalaz.{Monad, \/}

import scalaz._
import Scalaz._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
object MonadTransformers {
  def main(args: Array[String]): Unit = {
    println("Hello world!")
    // Different types in scala Int, String, Boolean
    val num: Int = 1
    val word: String = "pog"
    val bool: Boolean = true

//    val hello: String = test
//    println(hello)
  }

  case class User()
  case class Password() {
    def value() = "value"
  }

  object WhatIsAMonad {
    // A type that encompasses other types within a context (container)
    // e.g. Option, List, Map, Either, Future etc etc
    scalaz.Monad
    val lst: List[Int] = List(1, 2, 3)
    val map: Map[Int, Int] = Map(1 -> 1)
    val someString: Option[String] = "String".some
    val NoneString: Option[String] = None
  }

  object WhatIsATransfomer {
    val num: Int = 1
    val str: String = num.toString
    val lst: Long = num.toLong
  }

  object FlatMappingMonads {
    def getUser(emailAddress: String): Future[User] = ???

    def getPassword(user: User): Future[Password] = ???

    val andysPassword: Future[Future[Password]] =
      getUser("POGGERS@pogging.com").map(user => getPassword(user))

    andysPassword.flatten

    val andysPasswordFlatMap: Future[Password] =
      getUser("POGGERS@pogging.com").flatMap(user => getPassword(user))

    val passwordagain = for {
      user <- getUser("asdf")
      password <- getPassword(user)
    } yield password
  }

  object PlayingWith2Monads {
    val optionalEitherInt: Option[String \/ Int] = None
    optionalEitherInt.map(either => {
      either.map(num => num + 1)
    })

//      for {
//        either <- optionalEitherInt
//        a <- either // Does work
//      } yield a + 1

    val one: Option[Int] = Some(1)
    val eitherOptionInt: String \/ Option[Int] = one.right[String]
    eitherOptionInt.map(option => option.map(num => num + 1))

//      for {
//        option <- eitherOptionInt
//        a <- option // Is not String \/ Int
//      } yield a + 1
  }

  object WhenToUseMonadTransformers {
    def getUser(emailAddress: String): Future[Option[User]] = ???
    def getPassword(user: User): Future[Option[Password]] = ???

//    val passwordtry1 = for {
//      user <- getUser("asdf")
//      password <- getPassword(user) // needs a user
//    } yield password

    val passwordtry2: Future[Option[Password]] = for {
      user <- getUser("asdf")
      password <- getPassword(user.get)
    } yield password

    getUser("asdf").map(maybeUser => maybeUser.map(user => getPassword(user)))

//    val passwordtry3 = for {
//      optionalUser <- getUser("asdf")
//      user <- optionalUser
//      password <- getPassword(user)
//    } yield password

    getUser("asdf").flatMap(maybeUser =>
      maybeUser match {
        case Some(user) => getPassword(user)
        case None       => Future.successful(None)
      }
    )

    val passwordtry4: Future[Option[Password]] = for {
      user <- getUser("asdf")
      password <- user match {
        case Some(user) => getPassword(user)
        case None       => Future.successful(None)
      }
    } yield password

    def getUsers(emailAddress: String): List[Option[User]] = ???
    def getPasswords(user: User): List[Option[Password]] = ???

    getUsers("asdf").flatMap(maybeUser =>
      maybeUser match {
        case Some(user) => getPasswords(user)
        case None       => List(None)
      }
    )

    val passwordstry4: List[Option[Password]] = for {
      user <- getUsers("asdf")
      password <- user match {
        case Some(user) => getPasswords(user)
        case None       => List(None)
      }
    } yield password

    def getUsers123(emailAddress: String): Set[Option[User]] = ???
    def getPasswords123(user: User): Set[Option[Password]] = ???
  }

  object CreateFutOpt {

    case class FutOpt[A](value: Future[Option[A]])

    implicit object futOptInstance extends Monad[FutOpt] {

      override def point[A](a: => A): FutOpt[A] = ??? // also called pure

      override def map[A, B](fa: FutOpt[A])(f: A => B): FutOpt[B] = ???

      override def bind[A, B](fa: FutOpt[A])(f: A => FutOpt[B]): FutOpt[B] =
        ??? // bind is just flatMap

      def flatMap[A, B](fa: FutOpt[A])(f: A => FutOpt[B]): FutOpt[B] = FutOpt(
        fa.value.flatMap(opt =>
          opt match {
            case Some(value) => f(value).value
            case None        => Future(None)
          }
        )
      )
    }

    private def getUser(emailAddress: String): Future[Option[User]] = ???
    private def getPassword(user: User): Future[Option[Password]] = ???

    FutOpt(getUser("me"))
    val betterWay: FutOpt[Password] = for {
      user <- FutOpt(getUser("me"))
      password <- FutOpt(getPassword(user))
    } yield password

    betterWay.value
  }

//  OptionT[F[_], A]
  object UsingOptionT {
    def getUser(emailAddress: String): Future[Option[User]] = ???
    def getPassword(user: User): Future[Option[Password]] = ???

    val evenbetterWay: OptionT[Future, String] = for {
      user <- OptionT(getUser("me"))
      password <- OptionT(getPassword(user))
    } yield password.value()

    evenbetterWay.run

    // OptionT[Future, String] => Future[Option[String]]
    // BarT[Foo, X] -> Foo[Bar[X]
  }

  object MonadTransformerHelperFunction {
    def getUser(id: String): Future[Option[User]] = ???
    def getAge(user: User): Future[Int] = ???
    def getNickname(user: User): Option[String] = ???

//    3 Levels of monads
//    def getUser3Levels(id: String): Future[String \/ Option[User]] = ???
//    type FOO[A] = Future[String \/ A]
//    val x = OptionT[FOO, User](getUser3Levels("foo"))
//    x.map(a => a)

    // OptionT[Future, User] => Future[Option[User]]
//    val generateNameAndAgeString: OptionT[Future, String] = for {
//      user <- OptionT(getUser("pog"))
//      age <- OptionT(getAge(user)) // doesnt work
//      name <- OptionT(getNickname(user)) //does workj
//    } yield s"agename"

    // OptionT[Future, User] => Future[Option[User]]
    val generateNameAndAgeStringWorking: OptionT[Future, String] = for {
      user <- OptionT(getUser("pog"))
      age <- OptionT(getAge(user).map(_.some))
      name <- OptionT(Future.successful(getNickname(user)))
    } yield s"$name$age"

    val sameCodeAsAbove: OptionT[Future, String] = for {
      user <- OptionT(getUser("pog"))
      age <- OptionT(getAge(user).map(_.point[Option]))
      name <- OptionT(getNickname(user).point[Future])
    } yield s"$name$age"

    def getUsers(id: String): Future[Option[User]] = ???
    def getAges(user: User): Future[String] = ???
    def getNicknames(user: User): Option[String] = ???

    def OptionTLift[W[_]: Monad, A](monad: W[A]): OptionT[W, A] = OptionT(
      monad.map(_.point)
    )
    def OptionTFromOption[W[_]: Monad, A](option: Option[A]): OptionT[W, A] =
      OptionT(option.point)

    val withHelperFunctions: OptionT[Future, String] = for {
      user <- OptionT(getUser("pog"))
      age <- OptionTLift(getAge(user))
      name <- OptionTFromOption[Future, String](getNicknames(user))
    } yield s"$name$age"

  }

  object ExtensionFunctions {
    def getUser(id: String): Future[Option[User]] = ???
    def getAge(user: User): Future[String] = ???
    def getNickname(user: User): Option[String] = ???

    object ScalazExtensions {
      // Helper Functions

      implicit class OptionTExtension[W[_], A](stuff: OptionT[W, A]) {
        def andy(): OptionT[W, A] = stuff
      }

      implicit class OptionExtensions[A](value: Option[A]) {
        def hoist[M[_]: Applicative]: OptionT[M, A] = OptionT(value.point[M])
      }

    }

    {
      import ExtensionFunctions.{getUser, getAge, getNickname}
      import ScalazExtensions._
      import scalaz.OptionT
      import scalaz._
      import Scalaz._
      //getUser("pog").point[OptionT]
//      val optionT = OptionT(getUser("andy"))
//      optionT.andy()
//      "andy".some.hoist[Future]

      val withExtensionFunctions: OptionT[Future, String] = for {
        user <- OptionT(getUser("pog"))
        age <- getAge(user).liftM[OptionT]
        name <- getNickname(user).hoist[Future]
      } yield s"$name$age"
    }
  }

//  object usingOptionTF {
//    def getUser[F[_]](): Free[F, Option[User]] = ???
//
//    def callGetUser(): Free[Future, Option[User]] = {
//      for {
//        user <- getUser[Future]()
//      } yield user
//  }

  /*
    Example of updating a user
    - Check user exists
    - Check it can be updated
    - Update it
   */

  object RealScalaExample {
    def checkUserExists(id: String): Future[Option[User]] = ???
    def checkCanBeUpdated(u: User): Future[Boolean] = ???
    def updateUserOnDb(u: User): Future[User] = ???

    def updateUser(u: User): Future[String \/ User] =
      checkUserExists("test").flatMap { optionalUser =>
        optionalUser match {
          case Some(user) =>
            checkCanBeUpdated(user).flatMap { canUpdate =>
              if (canUpdate) {
                updateUserOnDb(u).map(a => a.right)
              } else {
                Future("Cannot update user".left)
              }
            }
          case None => Future("User does nto exist".left)
        }
      }

//    def updateUserWithTransformer(u: User): Future[String \/ User] = for {
//      user <- OptionT(checkUserExists("test"))
//      canUpdate <- checkCanBeUpdated(user).liftM[OptionT]
//      futureResult = canUpdate match {
//        case true => updateUserOnDb(u).map(a => a.right[String])
//        case false => Future("Cannot update user".left[User])
//      }
//    } yield futureResult

  }

  object LinearScaling {
    case class SomeError(msg: String)
    type ResultT[F[_], A] =
      EitherT[
        SomeError,
        F,
        A
      ] // EitherT[SomeError, Future, User] => Future[SomeError \/ User]
    type FutureResult[A] = ResultT[Future, A]

    def checkUserExists(id: String): FutureResult[User] = EitherT(Future {
      if (id == "Rocket is the best team") {
        User().right
      } else {
        SomeError("Megaman is the best team? SHEESH!!").left
      }
    })

    def checkCanBeUpdated(u: User): FutureResult[Boolean] = ???

    def updateUserOnDb(u: User): FutureResult[User] = ???
//    def updateUserOnDb(u: User, canUpdate: Boolean): FutureResult[User] = ???

    def updateUser(user: User): FutureResult[User] = for {
      user <- checkUserExists("id")
      canUpdate <- checkCanBeUpdated(user)
      updateUser <- updateUserOnDb(user)
    } yield updateUser

    updateUser(User()).run
  }

  // 1. This shit is super hard to read when u stack 2 monads transformers
  // 2. Wrapping and unwrapping a bunch of types
  // 3. Anything T is a monad transformer F[G[X]] => G[F[_], X]

//    val optionalEither: Option[String \/ Int] = None
//    private val transformer: EitherT[Option, String, Int] = EitherT(optionalEither)
//    val b: EitherT[Option, String, Int] = transformer.map(num => 1 + num) // Some(\/-(2))
//    val c: Option[String \/ Int] = b.run
//
//    val someList = List(1, 2, 3)
//    val someOption: Option[Int] = None
//    val someEither = Right(1)
//
//
//    val sum = someOption.flatMap(a =>
//      someEither.flatMap(b =>
//        a + b
//      )
//    )
//
//    val newSum = for {
//      x <- someOption
//      y <- someEither
//    } yield (x + y)
//
//    for {
//      x <- OptionT(someOption)
//      y <- EitherT(someEither)
//    } yield 1
//
//    val number = 1
//    number.toString
//
//    final case class User()
//
//    final case class Password() {
//      def value() = 1
//    }
}
