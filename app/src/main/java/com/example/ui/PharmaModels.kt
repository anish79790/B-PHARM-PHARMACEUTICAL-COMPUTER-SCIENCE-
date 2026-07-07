package com.example.ui

data class CommonDrugInfo(
    val name: String,
    val hindiName: String,
    val indications: String,
    val standardDose: String,
    val costPercentSaving: Int,
    val representativeBrand: String,
    val genericAlternative: String
)

data class MedicineSubclass(
    val name: String,
    val hindiName: String,
    val description: String,
    val commonDrugs: List<CommonDrugInfo>
)

data class Disease(
    val id: String,
    val name: String,
    val category: String,
    val overview: String,
    val symptoms: List<String>,
    val causes: List<String>,
    val tips: List<String>,
    val drugClasses: Map<String, String> // "Class Name" to "Examples"
)

data class Drug(
    val id: String,
    val name: String,
    val drugClass: String,
    val uses: List<String>,
    val mechanism: String,
    val sideEffects: List<String>,
    val dose: String,
    val contraindications: List<String>
)

object MedicalCatalog {
    val categories = listOf(
        "Neurology" to "Brain & Neurology",
        "Gastrology" to "Stomach & Digestion",
        "Cardiology" to "Heart & Blood Pressure",
        "Infections & Fungus" to "Antibiotics & Antifungals",
        "Endocrinology" to "Hormones & Diabetes",
        "Allergies & Pain" to "Antihistamines & Painkillers"
    )

