<%--

Copyright (c) 2012, Ooyala, Inc.
All rights reserved.
Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
•    Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
•    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation 
    and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

--%>

<%@page import="com.day.cq.wcm.api.WCMMode" %>
<%@page session="false" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %>
<%@include file="/libs/foundation/global.jsp" %>
<sling:defineObjects/>

<%
    final String videoIdProperty = "videoid";
    final String playerIdProperty = "playerID";
    final String widthProperty = "width";
    final String heightProperty = "height";
    final String autoPlayProperty = "autoPlay";

    if (currentNode != null) {
        final boolean isEditMode = (WCMMode.fromRequest(request) == WCMMode.EDIT);
        final String videoId = currentNode.hasProperty(videoIdProperty) ? currentNode.getProperty(videoIdProperty).getString() : null;
        final String playerId = currentNode.hasProperty(playerIdProperty) ? currentNode.getProperty(playerIdProperty).getString() : null;
        final String width = currentNode.hasProperty(widthProperty) ? currentNode.getProperty(widthProperty).getString() : null;
        final String height = currentNode.hasProperty(heightProperty) ? currentNode.getProperty(heightProperty).getString() : null;
        final boolean autoPlay = currentNode.hasProperty(autoPlayProperty) ? currentNode.getProperty(autoPlayProperty).getBoolean() : false;

        pageContext.setAttribute("isEditMode", isEditMode);
        pageContext.setAttribute("videoid", videoId);
        pageContext.setAttribute("playerID", playerId);
        pageContext.setAttribute("width", width);
        pageContext.setAttribute("height", height);
        pageContext.setAttribute("videoDivID", videoId + "asdf");
        pageContext.setAttribute("autoPlay", autoPlay);
    }

    // response.addHeader("Access-Control-Allow-Origin", "*");
    // response.addHeader("Access-Control-Allow-Credentials", "true");
%>

<%-- ONLY IN AUTHOR MODE --%>
<c:choose>
    <c:when test="${isEditMode}">
        <p>
            <strong>Drag video here to replace.</strong>
        </p>

        <ul>
            <c:if test="${empty videoid}">
                <li>Video Not Provided.</li>
            </c:if>

            <c:if test="${empty playerID}">
                <li>Player ID Not Provided.</li>
            </c:if>
        </ul>
    </c:when>
    <c:otherwise></c:otherwise>
</c:choose>

<%-- BOTH AUTHOR AND PUBLISH --%>
<c:choose>
    <c:when test="${ (!empty videoid) && (!empty playerID) }">
        <div id="${ videoDivID }" class='ooyalaVideoDiv' style="
        <c:if test="${not empty height }">height:${ height }px; </c:if>
            <c:if test="${not empty width  }">width: ${ width  }px;</c:if>">
            <script type="text/javascript" src="http://player.ooyala.com/v3/${playerID}"></script>
            <script type="text/javascript">
                var videoPlayer = OO.Player.create('${ videoDivID }', '${videoid}', {
                    <c:if test="${not empty height }">height: '${ height }px', </c:if>
                    <c:if test="${not empty width  }">width: '${ width  }px', </c:if>
                });
                <c:if test="${autoPlay}">
                videoPlayer.play();
                </c:if>
            </script>
        </div>
    </c:when>
</c:choose>