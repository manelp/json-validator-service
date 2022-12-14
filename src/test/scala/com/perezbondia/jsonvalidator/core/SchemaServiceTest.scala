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

import cats.effect.IO
import cats.effect.Resource
import cats.effect.kernel.Ref

import io.circe._
import io.circe.literal._
import munit.CatsEffectSuite

import com.perezbondia.jsonvalidator.core.domain.model.InvalidJson
import com.perezbondia.jsonvalidator.core.domain.model.SchemaId
import com.perezbondia.jsonvalidator.core.domain.model.SchemaIdInUse
import com.perezbondia.jsonvalidator.infra.FakeSchemaRepo

class SchemaServiceTest extends CatsEffectSuite {

  def testResource(initialValues: Map[SchemaId, Json]): Resource[IO, SchemaService[cats.effect.IO]] =
    for {
      ref <- Resource.eval(Ref[IO].of(initialValues))
      repo = new FakeSchemaRepo(ref)
    } yield new SchemaService[IO](repo)

  test("registerSchema json") {
    val test = testResource(Map.empty).use(service => service.registerSchema(SchemaId("schemaId"), Json.obj()))
    test.assertEquals(Right(()))
  }

  test("register existing schema returns SchemaIdInUse") {
    val schemaId = SchemaId("schemaId")
    val test = testResource(Map(schemaId -> Json.obj())).use(service => service.registerSchema(schemaId, Json.obj()))

    test.assertEquals(Left(SchemaIdInUse(schemaId)))
  }

  test("retrieve existing schema") {
    val schemaId = SchemaId("schemaId")
    val test     = testResource(Map(schemaId -> Json.obj())).use(service => service.retrieveSchema(schemaId))

    test.assertEquals(Some(Json.obj()))
  }
}
