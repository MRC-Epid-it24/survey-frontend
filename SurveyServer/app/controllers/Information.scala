package controllers

import javax.inject.Inject
import _root_.info.PageNames
import guice.CustomContent
import play.api.Configuration
import play.api.mvc.{Action, Controller}
import play.twirl.api.Html
import views.html.info._


class Information @Inject()(customContent: CustomContent, config: Configuration) extends Controller {

  private val gaTrackingCode = config.getString("intake24.ga.trackingCode")
  private val supportEmail = config.getString("intake24.supportEmail").get
  private val videoURL = config.getString("intake24.videoURL").get
  private val infoPageLocaleDisclaimer = config.getString("intake24.infoPageLocaleDisclaimer")


  private def renderInfoPage(title: String, content: Html): Html = {
    InfoPageLayout(title, content, customContent.footer(), videoURL, gaTrackingCode, infoPageLocaleDisclaimer)
  }

  def landing = Action {
    Ok(renderInfoPage(PageNames.landing, LandingContent(supportEmail)))
  }

  def recall = Action {
    Ok(renderInfoPage(PageNames.recall, RecallContent()))
  }

  def features = Action {
    Ok(renderInfoPage(PageNames.features, FeaturesContent()))
  }

  def openSource = Action {
    Ok(renderInfoPage(PageNames.openSource, OpenSourceContent(supportEmail)))
  }

  def output = Action {
    Ok(renderInfoPage(PageNames.output, OutputContent()))
  }

  def validation = Action {
    Ok(renderInfoPage(PageNames.validation, ValidationContent()))
  }

  def localisation = Action {
    Ok(renderInfoPage(PageNames.localisation, LocalisationContent()))
  }

  def feedback = Action {
    Ok(renderInfoPage(PageNames.feedback, FeedbackContent()))
  }

  def publications = Action {
    Ok(renderInfoPage(PageNames.publications, PublicationsContent()))
  }

  def contacts = Action {
    Ok(renderInfoPage(PageNames.contacts, ContactsContent(supportEmail)))
  }

  def privacy = Action {
    Ok(renderInfoPage(PageNames.privacy, customContent.privacy()))
  }

  def terms = Action {
    Ok(renderInfoPage(PageNames.terms, customContent.termsAndConditions()))
  }

}
