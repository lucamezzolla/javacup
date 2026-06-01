package io.cutalab.javacup.dashboard.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.cutalab.javacup.core.AppInfo;

public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
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

        addToNavbar(toggle, branding);
    }

    private void createDrawer() {
        SideNav navigation = new SideNav();

        navigation.addItem(new SideNavItem("Dashboard", MainView.class));
        navigation.addItem(new SideNavItem("Processes", ProcessesView.class));
        navigation.addItem(new SideNavItem("Current JVM Metrics", CurrentJvmMetricsView.class));
        navigation.addItem(new SideNavItem("Metric Samples", MetricSamplesView.class));

        Scroller scroller = new Scroller(navigation);
        addToDrawer(scroller);
    }
}