    val categorySubclasses = mapOf(
        "Neurology" to listOf(
            MedicineSubclass(
                name = "Antidepressants",
                hindiName = "Antidepressants (Neurotransmitters Balance)",
                description = "Used to balance neurotransmitters in the brain for treating clinical depression, anxiety disorders, and chronic panic syndromes.",
                commonDrugs = listOf(
                    CommonDrugInfo(
                        name = "Fluoxetine",
                        hindiName = "Fluoxetine (Prozac Generic)",
                        indications = "Major Depressive Disorder, OCD, Panic Attacks",
                        standardDose = "20 mg daily in the morning with or without food",
                        costPercentSaving = 70,
                        representativeBrand = "Prozac (Abbott)",
                        genericAlternative = "Fludac (Cadila Pharmaceuticals)"
                    ),
                    CommonDrugInfo(
                        name = "Sertraline",
                        hindiName = "Sertraline (Zoloft Generic)",
                        indications = "Depression, Social Anxiety, PTSD",
                        standardDose = "50 mg once daily, morning or evening",
                        costPercentSaving = 65,
                        representativeBrand = "Zoloft (Pfizer)",
                        genericAlternative = "Sertima (Intas Pharmaceuticals)"
                    ),
                    CommonDrugInfo(
                        name = "Amitriptyline",
                        hindiName = "Amitriptyline (Tryptomer Generic)",
                        indications = "Neuropathic chronic pain, Migraine prevention, Insomnia",
                        standardDose = "10 mg to 25 mg at bedtime to avoid day sleepiness",
                        costPercentSaving = 75,
                        representativeBrand = "Elavil (Abbott)",
                        genericAlternative = "Tryptomer (Wockhardt Ltd)"
                    )
                )
            ),
            MedicineSubclass(
                name = "Anticonvulsants",
                hindiName = "Anticonvulsants (Seizures & Nerve Pain)",
                description = "Stabilizes abnormal electrical activity in the brain to treat seizures, epilepsy, and diabetic neuropathic nerve pains.",
                commonDrugs = listOf(
                    CommonDrugInfo(
                        name = "Gabapentin",
                        hindiName = "Gabapentin (Neurontin Generic)",
                        indications = "Postherpetic neuralgia, neuropathic shingles pain, partial seizures",
                        standardDose = "300 mg three times daily",
                        costPercentSaving = 60,
                        representativeBrand = "Neurontin (Pfizer)",
                        genericAlternative = "Gabapin (Intas Pharmaceuticals)"
                    ),
                    CommonDrugInfo(
                        name = "Carbamazepine",
                        hindiName = "Carbamazepine (Tegretol Generic)",
                        indications = "Trigeminal neuralgia, tonic-clonic epilepsy, bipolar mania",
                        standardDose = "200 mg twice daily with food",
                        costPercentSaving = 55,
                        representativeBrand = "Tegretol (Novartis)",
                        genericAlternative = "Mazetol (Abbott)"
                    )
                )
            ),
            MedicineSubclass(
                name = "Anxiolytics",
                hindiName = "Anxiolytics (Anxiety & Sleep Aids)",
                description = "GABA enhancers that act as central nervous system depressants to quickly relieve acute panic, severe anxiety, and insomnia.",
                commonDrugs = listOf(
                    CommonDrugInfo(
                        name = "Alprazolam",
                        hindiName = "Alprazolam (Xanax Generic)",
                        indications = "Generalized Anxiety, severe panic disorders",
                        standardDose = "0.25 mg to 0.5 mg orally three times daily as prescribed",
                        costPercentSaving = 50,
                        representativeBrand = "Xanax (Pfizer)",
                        genericAlternative = "Alprax (Torrent Pharmaceuticals)"
                    ),
                    CommonDrugInfo(
                        name = "Diazepam",
                        hindiName = "Diazepam (Valium Generic)",
                        indications = "Acute anxiety, muscle spasms, alcohol withdrawal symptoms",
                        standardDose = "2 mg to 10 mg orally 2 to 4 times daily",
                        costPercentSaving = 68,
                        representativeBrand = "Valium (Roche)",
                        genericAlternative = "Calmpose (Ranbaxy/Sun Pharma)"
                    )
                )
            )
        ),
        "Gastrology" to listOf(
            MedicineSubclass(
                name = "Proton Pump Inhibitors",
                hindiName = "PPI (Gastric Reflux & Acidity)",
                description = "Inhibits gastric parietal cell proton pumps to strongly suppress the production of stomach acid, facilitating ulcer healing and preventing acid reflux.",
                commonDrugs = listOf(
                    CommonDrugInfo(
                        name = "Omeprazole",
                        hindiName = "Omeprazole (Omez Generic)",
                        indications = "GERD, heartburn, active duodenal/gastric ulcers",
                        standardDose = "20 mg to 40 mg once daily, 30 minutes before breakfast",
                        costPercentSaving = 72,
                        representativeBrand = "Prilosec (AstraZeneca)",
                        genericAlternative = "Omez (Dr. Reddy's Laboratories)"
                    ),
                    CommonDrugInfo(
                        name = "Pantoprazole",
                        hindiName = "Pantoprazole (Pan-40 Generic)",
                        indications = "Severe erosive esophagitis, hypersecretory acidity conditions",
                        standardDose = "40 mg once daily, 30-60 minutes before first meal",
                        costPercentSaving = 64,
                        representativeBrand = "Protonix (Pfizer)",
                        genericAlternative = "Pan-40 (Alkem Laboratories)"
                    )
                )
            ),
            MedicineSubclass(
                name = "Antacids",
                hindiName = "Antacids (Stomach Acid Neutralizer)",
                description = "Alkaline compounds that chemically neutralize stomach hydrochloric acid instantly on contact to relieve heartburn and indigestion.",
                commonDrugs = listOf(
                    CommonDrugInfo(
                        name = "Calcium Carbonate",
                        hindiName = "Calcium Carbonate (Calcimax Generic)",
                        indications = "Acid indigestion, sour stomach, mild calcium supplement",
                        standardDose = "500 mg to 1000 mg chewed thoroughly when symptoms arise",
                        costPercentSaving = 45,
                        representativeBrand = "Tums (GSK)",
                        genericAlternative = "Calcimax (Meyer Organics)"
                    ),
                    CommonDrugInfo(
                        name = "Magnesium Hydroxide",
                        hindiName = "Magnesium Hydroxide (Cremaffin Generic)",
                        indications = "Constipation (laxative effect), acute stomach acidity",
                        standardDose = "400 mg per tablet, taken with full glass of water",
                        costPercentSaving = 50,
                        representativeBrand = "Milk of Magnesia (Bayer)",
                        genericAlternative = "Cremaffin (Abbott India)"
                    )
                )
            ),
            MedicineSubclass(
                name = "Antiemetics",
                hindiName = "Antiemetics (Nausea & Vomiting Relief)",
                description = "Blocks serotonin or dopamine signals in the brain and gut that trigger the vomiting reflex, providing relief from motion sickness, nausea, or gastroenteritis.",
                commonDrugs = listOf(
                    CommonDrugInfo(
                        name = "Ondansetron",
                        hindiName = "Ondansetron (Ondem Generic)",
                        indications = "Severe vomiting, chemotherapy-induced nausea, motion sickness",
                        standardDose = "4 mg to 8 mg rapidly disintegrating tablet or oral liquid",
                        costPercentSaving = 78,
                        representativeBrand = "Zofran (GSK)",
                        genericAlternative = "Ondem (Alkem Laboratories)"
                    ),
                    CommonDrugInfo(
                        name = "Domperidone",
                        hindiName = "Domperidone (Domstal Generic)",
                        indications = "Nausea, postprandial fullness, gastric bloating",
                        standardDose = "10 mg up to three times daily, taken before meals",
                        costPercentSaving = 58,
                        representativeBrand = "Motilium (Janssen)",
                        genericAlternative = "Domstal (Torrent Pharmaceuticals)"
                    )
                )
            )
        ),
        "Cardiology" to listOf(
            MedicineSubclass(
                name = "Beta Blockers",
                hindiName = "Beta Blockers (Hypertension & Heart Rate)",
                description = "Blocks adrenaline receptors to reduce heart rate and force of contraction, lowering blood pressure and workload on the heart.",
                commonDrugs = listOf(
                    CommonDrugInfo(
                        name = "Metoprolol",
                        hindiName = "Metoprolol (Metocard Generic)",
                        indications = "Hypertension, chronic angina pectoris, post-heart attack",
                        standardDose = "50 mg to 100 mg daily in single or split doses",
                        costPercentSaving = 62,
                        representativeBrand = "Lopressor (Novartis)",
                        genericAlternative = "Metocard (Alembic Pharmaceuticals)"
                    ),
                    CommonDrugInfo(
                        name = "Atenolol",
                        hindiName = "Atenolol (Aten Generic)",
                        indications = "Essential high BP, cardiac arrhythmias, early post-MI",
                        standardDose = "25 mg to 50 mg once daily orally",
                        costPercentSaving = 71,
                        representativeBrand = "Tenormin (AstraZeneca)",
                        genericAlternative = "Aten (Zydus Cadila)"
                    )
                )
            ),
            MedicineSubclass(
                name = "ACE Inhibitors",
                hindiName = "ACE Inhibitors (Vasodilation & BP)",
                description = "Relaxes arteries and reduces salt retention by blocking the formation of Angiotensin II, a major natural blood-vessel constricting hormone.",
                commonDrugs = listOf(
                    CommonDrugInfo(
                        name = "Lisinopril",
                        hindiName = "Lisinopril (Listril Generic)",
                        indications = "Hypertension, diabetic nephropathy, heart failure adjunct",
                        standardDose = "10 mg once daily, max up to 40 mg daily",
                        costPercentSaving = 68,
                        representativeBrand = "Zestril (AstraZeneca)",
                        genericAlternative = "Listril (Torrent Pharmaceuticals)"
                    ),
                    CommonDrugInfo(
                        name = "Enalapril",
                        hindiName = "Enalapril (Enam Generic)",
                        indications = "Hypertension, asymptomatic left ventricular dysfunction",
                        standardDose = "5 mg once daily, adjusted based on response",
                        costPercentSaving = 74,
                        representativeBrand = "Vasotec (Merck)",
                        genericAlternative = "Enam (Dr. Reddy's Laboratories)"
                    )
                )
            ),
            MedicineSubclass(
                name = "Calcium Channel Blockers",
                hindiName = "Calcium Channel Blockers (Vessel Relaxation)",
                description = "Inhibits calcium ions from entering heart and vascular smooth muscle cells, relaxing blood vessels and easing blood flow.",
                commonDrugs = listOf(
                    CommonDrugInfo(
                        name = "Amlodipine",
                        hindiName = "Amlodipine (Amlokind Generic)",
                        indications = "High blood pressure, chronic stable chest angina",
                        standardDose = "5 mg to 10 mg once daily",
                        costPercentSaving = 80,
                        representativeBrand = "Norvasc (Pfizer)",
                        genericAlternative = "Amlokind (Mankind Pharma)"
                    )
                )
            )
        ),
        "Infections & Fungus" to listOf(
            MedicineSubclass(
                name = "Antibiotics",
                hindiName = "Antibiotics (Bacterial Infection)",
                description = "Inhibits bacterial cell wall synthesis or protein assembly, stopping bacterial growth to cure systemic infections.",
                commonDrugs = listOf(
                    CommonDrugInfo(
                        name = "Amoxicillin",
                        hindiName = "Amoxicillin (Mox Generic)",
                        indications = "Throat, ear, urinary tract, and dental bacterial infections",
                        standardDose = "500 mg three times daily for 5-7 days",
                        costPercentSaving = 55,
                        representativeBrand = "Amoxil (GSK)",
                        genericAlternative = "Mox (Sun Pharmaceutical Industries)"
                    ),
                    CommonDrugInfo(
                        name = "Azithromycin",
                        hindiName = "Azithromycin (Azee Generic)",
                        indications = "Strep throat, community pneumonia, skin infections, typhoid",
                        standardDose = "500 mg once daily for 3 days on empty stomach",
                        costPercentSaving = 60,
                        representativeBrand = "Zithromax (Pfizer)",
                        genericAlternative = "Azee (Cipla Ltd)"
                    ),
                    CommonDrugInfo(
                        name = "Ciprofloxacin",
                        hindiName = "Ciprofloxacin (Ciplox Generic)",
                        indications = "Urinary tract infections (UTIs), typhoid fever, infectious diarrhea",
                        standardDose = "500 mg twice daily with plenty of fluids",
                        costPercentSaving = 65,
                        representativeBrand = "Cipro (Bayer)",
                        genericAlternative = "Ciplox (Cipla Ltd)"
                    )
                )
            ),
            MedicineSubclass(
                name = "Antifungals",
                hindiName = "Antifungals (Fungal Infection)",
                description = "Disrupts the synthesis of fungal cell membranes, eradicating ringworm, dandruff, nail, and candidiasis infections.",
                commonDrugs = listOf(
                    CommonDrugInfo(
                        name = "Fluconazole",
                        hindiName = "Fluconazole (Forcan Generic)",
                        indications = "Vaginal yeast infections, oral thrush, ringworm, nail fungus",
                        standardDose = "150 mg single oral capsule, or as directed for chronic cases",
                        costPercentSaving = 70,
                        representativeBrand = "Diflucan (Pfizer)",
                        genericAlternative = "Forcan (Cipla Ltd)"
                    ),
                    CommonDrugInfo(
                        name = "Itraconazole",
                        hindiName = "Itraconazole (Canditral Generic)",
                        indications = "Severe fungal infections of nails, skin, or systemic fungal issues",
                        standardDose = "100 mg to 200 mg daily with a full meal",
                        costPercentSaving = 50,
                        representativeBrand = "Sporanox (Janssen)",
                        genericAlternative = "Canditral (Glenmark Pharmaceuticals)"
                    )
                )
            )
        ),
        "Endocrinology" to listOf(
            MedicineSubclass(
                name = "Antidiabetics",
                hindiName = "Antidiabetics (Blood Glucose Regulator)",
                description = "Improves peripheral insulin sensitivity and shuts down liver glucose output to regulate blood sugar levels.",
                commonDrugs = listOf(
                    CommonDrugInfo(
                        name = "Metformin",
                        hindiName = "Metformin (Glycomet Generic)",
                        indications = "Type 2 Diabetes mellitus, insulin resistance syndrome, PCOS",
                        standardDose = "500 mg to 1000 mg twice daily with food",
                        costPercentSaving = 75,
                        representativeBrand = "Glucophage (Merck)",
                        genericAlternative = "Glycomet (USV Private Ltd)"
                    ),
                    CommonDrugInfo(
                        name = "Glimepiride",
                        hindiName = "Glimepiride (Glimy Generic)",
                        indications = "Type 2 Diabetes blood glucose control",
                        standardDose = "1 mg to 2 mg once daily with the first main meal",
                        costPercentSaving = 65,
                        representativeBrand = "Amaryl (Sanofi)",
                        genericAlternative = "Glimy (Alkem Laboratories)"
                    )
                )
            ),
            MedicineSubclass(
                name = "Thyroid Hormones",
                hindiName = "Thyroid Hormones (Hypothyroidism Supplement)",
                description = "Synthetic thyroid hormone replacement therapy used to treat underactive thyroid gland (hypothyroidism).",
                commonDrugs = listOf(
                    CommonDrugInfo(
                        name = "Levothyroxine",
                        hindiName = "Levothyroxine (Thyronorm Generic)",
                        indications = "Hypothyroidism, goiter, thyroid cancer suppression",
                        standardDose = "25 mcg to 100 mcg once daily first thing in morning empty stomach",
                        costPercentSaving = 58,
                        representativeBrand = "Synthroid (AbbVie)",
                        genericAlternative = "Thyronorm (Abbott India)"
                    )
                )
            )
        ),
        "Allergies & Pain" to listOf(
            MedicineSubclass(
                name = "Antihistamines",
                hindiName = "Antihistamines (Allergy Relief)",
                description = "Blocks histamine receptors to prevent inflammatory responses such as sneezing, watery eyes, hives, and runny nose.",
                commonDrugs = listOf(
                    CommonDrugInfo(
                        name = "Cetirizine",
                        hindiName = "Cetirizine (Cetzine Generic)",
                        indications = "Allergic rhinitis, hives, hay fever, skin itching",
                        standardDose = "10 mg once daily in the evening (may cause mild drowsiness)",
                        costPercentSaving = 82,
                        representativeBrand = "Zyrtec (Abbott)",
                        genericAlternative = "Cetzine (GlaxoSmithKline India)"
                    ),
                    CommonDrugInfo(
                        name = "Levocetirizine",
                        hindiName = "Levocetirizine (1-AL Generic)",
                        indications = "Seasonal allergies, chronic hives, itching rashes",
                        standardDose = "5 mg once daily at bedtime for deep sleep benefit",
                        costPercentSaving = 75,
                        representativeBrand = "Xyzal (Sanofi)",
                        genericAlternative = "1-AL (FDC Ltd)"
                    )
                )
            ),
            MedicineSubclass(
                name = "NSAIDs & Painkillers",
                hindiName = "NSAIDs & Painkillers (Fever & Pain Relief)",
                description = "Inhibits cyclooxygenase (COX) enzymes to prevent prostaglandin synthesis, relieving pains, fevers, and joint inflammations.",
                commonDrugs = listOf(
                    CommonDrugInfo(
                        name = "Ibuprofen",
                        hindiName = "Ibuprofen (Ibugesic Generic)",
                        indications = "Headache, dental pain, arthritis, muscular inflammation",
                        standardDose = "200 mg to 400 mg every 6 hours with food to avoid stomach distress",
                        costPercentSaving = 60,
                        representativeBrand = "Advil (Pfizer)",
                        genericAlternative = "Ibugesic (Cipla Ltd)"
                    ),
                    CommonDrugInfo(
                        name = "Paracetamol",
                        hindiName = "Paracetamol (Calpol / Crocin Generic)",
                        indications = "High fevers, body aches, headaches, post-vaccine aches",
                        standardDose = "500 mg to 650 mg every 4-6 hours (max 4000 mg daily)",
                        costPercentSaving = 78,
                        representativeBrand = "Panadol (GSK)",
                        genericAlternative = "Calpol (GSK India) or Crocin (GSK)"
                    )
                )
            )
        )
    )

