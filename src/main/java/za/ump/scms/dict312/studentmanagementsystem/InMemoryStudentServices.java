/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.ump.scms.dict312.studentmanagementsystem;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author nhlay
 * This is a concrete implementation of the StudentService interface that stores data in memory
 * this bean is AppicationScoped, meaning that a single instance of the class 
 * will be created for the duration of the application and shared across all
 * requests and users
 */
@ApplicationScoped
public class InMemoryStudentServices implements StudentService{
      private final Map<Long,Student> studentStore = new ConcurrentHashMap<>();
      private final AtomicLong  idGenerator = new AtomicLong(0);
    
    public InMemoryStudentServices(){
    //prepopulate the student with some dummy data
    addStudent("Bennet Hlayiseko","Ramolefo","222110198@ump.ac.za");
    addStudent("dube","nkuna","dube@ump.ac.za");
    addStudent("fana","Ramolefo","fama@ump.ac.za");
    }
    
    private void addStudent(String firstName,String lastName,String emailAddress ){
    long id = idGenerator.incrementAndGet();
    studentStore.put(id, new Student(id,firstName,lastName,emailAddress));
    }

    @Override
    public List<Student> getAllStudent() {
        System.out.println("InMemoryStudentService: Fetching all students.");
        return new ArrayList<>(studentStore.values());
    }

    @Override
    public Optional<Student> findStudentById(long id) {
       System.out.println("InMemoryStudentService: Finding student with ID: "+id);
       return Optional.ofNullable(studentStore.get(id));
    }
}
