/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
  */
 package com.hlayiseko.AlumniMentoring.rest;
 
 import jakarta.annotation.security.DeclareRoles;
 import jakarta.enterprise.context.ApplicationScoped;
 import jakarta.security.enterprise.authentication.mechanism.http.FormAuthenticationMechanismDefinition;
 import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
 import jakarta.ws.rs.ApplicationPath;
 import jakarta.ws.rs.core.Application;
 
 @FormAuthenticationMechanismDefinition(
         loginToContinue = @LoginToContinue(
                 loginPage = "/login.html",
                 errorPage = "/login.html?error=true",
                 useForwardToLogin = false
         )
 )
 @DeclareRoles({"STUDENT", "ADMIN", "ALUMNI"})
 @ApplicationScoped
 @ApplicationPath("/resources")
 public class Resourceconfig extends Application {
     // No methods needed — this activates Form Auth globally
 }
