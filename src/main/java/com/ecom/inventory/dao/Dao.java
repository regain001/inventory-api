package com.ecom.inventory.dao;

import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
Helper Provided by Fahim Ahmed
 */
@Repository
@Transactional
public class Dao {
    @Autowired
    protected EntityManager entityManager;
    

    private static SessionFactory sessionFactory;
    private static final String CONNECTION_URL = "jdbc:postgresql://180.210.128.4:8091/ndub_mirror_2";
    private static final String DIALECT = "org.hibernate.dialect.PostgreSQLDialect";
    private static final String CONN_USERNAME = "postgres";
    private static final String CONN_PASS = "C0mml1nk";
    private static final Boolean FACTORY_LOCK = true;
    private static Boolean IS_FACTORY_INITIALIZED = false;
    
    protected Dao() {
    }
    
    public Session getCurrentSession() {
        return entityManager.unwrap(Session.class);
    }
    
    public static Session createNewSession(){
        initSessionFactory();
        try {
            Session session = sessionFactory.openSession();
            session.beginTransaction();
            return session;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
    public static void initSessionFactory() {
        try {
            synchronized(FACTORY_LOCK){
                if(!IS_FACTORY_INITIALIZED){
                    System.out.println("Trying to build session factory... URL: " + CONNECTION_URL);
                    Configuration cfg = new Configuration().configure();
                    cfg.setProperty("hibernate.dialect", DIALECT)
                            .setProperty("hibernate.connection.url", CONNECTION_URL)
                            .setProperty("hibernate.connection.username", CONN_USERNAME)
                            .setProperty("hibernate.connection.password", CONN_PASS)
                            .setProperty("hibernate.current_session_context_class", "thread")
                            .setProperty("hibernate.connection.autocommit", "false");
                    sessionFactory = cfg.buildSessionFactory();
                    System.out.println("Session factory built.");
                    IS_FACTORY_INITIALIZED = true;
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to build session factory...");
            e.printStackTrace();
        }
    }
    
    public static <E> E get(Session session, Class<E> clazz, Object id) {
        if (id == null) {
            return null;
        }
        return (E) session.find(clazz, (Serializable)id);
    }

    public static <E> List<E> getList(Session session, Class<E> clazz) {
        Query q = session.createQuery("from " + clazz.getSimpleName());
        List<E> list = q.list();
        return list;
    }

    public static <E> List<E> getListByIds(Session session, Class<E> clazz, List<Integer> ids) {
        List<E> ret = new ArrayList<>();
        try {
            for (Integer id : ids) {
                ret.add((E) session.find(clazz, id));
            }
        } catch (Exception e) {
        }
        return ret;
    }

    public static <E> Map<Integer, E> getListByIdsWithMap(Session session, Class<E> clazz, List<Integer> ids) {
        Map<Integer, E> ret = new HashMap<>();
        try {
            for (Integer id : ids) {
                ret.put(id, (E) session.find(clazz, id));
            }
        } catch (Exception e) {
        }
        return ret;
    }

    public static <E> List<E> getAll(Session session, Class<E> clazz) {
        List<E> ret = new ArrayList<>();
        try {
            ret = session.createQuery("from " + clazz.getName()).list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static <E> E persist(Session session, E entity) {
        session.persist(entity);
        return entity;
    }

    public static <E> E saveOrUpdate(Session session, E entity) {
        session.persist(entity);
        return entity;
    }

    public static <E> E merge(Session session, E entity) {
        try {
            session.merge(entity);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entity;
    }

    public static void delete(Session session, Object entity) {
        session.remove(entity);
    }

    public static void commit(Session session) {
        if (session != null) {
            try {
                session.getTransaction().commit();
            } catch (Exception e) {
                if (isSocketExceptionOccured(e)) {
                    invalidateSessionFactory(session);
                }
                e.printStackTrace();
                try {
                    session.getTransaction().rollback();
                } catch (Exception ex) {
                    //ex.printStackTrace();
                }
            }
        }
    }

    private static Boolean isSocketExceptionOccured(Exception e) {
        Throwable pred = e;
        while (true) {
            if (pred == null) {
                break;
            }
            if (pred instanceof SocketException) {
                System.out.println("---DETECTED SocketException during session handling, need to invalidate SessionFactory");
                return true;
            }
            pred = pred.getCause();
        }
        return false;
    }

    private static void invalidateSessionFactory(Session session) {
        System.out.println("---Invalidating factory");
        try {
            SessionFactory factory = session.getSessionFactory();

            if (factory.equals(sessionFactory)) {
                synchronized (FACTORY_LOCK) {
                    sessionFactory.close();
                    System.out.println("Session Factory closed");
                    IS_FACTORY_INITIALIZED = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void flush(Session session) {
        try {
            session.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void rollback(Session session) {
        if (session != null && session.isOpen() && session.getTransaction().isActive()) {
            session.getTransaction().rollback();
        }
    }

    public static void close(Session session) {
        if (session != null) {
            commit(session);
            try {
                session.close();
            } catch (Exception e) {
            }
        }
    }

    public static String getTableNameFromEntity(Class entityClass) {
        if (entityClass != null) {
            if (entityClass.isAnnotationPresent(Entity.class)) {
                if (entityClass.isAnnotationPresent(Table.class)) {
                    Table annotation = (Table) entityClass.getAnnotation(Table.class);
                    return annotation.name();
                } else {
                    return entityClass.getSimpleName();
                }
            }
        }
        return null;
    }

    public static <E> List<E> getListByQuery(Session session, String query) {
        Query q = session.createQuery(query);
        List<E> list = q.list();
        return list;
    }

    public static <E> E getByQuery(Session session, String query) {
        Query q = session.createQuery(query);
        E entity = (E) q.getSingleResult();
        return entity;
    }

    public static Long getCountByQuery(Session session, String query) {
        Query q = session.createQuery(query);
        Long count = ((Long) q.uniqueResult());
        return count;
    }

    public static Long getCountBySqlQuery(Session session, String queryStr) {
        Number result = (Number) session
                .createNativeQuery(queryStr)
                .getSingleResult();

        return result.longValue();
    }
}
