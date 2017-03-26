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

public class ListRequest<T> implements Serializable {

	private static final long serialVersionUID = 3078621363193754623L;
	
	int 	_first;
	int		_max;
	
	boolean _cachable = true;
	
	private Class<T> type;	
	Filter[] _and_filter;
	Filter[] _or_filter;
	Filter   _member_filter;
	Sort[]   _sort;
	
	protected ListRequest(int p_first, int p_max) {
		setFirst(p_first);
		setMax(p_max);
	}
	
	public ListRequest(Class<T> p_type, int p_first, int p_max) {
		setFirst(p_first);
		setMax(p_max);
		setType(p_type);
	}
	
	public ListRequest(Class<T> p_type, int p_first, int p_max, Filter ... p_filter) {
		setFirst(p_first);
		setMax(p_max);
		setType(p_type);
		setANDFilter(p_filter);		
	}
	
	public ListRequest(Class<T> p_type, int p_first, int p_max, Filter[] p_and_filter, Filter[] p_or_filter) {
		setFirst(p_first);
		setMax(p_max);
		setType(p_type);
		setANDFilter(p_and_filter);
		setORFilter(p_or_filter);
	}
	
	public ListRequest(Class<T> p_type, int p_first, int p_max, Filter[] p_and_filter, Filter[] p_or_filter, Sort[] p_sort) {
		setFirst(p_first);
		setMax(p_max);
		setType(p_type);
		setANDFilter(p_and_filter);
		setORFilter(p_or_filter);
		setSort(p_sort);
	}
	
	public boolean getCachable() {return _cachable;}
	public void setCachable(boolean p_cachable) {this._cachable = p_cachable;}
	
	public int getFirst() {return _first;}
	public void setFirst(int first) {this._first = first;}
	
	public int getMax() {return _max;}
	public void setMax(int max) {this._max = max;}
	
	public Filter[] getANDFilter() {return _and_filter;}
	public void setANDFilter(Filter...p_filter) {this._and_filter = p_filter;}
	
	public Filter[] getORFilter() {return _or_filter;}
	public void setORFilter(Filter...p_filter) {this._or_filter = p_filter;}
	
	public Filter getMemberFilter() {return _member_filter;}
	public void setMemberFilter(Filter p_filter) {this._member_filter = p_filter;}
	
	public Sort[] getSort() {return _sort;}
	public void setSort(Sort ...p_sort) {this._sort = p_sort;}

	public Class<T> getType() {return type;}
	public void setType(Class<T> type) {this.type = type;}
		
	public void prev() {setFirst(getFirst()-getMax());}
	public void next() {setFirst(getFirst()+getMax());}
	public void first() {setFirst(0);}
	public void last(int p_total) {setFirst(p_total-getMax());}

	
}
