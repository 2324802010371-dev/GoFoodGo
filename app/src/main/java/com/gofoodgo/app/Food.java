package com.gofoodgo.app;

import com.google.firebase.firestore.DocumentSnapshot;

public class Food {
    public String id="", name="", category="", description="", imageUrl="";
    public long price=0;
    public boolean active=true;
    public static Food from(DocumentSnapshot d) {
        Food f = new Food();
        f.id = d.getId();
        f.name = d.getString("name") == null ? "Món ăn" : d.getString("name");
        f.category = d.getString("category") == null ? "Khác" : d.getString("category");
        f.description = d.getString("description") == null ? "" : d.getString("description");
        f.imageUrl = d.getString("imageUrl") == null ? "" : d.getString("imageUrl");
        Long p = d.getLong("price"); f.price = p == null ? 0 : p;
        Boolean a = d.getBoolean("active"); f.active = a == null || a;
        return f;
    }
}
