import android.app.WallpaperInfo;
import android.content.Intent;
import android.graphics.drawable.Drawable;

/**
 * Holder object for WallpaperLoader
 * <p>
 * Created by Line'R (seriniti320@gmail.com) 19.07.2020
 */
public class LiveWallpaperItem {
    public String name;
    public Drawable preview;
    public WallpaperInfo wallpaperInfo;
    public Intent intent;
    public Intent configIntent;

    public LiveWallpaperItem() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Drawable getPreview() {
        return preview;
    }

    public void setPreview(Drawable preview) {
        this.preview = preview;
    }

    public WallpaperInfo getWallpaperInfo() {
        return wallpaperInfo;
    }

    public void setWallpaperInfo(WallpaperInfo wallpaperInfo) {
        this.wallpaperInfo = wallpaperInfo;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public Intent getConfigIntent() {
        return configIntent;
    }

    public void setConfigIntent(Intent configIntent) {
        this.configIntent = configIntent;
    }

    @Override
    public String toString() {
        return "LiveWallpaperItem{" +
                "name='" + name + '\'' +
                ", preview=" + preview +
                ", wallpaperInfo=" + wallpaperInfo +
                ", intent=" + intent +
                ", configIntent=" + configIntent +
                '}';
    }
}
