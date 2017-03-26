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
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import com.amazon.photosharing.dao.Album;
import com.amazon.photosharing.dao.User;
import com.amazon.photosharing.facade.UserFacade;
import com.amazon.photosharing.model.Filter;
import com.amazon.photosharing.model.ListRequest;
import com.amazon.photosharing.model.ListResponse;
import com.amazon.photosharing.model.Sort;

@ManagedBean(name="authcontroller")
@SessionScoped
public class AuthenticationController extends AbstractController {

	private static final long serialVersionUID = 2330450728804375825L;
	
	private transient UserFacade _facade = null;
			
	private String _from = null;		
	private User user = new User();
		
	public User getUser() {return user;}
	public void setUser(User user) {this.user = user;}	
	
	private ListRequest<User> _userListRequest = new ListRequest<User>(User.class, 
														0,6, 
														null, 
														null, 
														new Sort[] {new Sort("email", false)});	
	
	public UserFacade getFacade() {
		if (this._facade == null)
			_facade = new UserFacade();
		return _facade;
	}
	
	public boolean isLoggedIn() {return getUser().getId() != null;}
			
	public void updateReferer() {
		try {
		_from =  	getRequest().getAttribute("javax.servlet.forward.request_uri").toString()
				 +	(getRequest().getAttribute("javax.servlet.forward.query_string")!=null?("?"+getRequest().getAttribute("javax.servlet.forward.query_string")):"");
		} catch (NullPointerException ex) {}
	}
	
	public boolean login() {
		if (getFacade().login(getUser().getUserName(), getUser().getPassword(), getRequest())) {
			setUser(getFacade().findUser(getUser().getUserName()));
			getFacade().done();
			continueToPage();					
			return true;
		} else {
			return false;
		}
	}
	
	public boolean register() {		
		User u = getFacade().findUser(getUser().getUserName());		
		
		if (u != null) //already taken
			return false;
		
		getFacade().register(getUser());			
		login();
			
		return true;
	}
	
	private void continueToPage() {
		try {
			if (_from != null && _from.length() > 0) {				
				getResponse().sendRedirect(_from);
			} else {
				getResponse().sendRedirect(getRequest().getContextPath()+"/");
			}
		} catch (IOException e) {
			e.printStackTrace();			
		}
	}	
	
	public String logout() throws IOException {				
		getFacade().logout(getRequest());		
		return "/";
	}
	
    public List<User> completeUser(String p_query) {
    	_userListRequest.setORFilter(new Filter("userName", p_query, false));
    	ListResponse<User> resp = getFacade().list(_userListRequest);
    	List<User> res = resp.getResults();    	
    	res.remove(getUser());    	
    	return res;
    }
     
}
