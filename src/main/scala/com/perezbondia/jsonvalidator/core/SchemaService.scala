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

package com.perezbondia.jsonvalidator.core

import cats.effect.Sync
import cats.implicits._

import io.circe._
import io.circe.parser.parse

import com.perezbondia.jsonvalidator.core.domain.model.InvalidJson
import com.perezbondia.jsonvalidator.core.domain.model.OtherError
import com.perezbondia.jsonvalidator.core.domain.model.SchemaError
import com.perezbondia.jsonvalidator.core.domain.model.SchemaId
import com.perezbondia.jsonvalidator.core.domain.model.SchemaIdInUse

final class SchemaService[F[_]: Sync](schemaRepo: SchemaRepo[F]) {
  def registerSchema(schemaId: SchemaId, inputSchema: String): F[Either[SchemaError, Unit]] =
    // TODO: We can validate after checking that schemaId doesn't exists
    for {
      parsedJson <- validateJsonSchema(inputSchema)
      res <- parsedJson match {
        case Left(value) => Sync[F].pure(Left(value))
        case Right(json) =>
          schemaRepo.storeSchema(schemaId, json).map(Right(_)).handleErrorWith {
            case t: SchemaIdInUse => Sync[F].pure(Left(t))
            case t: Throwable     => Sync[F].pure(Left(OtherError(t.getMessage())))
          }
      }
    } yield res

  def retrieveSchema(schemaId: SchemaId): F[Option[Json]] = schemaRepo.retrieveSchema(schemaId)

  private[core] def validateJsonSchema(inputSchema: String): F[Either[InvalidJson, Json]] =
    Sync[F].delay(parse(inputSchema).left.map(r => InvalidJson(r.show)))
}
