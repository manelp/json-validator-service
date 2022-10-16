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

import com.typesafe.config.ConfigFactory
import com.perezbondia.jsonvalidator.config._
import com.perezbondia.jsonvalidator.types._
import munit._
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.FlywayException
import pureconfig._

import scala.util.Try
import com.perezbondia.jsonvalidator.infra.db.TransactorResource
import cats.effect.kernel.Resource
import cats.effect.IO
import java.util.UUID
import io.circe.Json
import com.perezbondia.jsonvalidator.core.domain.model.SchemaId
import io.circe.parser.parse

final class PostgresSchemaRepoTest extends DbCatsEffectSuite {

  override def munitFixtures = List(db)

  val testResources: Resource[IO, PostgresSchemaRepo[IO]] =
    for {
      transactor <- TransactorResource.resource(dbConfig)
    } yield new PostgresSchemaRepo(transactor)

  test("stores and retreives a new schema") {
    testResources.use { repo =>
      val jsonSchema = parse("{}").toOption
      val test = for {
        schemaId   <- IO(UUID.randomUUID()).map(id => SchemaId(id.toString))
        jsonSchema <- IO.fromOption(jsonSchema)(new Error("unable to parse"))
        _          <- repo.storeSchema(schemaId, jsonSchema)
        res        <- repo.retrieveSchema(schemaId)
      } yield res
      test.assertEquals(jsonSchema)
    }

  }
}
