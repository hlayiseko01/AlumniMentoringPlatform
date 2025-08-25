/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.ump.scms.dict312.studentmanagementsystem.resources;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 *
 * @author nhlay
 */
@Path("user")
public class user {
    
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response createUser(@FormParam("username") String username,
                               @FormParam("password") String password) {

        String msg = "<h1>User Received:</h1>" +
                     "<p>Username: " + username + "</p>" +
                     "<p>Password: " + password + "</p>";

        return Response.ok(msg).build();
    }
    
}
