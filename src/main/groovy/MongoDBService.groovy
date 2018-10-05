

import com.google.gson.Gson
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoDatabase
import com.mongodb.client.MongoCollection
import org.bson.Document

class MongoDBService {

    def static save(obj) {
        MongoClient mongoClient = MongoClients.create()
        MongoDatabase db = mongoClient.getDatabase("trnds")
        MongoCollection<Document> collection = db.getCollection("trnds")
        Document doc = DataFormatUtils.parseToMongoDoc(obj)
        collection.insertOne(doc)
    }

}