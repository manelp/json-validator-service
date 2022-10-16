package com.perezbondia.jsonvalidator.infra

import com.typesafe.config.ConfigFactory
import com.perezbondia.jsonvalidator.config._
import com.perezbondia.jsonvalidator.types._
import munit._
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.FlywayException
import pureconfig._

import scala.util.Try

trait DbCatsEffectSuite extends CatsEffectSuite {

  protected val dbConfig = ConfigSource
    .fromConfig(ConfigFactory.load(getClass().getClassLoader()))
    .at(DatabaseConfig.CONFIG_KEY.toString)
    .loadOrThrow[DatabaseConfig]

  protected val db = new Fixture[Unit]("database") {
    def apply(): Unit = ()
    override def beforeAll(): Unit = {
      val _        = Class.forName(dbConfig.driver.toString)
      val database = dbConfig.url.toString.split("/").reverse.take(1).mkString
      val connection = java.sql.DriverManager
        .getConnection(
          dbConfig.url.toString.replace(database, "template1"),
          dbConfig.user.toString,
          dbConfig.pass.toString
        )
      val statement = connection.createStatement()
      Try {
        statement.execute(s"""DROP DATABASE IF EXISTS "$database"""")
        statement.execute(s"""CREATE DATABASE "$database"""")
      } match {
        case scala.util.Failure(e) =>
          println(e.getMessage)
        case scala.util.Success(_) => // NOP
      }
      statement.close()
      connection.close()
    }

    override def beforeEach(context: BeforeEach): Unit = {
      val flyway: Flyway =
        Flyway
          .configure()
          .dataSource(dbConfig.url.toString, dbConfig.user.toString, dbConfig.pass.toString)
          .cleanDisabled(false)
          .load()
      val _ = flyway.migrate()
    }

    override def afterEach(context: AfterEach): Unit = {
      val flyway: Flyway =
        Flyway
          .configure()
          .dataSource(dbConfig.url.toString, dbConfig.user.toString, dbConfig.pass.toString)
          .cleanDisabled(false)
          .load()
      val _ = flyway.clean()
    }
  }


}
