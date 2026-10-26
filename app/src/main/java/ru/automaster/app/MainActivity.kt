
package ru.automaster.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var root: LinearLayout

    private val prefs by lazy {
        getSharedPreferences("automaster_data", Context.MODE_PRIVATE)
    }

    private data class Service(
        val name: String,
        val price: Int
    )

    private val services = listOf(
        Service("Компьютерная диагностика", 1500),
        Service("Замена масла двигателя", 1200),
        Service("Замена масляного фильтра", 300),
        Service("Замена воздушного фильтра", 400),
        Service("Замена салонного фильтра", 600),
        Service("Замена свечей зажигания", 1000),
        Service("Замена передних тормозных колодок", 1800),
        Service("Замена задних тормозных колодок", 2000),
        Service("Замена тормозного диска", 1500),
        Service("Замена тормозной жидкости", 1200),
        Service("Замена антифриза", 1800),
        Service("Замена термостата", 2500),
        Service("Замена ремня ГРМ", 6500),
        Service("Замена приводного ремня", 1500),
        Service("Замена аккумулятора", 500),
        Service("Замена стартера", 2500),
        Service("Замена генератора", 2500),
        Service("Замена амортизатора", 2500),
        Service("Замена шаровой опоры", 1800),
        Service("Замена рулевого наконечника", 1200),
        Service("Замена ступичного подшипника", 3500),
        Service("Замена сцепления", 9000),
        Service("Замена топливного фильтра", 1200),
        Service("Регулировка клапанов", 3000),
        Service("Снятие и установка ГБЦ", 12000),
        Service("Замена прокладки ГБЦ", 18000),
        Service("Замена двигателя", 25000),
        Service("Замена АКПП", 18000),
        Service("Замена МКПП", 12000),
        Service("Проверка подвески", 1000),
        Service("Развал-схождение", 2500)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun makeRoot(title: String) {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 20, 24, 28)
        }

        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)

        addText(title, 26, true)
        addText("АвтоМастер • учёт автосервиса", 14)
        addButton("← Главная") { showHome() }
    }

    private fun addText(
        value: String,
        size: Int = 16,
        bold: Boolean = false
    ) {
        root.addView(TextView(this).apply {
            text = value
            textSize = size.toFloat()
            setTextColor(0xFF202124.toInt())
            if (bold) setTypeface(null, Typeface.BOLD)
            setPadding(0, 8, 0, 8)
        })
    }

    private fun addButton(
        title: String,
        action: () -> Unit
    ) {
        root.addView(Button(this).apply {
            text = title
            isAllCaps = false
            setOnClickListener { action() }
        }, LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ))
    }

    private fun readRecords(key: String): JSONArray {
        return try {
            JSONArray(prefs.getString(key, "[]") ?: "[]")
        } catch (_: Exception) {
            JSONArray()
        }
    }

    private fun saveRecords(key: String, records: JSONArray) {
        prefs.edit().putString(key, records.toString()).apply()
    }

    private fun saveRecord(key: String, record: JSONObject) {
        val records = readRecords(key)
        records.put(record)
        saveRecords(key, records)
    }

    private fun showHome() {
        makeRoot("АвтоМастер")

        addText("Управление автосервисом", 20, true)

        addButton("🚗 Автомобили клиентов") { showCars() }
        addButton("🔧 Прайс-лист работ") { showPrices() }
        addButton("📋 Заказ-наряды") { showOrders() }
        addButton("🩺 Диагностика") { showDiagnostics() }

        addText("Данные хранятся локально на этом телефоне.")
        addText("Цены ориентировочные. Перед ремонтом согласуйте стоимость с клиентом.")
    }

    private fun showCars() {
        makeRoot("Автомобили клиентов")

        addButton("+ Добавить автомобиль") { addCarDialog() }

        val cars = readRecords("cars")

        if (cars.length() == 0) {
            addText("Автомобили ещё не добавлены.")
        }

        for (i in cars.length() - 1 downTo 0) {
            val car = cars.getJSONObject(i)
            val title = "${car.optString("brand")} ${car.optString("model")}"

            addButton(title) { showCarDetails(car) }
            addText(
                "Год: ${car.optString("year")}\n" +
                "Номер: ${car.optString("plate")}\n" +
                "Владелец: ${car.optString("owner")}\n" +
                "Телефон: ${car.optString("phone")}"
            )

            addButton("Удалить автомобиль") {
                AlertDialog.Builder(this)
                    .setMessage("Удалить автомобиль?")
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton("Удалить") { _, _ ->
                        val updated = JSONArray()
                        for (j in 0 until cars.length()) {
                            if (i != j) updated.put(cars.getJSONObject(j))
                        }
                        saveRecords("cars", updated)
                        showCars()
                    }
                    .show()
            }
        }
    }

    private fun showCarDetails(car: JSONObject) {
        val description =
            "${car.optString("brand")} ${car.optString("model")}\n" +
            "Год выпуска: ${car.optString("year")}\n" +
            "Госномер: ${car.optString("plate")}\n" +
            "Владелец: ${car.optString("owner")}\n" +
            "Телефон: ${car.optString("phone")}\n" +
            "VIN: ${car.optString("vin")}"

        AlertDialog.Builder(this)
            .setTitle("Карточка автомобиля")
            .setMessage(description)
            .setNegativeButton("Закрыть", null)
            .setPositiveButton("Создать заказ") { _, _ ->
                val carName =
                    "${car.optString("brand")} ${car.optString("model")} " +
                    car.optString("plate")
                addOrderDialog(carName, car.optString("owner"))
            }
            .show()
    }

    private fun addCarDialog() {
        val fields = listOf(
            "Марка" to "",
            "Модель" to "",
            "Год выпуска" to "",
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

            val car = JSONObject()
            val keys = listOf(
                "brand", "model", "year", "plate",
                "owner", "phone", "vin"
            )

            keys.forEachIndexed { index, key ->
                car.put(key, values[index])
            }

            saveRecord("cars", car)
            showCars()
            toast("Автомобиль сохранён")
        }
    }

    private fun showPrices() {
        makeRoot("Прайс-лист")

        addText("Ориентировочные цены на работы, ₽", 18, true)

        services.forEach { service ->
            addText("${service.name} — ${service.price} ₽")
        }

        addButton("Создать заказ-наряд") {
            addOrderDialog()
        }
    }

    private fun showOrders() {
        makeRoot("Заказ-наряды")
        addButton("+ Новый заказ-наряд") { addOrderDialog() }

        val orders = readRecords("orders")

        if (orders.length() == 0) {
            addText("Заказ-нарядов пока нет.")
        }

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
                    .setMessage("Удалить этот заказ-наряд?")
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton("Удалить") { _, _ ->
                        val updated = JSONArray()
                        for (j in 0 until orders.length()) {
                            if (j != i) updated.put(orders.getJSONObject(j))
                        }
                        saveRecords("orders", updated)
                        showOrders()
                    }
                    .show()
            }
        }
    }

    private fun addOrderDialog(
        initialCar: String = "",
        initialCustomer: String = ""
    ) {
        val number = (readRecords("orders").length() + 1)
            .toString().padStart(3, '0')

        val fields = listOf(
            "Номер заказ-наряда" to number,
            "ФИО клиента" to initialCustomer,
            "Автомобиль, госномер" to initialCar,
            "Перечень работ" to "",
            "Стоимость работ, ₽" to "0",
            "Статус" to "Принят",
            "Дата" to SimpleDateFormat(
                "dd.MM.yyyy", Locale.getDefault()
            ).format(Date())
        )

        showForm("Новый заказ-наряд", fields) { values ->
            if (values[1].isBlank() || values[3].isBlank()) {
                toast("Заполни клиента и перечень работ")
                return@showForm
            }

            val price = values[4].replace(',', '.').toDoubleOrNull()
            if (price == null || price < 0) {
                toast("Проверь стоимость")
                return@showForm
            }

            val order = JSONObject()
            val keys = listOf(
                "number", "customer", "car", "works",
                "price", "status", "date"
            )

            values.forEachIndexed { index, value ->
                order.put(
                    keys[index],
                    if (index == 4) price.toString() else value
                )
            }

            saveRecord("orders", order)
            showOrders()
            toast("Заказ-наряд сохранён")
        }
    }

    private fun showDiagnostics() {
        makeRoot("Диагностика")

        addButton("+ Новая запись диагностики") {
            addDiagnosticDialog()
        }

        val records = readRecords("diagnostics")

        if (records.length() == 0) {
            addText("Записей диагностики пока нет.")
        }

        for (i in records.length() - 1 downTo 0) {
            val item = records.getJSONObject(i)
            val description =
                "Автомобиль: ${item.optString("car")}\n" +
                "Симптомы: ${item.optString("symptoms")}\n" +
                "Данные сканера: ${item.optString("data")}\n" +
                "Заключение: ${item.optString("conclusion")}\n" +
                "Дата: ${item.optString("date")}"

            addText(description, 16, true)
            addButton("Поделиться") { shareText(description) }

            addButton("Удалить запись") {
                AlertDialog.Builder(this)
                    .setMessage("Удалить результат диагностики?")
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton("Удалить") { _, _ ->
                        val updated = JSONArray()
                        for (j in 0 until records.length()) {
                            if (i != j) updated.put(records.getJSONObject(j))
                        }
                        saveRecords("diagnostics", updated)
                        showDiagnostics()
                    }
                    .show()
            }
        }
    }

    private fun addDiagnosticDialog() {
        val fields = listOf(
            "Автомобиль и двигатель" to "",
            "Жалобы и симптомы" to "",
            "Коды ошибок и показания сканера" to "",
            "Выводы и рекомендации" to ""
        )

        showForm("Новая диагностика", fields) { values ->
            if (values[0].isBlank()) {
                toast("Укажи автомобиль")
                return@showForm
            }

            val record = JSONObject()
            val keys = listOf("car", "symptoms", "data", "conclusion")
            keys.forEachIndexed { index, key ->
                record.put(key, values[index])
            }
            record.put(
                "date",
                SimpleDateFormat(
                    "dd.MM.yyyy HH:mm",
                    Locale.getDefault()
                ).format(Date())
            )

            saveRecord("diagnostics", record)
            showDiagnostics()
            toast("Диагностика сохранена")
        }
    }

    private fun showForm(
        title: String,
        fields: List<Pair<String, String>>,
        onSave: (List<String>) -> Unit
    ) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 8, 24, 8)
        }

        val inputs = fields.map { (hintText, initial) ->
            EditText(this).apply {
                hint = hintText
                setText(initial)
                maxLines = 4
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
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    val values = inputs.map {
                        it.text.toString().trim()
                    }
                    onSave(values)
                    if (dialog.isShowing) dialog.dismiss()
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
