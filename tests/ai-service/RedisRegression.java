package io.dataease.ai;

import io.dataease.ai.dao.entity.CoreAiSetting;
import io.dataease.ai.dao.repository.CoreAiSettingRepository;
import io.dataease.ai.support.*;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public final class RedisRegression {
    private static void blocked(Runnable action){try{action.run();}catch(AiErrors.Failure expected){return;}throw new AssertionError("expected admission blocked");}
    private static AiAdmission node(CoreAiSettingRepository settings,PlatformTransactionManager manager,EntityManager em,RedisConnectionFactory connection,String namespace)throws Exception{
        var beanFactory=new StaticListableBeanFactory(Map.of("redis",connection));
        AiAdmission node=new AiAdmission("redis","distributed",beanFactory.getBeanProvider(RedisConnectionFactory.class),settings,namespace,manager);
        Field field=AiAdmission.class.getDeclaredField("em");field.setAccessible(true);field.set(node,em);return node;
    }
    public static void run(CoreAiSettingRepository settings,PlatformTransactionManager manager,EntityManager em,int port)throws Exception{
        LettuceConnectionFactory connection=new LettuceConnectionFactory("127.0.0.1",port);connection.afterPropertiesSet();connection.start();
        try{
            String namespace="test_"+UUID.randomUUID().toString().replace("-","");
            String prefix="de:ai:{"+namespace+":admission}:";
            StringRedisTemplate redis=new StringRedisTemplate(connection);
            AiAdmission a=node(settings,manager,em,connection,namespace),b=node(settings,manager,em,connection,namespace);
            CoreAiSetting s=settings.findById(1L).orElseThrow();
            blocked(()->a.acquire(settings.findById(1L).orElseThrow(),false,System.currentTimeMillis()+10000));
            a.recover();if(!"RECOVERING".equals(a.state(s).name()))throw new AssertionError("missing key must wait");
            redis.opsForHash().put(prefix+"policy","ready","0");a.recover();s=settings.findById(1L).orElseThrow();
            if(!"ACTIVE".equals(a.state(s).name()))throw new AssertionError("recovery under DB lock");
            List<AiAdmission.Permit> permits=new ArrayList<>();
            for(int i=0;i<s.getInteractiveConcurrency();i++)permits.add((i%2==0?a:b).acquire(s,false,System.currentTimeMillis()+10000));
            CoreAiSetting current=s;blocked(()->b.acquire(current,false,System.currentTimeMillis()+10000));
            var oldBackground=b.acquire(s,true,System.currentTimeMillis()+10000);
            permits.get(0).close();a.acquire(s,false,System.currentTimeMillis()+10000).close();
            redis.delete(prefix+"I");blocked(()->a.acquire(current,false,System.currentTimeMillis()+10000));
            if(!"RECOVERING".equals(b.state(s).name()))throw new AssertionError("pool loss not detected");
            a.recover();if(!"RECOVERING".equals(a.state(s).name()))throw new AssertionError("premature recovery");
            redis.opsForHash().put(prefix+"policy","ready","0");a.recover();s=settings.findById(1L).orElseThrow();
            List<AiAdmission.Permit> background=new ArrayList<>();for(int i=0;i<s.getBackgroundConcurrency();i++)background.add(a.acquire(s,true,System.currentTimeMillis()+10000));
            oldBackground.close();CoreAiSetting afterLoss=s;blocked(()->b.acquire(afterLoss,true,System.currentTimeMillis()+10000));background.forEach(AiAdmission.Permit::close);permits.forEach(AiAdmission.Permit::close);
            String stale=a.beginUpdate(s);redis.opsForHash().put(prefix+"policy","ready","0");b.recover();
            CoreAiSetting recovered=settings.findById(1L).orElseThrow();blocked(()->a.assertUpdateOwner(stale,recovered));blocked(()->a.finishUpdate(stale,recovered,true));
            String held=a.beginUpdate(recovered);
            CountDownLatch locked=new CountDownLatch(1),resume=new CountDownLatch(1);
            AtomicReference<Throwable> failure=new AtomicReference<>();
            Thread writer=new Thread(()->{
                try{
                    new TransactionTemplate(manager).executeWithoutResult(tx->{
                        CoreAiSetting setting=settings.findById(1L).orElseThrow();
                        if(em.createQuery("update CoreAiSetting s set s.rowVersion=s.rowVersion+1 where s.id=1 and s.rowVersion=:v").setParameter("v",setting.getRowVersion()).executeUpdate()!=1)throw new AssertionError("CAS");
                        em.refresh(setting);a.assertUpdateOwner(held,setting);locked.countDown();
                        try{resume.await();}catch(InterruptedException e){Thread.currentThread().interrupt();throw new RuntimeException(e);}
                        setting.setRevision(setting.getRevision()+1);em.flush();
                    });
                    a.finishUpdate(held,settings.findById(1L).orElseThrow(),true);
                }catch(Throwable error){failure.set(error);locked.countDown();}
            });writer.start();if(!locked.await(5,TimeUnit.SECONDS))throw new AssertionError("writer not locked");
            redis.opsForHash().put(prefix+"policy","ready","0");
            Thread recover=new Thread(b::recover);recover.start();Thread.sleep(100);
            if(!"UPDATING".equals(a.state(recovered).name()))throw new AssertionError("recovery bypassed held DB lock");
            resume.countDown();writer.join(5000);recover.join(5000);if(failure.get()!=null)throw new AssertionError("writer failed",failure.get());
            CoreAiSetting persisted=settings.findById(1L).orElseThrow();
            if(a.state(persisted).revision()!=persisted.getRevision())throw new AssertionError("published revision");
            String lostPublication=a.beginUpdate(persisted);
            new TransactionTemplate(manager).executeWithoutResult(tx->{CoreAiSetting setting=settings.findById(1L).orElseThrow();setting.setRevision(setting.getRevision()+1);em.flush();});
            redis.opsForHash().put(prefix+"policy","ready","0");b.recover();CoreAiSetting authoritative=settings.findById(1L).orElseThrow();
            if(a.state(authoritative).revision()!=authoritative.getRevision())throw new AssertionError("recovery ignored committed DB revision");
            blocked(()->a.finishUpdate(lostPublication,authoritative,true));
            var other=node(settings,manager,em,connection,namespace+"_other");other.state(authoritative);
            if(!"ACTIVE".equals(a.state(authoritative).name()))throw new AssertionError("namespace collision");
            System.out.println("PASS AI owned Redis two-node I/B admission, pool loss, epoch release, stale writer fence, held DB lock and post-commit recovery");
        }finally{connection.destroy();}
    }
}
