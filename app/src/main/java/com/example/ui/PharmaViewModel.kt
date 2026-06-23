package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.RetrofitClient
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PharmaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = PharmaDatabase.getDatabase(application)
    private val reportDao = db.medicalReportDao()
    private val chatDao = db.chatMessageDao()
    private val bookmarkDao = db.bookmarkDao()

    // --- State Flows from Room ---
    val reportsList: StateFlow<List<MedicalReport>> = reportDao.getAllReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = chatDao.getChatHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedList: StateFlow<List<BookmarkedItem>> = bookmarkDao.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
        viewModelScope.launch {
            val exists = bookmarkDao.isBookmarked(id)
            if (exists) {
                bookmarkDao.deleteBookmark(BookmarkedItem(id = id, type = type, name = name, subtitle = subtitle))
            } else {
                bookmarkDao.insertBookmark(BookmarkedItem(id = id, type = type, name = name, subtitle = subtitle))
            }
        }
    }

    fun isBookmarkedFlow(id: String): Flow<Boolean> {
        return bookmarkDao.isBookmarkedFlow(id)
    }

    // AI Chat Messenger Logic
    fun sendChatMessage() {
        val query = chatInput.value.trim()
        if (query.isEmpty()) return

        chatInput.value = ""
        chatLoading.value = true

        viewModelScope.launch {
            // Save User Message to SQLite
            val userMsg = ChatMessageEntity(role = "user", content = query)
            chatDao.insertMessage(userMsg)

            // Compile conversation history
            val pastMessages = chatDao.getChatHistory().first()
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
                    systemInstruction = "You are PharmaSense AI, a smart health pharmacist. Be extremely helpful and exact."
                )
            }

            // Save Model Reply to SQLite
            val modelMsg = ChatMessageEntity(role = "model", content = reply)
            chatDao.insertMessage(modelMsg)
            chatLoading.value = false
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            chatDao.clearHistory()
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

        // 2. Gemini symptom assistant if safe
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
                    systemInstruction = "You are an expert clinical triage assistant. Keep formatting professional and highly structured."
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

        // 2. Fetch via Gemini for more complex or unmapped check
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
                    systemInstruction = "You are an expert clinical pharmacist explaining medical interactions precisely."
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
            val prompt = """
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

            val analysisResult = withContext(Dispatchers.IO) {
                RetrofitClient.getGeminiResponse(
                    prompt = prompt,
                    systemInstruction = "You are a professional clinical hematologist and lab report analyst. Translate numbers into clear insight."
                )
            }

            // Determine local metadata indicators
            var risk = "Low"
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

            val finalIndication = if (indications.isEmpty()) "All Metrics in Normal Range" else indications.joinToString(", ")

            // Save report to Room DB
            val labDataString = "Hgb: $hgb | WBC: $wbc | Plt: $plt | Glucose: $glu | BP: $systolic/$diastolic"
            val reportRecord = MedicalReport(
                title = title,
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
                    systemInstruction = "You are a clinical labeling specialist. Transform raw scan text into tidy instruction charts."
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
