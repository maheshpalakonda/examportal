//package com.exam.portal.service;
//import com.exam.portal.dto.StudentDTO;
//import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
//import com.google.api.client.http.javanet.NetHttpTransport;
//import com.google.api.client.json.gson.GsonFactory;
//import com.google.api.services.sheets.v4.Sheets;
//import com.google.api.services.sheets.v4.SheetsScopes;
//import com.google.api.services.sheets.v4.model.ValueRange;
//import com.google.auth.http.HttpCredentialsAdapter;
//import com.google.auth.oauth2.GoogleCredentials;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import java.io.InputStream;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//
//@Service
//public class GoogleSheetsService {
//    @Value("${google.sheets.spreadsheet.id}") private String spreadsheetId;
//    @Value("${google.sheets.credentials.path}") private String credentialsPath;
//
//    private Sheets getSheetsService() throws Exception {
//        InputStream in = getClass().getResourceAsStream(credentialsPath.replace("classpath:", "/"));
//        if (in == null) throw new IllegalStateException("Cannot find credentials file: " + credentialsPath);
//
//        GoogleCredentials credentials = GoogleCredentials.fromStream(in).createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS_READONLY));
//        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
//        
//        return new Sheets.Builder(HTTP_TRANSPORT, GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
//                .setApplicationName("Online Exam Portal").build();
//    }
//
//    public List<StudentDTO> fetchAllStudents() throws Exception {
//        Sheets sheets = getSheetsService();
//        final String range = "Sheet1!A:H";
//        ValueRange response = sheets.spreadsheets().values().get(spreadsheetId, range).execute();
//        
//        List<List<Object>> values = response.getValues();
//        if (values == null || values.isEmpty()) return Collections.emptyList();
//
//        List<StudentDTO> students = new ArrayList<>();
//        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//
//        for (int i = 1; i < values.size(); i++) { // Skip header row
//            List<Object> row = values.get(i);
//            if (row.size() >= 8 && !row.get(2).toString().trim().isEmpty()) {
//                try {
//                    students.add(new StudentDTO(
//                        row.get(0).toString(), row.get(1).toString(), row.get(2).toString(), row.get(3).toString(),
//                        row.get(4).toString(), new BigDecimal(row.get(5).toString()),
//                        LocalDate.parse(row.get(6).toString(), dateFormatter), row.get(7).toString()
//                    ));
//                } catch (Exception e) { System.err.println("Skipping row " + (i + 1) + ": " + e.getMessage()); }
//            }
//        }
//        return students;
//    }
//}