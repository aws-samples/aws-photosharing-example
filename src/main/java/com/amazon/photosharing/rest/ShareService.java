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
import com.amazon.photosharing.dao.Share;
import com.amazon.photosharing.dao.User;
import com.amazon.photosharing.facade.AlbumFacade;
import com.amazon.photosharing.facade.ContentFacade;
import com.amazon.photosharing.facade.ShareFacade;
import com.amazon.photosharing.facade.UserFacade;
import com.amazon.photosharing.iface.ServiceFacade;
import com.amazon.photosharing.model.ListResponse;
import com.amazon.photosharing.rest.vo.ShareResult;
import com.amazon.photosharing.security.filters.Secured;
import org.apache.http.HttpStatus;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.util.ArrayList;
import java.util.List;

@Path("/shares")
public class ShareService extends ServiceFacade {

    @Context
    ServletContext _context;
    @Context
    HttpServletRequest _request;

    private ShareFacade _facade;
    private UserFacade _userFacade;
    private ContentFacade _contentFacade;
    private AlbumFacade _albumFacade;

    @PostConstruct
    private void init() {
        _facade = new ShareFacade();
        _userFacade = new UserFacade();
        _contentFacade = new ContentFacade();
        _albumFacade = new AlbumFacade();
    }

    @DELETE
    @Path("/{shareId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response delete(@PathParam("shareId") Long shareId) {
        _facade.deletePublicShare(shareId);

        return Response.status(HttpStatus.SC_OK).build();
    }

    @PUT
    @Path("/visibility/{shareId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response toggleVisibility(@PathParam("shareId") Long shareId) {
        _facade.toggleVisibility(shareId);

        return Response.status(HttpStatus.SC_OK).build();
    }

    @GET
    @Path("/permission/{hash}/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response validateSharePermissions(@PathParam("hash") String hash,
                                             @PathParam("userId") Long userId) {

        User user = _userFacade.findUser(userId);
        Share share = _facade.validateSharePermissions(hash, user);

        ShareResult shareResult = new ShareResult(share);

        return Response.status(HttpStatus.SC_OK).entity(shareResult).build();
    }

    @PUT
    @Path("/public/media/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response generateMediaPublicShareURL(@PathParam("id") Long id) {
        Media media = _contentFacade.findMedia(id);
        Share share = _facade.generatePublicShareURL(media);

        ShareResult shareResult = new ShareResult(share);
        return Response.status(HttpStatus.SC_OK).entity(shareResult).build();
    }

    @PUT
    @Path("/public/album/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response generateAlbumPublicShareURL(@PathParam("id") Long id) {
        Album album = _albumFacade.findAlbum(id);
        Share share = _facade.generatePublicShareURL(album);

        ShareResult shareResult = new ShareResult(share);
        return Response.status(HttpStatus.SC_OK).entity(shareResult).build();
    }

    @GET
    @Path("/volatile/media/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response generateMediaViolatileShareURL(@PathParam("id") Long id) {
        Media media = _contentFacade.findMedia(id);
        Share share = _facade.generateViolatileShareURL(media);

        ShareResult shareResult = new ShareResult(share);
        return Response.status(HttpStatus.SC_OK).entity(shareResult).build();
    }

    @GET
    @Path("/volatile/album/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response generateAlbumViolatileShareURL(@PathParam("id") Long id) {
        Album album = _albumFacade.findAlbum(id);
        Share share = _facade.generateViolatileShareURL(album);

        ShareResult shareResult = new ShareResult(share);
        return Response.status(HttpStatus.SC_OK).entity(shareResult).build();
    }

    @POST
    @Path("/media/{mediaId}/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response shareMediaWithUser(@PathParam("mediaId") Long mediaId,
                                        @PathParam("userId") Long userId) {

        Media media = _contentFacade.findMedia(mediaId);
        User user = _userFacade.findUser(userId);
        List<User> userList = new ArrayList<>();
        userList.add(user);
        List<Share> shareList = _facade.shareWithUsers(media, userList);

        Share share = null;
        if (shareList.size() > 0) {
            share = shareList.get(0);
        }

        ShareResult shareResult = new ShareResult(share);
        return Response.status(HttpStatus.SC_OK).entity(shareResult).build();
    }

