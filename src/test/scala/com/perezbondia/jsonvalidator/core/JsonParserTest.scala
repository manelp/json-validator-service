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

import io.circe.Json
import munit.CatsEffectSuite

import com.perezbondia.jsonvalidator.core.domain.model.InvalidJson

class JsonParserTest extends CatsEffectSuite {

  test("parses valid json") {
    val test = JsonParser.validateJsonSchema[IO]("{}")
    test.assertEquals(Right(Json.obj()))
  }

  test("returns InvalidJson if json invalid") {
    val test = JsonParser.validateJsonSchema[IO]("{invalid})")

    test.assertEquals(Left(InvalidJson("Invalid JSON. ParsingFailure: expected \" got 'invali...' (line 1, column 2)")))
  }
}
