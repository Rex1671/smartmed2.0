package com.example.smartmedbeta;
public class Prescription {
    private String Medication;
    private String Dosage;
    private String Frequency;

    public Prescription() {
   }

    public String getMedication() {
        return Medication;
    }

    public void setMedication(String Medication) {
        this.Medication = Medication;
    }

    public String getDosage() {
        return Dosage;
    }

    public void setDosage(String Dosage) {
        this.Dosage = Dosage;
    }

    public String getFrequency() {
        return Frequency;
    }

    public void setFrequency(String Frequency) {
        this.Frequency = Frequency;
    }

    @Override
    public String toString() {
        return "Prescription{" +
                "Medication='" + Medication + '\'' +
                ", Dosage='" + Dosage + '\'' +
                ", Frequency='" + Frequency + '\'' +
                '}';
    }
}
