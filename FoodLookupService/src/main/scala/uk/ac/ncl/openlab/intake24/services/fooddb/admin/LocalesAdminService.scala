package uk.ac.ncl.openlab.intake24.services.fooddb.admin

import uk.ac.ncl.openlab.intake24.Locale
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.DatabaseError
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.LocaleError
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.CreateError
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.UpdateError
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.LookupError
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.DeleteError

trait LocalesAdminService {
  def listLocales(): Either[DatabaseError, Map[String, Locale]]
  def getLocale(id: String): Either[LookupError, Locale]
  def createLocale(data: Locale): Either[CreateError, Unit]
  def updateLocale(id: String, data: Locale): Either[UpdateError, Unit]
  def deleteLocale(id: String): Either[DeleteError, Unit]
}
