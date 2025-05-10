package com.example.smartmedbeta;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.*;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PatientListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PatientAdapter patientAdapter;
    private List<Patient> patientList;
    private TextInputEditText num_of_records;
    private LinearLayout dynamicFilterLayout;
    private Button addFilterBtn, generateDataBtn;
    private List<Map<String, String>> appliedFilters = new ArrayList<>();
    private String selectedFilter;
private LinearLayout generation_area;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_list);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.parseColor("#F4F4F4"));
        }
        generation_area=findViewById(R.id.generation_area);
                recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        patientList = new ArrayList<>();
        patientAdapter = new PatientAdapter(patientList, this);
        recyclerView.setAdapter(patientAdapter);
        num_of_records=findViewById(R.id.num_of_records);

        dynamicFilterLayout = findViewById(R.id.dynamicFilterLayout);
        TextView btnShowFilters = findViewById(R.id.btnShowFilters);

        btnShowFilters.setOnClickListener(v -> showFilterOptions());



//        fetchPatientData();
        ImageView btnDownloadPdf = findViewById(R.id.btnDownloadPdf);
        btnDownloadPdf.setOnClickListener(v -> showDownloadOptions());
Button generate_custom_data=findViewById(R.id.generate_custom_data);
generate_custom_data.setOnClickListener(v -> generateFilteredPatientDataAndGiveToAdapter());
    }

    private void showDownloadOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Download Format");

        String[] options = {"PDF", "Excel", "CSV"};
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    generatePDF();
                    break;
                case 1:
                    generateExcel();
                    break;
                case 2:
                    generateCSV();
                    break;
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void fetchPatientData() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("patients/Synthetic medical data");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                patientList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Patient patient = snapshot.getValue(Patient.class);
                    if (patient != null) {
                        Log.d("FirebaseData", "Successfully parsed Patient: " + patient.toString());
                        patientList.add(patient);
                    } else {
                        Log.e("PatientListActivity", "Error parsing patient data from snapshot: " + snapshot.toString());
                    }
                }
                patientAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("PatientListActivity", "Database Error: " + databaseError.getMessage());
            }
        });
    }
    private void showFilterOptions() {
        dynamicFilterLayout.removeAllViews();
        dynamicFilterLayout.setVisibility(View.VISIBLE);


        Spinner filterSpinner = new Spinner(this);
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Select Filter", "Gender", "Age", "Ethnicity", "Alcohol Use", "Smoking Status", "Diet", "Chronic Condition", "Allergies", "Medication"});
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(filterAdapter);

        dynamicFilterLayout.addView(filterSpinner);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedFilter = (String) parentView.getItemAtPosition(position);
                if (!selectedFilter.equals("Select Filter")) {
                    showInputFieldForFilter(selectedFilter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
    }

    private void showInputFieldForFilter(String selectedFilter) {
        dynamicFilterLayout.removeAllViews();

       TextView inputLabel = new TextView(this);
        inputLabel.setText("Enter value for " + selectedFilter);
        inputLabel.setTextSize(16);
        inputLabel.setTextColor(getResources().getColor(R.color.black));
        dynamicFilterLayout.addView(inputLabel);

       if (selectedFilter.equals("Gender")) {
            Spinner genderSpinner = new Spinner(this);
            ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Male", "Female", "Other"});
            genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            genderSpinner.setAdapter(genderAdapter);
            dynamicFilterLayout.addView(genderSpinner);

            Button addButton = createAddButton(selectedFilter, genderSpinner);
            dynamicFilterLayout.addView(addButton);

        } else {
            EditText inputField = new EditText(this);
           inputField.setHint("Enter " + selectedFilter);
           inputField.setHintTextColor(getResources().getColor(R.color.black));
           inputField.setTextSize(16);
           inputField.setTextColor(getResources().getColor(R.color.black));

           inputField.setBackground(getResources().getDrawable(R.drawable.rounded_edittext));


           inputField.setGravity(Gravity.CENTER);
           inputField.setInputType(InputType.TYPE_CLASS_TEXT);

           LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                   LinearLayout.LayoutParams.MATCH_PARENT,
                   LinearLayout.LayoutParams.WRAP_CONTENT
           );
           layoutParams.setMargins(0, 20, 0, 16);

           inputField.setLayoutParams(layoutParams);

           dynamicFilterLayout.addView(inputField);

           Button addButton = createAddButton(selectedFilter, inputField);
            dynamicFilterLayout.addView(addButton);
        }
    }

    private Button createAddButton(String filterName, View inputField) {
        Button addButton = new Button(this);
        addButton.setText("Add");
        addButton.setTextColor(getResources().getColor(R.color.white));
        addButton.setBackground(getResources().getDrawable(R.drawable.rounded_edittext_smd));
        addButton.setOnClickListener(v -> {
            String inputValue;
            if (inputField instanceof Spinner) {
                inputValue = ((Spinner) inputField).getSelectedItem().toString();
            } else {
                inputValue = ((EditText) inputField).getText().toString().trim();
            }

            if (!inputValue.isEmpty()) {
                addFilterToList(filterName, inputValue);
            } else {
                Toast.makeText(this, "Please enter a value.", Toast.LENGTH_SHORT).show();
            }
        });
        return addButton;
    }

    private void addFilterToList(String filterName, String filterValue) {
        Map<String, String> filter = new HashMap<>();
        filter.put(filterName, filterValue);
        appliedFilters.add(filter);
        Log.d("FilterDebug", "Added filter: " + filterName + " = " + filterValue);
        Log.d("FilterDebug", "Total filters: " + appliedFilters.size());

        dynamicFilterLayout.removeAllViews();
        dynamicFilterLayout.setVisibility(View.GONE);

        Toast.makeText(this, filterName + " filter added: " + filterValue, Toast.LENGTH_SHORT).show();
    }

    private void generateFilteredPatientDataAndGiveToAdapter() {
        String inputText = num_of_records.getText().toString().trim();
        int numberOfRecords;

        try {
            numberOfRecords = Integer.parseInt(inputText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number of records.", Toast.LENGTH_SHORT).show();
            return;
        }

        patientList.clear();

       List<Map<String, Object>> generatedPatientData = PatientRecordGenerator.generatePatientRecords(numberOfRecords);

        applyUserDefinedFilters(generatedPatientData);

        for (Map<String, Object> patientDataMap : generatedPatientData) {
            Patient patient = new Patient();

            patient.setPatientID(patientDataMap.get("patientID").toString());
            patient.setName(patientDataMap.get("Name").toString());
            patient.setAge(Integer.parseInt(patientDataMap.get("Age").toString()));
            patient.setGender(patientDataMap.get("Gender").toString());
            patient.setEthnicity(patientDataMap.get("Ethnicity").toString());
            patient.setWeight(Double.parseDouble(patientDataMap.get("Weight").toString()));
            patient.setHeight(Integer.parseInt(patientDataMap.get("Height").toString()));
            patient.setBloodPressure(patientDataMap.get("BloodPressure").toString());
            patient.setHeartRate(Integer.parseInt(patientDataMap.get("HeartRate").toString()));
            patient.setBodyTemperature(Double.parseDouble(patientDataMap.get("BodyTemperature").toString()));
            patient.setDiagnosis(patientDataMap.get("Diagnosis").toString());
            patient.setMedication(patientDataMap.get("Medication").toString());
            patient.setLastVisitDate(patientDataMap.get("LastVisitDate").toString());
            patient.setEmergencyContactName(patientDataMap.get("EmergencyContactName").toString());
            patient.setEmergencyContactPhone(patientDataMap.get("EmergencyContactPhone").toString());
            patient.setNextAppointment(patientDataMap.get("NextAppointment").toString());
            patient.setAlcoholUse(patientDataMap.get("AlcoholUse").toString());
            patient.setSmokingStatus(patientDataMap.get("SmokingStatus").toString());
            patient.setExerciseFrequency(patientDataMap.get("ExerciseFrequency").toString());
            patient.setDiet(patientDataMap.get("Diet").toString());
            patient.setFamilyHistory(patientDataMap.get("FamilyHistory").toString());
            patient.setChronicConditions(patientDataMap.get("ChronicConditions").toString());
            patient.setInsuranceProvider(patientDataMap.get("InsuranceProvider").toString());
            patient.setInsurancePolicyNumber(patientDataMap.get("InsurancePolicyNumber").toString());
            patient.setInsuranceCoverageType(patientDataMap.get("InsuranceCoverageType").toString());
            patient.setMedicalHistory(patientDataMap.get("MedicalHistory").toString());
            patient.setAllergies(patientDataMap.get("Allergies").toString());
            patient.setVaccinationStatus(patientDataMap.get("VaccinationStatus").toString());

            patientList.add(patient);
        }

        patientAdapter.notifyDataSetChanged();
        updateDownloadButtonVisibility(generatedPatientData.isEmpty());
    }

    private void applyUserDefinedFilters(List<Map<String, Object>> patientData) {
        for (Map<String, String> filter : appliedFilters) {
            for (Map.Entry<String, String> entry : filter.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                Log.d("FilterDebug", "Setting all " + key + " to " + value);

               for (Map<String, Object> patient : patientData) {
                    patient.put(key, value);
                }
            }
        }
    }

    private void updateDownloadButtonVisibility(boolean isEmpty) {
        ImageView btnDownloadPdf = findViewById(R.id.btnDownloadPdf);
        btnDownloadPdf.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        generation_area.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }




    private void generatePDF() {
        try {
            String fileName = "PatientList.pdf";
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/SmartMed");

            Uri pdfUri = getContentResolver().insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues);

            if (pdfUri != null) {
                OutputStream outputStream = getContentResolver().openOutputStream(pdfUri);
                if (outputStream != null) {
                    Document document = new Document(PageSize.A4, 36, 36, 54, 36);
                    PdfWriter.getInstance(document, outputStream);
                    document.open();

                    Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, BaseColor.DARK_GRAY);
                    Font patientHeaderFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, new BaseColor(0, 102, 204));
                    Font sectionHeaderFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, new BaseColor(0, 153, 0));
                    Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
                    Font labelFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.DARK_GRAY);

                    Paragraph title = new Paragraph("Patient List", titleFont);
                    title.setAlignment(Element.ALIGN_CENTER);
                    title.setSpacingAfter(20);
                    document.add(title);

                    Paragraph date = new Paragraph("Generated on: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), normalFont);
                    date.setAlignment(Element.ALIGN_RIGHT);
                    date.setSpacingAfter(20);
                    document.add(date);

                    for (Patient patient : patientList) {

                        Paragraph patientHeader = new Paragraph(getStringValue(patient.getName()), patientHeaderFont);
                        patientHeader.setSpacingBefore(20);
                        patientHeader.setSpacingAfter(10);
                        document.add(patientHeader);

                        addSection(document, "Personal Information", sectionHeaderFont);
                        addField(document, "Age", getStringValue(patient.getAge()), labelFont, normalFont);
                        addField(document, "Gender", getStringValue(patient.getGender()), labelFont, normalFont);
                        addField(document, "Ethnicity", getStringValue(patient.getEthnicity()), labelFont, normalFont);

                        addSection(document, "Health Information", sectionHeaderFont);
                        addField(document, "Weight", getStringValue(patient.getWeight()) + " kg", labelFont, normalFont);
                        addField(document, "Height", getStringValue(patient.getHeight()) + " cm", labelFont, normalFont);
                        addField(document, "Blood Pressure", getStringValue(patient.getBloodPressure()), labelFont, normalFont);
                        addField(document, "Heart Rate", getStringValue(patient.getHeartRate()) + " bpm", labelFont, normalFont);
                        addField(document, "Body Temperature", getStringValue(patient.getBodyTemperature()) + " °C", labelFont, normalFont);

                        addSection(document, "Lifestyle", sectionHeaderFont);
                        addField(document, "Alcohol Use", getStringValue(patient.getAlcoholUse()), labelFont, normalFont);
                        addField(document, "Smoking Status", getStringValue(patient.getSmokingStatus()), labelFont, normalFont);
                        addField(document, "Exercise Frequency", getStringValue(patient.getExerciseFrequency()), labelFont, normalFont);
                        addField(document, "Diet", getStringValue(patient.getDiet()), labelFont, normalFont);

                        addSection(document, "Medical History", sectionHeaderFont);
                        addField(document, "Family History", getStringValue(patient.getFamilyHistory()), labelFont, normalFont);
                        addField(document, "Chronic Conditions", getStringValue(patient.getChronicConditions()), labelFont, normalFont);
                        addField(document, "Diagnosis", getStringValue(patient.getDiagnosis()), labelFont, normalFont);
                        addField(document, "Allergies", getStringValue(patient.getAllergies()), labelFont, normalFont);
                        addField(document, "Medication", getStringValue(patient.getMedication()), labelFont, normalFont);

                        addSection(document, "Appointments", sectionHeaderFont);
                        addField(document, "Last Visit Date", getStringValue(patient.getLastVisitDate()), labelFont, normalFont);
                        addField(document, "Next Appointment", getStringValue(patient.getNextAppointment()), labelFont, normalFont);

                        addSection(document, "Insurance Information", sectionHeaderFont);
                        addField(document, "Provider", getStringValue(patient.getInsuranceProvider()), labelFont, normalFont);
                        addField(document, "Policy Number", getStringValue(patient.getInsurancePolicyNumber()), labelFont, normalFont);
                        addField(document, "Coverage Type", getStringValue(patient.getInsuranceCoverageType()), labelFont, normalFont);


                        addSection(document, "Emergency Contact", sectionHeaderFont);
                        addField(document, "Name", getStringValue(patient.getEmergencyContactName()), labelFont, normalFont);
                        addField(document, "Phone", getStringValue(patient.getEmergencyContactPhone()), labelFont, normalFont);
                        addField(document, "Relationship", getStringValue(patient.getEmergencyContactRelationship()), labelFont, normalFont);

                        document.newPage();
                    }

                    document.close();
                    outputStream.close();

                    Log.d("PDF", "PDF saved successfully: " + pdfUri.toString());
                    Toast.makeText(this, "PDF saved successfully", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("PDF", "Failed to create PDF file");
                Toast.makeText(this, "Failed to create PDF file", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("PDF", "Error generating PDF: " + e.getMessage());
            Toast.makeText(this, "Error generating PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void addSection(Document document, String sectionTitle, Font font) throws DocumentException {
        Paragraph section = new Paragraph(sectionTitle, font);
        section.setSpacingBefore(10);
        section.setSpacingAfter(5);
        document.add(section);
    }

    private void addField(Document document, String label, String value, Font labelFont, Font valueFont) throws DocumentException {
        Paragraph field = new Paragraph();
        field.add(new Chunk(label + ": ", labelFont));
        field.add(new Chunk(value, valueFont));
        document.add(field);
    }

    private String getStringValue(Object value) {
        return value != null ? value.toString() : "Not available";
    }
    private void generateExcel() {
        try {
            String fileName = "PatientList.xlsx";
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/SmartMed");

            Uri excelUri = getContentResolver().insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues);

            if (excelUri != null) {
                OutputStream outputStream = getContentResolver().openOutputStream(excelUri);
                if (outputStream != null) {
                    Workbook workbook = new XSSFWorkbook();
                    Sheet sheet = workbook.createSheet("Patients");


                    Row headerRow = sheet.createRow(0);
                    String[] headers = {
                            "Name", "Age", "Gender", "Ethnicity", "Alcohol Use", "Smoking Status", "Exercise Frequency",
                            "Diet", "Family History", "Chronic Conditions", "Diagnosis", "Weight", "Height", "Blood Pressure",
                            "Heart Rate", "Body Temperature", "Last Visit", "Next Appointment", "Insurance Provider",
                            "Policy Number", "Coverage Type", "Emergency Contact Name", "Emergency Contact Phone",
                            "Relationship", "Medical History", "Allergies", "Medication"
                    };
                    for (int i = 0; i < headers.length; i++) {
                        Cell cell = headerRow.createCell(i);
                        cell.setCellValue(headers[i]);
                    }


                    int rowNum = 1;
                    for (Patient patient : patientList) {
                        Row row = sheet.createRow(rowNum++);
                        row.createCell(0).setCellValue(patient.getName());
                        row.createCell(1).setCellValue(patient.getAge());
                        row.createCell(2).setCellValue(patient.getGender());
                        row.createCell(3).setCellValue(patient.getEthnicity());
                        row.createCell(4).setCellValue(patient.getAlcoholUse());
                        row.createCell(5).setCellValue(patient.getSmokingStatus());
                        row.createCell(6).setCellValue(patient.getExerciseFrequency());
                        row.createCell(7).setCellValue(patient.getDiet());
                        row.createCell(8).setCellValue(patient.getFamilyHistory());
                        row.createCell(9).setCellValue(patient.getChronicConditions());
                        row.createCell(10).setCellValue(patient.getDiagnosis());
                        row.createCell(11).setCellValue(patient.getWeight() + " kg");
                        row.createCell(12).setCellValue(patient.getHeight() + " cm");
                        row.createCell(13).setCellValue(patient.getBloodPressure());
                        row.createCell(14).setCellValue(patient.getHeartRate() + " bpm");
                        row.createCell(15).setCellValue(patient.getBodyTemperature() + " °C");
                        row.createCell(16).setCellValue(patient.getLastVisitDate());
                        row.createCell(17).setCellValue(patient.getNextAppointment());
                        row.createCell(18).setCellValue(patient.getInsuranceProvider());
                        row.createCell(19).setCellValue(patient.getInsurancePolicyNumber());
                        row.createCell(20).setCellValue(patient.getInsuranceCoverageType());
                        row.createCell(21).setCellValue(patient.getEmergencyContactName());
                        row.createCell(22).setCellValue(patient.getEmergencyContactPhone());
                        row.createCell(23).setCellValue(patient.getEmergencyContactRelationship());
                        row.createCell(24).setCellValue(patient.getMedicalHistory());
                        row.createCell(25).setCellValue(patient.getAllergies());
                        row.createCell(26).setCellValue(patient.getMedication());
                    }


                    workbook.write(outputStream);
                    workbook.close();
                    outputStream.close();

                    Log.d("Excel", "Excel saved successfully: " + excelUri.toString());
                    Toast.makeText(this, "Excel saved successfully", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("Excel", "Failed to create Excel file");
                Toast.makeText(this, "Failed to create Excel file", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("Excel", "Error generating Excel: " + e.getMessage());
            Toast.makeText(this, "Error generating Excel: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void generateCSV() {
        try {
            String fileName = "PatientList.csv";
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/SmartMed");

            Uri csvUri = getContentResolver().insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues);

            if (csvUri != null) {
                OutputStream outputStream = getContentResolver().openOutputStream(csvUri);
                if (outputStream != null) {
                    StringBuilder csvBuilder = new StringBuilder();
                    String[] headers = {
                            "Name", "Age", "Gender", "Ethnicity", "Alcohol Use", "Smoking Status", "Exercise Frequency",
                            "Diet", "Family History", "Chronic Conditions", "Diagnosis", "Weight", "Height", "Blood Pressure",
                            "Heart Rate", "Body Temperature", "Last Visit", "Next Appointment", "Insurance Provider",
                            "Policy Number", "Coverage Type", "Emergency Contact Name", "Emergency Contact Phone",
                            "Relationship", "Medical History", "Allergies", "Medication"
                    };
                    csvBuilder.append(String.join(",", headers)).append("\n");

                    for (Patient patient : patientList) {
                        csvBuilder.append(patient.getName()).append(",")
                                .append(patient.getAge()).append(",")
                                .append(patient.getGender()).append(",")
                                .append(patient.getEthnicity()).append(",")
                                .append(patient.getAlcoholUse()).append(",")
                                .append(patient.getSmokingStatus()).append(",")
                                .append(patient.getExerciseFrequency()).append(",")
                                .append(patient.getDiet()).append(",")
                                .append(patient.getFamilyHistory()).append(",")
                                .append(patient.getChronicConditions()).append(",")
                                .append(patient.getDiagnosis()).append(",")
                                .append(patient.getWeight()).append(",")
                                .append(patient.getHeight()).append(",")
                                .append(patient.getBloodPressure()).append(",")
                                .append(patient.getHeartRate()).append(",")
                                .append(patient.getBodyTemperature()).append(",")
                                .append(patient.getLastVisitDate()).append(",")
                                .append(patient.getNextAppointment()).append(",")
                                .append(patient.getInsuranceProvider()).append(",")
                                .append(patient.getInsurancePolicyNumber()).append(",")
                                .append(patient.getInsuranceCoverageType()).append(",")
                                .append(patient.getEmergencyContactName()).append(",")
                                .append(patient.getEmergencyContactPhone()).append(",")
                                .append(patient.getEmergencyContactRelationship()).append(",")
                                .append(patient.getMedicalHistory()).append(",")
                                .append(patient.getAllergies()).append(",")
                                .append(patient.getMedication()).append("\n");
                    }

                    outputStream.write(csvBuilder.toString().getBytes());
                    outputStream.close();

                    Log.d("CSV", "CSV saved successfully: " + csvUri.toString());
                    Toast.makeText(this, "CSV saved successfully", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("CSV", "Failed to create CSV file");
                Toast.makeText(this, "Failed to create CSV file", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("CSV", "Error generating CSV: " + e.getMessage());
            Toast.makeText(this, "Error generating CSV: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}