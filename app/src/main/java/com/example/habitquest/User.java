package com.example.habitquest;

public class User {

    private int id;
    private String username;
    private String email;
    private int xp;
    private int level;
    private String createdAt;

    public User() {
    }

    public User(int id, String username, String email, int xp, int level, String createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.xp = xp;
        this.level = level;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getInitial() {
        if (username == null || username.isEmpty()) {
            return "?";
        }
        return username.substring(0, 1).toUpperCase();
    }

    public String getTitle() {
        return LevelSystem.titleForLevel(level);
    }
}
