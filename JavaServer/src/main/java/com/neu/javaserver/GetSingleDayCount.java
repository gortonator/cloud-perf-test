/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neu.javaserver;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author igortn
 */
public class GetSingleDayCount extends HttpServlet {
    
    private static final int USER_POS = 1;
    private static final int DAY_POS =2;
    private static final int URL_LEN =3 ;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
       String URLpath = request.getPathInfo(); 
       response.setContentType("text/plain");
       if (URLpath == null) {
           response.setStatus(400);
            response.getWriter().println("No user/day information - get real dude");
            return;
        } 
       
        String[] URLparts = URLpath.split("/");
        if ( URLparts.length  == URL_LEN) {
            // user key is just a string
            String user = URLparts[USER_POS];
            String day = URLparts [DAY_POS];
            // day must be a valid integer
            if ( (Utils.isValidNum(day) )){
                response.getWriter().println("User=" + user + "day = " + day);
            } else {
                response.setStatus(400);
                response.getWriter().println("user/day information must be numeric");
            }
         } else {
            response.setStatus(400);
            response.getWriter().println("No user/day information - get real dude");
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
        processRequest(request, response);
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
