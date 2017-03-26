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

package com.amazon.photosharing.facade;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.persistence.EntityManager;

import com.amazon.photosharing.dao.Album;
import com.amazon.photosharing.dao.Media;
import com.amazon.photosharing.dao.User;
import com.amazon.photosharing.iface.ServiceFacade;
import com.amazon.photosharing.listener.Persistence;
import com.amazon.photosharing.model.SelectModel;

public class AlbumFacade extends ServiceFacade {
   
        	
	public AlbumFacade() {
		super(Persistence::createEntityManager);		
	}
	
	public AlbumFacade(Supplier<EntityManager> p_emFactory) {
		super(p_emFactory);		
	}
        
	public void addToAlbum(Long p_album_id, SelectModel p_media_selected) {
		Album m_album = em().find(Album.class, p_album_id);
		if (m_album == null)
			return;
		beginTx();
		for (Long image_id  : p_media_selected.keySet()) {				
			if (p_media_selected.get(image_id)) {				
				Media m_media = em().find(Media.class, image_id);

				if (m_media != null && !m_media.getAlbums().contains(m_album)) {			
					m_media.getAlbums().add(m_album);
				}
			}			
		}
		em().persist(m_album);
		commitTx();
	}
	
	public Album findAlbum(Long p_album_id) {
		Album a = em().find(Album.class, p_album_id);
		return a;
	}
	
	public Album storeAlbum(Album p_album, User p_user) {
		if (p_album == null)
			return null;
			
		if (p_album.getId() != null) {
			_logger.info("Album id: " + p_album.getId());
			if (!p_album.getUser().getId().equals(p_user.getId())) {							
				throw new IllegalAccessError("Can not modify album from different user");
			} 
			beginTx();
				em().merge(p_album);
			commitTx();
			return p_album;
		} else {
			beginTx();				
				p_album.setUser(em().find(User.class, p_user.getId()));
				em().persist(p_album);
			commitTx();
			return p_album;			
		}			
	}
	
	public void removeMedia(Long p_album_id, ArrayList<Long> p_media_ids, boolean p_delete_contents) {
		ContentFacade content_facade = new ContentFacade(_emFactory);
		
		Album album = em().find(Album.class, p_album_id);
		if (album == null)
			return;
		
		if (p_delete_contents) {			
			content_facade.deleteMedia(p_media_ids);			
		} else {
			
			beginTx();
			
			for (Long media_id : p_media_ids) {
				Media m = em().find(Media.class, media_id);
				if (m == null)
					continue;
							
				m.getAlbums().remove(album);
				em().persist(m);
																									
			}
			
			commitTx();
		}
	}
	
	public void deleteAlbums(List<Long> p_album_ids, boolean p_delete_contents) {
		ContentFacade content_facade = new ContentFacade(_emFactory);
		for (Long album_id : p_album_ids) {
			Album a = em().find(Album.class, album_id);
			if (a == null)
				continue;
			
			//TODO: TEST THIS ONCE IMAGES CAN BE ADDED
			
			List<Media> media = a.getMedia();
			ArrayList<Long> media_ids = new ArrayList<>(media.size());
			
			beginTx();
			
			if (p_delete_contents) {
				for (Media m : media) media_ids.add(m.getId());			
					content_facade.deleteMedia(media_ids);
					
			} else {
				for (Media m : media) {
					m.getAlbums().remove(a);
					em().persist(m);
				}
			}						
			
			em().remove(a);
            em().flush();
			commitTx();
            em().close();
        }
	}
}
