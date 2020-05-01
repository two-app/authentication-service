package db

import org.flywaydb.core.Flyway
import config.Config
import javax.sql.DataSource
import com.typesafe.scalalogging.Logger
import org.flywaydb.core.api.configuration.Configuration

object FlywayHelper {

  val logger: Logger = Logger("FlywayHelper")

  def migrate(): Unit = {
    Flyway
      .configure()
      .dataSource(
        DatabaseConfig.jdbc,
        DatabaseConfig.username,
        DatabaseConfig.password
      )
      .schemas(DatabaseConfig.schema)
      .defaultSchema(DatabaseConfig.schema)
      .locations("migration")
      .load()
      .migrate()
  }
}
