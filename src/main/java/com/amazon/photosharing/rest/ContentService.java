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

import com.amazon.photosharing.dao.Comment;
import com.amazon.photosharing.dao.Media;
import com.amazon.photosharing.dao.User;
import com.amazon.photosharing.facade.ContentFacade;
import com.amazon.photosharing.facade.UserFacade;
import com.amazon.photosharing.iface.ServiceFacade;
import com.amazon.photosharing.rest.vo.MediaResult;
import com.amazon.photosharing.security.filters.Secured;
import org.apache.http.HttpStatus;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Path("/media")
public class ContentService extends ServiceFacade {
    @Context
    ServletContext _context;
    @Context
    HttpServletRequest _request;

    ContentFacade _facade;
    UserFacade _userFacade;
    
    public ContentService() {
    	super();
	} 
    
    @PostConstruct
    private void init() {
        _facade = new ContentFacade();
        _userFacade = new UserFacade();
    }

    @GET
    @Path("/{mediaId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response getMedia(@PathParam("mediaId") Long mediaId) {
        Media media =_facade.findMedia(mediaId);

        MediaResult mediaResult = new MediaResult(media);

        return Response.status(HttpStatus.SC_OK).entity(mediaResult).build();
    }

    @DELETE
    @Path("/{mediaId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response deleteMedia(@PathParam("mediaId") Long mediaId) {
        List<Long> idList = new ArrayList<>();
        idList.add(mediaId);

        _facade.deleteMedia(idList);

        return Response.status(HttpStatus.SC_OK).build();
    }

    @POST
    @Path("/upload")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)

    public Response uploadPictureToS3(@Context HttpHeaders httpHeaders,
                                      @FormDataParam("comments") List<String> commentStringList,
                                      @FormDataParam("file") InputStream fileInputStream,
                                      @FormDataParam("file") FormDataContentDisposition disposition ) {
        try {
            Comment [] comments = new Comment[commentStringList.size()];

            int i = 0;
            for (String commentString : commentStringList) {
                Comment comment = new Comment();
                comment.setText(commentString);
                comments[i] = comment;
                i++;
            }

            String userName = _request.getUserPrincipal().getName();

            _logger.info("Username : " + userName);
            _logger.info("Comments: " + comments);

            if (disposition != null) _logger.info("Name: " + disposition.getName());
            if (disposition != null) _logger.info("Filename: " + disposition.getFileName());

            User user = _userFacade.findUser(userName);
            Media media = _facade.uploadPictureToS3(user, disposition.getFileName(), fileInputStream, disposition.getType(), comments);

            MediaResult uploadResult = new MediaResult(media);

            return Response.status(HttpStatus.SC_OK).entity(uploadResult).build();
        }

        catch (IOException exc) {
            _logger.error(exc.toString(), exc);
            return Response.status(HttpStatus.SC_BAD_REQUEST).build();
        }
    }

}
