package com.example.smartmedbeta;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Patient {
    private String patientID;
    private String fullName;
    private int Age;
    private String Gender;
    private String Allergies;
    private String Diagnosis;
    private String LastVisitDate;
    private String AlcoholUse;
    private String BloodPressure;
    private double BodyTemperature;
    private String ChronicConditions;
    private String Diet;
    private String EmergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelationship;
    private String ethnicity;
    private String exerciseFrequency;
    private String familyHistory;
    private int heartRate;
    private int height;
    private String insuranceCoverageType;
    private String insurancePolicyNumber;
    private String insuranceProvider;
    private String medicalHistory;
    private String medication;
    private String nextAppointment;
    private String smokingStatus;
    private String vaccinationStatus;
    private double weight;
//    private Prescription prescriptions;

    // Constructor

//
//    public String getPrescriptions(){
//        return prescriptions;
//    }
//
//    public void setPrescriptions(String prescriptions){
//        this.prescriptions = prescriptions;
//    }
private Prescription prescriptions;

    public Patient() {

    }


    public Prescription getPrescriptions() {
        return prescriptions;
    }

    public void setPrescriptions(Prescription prescriptions) {
        this.prescriptions = prescriptions;
    }



    public String getPatientID() { return patientID; }
    public void setPatientID(String patientID) { this.patientID = patientID; }

    public String getName() { return fullName; }
    public void setName(String Name) { this.fullName = Name; }

    public int getAge() { return Age; }
    public void setAge(int age) { this.Age = age; }

    public String getGender() { return Gender; }
    public void setGender(String Gender) { this.Gender = Gender; }

    public String getAllergies() { return Allergies; }
    public void setAllergies(String Allergies) { this.Allergies = Allergies; }

    public String getDiagnosis() { return Diagnosis; }
    public void setDiagnosis(String Diagnosis) { this.Diagnosis = Diagnosis; }

    public String getLastVisitDate() { return LastVisitDate; }
    public void setLastVisitDate(String LastVisitDate) { this.LastVisitDate = LastVisitDate; }

    public String getAlcoholUse() { return AlcoholUse; }
    public void setAlcoholUse(String AlcoholUse) { this.AlcoholUse = AlcoholUse; }

    public String getBloodPressure() { return BloodPressure; }
    public void setBloodPressure(String BloodPressure) { this.BloodPressure = BloodPressure; }

    public double getBodyTemperature() { return BodyTemperature; }
    public void setBodyTemperature(double BodyTemperature) { this.BodyTemperature = BodyTemperature; }

    public String getChronicConditions() { return ChronicConditions; }
    public void setChronicConditions(String ChronicConditions) { this.ChronicConditions = ChronicConditions; }

    public String getDiet() { return Diet; }
    public void setDiet(String Diet) { this.Diet = Diet; }

    public String getEmergencyContactName() { return EmergencyContactName; }
    public void setEmergencyContactName(String EmergencyContactName) { this.EmergencyContactName = EmergencyContactName; }

    public String getEmergencyContactPhone() { return emergencyContactPhone; }
    public void setEmergencyContactPhone(String emergencyContactPhone) { this.emergencyContactPhone = emergencyContactPhone; }

    public String getEmergencyContactRelationship() { return emergencyContactRelationship; }
    public void setEmergencyContactRelationship(String emergencyContactRelationship) { this.emergencyContactRelationship = emergencyContactRelationship; }

    public String getEthnicity() { return ethnicity; }
    public void setEthnicity(String ethnicity) { this.ethnicity = ethnicity; }

    public String getExerciseFrequency() { return exerciseFrequency; }
    public void setExerciseFrequency(String exerciseFrequency) { this.exerciseFrequency = exerciseFrequency; }

    public String getFamilyHistory() { return familyHistory; }
    public void setFamilyHistory(String familyHistory) { this.familyHistory = familyHistory; }

    public int getHeartRate() { return heartRate; }
    public void setHeartRate(int heartRate) { this.heartRate = heartRate; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public String getInsuranceCoverageType() { return insuranceCoverageType; }
    public void setInsuranceCoverageType(String insuranceCoverageType) { this.insuranceCoverageType = insuranceCoverageType; }

    public String getInsurancePolicyNumber() { return insurancePolicyNumber; }
    public void setInsurancePolicyNumber(String insurancePolicyNumber) { this.insurancePolicyNumber = insurancePolicyNumber; }

    public String getInsuranceProvider() { return insuranceProvider; }
    public void setInsuranceProvider(String insuranceProvider) { this.insuranceProvider = insuranceProvider; }

    public String getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }

    public String getMedication() { return medication; }
    public void setMedication(String medication) { this.medication = medication; }

    public String getNextAppointment() { return nextAppointment; }
    public void setNextAppointment(String nextAppointment) { this.nextAppointment = nextAppointment; }

    public String getSmokingStatus() { return smokingStatus; }
    public void setSmokingStatus(String smokingStatus) { this.smokingStatus = smokingStatus; }

    public String getVaccinationStatus() { return vaccinationStatus; }
    public void setVaccinationStatus(String vaccinationStatus) { this.vaccinationStatus = vaccinationStatus; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
}
