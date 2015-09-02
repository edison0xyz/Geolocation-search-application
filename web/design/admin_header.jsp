<%            String loginUser = (String) session.getAttribute("user");
%>
<div class="navigation-bar bg-darkCobalt">
    <div class="navigation-bar-content container">
        <nav class="horizontal-menu compact">
            <a class="element" href="user.jsp"><img src="images/icon.png" width="25"/> <b>SLOCA</b>     <sup>G4T8</sup></a>
            <span class="element-divider"></span>

            <a class="pull-menu" href="#"></a>


            <a class="element place-right" href="logout.jsp">Logout</a>
            <span class="element-divider place-right"></span>
            <button class="element image-button image-left place-right">
                Admin
            </button>
        </nav>
    </div>

</div>