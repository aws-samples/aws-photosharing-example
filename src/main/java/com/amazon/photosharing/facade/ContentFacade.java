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

package com.amazon.photosharing.facade;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.amazon.photosharing.dao.Comment;
import com.amazon.photosharing.dao.Media;
import com.amazon.photosharing.dao.User;
import com.amazon.photosharing.enums.Configuration;
import com.amazon.photosharing.iface.ServiceFacade;
import com.amazon.photosharing.listener.Persistence;
import com.amazon.photosharing.utils.ContentHelper;
import com.amazon.photosharing.utils.S3Helper;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;

public class ContentFacade extends ServiceFacade {

	public ContentFacade() {
		super(Persistence::createEntityManager);
	}
	
	public ContentFacade(Supplier<EntityManager> p_emFactory) {
		super(p_emFactory);
	}

	public Media findMedia(Long p_media_id) {
		Media m = em().find(Media.class, p_media_id);		
		return m;
	}

	public void deleteMedia(List<Long> p_media_ids) {
		ExecutorService executor = Executors.newFixedThreadPool(10);
		for (Long media_id : p_media_ids) 
			executor.submit(new MediaDeletionTask(media_id));
		
		executor.shutdown();
		try {
			executor.awaitTermination(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {		
			_logger.error(e.getMessage(), e);
		} finally {
            // Calling System.gc is bad practice and should be avoided.
			// System.gc();
		}
	}
	
	public Media updateMedia(Media p_media) {
		Media m = em().find(Media.class, p_media.getId());
		m.setName(p_media.getName());
		beginTx();
		em().merge(m);
		commitTx();
		
		return m;
	}
	
    public Media uploadPictureToS3(User p_user, String p_file_name, InputStream p_file_stream, String p_content_type, Comment ... _comments) throws IOException {
        Media media = null;

        try {

            ContentHelper.getInstance().createS3BucketIfNotExists(ContentHelper.getInstance().getConfiguredBucketName());

            beginTx();

            String s3Key = S3Helper.createS3Key(p_file_name, p_user.getUserName(), new Date());
            String s3ThumbKey = S3Helper.createS3Key("thumb_"+p_file_name, p_user.getUserName(), new Date());
            
            byte[] original_bytes = null;                                  
            byte[] thumb_bytes = null;
            
            //clone a byte[] of the input original for image resize and thumb clone
            ByteArrayOutputStream byte_worker = new ByteArrayOutputStream();            
            ImageIO.write(ImageIO.read(p_file_stream), p_file_name.substring(p_file_name.lastIndexOf(".")+1), byte_worker);
            
            original_bytes = byte_worker.toByteArray();            
            try {            	         
            	thumb_bytes = new MediaResizeTask(new ByteArrayInputStream(original_bytes), p_file_name).call();            	
			} catch (Exception e) {
				_logger.error(e.getMessage(), e);
			}
            
            User u = em().find(User.class, p_user.getId());

            media = new Media();
            media.setS3Bucket(ContentHelper.getInstance().getConfiguredBucketName());
            media.setS3FileName(s3Key);
            media.setS3ThumbFileName(s3ThumbKey);
            media.setName(p_file_name);
            media.setUser(u);

            if (_comments != null) {
	            for (Comment comment : _comments) {
	            	comment.setMedia(media);
	                media.getComments().add(comment);
	            }
            }
            
            u.getMedia().add(media);

            em().persist(u);           
            
            commitTx();     
            
            ContentHelper.getInstance().uploadContent(p_content_type, thumb_bytes.length, ContentHelper.getInstance().getConfiguredBucketName(), s3ThumbKey, new ByteArrayInputStream(thumb_bytes));
            ContentHelper.getInstance().uploadContent(p_content_type, original_bytes.length, ContentHelper.getInstance().getConfiguredBucketName(), s3Key, new ByteArrayInputStream(original_bytes));
            
            try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				//do some sleeping here..
			}

        } catch (AmazonServiceException ase) {
            _logger.info("Caught an AmazonServiceException, which " +
                    "means your request made it " +
                    "to Amazon S3, but was rejected with an error response" +
                    " for some reason.");
            _logger.info("Error Message:    " + ase.getMessage());
            _logger.info("HTTP Status Code: " + ase.getStatusCode());
            _logger.info("AWS Error Code:   " + ase.getErrorCode());
            _logger.info("Error Type:       " + ase.getErrorType());
            _logger.info("Request ID:       " + ase.getRequestId());

            try {
            	rollbackTx();
            } catch (Exception ex) {}
            
            ase.printStackTrace();

        } catch (AmazonClientException ace) {
            _logger.info("Caught an AmazonClientException, which " +
                    "means the client encountered " +
                    "an internal error while trying to " +
                    "communicate with S3, " +
                    "such as not being able to access the network.");
            _logger.info("Error Message: " + ace.getMessage());

            ace.printStackTrace();
            
            rollbackTx();
        }

        return media;
    }
       
    private class MediaDeletionTask implements Runnable {
    	
    	private Long _media_id;
    	
    	public MediaDeletionTask(Long p_id) {
    		this._media_id = p_id;
			_logger.info("Delete media with id: " + p_id);
    	}

		public void run() {
			EntityManager em = _emFactory.get();
			Media m = em.find(Media.class, _media_id);
			if (m == null) {
                _logger.info("Media is null");
				return;
			}
			else {					
				try {
					em.getTransaction().begin();
                    em.remove(m);
					em.getTransaction().commit();

                    ContentHelper.getInstance().deleteContent(m.getS3Bucket(), m.getS3FileName());
                    ContentHelper.getInstance().deleteContent(m.getS3Bucket(), m.getS3ThumbFileName());
				} catch (Exception ex) {
					_logger.error(ex.getMessage(), ex);
				} finally {
					em.close();			
					em = null;
				}
			}				
		}    	    
    }
    
    private class MediaResizeTask implements Callable<byte[]> {
    	    	
    	private InputStream _image_in;    	
    	private String _file_name;
    	
    	private int MAX_WIDTH = Integer.parseInt(ConfigFacade.get(Configuration.IMG_THUMB_MAX_WIDTH));
    	private int MAX_HEIGHT = Integer.parseInt(ConfigFacade.get(Configuration.IMG_THUMB_MAX_HEIGHT));
    	
    	public MediaResizeTask(InputStream _image_in, String p_file_name) {
    		this._image_in = _image_in;
    		this._file_name = p_file_name;
    	}

		@Override
		public byte[] call() throws Exception {
			BufferedImage original = ImageIO.read(_image_in);
			float ratio = (float)original.getHeight()/(float)original.getWidth();
			
			//if height > width take max_height else resize  
			int thumb_height = ratio>=1?MAX_HEIGHT:(int) (MAX_HEIGHT*ratio);
			//if width > height max_width else multiply by ratio
			int thumb_width = ratio<=1?MAX_WIDTH:(int) (MAX_WIDTH/ratio);
							
			BufferedImage result = new BufferedImage(thumb_width, thumb_height, original.getType());
			Graphics2D g = result.createGraphics();
			g.drawImage(original, 0, 0, thumb_width, thumb_height, null);
			g.dispose();
						
			ByteArrayOutputStream os = new ByteArrayOutputStream();
	        ImageIO.write(result, _file_name.substring(_file_name.lastIndexOf(".")+1), os);
	        
	        return os.toByteArray();
		}    	    
    }
}
