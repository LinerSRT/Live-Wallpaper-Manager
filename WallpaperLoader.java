import android.annotation.SuppressLint;
import android.app.WallpaperInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.service.wallpaper.WallpaperService;
import android.service.wallpaper.WallpaperSettingsActivity;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.liner.linerlauncher.LW.LiveWallpaperManager.WATCHFACE;
import static com.liner.linerlauncher.LW.LiveWallpaperManager.WATCHFACE_CONFIG;
import static com.liner.linerlauncher.LW.LiveWallpaperManager.WATCHFACE_CONFIG_INTENT;
import static com.liner.linerlauncher.LW.LiveWallpaperManager.WATCHFACE_PREVIEW;
import static com.liner.linerlauncher.LW.LiveWallpaperManager.WATCHFACE_PREVIEW_CIRCULAR;
import static com.liner.linerlauncher.LW.LiveWallpaperManager.WATCHFACE_PREVIEW_CLOCKWORK;
import static com.liner.linerlauncher.LW.LiveWallpaperManager.WATCHFACE_PREVIEW_CLOCKWORK_CIRCULAR;


/**
 * Wallpaper Loader class used for async load all live wallpapers and watchfaces from device
 * <p>
 * Created by Line'R (seriniti320@gmail.com) 19.07.2020
 */
@SuppressLint("StaticFieldLeak")
public class WallpaperLoader extends AsyncTask<Void, LiveWallpaperItem, Void> {
    private IWallpaperLoader iWallpaperLoader;
    private Context context;

    /**
     * Constructor for Wallpaper Loader
     * @param context Application context
     * @param iWallpaperLoader Interface for receive loaded LiveWallpaperItem object
     */
    public WallpaperLoader(Context context, IWallpaperLoader iWallpaperLoader) {
        this.iWallpaperLoader = iWallpaperLoader;
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Intent filter = new Intent(WallpaperService.SERVICE_INTERFACE);
        filter.addCategory(WATCHFACE);
        List<ResolveInfo> list = context.getPackageManager().queryIntentServices(filter, PackageManager.GET_META_DATA);
        final PackageManager packageManager = context.getPackageManager();
        Collections.sort(list, new Comparator<ResolveInfo>() {
            final Collator collator;

            {
                collator = Collator.getInstance();
            }

            @Override
            public int compare(ResolveInfo t1, ResolveInfo t2) {
                return collator.compare(t1.loadLabel(packageManager), t2.loadLabel(packageManager));
            }
        });
        for (ResolveInfo resolveInfo : list) {
            WallpaperInfo wallpaperInfo;
            LiveWallpaperItem liveWallpaperItem = new LiveWallpaperItem();
            try {
                wallpaperInfo = new WallpaperInfo(context, resolveInfo);
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
                continue;
            }
            liveWallpaperItem.setName(resolveInfo.loadLabel(packageManager).toString());
            liveWallpaperItem.setIntent(new Intent(WallpaperService.SERVICE_INTERFACE));
            liveWallpaperItem.getIntent().setClassName(wallpaperInfo.getPackageName(), wallpaperInfo.getServiceName());
            liveWallpaperItem.setWallpaperInfo(wallpaperInfo);
            Bundle metaData = resolveInfo.serviceInfo.metaData;
            int previewID = metaData.getInt(WATCHFACE_PREVIEW);
            int circlePreviewID = metaData.getInt(WATCHFACE_PREVIEW_CIRCULAR);
            int clockPreviewID = metaData.getInt(WATCHFACE_PREVIEW_CLOCKWORK);
            int circleClockPreviewID = metaData.getInt(WATCHFACE_PREVIEW_CLOCKWORK_CIRCULAR);
            try {
                Context ctx = context.createPackageContext(resolveInfo.serviceInfo.packageName, Context.CONTEXT_IGNORE_SECURITY);
                Drawable drawable = null;
                if (isValidID(previewID)) {
                    drawable = ctx.getResources().getDrawable(previewID);
                }
                if (isValidID(circlePreviewID)) {
                    drawable = ctx.getResources().getDrawable(circlePreviewID);
                }
                if (isValidID(clockPreviewID)) {
                    drawable = ctx.getResources().getDrawable(clockPreviewID);
                }
                if (isValidID(circleClockPreviewID)) {
                    drawable = ctx.getResources().getDrawable(circleClockPreviewID);
                }
                liveWallpaperItem.setPreview(drawable);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            String configurationAction = metaData.getString(WATCHFACE_CONFIG);
            if (configurationAction != null && configurationAction.length() > 0) {
                Intent configIntent = new Intent(configurationAction);
                configIntent.addCategory(WATCHFACE_CONFIG_INTENT);
                configIntent.addCategory(Intent.CATEGORY_DEFAULT);
                liveWallpaperItem.setConfigIntent(configIntent);
            } else {
                if (wallpaperInfo.getSettingsActivity() != null && wallpaperInfo.getSettingsActivity().length() > 0) {
                    Intent standartConfig = new Intent();
                    standartConfig.setComponent(new ComponentName(wallpaperInfo.getPackageName(), wallpaperInfo.getSettingsActivity()));
                    standartConfig.putExtra(WallpaperSettingsActivity.EXTRA_PREVIEW_MODE, true);
                    liveWallpaperItem.setIntent(standartConfig);
                }
            }
            publishProgress(liveWallpaperItem);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(LiveWallpaperItem... values) {
        for (LiveWallpaperItem item : values)
            if (iWallpaperLoader != null)
                iWallpaperLoader.onWallpaperLoaded(item);
        super.onProgressUpdate(values);
    }

    /**
     * Query if ID exist in MetaData from Bundle
     * @param id ID for check
     * @return true if ID is valid
     */
    private boolean isValidID(int id) {
        return id != 0;
    }

    /**
     * IWallpaperLoader interface for receive loaded LiveWallpaperItem object
     */
    public interface IWallpaperLoader {
        void onWallpaperLoaded(LiveWallpaperItem liveWallpaperItem);
    }
}
