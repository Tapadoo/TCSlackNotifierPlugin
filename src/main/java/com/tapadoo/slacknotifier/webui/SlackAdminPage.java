package com.tapadoo.slacknotifier.webui;

import com.tapadoo.slacknotifier.SlackConfigProcessor;
import jetbrains.buildServer.controllers.admin.AdminPage;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.PositionConstraint;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Created by jasonconnery on 02/03/2014.
 */
public class SlackAdminPage extends AdminPage {

    private final SlackConfigProcessor configProcesser;

    public SlackAdminPage(PagePlaces pagePlaces, PluginDescriptor descriptor , SlackConfigProcessor configProcessor) {
        super(pagePlaces);
        setPluginName("slackNotifier");
        setIncludeUrl(descriptor.getPluginResourcesPath("/admin/slackAdminPage.jsp"));
        setTabTitle("Slack Notifier");
        setPosition(PositionConstraint.after("clouds", "email", "jabber"));
        register();

        this.configProcesser = configProcessor ;
    }

    @Override
    public boolean isAvailable(HttpServletRequest request) {
        return super.isAvailable(request) && checkHasGlobalPermission(request, Permission.CHANGE_SERVER_SETTINGS);
    }

    public String getGroup() {
        return INTEGRATIONS_GROUP;
    }

    @Override
    public void fillModel(Map<String, Object> model, HttpServletRequest request) {
        super.fillModel(model, request);

        model.put("defaultChannel" , configProcesser.getDefaultChannel());
        model.put("logoUrl" , configProcesser.getLogoUrl());
        model.put("postUrl" , configProcesser.getPostUrl());
    }
}
