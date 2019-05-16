package de.flwi.http4sMultipartImageUpload.service

import java.nio.file.Path

import cats.effect.{ContextShift, IO}
import org.http4s.multipart.Part

import scala.concurrent.ExecutionContext

class FileService(uploadFolder: Path) {

  def store(part: Part[IO])(implicit contextShiftIO: ContextShift[IO], blockingEc: ExecutionContext): IO[Unit] = {

    for {
      _        <- IO(println(s"trying to store this part: ${part.filename}"))
      filename <- IO(part.filename.getOrElse("sample"))
      path     <- IO(uploadFolder.resolve(filename))
      _        <- IO(println(s"storing image $filename at $path"))
      _        <- part.body.through(fs2.io.file.writeAll(path, blockingEc)).compile.drain
      _        <- IO(println(s"stored image $filename successfully at $path"))
    } yield ()
  }

}
