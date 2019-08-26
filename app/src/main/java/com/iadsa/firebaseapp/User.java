package com.iadsa.firebaseapp;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is model class for firebase database object called users.
 */
public class User {
    private String name;
    private String mobile;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
