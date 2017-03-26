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

package com.amazon.photosharing.iface;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.photosharing.enums.Configuration;
import com.amazon.photosharing.facade.ConfigFacade;
import com.amazon.photosharing.listener.Persistence;
import com.amazon.photosharing.model.Filter;
import com.amazon.photosharing.model.ListRequest;
import com.amazon.photosharing.model.ListResponse;
import com.amazon.photosharing.model.Sort;

public abstract class ServiceFacade {
	
	protected Supplier<EntityManager> _emFactory;
	private EntityManager _em;

	protected final Logger _logger = LoggerFactory.getLogger(this.getClass());	
	
	protected ServiceFacade() {
		_emFactory = Persistence::createEntityManager;
	}
	
	protected ServiceFacade(Supplier<EntityManager> p_emFactory) {
		_emFactory = p_emFactory;
	}
	

	protected EntityManager em() {
		if (_em == null || !_em.isOpen()) {
			_em = _emFactory.get();
		}		
		return _em;	
	}		
	
	protected String getConfig(Configuration p_key) {		
		return new ConfigFacade().getConfig(p_key);	
	}
	
	protected void beginTx() {em().getTransaction().begin(); }
	protected void commitTx() {em().getTransaction().commit();  done();}
	protected void rollbackTx() {em().getTransaction().rollback(); done();}
	
	
	@PreDestroy
	public void done() {
		if (_em != null) {
			if (_em.isOpen())
				_em.close();
			
			_em = null;
		}
	}		
	
	 public synchronized <T> ListResponse<T> list(ListRequest<T> p_request) {		 		 	
		 
	    	CriteriaBuilder builder = em().getCriteriaBuilder();
			
	    	CriteriaQuery<T> search = builder.createQuery(p_request.getType());		
			CriteriaQuery<Long> count = builder.createQuery(Long.class);
			
			Root<T> root = search.from(p_request.getType());			
					
			count.select(builder.count(root));
			count.from(p_request.getType());
			
			search.select(root);								
			
			Predicate or_predicate = getFilterPredicate(builder::or, root, builder, p_request.getORFilter());
			Predicate and_predicate = getFilterPredicate(builder::and, root, builder, p_request.getANDFilter());	
			Predicate member_predicate = getIsMemberOfPredicate(root, builder, p_request.getMemberFilter());
			Predicate filter_predicate = null;							
			
			if (and_predicate != null && or_predicate != null)
				filter_predicate = builder.and(and_predicate, or_predicate);
			else if (and_predicate != null)
				filter_predicate = and_predicate;
			else if (or_predicate != null)
				filter_predicate = or_predicate;	
			if (member_predicate != null) {
				if  (filter_predicate != null)
					filter_predicate = builder.and(filter_predicate, member_predicate);
				else
					filter_predicate = member_predicate;					
			}
			
			
			if (filter_predicate != null) {
				search.where(filter_predicate);
				count.where(filter_predicate);
			}					
			
			List<Order> order = getSort(root,  builder,  p_request.getSort());
			
			if (order.size() > 0) {
				search.orderBy(order);
			}
				
			
			List<T> results = em().createQuery(search)
				.setHint("org.hibernate.cacheable", p_request.getCachable())
				.setFirstResult(p_request.getFirst())
				.setMaxResults(p_request.getMax())				
				.getResultList();					
			
			Long total = em().createQuery(count)
							.setHint("org.hibernate.cacheable", p_request.getCachable())
							.getSingleResult();					
									
			done();
			
			return new ListResponse<T>(results, p_request.getFirst(), p_request.getMax(), total.intValue());
	    }
	 
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private <C,T> Predicate getIsMemberOfPredicate(Root<C> p_root, CriteriaBuilder p_builder, Filter p_filter) {
		if (p_filter == null)
			return null;
		
		Path<? extends Collection> property_path = null;	
		
		for (String hop : p_filter.getPropertyPath()) {
			if (property_path == null)
				property_path = p_root.get(hop);
			else
				property_path = property_path.get(hop);
		}		
		return p_builder.isMember(p_filter.getValue(), property_path);
	}
	 
	@SuppressWarnings("unchecked")
	private <T> Predicate getFilterPredicate(Function<Predicate[], Predicate>  p_method, Root<T> p_root, CriteriaBuilder p_builder, Filter[] p_filter) {
		 
		 Predicate predicate = null;
		 
		 if (p_filter != null && p_filter.length > 0) {	
				Path<?> property_path = null;							
				LinkedList<Predicate> predicates = new LinkedList<Predicate>();				
				
				for (Filter filter: p_filter) {							
					for (String hop : filter.getPropertyPath()) {
						if (property_path == null)
							property_path = p_root.get(hop);
						else
							property_path = property_path.get(hop);
					}
					if (filter.getValue() != null) {
						if (filter.isExact())
							predicates.add(p_builder.equal(property_path, filter.getValue()));
						else
							predicates.add(p_builder.like((Expression<String>) property_path, filter.getValue()+"%"));
					} else {
						if (filter.isInverse())
							predicates.add(p_builder.isNotNull(property_path));
						else
							predicates.add(p_builder.isNull(property_path));
					}
					
					property_path = null;					
				}											
				
				if (predicates.size() > 1)
					predicate = p_method.apply(predicates.toArray(new Predicate[predicates.size()]));
				else
					predicate = predicates.get(0);							
		}				 
		return predicate;
	 }
	
	private <T> List<Order> getSort(Root<T> p_root, CriteriaBuilder p_builder, Sort[] p_sort) {
		
		List<Order> order = new LinkedList<Order>();
		
		if (p_sort != null && p_sort.length > 0) {				
			
			for (Sort sort : p_sort) {
				Path<?> property_path = null;		
				
				for (String hop : sort.getPropertyPath()) {
					if (property_path == null)
						property_path = p_root.get(hop);
					else
						property_path = property_path.get(hop);
				}
				if (sort.getOrderAscending()) {
					order.add(p_builder.asc(property_path));
				} else {
					order.add(p_builder.desc(property_path));
				}
			}			
		}
		
		return order;
	}
}