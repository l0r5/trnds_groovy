import groovy.json.JsonSlurper

import java.text.SimpleDateFormat


apiData = [:]
fetchedData = ""
processedData = [:]

def script() {
    println("Start script")
    init()
    fetch()
    processData()
    save()
}

def init() {
    println "Initialize..."
    apiData = extractApiData()
}

def fetch() {
    def providersMap = apiData["providers"]
    def urlList = []

    providersMap.each {
        def baseUrl = it["baseUrl"] as String
        def query = "?part=snippet,contentDetails&chart=mostPopular&regionCode=US&maxResults=25&"
        def apiKey = "key=" + it["apiKey"]
        urlList.add(baseUrl + query + apiKey)
    }

    println "GET Following URLs:"
    urlList.each {
        println it.toString()

        def connection = new URL(it as String).openConnection() as HttpURLConnection

        // set some headers
        connection.setRequestProperty('User-Agent', 'groovy-2.4.15')
        connection.setRequestProperty('Accept', 'application/json')

        // get the response code - automatically sends the request
        println "Response code: " + connection.responseCode
        fetchedData = connection.inputStream.text
    }
}

def processData() {
    println "Process fetched data..."
    def date = new Date()
    def sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
    processedData << ["date": sdf.format(date)]
    if (fetchedData != null) {
        apiData.providers.each {
            switch (it.name) {
                case "YouTube":
                    processYouTubeData()
            }
        }
    } else {
        println "No data was fetched."
    }
}

def processYouTubeData() {
    def youtubeVideos = []
    def jsonSlurper = new JsonSlurper()
    fetchedData = jsonSlurper.parseText(fetchedData as String)
    fetchedData.items.snippet.eachWithIndex { it, count ->
        def video = [
                "publishedAt": new Date().parse("yyyy-MM-dd'T'HH:mm:ss.SSSX",it.publishedAt as String),
                "title"      : it.title,
                "description": it.description,
                "url"        : "https://www.youtube.com/watch?v=" + fetchedData.items[count].id as String
        ]
        youtubeVideos.add(video)
        println("Built Video:" +
                "\nTitle: " + video.title +
                "\nPublished at: " + video.publishedAt.toString() +
                "\nUrl: " + video.url)
    }
    processedData << ["youtube": youtubeVideos]
}

def save(){
    println("Save data...")
    MongoDBService.save(processedData)
}

def static extractApiData() {
    def jsonSlurper = new JsonSlurper()
    File apiDataFile = new File('../resources/api_data.json')
    def apiDataMap = jsonSlurper.parseText(apiDataFile.text)
    return apiDataMap
}

script()
