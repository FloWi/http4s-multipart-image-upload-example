package de.flwi.http4sMultipartTraverseProblem.service

import java.nio.file.Path

import cats.effect.{ContextShift, IO}
import de.flwi.http4sMultipartTraverseProblem.helper.StreamUtils
import fs2.Stream
import org.http4s.multipart.Part

import scala.concurrent.ExecutionContext

class FileService(uploadFolder: Path) {

  def store(part: Part[IO])(implicit S: StreamUtils[IO], contextShiftIO: ContextShift[IO], blockingEc: ExecutionContext): Stream[IO, Unit] = {

    for {
      _        <- S.evalF(println(s"trying to store this part: ${part.filename}"))
      filename <- S.evalF(part.filename.getOrElse("sample"))
      path     <- S.evalF(uploadFolder.resolve(filename))
      _        <- S.evalF(println(s"storing image $filename at $path"))
      _        <- part.body through fs2.io.file.writeAll(path, blockingEc)
      _        <- S.evalF(println(s"stored image $filename successfully at $path"))
    } yield ()
  }

}
