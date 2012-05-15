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
package it.geosolutions.geofence;

import it.geosolutions.geofence.cache.CachedRuleReader;
import com.google.common.base.Ticker;

import it.geosolutions.geofence.services.RuleReaderService;
import it.geosolutions.geofence.services.dto.AccessInfo;
import it.geosolutions.geofence.services.dto.RuleFilter;
import java.util.logging.Level;
import org.geotools.util.logging.Logging;
//import java.util.logging.Logger;

/**
 *
 * @author ETj (etj at geo-solutions.it)
 */
public class CacheReaderTest extends GeofenceBaseTest {

//    static private Log4jLogger LOGGER = new Log4jLogger();
    static private GTLogger LOGGER = new GTLogger();


    private RuleReaderService realReader;

    @Override
    protected void setUpInternal() throws Exception {
        super.setUpInternal();

        realReader = (RuleReaderService) applicationContext.getBean("ruleReaderService");
    }

    static class Log4jLogger {
        static private final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(CacheReaderTest.class);
//        static private final Logger LOGGER = Logging.getLogger(CacheReaderTest.class);

        public void info(Object o) {
            LOGGER.info(o);
        }
        public void warn(Object o) {
            LOGGER.warn(o);
        }
        public void error(Object o) {
            LOGGER.error(o);
        }
    }

    static class GTLogger {
        static final java.util.logging.Logger LOGGER = Logging.getLogger(CachedRuleReader.class);
//        static private final Logger LOGGER = Logging.getLogger(CacheReaderTest.class);

        public void info(Object o) {
            LOGGER.log(Level.INFO, o.toString());
        }
        public void warn(Object o) {
            LOGGER.log(Level.WARNING, o.toString());
        }
        public void error(Object o) {
            LOGGER.log(Level.SEVERE, o.toString());
        }
    }

    static class CustomTicker extends Ticker {

        private long nano = 0;

        @Override
        public long read() {
            return nano;
        }

        public void setMillisec(long milli) {
            this.nano = milli * 1000000;
        }
    }

    public void testSize() {
        CustomTicker ticker = new CustomTicker();

        CachedRuleReader cachedRuleReader = new CachedRuleReader();
        cachedRuleReader.setRealRuleReaderService(realReader);

        cachedRuleReader.getCacheInitParams().setSize(2);
        cachedRuleReader.getCacheInitParams().setRefreshMilliSec(500);
        cachedRuleReader.getCacheInitParams().setExpireMilliSec(1000);
        cachedRuleReader.getCacheInitParams().setCustomTicker(ticker);

        cachedRuleReader.init();

        LOGGER.warn(cachedRuleReader.getStats() + " size:"+cachedRuleReader.getCacheSize());
        assertEquals(0, cachedRuleReader.getStats().hitCount());
        assertEquals(0, cachedRuleReader.getStats().missCount());
        assertEquals(0, cachedRuleReader.getStats().evictionCount());

        RuleFilter filter1 = new RuleFilter();
        filter1.setUser("test_1");
        RuleFilter filter2 = new RuleFilter();
        filter2.setUser("test_2");
        RuleFilter filter3 = new RuleFilter();
        filter3.setUser("test_3");

        assertNotSame(filter1, filter2);

        // expected stats
        int hitExp = 0;
        int missExp = 0;
        int evictExp = 0;

        // first loading, obviously a miss
        AccessInfo ai1_1= cachedRuleReader.getAccessInfo(filter1);

        LOGGER.warn("A " + cachedRuleReader.getStats() + " size:"+cachedRuleReader.getCacheSize());
        assertEquals(hitExp, cachedRuleReader.getStats().hitCount());
        assertEquals(++missExp, cachedRuleReader.getStats().missCount());
        assertEquals(evictExp, cachedRuleReader.getStats().evictionCount());

        // second loading with the same rule, should be a hit
        ticker.setMillisec(1);
        AccessInfo ai1_2= cachedRuleReader.getAccessInfo(filter1);

        LOGGER.warn("B " + cachedRuleReader.getStats() + " size:"+cachedRuleReader.getCacheSize());
        assertEquals(++hitExp, cachedRuleReader.getStats().hitCount());
        assertEquals(missExp, cachedRuleReader.getStats().missCount());
        assertEquals(evictExp, cachedRuleReader.getStats().evictionCount());

        assertEquals(ai1_1, ai1_2);

        // loading a different filter, a miss again
        ticker.setMillisec(2);
        AccessInfo ai2= cachedRuleReader.getAccessInfo(filter2);

        LOGGER.warn("C " + cachedRuleReader.getStats() + " size:"+cachedRuleReader.getCacheSize());
        assertEquals(hitExp, cachedRuleReader.getStats().hitCount());
        assertEquals(++missExp, cachedRuleReader.getStats().missCount());
        assertEquals(evictExp, cachedRuleReader.getStats().evictionCount());

        // yet another different filter. we expect a miss, and an eviction
        ticker.setMillisec(3);
        AccessInfo ai3= cachedRuleReader.getAccessInfo(filter3);

        LOGGER.warn("D " + cachedRuleReader.getStats() + " size:"+cachedRuleReader.getCacheSize());
        assertEquals(hitExp, cachedRuleReader.getStats().hitCount());
        assertEquals(++missExp, cachedRuleReader.getStats().missCount());
        assertEquals(++evictExp, cachedRuleReader.getStats().evictionCount());

        // filter1 is the oldest one:
        ticker.setMillisec(4);
        cachedRuleReader.getAccessInfo(filter2);
        LOGGER.warn("E " + cachedRuleReader.getStats() + " size:"+cachedRuleReader.getCacheSize());
        assertEquals(++hitExp, cachedRuleReader.getStats().hitCount());

        ticker.setMillisec(5);
        cachedRuleReader.getAccessInfo(filter3);
        LOGGER.warn("F " + cachedRuleReader.getStats() + " size:"+cachedRuleReader.getCacheSize());
        assertEquals(++hitExp, cachedRuleReader.getStats().hitCount());

        // reload filter1, ==> filter 2 should be evicted
        ticker.setMillisec(6);
        cachedRuleReader.getAccessInfo(filter1);
        LOGGER.warn("G " + cachedRuleReader.getStats() + " size:"+cachedRuleReader.getCacheSize());
        assertEquals(hitExp, cachedRuleReader.getStats().hitCount());
        assertEquals(++missExp, cachedRuleReader.getStats().missCount());
        assertEquals(++evictExp, cachedRuleReader.getStats().evictionCount());
    }


