/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.ump.scms.dict312.studentmanagementsystem;

/**
 *
 * @author nhlay
 */
/**
 *A simple data class representing a student class
 * in a later sections, this class will become a JPA Entity
 */
public class Student {
    private long id;
    private String firstName;
    private String lastName;
    private String emailAddress;

    Student(long id, String firstName, String lastName, String emailAddress) {
        this.id=id;
        this.firstName=firstName;
        this.lastName=lastName;
        this.emailAddress=emailAddress;
    }
    
    public Student(){
    }
    //Getters and Setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @Override
    public String toString() {
        return "Student{" + "id=" + id + ", firstName=" + firstName + ", lastName=" + lastName +"\n"+ ", emailAddress=" + emailAddress + '}';
    }
    
    
}
