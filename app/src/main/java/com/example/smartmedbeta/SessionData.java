package com.example.smartmedbeta;
import java.util.List;
import java.util.Map;

public class SessionData {
    private String sessionId;
    private Map<String, Map<String, String>> conversations;
    private Diagnosis diagnosis;
    private List<String> furtherTestsDetails;

    private String medicationsDetails;
    private List<Map<String, Object>> medications;


    public SessionData(String sessionId) {
        this.sessionId = sessionId;
    }



    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Map<String, Map<String, String>> getConversations() {
        return conversations;
    }

    public void setConversations(Map<String, Map<String, String>> conversations) {
        this.conversations = conversations;
    }

    public Diagnosis getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(Diagnosis diagnosis) {
        this.diagnosis = diagnosis;
    }

    public List<String> getFurtherTestsDetails() {
        return furtherTestsDetails;
    }


    public void setFurtherTestsDetails(List<String> furtherTestsDetails) {
        this.furtherTestsDetails = furtherTestsDetails;
    }


    public List<Map<String, Object>> getMedications() {
        return medications;
    }

    public void setMedications(List<Map<String, Object>> medications) {
        this.medications = medications;
    }

    public void setMedicationsDetails(String string) {
    }
}
