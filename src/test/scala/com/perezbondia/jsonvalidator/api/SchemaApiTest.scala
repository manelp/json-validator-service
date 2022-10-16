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

import cats.implicits._
import cats.effect._
import com.perezbondia.jsonvalidator.types._
import munit._
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._
import org.http4s.server.Router
import com.perezbondia.jsonvalidator.core.SchemaService
import com.perezbondia.jsonvalidator.test.TestHelpers
import com.perezbondia.jsonvalidator.api.model._

class SchemaApiTest extends CatsEffectSuite {

  given EntityDecoder[IO, ErrorResponse] = jsonOf

  test("POST /schema/schemaId returns bad request") {
    val expectedStatusCode = Status.BadRequest
    val expectedResponse = ErrorResponse(Action.UploadSchema, ResourceId.ConfigSchema, ResponseStatus.Error, "not implemented")

    val response = for {
      uri <- TestHelpers.unsafeGet(Uri.fromString("/schema/schemaId").toOption)
      schemaService           = new SchemaService[IO]()
      service: HttpRoutes[IO] = Router("/" -> new SchemaApi[IO](schemaService).routes)
      request = Request[IO](
        method = Method.POST,
        uri = uri
      )
      response <- service.orNotFound.run(request)
    } yield response

    val test = for {
      result <- response
      body   <- result.as[ErrorResponse]
    } yield (result.status, body)
    test.assertEquals((expectedStatusCode, expectedResponse))

  }


  test("GET /schema/schemaId returns bad request") {
    val expectedStatusCode = Status.BadRequest
    val expectedResponse = ErrorResponse(Action.UploadSchema, ResourceId.ConfigSchema, ResponseStatus.Error, "not implemented")

    val response = for {
      uri <- TestHelpers.unsafeGet(Uri.fromString("/schema/schemaId").toOption)
      schemaService           = new SchemaService[IO]()
      service: HttpRoutes[IO] = Router("/" -> new SchemaApi[IO](schemaService).routes)
      request = Request[IO](
        method = Method.GET,
        uri = uri
      )
      response <- service.orNotFound.run(request)
    } yield response

    val test = for {
      result <- response
      body   <- result.as[ErrorResponse]
    } yield (result.status, body)
    test.assertEquals((expectedStatusCode, expectedResponse))

  }
}
