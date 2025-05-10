package com.example.smartmedbeta;
import android.util.Log;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.*;
import com.github.javafaker.Faker;
import java.util.concurrent.ThreadLocalRandom;

public class PatientRecordGenerator {


    private static final List<String> diagnoses = Arrays.asList(
            "Hypertension", "Diabetes", "Asthma", "Hyperlipidemia",
            "Coronary Artery Disease", "Chronic Kidney Disease",
            "Chronic Obstructive Pulmonary Disease", "Osteoporosis",
            "Depression", "Anxiety", "Migraine", "Arthritis",
            "Thyroid Disorder", "Parkinson’s Disease", "Alzheimer’s Disease",
            "Stroke", "Epilepsy", "Cancer",  "HIV/AIDS",
            "Pneumonia", "Bronchitis", "Tuberculosis",
            "Psoriasis", "Anemia", "Multiple Myeloma", "Hemophilia",
            "Dementia", "Cushing's Syndrome", "Chronic Fatigue Syndrome"
    );

    private static final Map<String, List<String>> medications = new HashMap<String, List<String>>() {{
        put("Hypertension", Arrays.asList("Lisinopril", "Amlodipine", "Hydrochlorothiazide", "Losartan", "Atenolol", "Valsartan", "Captopril"));
        put("Diabetes", Arrays.asList("Metformin", "Insulin", "Glipizide", "Sitagliptin", "Canagliflozin", "Glyburide", "Dulaglutide"));
        put("Asthma", Arrays.asList("Albuterol", "Fluticasone", "Budesonide", "Montelukast", "Formoterol", "Salmeterol", "Ipratropium"));
        put("Hyperlipidemia", Arrays.asList("Atorvastatin", "Rosuvastatin", "Simvastatin", "Ezetimibe", "Fenofibrate", "Lovastatin", "Pravastatin"));
        put("Coronary Artery Disease", Arrays.asList("Aspirin", "Clopidogrel", "Nitroglycerin", "Atorvastatin", "Bisoprolol", "Metoprolol", "Ranolazine"));
        put("Chronic Kidney Disease", Arrays.asList("Losartan", "Hydrochlorothiazide", "Furosemide", "Spironolactone", "Erythropoietin", "Calcium Acetate"));
        put("Chronic Obstructive Pulmonary Disease", Arrays.asList("Tiotropium", "Ipratropium", "Salmeterol", "Fluticasone", "Roflumilast", "Theophylline", "Budesonide"));
        put("Osteoporosis", Arrays.asList("Alendronate", "Risedronate", "Zoledronic Acid", "Denosumab", "Calcium Carbonate", "Ibandronate", "Teriparatide"));
        put("Depression", Arrays.asList("Fluoxetine", "Sertraline", "Escitalopram", "Bupropion", "Venlafaxine", "Duloxetine", "Amitriptyline"));
        put("Anxiety", Arrays.asList("Diazepam", "Lorazepam", "Buspirone", "Alprazolam", "Clonazepam", "Hydroxyzine", "Pregabalin"));
        put("Migraine", Arrays.asList("Sumatriptan", "Propranolol", "Topiramate", "Rizatriptan", "Frovatriptan", "Ergotamine", "Naproxen"));
        put("Arthritis", Arrays.asList("Ibuprofen", "Naproxen", "Methotrexate", "Hydroxychloroquine", "Celecoxib", "Leflunomide", "Adalimumab"));
        put("Thyroid Disorder", Arrays.asList("Levothyroxine", "Liothyronine", "Methimazole", "Propylthiouracil", "Radioactive Iodine"));
        put("Parkinson’s Disease", Arrays.asList("Levodopa", "Carbidopa", "Pramipexole", "Ropinirole", "Amantadine", "Selegiline"));
        put("Alzheimer’s Disease", Arrays.asList("Donepezil", "Rivastigmine", "Memantine", "Galantamine"));
        put("Stroke", Arrays.asList("Aspirin", "Clopidogrel", "Warfarin", "Dabigatran", "Alteplase"));
        put("Epilepsy", Arrays.asList("Phenytoin", "Valproate", "Carbamazepine", "Levetiracetam", "Lamotrigine"));
        put("Cancer", Arrays.asList("Cisplatin", "Doxorubicin", "Paclitaxel", "Methotrexate", "Tamoxifen", "Pembrolizumab"));
        put("HIV/AIDS", Arrays.asList("Tenofovir", "Emtricitabine", "Efavirenz", "Dolutegravir", "Lamivudine", "Zidovudine"));
        put("Pneumonia", Arrays.asList("Amoxicillin", "Azithromycin", "Ciprofloxacin", "Doxycycline", "Levofloxacin"));
        put("Bronchitis", Arrays.asList("Albuterol", "Ipratropium", "Cough suppressants", "Expectorants"));
        put("Obesity", Arrays.asList("Orlistat", "Liraglutide", "Phentermine", "Bupropion/Naltrexone", "Metformin"));
        put("Psoriasis", Arrays.asList("Adalimumab", "Etanercept", "Infliximab", "Ustekinumab", "Methotrexate"));
        put("Tuberculosis", Arrays.asList("Isoniazid", "Rifampin", "Pyrazinamide", "Ethambutol"));
        put("Anemia", Arrays.asList("Ferrous Sulfate", "Vitamin B12", "Folic Acid", "Erythropoietin"));
        put("Multiple Myeloma", Arrays.asList("Bortezomib", "Lenalidomide", "Dexamethasone", "Carfilzomib"));
        put("Hemophilia", Arrays.asList("Desmopressin", "Factor VIII", "Factor IX", "Emicizumab"));
        put("Dementia", Arrays.asList("Donepezil", "Rivastigmine", "Galantamine", "Memantine"));
        put("Cushing's Syndrome", Arrays.asList("Ketoconazole", "Metyrapone", "Pasireotide"));
        put("Chronic Fatigue Syndrome", Arrays.asList("Cognitive Behavioral Therapy", "Medications for symptoms", "Exercise therapy"));
    }};
    ;

