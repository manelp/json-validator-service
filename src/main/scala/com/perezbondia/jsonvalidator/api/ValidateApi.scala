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
import cats.implicits._

import io.circe.Json
import org.http4s.HttpRoutes
import sttp.model._
import sttp.tapir.Codec.PlainCodec
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.server.http4s.Http4sServerInterpreter

import com.perezbondia.jsonvalidator.api.model.Action
import com.perezbondia.jsonvalidator.api.model.BadRequestResponse
import com.perezbondia.jsonvalidator.api.model.ErrorResponse
import com.perezbondia.jsonvalidator.api.model.NotFoundResponse
import com.perezbondia.jsonvalidator.api.model.SuccessResponse
import com.perezbondia.jsonvalidator.core.JsonParser
import com.perezbondia.jsonvalidator.core.SchemaService
import com.perezbondia.jsonvalidator.core.ValidateService
import com.perezbondia.jsonvalidator.core.domain.model.FailedValidation
import com.perezbondia.jsonvalidator.core.domain.model.JsonDocument
import com.perezbondia.jsonvalidator.core.domain.model.JsonSchema
import com.perezbondia.jsonvalidator.core.domain.model.SchemaId

final class ValidateApi[F[_]: Async](schemaService: SchemaService[F], validateService: ValidateService[F]) {

  private val validateJson: HttpRoutes[F] =
    Http4sServerInterpreter[F]().toRoutes(ValidateApi.validateJsonEndpoint.serverLogic { (schemaId, json) =>
      for {
        schema     <- schemaService.retrieveSchema(schemaId)
        parsedJson <- JsonParser.validateJsonSchema(json)
        res <- parsedJson match {
          case Left(error) => Async[F].pure(Left(ErrorResponse.badRequest(schemaId, Action.validateDocument, error.message)))
          case Right(jsonDocument) =>
            schema.fold(
              Async[F].pure(Left(ErrorResponse.notFound(schemaId, Action.validateDocument)))
            ) { jsonSchema =>
              validateService.validateDocument(JsonSchema(jsonSchema), JsonDocument(jsonDocument)).map {
                case Right(_)    => Right(SuccessResponse(schemaId, Action.validateDocument))
                case Left(error) => Left(ErrorResponse.badRequest(schemaId, Action.validateDocument, error.message))
              }
            }
        }
      } yield res

    })

  val routes: HttpRoutes[F] = validateJson
}

object ValidateApi {

  given PlainCodec[SchemaId] = Codec.string.mapDecode(x => DecodeResult.Value(SchemaId(x)))(_.toString)

  private val baseEndpoint: PublicEndpoint[SchemaId, Unit, Unit, Any] = endpoint
    .in("validate")
    .in(path[SchemaId]("schemaId"))
    .errorOut(header(Header.contentType(MediaType.ApplicationJson)))
    .out(header(Header.contentType(MediaType.ApplicationJson)))

  val validateJsonEndpoint: PublicEndpoint[(SchemaId, String), ErrorResponse, SuccessResponse, Any] =
    baseEndpoint.post
      .in(stringJsonBody)
      .out(jsonBody[SuccessResponse])
      .errorOut(
        oneOf[ErrorResponse](
          oneOfVariant(StatusCode.BadRequest, jsonBody[BadRequestResponse].description("invalid request")),
          oneOfVariant(StatusCode.NotFound, jsonBody[NotFoundResponse].description("not found"))
        )
      )
      .description(
        "Validates the json body with the schema uploaded on :schemaId"
      )

  val endpoints = List(validateJsonEndpoint)
}
