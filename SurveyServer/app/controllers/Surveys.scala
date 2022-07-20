package controllers

import javax.inject.Inject
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import play.api.{Configuration, Logger}
import views.html.surveys.{Survey, SurveyFeedback}

import scala.concurrent.duration._

case class ErrorReportingSettings(reportSurveyState: Boolean, reportStackTrace: Boolean)

case class PublicSurveyParameters(localeId: String, respondentLanguageId: String, supportEmail: String, originatingURL: Array[String],
                                  errorReporting: ErrorReportingSettings)

class Surveys @Inject()(config: Configuration, ws: WSClient) extends Controller {

  private val internalApiBaseUrl = config.getString("intake24.internalApiBaseUrl").get
  private val externalApiBaseUrl = config.getString("intake24.externalApiBaseUrl").get

  private val privacyPolicyURL = config.getString("intake24.survey.privacyPolicyURL").get
  private val termsAndConditionsURL = config.getString("intake24.survey.termsAndConditionsURL").get
  private val displayLogos = config.getBoolean("intake24.survey.displayLogos").get
  private val displayCookieConsent: Boolean = Option(config.getBoolean("intake24.survey.displayCookieConsent").get).getOrElse(true)

  private val gaTrackingCode = config.getString("intake24.ga.trackingCode")

  private implicit val errorReportingReads = Json.reads[ErrorReportingSettings]

  private implicit val surveyParamReads = Json.reads[PublicSurveyParameters]

  def survey(surveyId: String, genUser: Option[String]) = Action.async {

    val request = ws.url(s"$internalApiBaseUrl/surveys/$surveyId/public-parameters").withRequestTimeout(30 seconds)


    request.get.map {
      response =>
        response.status match {
          case 200 => {

            val additionalCss = config.getOptional[Seq[String]](s"intake24.survey.${surveyId}.additionalCss").getOrElse(Seq())

            Json.fromJson[PublicSurveyParameters](Json.parse(response.body)) match {
              case JsSuccess(params, _) => Ok(Survey(surveyId, params, externalApiBaseUrl, privacyPolicyURL,
                termsAndConditionsURL, displayLogos, displayCookieConsent, gaTrackingCode, additionalCss))
              case JsError(p) =>
                Logger.error("Could not parse API server public survey parameters response")
                Logger.error("Response body: " + response.body)
                Logger.error("Error: " + p.toString)
                InternalServerError
            }
          }
          case code =>
            Logger.error(
              s"""API request ${request.method} ${request.url} failed
                 |The unexpected response was $code with the following body:
                 |${response.body}
                 |Forwarding the $code response to the user.""".stripMargin)
            Status(code)
        }
    }
  }

  def surveyFeedbackPage(surveyId: String) = Action {
    Ok(SurveyFeedback(externalApiBaseUrl, surveyId, gaTrackingCode))
  }

}
