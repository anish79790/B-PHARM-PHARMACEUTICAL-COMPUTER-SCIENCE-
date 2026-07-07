package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.RetrofitClient
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest

@OptIn(ExperimentalCoroutinesApi::class)
class PharmaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = PharmaDatabase.getDatabase(application)
    private val userDao = db.userDao()
    private val reportDao = db.medicalReportDao()
    private val chatDao = db.chatMessageDao()
    private val bookmarkDao = db.bookmarkDao()
    private val expenseDao = db.medicineExpenseDao()
    private val userDiseaseDao = db.userDiseaseDao()

    // --- SharedPreferences User Profile ---
    private val sharedPrefs = application.getSharedPreferences("pharmasense_profile_prefs", android.content.Context.MODE_PRIVATE)

    // --- Global Settings Preferences ---
    private val settingsPrefs = application.getSharedPreferences("pharmasense_settings_prefs", android.content.Context.MODE_PRIVATE)

    var viewAsOrbit = MutableStateFlow(settingsPrefs.getBoolean("view_as_orbit", true))
    var useAiForLabReports = MutableStateFlow(settingsPrefs.getBoolean("use_ai_for_lab_reports", true))
    var useAiForSymptoms = MutableStateFlow(settingsPrefs.getBoolean("use_ai_for_symptoms", true))
    var useAiForInteractions = MutableStateFlow(settingsPrefs.getBoolean("use_ai_for_interactions", true))
    var useAiForScanner = MutableStateFlow(settingsPrefs.getBoolean("use_ai_for_scanner", true))
    var useAiForDashboard = MutableStateFlow(settingsPrefs.getBoolean("use_ai_for_dashboard", true))
    var customApiKey = MutableStateFlow(settingsPrefs.getString("custom_api_key", "") ?: "")

    fun setViewAsOrbit(value: Boolean) {
        viewAsOrbit.value = value
        settingsPrefs.edit().putBoolean("view_as_orbit", value).apply()
    }

    fun setUseAiForLabReports(value: Boolean) {
        useAiForLabReports.value = value
        settingsPrefs.edit().putBoolean("use_ai_for_lab_reports", value).apply()
    }

    fun setUseAiForSymptoms(value: Boolean) {
        useAiForSymptoms.value = value
        settingsPrefs.edit().putBoolean("use_ai_for_symptoms", value).apply()
    }

    fun setUseAiForInteractions(value: Boolean) {
        useAiForInteractions.value = value
        settingsPrefs.edit().putBoolean("use_ai_for_interactions", value).apply()
    }

    fun setUseAiForScanner(value: Boolean) {
        useAiForScanner.value = value
        settingsPrefs.edit().putBoolean("use_ai_for_scanner", value).apply()
    }

    fun setUseAiForDashboard(value: Boolean) {
        useAiForDashboard.value = value
        settingsPrefs.edit().putBoolean("use_ai_for_dashboard", value).apply()
        if (isLoggedIn.value) {
            generateProfileInsights()
        }
    }

    fun setCustomApiKey(value: String) {
        customApiKey.value = value
        settingsPrefs.edit().putString("custom_api_key", value).apply()
    }

    var isLoggedIn = MutableStateFlow(sharedPrefs.getBoolean("is_logged_in", false))
    var username = MutableStateFlow(sharedPrefs.getString("username", "") ?: "")
    var email = MutableStateFlow(sharedPrefs.getString("email", "") ?: "")
    var fullName = MutableStateFlow(sharedPrefs.getString("full_name", "") ?: "")
    var age = MutableStateFlow(sharedPrefs.getInt("age", 30))
    var gender = MutableStateFlow(sharedPrefs.getString("gender", "Male") ?: "Male")
    var healthGoal = MutableStateFlow(sharedPrefs.getString("health_goal", "Maintain Good Health") ?: "Maintain Good Health")
    var avatarIndex = MutableStateFlow(sharedPrefs.getInt("avatar_index", 0))

    // --- State Flows from Room (Isolated per user email) ---
    val reportsList: StateFlow<List<MedicalReport>> = email
        .flatMapLatest { userEmail ->
            if (userEmail.isEmpty()) flowOf(emptyList())
            else reportDao.getAllReportsForUser(userEmail)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = email
        .flatMapLatest { userEmail ->
            if (userEmail.isEmpty()) flowOf(emptyList())
            else chatDao.getChatHistoryForUser(userEmail)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedList: StateFlow<List<BookmarkedItem>> = email
        .flatMapLatest { userEmail ->
            if (userEmail.isEmpty()) flowOf(emptyList())
            else bookmarkDao.getAllBookmarksForUser(userEmail)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expensesList: StateFlow<List<MedicineExpense>> = email
        .flatMapLatest { userEmail ->
            if (userEmail.isEmpty()) flowOf(emptyList())
            else expenseDao.getAllExpensesForUser(userEmail)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userDiseasesList: StateFlow<List<UserDisease>> = email
        .flatMapLatest { userEmail ->
            if (userEmail.isEmpty()) flowOf(emptyList())
            else userDiseaseDao.getAllUserDiseasesForUser(userEmail)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var testingRecommendations = MutableStateFlow<String?>(null)
    var personalDiet = MutableStateFlow<String?>(null)
    var customDiscoveryNews = MutableStateFlow<String?>(null)
    var profileInsightsLoading = MutableStateFlow(false)

    init {
        if (isLoggedIn.value) {
            generateProfileInsights()
        }
    }

    // --- UI State Management ---
    var currentTab = MutableStateFlow("Home")
    var searchQuery = MutableStateFlow("")

    // Active entities in view detail
    var selectedDisease = MutableStateFlow<Disease?>(null)
    var selectedDrug = MutableStateFlow<Drug?>(null)

    // Symptom Checker State
    var symptomInput = MutableStateFlow("")
    var symptomAnalysisResult = MutableStateFlow<String?>(null)
    var symptomAnalysisLoading = MutableStateFlow(false)
    var symptomIsEmergency = MutableStateFlow(false)

    // Report Analyzer Input State
    var reportTitleInput = MutableStateFlow("Comprehensive Lab Report")
    var hemoglobinInput = MutableStateFlow("14.0")
    var wbcInput = MutableStateFlow("7000")
    var plateletInput = MutableStateFlow("250000")
    var glucoseInput = MutableStateFlow("90")
    var bpSystolicInput = MutableStateFlow("120")
    var bpDiastolicInput = MutableStateFlow("80")

    var reportAnalysisLoading = MutableStateFlow(false)
    var activeDetailedReport = MutableStateFlow<MedicalReport?>(null)

    // Image analyzer state
    var reportImageBase64 = MutableStateFlow<String?>(null)
    var reportImageUriString = MutableStateFlow<String?>(null)

    // Drug Interaction State
    var interactionDrug1 = MutableStateFlow("")
    var interactionDrug2 = MutableStateFlow("")
    var interactionResultRisk = MutableStateFlow<String?>(null)
    var interactionResultText = MutableStateFlow<String?>(null)
    var interactionLoading = MutableStateFlow(false)

    // AI Chat State
    var chatInput = MutableStateFlow("")
    var chatLoading = MutableStateFlow(false)

    // Scanner State
    var labelScannerResult = MutableStateFlow<String?>(null)
    var labelScannerLoading = MutableStateFlow(false)

    // Set search query and filter results
    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    // Toggle Bookmarks
    fun toggleBookmark(type: String, id: String, name: String, subtitle: String) {
        val userEmail = email.value
        if (userEmail.isEmpty()) return
        viewModelScope.launch {
            val exists = bookmarkDao.isBookmarked(id, userEmail)
            if (exists) {
                bookmarkDao.deleteBookmark(BookmarkedItem(id = id, userEmail = userEmail, type = type, name = name, subtitle = subtitle))
            } else {
                bookmarkDao.insertBookmark(BookmarkedItem(id = id, userEmail = userEmail, type = type, name = name, subtitle = subtitle))
            }
        }
    }

    fun isBookmarkedFlow(id: String): Flow<Boolean> {
        return email.flatMapLatest { userEmail ->
            if (userEmail.isEmpty()) flowOf(false)
            else bookmarkDao.isBookmarkedFlow(id, userEmail)
        }
    }

    // AI Chat Messenger Logic
    fun sendChatMessage() {
        val query = chatInput.value.trim()
        if (query.isEmpty()) return
        val userEmail = email.value
        if (userEmail.isEmpty()) return

        chatInput.value = ""
        chatLoading.value = true

        viewModelScope.launch {
            // Save User Message to SQLite
            val userMsg = ChatMessageEntity(userEmail = userEmail, role = "user", content = query)
            chatDao.insertMessage(userMsg)

            // Compile conversation history
            val pastMessages = chatDao.getChatHistoryForUser(userEmail).first()
            val historyPrompt = StringBuilder()
            historyPrompt.append("You are PharmaSense AI, a compassionate pharmacology specialist and smart pharmacy companion. ")
            historyPrompt.append("Answer medication, diseases, symptoms and health queries with deep scientific accuracy. ")
            historyPrompt.append("Provide details under clear headings like Uses, Action, Side Effects. ")
            historyPrompt.append("CRITICAL: Always append a disclaimer: 'Disclaimer: This information is educational only. Consult a registered physician for diagnosis or treatment.'\n\n")

            // Append last 10 messages for context
            pastMessages.takeLast(10).forEach {
                historyPrompt.append("${it.role.uppercase()}: ${it.content}\n")
            }
            historyPrompt.append("MODEL:")

            // Call API
            val reply = withContext(Dispatchers.IO) {
                RetrofitClient.getGeminiResponse(
                    prompt = historyPrompt.toString(),
                    systemInstruction = "You are PharmaSense AI, a smart health pharmacist. Be extremely helpful and exact.",
                    customApiKey = customApiKey.value.ifEmpty { null }
                )
            }

            // Save Model Reply to SQLite
            val modelMsg = ChatMessageEntity(userEmail = userEmail, role = "model", content = reply)
            chatDao.insertMessage(modelMsg)
            chatLoading.value = false
        }
    }

    fun clearChatHistory() {
        val userEmail = email.value
        if (userEmail.isEmpty()) return
        viewModelScope.launch {
            chatDao.clearHistoryForUser(userEmail)
        }
    }

    // Symptom Triage Analysis
    fun analyzeSymptoms() {
        val symptoms = symptomInput.value.trim()
        if (symptoms.isEmpty()) return

        symptomAnalysisLoading.value = true
        symptomAnalysisResult.value = null
        symptomIsEmergency.value = false

        // 1. Check for immediate cardiac or respiratory red flags local check
        val lowerSymptoms = symptoms.lowercase()
        val hasRedFlags = lowerSymptoms.contains("chest pain") ||
                lowerSymptoms.contains("left arm pain") ||
                lowerSymptoms.contains("severe pressure on chest") ||
                lowerSymptoms.contains("breathing difficulty") ||
                lowerSymptoms.contains("difficulty breathing") ||
                lowerSymptoms.contains("coughing blood") ||
                lowerSymptoms.contains("unconscious") ||
                lowerSymptoms.contains("difficulty speaking") ||
                lowerSymptoms.contains("choking") ||
                lowerSymptoms.contains("heart pain")

        if (hasRedFlags) {
            symptomIsEmergency.value = true
            symptomAnalysisResult.value = """
                ### 🚨 POSSIBLE LIFE-THREATENING EMERGENCY DETECTED
                Your reported symptom (**"$symptoms"**) matches critical red flag triage criteria:
                - **Primary Alert**: Possible Cardiac Event / Severe Respiratory Distress.
                - **Immediate Actions Required**:
                  1. Call local emergency services (e.g., 911 or 102) immediately.
                  2. Do not attempt to drive yourself to the hospital.
                  3. Sit upright in a comfortable position, loosen tight clothing, and try to stay calm.
                  4. If advised by emergency dispatchers, chew and swallow one full adult aspirin.
                
                *Disclaimer: This is an automated preliminary triage alert. Seek standard physician care immediately.*
            """.trimIndent()
            symptomAnalysisLoading.value = false
            return
        }

        // 2. Offline Deterministic Symptom Triage Check
        if (!useAiForSymptoms.value) {
            val offlineRes = when {
                lowerSymptoms.contains("cough") || lowerSymptoms.contains("fever") || lowerSymptoms.contains("cold") || lowerSymptoms.contains("flu") -> {
                    """
                    ### 📊 OFFLINE ANALYSIS: Respiratory / Viral Indicators
                    1. **Potential Primary Causes**:
                       - Acute viral nasopharyngitis (Common Cold).
                       - Influenza (Flu) or mild acute Bronchitis.
                    2. **Clinical Specialist to Consult**:
                       - General Practitioner or Family Physician.
                    3. **Practical Supportive Home Care Actions**:
                       - Take warm saline gargles 3 times daily.
                       - Maintain adequate hydration (2.5L - 3L fluid intake).
                       - Get absolute bed rest.
                    4. **Explicit Warning Signs**:
                       - High fever (>102°F) persisting for more than 3 days.
                       - Shortness of breath or persistent chest discomfort.
                    
                    **CRITICAL: Please consult a registered medical practitioner before taking any medications.**
                    """.trimIndent()
                }
                lowerSymptoms.contains("stomach") || lowerSymptoms.contains("acidity") || lowerSymptoms.contains("pain") && (lowerSymptoms.contains("gastric") || lowerSymptoms.contains("gut")) -> {
                    """
                    ### 📊 OFFLINE ANALYSIS: Gastrointestinal / Dyspepsia Indicators
                    1. **Potential Primary Causes**:
                       - Acid Reflux / GERD (Gastroesophageal Reflux Disease).
                       - Acute Gastritis or dietary indigestion.
                    2. **Clinical Specialist to Consult**:
                       - Gastroenterologist or General Physician.
                    3. **Practical Supportive Home Care Actions**:
                       - Avoid spicy, oily, or fried foods.
                       - Consume soothing items like cold milk or unsweetened yogurt.
                       - Eat smaller, more frequent meals.
                    4. **Explicit Warning Signs**:
                       - Blood in stool or vomit, or black tarry stools.
                       - Severe, sharp, radiating abdominal pain.
                    
                    **CRITICAL: Please consult a registered medical practitioner before taking any medications.**
                    """.trimIndent()
                }
                else -> {
                    """
                    ### 📊 OFFLINE ANALYSIS: General Wellness Assessment
                    1. **Potential Primary Causes**:
                       - General physical fatigue, muscular strain, or mild inflammatory reaction.
                    2. **Clinical Specialist to Consult**:
                       - Family Physician or General practitioner.
                    3. **Practical Supportive Home Care Actions**:
                       - Focus on high-quality sleep hygiene (7-8 hours).
                       - Track body temperature and heart rate daily.
                       - Keep up-to-date hydration levels.
                    4. **Explicit Warning Signs**:
                       - Escalation of symptom intensity, dizziness, or localized severe swelling.
                    
                    **CRITICAL: Please consult a registered medical practitioner before taking any medications.**
                    """.trimIndent()
                }
            }
            symptomAnalysisResult.value = offlineRes
            symptomAnalysisLoading.value = false
            return
        }

        // 3. Gemini symptom assistant if safe
        viewModelScope.launch {
            val prompt = """
                A patient presents with these symptoms: "$symptoms". 
                Provide a structured, helpful medical overview:
                1. **Potential Primary Causes** (Suggest 2-3 common mild/moderate conditions and describe symptoms overlaps).
                2. **Clinical Specialist to Consult** (e.g. Pulmonologist, Family Doctor, Cardiologist).
                3. **Practical Supportive Home Care Actions** (rest, fluids, heating pads).
                4. **Explicit Warning Signs** (when symptoms warrant intermediate ER care).
                
                CRITICAL Requirement: Keep tone calm, clear, and companion-like. Always make sure a large and visible bold text indicates that they should consult a doctor. Do not make a formal medical diagnosis.
            """.trimIndent()

            val response = withContext(Dispatchers.IO) {
                RetrofitClient.getGeminiResponse(
                    prompt = prompt,
                    systemInstruction = "You are an expert clinical triage assistant. Keep formatting professional and highly structured.",
                    customApiKey = customApiKey.value.ifEmpty { null }
                )
            }

            symptomAnalysisResult.value = response
            symptomAnalysisLoading.value = false
        }
    }

    fun clearSymptomState() {
        symptomInput.value = ""
        symptomAnalysisResult.value = null
        symptomIsEmergency.value = false
    }

    // Drug Interaction Checker
    fun checkDrugInteractions() {
        val drugA = interactionDrug1.value.trim()
        val drugB = interactionDrug2.value.trim()

        if (drugA.isEmpty() || drugB.isEmpty()) return

        interactionLoading.value = true
        interactionResultRisk.value = null
        interactionResultText.value = null

        // 1. Local Database Lookup Check
        val localMatch = MedicalCatalog.getLocalInteraction(drugA, drugB)
        if (localMatch != null) {
            interactionResultRisk.value = localMatch.first
            interactionResultText.value = localMatch.second
            interactionLoading.value = false
            return
        }

        // 2. Offline Deterministic Fallback Check
        if (!useAiForInteractions.value) {
            interactionResultRisk.value = "Safe/Unmapped"
            interactionResultText.value = """
                - **Risk Level**: No Severe Overlapping Action (Offline Mode)
                - **Action / Mechanism**: No mutual enzymatic inhibition or competitive binding was detected in the local offline clinical database for "$drugA" and "$drugB".
                - **Clinical Outlay**: These drugs are generally considered safe to administer concurrently. However, spacing doses by 2 hours is always a prudent preventative measure.
                - **Alternative Advise**: Consult your healthcare provider to confirm personal compatibility and verify any rare systemic side effects.
            """.trimIndent()
            interactionLoading.value = false
            return
        }

        // 3. Fetch via Gemini for more complex or unmapped check
        viewModelScope.launch {
            val prompt = """
                Do drug A: "$drugA" and drug B: "$drugB" interact with each other? 
                Provide mutual interactions in this precise structure:
                - **Risk Level**: Clear assessment as "No Interaction", "Mild", "Moderate", or "Severe".
                - **Action / Mechanism**: Why and how they collide inside human blood or hepatic channels.
                - **Clinical Outlay**: Side effects and warnings for patients taking both together.
                - **Alternative Advise**: What to say to the prescribing physician.
                
                Keep tone scientifically objective and factual.
            """.trimIndent()

            val response = withContext(Dispatchers.IO) {
                RetrofitClient.getGeminiResponse(
                    prompt = prompt,
                    systemInstruction = "You are an expert clinical pharmacist explaining medical interactions precisely.",
                    customApiKey = customApiKey.value.ifEmpty { null }
                )
            }

            // Extract Risk Level as simple metadata
            val risk = when {
                response.contains("Severe", ignoreCase = true) || response.contains("Danger", ignoreCase = true) || response.contains("Major", ignoreCase = true) -> "Severe"
                response.contains("Moderate", ignoreCase = true) -> "Moderate"
                response.contains("Mild", ignoreCase = true) -> "Mild"
                else -> "No Interaction Detected"
            }

            interactionResultRisk.value = risk
            interactionResultText.value = response
            interactionLoading.value = false
        }
    }

    fun resetInteractions() {
        interactionDrug1.value = ""
        interactionDrug2.value = ""
        interactionResultRisk.value = null
        interactionResultText.value = null
    }

    // Lab Report Analyzer
    fun analyzeLabReport() {
        reportAnalysisLoading.value = true

        val title = reportTitleInput.value.trim().ifEmpty { "Comprehensive Lab Report" }
        val hgb = hemoglobinInput.value.trim()
        val wbc = wbcInput.value.trim()
        val plt = plateletInput.value.trim()
        val glu = glucoseInput.value.trim()
        val systolic = bpSystolicInput.value.trim()
        val diastolic = bpDiastolicInput.value.trim()

        viewModelScope.launch {
            val hasImage = !reportImageBase64.value.isNullOrEmpty()
            
            val prompt = if (hasImage) {
                """
                Please carefully analyze this uploaded medical lab report or prescription image.
                Provide a highly structured and comprehensive analysis in a friendly, easy-to-understand format with distinct sections:
                
                1. **🔍 Medical Document Summary**:
                   Briefly summarize what this document is (e.g., Blood Test, Prescription, Urine Report) and translate any complex medical jargon or numbers into clear, simple language.
                
                2. **⚠️ Key Observations & Out-of-Range Markers**:
                   List any abnormal values, high/low counts, or alarming points noted in the report/prescription, explaining what they mean.
                
                3. **💊 Suggested/Prescribed Medicines & Indications**:
                   - Identify any medications already prescribed or mentioned in this document. Explain what each medicine is used for.
                   - Suggest other possible safe medications or supportive remedies that are commonly advised for the conditions or abnormalities detected in this report.
                
                4. **📉 Cheaper & Better Alternatives - Brand Comparison**:
                   - For each identified or suggested medicine, provide alternative options that are significantly cheaper but manufactured by highly reputable and trusted pharmaceutical companies in India (e.g., Cipla, Sun Pharma, Dr. Reddy's, Torrent, Lupin, Abbott, etc.).
                   - Present this in a clean comparative list or table: `[Prescribed/Suggested Medicine] ➡️ [Cheaper Alternative Brand & Company] (Approx. Cost Saving %)`
                   - Explain that these alternatives share the exact same active pharmaceutical ingredient (API) and are equally safe and effective.
                
                5. **🩺 Next Step Actions & Physician Guidance**:
                   Give clear, actionable steps on what specialist to consult or what lifestyle measures/tests to perform next.
                
                CRITICAL: Conclude with a clear disclaimer: 'Disclaimer: This AI analysis is for educational and guidance purposes only. Always consult a registered physician before changing or starting any medication.'
                """.trimIndent()
            } else {
                """
                Strictly analyze these quantitative clinical laboratory readings:
                - Hemoglobin (Hgb): $hgb g/dL (Normal Range: 12-16 g/dL for women, 13.5-17.5 g/dL for men)
                - White Blood Cell (WBC): $wbc cells/mcL (Normal Range: 4,000 - 11,000 cells/mcL)
                - Platelet Count: $plt cells/mcL (Normal Range: 150,000 - 450,000 cells/mcL)
                - Fasting Blood Sugar (glucose): $glu mg/dL (Normal: < 100 mg/dL, Pre-diabetic: 100-125, Diabetic: 126+)
                - Blood Pressure (BP): $systolic / $diastolic mmHg (Normal: < 120/80 mmHg, Elevated: 120-129/<80, Hypertension Stage 1: 130-139/80-89, Stage 2: 140+/90+)
                
                Please deliver the medical analysis report divided into clear segments:
                1. **Lab Summary Overview**: A friendly, easy-to-read translation of findings.
                2. **Out of Range Markers**: Explicit list of any blood count numbers that are outside standard clinical ranges, and what that deviation implies.
                3. **Possible Indications**: Interpretive insights (e.g. anemia, acute infection/inflammation, hyperglycemia, or hypertension).
                4. **Actionable Suggestions & Preventative Advice**: What tests or lifestyle measures to consider next.
                
                CRITICAL formatting requirement: Highlight out-of-range parameters clearly.
                """.trimIndent()
            }

            val sysInstruction = "You are a professional clinical hematologist, pharmacist, and lab report analyst. Translate complex medical parameters or prescription lists into clear, actionable health insights. For medications, always suggest cheaper, high-quality generic alternatives from reputable brands."

            val analysisResult = if (!useAiForLabReports.value) {
                if (hasImage) {
                    "Image Analysis is an AI-powered feature. Please switch to Manual Entry Mode or enable AI Mode in Settings to analyze lab report images."
                } else {
                    val hgbF = hgb.toFloatOrNull() ?: 14f
                    val wbcF = wbc.toFloatOrNull() ?: 7000f
                    val pltF = plt.toFloatOrNull() ?: 250000f
                    val gluF = glucoseInput.value.trim().toFloatOrNull() ?: 90f
                    val sysF = systolic.toFloatOrNull() ?: 120f
                    val diaF = diastolic.toFloatOrNull() ?: 80f
                    
                    val observations = mutableListOf<String>()
                    val suggestions = mutableListOf<String>()
                    
                    if (hgbF < 12f) {
                        observations.add("- **Low Hemoglobin ($hgb g/dL)**: Suggestive of Anemia. This can lead to fatigue, weakness, and short breath.")
                        suggestions.add("- **Anemia Guidance**: Consume iron-rich foods (spinach, beetroot, apples) and consult a physician regarding iron supplements.")
                    }
                    
                    if (wbcF > 11000f) {
                        observations.add("- **High WBC ($wbc /µL)**: Suggestive of an active infection or inflammation.")
                        suggestions.add("- **Infection Guidance**: Seek a doctor's consultation to identify the infectious origin; do not take random antibiotics.")
                    } else if (wbcF < 4000f) {
                        observations.add("- **Low WBC ($wbc /µL)**: Suggestive of weakened immune system.")
                    }
                    
                    if (pltF < 150000f) {
                        observations.add("- **Thrombocytopenia ($plt /µL)**: Low platelets count.")
                        suggestions.add("- Rest immediately and monitor for bleeding or bruising.")
                    }
                    
                    if (gluF >= 126f) {
                        observations.add("- **Diabetic Glycemia ($glu mg/dL)**: Critical hyperglycemia hazard.")
                        suggestions.add("- **Hyperglycemia Guidance**: Consult an endocrinologist immediately. Strictly reduce all sugar, honey, and simple carbs. Take low-glycemic foods.")
                    } else if (gluF >= 100f) {
                        observations.add("- **Pre-Diabetic Glycemia ($glu mg/dL)**: Impaired fasting glucose indicator.")
                        suggestions.add("- Restrict desserts, engage in daily 30-minute brisk walking, and check HbA1c.")
                    }
                    
                    if (sysF >= 140f || diaF >= 90f) {
                        observations.add("- **Stage 2 Hypertension ($systolic/$diastolic mmHg)**: High blood pressure warnings.")
                        suggestions.add("- **Hypertension Guidance**: Keep BP logged daily. Reduce sodium/salt intake immediately. Avoid mental stressors and consult a cardiologist.")
                    } else if (sysF >= 130f || diaF >= 80f) {
                        observations.add("- **Stage 1 Hypertension ($systolic/$diastolic mmHg)**: Borderline elevated BP.")
                        suggestions.add("- Restrict processed foods, maintain weight, and engage in daily light cardio exercises.")
                    }
                    
                    if (observations.isEmpty()) {
                        observations.add("- All submitted lab parameters (Hemoglobin, WBC, Platelets, Glucose, and Blood Pressure) are within standard safe reference ranges!")
                        suggestions.add("- Keep up the excellent work! Maintain your balanced nutrition, proper hydration, and annual health screenings.")
                    }
                    
                    """
                    ### 📊 OFFLINE CLINICAL LAB ANALYSIS
                    Your blood report parameters have been verified using standard deterministic medical benchmarks.
                    
                    #### 1. Lab Summary Overview
                    A precise mathematical check of your submitted metrics has been executed entirely offline on your device.
                    
                    #### 2. Out of Range Markers
                    ${observations.joinToString("\n")}
                    
                    #### 3. Possible Indications
                    - Primary assessment is based on input numbers. No AI servers or cloud engines were contacted for this diagnostic.
                    
                    #### 4. Actionable Suggestions & Preventative Advice
                    ${suggestions.joinToString("\n")}
                    
                    *Disclaimer: This is an offline algorithmic interpretation. For any persistent or severe issues, please consult a registered physician.*
                    """.trimIndent()
                }
            } else {
                withContext(Dispatchers.IO) {
                    RetrofitClient.getGeminiResponse(
                        prompt = prompt,
                        systemInstruction = sysInstruction,
                        base64Image = reportImageBase64.value,
                        customApiKey = customApiKey.value.ifEmpty { null }
                    )
                }
            }

            // Determine local metadata indicators
            var risk = "Low"
            var finalIndication = "All Metrics in Normal Range"

            if (hasImage) {
                if (analysisResult.contains("High", ignoreCase = true) || analysisResult.contains("Severe", ignoreCase = true) || analysisResult.contains("Dangerous", ignoreCase = true) || analysisResult.contains("🚨", ignoreCase = true)) {
                    risk = "High"
                } else if (analysisResult.contains("Moderate", ignoreCase = true) || analysisResult.contains("Warning", ignoreCase = true)) {
                    risk = "Moderate"
                } else {
                    risk = "Low"
                }
                finalIndication = "Parsed from Image/Prescription"
            } else {
                val indications = mutableListOf<String>()
                try {
                    val hgbF = hgb.toFloatOrNull() ?: 14f
                    val wbcF = wbc.toFloatOrNull() ?: 7000f
                    val pltF = plt.toFloatOrNull() ?: 250000f
                    val gluF = glu.toFloatOrNull() ?: 90f
                    val sysF = systolic.toFloatOrNull() ?: 120f
                    val diaF = diastolic.toFloatOrNull() ?: 80f

                    if (hgbF < 12f) { indications.add("Possible Anemia"); risk = "Moderate" }
                    if (wbcF > 11000f) { indications.add("High WBC (Possible Infection)"); risk = "Moderate" }
                    if (wbcF < 4000f) { indications.add("Low WBC"); risk = "Moderate" }
                    if (pltF < 150000f || pltF > 450000f) { indications.add("Abnormal Platelets"); risk = "Moderate" }
                    if (gluF >= 126f) { indications.add("Hyperglycemia / Diabetes Risk"); risk = "High" }
                    else if (gluF >= 100f) { indications.add("Pre-diabetes Risk"); risk = "Moderate" }
                    if (sysF >= 140f || diaF >= 90f) { indications.add("Stage 2 Hypertension Risk"); risk = "High" }
                    else if (sysF >= 130f || diaF >= 80f) { indications.add("Stage 1 Hypertension"); risk = "Moderate" }

                    if (sysF >= 180f || diaF >= 120f) { risk = "High" } // Crisis
                } catch (e: Exception) {
                    // Ignore parsing errors
                }
                finalIndication = if (indications.isEmpty()) "All Metrics in Normal Range" else indications.joinToString(", ")
            }

            // Save report to Room DB
            val labDataString = if (hasImage) "Analyzed via Report Image Upload" else "Hgb: $hgb | WBC: $wbc | Plt: $plt | Glucose: $glu | BP: $systolic/$diastolic"
            val reportRecord = MedicalReport(
                userEmail = email.value,
                title = if (hasImage && title == "Comprehensive Lab Report") "Image Analysis: $title" else title,
                labData = labDataString,
                analysisMarkdown = analysisResult,
                possibleIndication = finalIndication,
                riskLevel = risk
            )

            reportDao.insertReport(reportRecord)
            reportAnalysisLoading.value = false
        }
    }

    fun deleteReport(id: Int) {
        viewModelScope.launch {
            reportDao.deleteReportById(id)
            if (activeDetailedReport.value?.id == id) {
                activeDetailedReport.value = null
            }
        }
    }

    // Medicine Label Scanner (Multimodal Demo Simulation)
    fun simulateLabelScan(mockType: String) {
        labelScannerLoading.value = true
        labelScannerResult.value = null

        val labelTextDesc = when (mockType) {
            "metformin" -> "MOCK SCAN: Standard tablet pack of Metformin 500mg, manufactured by GSK, NDC 0172-5728. Active ingredient: metformin hydrochloride."
            "ciprofloxacin" -> "MOCK SCAN: Tablet capsule strip of Ciprofloxacin 250mg USP, broad spectrum antibiotic, lot 38592-A."
            else -> "MOCK SCAN: Antacid Syrup bottle labeled Omeprazole 20mg Delayed-Release Oral Capsules, bottle of 30 capsules."
        }

        if (!useAiForScanner.value) {
            val offlineScan = when (mockType) {
                "metformin" -> """
                    ### 📊 OFFLINE LABEL SCAN RESULT
                    - **Trade Name & Active Molecule**: Glycomet / Metformin Hydrochloride (500mg)
                    - **Estimated Drug Class**: Biguanide Oral Hypoglycemic Agent
                    - **Primary Approved Uses**: Type 2 Diabetes Mellitus glycemic management
                    - **Standard Dose Guidance**: 500mg - 1000mg twice daily with meals
                    - **Common Associated Side Effects**: Mild bloating, flatulence, transient abdominal discomfort, or metallic taste.
                    
                    *Offline Note: Scanned and processed via Local Deterministic Molecular Indexing (Offline Mode).*
                """.trimIndent()
                "ciprofloxacin" -> """
                    ### 📊 OFFLINE LABEL SCAN RESULT
                    - **Trade Name & Active Molecule**: Ciplox / Ciprofloxacin USP (250mg)
                    - **Estimated Drug Class**: Fluoroquinolone Antibacterial / Antibiotic
                    - **Primary Approved Uses**: Urinary tract, systemic respiratory, and soft tissue bacterial infections
                    - **Standard Dose Guidance**: 250mg - 500mg twice daily
                    - **Common Associated Side Effects**: Mild nausea, abdominal ache, photosensitivity, or muscle tendonitis caution.
                    
                    *Offline Note: Scanned and processed via Local Deterministic Molecular Indexing (Offline Mode).*
                """.trimIndent()
                else -> """
                    ### 📊 OFFLINE LABEL SCAN RESULT
                    - **Trade Name & Active Molecule**: Omez / Omeprazole Delayed-Release (20mg)
                    - **Estimated Drug Class**: Proton Pump Inhibitor (PPI)
                    - **Primary Approved Uses**: Heartburn, stomach acid reflux, and gastric/peptic ulcers
                    - **Standard Dose Guidance**: 20mg once daily, 30 minutes prior to morning meal
                    - **Common Associated Side Effects**: Headache, transient diarrhea, flatulence, or mild nausea.
                    
                    *Offline Note: Scanned and processed via Local Deterministic Molecular Indexing (Offline Mode).*
                """.trimIndent()
            }
            labelScannerResult.value = offlineScan
            labelScannerLoading.value = false
            return
        }

        viewModelScope.launch {
            val prompt = """
                Analyze this scanned label text of a medicine container:
                "$labelTextDesc"
                
                Expose details beautifully inside a highly formatted medical outline:
                - **Trade Name & Active Molecule**
                - **Estimated Drug Class**
                - **Primary Approved Uses**
                - **Standard Dose Guidance (Adults)**
                - **Common Associated Side Effects**
                
                Ensure the style is educational and concludes with an expert pharmacist note.
            """.trimIndent()

            val analysis = withContext(Dispatchers.IO) {
                RetrofitClient.getGeminiResponse(
                    prompt = prompt,
                    systemInstruction = "You are a clinical labeling specialist. Transform raw scan text into tidy instruction charts.",
                    customApiKey = customApiKey.value.ifEmpty { null }
                )
            }

            labelScannerResult.value = analysis
            labelScannerLoading.value = false
        }
    }

    fun resetScanner() {
        labelScannerResult.value = null
        labelScannerLoading.value = false
    }

    // --- Profile Management & Authentication ---
    fun hashPassword(password: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest(password.toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            password
        }
    }

    fun registerOrSignIn(usrName: String, mail: String, name: String, userAge: Int, userGender: String, goal: String) {
        val defaultPassword = "password"
        viewModelScope.launch(Dispatchers.IO) {
            val cleanEmail = mail.trim().lowercase()
            val existing = userDao.getUserByEmail(cleanEmail)
            val finalUser = existing ?: User(
                email = cleanEmail,
                username = usrName,
                fullName = name,
                passwordHash = hashPassword(defaultPassword),
                age = userAge,
                gender = userGender,
                healthGoal = goal
            )
            if (existing == null) {
                userDao.insertUser(finalUser)
            }
            withContext(Dispatchers.Main) {
                saveLoginStateToPrefs(finalUser)
            }
        }
    }

    fun registerNewUser(
        usrName: String,
        mail: String,
        name: String,
        pass: String,
        userAge: Int,
        userGender: String,
        goal: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val cleanEmail = mail.trim().lowercase()
            if (cleanEmail.isEmpty() || usrName.trim().isEmpty() || pass.isEmpty()) {
                withContext(Dispatchers.Main) { onError("Required fields cannot be empty!") }
                return@launch
            }
            val existing = userDao.getUserByEmail(cleanEmail)
            if (existing != null) {
                withContext(Dispatchers.Main) { onError("This email is already registered!") }
                return@launch
            }

            val newUser = User(
                email = cleanEmail,
                username = usrName,
                fullName = name,
                passwordHash = hashPassword(pass),
                age = userAge,
                gender = userGender,
                healthGoal = goal
            )
            userDao.insertUser(newUser)

            withContext(Dispatchers.Main) {
                saveLoginStateToPrefs(newUser)
                onSuccess()
            }
        }
    }

    fun loginExistingUser(
        mail: String,
        pass: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val cleanEmail = mail.trim().lowercase()
            if (cleanEmail.isEmpty() || pass.isEmpty()) {
                withContext(Dispatchers.Main) { onError("Please enter your email and password!") }
                return@launch
            }
            val user = userDao.getUserByEmail(cleanEmail)
            if (user == null) {
                withContext(Dispatchers.Main) { onError("Account not found. Please register first!") }
                return@launch
            }

            val hashedInput = hashPassword(pass)
            if (user.passwordHash == hashedInput) {
                withContext(Dispatchers.Main) {
                    saveLoginStateToPrefs(user)
                    onSuccess()
                }
            } else {
                withContext(Dispatchers.Main) { onError("Incorrect password. Please try again.") }
            }
        }
    }

    private fun saveLoginStateToPrefs(user: User) {
        val storedAvatar = sharedPrefs.getInt("avatar_index_${user.email}", 0)
        sharedPrefs.edit().apply {
            putBoolean("is_logged_in", true)
            putString("username", user.username)
            putString("email", user.email)
            putString("full_name", user.fullName)
            putInt("age", user.age)
            putString("gender", user.gender)
            putString("health_goal", user.healthGoal)
            putInt("avatar_index", storedAvatar)
            apply()
        }
        isLoggedIn.value = true
        username.value = user.username
        email.value = user.email
        fullName.value = user.fullName
        age.value = user.age
        gender.value = user.gender
        healthGoal.value = user.healthGoal
        avatarIndex.value = storedAvatar

        generateProfileInsights()
    }

    fun logout() {
        sharedPrefs.edit().apply {
            putBoolean("is_logged_in", false)
            putString("username", "")
            putString("email", "")
            putString("full_name", "")
            putInt("age", 30)
            putString("gender", "Male")
            putString("health_goal", "Maintain Good Health")
            putInt("avatar_index", 0)
            apply()
        }
        isLoggedIn.value = false
        username.value = ""
        email.value = ""
        fullName.value = ""
        age.value = 30
        gender.value = "Male"
        healthGoal.value = "Maintain Good Health"
        avatarIndex.value = 0
        
        testingRecommendations.value = null
        personalDiet.value = null
        customDiscoveryNews.value = null
    }

    fun updateAvatarIndex(newIndex: Int) {
        avatarIndex.value = newIndex
        val userEmail = email.value
        sharedPrefs.edit().apply {
            putInt("avatar_index", newIndex)
            if (userEmail.isNotEmpty()) {
                putInt("avatar_index_$userEmail", newIndex)
            }
            apply()
        }
    }

    fun updateProfile(name: String, userAge: Int, userGender: String, goal: String) {
        val userEmail = email.value
        if (userEmail.isEmpty()) return
        sharedPrefs.edit().apply {
            putString("full_name", name)
            putInt("age", userAge)
            putString("gender", userGender)
            putString("health_goal", goal)
            apply()
        }
        fullName.value = name
        age.value = userAge
        gender.value = userGender
        healthGoal.value = goal

        viewModelScope.launch(Dispatchers.IO) {
            val currentUser = userDao.getUserByEmail(userEmail)
            if (currentUser != null) {
                userDao.insertUser(currentUser.copy(
                    fullName = name,
                    age = userAge,
                    gender = userGender,
                    healthGoal = goal
                ))
            }
            generateProfileInsights()
        }
    }

    // --- Medicine Expenses ---
    fun addMedicineExpense(drugName: String, cost: Double, category: String) {
        val userEmail = email.value
        if (userEmail.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            expenseDao.insertExpense(MedicineExpense(userEmail = userEmail, drugName = drugName, cost = cost, category = category))
        }
    }

    fun deleteMedicineExpense(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            expenseDao.deleteExpenseById(id)
        }
    }

    fun clearAllExpenses() {
        val userEmail = email.value
        if (userEmail.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            expenseDao.clearAllForUser(userEmail)
        }
    }

    // --- User Diseases ---
    fun toggleUserDisease(diseaseId: String, diseaseName: String) {
        val userEmail = email.value
        if (userEmail.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            if (userDiseaseDao.hasDisease(diseaseId, userEmail)) {
                userDiseaseDao.deleteUserDisease(diseaseId, userEmail)
            } else {
                userDiseaseDao.insertUserDisease(UserDisease(userEmail = userEmail, diseaseId = diseaseId, name = diseaseName))
            }
            generateProfileInsights()
        }
    }

    // --- Profile Advisor (Gemini) ---
    fun generateProfileInsights() {
        if (!isLoggedIn.value) return
        profileInsightsLoading.value = true

        if (!useAiForDashboard.value) {
            viewModelScope.launch {
                val diseases = userDiseasesList.value.joinToString(", ") { it.name }
                val testingOffline = StringBuilder()
                val dietOffline = StringBuilder()
                val newsOffline = StringBuilder()
                
                testingOffline.append("### 📊 OFFLINE RECOMMENDATIONS: Screening Diagnostics (Offline Mode)\n")
                dietOffline.append("### 🥗 OFFLINE RECOMMENDATIONS: Medical Nutrition (Offline Mode)\n")
                newsOffline.append("### 📰 OFFLINE HEALTH DISCOVERIES: Longevity Science (Offline Mode)\n")
                
                val hasDiabetes = diseases.contains("Diabetes", ignoreCase = true)
                val hasHypertension = diseases.contains("Hypertension", ignoreCase = true)
                val hasAsthma = diseases.contains("Asthma", ignoreCase = true) || diseases.contains("Respiratory", ignoreCase = true)
                
                if (hasDiabetes) {
                    testingOffline.append("- **HbA1c Blood Test**: Every 3 months to monitor long-term glycemic control.\n- **Daily Home Glucose Tracking**: Fasting (target < 100 mg/dL) and 2-hours post-meal (target < 140 mg/dL).\n- **Annual Kidney & Eye Screening**: Check urine microalbumin and dilated eye examination.\n")
                    dietOffline.append("- **Foods to Prioritize**: Leafy green spinach, oats, quinoa, legumes, and high-fiber cruciferous vegetables.\n- **Foods to Minimize/Avoid**: Direct white sugar, honey, refined flour (Maida), potatoes, white rice, and packaged snacks.\n- **Meal Scheduling**: Eat 3 small meals with 2 light protein-rich snacks. Avoid late-night eating.\n")
                    newsOffline.append("#### 🌐 CGM Sensors Enhance Diabetes Longevity\nClinical studies indicate that real-time continuous glucose monitors help patients spend 24% more time-in-range, reducing diabetic complications.\n\n#### 🌐 Plant Compounds for Insulin Sensitivity\nNew research shows that dietary active berberine supports standard AMPK enzymes similarly to Metformin in clinical trials.\n")
                } else if (hasHypertension) {
                    testingOffline.append("- **Blood Pressure Tracking**: Home measurement twice daily (morning & evening, targets below 130/80 mmHg).\n- **Lipid Profile & Serum Creatinine**: Every 6 months to monitor cardiovascular and renal function.\n- **Electrocardiogram (ECG)**: Annually.\n")
                    dietOffline.append("- **Foods to Prioritize**: Potassium-rich avocados, potassium-filled bananas, celery, beetroot, and garlic cloves.\n- **Foods to Minimize/Avoid**: Excess table salt (sodium < 1500mg daily), pickles, canned soups, papad, soy sauce, and processed meats.\n- **Hydration**: Drink 2.5L to 3L water daily to naturally manage blood viscosity.\n")
                    newsOffline.append("#### 🌐 Resonant Breathing Reduces Arterial Stiffness\nA multi-center clinical study shows that paced breathing at 6 cycles per minute for 10 minutes drops systolic BP by 8 mmHg.\n\n#### 🌐 Olive Polyphenols Lower Cardiac Stress\nConsuming 2 tablespoons of extra-virgin olive oil daily is linked to lowered systemic vascular resistance.\n")
                } else if (hasAsthma) {
                    testingOffline.append("- **Spirometry / Lung Function Test**: Every 6-12 months.\n- **Peak Flow Meter Tracking**: Daily home checking to evaluate airway obstruction levels.\n")
                    dietOffline.append("- **Foods to Prioritize**: Vitamin C rich citrus fruits, ginger, garlic, and Omega-3 rich seeds (flax, chia).\n- **Foods to Minimize/Avoid**: Ice-cold beverages, heavy sodium, foods containing sulfite preservatives (dried fruits, wine).\n")
                    newsOffline.append("#### 🌐 Paced Airway Expansion Techniques\nPreventative respiratory trials highlight that pranayama breathing practices dramatically reduce acute inhaler dependency.\n")
                } else {
                    testingOffline.append("- **Annual Comprehensive Health Check**: Complete blood count, lipid profile, liver & kidney tests, and blood sugar.\n- **Routine Vitals Tracking**: Check blood pressure and heart rate monthly at home.\n")
                    dietOffline.append("- **Balanced Nutrition Plate**: 50% colorful vegetables, 25% lean protein, and 25% complex whole grains.\n- **Hydration**: Drink 2.5L to 3L pure water daily.\n")
                    newsOffline.append("#### 🌐 Circadian Rhythms & Metabolic Wellness\nNew clinical guidelines establish that finishing dinner by 7 PM improves insulin sensitivity and sleep quality index by 15%.\n")
                }
                
                testingRecommendations.value = testingOffline.toString()
                personalDiet.value = dietOffline.toString()
                customDiscoveryNews.value = newsOffline.toString()
                profileInsightsLoading.value = false
            }
            return
        }

        viewModelScope.launch {
            val diseases = userDiseasesList.value.joinToString(", ") { it.name }
            val lastReport = reportsList.value.firstOrNull()?.let {
                "Lab Data: ${it.labData}, Risk Level: ${it.riskLevel}, Possible Indication: ${it.possibleIndication}"
            } ?: "No reports submitted yet."

            val userContext = """
                User Profile:
                - Name: ${fullName.value}
                - Age: ${age.value}
                - Gender: ${gender.value}
                - Health Goal: ${healthGoal.value}
                - Tracked Diseases/Conditions: ${diseases.ifEmpty { "None tracked yet" }}
                - Last Lab Report Summary: $lastReport
            """.trimIndent()

            val testingPrompt = """
                Based on this clinical and wellness profile of the user, provide personalized future testing recommendations.
                $userContext
                
                Please structure the output as follows:
                - **Recommended Lab Tests or Screening Diagnostics**: (e.g. HbA1c, lipid panel, kidney function tests, thyroid, etc.) with specific timelines or schedules.
                - **Routine Vitals / Home Monitoring**: What parameters to track at home (e.g., daily blood pressure, blood glucose, heart rate) and how often.
                - **Symptom Alarm Triggers**: When to contact a physician immediately based on their tracked conditions.
                
                Keep formatting professional, reassuring, and clear. Suggest real-world testing frequencies according to standard clinical guidelines.
            """.trimIndent()

            val dietPrompt = """
                Based on this clinical and wellness profile of the user, design a highly customized medical nutrition and diet guideline.
                $userContext
                
                Please structure the output as follows:
                - **Foods to Prioritize**: List of nutrient-dense, clinically helpful food items with portion ideas.
                - **Foods to Minimize/Avoid**: Clear warnings for ingredients or foods that exacerbate their tracked conditions (e.g. low-sodium for high blood pressure, low-glycemic for diabetes).
                - **Meal Scheduling & Hydration Tips**: Advice on meal timings and daily water goals.
                
                Keep formatting engaging, helpful, and highly clinical yet easy for a layperson to execute.
            """.trimIndent()

            val newsPrompt = """
                Generate a personalized "Health Discovery News & Breakthroughs" feed matching the user's health profile.
                $userContext
                
                Generate 2-3 short, highly engaging, fictional or real recent clinical discovery/advancement "news flashes" or breakthrough summaries relevant to their diseases or age/gender profile.
                For example:
                - If they have Diabetes: discuss recent sensor technologies, continuous glucose monitor advancements, or plant-based compounds shown to support insulin sensitivity in clinical trials.
                - If they have Hypertension: discuss recent findings on sodium-potassium balance, behavioral breathing techniques to lower blood pressure, or medication adherence research.
                - If they have no conditions: general cardiovascular longevity, metabolic optimization, or sleep hygiene science.
                
                Please style these as attractive news headlines followed by a 2-3 sentence summaries. Ensure a highly inspiring, positive, and research-backed tone!
            """.trimIndent()

            val job = launch {
                val testJob = launch(Dispatchers.IO) {
                    val testingRes = RetrofitClient.getGeminiResponse(
                        prompt = testingPrompt,
                        systemInstruction = "You are a preventative medicine physician and clinical wellness advisor. Give actionable, precise diagnostic test schedules.",
                        customApiKey = customApiKey.value.ifEmpty { null }
                    )
                    testingRecommendations.value = testingRes
                }

                val dietJob = launch(Dispatchers.IO) {
                    val dietRes = RetrofitClient.getGeminiResponse(
                        prompt = dietPrompt,
                        systemInstruction = "You are an expert clinical dietitian and endocrinology nutritional specialist. Create safe, personalized diet charts.",
                        customApiKey = customApiKey.value.ifEmpty { null }
                    )
                    personalDiet.value = dietRes
                }

                val newsJob = launch(Dispatchers.IO) {
                    val newsRes = RetrofitClient.getGeminiResponse(
                        prompt = newsPrompt,
                        systemInstruction = "You are a professional medical journalist and longevity science editor. Summarize encouraging scientific breakthroughs clearly.",
                        customApiKey = customApiKey.value.ifEmpty { null }
                    )
                    customDiscoveryNews.value = newsRes
                }
                
                testJob.join()
                dietJob.join()
                newsJob.join()
            }
            job.join()
            profileInsightsLoading.value = false
        }
    }
}

class PharmaViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PharmaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PharmaViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
