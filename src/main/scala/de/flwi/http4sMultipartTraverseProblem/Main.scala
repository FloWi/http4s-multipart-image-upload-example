package de.flwi.http4sMultipartTraverseProblem

import java.io.File

import cats.data.Kleisli
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import de.flwi.http4sMultipartTraverseProblem.service.FileService
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.{Request, Response}
import org.http4s.implicits._
import org.http4s.server.Router

import scala.concurrent.ExecutionContext

object Main extends IOApp {

  implicit val blockingEc: ExecutionContext = ExecutionContext.fromExecutor(java.util.concurrent.Executors.newCachedThreadPool())

  val uploadFolder = new File("image-upload")
  if (!uploadFolder.exists()) {
    uploadFolder.mkdir()
  }

  val fileUploadService = new service.FileUploadService(new FileService(uploadFolder.toPath))

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "0.0.0.0") //needs to be 0.0.0.0 to be able to run inside docker-container
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)

  def httpApp: Kleisli[IO, Request[IO], Response[IO]] = {

    Router(
      "/" -> fileUploadService.httpService
    ).orNotFound
  }
}
