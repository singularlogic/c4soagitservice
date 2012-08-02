<%-- 
    Document   : index
    Created on : Dec 25, 2011, 10:35:36 PM
    Author     : pgouvas
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%
    if (session.getAttribute("userid")==null){
        response.sendRedirect("jsfs/login.jsf");
    } else {
        response.sendRedirect("jsfs/initial.jsf");
    }
%>