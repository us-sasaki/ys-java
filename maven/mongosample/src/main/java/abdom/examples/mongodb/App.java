package abdom.examples.mongodb;

import com.mongodb.*;

/**
 * 結果は以下になった。
 * { "_id" : { "$oid" : "58144bb36f2e916e1de91edc"} , "name" : "yusuke" , 
 * "age" : 46.0 , "height" : 201.0}
 * { "_id" : { "$oid" : "58144bcf6f2e916e1de91edd"} , "values" :
 * [ 2.0 , 3.0 , 4.0 , 5.0] , "name" : "test"}
 * { "_id" : { "$oid" : "58144bcf6f2e916e1de91ede"} , "fuga" : "fuga"}
 * { "_id" : { "$oid" : "58144c286f2e916e1de91edf"} , "name" : "yusuke" ,
 * "age" : 46.0 , "height" : 199 , "additional" : "yusuke"}
 *
 */
public class App {
    public static void main( String[] args )  {
    	MongoClient c = new MongoClient();
    	DB db = c.getDB("foo");
    	DBCollection col1 = db.getCollection("col1");
    	DBCursor cursor = col1.find();
    	System.out.println("---- result ----");
    	for (DBObject dbo : cursor) {
    		System.out.println(dbo);
    	}
    }
}
