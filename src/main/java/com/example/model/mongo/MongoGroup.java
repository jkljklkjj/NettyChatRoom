package com.example.model.mongo;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.List;
import java.beans.Transient;
import java.util.ArrayList;

@Document(collection = "groups")
public class MongoGroup {
    @Id
    private ObjectId id;
    @Indexed(unique = true)
    private int groupId;
    private int admin;
    private List<Integer> members;

    public MongoGroup() {
        groupId = -1;
        admin = -1;
        members = new ArrayList<>();
    }

    public MongoGroup(int groupId, int admin) {
        this.groupId = groupId;
        this.admin = admin;
        members = new ArrayList<>();
    }

    public MongoGroup(int groupId){
        this.groupId = groupId;
        admin = -1;
        members = new ArrayList<>();
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getAdmin() {
        return admin;
    }

    public void setAdmin(int admin) {
        this.admin = admin;
    }

    public List<Integer> getMembers() {
        return members;
    }

    public void setMembers(List<Integer> members) {
        this.members = members;
    }

    public boolean removeMember(int memberId) {
        return members.remove(Integer.valueOf(memberId));
    }

    @Transactional
    public boolean addMember(int memberId) {
        if (members.contains(memberId)) {
            return false;
        }
        members.add(memberId);
        return true;
    }

    @Override
    public String toString() {
        return "MongoGroup{" +
                "id=" + id +
                ", groupid=" + groupId +
                '}';
    }
}