package com.gofoodgo.app;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class CartStore {
    public static class Line {
        public String id,name,imageUrl;
        public long price;
        public int quantity;
        public Line(String i,String n,String im,long p,int q) { id=i;name=n;imageUrl=im;price=p;quantity=q; }
    }
    private static SharedPreferences prefs(Context c) { return c.getSharedPreferences("gofoodgo_cart", Context.MODE_PRIVATE); }
    private static String key() {
        return "cart_" + (FirebaseAuth.getInstance().getUid() == null ? "guest" : FirebaseAuth.getInstance().getUid());
    }
    public static List<Line> get(Context c) {
        List<Line> items = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs(c).getString(key(), "[]"));
            for(int i=0;i<arr.length();i++) {
                JSONObject o=arr.getJSONObject(i);
                items.add(new Line(o.getString("id"),o.optString("name"),o.optString("imageUrl"),o.optLong("price"),o.optInt("quantity",1)));
            }
        } catch(JSONException ignored) { }
        return items;
    }
    public static void save(Context c,List<Line> items) {
        JSONArray arr = new JSONArray();
        for(Line l:items) {
            try {
                JSONObject o=new JSONObject();
                o.put("id",l.id);o.put("name",l.name);o.put("imageUrl",l.imageUrl);o.put("price",l.price);o.put("quantity",l.quantity);
                arr.put(o);
            }catch(JSONException ignored){}
        }
        prefs(c).edit().putString(key(),arr.toString()).apply();
    }
    public static void add(Context c,Food food) {
        List<Line> items=get(c);
        for(Line l:items) if(l.id.equals(food.id)) { if(l.quantity<99) l.quantity++; save(c,items);return; }
        items.add(new Line(food.id,food.name,food.imageUrl,food.price,1));save(c,items);
    }
    public static long total(List<Line> items) {
        long t=0; for(Line l:items)t+=l.price*l.quantity; return t;
    }
    public static void clear(Context c) { save(c,new ArrayList<>()); }
}
