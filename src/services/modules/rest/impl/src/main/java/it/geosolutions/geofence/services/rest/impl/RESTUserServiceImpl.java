/*
 *  Copyright (C) 2007 - 2012 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.geofence.services.rest.impl;

import java.util.List;


import it.geosolutions.geofence.core.model.GSUser;
import it.geosolutions.geofence.core.model.UserGroup;
import it.geosolutions.geofence.services.UserGroupAdminService;
import it.geosolutions.geofence.services.UserAdminService;
import it.geosolutions.geofence.services.exception.BadRequestServiceEx;
import it.geosolutions.geofence.services.exception.NotFoundServiceEx;
import it.geosolutions.geofence.services.rest.RESTUserService;
import it.geosolutions.geofence.services.rest.exception.*;
import it.geosolutions.geofence.services.rest.model.RESTInputUser;
import it.geosolutions.geofence.services.rest.model.RESTOutputUser;
import it.geosolutions.geofence.services.rest.model.RESTShortUser;
import it.geosolutions.geofence.services.rest.model.RESTShortUserList;
import it.geosolutions.geofence.services.rest.model.util.IdName;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.WebApplicationException;

import org.apache.log4j.Logger;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class RESTUserServiceImpl
    extends BaseRESTServiceImpl
    implements RESTUserService {

    private static final Logger LOGGER = Logger.getLogger(RESTUserServiceImpl.class);

    private UserAdminService userService;
    private UserGroupAdminService userGroupService;

    @Override
    public void delete(Long id, boolean cascade) throws BadRequestServiceEx, NotFoundRestEx {
        try {
            userService.delete(id);
        } catch (NotFoundServiceEx ex) {
            LOGGER.warn("User not found: " + id);
            throw new NotFoundRestEx("User not found: " + id);
        }
    }

    @Override
    public void delete(String name, boolean cascade) throws BadRequestServiceEx, NotFoundRestEx {
        try {
            GSUser user = userService.get(name);
            userService.delete(user.getId());
        } catch (NotFoundServiceEx ex) {
            LOGGER.warn("User not found: " + name);
            throw new NotFoundRestEx("User not found: " + name);
        }
    }

    @Override
    public RESTOutputUser get(Long id) throws NotFoundRestEx, InternalErrorRestEx {
        try {
            return toOutputUser(userService.get(id));
        } catch (NotFoundServiceEx ex) {
            LOGGER.warn("User not found: " + id);
            throw new NotFoundRestEx("User not found: " + id);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new InternalErrorRestEx(ex.getMessage());
        }
    }

    @Override
    public RESTOutputUser get(String name) throws NotFoundRestEx, InternalErrorRestEx {
        try {
            return toOutputUser(userService.get(name));
        } catch (NotFoundServiceEx ex) {
            LOGGER.warn("User not found: " + name);
            throw new NotFoundRestEx("User not found: " + name);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new InternalErrorRestEx(ex.getMessage());
        }
    }

    @Override
    public Long insert(RESTInputUser user) throws BadRequestRestEx, NotFoundRestEx, InternalErrorRestEx {
        try {
            // resolve groups
            List<IdName> inputGroups = user.getGroups();
            if ( inputGroups == null || inputGroups.isEmpty()) {
                throw new BadRequestRestEx("Can't insert user without group");
            }
            Set<UserGroup> groups = new HashSet<UserGroup>();
            for (IdName identifier : inputGroups) {
                UserGroup group = getUserGroup(identifier);
                groups.add(group);
            }

            GSUser u = new GSUser();
            u.setGroups(groups);
            u.setExtId(user.getExtId());
            u.setName(user.getName());
            u.setPassword(user.getPassword());
            u.setEnabled(user.isEnabled());
            u.setAdmin(user.isAdmin());
            u.setFullName(user.getFullName());
            u.setEmailAddress(user.getEmailAddress());

            return userService.insert(u);

        } catch (GeoFenceRestEx ex) {
            // already handled
            throw ex;
        } catch (NotFoundServiceEx ex) {
            LOGGER.warn("Problems inserting user: " + ex.getMessage(), ex);
            throw new NotFoundRestEx(ex.getMessage());
        } catch (BadRequestServiceEx ex) {
            LOGGER.warn("Problems inserting user: " + ex.getMessage(), ex);
            throw new BadRequestRestEx(ex.getMessage());
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new InternalErrorRestEx(ex.getMessage());
        }
    }

    @Override
    public void update(String name, RESTInputUser user) throws BadRequestRestEx, NotFoundRestEx, InternalErrorRestEx {
        try {
            GSUser old = userService.get(name);
            update(old.getId(), user);
        } catch (NotFoundServiceEx ex) {
            LOGGER.warn("User not found: " + name);
            throw new NotFoundRestEx("User not found: " + name);
        }        
    }

    @Override
    public void update(Long id, RESTInputUser user) throws BadRequestRestEx, NotFoundRestEx, InternalErrorRestEx {

        try {
            GSUser old = userService.get(id);

            if ( (user.getExtId() != null) && !user.getExtId().equals(old.getExtId()) ) {
                throw new BadRequestRestEx("ExtId can't be updated");
            }

            if ( (user.getName() != null)) {
                throw new BadRequestRestEx("Name can't be updated");
            }

            if(user.getPassword() != null)
                old.setPassword(user.getPassword());

            if(user.getEmailAddress() != null)
                old.setEmailAddress(user.getEmailAddress());

            if(user.isAdmin() != null)
                old.setAdmin(user.isAdmin());

            if(user.isEnabled() != null)
                old.setEnabled(user.isEnabled());

            if(user.getGroups() != null) {
                Set<UserGroup> groups = new HashSet<UserGroup>();
                for (IdName identifier : user.getGroups()) {
                    UserGroup group = getUserGroup(identifier);
                    groups.add(group);
                }
                old.setGroups(groups);
            }

            userService.update(old);

        } catch (GeoFenceRestEx ex) {
            // already handled
            throw ex;
        } catch (NotFoundServiceEx ex) {
            LOGGER.warn("Problems updating user " + id + ": " + ex.getMessage(), ex);
            throw new NotFoundRestEx(ex.getMessage());
        } catch (BadRequestServiceEx ex) {
            LOGGER.warn("Problems updating user " + id + ": " + ex.getMessage(), ex);
            throw new BadRequestRestEx(ex.getMessage());
        } catch (Exception ex) {
            LOGGER.error(ex);
            throw new InternalErrorRestEx(ex.getMessage());
        }
    }


    @Override
    public RESTShortUserList getUsers(String nameLike, Integer page, Integer entries) throws BadRequestRestEx, InternalErrorRestEx {
        try{
            List<GSUser> list = userService.getFullList(nameLike, page, entries);
            RESTShortUserList ret = new RESTShortUserList(list.size());
            for (GSUser user : list) {
                ret.add(toShortUser(user));
            }
            return ret;

        } catch(BadRequestServiceEx ex) {
            LOGGER.warn(ex.getMessage());
            throw new BadRequestRestEx(ex.getMessage());
        } catch(Exception ex) {
            LOGGER.warn("Unexpected exception", ex);
            throw new InternalErrorRestEx(ex.getMessage());
        }
    }

    @Override
    public long getCount(String nameLike) {
        return userService.getCount(nameLike);
    }

    // ==========================================================================
    public static RESTShortUser toShortUser(GSUser user) {
        RESTShortUser shu = new RESTShortUser();
        shu.setId(user.getId());
        shu.setExtId(user.getExtId());
        shu.setUserName(user.getName());
        shu.setEnabled(user.getEnabled());

        return shu;
    }

    public static RESTOutputUser toOutputUser(GSUser user) {
        RESTOutputUser ret = new RESTOutputUser();
        ret.setId(user.getId());
        ret.setExtId(user.getExtId());
        ret.setName(user.getName());
        ret.setEnabled(user.getEnabled());
        ret.setAdmin(user.isAdmin());
        ret.setFullName(user.getFullName());
        ret.setEmailAddress(user.getEmailAddress());

        List<IdName> groups = new ArrayList<IdName>();
        for (UserGroup group : user.getGroups()) {
            IdName nameId = new IdName(group.getId(), group.getName());
            groups.add(nameId);
        }
        ret.setGroups(groups);

        return ret;
    }



    // ==========================================================================
    public void setUserAdminService(UserAdminService service) {
        this.userService = service;
    }

    public void setUserGroupAdminService(UserGroupAdminService service) {
        this.userGroupService = service;
    }

    @Override
    public void addIntoGroup(Long userId, String groupName) throws BadRequestRestEx, NotFoundRestEx, InternalErrorRestEx {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addIntoGroup(Long userId, Long groupId) throws BadRequestRestEx, NotFoundRestEx, InternalErrorRestEx {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addIntoGroup(String userName, String groupName) throws BadRequestRestEx, NotFoundRestEx, InternalErrorRestEx {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addIntoGroup(String userName, Long groupId) throws BadRequestRestEx, NotFoundRestEx, InternalErrorRestEx {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeFromGroup(Long userId, String groupName) throws BadRequestRestEx, NotFoundRestEx, InternalErrorRestEx {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeFromGroup(Long userId, Long groupId) throws BadRequestRestEx, NotFoundRestEx, InternalErrorRestEx {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public void removeFromGroup(String userName, String groupName) throws BadRequestRestEx, NotFoundRestEx, InternalErrorRestEx {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeFromGroup(String userName, Long groupId) throws BadRequestRestEx, NotFoundRestEx, InternalErrorRestEx {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