    val diseases = listOf(
        Disease(
            id = "disease_diabetes",
            name = "Diabetes Mellitus (Type 2)",
            category = "Endocrinology",
            overview = "A chronic metabolic condition characterized by high blood sugar (glucose) levels, which results from the body's inability to produce or effectively use insulin. It can lead to long-term complications including neuropathy, retinopathy, and cardiovascular disease.",
            symptoms = listOf(
                "Frequent urination (Polyuria)",
                "Excessive thirst (Polydipsia)",
                "Increased hunger (Polyphagia)",
                "Unusual or rapid fatigue",
                "Blurred vision",
                "Slow-healing sores or cuts"
            ),
            causes = listOf(
                "Insulin resistance",
                "Genetic / Family history",
                "Sedentary lifestyle & lack of physical exercise",
                "Excess body weight (especially abdominal obesity)",
                "Diets high in refined sugar and processed foods"
            ),
            tips = listOf(
                "Eat healthy (High fiber, low sugars)",
                "Exercise regularly (150 mins aerobic/week)",
                "Monitor blood sugar daily",
                "Stay hydrated (Prefer water over soda)"
            ),
            drugClasses = mapOf(
                "Biguanides" to "Metformin",
                "Sulfonylureas" to "Glimepiride, Gliclazide",
                "DPP-4 Inhibitors" to "Sitagliptin, Vildagliptin",
                "SGLT2 Inhibitors" to "Dapagliflozin, Empagliflozin",
                "Insulins" to "Insulin Glargine, Insulin Aspart",
                "Thiazolidinediones" to "Pioglitazone"
            )
        ),
        Disease(
            id = "disease_influenza",
            name = "Influenza (Flu)",
            category = "Infections & Fungus",
            overview = "A highly contagious viral infection that attacks the respiratory system, including the nose, throat, and lungs. Unlike common colds, influenza hits suddenly and causes much more severe systemic symptoms like high fevers and muscle body aches.",
            symptoms = listOf(
                "Sudden onset of high fever (100.4°F or higher)",
                "Severe muscle or body aches",
                "Chills and sweats",
                "Dry, persistent cough",
                "Nasal congestion and sore throat",
                "Fatigue and weakness"
            ),
            causes = listOf(
                "Influenza virus (Type A and Type B)",
                "Inhaling virus-carrying respiratory droplets from coughs/sneezes",
                "Touching surfaces contaminated with the flu virus"
            ),
            tips = listOf(
                "Absolute bed rest to conserve energy",
                "Stay hydrated with warm water and herbal teas",
                "Perform warm saline gargles for sore throat",
                "Isolate yourself from others to stop viral spread"
            ),
            drugClasses = mapOf(
                "Neuraminidase Inhibitors" to "Oseltamivir (Tamiflu)",
                "Analgesics / Antipyretics" to "Ibuprofen, Acetaminophen (Paracetamol)"
            )
        ),
        Disease(
            id = "disease_hypertension",
            name = "Hypertension (High Blood Pressure)",
            category = "Cardiology",
            overview = "A chronic medical condition where the long-term force of blood against the artery walls is consistently elevated. Often referred to as the 'Silent Killer' because it typically exhibits no obvious outward symptoms while gradually damaging blood vessels and organs.",
            symptoms = listOf(
                "Asymptomatic in most standard cases",
                "Severe throbbing headaches (in hypertensive crisises)",
                "Shortness of breath",
                "Nosebleeds",
                "Dizziness or heart palpitations"
            ),
            causes = listOf(
                "High dietary sodium (excess salt consumption)",
                "Lack of moderate physical aerobic exercise",
                "Chronic high stress levels",
                "Smoking and heavy alcohol consumption",
                "Family history of high blood pressure"
            ),
            tips = listOf(
                "Follow a DASH diet (high potassium, low salt)",
                "Engage in 30 mins of moderate aerobic exercise daily",
                "Cut down caffeine and avoid tobacco use",
                "Maintain a healthy body weight"
            ),
            drugClasses = mapOf(
                "ACE Inhibitors" to "Lisinopril, Enalapril",
                "Beta-Blockers" to "Metoprolol, Atenolol",
                "Calcium Channel Blockers" to "Amlodipine, Nifedipine",
                "Angiotensin II Receptors" to "Losartan, Valsartan",
                "Thiazide Diuretics" to "Hydrochlorothiazide"
            )
        ),
        Disease(
            id = "disease_gerd",
            name = "GERD (Acid Reflux)",
            category = "Gastrology",
            overview = "Gastroesophageal Reflux Disease (GERD) is a digestive disorder where acidic contents from the stomach back up repeatedly into the tube connecting your mouth and stomach (esophagus), irritating its sensitive lining.",
            symptoms = listOf(
                "Heartburn (burning sensation in chest after eating)",
                "Acid regurgitation (sour or bitter taste in mouth)",
                "Difficulty swallowing (dysphagia)",
                "Sensation of a lump in your throat",
                "Chronic dry hacking cough"
            ),
            causes = listOf(
                "Weakness or relaxation of lower esophageal sphincter (LES)",
                "Hiatal hernia shifting gastric layouts",
                "Obesity increasing abdominal pressure",
                "Trigger foods (deep-fried, spicy, caffeine, citrus, mints)",
                "Lying down immediately after heavy meals"
            ),
            tips = listOf(
                "Eat smaller, more frequent meals",
                "Do not lie down for 3 hours after eating",
                "Elevate the head of your bed by 6 inches",
                "Identify and avoid specific dietary trigger foods"
            ),
            drugClasses = mapOf(
                "Proton Pump Inhibitors (PPIs)" to "Omeprazole, Pantoprazole",
                "H2 Receptor Blockers" to "Famotidine, Ranitidine",
                "Antacids (Symptomatic)" to "Calcium Carbonate, Magnesium Hydroxide"
            )
        ),
        Disease(
            id = "disease_asthma",
            name = "Asthma (Bronchial)",
            category = "Allergies & Pain",
            overview = "A respiratory condition in which the airways periodically swell, narrow, and secrete excess mucus. This makes air passage difficult and triggers breathlessness, wheezing sounds when exhaling, and troublesome coughing.",
            symptoms = listOf(
                "Shortness of breath (breathlessness)",
                "Chest tightness or weight-like pressure",
                "Wheezing or high-pitched whistling during exhalation",
                "Coughing attacks triggered by cold air or laughter"
            ),
            causes = listOf(
                "Genetic hyper-reactivity of the immune bronchial system",
                "Allergies (pollen, house dust mites, pet dander, mold)",
                "Cold environmental temperatures",
                "Exercise or intense physical activity",
                "Stress and emotional anxiety"
            ),
            tips = listOf(
                "Identify and avoid environmental allergens",
                "Always carry your rescue SABA inhaler",
                "Track respiration with a Peak Flow Meter",
                "Practice calm, controlled diaphragmatic breathing"
            ),
            drugClasses = mapOf(
                "SABA (Rescue Bronchodilator)" to "Albuterol (Salbutamol)",
                "Inhaled Corticosteroids (ICS)" to "Fluticasone, Budesonide",
                "LABA (Long-acting Relief)" to "Salmeterol, Formoterol"
            )
        )
    )

