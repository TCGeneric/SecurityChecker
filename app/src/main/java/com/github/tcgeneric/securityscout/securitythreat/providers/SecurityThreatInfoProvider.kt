package com.github.tcgeneric.securityscout.securitythreat.providers

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.URI
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

class SecurityThreatInfoProvider {

    data class SecurityThreatInfo(val id:String,
                                  val threatLevelMax:Int,
                                  val threatLevel:Int,
                                  val display:String,
                                  val colorId:String)

    companion object {
        private val executor = Executors.newSingleThreadExecutor()

        fun getFuture(url:String, targetHTML:String): Future<SecurityThreatInfo?> {
            return executor.submit(Callable {
                process(
                    urlToIdentifier(
                        url
                    ),
                    getTargetClassName(
                        url,
                        targetHTML
                    )
                )
            })
        }

        private fun urlToIdentifier(url:String):String {
            val uri = URI(url)
            var domain = uri.host
            if(domain.startsWith("www."))
                domain = domain.substring(4)
            return domain.split(".")[0]
        }

        private fun getTargetClassName(url:String, targetHTML:String):String? {
            return try {
                val doc: Document = Jsoup.connect(url).get()
                val target = doc.select(targetHTML).first()
                target.className()
            } catch(e:IOException) {
                null
            }
        }

        private fun process(id:String, clsName:String?): SecurityThreatInfo? {
            if(clsName == null)
                return null

            val data = DisplayDataProvider.map[id]!!
            val threatIdx = data.classNames.indexOf(clsName)
            return SecurityThreatInfo(
                id,
                data.maxThreatLevel,
                threatIdx,
                data.displayStrings[threatIdx],
                data.colorIDs[threatIdx]
            )
        }
    }
}