package uk.ac.ncl.openlab.intake24.foodsql.admin

import anorm._
import anorm.SqlParser.str
import anorm.NamedParameter.symbol
import uk.ac.ncl.openlab.intake24.foodsql.SqlDataService
import uk.ac.ncl.openlab.intake24.FoodGroupRecord
import uk.ac.ncl.openlab.intake24.FoodGroupMain
import uk.ac.ncl.openlab.intake24.FoodGroupLocal
import uk.ac.ncl.openlab.intake24.services.fooddb.admin.FoodGroupsAdminService
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.DatabaseError
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.RecordNotFound
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.LookupError
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.LocaleError
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.LocalLookupError
import uk.ac.ncl.openlab.intake24.foodsql.SqlResourceLoader
import uk.ac.ncl.openlab.intake24.foodsql.FirstRowValidation
import uk.ac.ncl.openlab.intake24.foodsql.FirstRowValidationClause
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.UndefinedLocale
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.CreateError
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.DuplicateCode

trait FoodGroupsAdminImpl extends FoodGroupsAdminService with SqlDataService with FirstRowValidation with SqlResourceLoader {
  private case class FoodGroupRow(id: Long, description: String, local_description: Option[String])

  private val parser = Macro.namedParser[FoodGroupRow]

  private lazy val listFoodGroupsQuery = sqlFromResource("admin/list_food_groups.sql")

  def listFoodGroups(locale: String): Either[LocaleError, Map[Int, FoodGroupRecord]] = tryWithConnection {
    implicit conn =>
      val result = SQL(listFoodGroupsQuery).on('locale_id -> locale).executeQuery()

      parseWithLocaleValidation(result, parser.+)(Seq(FirstRowValidationClause("id", Right(List())))).right
        .map {
          _.map {
            r => (r.id.toInt, FoodGroupRecord(FoodGroupMain(r.id.toInt, r.description), FoodGroupLocal(r.local_description)))
          }.toMap
        }
  }

  private lazy val getFoodGroupQuery = sqlFromResource("admin/get_food_group.sql")

  def getFoodGroup(id: Int, locale: String): Either[LocalLookupError, FoodGroupRecord] = tryWithConnection {
    implicit conn =>
      val result = SQL(getFoodGroupQuery).on('id -> id, 'locale_id -> locale).executeQuery()

      val validation: Seq[FirstRowValidationClause[LocalLookupError, FoodGroupRow]] = Seq(FirstRowValidationClause("locale_id", Left(UndefinedLocale)), FirstRowValidationClause[LocalLookupError, FoodGroupRow]("id", Left(RecordNotFound)))

      parseWithFirstRowValidation(result, validation, parser.single)
        .right
        .map {
          r => FoodGroupRecord(FoodGroupMain(r.id.toInt, r.description), FoodGroupLocal(r.local_description))
        }
  }

  def deleteAllFoodGroups(): Either[DatabaseError, Unit] = tryWithConnection {
    implicit conn =>
      SQL("DELETE FROM food_groups").execute()
      Right(())
  }

  def createFoodGroups(foodGroups: Seq[FoodGroupMain]): Either[CreateError, Unit] = tryWithConnection {
    implicit conn =>
      if (foodGroups.nonEmpty) {

        tryWithConstraintCheck("food_groups_id_pk", DuplicateCode) {
          val foodGroupParams = foodGroups.map(g => Seq[NamedParameter]('id -> g.id, 'description -> g.englishDescription))

          batchSql("""INSERT INTO food_groups VALUES ({id}, {description})""", foodGroupParams).execute()
        }
      } else
        Right(())
  }

  def deleteLocalFoodGroups(locale: String): Either[DatabaseError, Unit] = tryWithConnection {
    implicit conn =>
      SQL("DELETE FROM food_groups_locale WHERE locale_id={locale_id}")
        .on('locale_id -> locale)
        .execute()

      Right(())
  }

  def createLocalFoodGroups(localFoodGroups: Map[Int, String], locale: String): Either[DatabaseError, Unit] = tryWithConnection {
    implicit conn =>

      val foodGroupLocalParams = localFoodGroups.map {
        case (id, localDescription) =>
          Seq[NamedParameter]('id -> id, 'locale_id -> locale, 'local_description -> localDescription)
      }.toSeq

      batchSql("""INSERT INTO food_groups_local VALUES ({id}, {locale_id}, {local_description})""", foodGroupLocalParams).execute()

      Right(())
  }

}