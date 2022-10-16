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
import cats.effect.kernel.Resource

import munit.CatsEffectSuite

import com.perezbondia.jsonvalidator.core.domain.model.FailedValidation
import com.perezbondia.jsonvalidator.core.domain.model.JsonDocument
import com.perezbondia.jsonvalidator.core.domain.model.JsonSchema

class ValidateServiceTest extends CatsEffectSuite {

  test("validates valid document") {
    val test = testResources.use { service =>
      for {
        schema   <- validSchemaJson
        document <- validDocumentJson
        res      <- service.validateDocument(JsonSchema(schema), JsonDocument(document))
      } yield res
    }
    test.assertEquals(Right(()))
  }
  test("validates invalid document") {
    val test = testResources.use { service =>
      for {
        schema   <- validSchemaJson
        document <- invalidDocumentJson
        res      <- service.validateDocument(JsonSchema(schema), JsonDocument(document))
      } yield res
    }
    test.assertEquals(Left(FailedValidation("object has missing required properties ([\"destination\",\"source\"])")))
  }
  test("fails gracefully when trying to validate with an invalid schema".ignore) {
    val test = testResources.use { service =>
      for {
        schema   <- invalidSchemaJson
        document <- validDocumentJson
        res      <- service.validateDocument(JsonSchema(schema), JsonDocument(document))
      } yield res
    }
    test.assertEquals(())
  }

  private val validSchema = """
                            |{
                            |"$schema": "http://json-schema.org/draft-04/schema#",
                            |"type": "object",
                            |"properties": {
                            |  "source": {
                            |    "type": "string"
                            |  },
                            |  "destination": {
                            |    "type": "string"
                            |  },
                            |  "timeout": {
                            |    "type": "integer",
                            |    "minimum": 0,
                            |    "maximum": 32767
                            |  },
                            |  "chunks": {
                            |    "type": "object",
                            |    "properties": {
                            |      "size": {
                            |        "type": "integer"
                            |      },
                            |      "number": {
                            |        "type": "integer"
                            |      }
                            |    },
                            |    "required": ["size"]
                            |  }
                            |},
                            |"required": ["source", "destination"]
                            |} """.stripMargin

  private val validSchemaJson = IO.fromEither(io.circe.parser.parse(validSchema))
  private val invalidSchema = """
                            |{
                            |"$schema": "http://json-schema.org/draft-04/schema#",
                            |"type": "object",
                            |"properties": {
                            |  "source": {
                            |    "type": "any"
                            |  },
                            |  "destination": {
                            |    "type": "string"
                            |  }
                            |},
                            |"required": ["source", "destination"]
                            |} """.stripMargin

  private val invalidSchemaJson = IO.fromEither(io.circe.parser.parse(validSchema))

  private val validDocument = """{
                                | "source": "/home/alice/image.iso",
                                | "destination": "/mnt/storage",
                                | "timeout": null,
                                | "chunks": {
                                | "size": 1024,
                                | "number": null
                                | }
                                |}""".stripMargin

  private val invalidDocument = """{
                                | "timeout": null,
                                | "chunks": {
                                | "size": 1024,
                                | "number": null
                                | }
                                |}""".stripMargin

  private val validDocumentJson   = IO.fromEither(io.circe.parser.parse(validDocument))
  private val invalidDocumentJson = IO.fromEither(io.circe.parser.parse(invalidDocument))

  def testResources: Resource[IO, ValidateService[IO]] = Resource.eval(IO(new ValidateService()))
}
