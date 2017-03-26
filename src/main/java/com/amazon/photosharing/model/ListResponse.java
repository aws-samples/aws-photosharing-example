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

public class ListResponse<T> extends ListRequest<T> {

	private static final long serialVersionUID = -4172916953666204060L;
	
	private List<T> results;
	int		_total;	
	
	public ListResponse(List<T> p_results, int p_first, int p_max, int p_total) {
		super(p_first, p_max);	
		setResults(p_results);
		setTotal(p_total);
	}
	
	public List<T> getResults() {return results;}
	public void setResults(List<T> results) {this.results = results;}

	public int getTotal() {return _total;}
	public void setTotal(int total) {this._total = total;}
		
}
