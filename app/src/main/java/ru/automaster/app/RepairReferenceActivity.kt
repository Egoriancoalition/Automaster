
package ru.automaster.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Typeface
import android.view.ViewGroup
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.text.Editable
import android.text.TextWatcher
import org.json.JSONArray
import org.json.JSONObject

class RepairReferenceActivity : Activity() {

    private lateinit var content: LinearLayout
    private lateinit var search: EditText
    private var engines = JSONArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "Справочник двигателей"

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 20, 20, 12)
        }

        val heading = TextView(this).apply {
            text = "АвтоМастер — ремонт двигателей"
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 16)
        }
        root.addView(heading)

        search = EditText(this).apply {
            hint = "Поиск марки, двигателя или работы"
            singleLine = true
        }
        root.addView(
            search,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        val scroll = ScrollView(this)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        scroll.addView(content)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)

        try {
            val json = assets.open("engine_reference.json")
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }

            engines = JSONObject(json).optJSONArray("engines")
                ?: JSONArray()

            render("")
        } catch (e: Exception) {
            addText(
                "Не удалось загрузить справочник. " +
                    "Проверьте файл app/src/main/assets/engine_reference.json"
            )
        }

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?, start: Int, count: Int, after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count: Int
            ) {
                render(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun render(query: String) {
        content.removeAllViews()
        val q = query.trim().lowercase()
        var found = 0

        for (i in 0 until engines.length()) {
            val engine = engines.optJSONObject(i) ?: continue
            val jobs = engine.optJSONArray("jobs") ?: JSONArray()

            val make = engine.optString("make")
            val model = engine.optString("model")
            val category = engine.optString("category")

            val matchingJobs = mutableListOf<JSONObject>()

            for (j in 0 until jobs.length()) {
                val job = jobs.optJSONObject(j) ?: continue
                val searchable = job.optString("name") + " " +
                    job.optString("note") + " " + make + " " +
                    model + " " + category

                if (q.isEmpty() || searchable.lowercase().contains(q)) {
                    matchingJobs.add(job)
                }
            }

            if (q.isNotEmpty() && matchingJobs.isEmpty()) continue

            found++

            addText(
                "$make $model — $category",
                20f,
                true
            )
            addText(
                "Статус: ${engine.optString("status")}",
                13f,
                false
            )

            for (job in matchingJobs) {
                addText("• ${job.optString("name")}", 17f, true)
                addText(job.optString("note"), 15f, false)
                addText("", 6f, false)
            }

            addText("────────────────────", 12f, false)
        }

        if (found == 0) {
            addText("Ничего не найдено. Попробуйте другой запрос.")
        }
    }

    private fun addText(
        value: String,
        size: Float = 16f,
        bold: Boolean = false
    ) {
        val view = TextView(this).apply {
            text = value
            textSize = size
            setTextColor(android.graphics.Color.BLACK)
            setPadding(4, 8, 4, 8)
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }
        content.addView(
            view,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }
}
