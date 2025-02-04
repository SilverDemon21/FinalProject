package com.example.finalproject.mainAplication;

import java.util.HashMap;

public class GroupOfUsers {
    private String groupId;
    private String groupType;
    private String groupName;
    private HashMap<String, String> groupUsers;
    private String groupState;

    public GroupOfUsers(String groupId, String groupType, String groupName, HashMap<String, String> groupUsers) {
        this.groupId = groupId;
        this.groupType = groupType;
        this.groupName = groupName;
        this.groupUsers = groupUsers;
        groupState = "Pending";
    }



    public String getGroupId() {return groupId;}
    public void setGroupId(String groupId) {this.groupId = groupId;}


    public String getGroupType() {return groupType;}
    public void setGroupType(String groupType) {this.groupType = groupType;}


    public String getGroupName() {return groupName;}
    public void setGroupName(String groupName) {this.groupName = groupName;}


    public HashMap<String, String> getGroupUsers() {return groupUsers;}
    public void setGroupUsers(HashMap<String, String> groupUsers) {this.groupUsers = groupUsers;}


    public String getGroupState() {return groupState;}
    public void setGroupState(String groupState) {this.groupState = groupState;}
}
