# Live Wallpaper Manager
This Manager can be useful if you want apply Live Wallpaper without permissions

![](https://img.shields.io/badge/Working-Passed-green) 
![](https://img.shields.io/badge/API-21%2B-green) 


###### 1. Declare Manger in your MainActivity Class
```java
public static LiveWallpaperManager liveWallpaperManager;
```


###### 2. Init Manager
```java
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        liveWallpaperManager = LiveWallpaperManager.getInstance(this);
        setContentView(R.layout.activity_main);
	}
```

###### 3. Dispatch touch event for Manager
```java
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        liveWallpaperManager.handleTouch(ev);
        return super.dispatchTouchEvent(ev);
    }
```



###### 3. Apply live wallpaper to main activity window
```java
	liveWallpaperManager.setWallpaper(/*WallpaperInfo*/ wallpaperInfo);
```


###### 4. Done






