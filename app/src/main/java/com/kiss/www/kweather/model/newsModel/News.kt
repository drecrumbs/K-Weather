package com.kiss.www.kweather.model.newsModel

import android.util.Log
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

class News(var title: String, var link: String, var description: String, var author:String?){


    companion object {
        val logTag = this.javaClass.simpleName

        @Throws(XmlPullParserException::class, IOException::class)
        fun parseFeed(inputStream: InputStream): List<News>{
            val newsList: List<News>
            var title: String? = null
            var link: String? = null
            var description: String? = null
            var isItem = false
            var author: String? = null
            val items: ArrayList<News> = ArrayList()

            try {
                val xmlPullParser = Xml.newPullParser()
                xmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
                xmlPullParser.setInput(inputStream, null)
                xmlPullParser.nextTag()
                while (xmlPullParser.next() != XmlPullParser.END_DOCUMENT) {
                    val eventType = xmlPullParser.eventType

                    val name = xmlPullParser.name ?: continue

                    if (eventType == XmlPullParser.END_TAG) {
                        if (name.equals("item", ignoreCase = true)) {
                            isItem = false
                        }
                        continue
                    }

                    if (eventType == XmlPullParser.START_TAG) {
                        if (name.equals("item", ignoreCase = true)) {
                            isItem = true
                            continue
                        }
                    }

                    var result = ""
                    if (xmlPullParser.next() == XmlPullParser.TEXT) {
                        result = xmlPullParser.text
                        xmlPullParser.nextTag()
                    }

                    if (name.equals("title", ignoreCase = true)) {
                        author = result.substring(result.lastIndexOf("-"))
                        result = result.removeRange(result.lastIndexOf("-") - 1, result.length)
                        title = result

                        Log.d("MyXmlParser", "Parsed ==> $title : $author")
                    } else if (name.equals("link", ignoreCase = true)) {
                        link = result
                    } else if (name.equals("description", ignoreCase = true)) {
                        description = result
                    }

                    if (title != null && link != null && description != null) {
                        if (isItem) {
                            val item = News(title, link, description, author)
                            items.add(item)
                        } else {
                            Log.d(logTag, "NEWS: Do Nothing..")
//                            mFeedTitle = title
//                            mFeedLink = link
//                            mFeedDescription = description
                        }

                        title = null
                        link = null
                        description = null
                        isItem = false
                    }
                }

                newsList = items
                return newsList
            } finally {
                inputStream.close()
            }
        }
    }
}