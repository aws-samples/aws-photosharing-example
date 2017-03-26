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

import com.amazon.photosharing.dao.Album;
import com.amazon.photosharing.dao.Media;
import com.amazon.photosharing.facade.AlbumFacade;
import com.amazon.photosharing.iface.ServiceFacade;
import com.amazon.photosharing.model.SelectModel;
import com.amazon.photosharing.rest.vo.AlbumResult;
import com.amazon.photosharing.rest.vo.MediaResult;
import com.amazon.photosharing.security.filters.Secured;
import org.apache.http.HttpStatus;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/albums")
public class AlbumService extends ServiceFacade {
    @Context
    ServletContext _context;
    @Context
    HttpServletRequest _request;

    private AlbumFacade _facade;

    @PostConstruct
    private void init() {
        _facade = new AlbumFacade();
    }

    @GET
    @Path("/{albumId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response getAlbum(@PathParam("albumId") Long albumId) {
        Album album =_facade.findAlbum(albumId);

        AlbumResult albumResult = new AlbumResult(album);

        return Response.status(HttpStatus.SC_OK).entity(albumResult).build();
    }

    @DELETE
    @Path("/{albumId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response deleteAlbum(@PathParam("albumId") Long albumId) {
        List<Long> albumList = new ArrayList();
        albumList.add(albumId);
        _facade.deleteAlbums(albumList, true);

        return Response.status(HttpStatus.SC_OK).build();
    }

    @PUT
    @Path("/{mediaId}/{albumId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response addMediaToAlbum(@PathParam("mediaId") Long mediaId,
                                    @PathParam("albumId") Long albumId) {
        SelectModel model = new SelectModel();
        model.put(mediaId, true);

        _facade.addToAlbum(albumId, model);

        return Response.status(HttpStatus.SC_OK).build();
    }

}
