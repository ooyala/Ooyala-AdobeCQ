<%--
  Copyright 1997-2009 Day Management AG
  Barfuesserplatz 6, 4001 Basel, Switzerland
  All Rights Reserved.

  This software is the confidential and proprietary information of
  Day Management AG, ("Confidential Information"). You shall not
  disclose such Confidential Information and shall use it only in
  accordance with the terms of the license agreement you entered into
  with Day.

  ==============================================================================

  Atom feed renderer for cq:PageContent nodes

  Draws the content of the current page as a feed, listing its child pages as feed entries.

--%><%@ page session="false" %><%
%><%@ page import="com.day.cq.wcm.api.Page,
                   com.day.cq.commons.Externalizer,
                   java.util.Iterator,
                   com.day.cq.wcm.api.WCMMode,
                   com.day.cq.commons.ProductInfoService,
                   com.day.cq.commons.ProductInfo" %><%
%><%@ taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %><%
%><%@ taglib prefix="cq" uri="http://www.day.com/taglibs/cq/1.0" %><%
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%
%><%@ taglib prefix="atom" uri="http://sling.apache.org/taglibs/atom/1.0" %><%
%><cq:defineObjects /><%

    try {
        WCMMode.DISABLED.toRequest(request);
        ProductInfo pi = sling.getService(ProductInfoService.class).getInfo();

        Externalizer externalizer = sling.getService(Externalizer.class);
        String feedUrl = externalizer.absoluteLink(slingRequest, slingRequest.getScheme(), currentPage.getContentResource().getPath()) + ".feed";
        String url = externalizer.absoluteLink(slingRequest, slingRequest.getScheme(), currentPage.getPath());

        String link = url + ".html";
        String title = currentPage.getTitle() !=null ?
                currentPage.getTitle() : currentNode.getName();
        String subTitle = currentPage.getDescription() != null ?
                currentPage.getDescription() : (String)properties.get("jcr:description", null);
        String genUri = pi.getUrl();
        String genName = pi.getName();
        String genVersion = pi.getShortVersion();

        %><atom:feed id="<%= url %>"><%
            %><atom:title><c:out value="<%= title %>"/></atom:title><%
            if (subTitle != null) {
                %><atom:subtitle><c:out value="<%= subTitle %>"/></atom:subtitle><%
            }
        %><atom:link href="<%= feedUrl %>" rel="self"/><%
        %><atom:link href="<%= link %>"/><%
        %><atom:generator uri="<%= genUri %>" version="<%= genVersion%>"><%= genName %></atom:generator><%


        Iterator<Page> i = currentPage.listChildren();
        while (i.hasNext()) {
            Page p = i.next();
            pageContext.setAttribute("p", p);
            %><sling:include path="${p.path}.feedentry"/><%
        }

        %></atom:feed><%

    } catch (Exception e) {
        log.error("error rendering feed for page", e);
    }
%>