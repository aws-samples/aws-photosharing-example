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

package com.amazon.photosharing.model;

import java.io.Serializable;
import java.util.regex.Pattern;

public abstract class Property implements Serializable {
	
	private static final long serialVersionUID = -5950600498082715620L;
	
	private String[] _propertyPath;
	
	protected Property(String p_property) {
		setProperty(p_property);
	}
	
	public String getProperty() {return join(_propertyPath);}
	public void setProperty(String property) {this._propertyPath = split(property);}
	
	public String[] getPropertyPath() {return _propertyPath;}
	public void setPropertyPath(String[] p_path) {this._propertyPath = p_path;}
	
	private String[] split(String p_path) {
		if (p_path.contains("."))
			return p_path.split(Pattern.quote("."));
		else
			return new String[] {p_path};
	}
	
	private String join(String[] p_path) {
		StringBuffer result = new StringBuffer();
		for (String string : p_path) {
			result.append(string);
			result.append('.');
		}
		return result.substring(0, result.length()-1);
	}
}
