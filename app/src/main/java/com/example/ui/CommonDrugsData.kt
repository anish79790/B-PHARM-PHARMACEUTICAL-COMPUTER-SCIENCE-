package com.example.ui

data class InfographicDrug(
    val number: Int,
    val name: String,
    val uses: String,
    val sideEffects: String,
    val precautions: String,
    val alternativeBrand: String
)

data class InfographicCategory(
    val index: Int,
    val title: String,
    val accentColorHex: String,
    val drugs: List<InfographicDrug>
)

object CommonDrugsData {
    val categories = listOf(
        InfographicCategory(
            index = 1,
            title = "1. Pain Relievers (Analgesics)",
            accentColorHex = "#E53935", // Crimson/Red
            drugs = listOf(
                InfographicDrug(1, "Paracetamol", "Pain, Fever, Mild discomfort", "Liver strain (in high doses), skin rash", "Limit daily intake to 4g to avoid liver injury.", "Paracip, Crocin (Save up to 75%)"),
                InfographicDrug(2, "Ibuprofen", "Pain, Swelling, Inflammation", "Stomach upset, heartburn, acid reflux", "Take after food. Avoid in kidney disease or stomach ulcers.", "Brufen, Ibugesic (Save up to 80%)"),
                InfographicDrug(3, "Diclofenac", "Joint Pain, Arthritis, Swelling", "Acid reflux, dizziness, headache", "Apply topically when possible to reduce stomach risks.", "Voveran, Dynapar (Save up to 78%)"),
                InfographicDrug(4, "Aspirin", "Pain, Fever, Blood thinner", "Bleeding risk, stomach ulcers, tinnitus", "Never give to children with fever (Reye's syndrome risk).", "Ecosprin, Loprin (Save up to 70%)"),
                InfographicDrug(5, "Naproxen", "Pain, Joint stiffness, menstrual cramps", "Headache, drowsiness, constipation", "Avoid taking with other NSAIDs like Ibuprofen.", "Naxdom, Naprosyn (Save up to 72%)"),
                InfographicDrug(6, "Ketorolac", "Severe acute post-surgical pain", "Dizziness, fluid retention, gastrointestinal bleeding", "Usually restricted to maximum 5 days of use.", "Ketanov (Save up to 82%)"),
                InfographicDrug(7, "Tramadol", "Moderate to Severe physical pain", "Nausea, constipation, drowsiness, dry mouth", "Can be habit-forming. Use strictly as prescribed.", "Ultram, Contramal (Save up to 75%)"),
                InfographicDrug(8, "Codeine", "Severe dry cough, mild pain relief", "Constipation, mild sedation, respiratory depression", "Potential for physical dependence with long use.", "Codistar, Ascoril-C (Save up to 70%)"),
                InfographicDrug(9, "Morphine", "Severe post-op pain, severe trauma, cancer pain", "Respiratory depression, constipation, addiction risk", "Highly regulated narcotic. Monitor breathing closely.", "Morphitroy, Vermor (Save up to 85%)"),
                InfographicDrug(10, "Pethidine", "Moderate to severe obstetric & acute spasm pain", "Nausea, dry mouth, sweating, dizziness", "Used primarily in hospital emergencies & labor.", "Demerol, Pethisan (Save up to 80%)")
            )
        ),
        InfographicCategory(
            index = 2,
            title = "2. Antibiotics (Bacterial Infections)",
            accentColorHex = "#00897B", // Teal/Emerald
            drugs = listOf(
                InfographicDrug(11, "Amoxicillin", "Throat, ear, dental & chest infections", "Diarrhea, skin rash, nausea", "Always complete the full prescribed course even if feeling better.", "Novamox, Mox (Save up to 75%)"),
                InfographicDrug(12, "Augmentin", "Resistant respiratory, skin, joint infections", "Indigestion, oral thrush, loose stools", "Combination of Amoxicillin and Clavulanate for broader coverage.", "Clavam, Moxikind-CV (Save up to 80%)"),
                InfographicDrug(13, "Azithromycin", "Bronchitis, throat infection, typhoid, skin", "Vomiting, stomach pain, diarrhea", "Typically a short 3-to-5 day once-daily course.", "Azithral, Azee (Save up to 72%)"),
                InfographicDrug(14, "Ciprofloxacin", "Urinary tract, bone & gastrointestinal infections", "Tendon pain or swelling, mild diarrhea", "Stop immediately if you experience joint or tendon pain.", "Cifran, Ciplox (Save up to 78%)"),
                InfographicDrug(15, "Doxycycline", "Severe acne, malaria prevention, tick-borne fevers", "Photosensitivity (easy sunburn), mild nausea", "Do not lie down for 30 minutes after taking to prevent esophageal irritation.", "Microdox, Doxy-1 (Save up to 83%)"),
                InfographicDrug(16, "Cephalexin", "Bone, urinary tract, skin & throat infections", "Mild stomach upset, skin itching, diarrhea", "Belongs to 1st generation Cephalosporins. Check for penicillin allergies.", "Phexin, Cephadex (Save up to 75%)"),
                InfographicDrug(17, "Ceftriaxone", "Severe meningitis, typhoid, hospital sepsis", "Injection site pain, local rash, liver changes", "Given as intravenous or intramuscular injection.", "Monocef, Zone (Save up to 79%)"),
                InfographicDrug(18, "Clarithromycin", "Severe chest infections, H. pylori stomach ulcers", "Altered metallic taste, headache, diarrhea", "Often paired with antacids for ulcer treatment.", "Crixan, Claribid (Save up to 74%)"),
                InfographicDrug(19, "Metronidazole", "Anaerobic dental, gut & protozoal infections", "Metallic taste, dark-colored urine, headache", "Avoid alcohol completely during treatment to prevent severe flushing & nausea.", "Flagyl, Metrogyl (Save up to 85%)"),
                InfographicDrug(20, "Vancomycin", "Serious resistant MRSA or severe colitis", "Kidney strain, flushing (\"red man syndrome\")", "Requires blood level monitoring in hospital setups.", "Vancocin, Vantox (Save up to 80%)")
            )
        ),
        InfographicCategory(
            index = 3,
            title = "3. Heart & BP Drugs",
            accentColorHex = "#1E88E5", // Blue
            drugs = listOf(
                InfographicDrug(21, "Amlodipine", "High Blood Pressure, Angina chest pain", "Swelling in ankles (edema), fatigue, headache", "Do not stop suddenly. Monitor blood pressure weekly.", "Amlokind, Stamlo (Save up to 82%)"),
                InfographicDrug(22, "Atenolol", "High Blood Pressure, irregular heartbeat", "Cold extremities, tiredness, slow pulse", "A beta-blocker. Avoid in severe asthma or bradycardia.", "Aten, Atcard (Save up to 76%)"),
                InfographicDrug(23, "Metoprolol", "High Blood Pressure, preventing heart failure", "Dizziness, slow heart rate, fatigue", "Take with or immediately after meals to improve absorption.", "Metolar, Betaloc (Save up to 78%)"),
                InfographicDrug(24, "Carvedilol", "Congestive Heart Failure, hypertension", "Postural dizziness, dry eyes, weight gain", "Helps protect the heart muscle from over-exertion.", "Carca, Cardivas (Save up to 80%)"),
                InfographicDrug(25, "Losartan", "Hypertension, kidney protection in diabetes", "High blood potassium levels, muscle cramps", "Avoid potassium supplements or salt substitutes unless advised.", "Losacar, Covance (Save up to 75%)"),
                InfographicDrug(26, "Enalapril", "High Blood Pressure, post-heart attack recovery", "Dry hacking cough, dizziness, loss of taste", "Dry cough is a common class side effect. Inform your doctor if severe.", "Nuril, Envas (Save up to 81%)"),
                InfographicDrug(27, "Lisinopril", "Hypertension, congestive heart failure", "Dry persistent cough, lightheadedness, fatigue", "Check kidney function tests regularly during therapy.", "Listril, Lipril (Save up to 78%)"),
                InfographicDrug(28, "Furosemide", "Water retention (edema), high blood pressure", "Dehydration, low potassium, muscle weakness", "A powerful loop diuretic. Often taken in the morning to avoid sleep disruption.", "Lasix, Frusenex (Save up to 84%)"),
                InfographicDrug(29, "Spironolactone", "Heart failure, potassium-sparing diuretic", "Breast tenderness (gynecomastia), high potassium", "Helps preserve potassium while shedding excess water.", "Aldactone, Torget-Plus (Save up to 75%)"),
                InfographicDrug(30, "Aspirin (Low Dose)", "Preventing heart attack, ischemic strokes", "Mild indigestion, easy bruising, bleeding", "Standard daily 75mg/150mg preventative dose. Consult cardiologist.", "Ecosprin 75, Loprin 75 (Save up to 70%)")
            )
        ),
        InfographicCategory(
            index = 4,
            title = "4. Diabetes Drugs",
            accentColorHex = "#F57C00", // Orange/Amber
            drugs = listOf(
                InfographicDrug(31, "Metformin", "First-line Type 2 Diabetes mellitus", "Stomach cramps, diarrhea, metallic taste", "Take with meals to minimize gastrointestinal discomfort.", "Glycomet, Glimet (Save up to 85%)"),
                InfographicDrug(32, "Glimepiride", "Lowering blood glucose in Type 2", "Hypoglycemia (low blood sugar), weight gain", "Always carry a source of fast sugar (glucose/candy) with you.", "Glimy, Amaryl (Save up to 78%)"),
                InfographicDrug(33, "Gliclazide", "Lowering high blood sugar in diabetic adults", "Low blood sugar, mild nausea, sweating", "Take 30 minutes before breakfast.", "Reclimet, Diamicron (Save up to 74%)"),
                InfographicDrug(34, "Glipizide", "Controlling blood glucose levels", "Hypoglycemia, tremors, sweating", "Short-acting sulfonylurea. Avoid skipping meals.", "Glynase, Glytop (Save up to 80%)"),
                InfographicDrug(35, "Sitagliptin", "Enhancing natural insulin hormones", "Upper respiratory tract infection, headache", "Does not typically cause weight gain or hypoglycemia on its own.", "Januvia, Istavel (Save up to 70%)"),
                InfographicDrug(36, "Vildagliptin", "DPP-4 inhibitor for glycaemic control", "Dizziness, mild tremors, headache", "Often combined with Metformin for enhanced control.", "Galvus, Jalra (Save up to 72%)"),
                InfographicDrug(37, "Empagliflozin", "Excreting excess glucose through urine", "Urinary tract infections (UTI), increased urination", "Drink plenty of water to prevent genital yeast infections.", "Jardiance, Gibtulio (Save up to 68%)"),
                InfographicDrug(38, "Insulin Glargine", "Long-acting basal diabetic sugar control", "Local injection site reactions, low blood sugar", "Administered subcutaneously once daily at the same time.", "Lantus, Basalog (Save up to 65%)"),
                InfographicDrug(39, "Insulin Aspart", "Rapid-acting mealtime sugar control", "Weight gain, sudden low blood sugar, itching", "Inject immediately before or after starting a meal.", "Novorapid, Fiasp (Save up to 64%)"),
                InfographicDrug(40, "Pioglitazone", "Improving cellular insulin sensitivity", "Fluid retention, mild weight gain, risk of bone fracture", "Avoid in patients with active or history of heart failure.", "Pioz, Pioglar (Save up to 77%)")
            )
        ),
        InfographicCategory(
            index = 5,
            title = "5. Gastrointestinal Drugs",
            accentColorHex = "#8E24AA", // Purple
            drugs = listOf(
                InfographicDrug(41, "Omeprazole", "Severe acidity, GERD, gastric reflux", "Headache, mild diarrhea, abdominal gas", "Take 30 minutes before your first meal of the day.", "Omez, Zegerid (Save up to 82%)"),
                InfographicDrug(42, "Pantoprazole", "Stomach acidity, gastric ulcers, gas bloating", "Flatulence, stomach pain, dizziness", "Highly effective PPI. Often prescribed alongside pain relievers.", "Pan, Pantocid (Save up to 80%)"),
                InfographicDrug(43, "Rabeprazole", "Rapid stomach acid neutralization", "Sore throat, constipation, dry mouth", "Acts faster than Omeprazole in relieving severe heartburn.", "Rabeloc, Rabium (Save up to 79%)"),
                InfographicDrug(44, "Esomeprazole", "Healing acid-induced esophageal damage", "Dry mouth, nausea, constipation", "S-isomer of Omeprazole with high systemic bioavailability.", "Nexpro, Sompraz (Save up to 75%)"),
                InfographicDrug(45, "Domperidone", "Nausea, vomiting, stomach fullness", "Dry mouth, breast swelling or tenderness", "Promotes gastric emptying. Often combined with PPIs.", "Domstal, Motinorm (Save up to 83%)"),
                InfographicDrug(46, "Metoclopramide", "Nausea, diabetic gastroparesis, severe reflux", "Drowsiness, restlessness, involuntary movements", "Avoid long-term usage to prevent movement disorders.", "Reglan, Perinorm (Save up to 85%)"),
                InfographicDrug(47, "Ranitidine", "Mild acidity relief, peptic ulcer healing", "Headache, mild constipation or diarrhea", "An H2-receptor blocker. Use with caution in kidney patients.", "Rantac, Aciloc (Save up to 86%)"),
                InfographicDrug(48, "Sucralfate", "Protective gel coating over active ulcers", "Constipation, aluminum retention, gas", "Take on an empty stomach at least 1 hour before meals.", "Sucrafil, Sparacid (Save up to 74%)"),
                InfographicDrug(49, "Loperamide", "Rapid control of acute loose motions/diarrhea", "Constipation, abdominal cramps, sleepiness", "Do not use if diarrhea is accompanied by high fever or blood.", "Imolet, Lomotil (Save up to 82%)"),
                InfographicDrug(50, "Lactulose", "Severe constipation, hepatic encephalopathy", "Flatulence, abdominal bloating, mild diarrhea", "Draws water into the bowel to soften stools.", "Duphalac, Cremaffin (Save up to 72%)")
            )
        ),
        InfographicCategory(
            index = 6,
            title = "6. Respiratory Drugs",
            accentColorHex = "#E91E63", // Rose/Pink
            drugs = listOf(
                InfographicDrug(51, "Salbutamol", "Asthma wheezing, sudden bronchospasm relief", "Shaky hands, rapid heartbeat, throat irritation", "Carry as a rescue inhaler for quick breathing relief.", "Asthalin, Ventolin (Save up to 82%)"),
                InfographicDrug(52, "Levosalbutamol", "Asthma, airway narrowing relief", "Tremors, headache, muscle cramps", "Purified active isomer of Salbutamol with fewer heart side effects.", "Levolin (Save up to 80%)"),
                InfographicDrug(53, "Ipratropium", "COPD chronic breathing relief, severe asthma", "Dry mouth, throat irritation, dry cough", "An anticholinergic bronchodilator. Often nebulized.", "Ipraz, Duolin (Save up to 78%)"),
                InfographicDrug(54, "Budesonide", "Preventative asthma steroid controller", "Oral thrush (fungal infection), hoarse voice", "Rinse mouth with water thoroughly after every inhalation.", "Pulmicort, Budecort (Save up to 75%)"),
                InfographicDrug(55, "Formoterol", "Long-acting airway dilator (bronchocontrol)", "Muscle cramps, headache, nervousness", "Always paired with an inhaled corticosteroid like Budesonide.", "Foracort, Seroflo (Save up to 73%)"),
                InfographicDrug(56, "Montelukast", "Allergic asthma, chronic seasonal allergies", "Sleep disturbances, mood changes, headache", "Take in the evening. Report any sudden behavioral changes.", "Montair, Romilast (Save up to 79%)"),
                InfographicDrug(57, "Theophylline", "Severe chronic asthma, emphysema rescue", "Nausea, rapid pulse, insomnia, tremors", "Requires therapeutic blood level monitoring due to narrow margin.", "Theoped, Unicontin (Save up to 84%)"),
                InfographicDrug(58, "Ambroxol", "Loosening thick cough, chest congestion", "Numbness in mouth, mild nausea, stomach pain", "Drink plenty of warm water to help thin the mucus.", "Mucolite, Ambrodil (Save up to 81%)"),
                InfographicDrug(59, "Dextromethorphan", "Dry hacking cough suppressant", "Drowsiness, dizziness, mild stomach upset", "Do not use for productive coughs with phlegm.", "Lastuss, Grilinctus-DX (Save up to 80%)"),
                InfographicDrug(60, "Cetirizine (Respiratory)", "Running nose, watery eyes, allergic sneezing", "Mild sleepiness, fatigue, dry mouth", "Commonly used for allergic rhinitis.", "Okacet, Cetzine (Save up to 85%)")
            )
        ),
        InfographicCategory(
            index = 7,
            title = "7. CNS & Neurology Drugs",
            accentColorHex = "#5E35B1", // Deep Violet
            drugs = listOf(
                InfographicDrug(61, "Diazepam", "Anxiety, severe muscle spasms, active seizures", "Muscle weakness, sedation, unsteadiness", "Do not drive. Avoid alcohol completely.", "Calmpose, Valium (Save up to 82%)"),
                InfographicDrug(62, "Lorazepam", "Short-term severe panic, sleeping trouble", "Drowsiness, lightheadedness, dependency", "Usually prescribed for short periods to avoid tolerance.", "Ativan, Larpose (Save up to 80%)"),
                InfographicDrug(63, "Alprazolam", "Acute panic attacks, localized anxiety", "Fatigue, dry mouth, coordination issues", "High abuse potential. Taper off gradually under supervision.", "Alprax, Trika (Save up to 85%)"),
                InfographicDrug(64, "Amitriptyline", "Chronic nerve pain, migraine prevention", "Dry mouth, blurred vision, orthostatic dizziness", "A tricyclic antidepressant. Helps with deep nerve pain.", "Amixide, Tryptomer (Save up to 78%)"),
                InfographicDrug(65, "Fluoxetine", "Depression, OCD, eating disorders", "Insomnia, nausea, dry mouth, sexual changes", "May take 2-4 weeks to show significant therapeutic effects.", "Prozac, Fludac (Save up to 76%)"),
                InfographicDrug(66, "Sertraline", "Depression, social anxiety, panic attacks", "Diarrhea, tremors, sweating, insomnia", "Take in the morning to prevent nighttime sleeplessness.", "Sertima, Zoloft (Save up to 74%)"),
                InfographicDrug(67, "Haloperidol", "Severe psychosis, schizophrenia, heavy agitation", "Muscle stiffness, tremors, sleepiness", "Standard typical antipsychotic. Report stiff neck immediately.", "Halonace, Serenace (Save up to 82%)"),
                InfographicDrug(68, "Risperidone", "Schizophrenia, bipolar mania, acute irritability", "Sleepiness, increased appetite, weight gain", "Monitor blood glucose and lipid profile regularly.", "Risdone, Sizodon (Save up to 79%)"),
                InfographicDrug(69, "Levetiracetam", "Epileptic seizures prevention", "Irritability, extreme sleepiness, dizziness", "A widely preferred modern antiepileptic drug.", "Keppra, Levipil (Save up to 72%)"),
                InfographicDrug(70, "Carbidopa/Levodopa", "Parkinson's disease stiffness, tremors", "Involuntary movements (dyskinesia), nausea", "Do not skip doses. Protein-rich meals can decrease absorption.", "Syndopa, Tidomet (Save up to 75%)")
            )
        ),
        InfographicCategory(
            index = 8,
            title = "8. Antihistamines (Allergies)",
            accentColorHex = "#0288D1", // Sky Blue
            drugs = listOf(
                InfographicDrug(71, "Chlorpheniramine", "Allergy runny nose, itching, watery eyes", "Severe sedation, dry mouth, blurred vision", "Often found in night-time cold & flu syrups.", "Cadistin, Piriton (Save up to 85%)"),
                InfographicDrug(72, "Diphenhydramine", "Severe allergic hives, acute motion sickness", "Extreme drowsiness, dry nose & mouth", "Powerful sedative effect. Frequently used as a sleep aid.", "Benadryl, Dramerin (Save up to 80%)"),
                InfographicDrug(73, "Cetirizine", "Daily seasonal allergy, sneezing, hives", "Mild sleepiness, fatigue, dry mouth", "A standard second-generation antihistamine.", "Okacet, Cetzine (Save up to 85%)"),
                InfographicDrug(74, "Levocetirizine", "Potent 24-hour allergy relief", "Mild dry mouth, sleepiness, headache", "More potent than Cetirizine. Take at bedtime.", "Teczine, L-Cetriz (Save up to 81%)"),
                InfographicDrug(75, "Fexofenadine", "Non-drowsy seasonal allergies, itching", "Headache, dizziness, nausea", "Truly non-sedating. Avoid taking with fruit juices.", "Allegra, Fexo (Save up to 70%)"),
                InfographicDrug(76, "Loratadine", "Allergy relief, sneezing, runny nose", "Headache, dry mouth, mild sleepiness", "Long-acting, non-drowsy. Take once daily.", "Claritin, Loratin (Save up to 72%)"),
                InfographicDrug(77, "Desloratadine", "Chronic hives, severe allergic reactions", "Dry mouth, sore throat, muscle pain", "Active metabolite of Loratadine with low sedating profile.", "D-Loratin, Deslor (Save up to 75%)"),
                InfographicDrug(78, "Hydroxyzine", "Itching from eczema, hives, mild anxiety tension", "Drowsiness, dry mouth, coordination changes", "Has dual anti-allergy and mild calming properties.", "Atarax, Hicope (Save up to 76%)"),
                InfographicDrug(79, "Promethazine", "Allergy hives, severe motion nausea", "Deep sedation, dry mouth, dizziness", "Do not give to children under 2 years (respiratory depression).", "Phenergan, Avomine (Save up to 82%)"),
                InfographicDrug(80, "Cyproheptadine", "Allergy itching, clinically safe appetite stimulant", "Increased hunger, weight gain, drowsiness", "Often prescribed to help underweight patients gain weight.", "Practin, Ciplactin (Save up to 84%)")
            )
        ),
        InfographicCategory(
            index = 9,
            title = "9. Hormones & Steroids",
            accentColorHex = "#A0522D", // Sienna/Brown
            drugs = listOf(
                InfographicDrug(81, "Prednisolone", "Severe joint inflammation, acute allergy flares", "High blood sugar, increased appetite, weight gain", "Do not stop suddenly if taken for more than 2 weeks.", "Wysolone, Predone (Save up to 85%)"),
                InfographicDrug(82, "Dexamethasone", "Acute swelling, severe Covid respiratory distress", "Insomnia, mood swings, elevated blood pressure", "A highly potent corticosteroid. Use strictly under expert guidance.", "Decdan, Dexona (Save up to 86%)"),
                InfographicDrug(83, "Hydrocortisone", "Adrenal gland insufficiency, severe eczema", "Thin skin (with cream), slow wound healing", "Used topically for skin inflammation or systemically for hormone deficits.", "Primacort, Hcort (Save up to 80%)"),
                InfographicDrug(84, "Levothyroxine", "Underactive thyroid gland (hypothyroidism)", "Fast heartbeat, weight loss, heat intolerance", "Take first thing in the morning on an empty stomach.", "Thyronorm, Eltroxin (Save up to 75%)"),
                InfographicDrug(85, "Methimazole", "Overactive thyroid gland (hyperthyroidism)", "Joint pain, fever, reduction in white blood cells", "Regular thyroid panels are required to adjust dosing.", "Methimez (Save up to 72%)"),
                InfographicDrug(86, "Insulin", "Type 1 diabetes, insulin-deficient Type 2", "Hypoglycemia, weight gain, injection site lipo-changes", "Store unused insulin in a refrigerator (do not freeze).", "Huminsulin, Mixtard (Save up to 68%)"),
                InfographicDrug(87, "Norethisterone", "Delaying menstrual cycles, painful endometriosis", "Breast tenderness, headache, irregular spotting", "Synthetic progesterone. Take as directed to regulate cycle.", "Primolut-N, Regestrone (Save up to 78%)"),
                InfographicDrug(88, "Medroxyprogesterone", "Long-acting contraception, abnormal uterine bleeding", "Weight gain, mood changes, bone density reduction", "Given as 3-monthly depot injections.", "Depo-Provera, Deviry (Save up to 70%)"),
                InfographicDrug(89, "Estrogen", "Menopause hot flashes, vaginal dryness", "Abdominal bloating, headache, breast pain", "Used in hormone replacement therapy (HRT).", "Premarin, Estrogel (Save up to 72%)"),
                InfographicDrug(90, "Progesterone", "Supporting early pregnancy, absent menstrual cycles", "Fatigue, breast tenderness, abdominal bloating", "Often prescribed as vaginal gel or oral capsules.", "Susten, Naturogest (Save up to 66%)")
            )
        ),
        InfographicCategory(
            index = 10,
            title = "10. Vitamins & Supplements",
            accentColorHex = "#D4AF37", // Gold/Amber
            drugs = listOf(
                InfographicDrug(91, "Vitamin D3", "Improving bone mineral density & immunity", "High calcium levels (only in extreme overdose)", "Usually taken once a week for bone health.", "Calcirol, Uprise-D3 (Save up to 75%)"),
                InfographicDrug(92, "Vitamin B12", "Nerve conduction, healthy red blood cells", "Mild skin rash, temporary diarrhea", "Crucial supplement for pure vegetarians.", "Nurokind, Methylcobal (Save up to 78%)"),
                InfographicDrug(93, "Folic Acid", "Neural tube defect prevention in pregnancy", "Abdominal bloating, loss of appetite", "Recommended for all women planning pregnancy.", "Folvite, Foliculin (Save up to 80%)"),
                InfographicDrug(94, "Ferrous Sulfate", "Treating iron-deficiency anemia", "Black stools, constipation, stomach pain", "Take with Vitamin C to increase absorption. May darken stools.", "Autrin, Dexorange (Save up to 74%)"),
                InfographicDrug(95, "Calcium", "Severe bone, joint & muscle strength support", "Constipation, mild gas, kidney stones (rare)", "Take with food for better calcium carbonate absorption.", "Shelcal, Ostocalcium (Save up to 76%)"),
                InfographicDrug(96, "Magnesium", "Muscle relaxation, healthy sleep, stress relief", "Loose watery stools, stomach cramps", "Avoid taking high doses if you have kidney disease.", "Mag-Al, Mag-Plus (Save up to 72%)"),
                InfographicDrug(97, "Zinc", "Immune defense booster, severe wound healing", "Metallic taste, nausea when taken empty stomach", "Frequently paired with Vitamin C during common colds.", "Zincovit, Zinconia (Save up to 78%)"),
                InfographicDrug(98, "Vitamin C", "Scurvy protection, skin health, iron absorber", "Mild stomach upset, gas (in extremely high doses)", "An essential water-soluble antioxidant.", "Celin, Limcee (Save up to 82%)"),
                InfographicDrug(99, "Multivitamin", "General daily dietary gap coverage", "Mild nausea, yellow colored urine (due to B2)", "Best taken after a main meal.", "Becosules, Revital (Save up to 80%)"),
                InfographicDrug(100, "Omega-3", "Heart health, brain wellness, eye support", "Fishy aftertaste, acid reflux, mild gas", "Obtained from fish oil or flaxseed oil.", "Seacod, Maxepa (Save up to 74%)")
            )
        )
    )
}
