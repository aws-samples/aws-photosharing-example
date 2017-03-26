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

package com.amazon.photosharing.view;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import com.amazon.photosharing.dao.Album;
import com.amazon.photosharing.dao.Media;
import com.amazon.photosharing.dao.Share;
import com.amazon.photosharing.dao.User;
import com.amazon.photosharing.facade.ShareFacade;
import com.amazon.photosharing.model.ListResponse;

@ManagedBean(name="sharecontroller")
@ViewScoped
public class ShareController extends AbstractController {
	
	private static final long serialVersionUID = -263785837257342315L;
	
	private transient ShareFacade _facade = null;	
	private List<User> selectedUsers = new LinkedList<>();
		
	private String shareHash = null;
	private String sharedMediaId = null;
	
	private AlbumController albumcontroller = new AlbumController();
	private MediaController mediacontroller = new MediaController();
	
	public String getShareHash() {return shareHash;}	
	public void setShareHash(String shareHash) {this.shareHash = shareHash;}
	
	public String getSharedMediaId() {return sharedMediaId;}
	public void setSharedMediaId(String p_mediaId) {this.sharedMediaId = p_mediaId;}
	
	public AlbumController getAlbumController() {return albumcontroller;}		
	public MediaController getMediaController() {return mediacontroller;}	
	
	public void validate() {
		Share result = getFacade().validateSharePermissions(getShareHash(), getSessionUser());			
		if (result != null) {
			if (result.getAlbum() != null) {			
				if (sharedMediaId == null) {
					albumcontroller.getAlbum().setId(result.getAlbum().getId());
					albumcontroller.loadAlbum();
				} else {					
					try {						
						mediacontroller.getMedia().setId(Long.parseLong(sharedMediaId));
						mediacontroller.loadMedia();
					} catch (NumberFormatException ex) {
						try {
							FacesContext.getCurrentInstance().getExternalContext().responseSendError(HttpServletResponse.SC_NOT_FOUND, null);
							FacesContext.getCurrentInstance().responseComplete();
						} catch (IOException e) {
							//e.printStackTrace();
						}	
					}
				}
			}
			if (result.getMedia() != null) {
				mediacontroller.getMedia().setId(result.getMedia().getId());
				mediacontroller.loadMedia();
			}
			
			//delete a fleeting share after this view has been built
			if (result.isFleeting()) {
				getFacade().deletePublicShare(result.getId());
			}
									
		} else {			
			try {
				FacesContext.getCurrentInstance().getExternalContext().responseSendError(HttpServletResponse.SC_NOT_FOUND, null);
				FacesContext.getCurrentInstance().responseComplete();
			} catch (IOException e) {
				//e.printStackTrace();
			}		
		}
		
		getFacade().done();
	}
	
	public ShareFacade getFacade() {
		if (this._facade == null) {
			_facade = new ShareFacade();
		}
		return _facade;
	}	
	
	public List<User> getSelectedUsers() {return selectedUsers;}
	public void setSelectedUsers(List<User> selectedUsers) {this.selectedUsers = selectedUsers;}
	
	public ListResponse<Share> getPublicShares() {return finalize(getFacade().getPublicShares());}
	
	public ListResponse<Share> getPublicShares(Album p_album) {return finalize(getFacade().getPublicShares(p_album));}
	public ListResponse<Share> getPublicShares(Media p_media) {return finalize(getFacade().getPublicShares(p_media));}
	
	public ListResponse<Share> getFleetingShares(Album p_album) {return finalize(getFacade().getFleetingShares(p_album));}
	public ListResponse<Share> getFleetingShares(Media p_media) {return finalize(getFacade().getFleetingShares(p_media));}
	
	public ListResponse<Share> getUserShares(Album p_album) {return finalize(getFacade().getUserShares(p_album));}
	public ListResponse<Share> getUserShares(Media p_media) {return finalize(getFacade().getUserShares(p_media));}
	
	public void generatePublicShareUrl(Album p_album) {getFacade().generatePublicShareURL(p_album); getFacade().done();}
	public void generatePublicShareUrl(Media p_media) {getFacade().generatePublicShareURL(p_media); getFacade().done();}
	
	public void generateFleetingShareUrl(Album p_album) {getFacade().generateViolatileShareURL(p_album); getFacade().done();}
	public void generateFleetingShareUrl(Media p_media) {getFacade().generateViolatileShareURL(p_media); getFacade().done();}
		
	public void shareWithUsers(Album p_album) {
		getFacade().shareWithUsers(p_album, getSelectedUsers());
		getSelectedUsers().clear();
		getFacade().done();
	}
	
	public void shareWithUsers(Media p_media) {
		getFacade().shareWithUsers(p_media, getSelectedUsers());
		getSelectedUsers().clear();
		getFacade().done();
	}
	
	private ListResponse<Share> finalize(ListResponse<Share> p_response) {
		getFacade().done();
		return p_response;
	}
	
	public void deletePublicShare(Long p_share_id) {
		getFacade().deletePublicShare(p_share_id);
		getFacade().done();
	}
	
	public void toggleShareVisibility(Long p_share_id) {
		getFacade().toggleVisibility(p_share_id);
		getFacade().done();
	}
		
	public String getPresignedThumbUrl(Media p_media) {
		Long now = new Date().getTime();
		
		if (p_media.getPresignedThumbUrl() == null || (p_media.getPresignedThumbUrlExpires() != null && now >= p_media.getPresignedThumbUrlExpires().getTime())) {
			p_media = getFacade().generatePreSignedThumbURL(p_media);
		} 
		getFacade().done();
		return p_media.getPresignedThumbUrl().toString();
	}

	public String getPresignedUrl(Media p_media) {
		Long now = new Date().getTime();
		
		if (p_media.getPresignedUrl() == null || (p_media.getPresignedUrlExpires() != null && now >= p_media.getPresignedUrlExpires().getTime())) {
			p_media = getFacade().generatePreSignedURL(p_media);
		}
		getFacade().done();
		return p_media.getPresignedUrl().toString();
	}
	
	
	@PostConstruct
	public void onConstruct() {
	}
	
	@PreDestroy 
	public void onDestroy() {
		getFacade().done();
	}
}
