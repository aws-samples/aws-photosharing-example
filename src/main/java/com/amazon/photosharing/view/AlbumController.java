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
import java.util.ArrayList;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import com.amazon.photosharing.dao.Album;
import com.amazon.photosharing.dao.Media;
import com.amazon.photosharing.facade.AlbumFacade;
import com.amazon.photosharing.model.Filter;
import com.amazon.photosharing.model.ListModel;
import com.amazon.photosharing.model.ListRequest;
import com.amazon.photosharing.model.SelectModel;
import com.amazon.photosharing.model.Sort;

@ManagedBean(name="albumcontroller")
@ViewScoped
public class AlbumController extends AbstractController {
	
	private static final long serialVersionUID = -263785837257342315L;
	
	private transient AlbumFacade _facade = null;
	private transient ListModel<Album> _userAlbums;	
	private transient ListModel<Media> _userAlbumMedia;	
	
	private Album _album = new Album();	
	
	private SelectModel _selectedMedia = new SelectModel();	
	
	private ListRequest<Album> _albumRequest = new ListRequest<Album>(Album.class, 
																0,16, 
																null, 
																null, 
																new Sort[] {new Sort("createTime", false)});	

	private ListRequest<Media> _albumMediaRequest = new ListRequest<Media>(Media.class, 
																0,20, 
																null, 
																null, 
																new Sort[] {new Sort("createTime", false)});
	
	public AlbumFacade getFacade() {
		if (this._facade == null) {
			_facade = new AlbumFacade();
		}
		return _facade;
	}	
			

	public void refresh() {
		_userAlbums = null;
	}
	
	public ListModel<Album> getUserAlbumList() {				
		if (_userAlbums == null) {					
			getAlbumListRequest().setANDFilter(new Filter("user", getSessionUser()));		
			_userAlbums = new ListModel<Album>(getFacade()::list, getAlbumListRequest());
			_userAlbums .load();
		}								
		return _userAlbums;
	}
		
	public ListModel<Media> getAlbumMediaList() {
		if (_album.getId() == null)
			return null;
			
		if (_userAlbumMedia == null) {			
			if (getSessionUser().getId() != null)
				getAlbumMediaListRequest().setANDFilter(new Filter("user", getSessionUser()));
			
			getAlbumMediaListRequest().setMemberFilter(new Filter("albums", getFacade().findAlbum(_album.getId())));
			
			_userAlbumMedia = new ListModel<Media>(getFacade()::list, getAlbumMediaListRequest());
			_userAlbumMedia .load();
		}								
		return _userAlbumMedia ;
	}
	
	public Album getAlbum() {return _album;}
	public void setAlbum(Album p_album) {this._album = p_album;}
	
	public void loadAlbum() {
		setAlbum(getFacade().findAlbum(getAlbum().getId()));
		getFacade().done();
		if (getAlbum() == null) {
			try {
				FacesContext.getCurrentInstance().getExternalContext().responseSendError(HttpServletResponse.SC_NOT_FOUND, null);
				FacesContext.getCurrentInstance().responseComplete();
			} catch (IOException e) {
				//e.printStackTrace();
			}
		}
	}	
	
	public ListRequest<Album> getAlbumListRequest() {return _albumRequest;}
	public void setAlbumListRequest(ListRequest<Album> p_request) {this._albumRequest = p_request;}
	
	public ListRequest<Media> getAlbumMediaListRequest() {return _albumMediaRequest;}
	public void setAlbumMediaListRequest(ListRequest<Media> p_request) {this._albumMediaRequest = p_request;}
	
	
	public void removeSelectedMedia() {		
		ArrayList<Long> selected = new ArrayList<Long>(getSelectedMedia().entrySet().size());
		for (Map.Entry<Long, Boolean> selected_item : getSelectedMedia().entrySet()) {
			if (selected_item.getValue()) {
				selected.add(selected_item.getKey());				
			}
		}
		
		getFacade().removeMedia(getAlbum().getId(), selected, false);
		for (Long removed_id : selected)	getSelectedMedia().remove(removed_id); //remove all which have been deleted 
		
		_userAlbumMedia = null;
		
		getFacade().done();
	}
	
	public void deleteSelectedItems() {		
		ArrayList<Long> selected = new ArrayList<Long>(getSelected().entrySet().size());
		for (Map.Entry<Long, Boolean> selected_item : getSelected().entrySet()) {
			if (selected_item.getValue()) {
				selected.add(selected_item.getKey());				
			}
		}
		
		getFacade().deleteAlbums(selected, false);
		for (Long deleted_id : selected)	getSelected().remove(deleted_id); //remove all which have been deleted 
		
		_userAlbums = null;
		
		getFacade().done();
	}
		
	public void create() {
		getAlbum().setId(null);
		getFacade().storeAlbum(getAlbum(), getSessionUser());
		setAlbum(new Album());
		//refresh
		_userAlbums = null;
		_userAlbumMedia = null;
	}
	
	public void store() {						
		getFacade().storeAlbum(getAlbum(), getSessionUser());
		setAlbum(new Album());
		//refresh
		_userAlbums = null;
		_userAlbumMedia = null;
	}
	
	public SelectModel getSelectedMedia() {return _selectedMedia;}
	public void setSelectedMedia(SelectModel p_selected) {this._selectedMedia = p_selected;}
		
	
	@PostConstruct
	public void onConstruct() {
	}
	@PreDestroy 
	public void onDestroy() {
	}
}
