package guice

import javax.inject.Inject
import com.google.inject.Singleton
import play.twirl.api.Html
import play.api.Configuration
import views.html.info.newcastle.{Footer, PrivacyContent, TermsContent}

@Singleton
class NewcastleCustomContent @Inject()(config: Configuration) extends CustomContent {
  private val deploymentLogoDiv = config.getString("intake24.deploymentLocaleFooterLogo")

  override def footer(): Html = Footer(deploymentLogoDiv)
  override def privacy(): Html = PrivacyContent()
  override def termsAndConditions(): Html = TermsContent()
}
