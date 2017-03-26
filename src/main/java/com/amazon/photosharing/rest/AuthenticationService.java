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

import com.amazon.photosharing.facade.TokenFacade;
import com.amazon.photosharing.facade.UserFacade;
import com.amazon.photosharing.iface.ServiceFacade;
import com.amazon.photosharing.utils.TokenGenerator;
import org.apache.http.HttpStatus;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/authentication")
public class AuthenticationService extends ServiceFacade {

    @Context
    HttpServletRequest _request;

    private UserFacade _facade;
    private TokenFacade _tokenFacade;

    @PostConstruct
    private void init() {
        _facade = new UserFacade();
        _tokenFacade = new TokenFacade();
    }

    @POST
    @Path("/authenticate")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response login(
            @FormParam("username") String p_username,
            @FormParam("password") String p_password) {

        if (_facade.login(p_username, p_password, _request)) {
            String token = TokenGenerator.getInstance().issueToken(p_username);

            _tokenFacade.storeToken(p_username, token);
            return Response.ok(token).build();
        }

        return Response.status(HttpStatus.SC_UNAUTHORIZED).build();
    }

}
