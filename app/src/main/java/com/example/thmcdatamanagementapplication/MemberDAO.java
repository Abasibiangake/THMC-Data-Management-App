package com.example.thmcdatamanagementapplication;

public class MemberDAO {
    public String uid, memberFullName, memberAddress, memberEmail, memberGender, memberPhoneNo;

    public MemberDAO() {
        // Default no-argument constructor required by Firebase
    }

    public MemberDAO(String uid, String memberFullName, String memberAddress, String memberEmail, String memberGender, String memberPhoneNo){
        this.uid = uid;
        this.memberFullName = memberFullName;
        this.memberAddress = memberAddress;
        this.memberEmail = memberEmail;
        this.memberGender = memberGender;
        this.memberPhoneNo = memberPhoneNo;
    }

}