    val drugs = listOf(
        Drug(
            id = "drug_metformin",
            name = "Metformin",
            drugClass = "Biguanide (Antidiabetic)",
            uses = listOf(
                "First-line management of Type 2 Diabetes Mellitus",
                "Polycystic Ovary Syndrome (PCOS) insulin sensitizing",
                "Prevention of diabetic progression in high-risk pre-diabetics"
            ),
            mechanism = "Works by lowering hepatic glucose production (shutting down liver sugar release), decreasing the intestinal absorption of glucose, and improving insulin sensitivity by increasing peripheral glucose uptake and utilization in skeletal muscles.",
            sideEffects = listOf(
                "Nausea, vomiting, and temporary flatulence",
                "Abdominal cramps and diarrhea (usually resolves in 2 weeks)",
                "Metallic taste in mouth",
                "Long-term use can decrease Vitamin B12 absorption"
            ),
            dose = "Starts at 500 mg orally once or twice daily with food. Maximum adult dose is 2550 mg daily in divided portions.",
            contraindications = listOf(
                "Severe renal impairment (eGFR below 30 mL/min/1.73m²)",
                "Acute or chronic metabolic acidosis, including diabetic ketoacidosis",
                "Severe dehydration, sepsis, or shock"
            )
        ),
        Drug(
            id = "drug_ciprofloxacin",
            name = "Ciprofloxacin",
            drugClass = "Fluoroquinolone Antibiotic",
            uses = listOf(
                "Complicated Urinary Tract Infections (UTIs) & pyelonephritis",
                "Bacterial sinusitis, bone infections, and infectious diarrhea",
                "Severe skin and soft tissue structures infections"
            ),
            mechanism = "Bactericidal action. Inhibits bacterial DNA gyrase (topoisomerase II) and topoisomerase IV enzymes, which are vital for bacterial DNA replication, gene transcription, and chromosome repairing.",
            sideEffects = listOf(
                "Nausea and diarrhea",
                "Dizziness, headaches, and sleep disturbances (insomnia)",
                "Photosensitivity (severe sunburn from UV light)",
                "Rare risk of tendonitis or tendon rupture, particularly Achilles tendon"
            ),
            dose = "Oral tablet: 250 mg to 750 mg every 12 hours depending on the site and severity of infection.",
            contraindications = listOf(
                "Hypersensitivity to ciprofloxacin or other fluoroquinolones",
                "Concomitant administration with tizanidine (muscle relaxant)",
                "History of tendon disorders or myasthenia gravis"
            )
        ),
        Drug(
            id = "drug_lisinopril",
            name = "Lisinopril",
            drugClass = "ACE Inhibitor (Antihypertensive)",
            uses = listOf(
                "Treatment of Essential Hypertension",
                "Adjunctive therapy in Congestive Heart Failure",
                "Improves survival rates following acute myocardial infarction (heart attack)",
                "Slowing diabetic kidney damage (nephropathy)"
            ),
            mechanism = "Inhibits Angiotensin-Converting Enzyme (ACE), blocking the bio-conversion of inactive Angiotensin I into Angiotensin II, which is a key vasoconstrictor. This actions dilates blood channels and decreases aldosterone secretion, reducing blood pressure.",
            sideEffects = listOf(
                "Persistant, dry, hacking non-productive cough (very common)",
                "Dizziness, lightheadedness, and orthostatic hypotension",
                "Hyperkalemia (elevated blood potassium)",
                "Increased blood urea nitrogen (BUN) and creatinine (kidney stress)"
            ),
            dose = "Hypertension: Initial dose is 10 mg orally once daily. Maintenance range is 20 mg to 40 mg once daily.",
            contraindications = listOf(
                "History of angioedema (hereditary, idiopathic, or drug-induced)",
                "Second and third trimesters of pregnancy (severe risk of fetal toxicity)",
                "Concomitant use with aliskiren in diabetic patients"
            )
        ),
        Drug(
            id = "drug_omeprazole",
            name = "Omeprazole",
            drugClass = "Proton Pump Inhibitor (PPI)",
            uses = listOf(
                "Gastroesophageal Reflux Disease (GERD)",
                "Healing and maintenance of erosive esophagitis",
                "Active gastric or duodenal ulcers treatment",
                "Eradication of H. pylori bacterial infections in combination therapy"
            ),
            mechanism = "Works by irreversibly binding to and inhibiting the hydrogen/potassium adenosine triphosphatase (H+/K+ ATPase) enzyme systems, also called the Proton Pump, located in gastric parietal cells. This stops the secretion of stomach acid.",
            sideEffects = listOf(
                "Headaches",
                "Abdominal pain, flatulence, nausea, or mild diarrhea",
                "Long-term use can reduce calcium, magnesium, and B12 levels",
                "Marginal increase in susceptibility to enteric infections (C. difficile)"
            ),
            dose = "Standard dose is 20 mg to 40 mg orally once daily, taken 30 to 60 minutes before the first meal of the day.",
            contraindications = listOf(
                "Known hypersensitivity to omeprazole or benzimidazole drugs",
                "Co-administration with rilpivirine-containing antiretroviral agents"
            )
        ),
        Drug(
            id = "drug_albuterol",
            name = "Albuterol (Salbutamol)",
            drugClass = "Short-Acting Beta-2 Agonist (Bronchodilator)",
            uses = listOf(
                "Relief of acute bronchospasm inside Asthma, COPD, or bronchitis",
                "Prevention of exercise-induced bronchoconstriction (EIBA)",
                "Rescue therapy during respiratory asthma flaring-ups"
            ),
            mechanism = "Selectively acts upon the beta-2 adrenergic receptors located in bronchial smooth muscles, stimulating adenyl cyclase. This increases intracellular cAMP, relaxing tracheal smooth muscles and widening airways.",
            sideEffects = listOf(
                "Tremors (involuntary shaking, especially hands)",
                "Nervousness, anxiety, and sleeplessness",
                "Tachycardia (elevated heart rate) and heart palpitations",
                "Hypokalemia (especially with large rescue doses)"
            ),
            dose = "MDI Inhaler: 1 to 2 puffs (90 mcg per actuation) inhaled via inhaler mouth every 4 to 6 hours as needed.",
            contraindications = listOf(
                "Hypersensitivity to albuterol, levalbuterol, or milk proteins",
                "Use with extreme caution inside severe cardiac arrhythmias"
            )
        ),
        Drug(
            id = "drug_ibuprofen",
            name = "Ibuprofen",
            drugClass = "Non-Steroidal Anti-Inflammatory (NSAID)",
            uses = listOf(
                "Temporary reduction of fever",
                "Relief of mild to moderate pain (dental, headache, muscular, backache)",
                "Reduction of inflammatory swelling in rheumatologic conditions"
            ),
            mechanism = "Non-selectively blocks cyclooxygenase-1 and 2 (COX-1 and COX-2) enzymes. This block prevents the synthesis of inflammatory prostaglandins which sensitize pain receptors and elevate hypothalamic temp setpoints.",
            sideEffects = listOf(
                "Gastric upset, indigestion, nausea, or heartburn",
                "Fluid retention, swelling, and mild blood pressure rise",
                "Prolonged bleeding times",
                "Long-term high doses carry risks of kidney damage or stomach ulcers"
            ),
            dose = "OTC Analgesic: 200 mg to 400 mg orally every 4 to 6 hours when eating. Maximum safe daily limit is 1200 mg (OTC) or 3200 mg under medical supervision.",
            contraindications = listOf(
                "Third trimester of pregnancy (risk of premature closure of fetal ductus arteriosus)",
                "Active peptic ulcer disease, gastrointestinal bleeding, or renal failure",
                "History of asthma or hives triggered by aspirin or other NSAIDs",
                "Immediately following coronary artery bypass graft (CABG) surgery"
            )
        )
    )

