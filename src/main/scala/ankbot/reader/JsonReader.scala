package ankbot.reader

import org.json.{ JSONArray, JSONObject, JSONTokener }

import scala.jdk.CollectionConverters._
import scala.reflect.runtime.universe._

/**
 *
 * @param jsonString
 */
class JsonReader(jsonString: String) extends JsonObjectReader(new JSONTokener(jsonString).nextValue())

class JsonObjectReader(json: AnyRef) extends ObjectReader[AnyRef](json) {

  override def newInstance[T](obj: AnyRef): T with ObjectReader[AnyRef] = {
    new JsonObjectReader(obj).asInstanceOf[T with ObjectReader[AnyRef]]
  }

  override def getReaderTypeTag: TypeTag[_] = typeTag[JsonObjectReader]

  override def unwrap(obj: AnyRef): AnyRef = obj

  override def getValue(name: String, obj: AnyRef): AnyRef = {
    if (!obj.isInstanceOf[JSONObject]) throw new InvalidObject(new Exception("not a json object"))
    try {
      obj.asInstanceOf[JSONObject].get(name)
    } catch {
      case e: Throwable => throw new Missing(e)
    }
  }

  override def getList(obj: AnyRef): List[AnyRef] = {
    if (!obj.isInstanceOf[JSONArray]) throw new InvalidObject(new Exception("not a json array"))
    obj.asInstanceOf[JSONArray].iterator().asScala.toList
  }

  override def getMap[T](obj: AnyRef)(implicit ttag: TypeTag[T]): Map[String, AnyRef] = {
    if (!obj.isInstanceOf[JSONObject]) throw new InvalidObject(new Exception("not a json object"))
    val jsonObject = obj.asInstanceOf[JSONObject]
    jsonObject.keys().asScala.map(key => (key, jsonObject.get(key))).toMap
  }

}
