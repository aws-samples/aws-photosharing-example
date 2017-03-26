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

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.amazon.photosharing.dao.Album;
import com.amazon.photosharing.dao.Media;
import com.amazon.photosharing.dao.Share;
import com.amazon.photosharing.dao.User;
import com.amazon.photosharing.enums.Configuration;
import com.amazon.photosharing.iface.ServiceFacade;
import com.amazon.photosharing.listener.Persistence;
import com.amazon.photosharing.model.Filter;
import com.amazon.photosharing.model.ListRequest;
import com.amazon.photosharing.model.ListResponse;
import com.amazon.photosharing.model.Sort;
import com.amazon.photosharing.utils.ContentHelper;

public class ShareFacade extends ServiceFacade {
           	
	private ListRequest<Share> _shareListRequest = new ListRequest<>(Share.class,
													0,20, 
													null, 
													null, 
													new Sort[] {new Sort("createTime", false)});	
	
	public ShareFacade() {
		super(Persistence::createEntityManager);		
	}
	
	public ShareFacade(Supplier<EntityManager> p_emFactory) {
		super(p_emFactory);		
	}
     
	public Share validateSharePermissions(String p_hash, User p_user) throws IllegalAccessError {
		CriteriaBuilder builder = em().getCriteriaBuilder();		
    	CriteriaQuery<Share> search = builder.createQuery(Share.class);
    	Root<Share> root = search.from(Share.class);
    	search.where(builder.equal(root.get("hash"), p_hash));
    	try {
    		Share result = em().createQuery(search)
    	    	.setHint("org.hibernate.cacheable",true)
    	    	.getSingleResult();
    		    		
    		//private access to not public share
    		if (result.getSharedWith() != null) {    		
    			
	    		//not logged in or logged in access to share not shared with
	    		if (p_user == null || (p_user != null && !(p_user.getId().equals(result.getSharedWith().getId()) || p_user.getId().equals(result.getUser().getId())))){
	    			throw new IllegalAccessError();
	    		}
    		}
    		
    		return result;
    		
    	} catch (NoResultException ex) {
    		return null;
    	}
	}
	
	public ListResponse<Share> getPublicShares() {
		
		//count first to randomize
		_shareListRequest.setANDFilter(new Filter("album.id", null), new Filter("sharedWith", null), new Filter("fleeting", false));
		int total = this.list(_shareListRequest).getTotal();
		
		//randomize start
		_shareListRequest.setANDFilter(new Filter("album.id", null), new Filter("sharedWith", null), new Filter("fleeting", false));
		_shareListRequest.setSort(new Sort[] {new Sort("createTime", false), new Sort("media.id", false)});
		_shareListRequest.setFirst(total <2?0:(int)(Math.round((Math.random()*(total-1)))));
		_shareListRequest.setMax(1);
				
		return this.list(_shareListRequest);
	}
	
	public ListResponse<Share> getPublicShares(Album p_album) {
		_shareListRequest.setANDFilter(new Filter("album.id", p_album.getId()), new Filter("sharedWith", null), new Filter("fleeting", false));		
		return this.list(_shareListRequest);
	}
	
	public ListResponse<Share> getPublicShares(Media p_media) {		
		_shareListRequest.setANDFilter(new Filter("media.id", p_media.getId()), new Filter("sharedWith", null), new Filter("fleeting", false));		
		return this.list(_shareListRequest);
	}
	
	public ListResponse<Share> getFleetingShares(Album p_album) {
		_shareListRequest.setANDFilter(new Filter("album.id", p_album.getId()), new Filter("sharedWith", null), new Filter("fleeting", true));		
		return this.list(_shareListRequest);
	}
	
	public ListResponse<Share> getFleetingShares(Media p_media) {
		_shareListRequest.setANDFilter(new Filter("media.id", p_media.getId()), new Filter("sharedWith", null), new Filter("fleeting", true));		
		return this.list(_shareListRequest);
	}
	
	public ListResponse<Share> getUserShares(Album p_album) {
		_shareListRequest.setANDFilter(new Filter("album.id", p_album.getId()), new Filter("sharedWith", null, true, false));
		return this.list(_shareListRequest);		
	}
	
	public ListResponse<Share> getUserShares(Media p_media) {
		_shareListRequest.setANDFilter(new Filter("media.id", p_media.getId()), new Filter("sharedWith", null, true, false));
		return this.list(_shareListRequest);		
	}
	
	public Share toggleVisibility(Long p_share_id) {
		Share m_share = em().find(Share.class, p_share_id);
		beginTx();
			m_share.setListed(!m_share.isListed());
			em().persist(m_share);
		commitTx();		
		return m_share;
	}
	
