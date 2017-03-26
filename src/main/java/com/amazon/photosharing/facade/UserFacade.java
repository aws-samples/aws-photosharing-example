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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.amazon.photosharing.dao.Role;
import com.amazon.photosharing.dao.User;
import com.amazon.photosharing.iface.ServiceFacade;
import com.amazon.photosharing.utils.Security;
import com.amazon.photosharing.utils.TokenGenerator;
import com.amazon.photosharing.utils.TokenStorage;

public class UserFacade extends ServiceFacade {

	public UserFacade() {
		super();
	}
	
	public UserFacade(Supplier<EntityManager> p_emFactory) {
		super(p_emFactory);		
	}
	
	public User register(User p_user) {		
		if (p_user.getId() != null) {			
			User u = em().find(User.class, p_user.getId());
			return u;
		} 
		else {	
			beginTx();																			
				
				User u = new User(p_user.getUserName(), p_user.getPassword(), p_user.getEmail()); 
			
				Role role;
				if ((role = em().find(Role.class, com.amazon.photosharing.enums.Role.AUTHENTICATED)) == null) {
					role = new Role(com.amazon.photosharing.enums.Role.AUTHENTICATED);					
				} 
								
				u.getRoles().add(role);
				em().persist(u);
			
			commitTx();
						
			return u;
		}
	}
	
	public User findUser(Long p_id) {		
		User u = em().find(User.class, p_id);				
		return u;
	}
	
	public User findUser(String p_username) {
		CriteriaBuilder builder = em().getCriteriaBuilder();
		CriteriaQuery<User> criteria = builder.createQuery(User.class );
		Root<User> user_root = criteria.from(User.class);
		criteria.select(user_root);
		criteria.where(builder.equal(user_root.get("userName"), p_username));
		try {
			User u =  em().createQuery(criteria).getSingleResult();
			return u;
		} catch (NoResultException ex) {			
			return null;
		}		
	}
	
	public User findUserByEmail(String p_email) {
		CriteriaBuilder builder = em().getCriteriaBuilder();
		CriteriaQuery<User> criteria = builder.createQuery(User.class );
		Root<User> user_root = criteria.from(User.class);
		criteria.select(user_root);
		criteria.where(builder.equal(user_root.get("email"), p_email));
		try {
			User u =  em().createQuery(criteria).getSingleResult();
			return u;
		} catch (NoResultException ex) {
			return null;
		}		
	}
	
	public boolean login(String p_username, String p_password, HttpServletRequest req) {		
		try {			
			req.logout();
			beginTx();
				User u = findUser(p_username);
			
				if (u == null) {
                    _logger.info("User with username " + p_username + " not found");
                    commitTx();	
                    return false;
                }
			
				req.login(u.getId().toString(), Security.getPasswordHash(p_password, u.getSalt()));
				
				u.updatePassword(p_password);				
				u.setLastLogin(new Date());
			commitTx();			
			return true;
		} catch (ServletException e) {
			_logger.error(e.getMessage(), e);
			return false;
		}		
	}
	
	public void logout(HttpServletRequest req) {		
		req.getSession().invalidate();				
		try {
			req.logout();
		} catch (ServletException e) {
			_logger.error(e.getMessage(), e);
		}
	}
}
