/*
 * $ Header: it.geosolutions.geofence.gui.client.mvc.MapController,v. 0.1 25-gen-2011 11.30.32 created by afabiani <alessio.fabiani at geo-solutions.it> $
 * $ Revision: 0.1 $
 * $ Date: 25-gen-2011 11.30.32 $
 *
 * ====================================================================
 *
 * Copyright (C) 2007 - 2011 GeoSolutions S.A.S.
 * http://www.geo-solutions.it
 *
 * GPLv3 + Classpath exception
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by developers
 * of GeoSolutions.  For more information on GeoSolutions, please see
 * <http://www.geo-solutions.it/>.
 *
 */
package it.geosolutions.geofence.gui.client.mvc;

import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;

import it.geosolutions.geofence.gui.client.GeofenceEvents;
import it.geosolutions.geogwt.gui.client.GeoGWTEvents;


// TODO: Auto-generated Javadoc
/**
 * The Class MapController.
 */
public class MapController extends Controller
{

    /** The map view. */
    private MapView mapView;

    /**
     * Instantiates a new map controller.
     */
    public MapController()
    {
        registerEventTypes(
            GeofenceEvents.INIT_MAPS_UI_MODULE,
            GeofenceEvents.ATTACH_MAP_WIDGET,
            GeofenceEvents.UPDATE_MAP_SIZE,
            GeofenceEvents.ATTACH_TOOLBAR,
            GeofenceEvents.ACTIVATE_DRAW_FEATURES,
            GeofenceEvents.DEACTIVATE_DRAW_FEATURES,
            GeofenceEvents.ERASE_AOI_FEATURES,
            GeofenceEvents.ENABLE_DRAW_BUTTON,
            GeofenceEvents.DISABLE_DRAW_BUTTON,
            GeofenceEvents.DRAW_AOI_ON_MAP,
            GeofenceEvents.ZOOM_TO_CENTER,
            GeofenceEvents.ADMIN_MODE_CHANGE,
            GeofenceEvents.LOGIN_SUCCESS,
            GeoGWTEvents.INJECT_WKT);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.extjs.gxt.ui.client.mvc.Controller#initialize()
     */
    @Override
    public void initialize()
    {
        this.mapView = new MapView(this);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.extjs.gxt.ui.client.mvc.Controller#handleEvent(com.extjs.gxt.ui.client
     * .mvc.AppEvent)
     */
    @Override
    public void handleEvent(AppEvent event)
    {
        forwardToView(mapView, event);

    }

}
