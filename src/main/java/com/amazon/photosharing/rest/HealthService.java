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

import com.amazon.photosharing.facade.HealthFacade;
import com.amazon.photosharing.iface.ServiceFacade;

import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class HealthService extends ServiceFacade {

    private HealthFacade _facade;

    public HealthService() {
        super();
    }

    @PostConstruct
    private void init() {
        _facade = new HealthFacade();
    }

    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHealth() {

        if (!_facade.isHealthy())
            return Response.serverError().build();

        return Response.ok().build();
    }
}
