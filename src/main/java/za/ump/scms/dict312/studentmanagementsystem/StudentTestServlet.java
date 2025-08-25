/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package za.ump.scms.dict312.studentmanagementsystem;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;

/**
 *
 * @author nhlay
 */
@WebServlet 
public class StudentTestServlet extends HttpServlet {
    
    //The CDI Container will automatically find the ApplicationScoped Bean
    // that implements the StudentService and inject it here
    @Inject
    private StudentService studentService;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     *This is a simple way Servlet that is used to test our CDI setup. 
     *A Servlet is a basic way to  handle web request in Jakarta EE.
     * 
     * 
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet StudentTestServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet StudentTestServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
       response.setContentType("text/html");
       PrintWriter out = response.getWriter();
       
       out.println("<html><body>");
       out.println("<h1>Student Service CDI Injection Test</h1>");
       
       if (studentService == null){
       out.println("<p Style='color:red;'>Injection FAILED!</p>");
       }else{
       out.println("<p Style='color:green;'>Injection SUCCESSFUL!</p>");
       out.println("<h2> ALL Students:</h2>");
       out.println("<ul>");
       studentService.getAllStudent().forEach(student-> 
               out.println("<li>"+student.toString()+"</li>")
        );
       out.println("</ul>");
       out.println("<h2>Finding Student by ID 2 :</h2>");
       studentService.findStudentById(2L).ifPresentOrElse(
               student->out.println("<p>"+student.toString()+"</p>"),
               ()->out.println("<p>Student not found.</p>")
            );
       }
       
       out.println("</body></html>");
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
