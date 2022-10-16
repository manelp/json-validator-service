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

import cats.effect.kernel.Sync
import cats.implicits._

import collection.JavaConverters._
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.report.LogLevel
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.JsonSchemaFactory
import io.circe.Json

import com.perezbondia.jsonvalidator.core.domain.model.FailedValidation
import com.perezbondia.jsonvalidator.core.domain.model.JsonDocument
import com.perezbondia.jsonvalidator.core.domain.model.JsonSchema
import com.perezbondia.jsonvalidator.core.domain.model.SchemaId

final class ValidateService[F[_]: Sync]() {

  def validateDocument(schema: JsonSchema, document: JsonDocument): F[Either[FailedValidation, Unit]] =
    for {
      schemaValidator <- Sync[F].delay(JsonLoader.fromString(schema.value.noSpaces))
      jsonDocument    <- Sync[F].delay(JsonLoader.fromString(document.value.deepDropNullValues.noSpaces))
      schema          <- Sync[F].delay(JsonSchemaFactory.byDefault().getJsonSchema(schemaValidator))
      validationRes   <- Sync[F].delay(schema.validate(jsonDocument))
      res =
        if (validationRes.isSuccess()) Right(())
        else
          Left(
            FailedValidation(
              validationRes
                .iterator()
                .asScala
                .filter(_.getLogLevel().equals(LogLevel.ERROR))
                .map(_.getMessage())
                .mkString(" | ")
            )
          )
    } yield res

}
