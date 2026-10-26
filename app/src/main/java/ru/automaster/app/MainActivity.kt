package ru.automaster.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

    private val navy = Color.rgb(22, 35, 56)
    private val orange = Color.rgb(245, 139, 35)
    private val pale = Color.rgb(243, 246, 250)
    private val ink = Color.rgb(38, 48, 64)
    private val muted = Color.rgb(105, 117, 133)

    private data class Service(
        val category: String,
        val name: String,
        val price: Int
    )

    private val services = listOf(
        Service("Диагностика", "Компьютерная диагностика", 1500),
        Service("Диагностика", "Проверка подвески", 1000),
        Service("Диагностика", "Поиск неисправности электрики", 1800),

        Service("ТО", "Замена масла двигателя", 1200),
        Service("ТО", "Замена масляного фильтра", 300),
        Service("ТО", "Замена воздушного фильтра", 400),
        Service("ТО", "Замена салонного фильтра", 600),
        Service("ТО", "Замена свечей зажигания", 1000),
        Service("ТО", "Замена топливного фильтра", 1200),
        Service("ТО", "Замена приводного ремня", 1500),

        Service("Тормоза", "Замена передних тормозных колодок", 1800),
        Service("Тормоза", "Замена задних тормозных колодок", 2000),
        Service("Тормоза", "Замена тормозного диска, одна сторона", 1500),
        Service("Тормоза", "Замена тормозной жидкости", 1200),

        Service("Охлаждение", "Замена антифриза", 1800),
        Service("Охлаждение", "Замена термостата", 2500),

        Service("Двигатель", "Замена ремня ГРМ", 6500),
        Service("Двигатель", "Регулировка клапанов", 3000),
        Service("Двигатель", "Снятие и установка ГБЦ", 12000),
        Service("Двигатель", "Замена прокладки ГБЦ", 18000),
        Service("Двигатель", "Замена двигателя", 25000),

        Service("Электрика", "Замена аккумулятора", 500),
        Service("Электрика", "Замена стартера", 2500),
        Service("Электрика", "Замена генератора", 2500),

        Service("Подвеска", "Замена амортизатора, одна сторона", 2500),
        Service("Подвеска", "Замена шаровой опоры", 1800),
        Service("Подвеска", "Замена рулевого наконечника", 1200),
        Service("Подвеска", "Замена ступичного подшипника", 3500),

        Service("Трансмиссия", "Замена сцепления", 9000),
        Service("Трансмиссия", "Замена АКПП", 18000),
        Service("Трансмиссия", "Замена МКПП", 12000),

        Service("Регулировки", "Развал-схождение", 2500)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = navy
        window.navigationBarColor = navy

        showHome()
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun rounded(color: Int, radius: Int = 14) =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }

    private fun makeRoot(
        title: String,
        subtitle: String = "УЧЁТ АВТОСЕРВИСА"
    ) {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(26))
            setBackgroundColor(pale)
        }

        val scroll = ScrollView(this).apply {
            isFillViewport = true
        }

        scroll.addView(root)
        setContentView(scroll)

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(16), dp(18), dp(18))
            background = rounded(navy, 18)
        }

        header.addView(TextView(this).apply {
            text = title
            textSize = 25f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
        })

        header.addView(TextView(this).apply {
            text = subtitle
            textSize = 11f
            setTextColor(Color.rgb(205, 215, 229))
            setPadding(0, dp(5), 0, 0)
        })

        root.addView(
            header,
            LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = dp(12)
            }
        )

        if (title != "АвтоМастер") {
            addButton("← На главную") {
                showHome()
            }
        }
    }

    private fun addText(
        value: String,
        size: Int = 15,
        bold: Boolean = false,
        color: Int = ink
    ) {
        root.addView(TextView(this).apply {
            text = value
            textSize = size.toFloat()
            setTextColor(color)

            if (bold) {
                setTypeface(null, Typeface.BOLD)
            }

            setPadding(dp(2), dp(6), dp(2), dp(6))
        })
    }

    private fun addButton(
        title: String,
        action: () -> Unit
    ) {
        root.addView(
            Button(this).apply {
                text = title
                isAllCaps = false
                textSize = 14f
                setTextColor(Color.WHITE)

                background = rounded(
                    if (title.startsWith("Удалить")) {
                        Color.rgb(180, 62, 62)
                    } else {
                        navy
                    },
                    12
                )

                minHeight = dp(46)
                setPadding(dp(10), dp(5), dp(10), dp(5))

                setOnClickListener {
                    action()
                }
            },
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(3)
                bottomMargin = dp(3)
            }
        )
    }

    private fun addCard(
        title: String,
        detail: String,
        action: () -> Unit
    ) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15), dp(13), dp(15), dp(13))
            background = rounded(Color.WHITE, 14)
            elevation = dp(2).toFloat()
            isClickable = true
            isFocusable = true

            setOnClickListener {
                action()
            }
        }

        card.addView(TextView(this).apply {
            text = title
            textSize = 17f
            setTextColor(navy)
            setTypeface(null, Typeface.BOLD)
        })

        card.addView(TextView(this).apply {
            text = detail
            textSize = 13f
            setTextColor(muted)
            setPadding(0, dp(5), 0, 0)
        })

        root.addView(
            card,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(5)
                bottomMargin = dp(5)
            }
        )
    }

    private fun readRecords(key: String): JSONArray {
        return try {
            JSONArray(prefs.getString(key, "[]") ?: "[]")
        } catch (_: Exception) {
            JSONArray()
        }
    }

    private fun saveRecords(
        key: String,
        records: JSONArray
    ) {
        prefs.edit()
            .putString(key, records.toString())
            .apply()
    }

    private fun saveRecord(
        key: String,
        record: JSONObject
    ) {
        val records = readRecords(key)
        records.put(record)
        saveRecords(key, records)
    }

    private fun watch(
        edit: EditText,
        changed: () -> Unit
    ) {
        edit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                changed()
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun money(amount: Int): String =
        String.format(
            Locale("ru", "RU"),
            "%,d ₽",
            amount
        ).replace(',', ' ')

    private fun today(): String =
        SimpleDateFormat(
            "dd.MM.yyyy",
            Locale.getDefault()
        ).format(Date())

    private fun now(): String =
        SimpleDateFormat(
            "dd.MM.yyyy HH:mm",
            Locale.getDefault()
        ).format(Date())

    // Главный экран
    private fun showHome() {
        makeRoot(
            "АвтоМастер",
            "ПРОФЕССИОНАЛЬНЫЙ УЧЁТ АВТОСЕРВИСА"
        )

        val cars = readRecords("cars").length()
        val orders = readRecords("orders")

        var total = 0

        for (i in 0 until orders.length()) {
            total += orders.optJSONObject(i)
                ?.optString("price")
                ?.toDoubleOrNull()
                ?.toInt() ?: 0
        }

        val stats = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = rounded(Color.WHITE, 16)
        }

        stats.addView(TextView(this).apply {
            text = "РАБОЧИЙ СТОЛ"
            textSize = 11f
            setTextColor(muted)
        })

        stats.addView(TextView(this).apply {
            text = "$cars автомобилей  •  ${orders.length()} заказов"
            textSize = 18f
            setTextColor(navy)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(7), 0, dp(4))
        })

        stats.addView(TextView(this).apply {
            text = "Сумма заказов: ${money(total)}"
            textSize = 15f
            setTextColor(orange)
            setTypeface(null, Typeface.BOLD)
        })

        root.addView(
            stats,
            LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = dp(12)
            }
        )

        addButton("Клиенты и автомобили") {
            showCars()
        }

        addButton("Заказ-наряды и расчёт стоимости") {
            showOrders()
        }

                addButton("Прайс-лист работ") {
            showPrices()
        }

        addButton("Справочник ремонта двигателей") {
            startActivity(
                Intent(this, RepairReferenceActivity::class.java)
            )
        }

        addButton("История обслуживания") {
            showHistory()
        }
        addButton("Диагностика") {
            showDiagnostics()
        }

        // Новая функция резервного копирования
        addButton("Резервная копия данных") {
            exportBackup()
        }

        addText(
            "Работает офлайн. Данные хранятся на этом телефоне.",
            12,
            false,
            muted
        )

        addText(
            "Цены ориентировочные для Красноярского края. " +
                "Это стоимость работ без запчастей; уточняйте цену до ремонта.",
            12,
            false,
            muted
        )
    }

    // Клиенты и автомобили
    private fun showCars() {
        makeRoot("Клиенты и автомобили")

        addButton("+ Добавить клиента / автомобиль") {
            addCarDialog()
        }

        val cars = readRecords("cars")

        if (cars.length() == 0) {
            addText(
                "Пока нет клиентов. Добавьте первую карточку.",
                14,
                false,
                muted
            )
        }

        for (i in cars.length() - 1 downTo 0) {
            val car = cars.optJSONObject(i) ?: continue

            val name =
                "${car.optString("brand")} ${car.optString("model")}".trim()

            val details =
                "Владелец: ${car.optString("owner")}\n" +
                "Госномер: ${car.optString("plate")} • " +
                "Год: ${car.optString("year")}\n" +
                "Телефон: ${car.optString("phone")}"

            addCard(
                name.ifBlank { "Автомобиль" },
                details
            ) {
                showCarDetails(car)
            }

            addButton("Удалить: $name") {
                AlertDialog.Builder(this)
                    .setMessage(
                        "Удалить карточку автомобиля? " +
                            "История заказов сохранится."
                    )
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton("Удалить") { _, _ ->
                        val updated = JSONArray()

                        for (j in 0 until cars.length()) {
                            if (j != i) {
                                updated.put(cars.getJSONObject(j))
                            }
                        }

                        saveRecords("cars", updated)
                        showCars()
                    }
                    .show()
            }
        }
    }

    private fun showCarDetails(car: JSONObject) {
        val carName =
            "${car.optString("brand")} ${car.optString("model")} " +
                car.optString("plate")

        val description =
            "$carName\n" +
            "Владелец: ${car.optString("owner")}\n" +
            "Телефон: ${car.optString("phone")}\n" +
            "Год: ${car.optString("year")}\n" +
            "VIN: ${car.optString("vin")}"

        AlertDialog.Builder(this)
            .setTitle("Карточка автомобиля")
            .setMessage(description)
            .setNegativeButton("Закрыть", null)
            .setPositiveButton("Новый заказ") { _, _ ->
                addOrderDialog(
                    carName.trim(),
                    car.optString("owner")
                )
            }
            .setNeutralButton("История") { _, _ ->
                showHistory(carName.trim())
            }
            .show()
    }

    private fun addCarDialog() {
        val fields = listOf(
            "Марка" to "",
            "Модель" to "",
            "Год выпуска" to "",
            "Госномер" to "",
            "ФИО клиента" to "",
            "Телефон" to "",
            "VIN" to ""
        )

        showForm("Новый клиент и автомобиль", fields) { values ->
            if (values[0].isBlank() || values[1].isBlank()) {
                toast("Укажи марку и модель")
                return@showForm
            }

            val car = JSONObject()

            listOf(
                "brand",
                "model",
                "year",
                "plate",
                "owner",
                "phone",
                "vin"
            ).forEachIndexed { index, key ->
                car.put(key, values[index])
            }

            car.put("createdAt", today())

            saveRecord("cars", car)
            showCars()
            toast("Карточка сохранена")
        }
    }

    // Прайс-лист
    private fun showPrices() {
        makeRoot(
            "Прайс-лист",
            "КРАСНОЯРСКИЙ КРАЙ • ОРИЕНТИРОВОЧНЫЕ ЦЕНЫ"
        )

        addText(
            "Стоимость работ без запчастей. Поиск по названию.",
            12,
            false,
            muted
        )

        val search = EditText(this).apply {
            hint = "Например, замена масла"
            textSize = 15f
            setSingleLine(true)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = rounded(Color.WHITE, 12)
        }

        root.addView(
            search,
            LinearLayout.LayoutParams(-1, -2).apply {
                topMargin = dp(8)
                bottomMargin = dp(8)
            }
        )

        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        root.addView(list)

        fun render(query: String) {
            list.removeAllViews()

            val filtered = services.filter {
                it.name.contains(query, true) ||
                    it.category.contains(query, true)
            }

            if (filtered.isEmpty()) {
                list.addView(TextView(this).apply {
                    text = "Ничего не найдено"
                    setTextColor(muted)
                    setPadding(0, dp(12), 0, dp(12))
                })
            }

            filtered.forEach { service ->
                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(14), dp(10), dp(14), dp(10))
                    background = rounded(Color.WHITE, 12)
                }

                card.addView(TextView(this).apply {
                    text = service.category.uppercase(Locale.getDefault())
                    textSize = 10f
                    setTextColor(orange)
                    setTypeface(null, Typeface.BOLD)
                })

                card.addView(TextView(this).apply {
                    text = service.name
                    textSize = 14f
                    setTextColor(ink)
                    setPadding(0, dp(3), 0, dp(3))
                })

                card.addView(TextView(this).apply {
                    text = money(service.price)
                    textSize = 16f
                    setTextColor(navy)
                    setTypeface(null, Typeface.BOLD)
                })

                list.addView(
                    card,
                    LinearLayout.LayoutParams(-1, -2).apply {
                        topMargin = dp(4)
                        bottomMargin = dp(4)
                    }
                )
            }
        }

        watch(search) {
            render(search.text.toString())
        }

        render("")

        addButton("Создать заказ-наряд") {
            addOrderDialog()
        }
    }

    // Заказ-наряды
    private fun showOrders() {
        makeRoot("Заказ-наряды")

        addButton("+ Новый заказ-наряд") {
            addOrderDialog()
        }

        val orders = readRecords("orders")

        if (orders.length() == 0) {
            addText(
                "Заказов пока нет. Создайте заказ — стоимость " +
                    "рассчитается по выбранным работам.",
                14,
                false,
                muted
            )
        }

        for (i in orders.length() - 1 downTo 0) {
            val order = orders.optJSONObject(i) ?: continue
            val description = orderDescription(order)

            val amount = order.optString("price")
                .toDoubleOrNull()
                ?.toInt() ?: 0

            addCard(
                "Заказ № ${order.optString("number")} • " +
                    order.optString("status"),
                "${order.optString("customer")}\n" +
                    "${order.optString("car")}\n" +
                    "${order.optString("date")}\n" +
                    "Итого: ${money(amount)}"
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Заказ-наряд")
                    .setMessage(description)
                    .setNegativeButton("Закрыть", null)
                    .setPositiveButton("Поделиться") { _, _ ->
                        shareText(description)
                    }
                    .setNeutralButton("История авто") { _, _ ->
                        showHistory(order.optString("car"))
                    }
                    .show()
            }

            addButton("Поделиться заказом № ${order.optString("number")}") {
                shareText(description)
            }

            addButton("Удалить заказ № ${order.optString("number")}") {
                AlertDialog.Builder(this)
                    .setMessage("Удалить заказ-наряд?")
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton("Удалить") { _, _ ->
                        val updated = JSONArray()

                        for (j in 0 until orders.length()) {
                            if (j != i) {
                                updated.put(orders.getJSONObject(j))
                            }
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
        val chosen = linkedMapOf<String, Int>()

        val fields = listOf(
            "Номер заказ-наряда" to nextOrderNumber(),
            "ФИО клиента" to initialCustomer,
            "Автомобиль, госномер" to initialCar,
            "Дополнительная работа (название)" to "",
            "Цена дополнительной работы, ₽" to "0",
            "Скидка, ₽" to "0",
            "Статус: Принят / В работе / Готов / Выдан" to "Принят",
            "Комментарий" to ""
        )

        val wrap = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(8), dp(16), dp(8))
        }

        wrap.addView(TextView(this).apply {
            text = "Выбери работы — итоговая сумма пересчитается автоматически."
            textSize = 13f
            setTextColor(muted)
            setPadding(0, 0, 0, dp(6))
        })

        val search = EditText(this).apply {
            hint = "Поиск работы"
            setSingleLine(true)
            setPadding(dp(10), dp(8), dp(10), dp(8))
            background = rounded(Color.WHITE, 10)
        }

        wrap.addView(search)

        val serviceList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        wrap.addView(serviceList)

        val priceLabel = TextView(this).apply {
            text = "Итого: 0 ₽"
            textSize = 18f
            setTextColor(navy)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(8), 0, dp(8))
        }

        wrap.addView(priceLabel)

        val inputs = fields.map { (hintText, initial) ->
            EditText(this).apply {
                hint = hintText
                setText(initial)
                maxLines = 3
                setPadding(dp(8), dp(8), dp(8), dp(8))
                wrap.addView(this)
            }
        }

        fun total(): Int {
            val extra =
                inputs[4].text.toString().toIntOrNull() ?: 0

            val discount =
                inputs[5].text.toString().toIntOrNull() ?: 0

            return (
                chosen.values.sum() + extra - discount
            ).coerceAtLeast(0)
        }

        fun updateTotal() {
            priceLabel.text = "Итого: ${money(total())}"
        }

        fun renderServices(query: String) {
            serviceList.removeAllViews()

            services.filter {
                it.name.contains(query, true) ||
                    it.category.contains(query, true)
            }.forEach { service ->
                val cb = CheckBox(this).apply {
                    text = "${service.name} — ${service.price} ₽"
                    textSize = 12f
                    setTextColor(ink)
                    isChecked = chosen.containsKey(service.name)

                    setOnCheckedChangeListener { _, checked ->
                        if (checked) {
                            chosen[service.name] = service.price
                        } else {
                            chosen.remove(service.name)
                        }

                        updateTotal()
                    }
                }

                serviceList.addView(cb)
            }

            updateTotal()
        }

        watch(search) {
            renderServices(search.text.toString())
        }

        watch(inputs[4]) {
            updateTotal()
        }

        watch(inputs[5]) {
            updateTotal()
        }

        renderServices("")

        val dialogScroll = ScrollView(this).apply {
            addView(wrap)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Новый заказ-наряд")
            .setView(dialogScroll)
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Сохранить", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    if (
                        inputs[1].text.isBlank() ||
                        inputs[2].text.isBlank()
                    ) {
                        toast("Укажи клиента и автомобиль")
                        return@setOnClickListener
                    }

                    val extraName =
                        inputs[3].text.toString().trim()

                    val extraPrice =
                        inputs[4].text.toString().toIntOrNull() ?: 0

                    val discount =
                        inputs[5].text.toString().toIntOrNull() ?: 0

                    if (extraPrice < 0 || discount < 0) {
                        toast("Цена и скидка не могут быть отрицательными")
                        return@setOnClickListener
                    }

                    val workNames = chosen.keys.toMutableList()

                    if (extraName.isNotBlank()) {
                        workNames.add(extraName)
                    }

                    if (workNames.isEmpty()) {
                        toast("Выбери работу или укажи дополнительную")
                        return@setOnClickListener
                    }

                    if (extraPrice > 0 && extraName.isBlank()) {
                        toast("Укажи название дополнительной работы")
                        return@setOnClickListener
                    }

                    val order = JSONObject()

                    order.put("number", inputs[0].text.toString())
                    order.put("customer", inputs[1].text.toString())
                    order.put("car", inputs[2].text.toString())
                    order.put("works", workNames.joinToString(", "))
                    order.put("items", JSONArray(chosen.keys.toList()))
                    order.put("price", total().toString())
                    order.put("discount", discount)

                    order.put(
                        "status",
                        inputs[6].text.toString().ifBlank { "Принят" }
                    )

                    order.put("comment", inputs[7].text.toString())
                    order.put("date", today())

                    saveRecord("orders", order)

                    dialog.dismiss()
                    showOrders()

                    toast("Заказ сохранён: ${money(total())}")
                }
        }

        dialog.show()
    }

    // История обслуживания
    private fun showHistory(carFilter: String = "") {
        makeRoot(
            "История обслуживания",
            "ЗАКАЗЫ И РЕМОНТЫ ПО АВТОМОБИЛЯМ"
        )

        val search = EditText(this).apply {
            hint = "Поиск по авто, владельцу, госномеру"
            setText(carFilter)
            setSingleLine(true)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            background = rounded(Color.WHITE, 12)
        }

        root.addView(search)

        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        root.addView(list)

        fun render(query: String) {
            list.removeAllViews()

            val orders = readRecords("orders")
            var count = 0

            for (i in orders.length() - 1 downTo 0) {
                val order = orders.optJSONObject(i) ?: continue

                val searchable =
                    "${order.optString("car")} " +
                    "${order.optString("customer")} " +
                    "${order.optString("works")}"

                if (
                    query.isNotBlank() &&
                    !searchable.contains(query, true)
                ) {
                    continue
                }

                count++

                val amount = order.optString("price")
                    .toDoubleOrNull()
                    ?.toInt() ?: 0

                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(14), dp(12), dp(14), dp(12))
                    background = rounded(Color.WHITE, 12)
                }

                card.addView(TextView(this).apply {
                    text =
                        "${order.optString("date")} • " +
                        "Заказ № ${order.optString("number")}"

                    textSize = 11f
                    setTextColor(muted)
                })

                card.addView(TextView(this).apply {
                    text = order.optString("car")
                    textSize = 16f
                    setTextColor(navy)
                    setTypeface(null, Typeface.BOLD)
                    setPadding(0, dp(4), 0, dp(4))
                })

                card.addView(TextView(this).apply {
                    text =
                        "Клиент: ${order.optString("customer")}\n" +
                        "Работы: ${order.optString("works")}\n" +
                        "Статус: ${order.optString("status")}"

                    textSize = 13f
                    setTextColor(ink)
                })

                card.addView(TextView(this).apply {
                    text = money(amount)
                    textSize = 16f
                    setTextColor(orange)
                    setTypeface(null, Typeface.BOLD)
                    setPadding(0, dp(5), 0, 0)
                })

                list.addView(
                    card,
                    LinearLayout.LayoutParams(-1, -2).apply {
                        topMargin = dp(5)
                        bottomMargin = dp(5)
                    }
                )
            }

            if (count == 0) {
                list.addView(TextView(this).apply {
                    text = if (query.isBlank()) {
                        "История появится после сохранения заказ-нарядов."
                    } else {
                        "По запросу ничего не найдено."
                    }

                    setTextColor(muted)
                    setPadding(0, dp(12), 0, dp(12))
                })
            }
        }

        watch(search) {
            render(search.text.toString())
        }

        render(carFilter)
    }

    // Диагностика
    private fun showDiagnostics() {
        makeRoot("Диагностика")

        addButton("+ Новая запись диагностики") {
            addDiagnosticDialog()
        }

        val records = readRecords("diagnostics")

        if (records.length() == 0) {
            addText(
                "Записей диагностики пока нет.",
                14,
                false,
                muted
            )
        }

        for (i in records.length() - 1 downTo 0) {
            val item = records.optJSONObject(i) ?: continue

            val description =
                "Автомобиль: ${item.optString("car")}\n" +
                "Симптомы: ${item.optString("symptoms")}\n" +
                "Данные сканера: ${item.optString("data")}\n" +
                "Заключение: ${item.optString("conclusion")}\n" +
                "Дата: ${item.optString("date")}"

            addCard(
                item.optString("car"),
                "Дата: ${item.optString("date")}\n" +
                    item.optString("symptoms")
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Диагностика")
                    .setMessage(description)
                    .setNegativeButton("Закрыть", null)
                    .setPositiveButton("Поделиться") { _, _ ->
                        shareText(description)
                    }
                    .show()
            }

            addButton("Удалить запись диагностики") {
                AlertDialog.Builder(this)
                    .setMessage("Удалить запись?")
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton("Удалить") { _, _ ->
                        val updated = JSONArray()

                        for (j in 0 until records.length()) {
                            if (j != i) {
                                updated.put(records.getJSONObject(j))
                            }
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
            "Коды ошибок и данные сканера" to "",
            "Заключение и рекомендации" to ""
        )

        showForm("Новая диагностика", fields) { values ->
            if (values[0].isBlank()) {
                toast("Укажи автомобиль")
                return@showForm
            }

            val item = JSONObject()

            listOf(
                "car",
                "symptoms",
                "data",
                "conclusion"
            ).forEachIndexed { index, key ->
                item.put(key, values[index])
            }

            item.put("date", now())

            saveRecord("diagnostics", item)
            showDiagnostics()

            toast("Диагностика сохранена")
        }
    }

    // Универсальная форма
    private fun showForm(
        title: String,
        fields: List<Pair<String, String>>,
        onSave: (List<String>) -> Unit
    ) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(8), dp(16), dp(8))
        }

        val inputs = fields.map { (hintText, initial) ->
            EditText(this).apply {
                hint = hintText
                setText(initial)
                maxLines = 3
                setPadding(dp(8), dp(10), dp(8), dp(10))
                container.addView(this)
            }
        }

        val scroll = ScrollView(this).apply {
            addView(container)
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setView(scroll)
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Сохранить", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    onSave(
                        inputs.map {
                            it.text.toString().trim()
                        }
                    )

                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                }
        }

        dialog.show()
    }

    // Формирование текста заказ-наряда
    private fun orderDescription(order: JSONObject): String {
        val amount = order.optString("price")
            .toDoubleOrNull()
            ?.toInt() ?: 0

        return "АвтоМастер — заказ-наряд № " +
            "${order.optString("number")}\n" +
            "Дата: ${order.optString("date")}\n" +
            "Клиент: ${order.optString("customer")}\n" +
            "Автомобиль: ${order.optString("car")}\n" +
            "Работы: ${order.optString("works")}\n" +
            "Стоимость работ: ${money(amount)}\n" +
            "Статус: ${order.optString("status")}\n" +
            "Комментарий: ${order.optString("comment")}"
    }

    private fun nextOrderNumber(): String {
        var max = 0
        val orders = readRecords("orders")

        for (i in 0 until orders.length()) {
            val number = orders.optJSONObject(i)
                ?.optString("number")
                ?.toIntOrNull() ?: 0

            max = maxOf(max, number)
        }

        return (max + 1).toString().padStart(4, '0')
    }

    // Резервная копия автомобилей, заказов и диагностики
    private fun exportBackup() {
        val backup = JSONObject().apply {
            put("app", "АвтоМастер")
            put("formatVersion", 1)
            put("exportedAt", now())
            put("cars", readRecords("cars"))
            put("orders", readRecords("orders"))
            put("diagnostics", readRecords("diagnostics"))
        }

        shareText(backup.toString(2))
        toast("Резервная копия подготовлена")
    }

    // Поделиться текстом или резервной копией
    private fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }

        startActivity(
            Intent.createChooser(intent, "Поделиться")
        )
    }

    private fun toast(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}