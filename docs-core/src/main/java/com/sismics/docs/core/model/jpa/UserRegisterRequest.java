package com.sismics.docs.core.model.jpa;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "T_REGISTER_USER")
@org.hibernate.annotations.DynamicUpdate
public class UserRegisterRequest {
    @Id
    @Column(name = "REG_ID_C")
    private String id;

    @Column(name = "REG_USERNAME_C", nullable = false)
    private String username;

    @Column(name = "REG_EMAIL_C")
    private String email;

    @Column(name = "REG_PASSWORD_C")
    private String password;

    @Column(name = "REG_SUBMIT_TIME_D", nullable = false)
    private Date createDate;

    @Column(name = "REG_STATUS_N")
    private Integer status; // 0-pending, 1-approved, 2-rejected

    @Column(name = "REG_OPERATED_TIME_D")
    private Date operatedDate;

    @Column(name = "REG_STORAGE_N")
    private Long storageQuota = 10000L; // 默认10GB

    // 默认构造函数
    public UserRegisterRequest() {
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getStatus() {
        if (this.status == null)
            return "pending";
        if (this.status == 0)
            return "pending";
        if (this.status == 1)
            return "approved";
        if (this.status == 2)
            return "rejected";
        return "unknown";
    }

    public void setStatus(String status) {
        if ("pending".equals(status)) {
            this.status = 0;
        } else if ("approved".equals(status)) {
            this.status = 1;
        } else if ("rejected".equals(status)) {
            this.status = 2;
        }
    }

    public Integer getStatusCode() {
        return this.status;
    }

    public void setStatus(Integer statusCode) {
        this.status = statusCode;
    }

    public Date getOperatedDate() {
        return operatedDate;
    }

    public void setOperatedDate(Date operatedDate) {
        this.operatedDate = operatedDate;
    }

    public Long getStorageQuota() {
        return storageQuota;
    }

    public void setStorageQuota(Long storageQuota) {
        this.storageQuota = storageQuota;
    }
}
