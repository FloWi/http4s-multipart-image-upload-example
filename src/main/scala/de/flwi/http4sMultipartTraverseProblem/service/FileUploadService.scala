package de.flwi.http4sMultipartTraverseProblem.service

import cats.effect.{ContextShift, IO}
import org.http4s.HttpRoutes
import cats.implicits._
import fs2.Stream
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.multipart.Part

import scala.concurrent.ExecutionContext

class FileUploadService(fileService: FileService) extends Http4sDsl[IO] {

  def filterFileTypes(part: Part[IO]): Boolean = {
    part.headers.exists(_.value.contains("filename"))
  }

  def httpService(implicit contextShift: ContextShift[IO], blockingEc: ExecutionContext): HttpRoutes[IO] =
    HttpRoutes.of[IO] {

      case req @ POST -> Root / "upload-images-with-traverse" =>
        req.decodeWith(EntityDecoder.multipart, strict = true) { response =>

          val filteredParts: Vector[Part[IO]]      = response.parts.filter(filterFileTypes)
          val stream: fs2.Stream[IO, Vector[Unit]] = filteredParts.traverse(fileService.store)

          Ok(stream.map(_ => s"Multipart file parsed successfully --> ${response.parts.size} parts"))
        }

      case req @ POST -> Root / "upload-images-without-traverse" =>
        req.decodeWith(EntityDecoder.multipart, strict = true) { response =>

          val filteredParts: Vector[Part[IO]] = response.parts.filter(filterFileTypes)
          val stream = Stream
            .fromIterator[IO, Part[IO]](filteredParts.toIterator)
            .flatMap(fileService.store)

          Ok(stream.map(_ => s"Multipart file parsed successfully --> ${response.parts.size} parts"))
        }

    }
}
