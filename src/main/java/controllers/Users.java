package controllers;

import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import server.Main;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

@Path("users/")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)

public class Users{
    @POST
    @Path("login")
    public String UsersLogin(@FormDataParam("email") String email, @FormDataParam("password") String password) {
        System.out.println("Invoked loginUser() on path users/login");
        try {
            PreparedStatement ps1 = Main.db.prepareStatement("SELECT password FROM Users WHERE email = ?");
            ps1.setString(1, email);
            ResultSet loginResults = ps1.executeQuery();
            if (loginResults.next() == true) {
                String correctPassword = loginResults.getString(1);
                if (password.equals(correctPassword)) {
                    String token = UUID.randomUUID().toString();
                    PreparedStatement ps2 = Main.db.prepareStatement("UPDATE Users SET token = ? WHERE email = ?");
                    ps2.setString(1, token);
                    ps2.setString(2, email);
                    ps2.executeUpdate();
                    JSONObject userDetails = new JSONObject();
                    userDetails.put("email", email);
                    userDetails.put("token", token);
                    return userDetails.toString();
                } else {
                    return "{\"Error\": \"Incorrect password!\"}";
                }
            } else {
                return "{\"Error\": \"Incorrect username.\"}";
            }
        } catch (Exception exception) {
            System.out.println("Database error during /users/login: " + exception.getMessage());
            return "{\"Error\": \"Server side error!\"}";
        }
    }

    @POST
    @Path("logout")
    public static String logout(@CookieParam("token") String token){
        try{
            System.out.println("users/logout "+ token);
            PreparedStatement ps = Main.db.prepareStatement("SELECT userId FROM Users WHERE token = ?");
            ps.setString(1, token);
            ResultSet logoutResults = ps.executeQuery();
            if (logoutResults.next()){
                int userId = logoutResults.getInt(1);
                //Set the token to null to indicate that the user is not logged in
                PreparedStatement ps1 = Main.db.prepareStatement("UPDATE Users SET token = NULL WHERE userId = ?");
                ps1.setInt(1, userId);
                ps1.executeUpdate();
                return "{\"status\": \"OK\"}";
            } else {
                return "{\"error\": \"Invalid token!\"}";
            }
        } catch (Exception ex) {
            System.out.println("Database error during /users/logout: " + ex.getMessage());
            return "{\"error\": \"Server side error!\"}";
        }
    }

    @GET
    @Path("list") //Read method to read from the database, 'curl -s localhost:8081/users/list' in git bash (ATM only displays userId and name)
    public String UsersList() {
        System.out.println("Invoked Users.UsersList()");
        JSONArray response = new JSONArray();
        try {
            PreparedStatement ps = Main.db.prepareStatement("SELECT userId, name FROM Users");
            ResultSet results = ps.executeQuery();
            while (results.next()==true) {
                JSONObject row = new JSONObject();
                row.put("userId", results.getInt(1));
                row.put("name", results.getString(2));
                response.add(row);
            }
            return response.toString();
        } catch (Exception exception) {
            System.out.println("Database error: " + exception.getMessage());
            return "{\"Error\": \"Unable to list items.  Error code xx.\"}";
        }
    }

    @GET
    @Path("get/{userId}") //Get one record from the database, 'curl -s localhost:8081/users/get/1' (1 is example of UserId) in git bash (Currently returns the name of the userId entered)
    public String GetUser(@PathParam("userId") Integer userId) {
        System.out.println("Invoked Users.GetUser() with userId " + userId);
        try {
            PreparedStatement ps = Main.db.prepareStatement("SELECT name FROM Users WHERE userId = ?");
            ps.setInt(1, userId);
            ResultSet results = ps.executeQuery();
            JSONObject response = new JSONObject();
            if (results.next()== true) {
                response.put("userId", userId);
                response.put("name", results.getString(1));
            }
            return response.toString();
        } catch (Exception exception) {
            System.out.println("Database error: " + exception.getMessage());
            return "{\"Error\": \"Unable to get item, please see server console for more info.\"}";
        }
    }

    @POST
    @Path("add") //Create method to create a new record in the database, 'curl -s localhost:8081/users/add -F name='Pascal8' -F password='YesNo1' -F subMaths=True -F subCompSci=True -F subPhysics=True' for example in git bash
    public String UsersAdd(@FormDataParam("name") String name, @FormDataParam("password") String password, @FormDataParam("email") String email,
                           @FormDataParam("subMaths") Boolean subMaths, @FormDataParam("subCompSci") Boolean subCompSci, @FormDataParam("subPhysics") Boolean subPhysics
                            , @FormDataParam("admin") Boolean admin){
        System.out.println("Invoked Users.UsersAdd()");
        try {
            PreparedStatement ps = Main.db.prepareStatement("INSERT INTO Users (name, password, email, subMaths, subCompSci, subPhysics, admin) VALUES (?, ?, ?, ?, ?, ?, ?");
            ps.setString(1, name);
            ps.setString(2, password);
            ps.setString(3, email);
            ps.setBoolean(4, subMaths);
            ps.setBoolean(5, subCompSci);
            ps.setBoolean(6, subPhysics);
            ps.setBoolean(7, admin);
            ps.execute();
            return "{\"OK\": \"Added user. (∩─ᗝ─)⊃━☆ﾟ.*☆ﾟ *Bing*\"}";
        } catch (Exception exception) {
            System.out.println("Database error: " + exception.getMessage());
            return "{\"Error\": \"Unable to create new item, please see server console for more info.\"}";
        }

    }

    @POST
    @Path("update") //Update method to update an existing record in the database, 'curl -s -X POST localhost:8081/users/update -F UserID=8 -F UserName='Toast' for example in git bash
    public String updateFood(@FormDataParam("userId") Integer userId, @FormDataParam("name") String name) {
        try {
            System.out.println("Invoked Users.UpdateUsers/update userId=" + userId);
            PreparedStatement ps = Main.db.prepareStatement("UPDATE Users SET name = ? WHERE userId = ?");
            ps.setString(1, name);
            ps.setInt(2, userId);
            ps.execute();
            return "{\"OK\": \"Users updated. (∩─ᗝ─)⊃━☆ﾟ.*☆ﾟ Magic.\"}";
        } catch (Exception exception) {
            System.out.println("Database error: " + exception.getMessage());
            return "{\"Error\": \"Unable to update item, please see server console for more info.\"}";
        }
    }

    @POST
    @Path("delete/{UserID}") //Delete method to delete a record from the database, 'curl -s -X POST localhost:8081/users/delete/8' (8 is example of userId) in git bash

    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public String DeleteUser(@PathParam("UserID") Integer UserID) throws Exception {
        System.out.println("Invoked Users.DeleteUser()");
        if (UserID == null) {
            throw new Exception("UserID is missing in the HTTP request's URL.");
        }
        try {
            PreparedStatement ps = Main.db.prepareStatement("DELETE FROM Users WHERE userId = ?");
            ps.setInt(1, UserID);
            ps.execute();
            return "{\"OK\": \"User deleted. (∩─ᗝ─)⊃━☆ﾟ.*☆ﾟ *Poof*\"}";
        } catch (Exception exception) {
            System.out.println("Database error: " + exception.getMessage());
            return "{\"Error\": \"Unable to delete item, please see server console for more info.\"}";
        }
    }
}