
package ru.automaster.app

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : Activity() {
    private lateinit var root: LinearLayout
    private val prefs by lazy {
        getSharedPreferences("automaster_data", Context.MODE_PRIVATE)
    }

    private val prices = listOf(
        "Компьютерная диагностика" to 1500,
        "Замена масла двигателя" to 1200,
        "Замена масляного фильтра" to 300,
        "Замена воздушного фильтра" to 400,
        "Замена салонного фильтра" to 600,
        "Замена свечей зажигания" to 1000,
        "Замена тормозных колодок передних" to 1800,
        "Замена тормозных колодок задних" to 2000,
        "Замена тормозного диска" to 1500,
        "Замена тормозной жидкости" to 1200,
        "Замена антифриза" to 1800,
        "Замена термостата" to 2500,
        "Замена ремня ГРМ" to 6500,
        "Замена приводного ремня" to 1500,
        "Замена воздушного патрубка" to 700,
        "Замена аккумулятора" to 500,
        "Замена стартера" to 2500,
        "Замена генератора" to 2500,
        "Замена амортизатора" to 2500,
        "Замена шаровой опоры" to 1800,
        "Замена рулевого наконечника" to 1200,
        "Замена ступичного подшипника" to 3500,
        "Замена сцепления" to 9000,
        "Замена топливного фильтра" to 1200,
        "Регулировка клапанов" to 3000,
        "Снятие и установка головки блока" to 12000,
        "Замена прокладки ГБЦ" to 18000,
        "Замена двигателя" to 25000,
        "Замена АКПП" to 18000,
        "Замена МКПП" to 12000,
        "Проверка подвески" to 1000,
        "Развал-схождение" to 2500
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun makeRoot(title: String) {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 24, 28, 24)
        }
        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)
        addText(title, 25, true)
        addText("АвтоМастер • автосервис", 14, false)
        addButton("← На главную") { showHome() }
    }

    private fun addText(text: String, size: Int = 16, bold: Boolean = false) {
        root.addView(TextView(this).apply {
            this.text = text
            textSize = size.toFloat()
            if (bold) setTypeface(null, Typeface.BOLD)
            setPadding(0, 10, 0, 10)
        })
    }

    private fun addButton(text: String, action: () -> Unit) {
        root.addView(Button(this).apply {
            this.text = text
            isAllCaps = false
            setOnClickListener { action() }
        }, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))
    }

    private fun showHome() {
        makeRoot("АвтоМастер")
        addText("Управление автосервисом", 18, true)
        addButton("Автомобили клиентов") { showCars() }
        addButton("Прайс-лист работ") { showPrices() }
        addButton("Заказ-наряды") { showOrders() }
        addButton("Диагностика") { showDiagnostics() }
        addText("Данные автомобилей и заказ-нарядов сохраняются на этом телефоне.")
        addText("Цены ориентировочные. Уточняйте стоимость перед ремонтом.")
    }

    private fun readRecords(key: String): JSONArray {
        return try {
            JSONArray(prefs.getString(key, "[]") ?: "[]")
        } catch (_: Exception) {
            JSONArray()
        }
    }

    private fun saveRecord(key: String, record: JSONObject) {
        val records = readRecords(key)
        records.put(record)
        prefs.edit().putString(key, records.toString()).apply()
    }

    private fun showCars() {
        makeRoot("Автомобили клиентов")
        addButton("+ Добавить автомобиль") { addCarDialog() }

        val cars = readRecords("cars")
        if (cars.length() == 0) addText("Пока нет сохранённых автомобилей.")
        for (i in cars.length() - 1 downTo 0) {
            val car = cars.getJSONObject(i)
            addText(
                "${car.optString("brand")} ${car.optString("model")}\n" +
                "Год: ${car.optString("year")} • Госномер: ${car.optString("plate")}\n" +
                "Владелец: ${car.optString("owner")}\n" +
                "Телефон: ${car.optString("phone")}\n" +
                "VIN: ${car.optString("vin")}",
                16, true
            )
            addButton("Удалить эту запись") {
                AlertDialog.Builder(this)
                    .setMessage("Удалить карточку автомобиля?")
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton("Удалить") { _, _ ->
                        val updated = JSONArray()
                        for (j in 0 until cars.length()) {
                            if (j != i) updated.put(cars.getJSONObject(j))
                        }
                        prefs.edit().putString("cars", updated.toString()).apply()
                        showCars()
                    }.show()
            }
        }
    }

    private fun addCarDialog() {
        val fields = listOf(
            "Марка" to "Toyota",
            "Модель" to "Corolla",
            "Год выпуска" to "2008",
            "Госномер" to "",
            "Владелец" to "",
            "Телефон" to "",
            "VIN" to ""
        )
        showForm("Новый автомобиль", fields) { values ->
            if (values[0].isBlank() || values[1].isBlank()) {
                toast("Укажи марку и модель")
                return@showForm
            }
            val record = JSONObject()
            val keys = listOf("brand", "model", "year", "plate", "owner", "phone", "vin")
            keys.forEachIndexed { i, key -> record.put(key, values[i]) }
            saveRecord("cars", record)
            showCars()
            toast("Автомобиль сохранён")
        }
    }

    private fun showPrices() {
        makeRoot("Прайс-лист")
        addText("Ориентировочные цены на работы, ₽", 17, true)
        prices.forEach { (name, price) ->
            addText("$name — $price ₽")
        }
        addButton("Создать заказ-наряд") { addOrderDialog() }
    }

    private fun showOrders() {
        makeRoot("Заказ-наряды")
        addButton("+ Новый заказ-наряд") { addOrderDialog() }

        val orders = readRecords("orders")
        if (orders.length() == 0) addText("Заказ-нарядов пока нет.")
        for (i in orders.length() - 1 downTo 0) {
            val order = orders.getJSONObject(i)
            val description =
                "Заказ № ${order.optString("number")}\n" +
                "Клиент: ${order.optString("customer")}\n" +
                "Автомобиль: ${order.optString("car")}\n" +
                "Работы: ${order.optString("works")}\n" +
                "Стоимость: ${order.optString("price")} ₽\n" +
                "Статус: ${order.optString("status")}\n" +
                "Дата: ${order.optString("date")}"
            addText(description, 16, true)
            addButton("Поделиться заказ-нарядом") {
                shareText(description)
            }
            addButton("Удалить заказ-наряд") {
                AlertDialog.Builder(this)
                    .setMessage("Удалить заказ-наряд?")
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton("Удалить") { _, _ ->
                        val updated = JSONArray()
                        for (j in 0 until orders.length()) {
                            if (j != i) updated.put(orders.getJSONObject(j))
                        }
                        prefs.edit().putString("orders", updated.toString()).apply()
                        showOrders()
                    }.show()
            }
        }
    }

    private fun addOrderDialog() {
        val fields = listOf(
            "Номер заказ-наряда" to "001",
            "ФИО клиента" to "",
            "Автомобиль (марка, модель, номер)" to "",
            "Перечень работ" to "",
            "Стоимость работ, ₽" to "0",
            "Статус (принят / в работе / готов)" to "Принят",
            "Дата" to java.text.SimpleDateFormat(
                "dd.MM.yyyy", java.util.Locale.getDefault()
            ).format(java.util.Date())
        )
        showForm("Новый заказ-наряд", fields) { values ->
            if (values[1].isBlank() || values[3].isBlank()) {
                toast("Заполни клиента и перечень работ")
                return@showForm
            }
            val price = values[4].toDoubleOrNull()
            if (price == null || price < 0) {
                toast("Проверь стоимость")
                return@showForm
            }
            val record = JSONObject()
            val keys = listOf(
                "number", "customer", "car", "works",
                "price", "status", "date"
            )
            values.forEachIndexed { i, value -> record.put(keys[i], value) }
            saveRecord("orders", record)
            showOrders()
            toast("Заказ-наряд сохранён")
        }
    }

    private fun showDiagnostics() {
        makeRoot("Диагностика")
        addText("Запиши результаты проверки автомобиля.")
        val car = EditText(this).apply {
            hint = "Автомобиль и двигатель"
        }
        val symptoms = EditText(this).apply {
            hint = "Жалобы и симптомы"
        }
        val data = EditText(this).apply {
            hint = "Коды ошибок и показания сканера"
            minLines = 3
            gravity = android.view.Gravity.TOP
        }
        val conclusion = EditText(this).apply {
            hint = "Выводы и рекомендации"
            minLines = 3
            gravity = android.view.Gravity.TOP
        }
        listOf(car, symptoms, data, conclusion).forEach { root.addView(it) }
        addButton("Поделиться результатами") {
            val text = "ДИАГНОСТИКА\nАвтомобиль: ${car.text}\n" +
                "Симптомы: ${symptoms.text}\n" +
                "Данные сканера: ${data.text}\n" +
                "Выводы: ${conclusion.text}"
            shareText(text)
        }
        addText("Примечание: этот экран пока не сохраняет записи после выхода.")
    }

    private fun showForm(
        title: String,
        fields: List<Pair<String, String>>,
        onSave: (List<String>) -> Unit
    ) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 8, 32, 8)
        }
        val inputs = fields.map { (label, initial) ->
            EditText(this).apply {
                hint = label
                setText(initial)
                singleLine = false
                maxLines = 3
                setPadding(8, 12, 8, 12)
                container.addView(this)
            }
        }
        val scroll = ScrollView(this)
        scroll.addView(container)
        val dialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setView(scroll)
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Сохранить", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                onSave(inputs.map { it.text.toString().trim() })
                if (!dialog.isShowing) return@setOnClickListener
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "Поделиться"))
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
