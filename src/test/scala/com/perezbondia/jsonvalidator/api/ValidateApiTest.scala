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

class ValidateApiTest extends CatsEffectSuite {

  given EntityDecoder[IO, BadRequestResponse] = jsonOf
  given EntityDecoder[IO, SuccessResponse]    = jsonOf

  test("GET /validate/schemaId returns bad request") {
    val expectedStatusCode  = Status.BadRequest
    val expectedResponse    = ErrorResponse.badRequest(Action.validateDocument, "not implemented yet")
    val expectedContentType = "application/json"

    val response = testResources().use { service =>
      for {
        uri <- Uri.fromString("/validate/schemaId").toOption.getOrThrow
        request = Request[IO](method = Method.POST, uri = uri)
        response <- service.orNotFound.run(request)
      } yield response
    }

    val test = for {
      result      <- response
      body        <- result.as[BadRequestResponse]
      contentType <- result.headers.get(CIString("content-type")).getOrThrow
    } yield (result.status, body, contentType.head.value)
    test.assertEquals((expectedStatusCode, expectedResponse, expectedContentType))

  }

  def testResources(): Resource[IO, HttpRoutes[IO]] =
    for {
      // ref <- Ref[IO].of(Map.empty[SchemaId, Json])
      // repo                    = new FakeSchemaRepo(ref)
      // schemaService           = new SchemaService[IO](repo)
      service: HttpRoutes[IO] <- Resource.eval(IO(Router("/" -> new ValidateApi[IO]().routes)))
    } yield service
}
