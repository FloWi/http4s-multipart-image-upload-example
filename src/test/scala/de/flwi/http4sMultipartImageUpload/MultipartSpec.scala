package de.flwi.http4sMultipartImageUpload

import java.io.File
import java.net.URL
import java.nio.file.{Files, Path}

import cats.effect.{ContextShift, IO}
import de.flwi.http4sMultipartImageUpload.service.{FileService, FileUploadService}
import org.http4s._
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.headers._
import org.http4s.implicits._
import org.http4s.multipart._
import org.scalatest.{Assertion, FunSuite, Matchers}

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global
import scala.util.{Failure, Success, Try}

class MultipartSpec extends FunSuite with Matchers with Http4sClientDsl[IO] {

  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val ec: ExecutionContext           = global

  val images: List[URL] = 1.to(3).map(i => getClass.getResource(s"/example-images/$i.png")).toList

  def testHelper(uri: Uri): Assertion = {
    val tempPath: Path                            = Files.createTempDirectory("file-upload")
    val (request @ _, response: IO[Response[IO]]) = go(uri, tempPath)

    val checkResult = Try(check[String](response, Status.Ok, Some("Multipart file parsed successfully --> 3 parts")))

    val actualFileNames   = tempPath.toFile.listFiles().toList.map(_.getName)
    val expectedFileNames = images.map(uri => new File(uri.toURI).getName)
    actualFileNames should contain theSameElementsAs expectedFileNames

    checkResult match {
      case Failure(exception) => fail(exception)
      case Success(_)         => succeed
    }
  }

  def go(uri: Uri, tempPath: Path): (Request[IO], IO[Response[IO]]) = {

    val service: HttpRoutes[IO] = new FileUploadService(new FileService(tempPath)).httpService

    def multipart(urls: List[URL]): Multipart[IO] = {
      val parts = urls.map(url => Part.fileData[IO](url.getFile, url, global, `Content-Type`(MediaType.image.png))).toVector

      Multipart[IO](parts)
    }

    val parts = multipart(images)
    val request: Request[IO] =
      Request(method = Method.POST, uri = uri)
        .withEntity(parts)
        .withHeaders(parts.headers)

    val response = service.orNotFound.run(request)

    (request, response)
  }

  def check[A](actual: IO[Response[IO]], expectedStatus: Status, expectedBody: Option[A])(
      implicit ev: EntityDecoder[IO, A]
  ): Assertion = {
    val actualResp = actual.unsafeRunSync

    actualResp.status shouldBe expectedStatus
    expectedBody.fold(
      actualResp.body.compile.toVector.unsafeRunSync.isEmpty shouldBe true
    )(expected => {
      val actual = actualResp.as[A].unsafeRunSync
      actual shouldBe expected
    })
  }

  test("files should be uploaded successfully") {

    testHelper(Uri.uri("/upload-images"))
  }
}
