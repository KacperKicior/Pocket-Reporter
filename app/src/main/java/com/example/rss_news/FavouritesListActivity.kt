package com.example.rss_news

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class FavouritesListActivity : AppCompatActivity() {
    val auth by lazy { Firebase.auth }
    private var currentUserEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fav_list)
        val listView: ListView = findViewById(R.id.fav_list_view)

        val currentUser: FirebaseUser? = auth.currentUser
        currentUser?.let {
            currentUserEmail = currentUser.email ?: ""
        }


        lifecycleScope.launch {
            val articles = ArticleCache.loadFavouriteArticles(this@FavouritesListActivity,currentUserEmail)
            val adapter = ArticleAdapter(this@FavouritesListActivity, articles)
            listView.adapter = adapter

            listView.setOnItemClickListener { _, _, position, _ ->
                val article = articles[position]

                article.email = currentUserEmail
                article.read = true

                ArticleCache.saveArticle(this@FavouritesListActivity,article)
                adapter.notifyDataSetChanged()

                val intent = Intent(this@FavouritesListActivity, WebViewActivity::class.java).apply {
                    putExtra("article_url", article.link)
                }
                startActivity(intent)
            }


        }

    }


    fun showFavourites(view: View?){
        startActivity(Intent(this,FavouritesListActivity::class.java))
    }

    object ArticleCache {
        private const val PREFS_NAME = "article_prefs"
        private const val ARTICLES_KEY = "articles"

        private fun getSharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }

        fun saveArticle(context: Context, article: Article) {
            val sharedPreferences = getSharedPreferences(context)
            val editor = sharedPreferences.edit()
            val articles = loadAllArticles(context).toMutableList()
            val existingArticle = articles.find { it.link == article.link && it.email == article.email }
            if (existingArticle != null) {
                articles.remove(existingArticle)
            }
            articles.add(article)
            val jsonArray = JSONArray()
            articles.forEach { jsonArray.put(JSONObject(it.toJson())) }
            editor.putString(ARTICLES_KEY, jsonArray.toString())
            editor.apply()
        }

        fun loadFavouriteArticles(context: Context, userEmail: String): List<Article> {
            val savedArticles = NewsListActivity.ArticleCache.loadAllArticles(context)
            return savedArticles.filter { it.email == userEmail && it.favourite }
        }

        fun loadArticle(context: Context, link: String, email: String): Article? {
            val articles = loadAllArticles(context)
            return articles.find { it.link == link && it.email == email }
        }

        fun loadAllArticles(context: Context): List<Article> {
            val sharedPreferences = getSharedPreferences(context)
            val json = sharedPreferences.getString(ARTICLES_KEY, null) ?: return emptyList()
            val jsonArray = JSONArray(json)
            val articles = mutableListOf<Article>()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                articles.add(Article.fromJson(jsonObject.toString()))
            }
            return articles
        }
    }



}