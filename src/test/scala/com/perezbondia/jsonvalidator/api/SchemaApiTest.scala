/*
 * Copyright (c) 2022 Manel Perez
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.perezbondia.jsonvalidator.api

import cats.effect._
import cats.implicits._

import io.circe._
import io.circe.literal._
import munit._
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._
import org.http4s.server.Router
import org.typelevel.ci.CIString
import org.typelevel.ci.CIString.apply

import com.perezbondia.jsonvalidator.api.model._
import com.perezbondia.jsonvalidator.core.SchemaService
import com.perezbondia.jsonvalidator.core.domain.model.SchemaId
import com.perezbondia.jsonvalidator.infra.FakeSchemaRepo
import com.perezbondia.jsonvalidator.test.TestHelpers._
import com.perezbondia.jsonvalidator.types._

class SchemaApiTest extends CatsEffectSuite {

  given EntityDecoder[IO, BadRequestResponse] = jsonOf
  given EntityDecoder[IO, NotFoundResponse]   = jsonOf
  given EntityDecoder[IO, SuccessResponse]    = jsonOf

  test("POST /schema/schemaId returns success on valid json") {
    val expectedStatusCode     = Status.Ok
    val expectedResponse       = SuccessResponse(Action.uploadSchema)
    val expectedResponseString = """{"id":"config-schema","action":"uploadSchema","status":"success"}"""
    val expectedContentType    = "application/json"

    val test = testResources(Map.empty).use { service =>
      for {
        uri <- Uri.fromString("/schema/schemaId").toOption.getOrThrow
        request = Request[IO](method = Method.POST, uri = uri).withEntity(json"""{}""")
        response    <- service.orNotFound.run(request)
        body        <- response.as[SuccessResponse]
        bodyString  <- response.as[String]
        contentType <- response.headers.get(CIString("content-type")).getOrThrow
      } yield (response.status, body, bodyString, contentType.head.value)
    }
    test.assertEquals((expectedStatusCode, expectedResponse, expectedResponseString, expectedContentType))
  }

  test("POST /schema/schemaId returns bad request on invalid json") {
    val expectedStatusCode = Status.BadRequest
    val expectedResponse =
      """{"id":"config-schema","action":"uploadSchema","status":"error","message":"Invalid JSON. ParsingFailure: expected \" got 'invali...' (line 1, column 13)"}"""
    val expectedContentType = "application/json"

    val test = testResources(Map.empty).use { service =>
      for {
        uri <- Uri.fromString("/schema/schemaId").toOption.getOrThrow
        request = Request[IO](method = Method.POST, uri = uri).withEntity("""{"valid":1, invalid: 2}""")
        response    <- service.orNotFound.run(request)
        body        <- response.as[String]
        contentType <- response.headers.get(CIString("content-type")).getOrThrow
      } yield (response.status, body, contentType.head.value)
    }
    test.assertEquals((expectedStatusCode, expectedResponse, expectedContentType))
  }

  test("GET /schema/schemaId returns not found") {
    val expectedStatusCode  = Status.NotFound
    val expectedResponse    = ErrorResponse.notFound(Action.downloadSchema, SchemaId("schemaId"))
    val expectedContentType = "application/json"

    val response = testResources(Map.empty).use { service =>
      for {
        uri <- Uri.fromString("/schema/schemaId").toOption.getOrThrow
        request = Request[IO](method = Method.GET, uri = uri)
        response <- service.orNotFound.run(request)
      } yield response
    }

    val test = for {
      result      <- response
      body        <- result.as[NotFoundResponse]
      contentType <- result.headers.get(CIString("content-type")).getOrThrow
    } yield (result.status, body, contentType.head.value)
    test.assertEquals((expectedStatusCode, expectedResponse, expectedContentType))

  }

  test("GET /schema/schemaId returns schema") {
    val expectedStatusCode  = Status.Ok
    val expectedResponse    = """{}"""
    val expectedContentType = "application/json"

    val response = testResources(Map(SchemaId("schemaId") -> Json.obj())).use { service =>
      for {
        uri <- Uri.fromString("/schema/schemaId").toOption.getOrThrow
        request = Request[IO](method = Method.GET, uri = uri)
        response <- service.orNotFound.run(request)
      } yield response
    }

    val test = for {
      result      <- response
      body        <- result.as[String]
      contentType <- result.headers.get(CIString("content-type")).getOrThrow
    } yield (result.status, body, contentType.head.value)
    test.assertEquals((expectedStatusCode, expectedResponse, expectedContentType))

  }

  def testResources(initialValues: Map[SchemaId, Json]): Resource[IO, HttpRoutes[IO]] =
    for {
      ref <- Resource.eval(Ref[IO].of(initialValues))
      repo                    = new FakeSchemaRepo(ref)
      schemaService           = new SchemaService[IO](repo)
      service: HttpRoutes[IO] = Router("/" -> new SchemaApi[IO](schemaService).routes)
    } yield service
}