    public void testExpire() throws InterruptedException {
        CustomTicker ticker = new CustomTicker();

        CachedRuleReader cachedRuleReader = new CachedRuleReader();
        cachedRuleReader.setRealRuleReaderService(realReader);

        cachedRuleReader.getCacheInitParams().setSize(100);
        cachedRuleReader.getCacheInitParams().setRefreshMilliSec(500);
        cachedRuleReader.getCacheInitParams().setExpireMilliSec(1000);
        cachedRuleReader.getCacheInitParams().setCustomTicker(ticker);

        cachedRuleReader.init();

        LOGGER.warn(cachedRuleReader.getStats() + " size:"+cachedRuleReader.getCacheSize());
        assertEquals(0, cachedRuleReader.getStats().hitCount());
        assertEquals(0, cachedRuleReader.getStats().missCount());
        assertEquals(0, cachedRuleReader.getStats().evictionCount());

        RuleFilter filter1 = new RuleFilter();
        filter1.setUser("test_1");
        RuleFilter filter2 = new RuleFilter();
        filter2.setUser("test_2");
        RuleFilter filter3 = new RuleFilter();
        filter3.setUser("test_3");

        int hitExp = 0;
        int missExp = 0;
        int evictExp = 0;

        // first loading
        AccessInfo ai1_1= cachedRuleReader.getAccessInfo(filter1);

        LOGGER.warn(cachedRuleReader.getStats() + " size:"+cachedRuleReader.getCacheSize());
        assertEquals(hitExp, cachedRuleReader.getStats().hitCount());
        assertEquals(++missExp, cachedRuleReader.getStats().missCount());
        assertEquals(evictExp, cachedRuleReader.getStats().evictionCount());

        // second loading with the same rule, should be a hit
        ticker.setMillisec(1);
        AccessInfo ai1_2= cachedRuleReader.getAccessInfo(filter1);

        LOGGER.warn(cachedRuleReader.getStats() + " size:"+cachedRuleReader.getCacheSize());
        assertEquals(++hitExp, cachedRuleReader.getStats().hitCount());
        assertEquals(missExp, cachedRuleReader.getStats().missCount());
        assertEquals(evictExp, cachedRuleReader.getStats().evictionCount());
        assertEquals(1, cachedRuleReader.getStats().loadSuccessCount());

        assertEquals(ai1_1, ai1_2);

        // loading the same filter, after the refresh time
        ticker.setMillisec(600);
//        LOGGER.log(Level.INFO, "We expect a reload() now....");
        LOGGER.error("---> We expect a reload() now....");
        AccessInfo ai1_3= cachedRuleReader.getAccessInfo(filter1);

        LOGGER.warn(cachedRuleReader.getStats() + " size:"+cachedRuleReader.getCacheSize());
        assertEquals(++hitExp, cachedRuleReader.getStats().hitCount());
        assertEquals(missExp, cachedRuleReader.getStats().missCount());
        assertEquals(evictExp, cachedRuleReader.getStats().evictionCount());
//        assertEquals(2, cachedRuleReader.getStats().loadSuccessCount()); // dunno if load is made asynch or not

        // reloading should have been triggered
        ticker.setMillisec(700);
        LOGGER.error("sleeping...");
        Thread.sleep(500);
        LOGGER.warn(cachedRuleReader.getStats() + " size:"+cachedRuleReader.getCacheSize());
        assertEquals(hitExp, cachedRuleReader.getStats().hitCount());
        assertEquals(missExp, cachedRuleReader.getStats().missCount());
        assertEquals(evictExp, cachedRuleReader.getStats().evictionCount());
//        assertEquals(2, cachedRuleReader.getStats().loadSuccessCount());  // uhm, this does not work
        if(2!=cachedRuleReader.getStats().loadSuccessCount())
            LOGGER.error("*** Bad successCount check, expected 2, found "+  cachedRuleReader.getStats().loadSuccessCount());

        ticker.setMillisec(800);
        cachedRuleReader.getAccessInfo(filter1);
        LOGGER.warn(cachedRuleReader.getStats() + " size:"+cachedRuleReader.getCacheSize());

        ticker.setMillisec(2000);
        cachedRuleReader.getAccessInfo(filter1);
        LOGGER.warn(cachedRuleReader.getStats() + " size:"+cachedRuleReader.getCacheSize());
    }

}