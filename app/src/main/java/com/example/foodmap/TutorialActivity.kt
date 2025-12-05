package com.example.foodmap

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

// Classe de dados
data class TutorialStep(
    val imageRes: Int,
    val title: String,
    val description: String
)

class TutorialActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var layoutIndicators: LinearLayout
    private lateinit var tvDescription: TextView
    private lateinit var btnNext: androidx.appcompat.widget.AppCompatButton
    private lateinit var btnSkip: TextView

    // --- LISTA DE DADOS ---
    private val tutorialSteps = listOf(
        TutorialStep(
            imageRes = R.drawable.img_welcome_pizza,
            title = "SEJA\nBEM\nVINDO!!",
            description = "Bem-vindo ao FoodMap! Gerencie sua alimentação de forma inteligente e prática."
        ),
        TutorialStep(
            imageRes = R.drawable.print_weekly,
            title = "",
            description = "Acompanhe seu progresso diário e semanal e visualize suas metas de calorias."
        ),
        TutorialStep(
            imageRes = R.drawable.print_scanner,
            title = "",
            description = "Use o Scanner Inteligente para identificar alimentos e obter informações nutricionais instantaneamente."
        ),
        TutorialStep(
            imageRes = R.drawable.print_add_meal,
            title = "",
            description = "Adicione refeições manualmente ou edite os detalhes para manter seu diário sempre preciso."
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        initViews()
        setupViewPager()
        setupIndicators()
        setupListeners()
    }

    private fun initViews() {
        viewPager = findViewById(R.id.viewPagerTutorial)
        layoutIndicators = findViewById(R.id.layoutIndicators)
        tvDescription = findViewById(R.id.tvDescription)
        btnNext = findViewById(R.id.btnNext)
        btnSkip = findViewById(R.id.btnSkip)
    }

    private fun setupViewPager() {
        val adapter = TutorialAdapter(tutorialSteps)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateIndicators(position)
                updateUIForPage(position)
            }
        })
    }

    private fun updateUIForPage(position: Int) {
        tvDescription.text = tutorialSteps[position].description

        // Se for a última página, muda o texto do botão
        if (position == tutorialSteps.size - 1) {
            btnNext.text = "Começar"
        } else {
            btnNext.text = "Próximo"
        }
    }

    private fun setupIndicators() {
        val indicators = arrayOfNulls<ImageView>(tutorialSteps.size)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(10, 0, 10, 0)

        for (i in indicators.indices) {
            indicators[i] = ImageView(this)
            layoutIndicators.addView(indicators[i], layoutParams)
        }
        updateIndicators(0)
    }

    private fun updateIndicators(position: Int) {
        val childCount = layoutIndicators.childCount
        for (i in 0 until childCount) {
            val imageView = layoutIndicators.getChildAt(i) as ImageView
            if (i == position) {
                // Bolinha Ativa (Laranja)
                imageView.setImageResource(android.R.drawable.radiobutton_on_background)
                imageView.setColorFilter(ContextCompat.getColor(this, R.color.orange))
            } else {
                // Bolinha Inativa (Cinza)
                imageView.setImageResource(android.R.drawable.radiobutton_off_background)
                imageView.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray))
            }
        }
    }

    private fun setupListeners() {
        btnNext.setOnClickListener {
            if (viewPager.currentItem + 1 < tutorialSteps.size) {
                viewPager.currentItem += 1
            } else {
                finishTutorial()
            }
        }

        btnSkip.setOnClickListener {
            finishTutorial()
        }
    }

    private fun finishTutorial() {
        // Criamos a intenção de ir para a tela de Login
        val intent = Intent(this, Login::class.java)

        // Iniciamos a tela
        startActivity(intent)

        // Fechamos o tutorial para não voltar para ele com o botão 'voltar'
        finish()
    }

    // --- ADAPTER ---
    inner class TutorialAdapter(private val steps: List<TutorialStep>) :
        RecyclerView.Adapter<TutorialAdapter.TutorialViewHolder>() {

        inner class TutorialViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imgSlide: ImageView = view.findViewById(R.id.imgSlide)
            val tvSlideTitle: TextView = view.findViewById(R.id.tvSlideTitle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorialViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_tutorial_page, parent, false)
            return TutorialViewHolder(view)
        }

        override fun onBindViewHolder(holder: TutorialViewHolder, position: Int) {
            val step = steps[position]
            holder.imgSlide.setImageResource(step.imageRes)

            if (step.title.isNotEmpty()) {
                holder.tvSlideTitle.text = step.title
                holder.tvSlideTitle.visibility = View.VISIBLE
            } else {
                holder.tvSlideTitle.visibility = View.GONE
            }
        }

        override fun getItemCount(): Int = steps.size
    }
}