    public static List<Map<String, Object>> generatePatientRecords(int numberOfRecords) {
        List<Map<String, Object>> patientRecords = new ArrayList<>();
        Faker faker = new Faker();

        List<String> alcoholUseOptions = Arrays.asList("None", "Occasional", "Moderate", "Heavy");
        List<String> smokingStatusOptions = Arrays.asList("Never", "Former Smoker", "Current Smoker");
        List<String> exerciseFrequencyOptions = Arrays.asList("None", "1-2 times/week", "3-5 times/week", "Daily");
        List<String> dietOptions = Arrays.asList("Balanced", "High-Protein", "Vegan", "Vegetarian", "Keto", "Mediterranean");
        List<String> familyHistoryOptions = Arrays.asList("Heart Disease", "Diabetes", "Cancer", "Hypertension", "None");
        List<String> insuranceProviders = Arrays.asList("HealthFirst", "UnitedHealth", "Aetna", "Cigna", "Blue Cross");
        List<String> coverageTypes = Arrays.asList("Basic", "Premium", "Comprehensive");
        List<String> vaccinationStatusOptions = Arrays.asList("Up to date", "Not up to date", "Unknown");

        for (int i = 0; i < numberOfRecords; i++) {
            Map<String, Object> patientRecord = new HashMap<>();

            patientRecord.put("patientID", faker.idNumber().valid());
            patientRecord.put("Name", faker.name().fullName());
            patientRecord.put("Age", ThreadLocalRandom.current().nextInt(18, 91));
            patientRecord.put("Gender", faker.demographic().sex());
            patientRecord.put("Ethnicity", faker.demographic().race());
            patientRecord.put("Weight", ThreadLocalRandom.current().nextDouble(50, 120));
            patientRecord.put("Height", ThreadLocalRandom.current().nextInt(150, 200));
            patientRecord.put("BloodPressure", ThreadLocalRandom.current().nextInt(90, 160) + "/" + ThreadLocalRandom.current().nextInt(60, 100));
            patientRecord.put("HeartRate", ThreadLocalRandom.current().nextInt(60, 100));
            patientRecord.put("BodyTemperature", ThreadLocalRandom.current().nextDouble(36.0, 37.5));


            String diagnosis = diagnoses.get(new Random().nextInt(diagnoses.size()));
            List<String> medicationList = medications.getOrDefault(diagnosis, Arrays.asList("None"));
            patientRecord.put("Diagnosis", diagnosis);
            patientRecord.put("Medication", medicationList.get(new Random().nextInt(medicationList.size())));

            patientRecord.put("LastVisitDate", generateRandomDateInCurrentYear().toString());
            patientRecord.put("NextAppointment", generateRandomFutureDate().toString());
            patientRecord.put("EmergencyContactName", faker.name().fullName());
            patientRecord.put("EmergencyContactPhone", faker.phoneNumber().cellPhone());
            patientRecord.put("EmergencyContactRelationship", "Parent");

            patientRecord.put("AlcoholUse", alcoholUseOptions.get(new Random().nextInt(alcoholUseOptions.size())));
            patientRecord.put("SmokingStatus", smokingStatusOptions.get(new Random().nextInt(smokingStatusOptions.size())));
            patientRecord.put("ExerciseFrequency", exerciseFrequencyOptions.get(new Random().nextInt(exerciseFrequencyOptions.size())));
            patientRecord.put("Diet", dietOptions.get(new Random().nextInt(dietOptions.size())));
            patientRecord.put("FamilyHistory", familyHistoryOptions.get(new Random().nextInt(familyHistoryOptions.size())));
            patientRecord.put("ChronicConditions", faker.medical().diseaseName());

            patientRecord.put("InsuranceProvider", insuranceProviders.get(new Random().nextInt(insuranceProviders.size())));
            patientRecord.put("InsurancePolicyNumber", faker.number().digits(10));
            patientRecord.put("InsuranceCoverageType", coverageTypes.get(new Random().nextInt(coverageTypes.size())));

            patientRecord.put("MedicalHistory", faker.lorem().sentence());
            patientRecord.put("Allergies", faker.food().ingredient());
            patientRecord.put("VaccinationStatus", vaccinationStatusOptions.get(new Random().nextInt(vaccinationStatusOptions.size())));

            patientRecords.add(patientRecord);
        }

        return patientRecords;
    }

    private static LocalDate generateRandomFutureDate() {
        LocalDate today = LocalDate.now();
        int randomDays = ThreadLocalRandom.current().nextInt(30, 365);
        return today.plusDays(randomDays);
    }


    private static LocalDate generateRandomDateInCurrentYear() {
        LocalDate start = LocalDate.of(LocalDate.now().getYear(), Month.JANUARY, 1);
        long days = ChronoUnit.DAYS.between(start, LocalDate.now());
        return start.plusDays(ThreadLocalRandom.current().nextLong(days + 1));
    }


}
