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

public class Filter extends Property { 
	
	private static final long serialVersionUID = 7209500278607939616L;
	private Object _value;	
	private boolean exact = true;
	private boolean inverse = false;
	
	public Filter(String p_property, Object p_value) {
		super(p_property);
		setValue(p_value);
	}
	
	public Filter(String p_property, Object p_value, boolean p_exact) {
		super(p_property);
		setValue(p_value);
		setExact(p_exact);
	}
	
	public Filter(String p_property, Object p_value, boolean p_inverse, boolean p_exact) {
		super(p_property);
		setValue(p_value);
		setExact(p_exact);
		setInverse(p_inverse);
	}
	
	public Object getValue() {return _value;}
	public void setValue(Object value) {this._value = value;}

	public boolean isExact() {return exact;}
	public void setExact(boolean exact) {this.exact = exact;}
	
	public boolean isInverse() {return inverse;}
	public void setInverse(boolean inverse) {this.inverse = inverse;}
	
}