    fun getLocalInteraction(drugA: String, drugB: String): Pair<String, String>? {
        val d1 = drugA.lowercase().trim()
        val d2 = drugB.lowercase().trim()

        val isMetformin = { name: String -> name.contains("metformin") }
        val isCipro = { name: String -> name.contains("ciprofloxacin") || name.contains("cipro") }
        val isLisinopril = { name: String -> name.contains("lisinopril") }
        val isIbuprofen = { name: String -> name.contains("ibuprofen") }
        val isOmeprazole = { name: String -> name.contains("omeprazole") }

        return when {
            // Metformin + Ciprofloxacin
            (isMetformin(d1) && isCipro(d2)) || (isCipro(d1) && isMetformin(d2)) -> {
                "Moderate" to "Ciprofloxacin can increase plasma concentrations of Metformin by competing for renal tubular transporters. This elevated level gains a greater risk of hypoglycemia and lactic acidosis. Monitor blood sugar levels closely."
            }
            // Lisinopril + Ibuprofen
            (isLisinopril(d1) && isIbuprofen(d2)) || (isIbuprofen(d1) && isLisinopril(d2)) -> {
                "Severe" to "Concomitant use of NSAIDs like Ibuprofen with ACE inhibitors like Lisinopril can significantly compromise renal kidney function, potentially inducing acute kidney failure. Furthermore, Ibuprofen can blunt the blood pressure-lowering utility of Lisinopril. Avoid unless strictly supervised."
            }
            // Omeprazole + Ciprofloxacin
            (isOmeprazole(d1) && isCipro(d2)) || (isCipro(d1) && isOmeprazole(d2)) -> {
                "Mild" to "Omeprazole decreases gastric acidity which can theoretically reduce the systemic absorption and blood levels of Ciprofloxacin. While not universally dangerous, it is recommended to administer Ciprofloxacin 2 hours before or 6 hours after acid suppressants."
            }
            // Ibuprofen + Metformin
            (isIbuprofen(d1) && isMetformin(d2)) || (isMetformin(d1) && isIbuprofen(d2)) -> {
                "Moderate" to "Ibuprofen can reduce kidney blood flow and kidney output, particularly in dehydrated or elderly individuals. This reduction slows down the clearance of Metformin, raising the risk of severe Metformin-induced lactic acidosis. Hydrate well."
            }
            else -> null
        }
    }
}
