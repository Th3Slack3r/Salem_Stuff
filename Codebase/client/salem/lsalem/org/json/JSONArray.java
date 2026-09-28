package org.json;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class JSONArray {
   private ArrayList<Object> myArrayList = new ArrayList<>();

   public JSONArray() {
   }

   public JSONArray(JSONTokener x) throws JSONException {
      this();
      if (x.nextClean() != '[') {
         throw x.syntaxError("A JSONArray text must start with '['");
      } else if (x.nextClean() != ']') {
         x.back();

         while (true) {
            if (x.nextClean() == ',') {
               x.back();
               this.myArrayList.add(JSONObject.NULL);
            } else {
               x.back();
               this.myArrayList.add(x.nextValue());
            }

            switch (x.nextClean()) {
               case ',':
               case ';':
                  if (x.nextClean() == ']') {
                     return;
                  }

                  x.back();
                  break;
               case ']':
                  return;
               default:
                  throw x.syntaxError("Expected a ',' or ']'");
            }
         }
      }
   }

   public JSONArray(String source) throws JSONException {
      this(new JSONTokener(source));
   }

   public JSONArray(Collection<?> collection) {
      if (collection != null) {
         Iterator<?> iter = collection.iterator();

         while (iter.hasNext()) {
            this.myArrayList.add(JSONObject.wrap(iter.next()));
         }
      }
   }

   public JSONArray(Object array) throws JSONException {
      this();
      if (!array.getClass().isArray()) {
         throw new JSONException("JSONArray initial value should be a string or collection or array.");
      } else {
         int length = Array.getLength(array);

         for (int i = 0; i < length; i++) {
            this.put(JSONObject.wrap(Array.get(array, i)));
         }
      }
   }

   public Object get(int index) throws JSONException {
      Object object = this.opt(index);
      if (object == null) {
         throw new JSONException("JSONArray[" + index + "] not found.");
      } else {
         return object;
      }
   }

   public boolean getBoolean(int index) throws JSONException {
      Object object = this.get(index);
      if (!object.equals(Boolean.FALSE) && (!(object instanceof String) || !((String)object).equalsIgnoreCase("false"))) {
         if (!object.equals(Boolean.TRUE) && (!(object instanceof String) || !((String)object).equalsIgnoreCase("true"))) {
            throw new JSONException("JSONArray[" + index + "] is not a boolean.");
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   public double getDouble(int index) throws JSONException {
      Object object = this.get(index);

      try {
         return object instanceof Number ? ((Number)object).doubleValue() : Double.parseDouble((String)object);
      } catch (Exception var4) {
         throw new JSONException("JSONArray[" + index + "] is not a number.");
      }
   }

   public int getInt(int index) throws JSONException {
      Object object = this.get(index);

      try {
         return object instanceof Number ? ((Number)object).intValue() : Integer.parseInt((String)object);
      } catch (Exception var4) {
         throw new JSONException("JSONArray[" + index + "] is not a number.");
      }
   }

   public JSONArray getJSONArray(int index) throws JSONException {
      Object object = this.get(index);
      if (object instanceof JSONArray) {
         return (JSONArray)object;
      } else {
         throw new JSONException("JSONArray[" + index + "] is not a JSONArray.");
      }
   }

   public JSONObject getJSONObject(int index) throws JSONException {
      Object object = this.get(index);
      if (object instanceof JSONObject) {
         return (JSONObject)object;
      } else {
         throw new JSONException("JSONArray[" + index + "] is not a JSONObject.");
      }
   }

   public long getLong(int index) throws JSONException {
      Object object = this.get(index);

      try {
         return object instanceof Number ? ((Number)object).longValue() : Long.parseLong((String)object);
      } catch (Exception var4) {
         throw new JSONException("JSONArray[" + index + "] is not a number.");
      }
   }

   public String getString(int index) throws JSONException {
      Object object = this.get(index);
      return object == JSONObject.NULL ? null : object.toString();
   }

   public boolean isNull(int index) {
      return JSONObject.NULL.equals(this.opt(index));
   }

   public String join(String separator) throws JSONException {
      int len = this.length();
      StringBuffer sb = new StringBuffer();

      for (int i = 0; i < len; i++) {
         if (i > 0) {
            sb.append(separator);
         }

         sb.append(JSONObject.valueToString(this.myArrayList.get(i)));
      }

      return sb.toString();
   }

   public int length() {
      return this.myArrayList.size();
   }

   public Object opt(int index) {
      return index >= 0 && index < this.length() ? this.myArrayList.get(index) : null;
   }

   public boolean optBoolean(int index) {
      return this.optBoolean(index, false);
   }

   public boolean optBoolean(int index, boolean defaultValue) {
      try {
         return this.getBoolean(index);
      } catch (Exception var4) {
         return defaultValue;
      }
   }

   public double optDouble(int index) {
      return this.optDouble(index, Double.NaN);
   }

   public double optDouble(int index, double defaultValue) {
      try {
         return this.getDouble(index);
      } catch (Exception var5) {
         return defaultValue;
      }
   }

   public int optInt(int index) {
      return this.optInt(index, 0);
   }

   public int optInt(int index, int defaultValue) {
      try {
         return this.getInt(index);
      } catch (Exception var4) {
         return defaultValue;
      }
   }

   public JSONArray optJSONArray(int index) {
      Object o = this.opt(index);
      return o instanceof JSONArray ? (JSONArray)o : null;
   }

   public JSONObject optJSONObject(int index) {
      Object o = this.opt(index);
      return o instanceof JSONObject ? (JSONObject)o : null;
   }

   public long optLong(int index) {
      return this.optLong(index, 0L);
   }

   public long optLong(int index, long defaultValue) {
      try {
         return this.getLong(index);
      } catch (Exception var5) {
         return defaultValue;
      }
   }

   public String optString(int index) {
      return this.optString(index, "");
   }

   public String optString(int index, String defaultValue) {
      Object object = this.opt(index);
      return object != null ? object.toString() : defaultValue;
   }

   public JSONArray put(boolean value) {
      this.put(value ? Boolean.TRUE : Boolean.FALSE);
      return this;
   }

   public JSONArray put(Collection<?> value) {
      this.put(new JSONArray(value));
      return this;
   }

   public JSONArray put(double value) throws JSONException {
      Double d = new Double(value);
      JSONObject.testValidity(d);
      this.put(d);
      return this;
   }

   public JSONArray put(int value) {
      this.put(new Integer(value));
      return this;
   }

   public JSONArray put(long value) {
      this.put(new Long(value));
      return this;
   }

   public JSONArray put(Map<?, ?> value) {
      this.put(new JSONObject(value));
      return this;
   }

   public JSONArray put(Object value) {
      this.myArrayList.add(value);
      return this;
   }

   public JSONArray put(int index, boolean value) throws JSONException {
      this.put(index, value ? Boolean.TRUE : Boolean.FALSE);
      return this;
   }

   public JSONArray put(int index, Collection<?> value) throws JSONException {
      this.put(index, new JSONArray(value));
      return this;
   }

   public JSONArray put(int index, double value) throws JSONException {
      this.put(index, new Double(value));
      return this;
   }

   public JSONArray put(int index, int value) throws JSONException {
      this.put(index, new Integer(value));
      return this;
   }

   public JSONArray put(int index, long value) throws JSONException {
      this.put(index, new Long(value));
      return this;
   }

   public JSONArray put(int index, Map<?, ?> value) throws JSONException {
      this.put(index, new JSONObject(value));
      return this;
   }

   public JSONArray put(int index, Object value) throws JSONException {
      JSONObject.testValidity(value);
      if (index < 0) {
         throw new JSONException("JSONArray[" + index + "] not found.");
      } else {
         if (index < this.length()) {
            this.myArrayList.set(index, value);
         } else {
            while (index != this.length()) {
               this.put(JSONObject.NULL);
            }

            this.put(value);
         }

         return this;
      }
   }

   public Object remove(int index) {
      Object o = this.opt(index);
      this.myArrayList.remove(index);
      return o;
   }

   public JSONObject toJSONObject(JSONArray names) throws JSONException {
      if (names != null && names.length() != 0 && this.length() != 0) {
         JSONObject jo = new JSONObject();

         for (int i = 0; i < names.length(); i++) {
            jo.put(names.getString(i), this.opt(i));
         }

         return jo;
      } else {
         return null;
      }
   }

   @Override
   public String toString() {
      try {
         return '[' + this.join(",") + ']';
      } catch (Exception var2) {
         return null;
      }
   }

   public String toString(int indentFactor) throws JSONException {
      return this.toString(indentFactor, 0);
   }

   String toString(int indentFactor, int indent) throws JSONException {
      int len = this.length();
      if (len == 0) {
         return "[]";
      } else {
         StringBuffer sb = new StringBuffer("[");
         if (len == 1) {
            sb.append(JSONObject.valueToString(this.myArrayList.get(0), indentFactor, indent));
         } else {
            int newindent = indent + indentFactor;
            sb.append('\n');

            for (int i = 0; i < len; i++) {
               if (i > 0) {
                  sb.append(",\n");
               }

               for (int j = 0; j < newindent; j++) {
                  sb.append(' ');
               }

               sb.append(JSONObject.valueToString(this.myArrayList.get(i), indentFactor, newindent));
            }

            sb.append('\n');

            for (int var8 = 0; var8 < indent; var8++) {
               sb.append(' ');
            }
         }

         sb.append(']');
         return sb.toString();
      }
   }

   public Writer write(Writer writer) throws JSONException {
      try {
         boolean b = false;
         int len = this.length();
         writer.write(91);

         for (int i = 0; i < len; i++) {
            if (b) {
               writer.write(44);
            }

            Object v = this.myArrayList.get(i);
            if (v instanceof JSONObject) {
               ((JSONObject)v).write(writer);
            } else if (v instanceof JSONArray) {
               ((JSONArray)v).write(writer);
            } else {
               writer.write(JSONObject.valueToString(v));
            }

            b = true;
         }

         writer.write(93);
         return writer;
      } catch (IOException var6) {
         throw new JSONException(var6);
      }
   }
}
