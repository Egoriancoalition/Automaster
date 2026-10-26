
package ru.automaster.app

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val ink = Color.rgb(25, 35, 48)
    private val blue = Color.rgb(24, 105, 190)
    private val bg = Color.rgb(245, 247, 250)
    private lateinit var content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ink
        window.navigationBarColor = ink
        showHome()
    }

    private fun base(title: String, subtitle: String) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 22, 22, 20)
            setBackgroundColor(ink)
        }

        header.addView(TextView(this).apply {
            text = "АВТОМАСТЕР"
            textSize = 12f
            setTextColor(Color.rgb(130, 190, 255))
        })

        header.addView(TextView(this).apply {
            text = title
            textSize = 26f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            setPadding(0, 8, 0, 4)
        })

        header.addView(TextView(this).apply {
            text = subtitle
            textSize = 14f
            setTextColor(Color.rgb(210, 220, 232))
        })

        root.addView(header)

        val scroll = ScrollView(this)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 24)
        }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val nav = LinearLayout(this).apply {
            setPadding(4, 4, 4, 4)
            setBackgroundColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        listOf("Главная", "Авто", "Прайс", "Заказ-наряд").forEach { label ->
            nav.addView(Button(this).apply {
                text = label
                textSize = 10f
                isAllCaps = false
                setOnClickListener {
                    when (label) {
                        "Главная" -> showHome()
                        "Авто" -> showCars()
                        "Прайс" -> showPrices()
                        else -> showWorkOrder()
                    }
                }
            }, LinearLayout.LayoutParams(0, -2, 1f))
        }

        root.addView(nav)
        setContentView(root)
    }

    private fun heading(text: String) {
        content.addView(TextView(this).apply {
            this.text = text
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(ink)
            setPadding(2, 14, 2, 8)
        })
    }

    private fun card(
        title: String,
        description: String,
        action: (() -> Unit)? = null
    ) {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 14, 16, 14)
            setBackgroundColor(Color.WHITE)
        }

        box.addView(TextView(this).apply {
            text = title
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(ink)
        })

        box.addView(TextView(this).apply {
            text = description
            textSize = 14f
            setTextColor(Color.rgb(90, 101, 115))
            setPadding(0, 6, 0, 0)
        })

        if (action != null) box.setOnClickListener { action() }

        content.addView(
            box,
            LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = 10
            }
        )
    }

    private fun field(hint: String): EditText {
        val edit = EditText(this).apply {
            this.hint = hint
            textSize = 15f
            setSingleLine(true)
            setPadding(12, 8, 12, 8)
            setBackgroundColor(Color.WHITE)
        }

        content.addView(
            edit,
            LinearLayout.LayoutParams(-1, -2).apply {
                bottomMargin = 10
            }
        )
        return edit
    }

    private fun actionButton(label: String, action: () -> Unit) {
        content.addView(Button(this).apply {
            text = label
            isAllCaps = false
            setTextColor(Color.WHITE)
            setBackgroundColor(blue)
            setOnClickListener { action() }
        }, LinearLayout.LayoutParams(-1, -2).apply {
            bottomMargin = 12
        })
    }

    private fun showHome() {
        base("Рабочее место", "Помощник автослесаря · офлайн")
        heading("Быстрый доступ")

        card("Автомобили", "Карточки клиентов и автомобилей") {
            showCars()
        }
        card("Прайс-лист работ", "Стандартные цены для Красноярского края") {
            showPrices()
        }
        card("Заказ-наряд", "Создание черновика документа") {
            showWorkOrder()
        }
        card("Диагностика", "Коды ошибок и заметки по неисправностям") {
            showDiagnostics()
        }

        heading("О приложении")
        card(
            "АвтоМастер · версия 1.0",
            "Первая версия. Данные пока не сохраняются после закрытия приложения."
        )
    }

    private fun showCars() {
        base("Автомобили", "Карточка автомобиля")
        heading("Данные автомобиля")

        val make = field("Марка, например Toyota")
        val model = field("Модель")
        val year = field("Год выпуска")
        val vin = field("Госномер или VIN")
        val engine = field("Двигатель")
        val note = field("Жалоба клиента")

        actionButton("Сформировать карточку") {
            val values = listOf(
                "Марка: ${make.text}",
                "Модель: ${model.text}",
                "Год: ${year.text}",
                "VIN / номер: ${vin.text}",
                "Двигатель: ${engine.text}",
                "Примечание: ${note.text}"
            ).filterNot { it.substringAfter(": ").isBlank() }

            if (values.isEmpty()) {
                Toast.makeText(
                    this, "Заполни хотя бы марку автомобиля",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                shareText("Карточка автомобиля", values.joinToString("\n"))
            }
        }

        card("Примечание", "Постоянное хранение карточек добавим следующим этапом.")
    }

    private fun showPrices() {
        base("Прайс-лист", "Красноярский край · стандарт")
        heading("Стоимость работ")

        val rows = listOf(
            "Компьютерная диагностика — от 1 000 ₽",
            "Замена масла и фильтра — от 1 200 ₽",
            "Замена воздушного фильтра — от 300 ₽",
            "Замена свечей зажигания — от 800 ₽",
            "Передние тормозные колодки — от 1 500 ₽",
            "Тормозные диски за ось — от 2 500 ₽",
            "Замена масла в АКПП — от 2 500 ₽",
            "Замена ремня ГРМ — от 5 000 ₽",
            "Передний амортизатор — от 2 500 ₽",
            "Поиск неисправности двигателя — от 1 500 ₽"
        )

        rows.forEachIndexed { index, row ->
            card("${index + 1}. $row", "Стоимость работы без запчастей")
        }

        card(
            "Важно",
            "Цены ориентировочные. Перед использованием в автосервисе проверь их по своим расценкам."
        )
    }

    private fun showWorkOrder() {
        base("Заказ-наряд", "Черновик документа")
        heading("Клиент и автомобиль")

        val client = field("ФИО клиента")
        val vehicle = field("Марка, модель, год")
        val plate = field("Госномер")
        val mileage = field("Пробег, км")
        val complaint = field("Жалоба клиента")
        val work = field("Выполненные работы")
        val parts = field("Запчасти и материалы")
        val price = field("Стоимость работ, ₽")

        actionButton("Поделиться заказ-нарядом") {
            val lines = listOf(
                "ЗАКАЗ-НАРЯД",
                "Клиент: ${client.text}",
                "Автомобиль: ${vehicle.text}",
                "Госномер: ${plate.text}",
                "Пробег: ${mileage.text}",
                "Жалоба: ${complaint.text}",
                "Работы: ${work.text}",
                "Материалы: ${parts.text}",
                "Стоимость работ: ${price.text} ₽"
            )
            shareText("Заказ-наряд АвтоМастер", lines.joinToString("\n"))
        }

        card("Статус", "Пока можно отправить черновик текстом. PDF добавим отдельно.")
    }

    private fun showDiagnostics() {
        base("Диагностика", "Заметки по ремонту")
        heading("Запись диагностики")

        val vehicle = field("Автомобиль и двигатель")
        val dtc = field("Коды ошибок OBD-II")
        val symptoms = field("Симптомы")
        val readings = field("Показания сканера")
        val conclusion = field("Предполагаемая причина")

        actionButton("Поделиться диагностикой") {
            val lines = listOf(
                "ДИАГНОСТИКА",
                "Автомобиль: ${vehicle.text}",
                "Коды ошибок: ${dtc.text}",
                "Симптомы: ${symptoms.text}",
                "Показания: ${readings.text}",
                "Предполагаемая причина: ${conclusion.text}"
            )
            shareText("Диагностика АвтоМастер", lines.joinToString("\n"))
        }
    }

    private fun shareText(subject: String, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "Поделиться"))
    }
}
