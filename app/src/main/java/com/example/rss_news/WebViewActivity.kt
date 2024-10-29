package com.example.rss_news

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class WebViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.news_web_view)

        val webView: WebView = findViewById(R.id.web_view)
        webView.webViewClient = WebViewClient()

        val articleUrl = intent.getStringExtra("article_url")

        val userEmail = Firebase.auth.currentUser?.email.toString()

        val addFavouriteCheckbox: CheckBox = findViewById(R.id.favouriteCheck)

        articleUrl?.let {
            webView.webViewClient = WebViewClient()
            webView.loadUrl(it)

            val article = NewsListActivity.ArticleCache.loadArticle(this, it, userEmail) ?: Article(
                email = userEmail,
                title = "",
                description = "",
                imageUrl = null,
                link = it
            )
            addFavouriteCheckbox.isChecked = article.favourite

            addFavouriteCheckbox.setOnCheckedChangeListener { _, isChecked ->
                article.favourite = isChecked
                NewsListActivity.ArticleCache.saveArticle(this, article)
            }
        }

        val sharebutton : Button = findViewById(R.id.shareButton)

        sharebutton.setOnClickListener { shareArticle(articleUrl) }
    }

    private fun shareArticle(articleurl: String?) {
        val shareIntent = Intent().apply{
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT,"Look what is going on: $articleurl")
            type = "text/plain"
        }
        val chose = Intent.createChooser(shareIntent,null)
        startActivity(chose)
    }
}