    @POST
    @Path("/album/{albumId}/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response shareAlbumWithUser(@PathParam("albumId") Long albumId,
                                       @PathParam("userId") Long userId) {

        Album album = _albumFacade.findAlbum(albumId);
        User user = _userFacade.findUser(userId);
        List<User> userList = new ArrayList<>();
        userList.add(user);
        List<Share> shareList = _facade.shareWithUsers(album, userList);

        Share share = null;
        if (shareList.size() > 0) {
            share = shareList.get(0);
        }

        ShareResult shareResult = new ShareResult(share);
        return Response.status(HttpStatus.SC_OK).entity(shareResult).build();
    }

    @GET
    @Path("/public/album/{albumId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response getPublicSharesForAlbum(@PathParam("albumId") Long albumId) {

        Album album = _albumFacade.findAlbum(albumId);
        ListResponse<Share> shareList = _facade.getPublicShares(album);
        List<ShareResult> resultList = new ArrayList<>();

        for (Share share : shareList.getResults()) {
            ShareResult shareResult = new ShareResult(share);
            resultList.add(shareResult);
        }

        return Response.status(HttpStatus.SC_OK).entity(resultList).build();
    }

    @GET
    @Path("/public/media/{mediaId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response getPublicSharesForMedia(@PathParam("mediaId") Long mediaId) {

        Media media = _contentFacade.findMedia(mediaId);
        ListResponse<Share> shareList = _facade.getPublicShares(media);
        List<ShareResult> resultList = new ArrayList<>();

        for (Share share : shareList.getResults()) {
            ShareResult shareResult = new ShareResult(share);
            resultList.add(shareResult);
        }

        return Response.status(HttpStatus.SC_OK).entity(resultList).build();
    }

    @GET
    @Path("/fleeting/album/{albumId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response getFleetingSharesForAlbum(@PathParam("albumId") Long albumId) {

        Album album = _albumFacade.findAlbum(albumId);
        ListResponse<Share> shareList = _facade.getFleetingShares(album);
        List<ShareResult> resultList = new ArrayList<>();

        for (Share share : shareList.getResults()) {
            ShareResult shareResult = new ShareResult(share);
            resultList.add(shareResult);
        }

        return Response.status(HttpStatus.SC_OK).entity(resultList).build();
    }

    @GET
    @Path("/fleeting/media/{mediaId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response getFleetingSharesForMedia(@PathParam("mediaId") Long mediaId) {

        Media media = _contentFacade.findMedia(mediaId);
        ListResponse<Share> shareList = _facade.getFleetingShares(media);
        List<ShareResult> resultList = new ArrayList<>();

        for (Share share : shareList.getResults()) {
            ShareResult shareResult = new ShareResult(share);
            resultList.add(shareResult);
        }

        return Response.status(HttpStatus.SC_OK).entity(resultList).build();
    }

    @GET
    @Path("/album/{albumId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response getUserSharesForAlbum(@PathParam("albumId") Long mediaId) {

        Media media = _contentFacade.findMedia(mediaId);
        ListResponse<Share> shareList = _facade.getUserShares(media);
        List<ShareResult> resultList = new ArrayList<>();

        for (Share share : shareList.getResults()) {
            ShareResult shareResult = new ShareResult(share);
            resultList.add(shareResult);
        }

        return Response.status(HttpStatus.SC_OK).entity(resultList).build();
    }

    @GET
    @Path("/media/{mediaId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured
    public Response getUserSharesForMedia(@PathParam("mediaId") Long mediaId) {

        Media media = _contentFacade.findMedia(mediaId);
        ListResponse<Share> shareList = _facade.getUserShares(media);
        List<ShareResult> resultList = new ArrayList<>();

        for (Share share : shareList.getResults()) {
            ShareResult shareResult = new ShareResult(share);
            resultList.add(shareResult);
        }

        return Response.status(HttpStatus.SC_OK).entity(resultList).build();
    }
}
