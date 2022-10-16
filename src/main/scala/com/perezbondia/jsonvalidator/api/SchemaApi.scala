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

import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.Http4sServerInterpreter
import cats.effect.kernel.Async
import com.perezbondia.jsonvalidator.core.SchemaService
import com.perezbondia.jsonvalidator.api.model._
import sttp.model.StatusCode
import com.perezbondia.jsonvalidator.Greetings
import cats.effect.kernel.Sync
import sttp.model._
import sttp.tapir._
import cats.implicits._
import sttp.tapir.CodecFormat.TextPlain
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import io.circe.Json

final class SchemaApi[F[_]: Async](schemaService: SchemaService[F]) {
  private val postSchema: HttpRoutes[F] =
    Http4sServerInterpreter[F]().toRoutes(SchemaApi.postSchemaEndpoint.serverLogic { _ =>
      Sync[F].delay(ErrorResponse(Action.UploadSchema, ResourceId.ConfigSchema, ResponseStatus.Error, "not implemented").asLeft[Unit])
    })

  private val getSchema: HttpRoutes[F] =
    Http4sServerInterpreter[F]().toRoutes(SchemaApi.getSchemaEndpoint.serverLogic { _ =>
      Sync[F].delay(ErrorResponse(Action.UploadSchema, ResourceId.ConfigSchema, ResponseStatus.Error, "not implemented").asLeft[String])
    })

  val routes: HttpRoutes[F] = postSchema <+> getSchema
}

object SchemaApi {

  val postSchemaEndpoint: Endpoint[Unit, (String, String), ErrorResponse, Unit, Any] =
    endpoint.post
      .in("schema")
      .in(path[String]("schemaId"))
      .in(stringBody)
      .errorOut(jsonBody[ErrorResponse])
      .description(
        "Registers a new JSON schema with the given :schemaId"
      )

  val getSchemaEndpoint: Endpoint[Unit, String, ErrorResponse, String, Any] =
    endpoint.get
      .in("schema")
      .in(path[String]("schemaId"))
      .errorOut(jsonBody[ErrorResponse])
      .out(stringJsonBody)
      .description(
        "Registers a new JSON schema with the given :schemaId"
      )
}
