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

import io.circe._
import io.circe.generic.semiauto._

object model {

  enum Action(val value: String) {
    case UploadSchema   extends Action("uploadSchema")
    case ValidateSchema extends Action("validateSchema")
    case DownloadSchema extends Action("downloadSchema")
  }

  enum ResourceId(val value: String) {
    case ConfigSchema extends ResourceId("config-schema")
  }

  enum ResponseStatus(val value: String) {
    case Success extends ResponseStatus("success")
    case Error   extends ResponseStatus("error")
  }

  final case class ErrorResponse(action: Action, id: ResourceId, status: ResponseStatus, message: String)

  // TODO: Use a generic solution for scala3 enum
  given Codec[Action] = Codec.from(Decoder.decodeString.map(Action.valueOf), (a: Action) => Encoder.encodeString(a.toString))
  given Codec[ResourceId] = Codec.from(Decoder.decodeString.map(ResourceId.valueOf), (a: ResourceId) => Encoder.encodeString(a.toString))
  given Codec[ResponseStatus] = Codec.from(Decoder.decodeString.map(ResponseStatus.valueOf), (a: ResponseStatus) => Encoder.encodeString(a.toString))

  given Codec[ErrorResponse] = deriveCodec[ErrorResponse]

}
