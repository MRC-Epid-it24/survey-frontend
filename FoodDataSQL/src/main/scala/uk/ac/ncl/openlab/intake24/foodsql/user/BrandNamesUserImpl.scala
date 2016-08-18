package uk.ac.ncl.openlab.intake24.foodsql.user

import uk.ac.ncl.openlab.intake24.services.fooddb.user.BrandNamesService
import uk.ac.ncl.openlab.intake24.foodsql.SqlDataService
import anorm._
import anorm.SqlParser.str
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.LocalLookupError
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.RecordNotFound
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.UndefinedLocale
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.DatabaseError
import uk.ac.ncl.openlab.intake24.foodsql.FirstRowValidationClause
import uk.ac.ncl.openlab.intake24.foodsql.FirstRowValidation
import uk.ac.ncl.openlab.intake24.foodsql.SqlResourceLoader

trait BrandNamesUserImpl extends BrandNamesService with SqlDataService with FirstRowValidation with SqlResourceLoader {

  private lazy val getBrandNamesQuery = sqlFromResource("user/get_brand_names_frv.sql")

  def getBrandNames(foodCode: String, locale: String): Either[LocalLookupError, Seq[String]] = tryWithConnection {
    implicit conn =>
      val result = SQL(getBrandNamesQuery).on('food_code -> foodCode, 'locale_id -> locale).executeQuery()

      parseWithLocaleAndFoodValidation(result, SqlParser.str("name").+)(Seq(FirstRowValidationClause("name", Right(List()))))
  }
}
