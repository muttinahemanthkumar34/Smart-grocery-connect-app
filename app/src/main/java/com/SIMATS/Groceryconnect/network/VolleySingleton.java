package com.SIMATS.Groceryconnect.network;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Singleton class for managing Volley RequestQueue.
 * Ensures only one instance of RequestQueue exists throughout the app.
 */
public class VolleySingleton {
    
    private static VolleySingleton instance;
    private RequestQueue requestQueue;
    private static Context context;
    
    private VolleySingleton(Context ctx) {
        context = ctx.getApplicationContext();
        requestQueue = getRequestQueue();
    }
    
    public static synchronized VolleySingleton getInstance(Context context) {
        if (instance == null) {
            instance = new VolleySingleton(context);
        }
        return instance;
    }
    
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context);
        }
        return requestQueue;
    }
    
    public <T> void addToRequestQueue(Request<T> request) {
        getRequestQueue().add(request);
    }
}
