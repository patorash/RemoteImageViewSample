package com.okolabo.android.network;

import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.okolabo.android.db.ImageCacheDB;

public class ImageDownloadCacheTask extends AsyncTask<String, Void, Void> {
    
    private static final String TAG = "ImageDownloadCacheTask";

    private Context mContext;
    
    public ImageDownloadCacheTask(Context context) {
        mContext = context;
    }
    
    @Override
    protected Void doInBackground(String... urls) {
        ImageCacheDB db = ImageCacheDB.getInstance(mContext);
        long id = 0;
        HttpClient httpClient = null;
        for (String url : urls) {
            Cursor c = db.exists(url);
            if (!c.moveToFirst()) {
                try {
                    id = db.insert(url);
                    HttpGet httpRequest = new HttpGet(url);
                    httpClient = new DefaultHttpClient();
                    HttpResponse response;
                    response = (HttpResponse) httpClient.execute(httpRequest);
                    HttpEntity entity = response.getEntity();
                    BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
                    
                    // ファイルに保存
                    String filename = String.format("%06d", id);
                    String type = entity.getContentType().getValue();
                    FileOutputStream stream = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
                    InputStream is = bufHttpEntity.getContent();
                    byte[] data = new byte[4096];
                    int size;
                    while((size = is.read(data)) > 0) {
                        stream.write(data, 0, size);
                    }
                    stream.close();
                    is.close();
                    // キャッシュディレクトリに画像を保存する
                    db.update(id, filename, type);
                } catch (Exception e) {
                    Log.e(TAG, e.getClass().getSimpleName(), e);
                    if (id > 0) {
                        try {
                            db.delete(id);
                        } catch (Exception e2) {
                            Log.e(TAG, e2.getClass().getSimpleName(), e2);
                        }
                    }
                } finally {
                    // クライアントを終了させる
                    if (httpClient != null) {
                        httpClient.getConnectionManager().shutdown();
                    }
                }
            }
            c.close();
        }
        return null;
    }
}
