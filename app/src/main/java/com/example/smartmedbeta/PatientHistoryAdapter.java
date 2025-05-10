package com.example.smartmedbeta;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
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
public class PatientHistoryAdapter extends RecyclerView.Adapter<PatientHistoryAdapter.PatientHistoryViewHolder> {

    private List<SessionData> sessionDataList;
    private Context context;
    public PatientHistoryAdapter(List<SessionData> sessionDataList, Context context) {
        this.sessionDataList = sessionDataList;
        this.context = context;
    }
    @NonNull
    @Override
    public PatientHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session, parent, false);
        return new PatientHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientHistoryViewHolder holder, int position) {
        SessionData sessionData = sessionDataList.get(position);

        if (sessionData != null) {

            if (sessionData.getDiagnosis() != null) {
                holder.tvDiagnosisDetails.setText(sessionData.getDiagnosis().getCondition());
                Log.d("PatientHistoryAdapter", "Diagnosis: " + sessionData.getDiagnosis().getCondition());
            } else {
                holder.tvDiagnosisDetails.setText("No diagnosis available");
            }


            holder.tvConversationsDetails.setText(formatConversations(sessionData.getConversations()));


            holder.tvFurtherTestsDetails.setText(formatFurtherTests(sessionData.getFurtherTestsDetails()));


            holder.tvMedicationsDetails.setText(formatMedications(sessionData.getMedications()));


            holder.tvDiagnosisDetails.setVisibility(View.VISIBLE);
            holder.tvConversationsDetails.setVisibility(View.GONE);
            holder.tvFurtherTestsDetails.setVisibility(View.GONE);
            holder.tvMedicationsDetails.setVisibility(View.GONE);


            holder.tvConversations.setOnClickListener(v -> {
                holder.tvConversationsDetails.setVisibility(
                        holder.tvConversationsDetails.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
            });

            holder.tvFurtherTests.setOnClickListener(v -> {
                holder.tvFurtherTestsDetails.setVisibility(
                        holder.tvFurtherTestsDetails.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
            });

            holder.tvMedications.setOnClickListener(v -> {
                holder.tvMedicationsDetails.setVisibility(
                        holder.tvMedicationsDetails.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);




            });


            holder.itemView.setOnClickListener(v -> {

                Intent intent = new Intent(context, SessionDetailsActivity.class);
                intent.putExtra("diagnosis", sessionData.getDiagnosis() != null ? sessionData.getDiagnosis().getCondition() : "No diagnosis available");
                intent.putExtra("conversations", formatConversations(sessionData.getConversations()));
                intent.putExtra("furtherTests", formatFurtherTests(sessionData.getFurtherTestsDetails()));
                intent.putExtra("medications", formatMedications(sessionData.getMedications()));

                context.startActivity(intent);
            });
        } else {
            Log.e("PatientHistoryAdapter", "SessionData is null at position: " + position);
        }
    }
    private String formatConversations(Map<String, Map<String, String>> conversations) {
        StringBuilder conversationsBuilder = new StringBuilder();

        if (conversations != null && !conversations.isEmpty()) {
            for (Map.Entry<String, Map<String, String>> entry : conversations.entrySet()) {
                String key = entry.getKey();
                conversationsBuilder.append(key).append(":\n");
                Map<String, String> conversation = entry.getValue();

                for (Map.Entry<String, String> convoEntry : conversation.entrySet()) {
                    String convoKey = convoEntry.getKey();
                    String convoValue = convoEntry.getValue();

                    if (convoKey.equals("text")) {

                        convoValue = convoValue.replaceFirst("text: ", "").trim();
                    }


                    conversationsBuilder.append(convoKey).append(": ").append(convoValue).append("\n");
                }
                conversationsBuilder.append("\n");
            }
        } else {
            conversationsBuilder.append("No conversations available");
        }

        Log.d("PatientHistoryAdapter", "Formatted Conversations: " + conversationsBuilder.toString().trim());
        return conversationsBuilder.toString().trim();
    }


    private String formatFurtherTests(List<String> furtherTests) {
        StringBuilder furtherTestsBuilder = new StringBuilder();
        if (furtherTests != null && !furtherTests.isEmpty()) {
            for (Object test : furtherTests) {

                furtherTestsBuilder.append(test.toString()).append("\n");
            }
        } else {
            furtherTestsBuilder.append("No further tests available");
        }
        Log.d("PatientHistoryAdapter", "Further Tests: " + furtherTestsBuilder.toString().trim());
        return furtherTestsBuilder.toString().trim();
    }


    private String formatMedications(List<Map<String, Object>> medications) {
        StringBuilder medicationsBuilder = new StringBuilder();
        if (medications != null) {
            for (Map<String, Object> medication : medications) {
                medicationsBuilder.append("Name: ").append(medication.get("name")).append("\n")
                        .append("Dose: ").append(medication.get("dose")).append("\n")
                        .append("Duration: ").append(medication.get("duration")).append("\n");

                List<String> sideEffects = (List<String>) medication.get("sideEffects");
                if (sideEffects != null && !sideEffects.isEmpty()) {
                    medicationsBuilder.append("Side Effects: ").append(TextUtils.join(", ", sideEffects)).append("\n");
                } else {
                    medicationsBuilder.append("Side Effects: None\n");
                }
                medicationsBuilder.append("\n");
                Log.d("PatientHistoryAdapter", "Medication: " + medication.toString());
            }
        } else {
            medicationsBuilder.append("No medications available");
        }
        return medicationsBuilder.toString().trim();
    }


    @Override
    public int getItemCount() {
        return sessionDataList.size();
    }

    public static class PatientHistoryViewHolder extends RecyclerView.ViewHolder {

        TextView tvConversations, tvDiagnosis, tvFurtherTests, tvMedications;
        TextView tvConversationsDetails, tvDiagnosisDetails, tvFurtherTestsDetails, tvMedicationsDetails;

        public PatientHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvConversations = itemView.findViewById(R.id.tvConversations);
            tvDiagnosis = itemView.findViewById(R.id.tvDiagnosis);
            tvFurtherTests = itemView.findViewById(R.id.tvFurtherTests);
            tvMedications = itemView.findViewById(R.id.tvMedications);

            tvConversationsDetails = itemView.findViewById(R.id.tvConversationsDetails);
            tvDiagnosisDetails = itemView.findViewById(R.id.tvDiagnosisDetails);
            tvFurtherTestsDetails = itemView.findViewById(R.id.tvFurtherTestsDetails);
            tvMedicationsDetails = itemView.findViewById(R.id.tvMedicationsDetails);
        }
    }
}
