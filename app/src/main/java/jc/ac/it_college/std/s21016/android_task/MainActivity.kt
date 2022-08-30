package jc.ac.it_college.std.s21016.android_task

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.SimpleAdapter
import android.widget.Toolbar
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.lifecycleScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.picasso.Picasso
import jc.ac.it_college.std.s21016.android_task.databinding.ActivityMainBinding
import jc.ac.it_college.std.s21016.android_task.service.PokemonInfo
import jc.ac.it_college.std.s21016.android_task.service.PokemonService
import jc.ac.it_college.std.s21016.android_task.service.SpeciesInfo
import jc.ac.it_college.std.s21016.android_task.service.TypeInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException

private const val BASE_URL = "https://pokeapi.co/"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val retrofit = Retrofit.Builder().apply {
        baseUrl(BASE_URL)
        addConverterFactory(MoshiConverterFactory.create(moshi))
    }.build()
    private val service: PokemonService = retrofit.create(PokemonService::class.java)
    private val pokemonList = mapOf(
        "ライコウ" to 243,
        "エンテイ" to 244,
        "スイクン" to 245,
        "ルギア" to 249,
        "グラードン" to 383,
        "ディアルガ" to 483,
        "パルキア" to 484,
        "ギラティナ" to 487,
        "ダークライ" to 491,
        "アルセウス" to 493,
        "レシラム" to 643,
        "ゼクロム" to 644,
        "キュレム" to 646,
        "ゼルネアス" to 716,
        "イベルタル" to 717,
        "ボルケニオン" to 721,
        "ソルガレオ" to 791,
        "ルナアーラ" to 792,
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.spPokemon.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            pokemonList.keys.toTypedArray()
        )
        binding.btDisplay.setOnClickListener {
            val id = pokemonList[binding.spPokemon.selectedItem]
            showPokemonInfo(id?:throw IllegalArgumentException("存在しないポケモンが選ばれました"))
        }
    }

    @UiThread
    private fun showPokemonInfo(id: Int) {
        lifecycleScope.launch {
            val info = getPokemonInfo(id)
            Picasso.get().load(info.sprites.other.officialArtwork.frontDefault).into(binding.imgPokemon)
            val typeNameList = info.types.map {
                val typeId = getNumberAtEndOfURL(it.type.url)
                getTypeInfo(typeId).names.filter { n -> n.language.name == "ja-Hrkt" }[0].name
            }
            val speciesId = getNumberAtEndOfURL(info.species.url)
            val species = getSpeciesInfo(speciesId)
            val japaneseText = species.flavorTexts.filter { text -> text.language.name == "ja" }[0].flavorText
            val genus = species.genera.filter { g -> g.language.name == "ja-Hrkt"}[0].genus
            binding.tvType.text = getString(R.string.type,
                typeNameList.joinToString("\n") { "・${it}" })
            binding.tvWeight.text = getString(R.string.weight, info.weight)
            binding.tvGenus.text = getString(R.string.genus, genus)
            binding.tvFlavorText.text = japaneseText
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @WorkerThread
    private suspend fun getPokemonInfo(id: Int): PokemonInfo {
        return withContext(Dispatchers.IO) {
            service.fetchPokemon(id).execute().body() ?: throw IllegalStateException("ポケモンが取れませんでした")
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @WorkerThread
    private suspend fun getTypeInfo(id: Int): TypeInfo {
        return withContext(Dispatchers.IO) {
            service.fetchType(id).execute().body() ?: throw IllegalStateException("ポケモンが取れませんでした")
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @WorkerThread
    private suspend fun getSpeciesInfo(id: Int): SpeciesInfo {
        return withContext(Dispatchers.IO) {
            service.fetchSpecies(id).execute().body() ?: throw IllegalStateException("ポケモンが取れませんでした")
        }
    }

    private fun getNumberAtEndOfURL(url: String): Int {
        val split= url.split("/")
        return split[split.size - 2].toInt()
    }
}