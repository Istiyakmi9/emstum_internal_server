package com.ems.buildorg.util;

import com.ems.buildorg.modal.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetEncryptedPassword {
    private static GetEncryptedPassword getEncryptedPassword;
    private GetEncryptedPassword() {

    }

    public static GetEncryptedPassword getEncryptedPassword() {
        if (getEncryptedPassword == null) {
            synchronized (GetEncryptedPassword.class) {
                if (getEncryptedPassword == null) {
                    getEncryptedPassword = new GetEncryptedPassword();
                }
            }
        }
        return getEncryptedPassword;
    }

    public String generateEncryptedPassword() throws IOException {
        String password = "welcome@$Bot_001";
        String encryptedPassword = "";
        try {
            // Specify the URL you want to send the POST request to
            URL url = new URL("https://www.emstum.com/bot/dn/api/auth/login/EncryptDetail/" + password);

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set the request method to POST
            connection.setRequestMethod("GET");

            // Set the request headers (if needed)
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            // Get the response code
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // Read the response from the server
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                // Print the response
                System.out.println("Response: " + response.toString());
                ObjectMapper mapper = new ObjectMapper();
                ApiResponse apiResponse = mapper.readValue(response.toString(), ApiResponse.class);
                encryptedPassword = apiResponse.getResponseBody();
            }

            // Close the connection
            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return encryptedPassword;
    }
}
