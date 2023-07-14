//import java.io.IOException;
//import java.io.InputStream;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//
//@WebServlet("/DownloadServlet")
//public class DownloadServlet extends HttpServlet {
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        // Retrieve the file name from the request parameter
//        String fileName = request.getParameter("file");
//
//        // Retrieve the file content from the database based on the file name
//        byte[] fileContent = retrieveFileContentFromMySQL(fileName);
//
//        if (fileContent != null) {
//            // Set the response content type
//            response.setContentType("application/octet-stream");
//            // Set the Content-Disposition header to prompt the user to download the file
//            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
//
//            // Write the file content to the response output stream
//            response.getOutputStream().write(fileContent);
//        } else {
//            // Handle the case where the file content is not found
//            response.getWriter().println("File not found.");
//        }
//    }
//
//    private byte[] retrieveFileContentFromMySQL(String fileName) {
//        String jdbcUrl = "jdbc:mysql://localhost:3306/ternaryhashtree";
//        String username = "root";
//        String password = "Narayana#3837";
//
//        try {
//            Class.forName("com.mysql.cj.jdbc.Driver");
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
//            String sql = "SELECT hash FROM files WHERE name = ?";
//            PreparedStatement statement = conn.prepareStatement(sql);
//            statement.setString(1, fileName);
//
//            ResultSet resultSet = statement.executeQuery();
//            if (resultSet.next()) {
//                // Retrieve the file content from the result set
//                return resultSet.getBytes("hash");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return null; // Return null if the file content is not found
//    }
//}
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/DownloadServlet")
public class DownloadServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Retrieve the file name from the request parameter
        String fileName = request.getParameter("file");

        // Retrieve the file content from the database based on the file name
        byte[] fileContent = retrieveFileContentFromMySQL(fileName);

        if (fileContent != null) {
            // Decode the Base64-encoded content
            byte[] decodedContent = Base64.getDecoder().decode(fileContent);

            // Set the response content type
            response.setContentType("text/plain");
            // Set the Content-Disposition header to prompt the user to download the file
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            // Write the decoded file content to the response output stream
            response.getOutputStream().write(decodedContent);
        } else {
            // Handle the case where the file content is not found
            response.getWriter().println("File not found.");
        }
    }

    private byte[] retrieveFileContentFromMySQL(String fileName) {
    	String jdbcUrl = "jdbc:mysql://localhost:3306/ternaryhashtree";
        String username = "root";
        String password = "Narayana#3837";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            String sql = "SELECT hash FROM files WHERE name = ?";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, fileName);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                // Retrieve the file content from the result set
                return resultSet.getBytes("hash");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // Return null if the file content is not found
    }
}
