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
package it.geosolutions.geofence.cache.rest;

import com.google.common.cache.CacheStats;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class RESTCacheStats extends Resource {

    private final CacheStats stats;

    RESTCacheStats(Context context, Request request, Response response, CacheStats stats) {
        super(context, request, response);
        this.stats = stats;
    }

    @Override
    public void handleGet() {
        Representation representation = new StringRepresentation(stats.toString());
        getResponse().setEntity(representation);
    }
}
