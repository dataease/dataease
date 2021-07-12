package io.dataease.listener;

import io.dataease.commons.constants.AuthConstants;
import io.dataease.listener.util.CacheUtils;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.CacheEventListenerFactory;
import org.springframework.stereotype.Component;
import java.util.Properties;

@Component
public class LicCacheEventListener extends CacheEventListenerFactory implements CacheEventListener {

    private static CacheEventListener cacheEventListener;

    public LicCacheEventListener() {
        cacheEventListener = cacheEventListener == null ? this : cacheEventListener;
    }

    @Override
    public void notifyElementRemoved(Ehcache ehcache, Element element) throws CacheException {
        /*System.out.println("notifyElementRemoved");*/
    }

    @Override
    public void notifyElementPut(Ehcache ehcache, Element element) throws CacheException {

        /*long expirationTime = element.getExpirationTime();
        System.out.println(expirationTime);
        System.out.println("notifyElementPut");*/
    }

    @Override
    public void notifyElementUpdated(Ehcache ehcache, Element element) throws CacheException {
        /*System.out.println("notifyElementUpdated");*/
    }

    /**
     * lic过期触发： 清除用户、角色、权限缓存
     * @param ehcache
     * @param element
     */
    @Override
    public void notifyElementExpired(Ehcache ehcache, Element element) {
        CacheUtils.removeAll(AuthConstants.USER_CACHE_NAME);
        CacheUtils.removeAll(AuthConstants.USER_ROLE_CACHE_NAME);
        CacheUtils.removeAll(AuthConstants.USER_PERMISSION_CACHE_NAME);
    }

    @Override
    public void notifyElementEvicted(Ehcache ehcache, Element element) {
        /*System.out.println("notifyElementEvicted");*/
    }

    @Override
    public void notifyRemoveAll(Ehcache ehcache) {
        /*System.out.println("notifyRemoveAll");*/
    }

    @Override
    public void dispose() {

    }

    @Override
    public CacheEventListener createCacheEventListener(Properties properties) {
        return cacheEventListener;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
