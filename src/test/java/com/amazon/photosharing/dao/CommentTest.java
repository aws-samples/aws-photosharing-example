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

package com.amazon.photosharing.dao;

import com.amazon.photosharing.listener.Persistence;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class CommentTest {

    private void checkComment(Comment _comment) {
        Assert.assertEquals(_comment.getText(), "mycomment");
    }

    @Test
    public void testCRUD() {
        boolean error = false;

        EntityManager _em;

        try {
             _em = Persistence.createEntityManager();

            Comment comment = new Comment();
            comment.setText("mycomment");

            _em.getTransaction().begin();
            _em.persist(comment);
            _em.getTransaction().commit();

            Query query = _em.createQuery("select c from Comment c where c.text='mycomment'");
            Comment retComment = (Comment)query.getSingleResult();

            checkComment(retComment);

            _em.getTransaction().begin();
            _em.remove(retComment);
            _em.getTransaction().commit();
        }

        catch (Exception exc) {
            error = true;
            exc.printStackTrace();
        }

        Assert.assertFalse(error);

        error = false;
        try {
            _em = Persistence.createEntityManager();

            Query query = _em.createQuery("select c from Comment c where c.text='mycomment'");
            Comment retComment = (Comment)query.getSingleResult();
        }

        catch (javax.persistence.NoResultException e) {
            error = true;
        }

        Assert.assertTrue(error);
    }
}
