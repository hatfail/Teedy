/*
 * @Author: hatfail 1833943280@qq.com
 * @Date: 2025-05-21 21:43:49
 * @LastEditors: hatfail 1833943280@qq.com
 * @LastEditTime: 2025-05-22 11:01:35
 * @FilePath: \Teedy\docs-core\src\main\java\com\sismics\docs\core\dao\UserRegisterRequestDao.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package com.sismics.docs.core.dao;

import com.sismics.docs.core.model.jpa.UserRegisterRequest;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
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
        req.setStatus("pending");
        // 确保storage字段有值
        if (req.getStorageQuota() == null) {
            req.setStorageQuota(1000000L); // 默认存储大小
        }

        EntityManager em = ThreadLocalContext.get().getEntityManager();
        EntityTransaction tx = em.getTransaction();
        boolean shouldCommit = false;

        try {
            // 只有当事务未激活时才开始新事务
            if (!tx.isActive()) {
                tx.begin();
                shouldCommit = true;
            }

            // 执行持久化操作
            em.persist(req);

            // 只有我们自己开始的事务才需要提交
            if (shouldCommit) {
                tx.commit();
            }
            return req.getId();
        } catch (Exception e) {
            // 只有我们自己开始的事务才需要回滚
            if (shouldCommit && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }

    public List<UserRegisterRequest> findAllPending() {
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        try {
            // 获取实体类中实际的字段名
            TypedQuery<UserRegisterRequest> q = em.createQuery(
                    "SELECT r FROM UserRegisterRequest r WHERE r.status = :status", UserRegisterRequest.class);
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
