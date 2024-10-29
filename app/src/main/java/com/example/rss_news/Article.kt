package com.example.rss_news

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

data class Article(var email: String = "", val title: String, val description: String, val imageUrl: String?, val link: String, var read: Boolean = false, var favourite: Boolean = false)

{
    fun toJson(): String {
        return JSONObject().apply {
            put("email", email)
            put("title", title)
            put("description", description)
            put("imageUrl", imageUrl)
            put("link", link)
            put("read", read)
            put("favourite", favourite)
        }.toString()
    }

    companion object {
        fun fromJson(json: String): Article {
            val jsonObject = JSONObject(json)
            return Article(
                email = jsonObject.getString("email"),
                title = jsonObject.getString("title"),
                description = jsonObject.getString("description"),
                imageUrl = jsonObject.optString("imageUrl", null),
                link = jsonObject.getString("link"),
                read = jsonObject.getBoolean("read"),
                favourite = jsonObject.getBoolean("favourite")
            )
        }
    }
}

suspend fun fetchArticles(context: Context, userEmail: String): List<Article> = withContext(Dispatchers.IO) {
    val articles = mutableListOf<Article>()
    val url = "https://wiadomosci.gazeta.pl/pub/rss/wiadomosci_kraj.htm"
    val doc: Document = Jsoup.connect(url).get()
    val items = doc.select("item")

    val savedArticles = NewsListActivity.ArticleCache.loadAllArticles(context)

    for (item in items) {
        val title = item.select("title").text()
        val description = item.select("description").text()

        val imgElement: Element? = Jsoup.parse(description).selectFirst("img")
        val imageUrl = imgElement?.attr("src")

        val descriptionText = Jsoup.parse(description).text()
        val link = item.select("link").text()

        val savedArticle = savedArticles.find { it.link == link && it.email == userEmail }

        if (savedArticle != null) {
            articles.add(savedArticle)
        } else {
            articles.add(Article(email = userEmail, title = title, description = descriptionText, imageUrl = imageUrl, link = link))
        }
    }
    for (savedArticle in savedArticles) {
        if (!articles.any { it.link == savedArticle.link }) {
            articles.add(savedArticle)
        }
    }

    articles
}
