package com.example.spacejuice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.example.spacejuice.activity.AllHabitsActivity;
import com.example.spacejuice.controller.HabitController;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


// Yuchen: Add tests to create a member and add score and add/subtract followers/followings

/**
 * This is a class to represent a user of the app
 */
public class Member {
   // Might need to redo accesses for these cuz i dunno shit abt private vs public - Harish
   private final String memberName;
   private final String memberPassword;
   private int uniqueId = 1; // this is the current state of the uniqueId value, not to be used as a unique Id for this.
   private int id; // Needs to be the primary id on Firestore
   private final ArrayList<Habit> habitListItems = new ArrayList<>();
   private int score;
   private int followers;
   private int followings;
   private final Follow follow;
   private Date nextMidnight;
   private Date prevNextMidnight;
   private Boolean admin = false;
   private long adminTimeOffset = 0;

   /**
    * Constructor for generate a empty Member
    */
   public Member() {
      this.memberName = "";
      this.memberPassword = "";

      //Set the score and social stats to 0
      this.score = 0;
      this.followers = 0;
      this.followings = 0;
      this.follow = new Follow();

   }

   /**
    * temporary constructor for setting up test members
    * @param name
    */
   public Member(String name) {

      this.memberName = name;
      this.memberPassword = "";

      //Set the score and social stats to 0
      this.score = 0;
      this.followers = 0;
      this.followings = 0;
      this.follow = new Follow();
   }

   /**
    * Constructor for user sign up.
    * @param memberName    Name also username for the member
    * @param memberPassword   Password use for login
    */
   public Member(String memberName, String memberPassword){
      this.memberName = memberName;
      this.memberPassword = memberPassword;

      //Set the score and social stats to 0
      this.score = 0;
      this.followers = 0;
      this.followings = 0;
      this.follow = new Follow();
   }



      // Getters -- Need to figure out how to compare password in login so we can access the private password
   //            and return true or false whether it matches or not during login.

   // Public function here that verifies password. so

   // member.verifyPassword(passwordAttempt)

   /**
    * Verify if entered password match to the user set
    * @param password   password try
    * @return  Return true if matched
    */
   public boolean verifyPassword(String password){
      return (password.equals(memberPassword));
   }

   public void initTestData() {
            /*
      initialization of TEST DATA
       */
   }

   /**
    * Checj if the member is current user logged in
    * @return  Return true this user is the logged in user
    */
   public Boolean isUser() {
      // returns true if this current Member is the User;
      // check for this before allowing certain methods to be used

      return (this == MainActivity.getUser());
   }

   /**
    * This returns the username/name of the member
    * @return  Return name
    */
   public String getMemberName() {
      return memberName;
   }

   /**
    * This returns the score that member has
    * @return  Return score
    */
   public int getScore() {
      return score;
   }

   /**
    * This returns followers of the member
    * @return Returns the number of followers that member has
    */
   public int getFollowers() {
      return followers;
   }

   /**
    * This returns the followings of the member
    * @return  Returns the number of followings that member has
    */
   public int getFollowings() {
      return followings;
   }

   /**
    * This returns all habits of the member
    * @return  return a list of habit of the member
    */
   public ArrayList<Habit> getHabitListItems() {
      return this.habitListItems;
   }

   public Follow getFollow(){return this.follow;}
   // Setters

   /**
    * Add a habit to member
    * @param habit   habit wants to add
    */
   public void addHabit(Habit habit) {
      if (isUser()) {
         if (habit.getUid() == -1) {
            habit.setUid();
         }
         habitListItems.add(habit);
      }
   }

   /**
    * Set the score of the user
    * @param score   the score wants to set
    */
   public void setScore(int score) {
      this.score = score;
   }

   /**
    * Set the number of followers of the user
    * @param followers  number of followers
    */
   public void setFollowers(int followers) {
      this.followers = followers;
   }

   /**
    * Set the number of followings of the user
    * @param followings number of followings
    */
   public void setFollowings(int followings) {
      this.followings = followings;
   }


   public void addCustomHabit(String name, String reason, int imageIndicator) {
      // for debug purposes, allows setting a habit with pre-set values

   }

   /**
    * This returns the unique id of the user
    * @return  Returns unique id
    */
   public int getUniqueID() {
      this.uniqueId += 1;
      if (isUser()) {

      }
      return this.uniqueId - 1;
   }

   /**
    * This returns the current Max unique id for all users
    * @return  Max Uid
    */
   public int getMaxUID() {
      return this.uniqueId;
   }

   /**
    * Set the UID for the user manually
    * @param val  Uid
    */
   public void setUniqueId(int val) {
      /* this should only be used to set the uniqueId value to that which is stored in Firebase */
      this.uniqueId = val;
   }

   /**
    * This returns the habit by input habit id
    * @param getid   the uid for habit
    * @return  The habit has the input uid
    */
   public Habit getHabitFromUid(int getid) {
      for (Habit habit : habitListItems) {
         if (habit.getUid() == getid) {
            return habit;
         }
      }
      // if no habit with matching id is found, a new habit is returned
      Habit habit;
      habit = new Habit("ERROR NO HABIT FOUND", "This erroneous habit was created since the UID was not found", -1);
      return habit;
   }

   /**
    * Delete a habit of the member
    * @param habitDelete   The habit want to delete
    */
   public void deleteHabit(Habit habitDelete){ habitListItems.remove(habitDelete);
   }

   public void setNextMidnight(Date midn) {
      this.nextMidnight = midn;
   }

   public Date getNextMidnight() {
      return this.nextMidnight;
   }

   public void setPrevNextMidnight(Date midn) {
      this.prevNextMidnight = midn;
   }

   public Date getPrevNextMidnight() {
      return this.prevNextMidnight;
   }

   public Boolean isAdmin() { return this.admin; }

   public void setAdmin(Boolean bool) { this.admin = bool; }

   public Long getAdminTimeOffset() { return this.adminTimeOffset; }

   public void setAdminTimeOffset(Long offset) { this.adminTimeOffset = offset; }

}

