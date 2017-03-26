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

public class UserTest {

    private void checkUser(User _user) {
        Assert.assertEquals(_user.getUserName(), "username");
        Assert.assertEquals(_user.getEmail(), "jon@doe.com");
        Assert.assertEquals(_user.getPassword(), "test1234");
        Assert.assertEquals(_user.getRoles().size(), 1);
        Assert.assertEquals(_user.getRoles().get(0).getRole(), com.amazon.photosharing.enums.Role.ADMINISTRATOR);
    }

    @Test
    public void testCRUD() {
        boolean error = false;

        EntityManager _em;

        try {
            _em = Persistence.createEntityManager();

            List<Role> rolesList = new ArrayList<>();
            Role role = new Role();
            role.setRole(com.amazon.photosharing.enums.Role.ADMINISTRATOR);
            rolesList.add(role);

            User user = new User();

            user.setUserName("username");
            user.setEmail("jon@doe.com");
            user.setPassword("test1234");
            user.setSalt("salt".getBytes());
            user.setRoles(rolesList);

            _em.getTransaction().begin();
            _em.merge(user);
            _em.getTransaction().commit();

            Query query = _em.createQuery("select u from User u where u.userName='username'");
            User retUser = (User)query.getSingleResult();

            checkUser(retUser);

            _em.getTransaction().begin();
            for (Role tmpRole : retUser.getRoles())
                _em.remove(tmpRole);
            _em.getTransaction().commit();

            _em.getTransaction().begin();
            _em.remove(retUser);
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

            Query query = _em.createQuery("select u from User u where u.userName='username'");
            User retUser = (User)query.getSingleResult();
        }

        catch (javax.persistence.NoResultException e) {
            error = true;
        }

        Assert.assertTrue(error);

    }
}
