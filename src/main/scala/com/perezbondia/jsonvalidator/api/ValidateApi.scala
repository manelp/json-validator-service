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

import cats.effect.kernel.Async
import cats.effect.kernel.Sync
import cats.implicits._

import io.circe.Json
import org.http4s.HttpRoutes
import sttp.model._
import sttp.tapir.Codec.PlainCodec
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.http4s.Http4sServerInterpreter

import com.perezbondia.jsonvalidator.api.model._
import com.perezbondia.jsonvalidator.core.SchemaService
import com.perezbondia.jsonvalidator.core.domain.model.SchemaId

final class ValidateApi[F[_]: Async]() {

  private val validateJson: HttpRoutes[F] =
    Http4sServerInterpreter[F]().toRoutes(ValidateApi.validateJsonEndpoint.serverLogic { (schemaId, json) =>
      Async[F].pure(ErrorResponse.badRequest(Action.validateDocument, "not implemented yet").asLeft[SuccessResponse])
    })

  val routes: HttpRoutes[F] = validateJson
}

object ValidateApi {

  given PlainCodec[SchemaId] = Codec.string.mapDecode(x => DecodeResult.Value(SchemaId(x)))(_.toString)

  private val baseEndpoint: Endpoint[Unit, SchemaId, Unit, Unit, Any] = endpoint
    .in("validate")
    .in(path[SchemaId]("schemaId"))
    .errorOut(header(Header.contentType(MediaType.ApplicationJson)))
    .out(header(Header.contentType(MediaType.ApplicationJson)))

  val validateJsonEndpoint: Endpoint[Unit, (SchemaId, String), BadRequestResponse | Unit, SuccessResponse, Any] =
    baseEndpoint.post
      .in(stringJsonBody)
      .out(jsonBody[SuccessResponse])
      .errorOutVariant(
        oneOfVariant(jsonBody[BadRequestResponse].description("invalid request"))
      )
      .description(
        "Validates the json body with the schema uploaded on :schemaId"
      )

  val endpoints = List(validateJsonEndpoint)
}
