
<%@page import="com.sloca.entity.User"%>
<%@page import="com.sloca.model.UserDAO"%>
<%
    String loginUser = (String) session.getAttribute("user");
    UserDAO userdm = new UserDAO();
    User user = userdm.retrieve(loginUser);
    String name = "";
    if (user != null) {
        name = user.getName();
    }
%>
<div class="navigation-bar bg-darkCobalt">
    <div class="navigation-bar-content container">
        <nav class="horizontal-menu">
            <a class="element" href="user.jsp"><img src="images/icon.png" width="25"/> <b>SLOCA</b>     <sup>G4T8</sup></a>
            <span class="element-divider"></span>

            <a class="pull-menu" href="#"></a>
            <ul class="element-menu">
                <li>
                    <a href="heatmapPage.jsp">Heatmap</a>
                </li>
                <li>
                    <a class="dropdown-toggle" href="#">Basic Location Reports</a>
                    <ul class="dropdown-menu" data-show="hover" data-role="dropdown">
                        <li><a href="report_breakdown_page.jsp">Breakdown by year and gender</a></li>
                        <li><a href="top_k_next_places.jsp">Top K Next Places</a></li>
                        <li><a href="top_k_companions_page.jsp">Top K Companions</a></li>
                        <li><a href="top_k_popular_places_page.jsp">Top K Popular Places</a></li>
                    </ul>
                </li>
                <li>
                    <a class="dropdown-toggle" href="#">Group Reports</a>
                    <ul class="dropdown-menu" data-show="hover" data-role="dropdown">
                        <li><a href="autoGroupDetection.jsp">Automatic Group Detection</a></li>
                        <li><a href="group_popular.jsp">Group Popular Places</a></li>
                        <li><a href="group_next.jsp">Group Next Places</a></li>
                    </ul>
                </li>

            </ul><a class="element place-right" href="logout.jsp">Logout</a>
            <span class="element-divider place-right"></span>
            <button class="element image-button image-left place-right">
                <%=name%>
            </button>
        </nav>
    </div>

</div>