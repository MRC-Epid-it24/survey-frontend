package uk.ac.ncl.openlab.intake24.client.ui;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import uk.ac.ncl.openlab.intake24.client.BrowserConsole;
import uk.ac.ncl.openlab.intake24.client.CommonMessages;
import uk.ac.ncl.openlab.intake24.client.EmbeddedData;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class Layout {
    private static final Logger logger = Logger.getLogger(Layout.class.getName());

    private static final CommonMessages messages = CommonMessages.Util.getInstance();

    public static final RootPanel UIRootPanel = RootPanel.get("UIContainer");

    private static FlowPanel headerContainer;
    private static FlowPanel header;

    private static FlowPanel navbarContainer;
    private static FlowPanel navbar;
    private static HTMLPanel navbarUserInfo;
    private static HTMLPanel navbarLinks;

    private static FlowPanel mainContentContainer;
    private static FlowPanel mainContent;

    private static FlowPanel footerContainer;
    private static FlowPanel footer;
    private static HTMLPanel privacyAndTermsLinks = new HTMLPanel("ul", "");;

    // public static final

    public static final Anchor watchTutorial = WidgetFactory.createTutorialVideoLink();
    public static final Anchor logOut = new Anchor(SafeHtmlUtils.fromSafeConstant(messages.callbackRequestForm_success()));

    public static void setNavBar(List<Anchor> left, List<Anchor> right) {
        navbar.clear();

        navbarUserInfo = new HTMLPanel("ul", "");

        for (Anchor a : left) {
            HTMLPanel li = new HTMLPanel("li", "");
            li.add(a);
            navbarUserInfo.add(li);
        }

        navbarLinks = new HTMLPanel("ul", "");

        for (Anchor a : right) {
            HTMLPanel li = new HTMLPanel("li", "");
            li.add(a);
            navbarLinks.add(li);
        }

        navbar.add(navbarUserInfo);
        navbar.add(navbarLinks);
    }

    public static void setNavBarLinks(Anchor... links) {
        setNavBarLinks(Arrays.asList(links));
    }

    public static void setNavBarLinks(List<Anchor> links) {
        navbar.clear();

        navbarLinks = new HTMLPanel("ul", "");

        for (Anchor a : links) {
            HTMLPanel li = new HTMLPanel("li", "");
            li.add(a);
            navbarLinks.add(li);
        }

        navbar.add(navbarLinks);
    }

    public static void setFooterLinks(Widget... htmlElements) {
        for (Widget widget : htmlElements) {
            HTMLPanel panel = new HTMLPanel("li", "");
            panel.getElement().getStyle().setDisplay(Display.INLINE);
            panel.add(widget);
            privacyAndTermsLinks.add(panel);
        }
    }

    public static void setMainContent(Widget content) {
        mainContent.add(content);
    }

    public static FlowPanel getMainContentPanel() {
        return mainContent;
    }

    private static void onResize() {
        int windowHeight = Window.getClientHeight();

        int headerHeight = headerContainer.getOffsetHeight();
        int navbarHeight = navbarContainer.getOffsetHeight();
        int footerHeight = footerContainer.getOffsetHeight();

        int minHeight = windowHeight - headerHeight - navbarHeight - footerHeight;

        mainContent.getElement().getStyle().setProperty("minHeight", minHeight, Style.Unit.PX);
    }

    public static void createMainPageLayout() {
        headerContainer = new FlowPanel();
        headerContainer.addStyleName("intake24-header-container");

        header = new FlowPanel();
        header.addStyleName("intake24-header");
        header.add(new HTMLPanel("h1", "Intake24"));
        headerContainer.add(header);

        navbarContainer = new FlowPanel();
        navbarContainer.addStyleName("intake24-navbar-container");

        navbar = new FlowPanel();
        navbar.addStyleName("intake24-navbar");
        navbarContainer.add(navbar);

        mainContentContainer = new FlowPanel();
        mainContentContainer.addStyleName("intake24-main-content-container");

        mainContent = new FlowPanel();
        mainContent.addStyleName("intake24-main-content");
        mainContentContainer.add(mainContent);

        footerContainer = new FlowPanel();
        footerContainer.addStyleName("intake24-footer-container");

        footer = new FlowPanel();
        footer.addStyleName("intake24-footer");
        footerContainer.add(footer);


        FlowPanel privacyContainer = new FlowPanel();
        privacyContainer.addStyleName("intake24-footer-privacy");
        privacyAndTermsLinks.getElement().getStyle().setPaddingLeft(0, Style.Unit.PX);

        Anchor privacyLink = new Anchor(SafeHtmlUtils.fromSafeConstant(messages.privacyLinkLabel()));
        privacyLink.setHref(EmbeddedData.privacyPolicyUrl);
        privacyLink.setTarget("_blank");

        HTMLPanel spacer = new HTMLPanel("span", "|");
        spacer.getElement().getStyle().setMarginLeft(10, Style.Unit.PX);
        spacer.getElement().getStyle().setMarginRight(10, Style.Unit.PX);

        Anchor tocLink = new Anchor(SafeHtmlUtils.fromSafeConstant(messages.tocLinkLabel()));
        tocLink.setHref(EmbeddedData.termsAndConditionsUrl);
        tocLink.setTarget("_blank");

        Widget[] privacywidgets = { privacyLink, spacer, tocLink };

        setFooterLinks(privacywidgets);

        privacyContainer.add(privacyAndTermsLinks);

        if (EmbeddedData.displayLogos) {
            Anchor nuLogo = new Anchor();
            nuLogo.addStyleName("intake24-footer-nu-logo");
            nuLogo.setHref("https://openlab.ncl.ac.uk/");

            Anchor fssLogo = new Anchor();
            fssLogo.addStyleName("intake24-footer-fss-logo");
            fssLogo.setHref("http://www.foodstandards.gov.scot/");

            footer.add(nuLogo);
            footer.add(fssLogo);
        } else {
            privacyContainer.addStyleName("intake24-footer-privacy-no-logos");
        }

        footer.add(privacyContainer);

        UIRootPanel.clear();
        UIRootPanel.add(headerContainer);
        UIRootPanel.add(navbarContainer);
        UIRootPanel.add(mainContentContainer);
        UIRootPanel.add(footerContainer);

        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent resizeEvent) {

                Layout.onResize();
            }
        });

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                onResize();
            }
        });

        /* <div id="main-content-container">
        <div id="main-content" style="min-height: 697px;"><div class="intake24-survey-content-container"><div><iframe width="560" height="315" src="https://www.youtube.com/embed/5vB4NgI4ATc" frameborder="0" allowfullscreen=""></iframe></div><div><p style="font-size: 120%;">Welcome to Intake24!</p><p>We would like you to tell us everything you had to eat and drink yesterday. Please include all meals, snacks and drinks (including water and alcohol).</p><p>It may help you to think about what you did yesterday:</p><ul style="list-style-type: disc;"><li>What time did you wake up?</li><li>Were you at school, college, home, work?</li><li>Who were you with?</li><li>What time did you go to sleep?</li></ul></div><button type="button" class="gwt-Button intake24-button intake24-green-button">I am ready!</button></div></div>
	</div>*/
    }
}
