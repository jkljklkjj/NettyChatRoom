package com.example.model.mongo;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;


import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
public class MongoUser {
    @Id
    private ObjectId id;
    @Indexed(unique = true)
    private int userId; // 对应 MySQL 中的用户 ID
    private List<Integer> friends;
    private List<Integer> groups;

    public MongoUser() {
        userId = 0;
        friends = new ArrayList<>();
        groups = new ArrayList<>();
    }

    public MongoUser(int userId, List<Integer> friends, List<Integer> groups) {
        this.userId = userId;
        this.friends = friends != null ? friends : new ArrayList<>();
        this.groups = groups != null ? groups : new ArrayList<>();
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public List<Integer> getFriends() {
        return friends;
    }

    public void setFriends(List<Integer> friends) {
        this.friends = friends;
    }

    public List<Integer> getGroups() {
        return groups;
    }

    public void setGroups(List<Integer> groups) {
        this.groups = groups;
    }

    public boolean addFriend(int friendId) {
        if(friends==null){
            friends = new ArrayList<>();
        }

        if(friends.contains(friendId)){
            return false;
        }

        return friends.add(friendId);
    }

    public boolean delFriend(int friendId) {
        if(friends==null){
            friends = new ArrayList<>();
        }

        if(!friends.contains(friendId)){
            return false;
        }

        boolean res = friends.remove(Integer.valueOf(friendId));
        return res;
    }

    public boolean addGroup(int groupId) {
        if(groups==null){
            groups = new ArrayList<>();
        }

        if(groups.contains(groupId)){
            return false;
        }

        return groups.add(groupId);
    }

    @Override
    public String toString() {
        return "MongoUser{" +
                "id=" + id +
                ", userId=" + userId +
                ", friends=" + friends +
                ", groups=" + groups +
                '}';
    }

}