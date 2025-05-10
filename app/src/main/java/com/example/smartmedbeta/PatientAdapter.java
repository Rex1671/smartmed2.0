package com.example.smartmedbeta;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {
    private List<Patient> patientList;
    private Context context;

    public PatientAdapter(List<Patient> patientList, Context context) {
        this.patientList = patientList;
        this.context = context;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patient_item, parent, false);
        return new PatientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = patientList.get(position);
        holder.nameTextView.setText("Name: " + patient.getName());
        holder.ageTextView.setText("Age: " + patient.getAge());
        holder.genderTextView.setText("Gender: " + patient.getGender());
        holder.ethnicityTextView.setText("Ethnicity: " + patient.getEthnicity());
        holder.alcoholUseTextView.setText("Alcohol Use: " + patient.getAlcoholUse());
        holder.smokingStatusTextView.setText("Smoking Status: " + patient.getSmokingStatus());
        holder.exerciseFrequencyTextView.setText("Exercise Frequency: " + patient.getExerciseFrequency());
        holder.dietTextView.setText("Diet: " + patient.getDiet());
        holder.familyHistoryTextView.setText("Family History: " + patient.getFamilyHistory());
        holder.chronicConditionsTextView.setText("Chronic Conditions: " + patient.getChronicConditions());
        holder.diagnosisTextView.setText("Diagnosis: " + patient.getDiagnosis());
        holder.weightTextView.setText("Weight: " + patient.getWeight() + " kg");
        holder.heightTextView.setText("Height: " + patient.getHeight() + " cm");
        holder.bloodPressureTextView.setText("Blood Pressure: " + patient.getBloodPressure());
        holder.heartRateTextView.setText("Heart Rate: " + patient.getHeartRate() + " bpm");
        holder.bodyTemperatureTextView.setText("Body Temperature: " + patient.getBodyTemperature() + " °C");
        holder.lastVisitTextView.setText("Last Visit: " + patient.getLastVisitDate());
        holder.nextAppointmentTextView.setText("Next Appointment: " + patient.getNextAppointment());
        holder.insuranceProviderTextView.setText("Insurance Provider: " + patient.getInsuranceProvider());
        holder.insurancePolicyNumberTextView.setText("Policy Number: " + patient.getInsurancePolicyNumber());
        holder.insuranceCoverageTypeTextView.setText("Coverage Type: " + patient.getInsuranceCoverageType());
        holder.emergencyContactNameTextView.setText("Emergency Contact Name: " + patient.getEmergencyContactName());
        holder.emergencyContactPhoneTextView.setText("Emergency Contact Phone: " + patient.getEmergencyContactPhone());
        holder.emergencyContactRelationshipTextView.setText("Relationship: " + patient.getEmergencyContactRelationship());
        holder.medicalHistoryTextView.setText("Medical History: " + patient.getMedicalHistory());
        holder.allergiesTextView.setText("Allergies: " + patient.getAllergies());
        holder.medicationTextView.setText("Medication: " + patient.getMedication());
//        holder.prescriptionsTextView.setText("Prescriptions: " + patient.getPrescriptions());
        Prescription prescription = patient.getPrescriptions();
        if (prescription != null) {

            StringBuilder prescriptionDetails = new StringBuilder();
            prescriptionDetails.append("Medication: ").append(prescription.getMedication()).append("\n")
                    .append("Dosage: ").append(prescription.getDosage()).append("\n")
                    .append("Frequency: ").append(prescription.getFrequency());


            holder.prescriptionsTextView.setText(prescriptionDetails.toString());
        } else {
            holder.prescriptionsTextView.setText("No prescriptions available.");
            Log.e("PrescriptionError", "No prescriptions found for patient: " + patient.getName());
        }}

    @Override
    public int getItemCount() {
        return patientList.size();
    }

    public static class PatientViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, ageTextView, genderTextView, ethnicityTextView, alcoholUseTextView, smokingStatusTextView,
                exerciseFrequencyTextView, dietTextView, familyHistoryTextView, chronicConditionsTextView,
                diagnosisTextView, weightTextView, heightTextView, bloodPressureTextView, heartRateTextView,
                bodyTemperatureTextView, lastVisitTextView, nextAppointmentTextView, insuranceProviderTextView,
                insurancePolicyNumberTextView, insuranceCoverageTypeTextView, emergencyContactNameTextView,
                emergencyContactPhoneTextView, emergencyContactRelationshipTextView, medicalHistoryTextView,
                allergiesTextView, medicationTextView, prescriptionsTextView;

        public PatientViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tvName);
            ageTextView = itemView.findViewById(R.id.tvAge);
            genderTextView = itemView.findViewById(R.id.tvGender);
            ethnicityTextView = itemView.findViewById(R.id.tvEthnicity);
            alcoholUseTextView = itemView.findViewById(R.id.tvAlcoholUse);
            smokingStatusTextView = itemView.findViewById(R.id.tvSmokingStatus);
            exerciseFrequencyTextView = itemView.findViewById(R.id.tvExerciseFrequency);
            dietTextView = itemView.findViewById(R.id.tvDiet);
            familyHistoryTextView = itemView.findViewById(R.id.tvFamilyHistory);
            chronicConditionsTextView = itemView.findViewById(R.id.tvChronicConditions);
            diagnosisTextView = itemView.findViewById(R.id.tvDiagnosis);
            weightTextView = itemView.findViewById(R.id.tvWeight);
            heightTextView = itemView.findViewById(R.id.tvHeight);
            bloodPressureTextView = itemView.findViewById(R.id.tvBloodPressure);
            heartRateTextView = itemView.findViewById(R.id.tvHeartRate);
            bodyTemperatureTextView = itemView.findViewById(R.id.tvBodyTemperature);
            lastVisitTextView = itemView.findViewById(R.id.tvLastVisit);
            nextAppointmentTextView = itemView.findViewById(R.id.tvNextAppointment);
            insuranceProviderTextView = itemView.findViewById(R.id.tvInsuranceProvider);
            insurancePolicyNumberTextView = itemView.findViewById(R.id.tvInsurancePolicyNumber);
            insuranceCoverageTypeTextView = itemView.findViewById(R.id.tvInsuranceCoverageType);
            emergencyContactNameTextView = itemView.findViewById(R.id.tvEmergencyContactName);
            emergencyContactPhoneTextView = itemView.findViewById(R.id.tvEmergencyContactPhone);
            emergencyContactRelationshipTextView = itemView.findViewById(R.id.tvEmergencyContactRelationship);
            medicalHistoryTextView = itemView.findViewById(R.id.tvMedicalHistory);
            allergiesTextView = itemView.findViewById(R.id.tvAllergies);
            medicationTextView = itemView.findViewById(R.id.tvMedication);
            prescriptionsTextView = itemView.findViewById(R.id.tvPrescriptions);
        }
    }
}
