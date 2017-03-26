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

import java.io.Serializable;

import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.photosharing.dao.User;
import com.amazon.photosharing.model.SelectModel;

public abstract class AbstractController implements Serializable {

	private static final long serialVersionUID = 3816198605185091261L;
	
	private ValueExpression _sessionUserExpression = null;
	
	private SelectModel _selected = new SelectModel();	
	private boolean _selectMode = false;
	
	protected final Logger _logger = LoggerFactory.getLogger(this.getClass());		
		
	public boolean isSelectMode() {return _selectMode;}
	public void setSelectMode(boolean p_value) {this._selectMode = p_value;}
	
	public void onSelectModeChanged() {
//		if (!isSelectMode())
//			_selected.clear();		
	}
	
	protected HttpServletRequest getRequest() {
		return ((HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest());				
	}
	protected HttpServletResponse getResponse() {
		return ((HttpServletResponse)FacesContext.getCurrentInstance().getExternalContext().getResponse());
	}
			
	public User getSessionUser() {					
		return (User) getSessionUserExpression().getValue(FacesContext.getCurrentInstance().getELContext());			
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T getExpressionValue(Class<T> p_class, String p_expression) {
		return (T) FacesContext.getCurrentInstance().getApplication().getExpressionFactory().createValueExpression(
				FacesContext.getCurrentInstance().getELContext(), p_expression, p_class).getValue(FacesContext.getCurrentInstance().getELContext());
	}
	
	private ValueExpression getSessionUserExpression() {
		if (_sessionUserExpression == null)
			_sessionUserExpression = FacesContext.getCurrentInstance().getApplication().getExpressionFactory().createValueExpression(
					FacesContext.getCurrentInstance().getELContext(), "#{authcontroller.user}", User.class);
		return _sessionUserExpression;
	}
	
	
	public SelectModel getSelected() {return _selected;}
	public void setSelected(SelectModel p_selected) {this._selected = p_selected;}
	

}


