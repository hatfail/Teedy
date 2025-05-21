package com.sismics.docs.core.dao;

import com.sismics.docs.core.model.jpa.UserRegisterRequest;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class UserRegisterRequestDao {

    public String create(UserRegisterRequest req) {
        req.setId(UUID.randomUUID().toString());
        req.setCreateDate(new Date());
        // 使用setStatus方法设置状态
        req.setStatus(0);
        // 确保storage字段有值
        if (req.getStorageQuota() == null) {
            req.setStorageQuota(1000000L); // 默认存储大小
        }

        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.persist(req);
        return req.getId();
    }

    public List<UserRegisterRequest> findAllPending() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            TypedQuery<UserRegisterRequest> q = em.createQuery(
                    "SELECT r FROM UserRegisterRequest r WHERE r.statusCode = :status", UserRegisterRequest.class);
            q.setParameter("status", 0);
            return q.getResultList();
        } catch (Exception e) {
            if (e.getMessage().contains("not found")) {
                return new ArrayList<>();
            }
            throw e;
        }
    }

    public UserRegisterRequest getById(String id) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        return em.find(UserRegisterRequest.class, id);
    }

    public void update(UserRegisterRequest req) {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        em.merge(req);
    }
}
