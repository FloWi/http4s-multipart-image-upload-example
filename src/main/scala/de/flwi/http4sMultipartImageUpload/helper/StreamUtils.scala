package de.flwi.http4sMultipartImageUpload.helper

import cats.effect.Sync
import fs2.Stream

trait StreamUtils[F[_]] {
  def putStrLn(value: String)(implicit F: Sync[F]): Stream[F, Unit]     = evalF(println(value))

  def putStr(value: String)(implicit F: Sync[F]): Stream[F, Unit]       = evalF(print(value))

  def env(name: String)(implicit F: Sync[F]): Stream[F, Option[String]] = evalF(sys.env.get(name))

  def evalF[A](thunk: => A)(implicit F: Sync[F]): Stream[F, A]          = Stream.eval(F.delay(thunk))
}

object StreamUtils {
  implicit def syncInstance[F[_]: Sync]: StreamUtils[F] = new StreamUtils[F] {}
}
