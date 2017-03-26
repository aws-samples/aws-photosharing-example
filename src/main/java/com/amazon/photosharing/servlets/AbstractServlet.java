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

package com.amazon.photosharing.servlets;

import com.amazon.photosharing.dao.User;

import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServlet;

public class AbstractServlet extends HttpServlet {
	
	private static final long serialVersionUID = 4577486144135992631L;
	
	private ValueExpression _sessionUserExpression = null;

    public User getSessionUser() {
        return (User) getSessionUserExpression().getValue(FacesContext.getCurrentInstance().getELContext());
    }

    private ValueExpression getSessionUserExpression() {
        if (_sessionUserExpression == null)
            _sessionUserExpression = FacesContext.getCurrentInstance().getApplication().getExpressionFactory().createValueExpression(
                    FacesContext.getCurrentInstance().getELContext(), "#{authcontroller.user}", User.class);
        return _sessionUserExpression;
    }
}
