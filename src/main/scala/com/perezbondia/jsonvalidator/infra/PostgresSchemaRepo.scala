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

import cats.effect.kernel.MonadCancelThrow
import cats.implicits._

import doobie.implicits._
import doobie.postgres.circe.json.implicits._
import doobie.util.meta.Meta
import doobie.util.transactor.Transactor
import io.circe.Json

import com.perezbondia.jsonvalidator.core.SchemaRepo
import com.perezbondia.jsonvalidator.core.domain.model.SchemaId

class PostgresSchemaRepo[F[_]: MonadCancelThrow](transactor: Transactor[F]) extends SchemaRepo[F] {

  given Meta[SchemaId] = Meta[String].timap(SchemaId.apply)(_.toString)

  override def storeSchema(schemaId: SchemaId, jsonSchema: Json): F[Unit] = {
    val insertQuery = sql"""insert into json_schemas(schema_id,json_schema) values ($schemaId, $jsonSchema)"""
    insertQuery.update.run.transact(transactor).void
  }

  override def retrieveSchema(schemaId: SchemaId): F[Option[Json]] = {
    val selectQuery = sql"""select json_schema from json_schemas where schema_id = $schemaId"""
    selectQuery.query[Json].option.transact(transactor)
  }

}
