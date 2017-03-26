/*
 * Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package com.amazon.photosharing.rest;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.amazon.photosharing.rest.vo.UserResult;
import com.amazon.photosharing.security.filters.Secured;

import com.amazon.photosharing.dao.User;
import com.amazon.photosharing.facade.UserFacade;
import com.amazon.photosharing.iface.ServiceFacade;

@Path("/users")
public class UserService extends ServiceFacade {

    @Context ServletContext _context;    
    @Context HttpServletRequest _request;
    
    private UserFacade _facade;
    
    @PostConstruct
    private void init() {
    	_facade = new UserFacade();    	
    }
    
    @POST
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON)    
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response register(@FormParam("username") String p_username, @FormParam("password") String p_password, @FormParam("email") String p_email) {    	
    	return Response.ok(_facade.register(new User(p_username, p_password, p_email))).build();
    }
    
    @GET
    @Secured
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)   
    public Response getUserById(@PathParam("userId") Long userId) {
        User user = _facade.findUser(userId);

        if (userId.equals(user.getId())) {
            UserResult userResult = new UserResult(user);
            return Response.ok(userResult).build();
        } else {
            return Response.ok("Not allowed to look up userId " + userId).build();
        }
    }

}
