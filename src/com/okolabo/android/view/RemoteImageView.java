package com.okolabo.android.view;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.okolabo.android.db.ImageCacheDB;
import com.okolabo.android.db.ImageCacheDB.CacheColumn;

abstract public class RemoteImageView extends ImageView {
    private static final String TAG = "RemoteImageView";
    
    public static final int IMG_DOWNLOADING = 1;
    
    public final Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            // 1秒毎にキャッシュからヒットするか検索する
            // ヒットしたら、検索をやめる
            // 画像表示に10回挑戦してダメだったらフラグを立てるとかしないとマズい
            if (msg.what == IMG_DOWNLOADING) {
                Context cxt = getContext();
                final String url = (String)msg.obj;
                int count = msg.arg1;
                ImageCacheDB db = ImageCacheDB.getInstance(cxt);
                if (url != null && !url.equals("")) {
                    final Cursor c = db.existsFile(url);
                    if (c.moveToFirst()) {
                        final String filename = c.getString(c.getColumnIndex(CacheColumn.NAME));
                        final String type = c.getString(c.getColumnIndex(CacheColumn.TYPE));
                        if (type.equals("image/jpg")
                                || type.equals("image/jpeg")
                                || type.equals("image/png")
                                || type.equals("image/gif")) {
                            Drawable drawable = Drawable.createFromPath(cxt.getFileStreamPath(filename).getAbsolutePath());
                            setImageDrawable(drawable);
                            setVisibility(RemoteImageView.VISIBLE);
                        } else {
                            // 表示できる類いではない
                            setImageNotFound();
                        }
                    } else {
                        if (count <= 10) {
                            setImageNowLoading();
                            msg = obtainMessage(IMG_DOWNLOADING, ++count, 0, url);
//                            msg = obtainMessage(IMG_DOWNLOADING, url);
                            long current = SystemClock.uptimeMillis();
                            long nextTime = current + 1000;
                            sendMessageAtTime(msg, nextTime);
                        } else {
                            // チャレンジ10回して失敗したので失敗扱い
                            setImageNotFound();
                        }
                    }
                    c.close();
                } else {
                    setImageNotFound();
                }
                // db.close();
            }
        }
    };

    public RemoteImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RemoteImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RemoteImageView(Context context) {
        super(context);
    }

    abstract public void setImageNotFound();
    
    abstract public void setImageNowLoading();
}
