package net.suntec.sdl.autotester;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * Responsible for marshalling and unmarshing between RPC Objects and byte streams that are sent
 * over transmission
 */

public class MyJsonRPCMarshaller {
	boolean hasInsert = false;

	public boolean isHasInsert() {
		return hasInsert;
	}

	public MyJsonRPCMarshaller() {
	}

	public static void deserializeJSONObject(Hashtable<String, Object> ret,
			String key, Object value, OgnlContext context) throws JSONException {
		if (value instanceof JSONObject) {
			ret.put(key, deserializeJSONObject((JSONObject) value, context));
		} else if (value instanceof JSONArray) {
			JSONArray arrayValue = (JSONArray) value;
			List<Object> putList = new ArrayList<Object>(arrayValue.length());
			for (int i = 0; i < arrayValue.length(); i++) {
				Object anObject = arrayValue.get(i);
				if (anObject instanceof JSONObject) {
					Hashtable<String, Object> deserializedObject = deserializeJSONObject(
							(JSONObject) anObject, context);
					putList.add(deserializedObject);
				} else {
					putList.add(anObject);
				}
			}
			ret.put(key, putList);
		} else {
			ret.put(key, value);
		}
	}

	@SuppressWarnings("unchecked")
	public static Hashtable<String, Object> deserializeJSONObject(
			JSONObject jsonObject, OgnlContext context) throws JSONException {
		Hashtable<String, Object> ret = new Hashtable<String, Object>();
		Iterator<String> it = jsonObject.keys();
		String key = null;
		while (it.hasNext()) {
			key = it.next();
			Object value = jsonObject.get(key);
			if (value instanceof JSONObject) {
				ret.put(key, deserializeJSONObject((JSONObject) value, context));
			} else if (value instanceof JSONArray) {
				JSONArray arrayValue = (JSONArray) value;
				List<Object> putList = new ArrayList<Object>(
						arrayValue.length());
				for (int i = 0; i < arrayValue.length(); i++) {
					Object anObject = arrayValue.get(i);
					if (anObject instanceof JSONObject) {
						Hashtable<String, Object> deserializedObject = deserializeJSONObject(
								(JSONObject) anObject, context);
						putList.add(deserializedObject);
					} else {
						String valueStr = anObject.toString();
						if (valueStr.contains("#")) {
							try {
								Object replaceValue = Ognl.getValue(valueStr,
										context, context.getRoot());
								putList.add(replaceValue);
							} catch (OgnlException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							putList.add(anObject);
						}
					}
				}
				ret.put(key, putList);
			} else {
				String valueStr = value.toString();
				if (valueStr.contains("#")) {
					try {
						Object replaceValue = Ognl.getValue(valueStr, context,
								context.getRoot());
						deserializeJSONObject(ret, key, replaceValue, context);
					} catch (OgnlException e) {
						ThreadLogUtil.error("ognl:" + e.getMessage());
						return null;
					}
				} else {
					ret.put(key, value);
				}
			}
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	public Hashtable<String, Object> deserializeJSONObject(JSONObject jsonObject)
			throws JSONException {
		Hashtable<String, Object> ret = new Hashtable<String, Object>();
		Iterator<String> it = jsonObject.keys();
		String key = null;
		while (it.hasNext()) {
			key = it.next();
			Object value = jsonObject.get(key);
			if (value instanceof JSONObject) {
				ret.put(key, deserializeJSONObject((JSONObject) value));
			} else if (value instanceof JSONArray) {
				JSONArray arrayValue = (JSONArray) value;
				List<Object> putList = new ArrayList<Object>(
						arrayValue.length());
				for (int i = 0; i < arrayValue.length(); i++) {
					Object anObject = arrayValue.get(i);
					if (anObject instanceof JSONObject) {
						Hashtable<String, Object> deserializedObject = deserializeJSONObject((JSONObject) anObject);
						putList.add(deserializedObject);
					} else {
						putList.add(anObject);
					}
				}
				ret.put(key, putList);
			} else {
				checkValue(value);
				if (hasInsert) {
					break;
				}
				ret.put(key, value);
			}
		}
		return ret;
	}

	private void checkValue(Object value) {
		String valueStr = value.toString();
		if (hasInsert) {
			return;
		}
		if (valueStr.contains("#")) {
			hasInsert = true;
		}
	}
	// @SuppressWarnings("unchecked")
	// private JSONArray serializeList(List<?> list) throws JSONException{
	// JSONArray toPut = new JSONArray();
	// Iterator<Object> valueIterator = (Iterator<Object>) list.iterator();
	// while(valueIterator.hasNext()){
	// Object anObject = valueIterator.next();
	// if (anObject instanceof RPCStruct) {
	// RPCStruct toSerialize = (RPCStruct) anObject;
	// toPut.put(toSerialize.serializeJSON());
	// } else if(anObject instanceof Hashtable){
	// Hashtable<String, Object> toSerialize = (Hashtable<String,
	// Object>)anObject;
	// toPut.put(serializeHashtable(toSerialize));
	// } else {
	// toPut.put(anObject);
	// }
	// }
	// return toPut;
	// }
	//
	// @SuppressWarnings({"unchecked" })
	// public static JSONObject serializeHashtable(Hashtable<String, Object>
	// hash) throws JSONException{
	// JSONObject obj = new JSONObject();
	// Iterator<String> hashKeyIterator = hash.keySet().iterator();
	// while (hashKeyIterator.hasNext()){
	// String key = (String) hashKeyIterator.next();
	// Object value = hash.get(key);
	// if (value instanceof RPCStruct) {
	// obj.put(key, ((RPCStruct) value).serializeJSON());
	// } else if (value instanceof List<?>) {
	// obj.put(key, serializeList((List<?>) value));
	// } else if (value instanceof Hashtable) {
	// obj.put(key, serializeHashtable((Hashtable<String, Object>)value));
	// } else {
	// obj.put(key, value);
	// }
	// }
	// return obj;
	// }
}
