package uk.ac.ncl.openlab.intake24.sql.tools.food

import org.rogach.scallop.ScallopConf
import org.slf4j.LoggerFactory
import uk.ac.ncl.openlab.intake24.sql.migrations.{DatabaseError, MigrationFailed, MigrationsImpl}
import uk.ac.ncl.openlab.intake24.sql.tools._

object MigrateFoodDatabase extends App with DatabaseConnection with WarningMessage {

  val logger = LoggerFactory.getLogger(getClass)

  val options = new ScallopConf(args) {
    val dbConfigDir = opt[String](required = true)
  }

  options.verify()

  val databaseConfig = DatabaseConfigChooser.chooseDatabaseConfiguration(options.dbConfigDir())

  displayWarningMessage(s"Warning: this will change the format of ${databaseConfig.host}/${databaseConfig.database}. Are you sure that is what you want?")

  val dataSource = getDataSource(databaseConfig)

  val migrations = new MigrationsImpl(dataSource)

  migrations.applyMigrations(uk.ac.ncl.openlab.intake24.foodsql.migrations.FoodDatabaseMigrations.activeMigrations) match {
    case Left(MigrationFailed(e)) => throw e
    case Left(DatabaseError(e)) => throw e.exception
    case Right(()) => {}
  }
}
