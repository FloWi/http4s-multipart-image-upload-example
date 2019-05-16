package de.flwi.http4sMultipartImageUpload.service

import cats.effect.{ContextShift, IO}
import cats.implicits._
import org.http4s.dsl.Http4sDsl
import org.http4s.multipart.Part
import org.http4s.{HttpRoutes, _}

import scala.concurrent.ExecutionContext

class FileUploadService(fileService: FileService) extends Http4sDsl[IO] {

  def httpService(implicit contextShift: ContextShift[IO], blockingEc: ExecutionContext): HttpRoutes[IO] =
    HttpRoutes.of[IO] {

      case req@POST -> Root / "upload-images" =>
        req.decodeWith(EntityDecoder.multipart, strict = true) { response =>
          val filteredParts: Vector[Part[IO]] = response.parts.filter(filterFileTypes)
          val saveResult: IO[Vector[Unit]] = filteredParts.traverse(fileService.store)

          Ok(saveResult.map(_ => s"Multipart file parsed successfully --> ${response.parts.size} parts"))
        }
    }

  def filterFileTypes(part: Part[IO]): Boolean = {
    part.headers.exists(_.value.contains("filename"))
  }
}
