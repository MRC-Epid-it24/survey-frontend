package uk.ac.ncl.openlab.intake24.foodsql.admin

import scala.Left
import scala.Right
import anorm.Macro
import anorm.NamedParameter.symbol
import anorm.SQL
import anorm.sqlToSimple
import uk.ac.ncl.openlab.intake24.AssociatedFood
import uk.ac.ncl.openlab.intake24.AssociatedFoodWithHeader
import uk.ac.ncl.openlab.intake24.CategoryHeader
import uk.ac.ncl.openlab.intake24.FoodHeader

import anorm.NamedParameter

import anorm.BatchSql
import org.postgresql.util.PSQLException

import uk.ac.ncl.openlab.intake24.foodsql.SqlDataService

import uk.ac.ncl.openlab.intake24.services.fooddb.admin.AssociatedFoodsAdminService
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.UpdateError
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.DatabaseError
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.UndefinedCode
import uk.ac.ncl.openlab.intake24.services.fooddb.admin.AssociatedFoodsAdminService
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.LocalFoodCodeError
import org.slf4j.LoggerFactory

trait AssociatedFoodsAdminImpl extends AssociatedFoodsAdminService with SqlDataService {

  private val logger = LoggerFactory.getLogger(classOf[AssociatedFoodsAdminImpl])

  private case class AssociatedFoodPromptsRow(
    associated_food_code: Option[String], food_english_description: Option[String], food_local_description: Option[String], food_do_not_use: Option[Boolean],
    associated_category_code: Option[String], category_english_description: Option[String], category_local_description: Option[String], category_is_hidden: Option[Boolean],
    text: Option[String], link_as_main: Option[Boolean], generic_name: Option[String])

  private val query =
    """|SELECT associated_food_code, f1.description as food_english_description, foods_local.local_description as food_local_description, foods_local.do_not_use as food_do_not_use,
       |  associated_category_code, c1.description as category_english_description, c1.is_hidden as category_is_hidden, categories_local.local_description as category_local_description, 
       |  text, link_as_main, generic_name
       |FROM foods
       |  LEFT JOIN associated_foods ON foods.code = associated_foods.food_code
	     |  LEFT JOIN foods as f1 ON associated_foods.associated_food_code = f1.code
       |  LEFT JOIN foods_local ON associated_foods.associated_food_code = foods_local.food_code AND foods_local.locale_id = {locale_id}
       |  LEFT JOIN categories as c1 ON associated_foods.associated_category_code = c1.code
	     |  LEFT JOIN categories_local ON associated_foods.associated_category_code = categories_local.category_code AND categories_local.locale_id = {locale_id}
       |WHERE
       |  foods.code = {food_code} AND (associated_foods.locale_id = {locale_id} OR associated_foods.locale_id IS NULL) 
       |ORDER BY id""".stripMargin

  def associatedFoods(foodCode: String, locale: String): Either[LocalFoodCodeError, Seq[AssociatedFoodWithHeader]] = tryWithConnection {
    implicit conn =>

      // TODO: change query to first row validation style

      val rows = SQL(query).on('food_code -> foodCode, 'locale_id -> locale).executeQuery().as(Macro.namedParser[AssociatedFoodPromptsRow].*)

      def mkPrompt(row: AssociatedFoodPromptsRow) = {
        val foodOrCategory: Either[FoodHeader, CategoryHeader] =
          if (row.food_english_description.nonEmpty)
            Left(FoodHeader(row.associated_food_code.get, row.food_english_description.get, row.food_local_description, row.food_do_not_use.get))
          else
            Right(CategoryHeader(row.associated_category_code.get, row.category_english_description.get, row.category_local_description, row.category_is_hidden.get))

        AssociatedFoodWithHeader(foodOrCategory, row.text.get, row.link_as_main.get, row.generic_name.get)
      }

      if (rows.isEmpty) // No such food in foods table
        Left(UndefinedCode)
      else if (rows.head.text.isEmpty) // All columns will be null if there are no matching associated food records, check
        // the first one that cannot be null 
        Right(Seq())
      else
        Right(rows.map(mkPrompt))
  }

  def updateAssociatedFoods(foodCode: String, locale: String, foods: Seq[AssociatedFood]): Either[UpdateError, Unit] = tryWithConnection {
    implicit conn =>
      conn.setAutoCommit(false)

      SQL("DELETE FROM associated_food_prompts WHERE food_code={food_code} AND locale_id={locale_id}")
        .on('food_code -> foodCode, 'locale_id -> locale)
        .execute()

      if (foods.nonEmpty) {

        val params = foods.flatMap {
          p =>

            val foodOption = p.foodOrCategoryCode.left.toOption
            val categoryOption = p.foodOrCategoryCode.right.toOption

            Seq[NamedParameter]('food_code -> foodCode, 'locale_id -> locale, 'associated_food_code -> foodOption,
              'associated_category_code -> categoryOption, 'text -> p.promptText, 'link_as_main -> p.linkAsMain, 'generic_name -> p.genericName)
        }

        BatchSql("INSERT INTO associated_food_prompts VALUES (DEFAULT, {food_code}, {locale_id}, {associated_category_code}, {associated_food_code}, {text}, {link_as_main}, {generic_name})", params)
          .execute()
      }

      conn.commit()

      Right(())
  }

  def deleteAllAssociatedFoods(): Either[DatabaseError, Unit] = tryWithConnection {
    implicit conn =>
      logger.info("Deleting existing associated food prompts")

      SQL("DELETE FROM associated_foods").execute()

      Right(())
  }

  def createAssociatedFoods(assocFoods: Map[String, Seq[AssociatedFood]], locale: String): Either[DatabaseError, Unit] = tryWithConnection {
    implicit conn =>
      if (!assocFoods.isEmpty) {
        conn.setAutoCommit(false)
        logger.info("Writing " + assocFoods.values.map(_.size).foldLeft(0)(_ + _) + " associated food prompts to database")

        val promptParams = assocFoods.flatMap {
          case (foodCode, foods) =>
            foods.flatMap {
              assocFood =>
                Seq[NamedParameter]('food_code -> foodCode, 'locale_id -> locale, 'associated_food_code -> assocFood.foodOrCategoryCode.left.toOption, 'associated_category_code -> assocFood.foodOrCategoryCode.right.toOption,
                  'text -> assocFood.promptText, 'link_as_main -> assocFood.linkAsMain, 'generic_name -> assocFood.genericName)
            }
        }.toSeq

        BatchSql("""INSERT INTO associated_foods VALUES (DEFAULT, {food_code}, {locale_id}, {associated_food_code}, {associated_category_code}, {text}, {link_as_main}, {generic_name})""", promptParams).execute()
        conn.commit()
        Right(())
      } else {
        logger.warn("createAssociatedFoods request with empty associated foods map")
        Right(())
      }

  }
}
