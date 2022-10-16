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

package com.perezbondia.jsonvalidator.infra

import cats.effect.IO
import cats.effect.kernel.Ref

import io.circe.Json

import com.perezbondia.jsonvalidator.core.SchemaRepo
import com.perezbondia.jsonvalidator.core.domain.model.SchemaId
import com.perezbondia.jsonvalidator.core.domain.model.SchemaIdInUse

class FakeSchemaRepo(ref: Ref[IO, Map[SchemaId, Json]]) extends SchemaRepo[IO] {

  override def storeSchema(schemaId: SchemaId, jsonSchema: Json): IO[Unit] =
    for {
      r <- ref.get
      _ <- IO.whenA(r.get(schemaId).isDefined)(IO.raiseError(SchemaIdInUse(schemaId)))
      _ <- ref.update(m => m.updated(schemaId, jsonSchema))
    } yield ()

  override def retrieveSchema(schemaId: SchemaId): IO[Option[Json]] = ref.get.map(_.get(schemaId))

}
