package com.batterysimplewidget;
import com.batterysimplewidget.R;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.view.View;
import android.widget.RemoteViews;
public class BatteryService extends Service {
    private static final String TAG = "BatteryService";
    private BroadcastReceiver batteryReceiver;
    private Integer currentLevel = 0;
    private Integer currentTemperature = 0;
    private double currentVoltage = 0;
    private String currentType,strStatus;
    @Override
    public void onStart(Intent intent, int startId) {
        if (this.batteryReceiver == null) {
            this.batteryReceiver = batteryLevelReceiver();
            registerReceiver(this.batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }
        // Build the widget update.
        RemoteViews updateViews = buildUpdate(this);
        // Show power usage when widget is tapped.
        Intent intentBatteryUsage = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intentBatteryUsage,PendingIntent.FLAG_UPDATE_CURRENT);
        updateViews.setOnClickPendingIntent(R.id.widget, pendingIntent);

        // Push update for this widget to the home screen
        ComponentName batteryWidget = new ComponentName(this, BatteryAppWidgetProvider.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        appWidgetManager.updateAppWidget(batteryWidget, updateViews);
    }
    private BroadcastReceiver batteryLevelReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {                              
                String action = intent.getAction();               
                if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                	int status = intent.getIntExtra("status", 0);
                    int level = intent.getIntExtra("level", 0);
                    int scale = intent.getIntExtra("scale", 1);
                    double voltage = intent.getIntExtra("voltage", 0);
                    int temperature = intent.getIntExtra("temperature", 0);   
                    if (scale <= 0) {
                        scale = 100;
                    }
                    level = level * 100 / scale;                     
                    
                    if (currentLevel != level) {
                        currentLevel = level;                        
                    }                   
                    if (currentTemperature != temperature) {
                        currentTemperature = temperature;                        
                    }   
                    if (currentVoltage != voltage) {
                    	currentVoltage=voltage;
                    }
                   
                    if (status == BatteryManager.BATTERY_STATUS_CHARGING) {
                        strStatus = "Charging";                    
                    } else if (status == BatteryManager.BATTERY_STATUS_FULL) {
                        strStatus = "Full";                    
                    }else if (status == BatteryManager.BATTERY_STATUS_DISCHARGING) {
                    	strStatus = "DisCharging"; 
                    }else if (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
                    	strStatus = "NotCharging"; 
                    }else if (status == BatteryManager.BATTERY_STATUS_UNKNOWN) {
                    	strStatus = "?"; 
                    }else
                    	strStatus = "?"; 
                    currentType=intent.getStringExtra("technology");                     
                }        
                Intent statusChanged = new Intent(context, BatteryService.class);
                context.startService(statusChanged);
            }
        };
    }
    int i=0;
    private RemoteViews buildUpdate(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(),
                BatteryAppWidgetProvider.currentLayout);
        views.setTextViewText(R.id.status, this.currentLevel.toString());
        Integer temperature = this.currentTemperature > 100 ? this.currentTemperature / 10: this.currentTemperature;
        String batstr; 
        if(currentLevel<100)
        {
              batstr=" Level:    "+String.valueOf(currentLevel) +"  %\n"+
		  			  String.format(" Volt :  %1.2f  V",(double) currentVoltage/1000).replace(",",".")+"\n"+
		  			  String.format(" Temp :  %d  C°",temperature)+"\n"+
		              " " + currentType + " " + strStatus;
        }else
        {
        	 batstr=" Level: "+String.valueOf(currentLevel) +" %\n"+
         			  String.format(" Volt : %1.2f V",(double) currentVoltage/1000).replace(",",".")+"\n"+
         			  String.format(" Temp : %d C°",temperature)+"\n"+
         			 " " + currentType + " " + strStatus;
        }
            views.setTextViewText(R.id.status, batstr);
            views.setViewVisibility(R.id.status, View.VISIBLE);     
        ComponentName cn = new ComponentName(context, BatteryAppWidgetProvider.class);
        AppWidgetManager.getInstance(context).updateAppWidget(cn, views);       
        return views;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(this.batteryReceiver);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // We don't need to bind to this service
        return null;
    }

}
