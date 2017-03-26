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
import java.lang.annotation.Documented;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.primefaces.event.FileUploadEvent;

import com.amazon.photosharing.dao.Comment;
import com.amazon.photosharing.dao.Media;
import com.amazon.photosharing.facade.AlbumFacade;
import com.amazon.photosharing.facade.ContentFacade;
import com.amazon.photosharing.model.Filter;
import com.amazon.photosharing.model.ListModel;
import com.amazon.photosharing.model.ListRequest;
import com.amazon.photosharing.model.Sort;

@ManagedBean(name="mediacontroller")
@ViewScoped
public class MediaController extends AbstractController {

	private static final long serialVersionUID = 2330450728804375825L;
	
	private transient ContentFacade _facade = null;
	private transient ListModel<Media> _userMedia;	
		
	private Media _media = new Media();
	
	private ListRequest<Media> _mediaRequest = new ListRequest<Media>(Media.class, 
																0,20, 
																null, 
																null, 
																new Sort[] {new Sort("createTime", false)});	

	
	public ContentFacade getFacade() {
		if (this._facade == null) {
			_facade = new ContentFacade();
		}
		return _facade;
	}	
	
	public void handleFileUpload(FileUploadEvent e) {
		try {			
			ContentFacade facade = new ContentFacade();
			facade.uploadPictureToS3(getSessionUser(), e.getFile().getFileName(), e.getFile().getInputstream(), e.getFile().getContentType(), (Comment[]) null);
			facade.done();
			_userMedia = null;
		} catch (IOException e1) {
			e1.printStackTrace();
		}		
	}
		
	
	public Media getMedia() {return _media;}	
	public void setMedia(Media p_media) {this._media = p_media;}
	
	public void loadMedia() {
		this._media = getFacade().findMedia(getMedia().getId());
		getFacade().done();
		if (getMedia() == null) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().responseSendError(HttpServletResponse.SC_NOT_FOUND, null);
				FacesContext.getCurrentInstance().responseComplete();
			} catch (IOException e) {
				//e.printStackTrace();
			}	
		}
	}
	
	public ListModel<Media> getUserMediaList() {				
		if (_userMedia == null) {					
			 getMediaListRequest().setANDFilter(new Filter("user", getSessionUser()));
			_userMedia = new ListModel<>(getFacade()::list, getMediaListRequest());
			_userMedia.load();
			getFacade().done();
		}								
		return _userMedia;
	}	
	
	public void updateMedia() {
		this._media = getFacade().updateMedia(this._media);
		getFacade().done();
	}
	
	public void deleteSelectedItems() {		
		ArrayList<Long> selected = new ArrayList<Long>(getSelected().entrySet().size());
		for (Map.Entry<Long, Boolean> selected_item : getSelected().entrySet()) {
			if (selected_item.getValue()) {
				selected.add(selected_item.getKey());				
			}
		}
		
		getFacade().deleteMedia(selected);
		for (Long deleted_id : selected)	getSelected().remove(deleted_id); //remove all which have been deleted 
		
		
		getFacade().done();
		_userMedia = null;
	}
	
	public void addMediaToAlbum() {		
		AlbumController c = getExpressionValue(AlbumController.class, "#{albumcontroller}");
		AlbumFacade a_facade = new AlbumFacade();
		List<Long> selected_albums = c.getSelected().getSelectedList();
		
		for (Long album_id: selected_albums) {
			a_facade.addToAlbum(album_id, getSelected());
		}	
				
		getFacade().done();
		
		c.refresh();
	}
	
	public ListRequest<Media> getMediaListRequest() {return _mediaRequest;}
	public void setMediaListRequest(ListRequest<Media> p_request) {this._mediaRequest = p_request;}
		
	
	
	@PostConstruct
	public void onConstruct() {
	}
	@PreDestroy 
	public void onDestroy() {
	}
}
