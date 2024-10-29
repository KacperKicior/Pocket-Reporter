package com.example.rss_news

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat


class ArticleAdapter(context: Context, private val articles: List<Article>) : ArrayAdapter<Article>(context, 0, articles) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.article_item, parent, false)
        }
        val article = articles[position]
        val titleTextView = view?.findViewById<TextView>(R.id.article_title)
        val descriptionTextView = view?.findViewById<TextView>(R.id.article_description)
        val imageView = view?.findViewById<ImageView>(R.id.article_image)

        titleTextView?.text = article.title
        descriptionTextView?.text = article.description
        if (article.imageUrl != null) {
            ImageLoaderTask(imageView).execute(article.imageUrl)
        } else {
            imageView?.setImageDrawable(ContextCompat.getDrawable(context, com.google.android.gms.base.R.drawable.common_google_signin_btn_icon_dark_normal))
        }

        if (article.read) {
            view?.alpha = 0.5f
        } else {
            view?.alpha = 1.0f
        }

        return view!!
    }
}
