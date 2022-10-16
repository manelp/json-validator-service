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

import com.perezbondia.jsonvalidator.core.domain.model.SchemaId

object model {

  enum Action {
    case uploadSchema     extends Action
    case downloadSchema   extends Action
    case validateDocument extends Action
  }

  enum ResourceId {
    case `config-schema` extends ResourceId
  }

  enum ResponseStatus {
    case success extends ResponseStatus
    case error   extends ResponseStatus
  }

  trait ApiResponse {
    val id: ResourceId
    val status: ResponseStatus
  }

  final case class SuccessResponse(id: ResourceId, action: Action, status: ResponseStatus) extends ApiResponse
  object SuccessResponse {
    def apply(action: Action): SuccessResponse =
      new SuccessResponse(ResourceId.`config-schema`, action, ResponseStatus.success)
  }

  sealed trait ErrorResponse extends ApiResponse

  final case class BadRequestResponse(id: ResourceId, action: Action, status: ResponseStatus, message: String)
      extends ErrorResponse
  final case class NotFoundResponse(id: ResourceId, action: Action, status: ResponseStatus, message: String)
      extends ErrorResponse
  object ErrorResponse {
    def notFound(action: Action, schemaId: SchemaId): NotFoundResponse =
      new NotFoundResponse(ResourceId.`config-schema`, action, ResponseStatus.error, s"schema $schemaId not found")
    def badRequest(action: Action, message: String): BadRequestResponse =
      new BadRequestResponse(ResourceId.`config-schema`, action, ResponseStatus.error, message)
  }

  // Codecs

  // TODO: Use a generic solution for scala3 enum
  given Codec[Action] =
    Codec.from(Decoder.decodeString.map(Action.valueOf), (a: Action) => Encoder.encodeString(a.toString))
  given Codec[ResourceId] =
    Codec.from(Decoder.decodeString.map(ResourceId.valueOf), (a: ResourceId) => Encoder.encodeString(a.toString))
  given Codec[ResponseStatus] =
    Codec.from(
      Decoder.decodeString.map(ResponseStatus.valueOf),
      (a: ResponseStatus) => Encoder.encodeString(a.toString)
    )

  given Codec[BadRequestResponse] = deriveCodec[BadRequestResponse]
  given Codec[NotFoundResponse]   = deriveCodec[NotFoundResponse]
  given Codec[SuccessResponse]    = deriveCodec[SuccessResponse]

}
