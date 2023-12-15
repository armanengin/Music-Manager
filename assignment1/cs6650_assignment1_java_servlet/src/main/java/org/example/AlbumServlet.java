package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@WebServlet("/albums/*")
@MultipartConfig
public class AlbumServlet extends HttpServlet {
    private final Gson gson = new Gson();


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String artist = null;
            String title = null;
            String year = null;
            byte[] imageContent = null;


            for (Part part : req.getParts()) {
                switch (part.getName()) {
                    case "profile":
                        StringBuilder profileSb = new StringBuilder();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(part.getInputStream()));
                        String profileLine;
                        while ((profileLine = reader.readLine()) != null) {
                            profileSb.append(profileLine).append("\n");
                        }
                        String profileContent = profileSb.toString();


                        int artistStartIndex = profileContent.indexOf("artist:") + "artist:".length();
                        int titleStartIndex = profileContent.indexOf("title:") + "title:".length();
                        int yearStartIndex = profileContent.indexOf("year:") + "year:".length();

                        int artistEndIndex = profileContent.indexOf("title:") - 1;  // -1 to avoid the trailing space before title
                        int titleEndIndex = profileContent.indexOf("year:") - 1;
                        int yearEndIndex = profileContent.indexOf("}", yearStartIndex);

                        artist = profileContent.substring(artistStartIndex, artistEndIndex).trim();
                        title = profileContent.substring(titleStartIndex, titleEndIndex).trim();
                        year = profileContent.substring(yearStartIndex, yearEndIndex).trim();
                        break;

                    case "image":
                        imageContent = part.getInputStream().readAllBytes();
                        break;
                }
            }

            if (artist == null || title == null || year == null || imageContent == null) {
                throw new IllegalArgumentException("Missing required fields in request.");
            }

            if (artist.isEmpty() || title.isEmpty() || year.isEmpty() || !year.matches("\\d+")) {
                throw new IllegalArgumentException("Invalid data provided.");
            }

            JsonObject response = new JsonObject();
            response.addProperty("albumID", "12345");
            response.addProperty("imageSize", imageContent.length);

            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(response));

        } catch (Exception e) {
            e.printStackTrace();  // Print the full stack trace for debugging purposes.
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject error = new JsonObject();
            error.addProperty("msg", e.getMessage());
            resp.getWriter().println(gson.toJson(error));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String albumID = req.getPathInfo().substring(1); // Extract albumID from the URL

        if (!albumID.matches("\\d+")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject error = new JsonObject();
            error.addProperty("msg", "Invalid albumID format. Must be an integer.");
            resp.getWriter().println(gson.toJson(error));
            return;
        }

        if (albumID instanceof String) { //check if albumID is valid or not
            JsonObject response = new JsonObject();
            response.addProperty("artist", "Sex Pistols");
            response.addProperty("title", "Never Mind The Bollocks!");
            response.addProperty("year", "1977");

            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(response));
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            JsonObject error = new JsonObject();
            error.addProperty("msg", "Key not found");
            resp.getWriter().println(gson.toJson(error));
        }
    }
}