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

package com.perezbondia.jsonvalidator.infra.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import cats.effect.kernel.Resource
import com.perezbondia.jsonvalidator.config.DatabaseConfig
import scala.concurrent.duration._
import cats.effect.IO
import doobie.util.transactor.Transactor
import doobie.util.ExecutionContexts

object TransactorResource {

  private def hikariDataSource(dbConfig: DatabaseConfig) = {
    // https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby
    val hikariConfig = new HikariConfig()
    hikariConfig.setDriverClassName(dbConfig.driver.toString)
    hikariConfig.setJdbcUrl(dbConfig.url.toString)
    hikariConfig.setUsername(dbConfig.user.toString)
    hikariConfig.setPassword(dbConfig.pass.toString)
    hikariConfig.setMaximumPoolSize(16)
    hikariConfig.setMaxLifetime(50.seconds.toMillis)
    hikariConfig.setPoolName("jsonvalidator-pool")
    hikariConfig.setConnectionInitSql("set time zone 'UTC'")
    new HikariDataSource(hikariConfig)
  }

  // https://tpolecat.github.io/doobie/docs/14-Managing-Connections.html
  def resource(
      config: DatabaseConfig
  ): Resource[IO, Transactor[IO]] =
    for {
      dataSource   <- Resource.fromAutoCloseable(IO(hikariDataSource(config)))
      connectionEC <- ExecutionContexts.fixedThreadPool[IO](16)
    } yield Transactor
      .fromDataSource[IO]
      .apply(dataSource = dataSource, connectEC = connectionEC)
}
