package com.example.smartmedbeta;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.text.method.ScrollingMovementMethod;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DrugDiscoveryActivity extends AppCompatActivity {
    private static final String TAG = "DrugDiscoveryActivity";
    private EditText smilesInput;
    private TextView analysisResult;
    private ImageView moleculeImage;
    private TextView analyzeButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug_discovery);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.parseColor("#FFFFFF"));
        }
        smilesInput = findViewById(R.id.smilesInput);
        analysisResult = findViewById(R.id.analysisResult);
        moleculeImage = findViewById(R.id.moleculeImage);
        analyzeButton = findViewById(R.id.analyzeButton);

        analysisResult.setMovementMethod(new ScrollingMovementMethod());

        analyzeButton.setOnClickListener(v -> {
            String smiles = smilesInput.getText().toString().trim();
            if (!smiles.isEmpty()) {
                analyzeCompound(smiles);
            } else {
                Toast.makeText(this, "Please enter a valid SMILES notation.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void analyzeCompound(String smiles) {
        analyzeButton.setEnabled(false);
        new Thread(() -> {
            try {
                String response = getCompoundData(smiles);
                if (response != null) {
                    runOnUiThread(() -> parseResponse(response));
                } else {
                    runOnUiThread(() -> showError("Error fetching data. Please check your internet connection and try again."));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error analyzing compound", e);
                runOnUiThread(() -> showError("An error occurred: " + e.getMessage()));
            } finally {
                runOnUiThread(() -> analyzeButton.setEnabled(true));
            }
        }).start();
    }

    private String getCompoundData(String smiles) throws Exception {
        String urlString = "https://www.ebi.ac.uk/chembl/api/data/molecule.json?smiles=" + URLEncoder.encode(smiles, "UTF-8");
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(urlString)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                Log.e(TAG, "ChEMBL API Error: " + response.code() + " " + response.message());
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Network error", e);
            throw new Exception("Network error: " + e.getMessage());
        }
    }

    private void parseResponse(String response) {
        Gson gson = new Gson();
        try {
            ChEMBLResponse chemblResponse = gson.fromJson(response, ChEMBLResponse.class);
            if (chemblResponse != null && chemblResponse.getMolecules() != null && !chemblResponse.getMolecules().isEmpty()) {
                Molecule molecule = chemblResponse.getMolecules().get(0);
                displayCompoundInfo(molecule);
                loadImage(molecule.getStructureImage());
            } else {
                showError("No data available for the entered SMILES.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing response", e);
            showError("Error parsing data. Please try again.");
        }
    }

    private void displayCompoundInfo(Molecule molecule) {
        StringBuilder infoBuilder = new StringBuilder();

        infoBuilder.append("Compound Information:\n\n");
        appendIfNotNull(infoBuilder, "Preferred Name: ", molecule.getPrefName());
        appendIfNotNull(infoBuilder, "Molecular Formula: ", molecule.getMolecularFormula());
        appendIfNotNull(infoBuilder, "Molecular Weight: ", molecule.getMolecularWeight());
        appendIfNotNull(infoBuilder, "ChEMBL ID: ", molecule.getChemblId());
        appendIfNotNull(infoBuilder, "InChI Key: ", molecule.getInchiKey());
        appendIfNotNull(infoBuilder, "SMILES: ", molecule.getSmiles());

        if (molecule.getMoleculeProperties() != null) {
            MoleculeProperties props = molecule.getMoleculeProperties();
            appendIfNotNull(infoBuilder, "ALogP: ", props.alogp);
            appendIfNotNull(infoBuilder, "Polar Surface Area: ", props.psa);
            appendIfNotNull(infoBuilder, "H-Bond Donors: ", props.hbdCount);
            appendIfNotNull(infoBuilder, "H-Bond Acceptors: ", props.hbaCount);
            appendIfNotNull(infoBuilder, "Rotatable Bonds: ", props.rotatableBondCount);
        }

        infoBuilder.append("\nFunctional Groups: ").append(detectFunctionalGroups(molecule.getMolecularFormula()));

        analysisResult.setText(infoBuilder.toString());
    }

    private void appendIfNotNull(StringBuilder builder, String label, Object value) {
        if (value != null) {
            builder.append(label).append(value).append("\n");
        }
    }

    private String detectFunctionalGroups(String molecularFormula) {
        if (molecularFormula == null || molecularFormula.equals("N/A")) {
            return "Unable to detect functional groups";
        }

        StringBuilder groups = new StringBuilder();
        if (molecularFormula.contains("O")) groups.append("Alcohol/Ether, ");
        if (molecularFormula.contains("N")) groups.append("Amine, ");
        if (molecularFormula.contains("C=O")) groups.append("Carbonyl, ");
        if (molecularFormula.contains("COOH")) groups.append("Carboxylic Acid, ");
        if (molecularFormula.contains("C=C")) groups.append("Alkene, ");
        if (molecularFormula.contains("C#C")) groups.append("Alkyne, ");
        if (molecularFormula.contains("F") || molecularFormula.contains("Cl") ||
                molecularFormula.contains("Br") || molecularFormula.contains("I"))
            groups.append("Halide, ");

        if (groups.length() > 0) {
            groups.setLength(groups.length() - 2); // Remove trailing comma and space
            return groups.toString();
        } else {
            return "No common functional groups detected";
        }
    }

    private void loadImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(moleculeImage);
            moleculeImage.setVisibility(View.VISIBLE);
        } else {
            moleculeImage.setVisibility(View.GONE);
//            Toast.makeText(this, "No image available for this compound", Toast.LENGTH_SHORT).show();
//        }
    }}

    private void showError(String message) {
        analysisResult.setText(message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    // Inner classes for parsing the ChEMBL API response
    private static class ChEMBLResponse {
        @SerializedName("molecules")
        private List<Molecule> molecules;

        public List<Molecule> getMolecules() {
            return molecules;
        }
    }

    private static class Molecule {
        @SerializedName("pref_name")
        private String prefName;
        @SerializedName("molecule_chembl_id")
        private String chemblId;
        @SerializedName("molecule_structures")
        private MoleculeStructures moleculeStructures;
        @SerializedName("molecule_properties")
        private MoleculeProperties moleculeProperties;

        public String getPrefName() {
            return prefName;
        }

        public String getChemblId() {
            return chemblId;
        }

        public String getMolecularFormula() {
            return moleculeProperties != null ? moleculeProperties.fullMolformula : null;
        }

        public String getMolecularWeight() {
            return moleculeProperties != null ? moleculeProperties.molecularWeight : null;
        }

        public String getStructureImage() {
            return moleculeStructures != null ? moleculeStructures.structureImage : null;
        }

        public String getInchiKey() {
            return moleculeStructures != null ? moleculeStructures.standardInchiKey : null;
        }

        public String getSmiles() {
            return moleculeStructures != null ? moleculeStructures.canonicalSmiles : null;
        }

        public MoleculeProperties getMoleculeProperties() {
            return moleculeProperties;
        }
    }

    private static class MoleculeStructures {
        @SerializedName("standard_inchi_key")
        private String standardInchiKey;
        @SerializedName("canonical_smiles")
        private String canonicalSmiles;
        @SerializedName("structure_image")
        private String structureImage;
    }

    private static class MoleculeProperties {
        @SerializedName("full_molformula")
        private String fullMolformula;
        @SerializedName("mw_freebase")
        private String molecularWeight;
        @SerializedName("alogp")
        private String alogp;
        @SerializedName("psa")
        private String psa;
        @SerializedName("hbd")
        private String hbdCount;
        @SerializedName("hba")
        private String hbaCount;
        @SerializedName("rtb")
        private String rotatableBondCount;
    }
}