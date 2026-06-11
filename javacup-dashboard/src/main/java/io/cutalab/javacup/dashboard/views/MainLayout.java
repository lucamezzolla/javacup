package io.cutalab.javacup.dashboard.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.cutalab.javacup.core.AppInfo;

public class MainLayout extends AppLayout {

    private Button themeToggle;

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        attachEvent.getUI()
                .getPage()
                .executeJs("return localStorage.getItem('javacup.theme')")
                .then(String.class, theme -> applyTheme("dark".equals(theme)));
    }

    private void createHeader() {
        DrawerToggle toggle = new DrawerToggle();

        H1 title = new H1(AppInfo.NAME);
        title.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.NONE
        );

        Span coffeeIcon = new Span("☕");
        coffeeIcon.getStyle()
                .set("font-size", "1rem")
                .set("line-height", "1")
                .set("margin-left", "0.35rem");

        HorizontalLayout titleRow = new HorizontalLayout(title, coffeeIcon);
        titleRow.setPadding(false);
        titleRow.setSpacing(false);
        titleRow.setAlignItems(HorizontalLayout.Alignment.CENTER);
        titleRow.getStyle()
                .set("gap", "0.15rem")
                .set("line-height", "1");

        Span subtitle = new Span("Local-first JVM memory diagnostics");
        subtitle.addClassNames(
                LumoUtility.FontSize.SMALL,
                LumoUtility.TextColor.SECONDARY
        );

        VerticalLayout branding = new VerticalLayout(titleRow, subtitle);
        branding.setPadding(false);
        branding.setSpacing(false);
        branding.getStyle()
                .set("gap", "0.15rem")
                .set("line-height", "1.1");

        themeToggle = new Button("Dark", event -> toggleTheme());
        themeToggle.getElement().setAttribute("aria-label", "Toggle light and dark theme");
        themeToggle.addClassNames(LumoUtility.Margin.Left.AUTO);

        addToNavbar(toggle, branding, themeToggle);
    }

    private void createDrawer() {
        SideNav navigation = new SideNav();

        navigation.addItem(new SideNavItem("Dashboard", MainView.class));
        navigation.addItem(new SideNavItem("Processes", ProcessesView.class));
        navigation.addItem(new SideNavItem("Current JVM Metrics", CurrentJvmMetricsView.class));
        navigation.addItem(new SideNavItem("Metric Samples", MetricSamplesView.class));
        navigation.addItem(new SideNavItem("Archived reports", ArchivedReportsView.class));
        navigation.addItem(new SideNavItem("Local Lab", LocalLabView.class));
        navigation.addItem(new SideNavItem("Guide", GuideView.class));
        navigation.addItem(new SideNavItem("Donate", DonationsView.class));

        Scroller scroller = new Scroller(navigation);
        scroller.getStyle().set("padding-top", "15px");

        addToDrawer(scroller);
    }

    private void toggleTheme() {
        UI ui = UI.getCurrent();

        if (ui == null) {
            return;
        }

        boolean currentlyDark = ui.getElement().getThemeList().contains(Lumo.DARK);
        boolean nextDark = !currentlyDark;

        applyTheme(nextDark);
        ui.getPage().executeJs("localStorage.setItem('javacup.theme', $0)", nextDark ? "dark" : "light");
    }

    private void applyTheme(boolean dark) {
        UI ui = UI.getCurrent();

        if (ui == null) {
            return;
        }

        if (dark) {
            ui.getElement().getThemeList().add(Lumo.DARK);
            if (themeToggle != null) {
                themeToggle.setText("Light");
            }
        } else {
            ui.getElement().getThemeList().remove(Lumo.DARK);
            if (themeToggle != null) {
                themeToggle.setText("Dark");
            }
        }
    }
}
