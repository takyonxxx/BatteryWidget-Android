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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.BatteryManager;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
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
    int chargestatus;
    private BroadcastReceiver batteryLevelReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {                              
                String action = intent.getAction();                   

                if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                	chargestatus = intent.getIntExtra("status", 0);
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
                  
                    currentType=intent.getStringExtra("technology");                     
                }        
                Intent statusChanged = new Intent(context, BatteryService.class);
                context.startService(statusChanged);
            }
        };
    }
    int i=0;
    private RemoteViews buildUpdate(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), BatteryAppWidgetProvider.currentLayout);
        views.setTextViewText(R.id.status, this.currentLevel.toString());
        Integer temperature = this.currentTemperature > 100 ? this.currentTemperature / 10: this.currentTemperature;
        String batstr; 
        if(currentLevel<100)
        {
              batstr=" Level:    "+String.valueOf(currentLevel) +"  %\n"+
		  			  String.format(" Volt :  %1.2f  V",(double) currentVoltage/1000).replace(",",".")+"\n"+
		  			  String.format(" Temp :  %d  C°",temperature);
        }else
        {
        	  batstr=" Level: "+String.valueOf(currentLevel) +" %\n"+
         			  String.format(" Volt : %1.2f V",(double) currentVoltage/1000).replace(",",".")+"\n"+
         			  String.format(" Temp : %d C°",temperature);
        }
        views.setTextViewText(R.id.status, batstr); 
        
        if (chargestatus == BatteryManager.BATTERY_STATUS_CHARGING) 
            views.setImageViewResource(R.id.imageView1, getPercentCharge(currentLevel));
        else if (chargestatus == BatteryManager.BATTERY_STATUS_FULL) {
        	views.setImageViewResource(R.id.imageView1, getPercentDisCharge(currentLevel));                
        }else if (chargestatus == BatteryManager.BATTERY_STATUS_DISCHARGING) {
        	views.setImageViewResource(R.id.imageView1, getPercentDisCharge(currentLevel));    
        }else 
        	views.setImageViewResource(R.id.imageView1, getUnknown());           	
               
        ComponentName cn = new ComponentName(context, BatteryAppWidgetProvider.class);
        AppWidgetManager.getInstance(context).updateAppWidget(cn, views);       
        return views;
    }
    private int getPercentCharge(int batteryLevel) {
        if(batteryLevel <=100 && batteryLevel > 80) {
            return R.drawable.charging_100_icon;
        } else if(batteryLevel <= 80 && batteryLevel > 60) {
            return R.drawable.charging_080_icon;
        } else if(batteryLevel <=60 && batteryLevel > 40) {
            return R.drawable.charging_060_icon;
        } else if(batteryLevel <=40 && batteryLevel > 20) {
            return R.drawable.charging_040_icon;
        } else if(batteryLevel <=20 && batteryLevel >= 0) {
            return R.drawable.charging_020_icon;
        } else
        	return R.drawable.missing_icon;
    }
    private int getPercentDisCharge(int batteryLevel) {
        if(batteryLevel <=100 && batteryLevel > 80) {
            return R.drawable.dis_100_icon;
        } else if(batteryLevel <= 80 && batteryLevel > 60) {
            return R.drawable.dis_080_icon;
        } else if(batteryLevel <=60 && batteryLevel > 40) {
            return R.drawable.dis_060_icon;
        } else if(batteryLevel <=40 && batteryLevel > 20) {
            return R.drawable.dis_040_icon;
        } else if(batteryLevel <=20 && batteryLevel >= 0) {
            return R.drawable.dis_020_icon;
        }else
        	return R.drawable.missing_icon;
    }
    private int getUnknown() {
       	return R.drawable.missing_icon;
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
