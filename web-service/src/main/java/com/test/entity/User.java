package com.test.entity;


import java.util.Date;

public class User {

    private int id;
    private String userName;
    private Long permission;
    private Boolean isAdministrator;
    private Date createAt;

    public User(String userName, long permission, boolean isAdministrator, Date createAt) {

        this.userName = userName;
        this.permission = permission;
        this.isAdministrator = isAdministrator;
        this.createAt = createAt;
    }

    public User() {

    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getPermission() {
        return permission;
    }

    public void setPermission(Long permission) {
        this.permission = permission;
    }

    public Boolean getAdministrator() {
        return isAdministrator;
    }

    public void setAdministrator(Boolean administrator) {
        isAdministrator = administrator;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", permission=" + permission +
                ", isAdministrator=" + isAdministrator +
                ", createAt=" + createAt +
                '}';
    }
}
