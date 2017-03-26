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

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.photosharing.dao.Media;
import com.amazon.photosharing.facade.ContentFacade;
import com.amazon.photosharing.utils.ContentHelper;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;

@WebServlet(name = "media", urlPatterns = {"/private/media/*"}, asyncSupported = true, loadOnStartup = 3)
public class PrivateMediaServlet extends AbstractServlet {

    private static final long serialVersionUID = 3035142586019273646L;

	protected final Logger _logger = LoggerFactory.getLogger(this.getClass());
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	    	
    	try {    		    		
    		getPrivateContent(req, resp);    			
    	} catch (Exception e) {
			_logger.error(e.getMessage(), e);
    		resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
    	}    	    	      
    }
    
    private void getPrivateContent(HttpServletRequest p_req, HttpServletResponse p_resp) throws ServletException, IOException {    	    	    
    	Media m = new ContentFacade().findMedia(Long.parseLong(p_req.getPathInfo().substring(1)));
    	if (m == null)
    		p_resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
    	
    	addHeaders(p_resp, m);
    	if (m.getUser().getId().equals(Long.parseLong(p_req.getRemoteUser()))) {
    		if (p_req.getParameter("format")!=null && p_req.getParameter("format").equals("thumb"))
    			streamS3Content(m.getS3Bucket(), m.getS3ThumbFileName(), p_resp, false);
    		else
    			streamS3Content(m.getS3Bucket(), m.getS3FileName(), p_resp, false);    		
    	}
    }
    
    private void addHeaders(HttpServletResponse p_resp, Media m) {    	
    	long expires = new Date().getTime()+(1000*3600*24); //24h    	
    	p_resp.setDateHeader("Expires", expires);
    }      
    
    private void streamS3Content(String p_s3_bucket, String p_s3_file, HttpServletResponse p_resp, boolean p_no_retry) throws IOException{      	
		S3ObjectInputStream stream = ContentHelper.getInstance().downloadContent(p_s3_bucket, p_s3_file);
         if (stream != null) {        	
        	 try {
	             IOUtils.copy(stream, p_resp.getOutputStream());             	           
        	 } catch (IOException e) {
        		 //usually broken pipe if user cancels
        	 } finally {
        	     stream.close();
 	             p_resp.getOutputStream().flush();
 	             p_resp.getOutputStream().close();
			 }             
         } else {
             try {
				Thread.sleep(1000); //back off. eventually consistency S3 responses..
			} catch (InterruptedException e) {			
				//why would that happen?
			}
            if (!p_no_retry)
            	streamS3Content(p_s3_bucket, p_s3_file, p_resp, true);
         }
     }
    
    @Override
    protected long getLastModified(HttpServletRequest req) {
    	try {    	
    		ContentFacade f = new ContentFacade(); 
    		Media m = f.findMedia(Long.parseLong(req.getPathInfo().substring(1)));
    		f.done();
    		if (m != null)
    			return m.getCreateTime().getTime();    	
    	} catch (Exception e) {
			_logger.error(e.getMessage(), e);
    	}   
    	return 0;
    }
}
