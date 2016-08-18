package uk.ac.ncl.openlab.intake24.services.fooddb.admin

import uk.ac.ncl.openlab.intake24.AssociatedFood
import uk.ac.ncl.openlab.intake24.AssociatedFoodWithHeader
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.DatabaseError
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.UpdateError
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.LocalLookupError
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.CreateError
import uk.ac.ncl.openlab.intake24.services.fooddb.errors.LocalDependentCreateError

trait AssociatedFoodsAdminService {
  
  def getAssociatedFoodsWithHeaders(foodCode: String, locale: String): Either[LocalLookupError, Seq[AssociatedFoodWithHeader]]
  def updateAssociatedFoods(foodCode: String, locale: String, associatedFoods: Seq[AssociatedFood]): Either[LocalLookupError, Unit]
  
  def deleteAllAssociatedFoods(locale: String): Either[DatabaseError, Unit]
  def createAssociatedFoods(assocFoods: Map[String, Seq[AssociatedFood]], locale: String): Either[LocalDependentCreateError, Unit]
}