	public Media generatePreSignedThumbURL(Media p_media) {
		Media media = em().find(Media.class, p_media.getId());
		if (media == null) 
			return null;
		
		java.util.Date expires = new java.util.Date();
    	long msec = expires.getTime();
    		 msec += Long.parseLong(ConfigFacade.get(Configuration.IMG_THUMB_PRESIGNED_TIMEOUT));
    	expires.setTime(msec);
		
		URL presigned = ContentHelper.getInstance().getSignedUrl(p_media.getS3Bucket(), p_media.getS3ThumbFileName(), expires);
		media.setPresignedThumbUrl(presigned);
		media.setPresignedThumbUrlExpires(expires);
		
		beginTx();
		em().merge(media);
		commitTx();	
		
		return media;
	}
	
	public Media generatePreSignedURL(Media p_media) {
		Media media = em().find(Media.class, p_media.getId());
		if (media == null) 
			return null;
		
		java.util.Date expires = new java.util.Date();
    	long msec = expires.getTime();
    		 msec += Long.parseLong(ConfigFacade.get(Configuration.IMG_FULL_PRESIGNED_TIMEOUT));
    	expires.setTime(msec);
		
		URL presigned = ContentHelper.getInstance().getSignedUrl(p_media.getS3Bucket(), p_media.getS3FileName(), expires);
		media.setPresignedUrl(presigned);
		media.setPresignedUrlExpires(expires);
		
		beginTx();
		em().merge(media);
		commitTx();	
		
		return media;
	}
	
	public Share generatePublicShareURL(Album p_album) {
		Album album = em().find(Album.class, p_album.getId());
		if (album == null)
			return null;
		
		Share s = new Share();
		s.setAlbum(album);
		s.setUser(album.getUser());
		
		beginTx();
		em().persist(s);
		commitTx();		
		
		return s;
	}
	
	public Share generatePublicShareURL(Media p_media) {
		Media media = em().find(Media.class, p_media.getId());
		if (media == null)
			return null;
		
		Share s = new Share();
		s.setMedia(media);
		s.setUser(media.getUser());
		
		beginTx();
		em().persist(s);
		commitTx();		
		return s;
	}
	
	public Share generateViolatileShareURL(Album p_album) {
		Album album = em().find(Album.class, p_album.getId());
		if (album == null)
			return null;
		
		Share s = new Share();
		s.setAlbum(album);
		s.setUser(album.getUser());
		s.setFleeting(true);
		
		beginTx();
		em().persist(s);
		commitTx();
		
		return s;
	}
	
	public Share generateViolatileShareURL(Media p_media) {
		Media media = em().find(Media.class, p_media.getId());
		if (media == null)
			return null;
		
		Share s = new Share();
		s.setMedia(media);
		s.setUser(media.getUser());
		s.setFleeting(true);
		
		beginTx();
		em().persist(s);
		commitTx();
		
		return s;
	}
	
	public List<Share> shareWithUsers(Album p_album, List<User> p_users) {
		Album album = em().find(Album.class, p_album.getId());
		if (album == null)
			return null;		
		List<Share> result = new LinkedList<Share>();
				
			
		
		for (User user : p_users) {			
			User u = em().find(User.class, user.getId());
			if (u == null) //not with no-one
				continue;
			if (album.getUser().equals(u)) //not with self
				continue;
						
			//check if such a share already exists
			_shareListRequest.setANDFilter(new Filter("album.id", p_album.getId()), new Filter("sharedWith", u));
			List<Share> existing_shares_for_user = this.list(_shareListRequest).getResults();
			
			if (existing_shares_for_user == null || existing_shares_for_user.size() == 0) {
				
				beginTx();
					Share s = new Share();
					s.setSharedWith(u);
					s.setAlbum(album);
					s.setUser(album.getUser());
					em().persist(s);																		
				commitTx();
				
				result.add(s);
			}
		}					
		
		return result;
	}
	
	public List<Share> shareWithUsers(Media p_media, List<User> p_users) {
		Media media = em().find(Media.class, p_media.getId());
		if (media == null)
			return null;		
		List<Share> result = new LinkedList<Share>();
				
			
		
		for (User user : p_users) {			
			User u = em().find(User.class, user.getId());
			if (u == null) //not with no-one
				continue;
			if (media.getUser().equals(u)) //not with self
				continue;
						
			//check if such a share already exists
			_shareListRequest.setANDFilter(new Filter("album.id", p_media.getId()), new Filter("sharedWith", u));
			List<Share> existing_shares_for_user = this.list(_shareListRequest).getResults();
			
			if (existing_shares_for_user == null || existing_shares_for_user.size() == 0) {
				
				beginTx();
					Share s = new Share();
					s.setSharedWith(u);
					s.setMedia(media);
					s.setUser(media.getUser());
					em().persist(s);																		
				commitTx();
				
				result.add(s);
			}
		}					
		
		return result;
	}
	
	public void deletePublicShare(Long p_share_id) {
		Share s = em().find(Share.class, p_share_id);
		if (s == null)
			return;
		else {
			beginTx();
				em().remove(s);
			commitTx();
		}			
	}
	
}
