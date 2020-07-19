import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.IWallpaperManager;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Live Wallpaper Manager class for setting Android Live Wallpaper programmatically without any permissions
 * For setting wallpaper manager uses own WallpaperConnection service and reflect @hide android API methods
 *
 * Created by Line'R (seriniti320@gmail.com) 19.07.2020
 */
@SuppressLint("StaticFieldLeak")
public class LiveWallpaperManager {
    private WallpaperManager wallpaperManager;
    private IWallpaperManager iWallpaperManager;
    private WallpaperConnection wallpaperConnection;
    private Activity activity;
    private IBinder windowToken;
    private static LiveWallpaperManager liveWallpaperManager;

    /**
     * Get instance of Live Wallpaper Manager
     *
     * IMPORTANT
     * Need to be implemented in MainActivity class!
     *
     * @param activity MainActivity for correctly working manager
     * @return instance of Live Wallpaper Manager
     */
    public static LiveWallpaperManager getInstance(Activity activity) {
        if(liveWallpaperManager == null)
            return liveWallpaperManager = new LiveWallpaperManager(activity);
        return liveWallpaperManager;
    }

    /**
     * Internal constructor for Live Wallpaper Manager
     *
     * @param activity MainActivity for correctly working manager
     * {windowToken} uses for WallpaperConnection to bind Live Wallpaper into our application
     * {wallpaperManager} uses for reflection and settings wallpaper offset
     * {iWallpaperManager} private hidden service of {wallpaperManager} used in OS for applying Live Wallpapers
     */
    private LiveWallpaperManager(Activity activity) {
        this.activity = activity;
        this.windowToken = activity.getWindow().getDecorView().getWindowToken();
        this.wallpaperManager = WallpaperManager.getInstance(activity);
        try {
            this.iWallpaperManager = (IWallpaperManager) invokeMethod(wallpaperManager, "getIWallpaperManager");
        } catch (InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method using for settings Live Wallpaper for WallpaperConnection
     * @param wallpaper WallpaperInfo using to get all required fields for applying
     */
    public void setWallpaper(WallpaperInfo wallpaper){
        Intent wallpaperServiceIntent = new Intent(WallpaperService.SERVICE_INTERFACE);
        wallpaperServiceIntent.setClassName(wallpaper.getPackageName(), wallpaper.getServiceName());
        wallpaperConnection = new WallpaperConnection(activity, wallpaperServiceIntent, windowToken);
        if(iWallpaperManager != null){
            try {
                iWallpaperManager.setWallpaperComponent(wallpaperServiceIntent.getComponent());
                wallpaperManager.setWallpaperOffsetSteps(0, 0);
                if(!wallpaperConnection.connect()){
                    wallpaperConnection.wallpaperEngine.setVisibility(true);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method uses to handle touch and send it to Android Wallpaper Service
     */
    public void handleTouch(MotionEvent motionEvent){
        if(wallpaperConnection != null && wallpaperConnection.wallpaperEngine != null){
            MotionEvent historyEvent = MotionEvent.obtainNoHistory(motionEvent);
            try {
                wallpaperConnection.wallpaperEngine.dispatchPointer(historyEvent);
                int eventAction = motionEvent.getAction();
                switch (eventAction){
                    case MotionEvent.ACTION_UP:
                        wallpaperConnection.wallpaperEngine.dispatchWallpaperCommand(WallpaperManager.COMMAND_TAP, Math.round(motionEvent.getX()), Math.round(motionEvent.getY()), 0, null);
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        int pointerIndex = motionEvent.getActionIndex();
                        wallpaperConnection.wallpaperEngine.dispatchWallpaperCommand(WallpaperManager.COMMAND_TAP, Math.round(motionEvent.getX(pointerIndex)), Math.round(motionEvent.getY(pointerIndex)), 0, null);
                        break;
                    default:
                        break;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method used for access hidden Android API in WindowManager
     * @param wallpaperManager WindowManager to handle private hidden methods
     * @param methodName Method name for execution
     * @return Result of execution
     * @throws InvocationTargetException throws when result mismatch with required type
     * @throws IllegalAccessException throws when some argument are wrong
     */
    public static Object invokeMethod(WallpaperManager wallpaperManager, String methodName) throws InvocationTargetException, IllegalAccessException {
        Object object = new Object();
        Method[] methods = wallpaperManager.getClass().getDeclaredMethods();
        for(Method method:methods){
            if(method.getName().equalsIgnoreCase(methodName)){
                return method.invoke(wallpaperManager);
            }
        }
        return object;
    }
}
