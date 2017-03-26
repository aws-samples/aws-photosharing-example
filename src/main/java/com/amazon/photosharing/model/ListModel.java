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


import java.util.List;
import java.util.function.Function;

public class ListModel<T> {
	
	private ListResponse<T> _response;
	private ListRequest<T> _request;
	
	private Function<ListRequest<T>, ListResponse<T>> _action;
	
	public ListModel(Function<ListRequest<T>, ListResponse<T>> p_action, ListRequest<T> p_request) {
		this._request = p_request;	
		this._action = p_action;
	}
	
	public ListResponse<T> load() {
		_response = _action.apply(_request);		
		return _response;
	}
	
	public List<T> getResults() {return _response!=null?_response.getResults():load().getResults();}	
	
	public int getFirst() {	return _request.getFirst();}
	public void setFirst(int first) {_request.setFirst(first);}
	
	public int getLast() {return _request.getMax();}
	public void setLast(int last) {this._request.setMax(last);}
	
	public int getTotal() {
		return _response!=null?_response.getTotal():0;
	}
	public void setTotal(int total) {return;}
	
	public void first() {_request.first();load();}
	public void last() {_request.last(getTotal());load();}
	public void prev() {_request.prev();load();}
	public void next() {_request.next();load();}
	
}
