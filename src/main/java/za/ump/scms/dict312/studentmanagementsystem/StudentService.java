/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.ump.scms.dict312.studentmanagementsystem;

/**
 *
 * @author nhlay
 * an interface that defines the contract for student-related business operations
 * Using an interface promotes loose coupling
 */
import java.util.List;
import java.util.Optional;

public interface StudentService {
    
    List<Student> getAllStudent();
    
    Optional<Student> findStudentById(long id);
    
}
