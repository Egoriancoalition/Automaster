
package ru.automaster.app

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import android.graphics.Color
import android.text.InputFilter
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Executors

class VinLookupActivity : Activity() {

    private lateinit var vinInput: EditText
    private lateinit var resultText: TextView
    private val executor = Executors.newSingleThreadExecutor()
    private val prefs by lazy {
        getSharedPreferences("vin_cache", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.WHITE)
        }

        val title = TextView(this).apply {
            text = "АвтоМастер — поиск двигателя по VIN"
            textSize = 22f
            setTextColor(Color.BLACK)
        }
        root.addView(title)

        vinInput = EditText(this).apply {
            hint = "Введите VIN (17 символов)"
            textSize = 18f
            filters = arrayOf(
                InputFilter.AllCaps(),
                InputFilter.LengthFilter(17)
            )
        }
        root.addView(vinInput)

        val searchButton = Button(this).apply {
            text = "Найти автомобиль"
            setOnClickListener { searchVin() }
        }
        root.addView(searchButton)

        val clearButton = Button(this).apply {
            text = "Очистить"
            setOnClickListener {
                vinInput.text.clear()
                resultText.text = "Введите VIN и нажмите «Найти автомобиль»."
            }
        }
        root.addView(clearButton)

        resultText = TextView(this).apply {
            text = "Поиск работает через интернет. Ранее найденные результаты сохраняются на телефоне."
            textSize = 16f
            setTextColor(Color.DKGRAY)
            setPadding(0, 20, 0, 0)
        }
        val scroll = ScrollView(this)
        scroll.addView(resultText)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            )
        )

        setContentView(root)
    }

    private fun searchVin() {
        val vin = vinInput.text.toString().trim().uppercase()

        if (vin.length != 17 || vin.any { it !in 'A'..'Z' && it !in '0'..'9' } ||
            vin.any { it in "IOQ" }) {
            Toast.makeText(
                this, "Проверьте VIN: обычно это 17 символов без I, O и Q",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        resultText.text = "Ищем автомобиль по VIN: $vin…"

        val cached = prefs.getString(vin, null)
        if (cached != null) {
            resultText.text = "Сохранённый результат:\n\n$cached\n\nПроверяем данные через интернет…"
        }

        executor.execute {
            try {
                val encodedVin = URLEncoder.encode(vin, "UTF-8")
                val url = URL(
                    "https://vpic.nhtsa.dot.gov/api/vehicles/DecodeVinValuesExtended/$encodedVin?format=json"
                )
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 12000
                connection.readTimeout = 12000

                val response = connection.inputStream.bufferedReader().use {
                    it.readText()
                }
                connection.disconnect()

                val results = JSONObject(response)
                    .getJSONArray("Results")
                if (results.length() == 0) {
                    throw Exception("Сервис не вернул данные для этого VIN.")
                }

                val item = results.getJSONObject(0)
                fun field(name: String): String {
                    val value = item.optString(name, "").trim()
                    return if (value.isBlank() || value == "null") "Не определено" else value
                }

                val report = """
Марка: ${field("Make")}
Модель: ${field("Model")}
Год выпуска: ${field("ModelYear")}
Тип кузова: ${field("BodyClass")}
Двигатель: ${field("EngineModel")}
Код двигателя: ${field("EngineModel")}
Объём, л: ${field("DisplacementL")}
Объём, см³: ${field("DisplacementCC")}
Цилиндры: ${field("EngineCylinders")}
Топливо: ${field("FuelTypePrimary")}
Мощность, л.с.: ${field("EngineHP")}
Привод: ${field("DriveType")}
Коробка передач: ${field("TransmissionStyle")}
Страна сборки: ${field("PlantCountry")}

Важно: база может не содержать точный код двигателя, особенно для автомобилей российского, европейского и азиатского рынков. Подтвердите его по заводскому каталогу, маркировке двигателя или документам автомобиля.
                """.trimIndent()

                prefs.edit().putString(vin, report).apply()

                runOnUiThread {
                    resultText.text = report
                }
            } catch (e: Exception) {
                runOnUiThread {
                    resultText.text =
                        "Не удалось получить данные через интернет.\n\n" +
                        "Причина: ${e.message ?: "ошибка соединения"}\n\n" +
                        (cached?.let { "Сохранённый результат:\n\n$it" }
                            ?: "Для первого поиска необходимо подключение к интернету.")
                }
            }
        }
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }
}
