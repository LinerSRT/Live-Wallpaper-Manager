import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.service.wallpaper.IWallpaperConnection;
import android.service.wallpaper.IWallpaperEngine;
import android.service.wallpaper.IWallpaperService;

/**
 * Wallpaper Connection Service for Live Wallpaper Manager
 *
 * Created by Line'R (seriniti320@gmail.com) 19.07.2020
 */
public class WallpaperConnection extends IWallpaperConnection.Stub implements ServiceConnection {
    IWallpaperService wallpaperService;
    public IWallpaperEngine wallpaperEngine;
    boolean connected;
    private Intent intent;
    private Context context;
    private IBinder windowToken;

    /**
     * Default constructor
     * @param context Application context
     * @param intent Wallpaper Service Intent
     * @param windowToken IBinder to bind engine to Window
     */
    public WallpaperConnection(Context context, Intent intent, IBinder windowToken) {
        this.intent = intent;
        this.context = context;
        this.windowToken = windowToken;
    }

    /**
     * Bind Wallpaper Connection
     * @return true if connection success
     */
    public boolean connect() {
        synchronized (this) {
            if (!context.bindService(intent, this, Context.BIND_AUTO_CREATE)) {
                return false;
            }
            connected = true;
            return true;
        }
    }

    /**
     * UnBind Wallpaper Connection
     */
    public void disconnect() {
        synchronized (this) {
            connected = false;
            if (wallpaperEngine != null) {
                try {
                    wallpaperEngine.destroy();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                wallpaperEngine = null;
            }
            context.unbindService(this);
            wallpaperService = null;
        }
    }

    /**
     * This method called when WallpaperConnection attached to IWallpaperManager
     * @param name Wallpaper ComponentName
     * @param service IBinder IWallpaperService
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        wallpaperService = IWallpaperService.Stub.asInterface(service);
        try {
            wallpaperService.attach(this, windowToken, 1004, true, Resources.getSystem().getDisplayMetrics().widthPixels, Resources.getSystem().getDisplayMetrics().heightPixels, new Rect(0, 0, 0, 0));
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    /**
     * This method called when WallpaperConnection detached from IWallpaperManager
     * @param name Wallpaper ComponentName
     */
    @Override
    public void onServiceDisconnected(ComponentName name) {
        wallpaperService = null;
        wallpaperEngine = null;
    }

    /**
     * This method uses for attach IWallpaperEngine to own WallpaperEngine
     * @param engine system IWallpaperEngine
     */
    @Override
    public void attachEngine(IWallpaperEngine engine) {
        synchronized (this) {
            if (connected) {
                wallpaperEngine = engine;
                try {
                    engine.setVisibility(true);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    engine.destroy();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Dummy method not using
     * @param name
     * @return
     */
    @Override
    public ParcelFileDescriptor setWallpaper(String name) {
        return null;
    }

    /**
     * Called when engine are visible
     * @throws RemoteException
     */
    @Override
    public void engineShown(IWallpaperEngine engine) {
    }
}