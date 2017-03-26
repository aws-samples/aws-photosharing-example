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

public class Sort extends Property { 
		
	private boolean  _order;
	
	public Sort(String p_property, boolean p_asc) {
		super(p_property);
		setOrderAscending(p_asc);
	}
	
	public boolean getOrderAscending() {return _order;}
	public void setOrderAscending(boolean p_asc) {this._order = p_asc;}
}
