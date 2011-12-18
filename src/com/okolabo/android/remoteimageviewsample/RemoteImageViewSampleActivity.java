
package com.okolabo.android.remoteimageviewsample;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView.ScaleType;

import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndEntry;
import com.google.code.rome.android.repackaged.com.sun.syndication.feed.synd.SyndFeed;
import com.okolabo.android.network.MultiTheadImageDownloader;
import com.okolabo.android.view.RemoteImageView;

public class RemoteImageViewSampleActivity extends Activity {

    private static final String TAG = "RemoteImageViewSampleActivity";

    private GridView mGrid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mGrid = (GridView) findViewById(R.id.grid);
        // はてなフォトライフから猫のタグのついたRSSを取得、解析する
        new HatenaPhotoLifeRssReaderTask(this).execute();
    }

    /**
     * はてなフォトライフの猫のタグのついたRSSを解析する
     * 
     * @author Toyoaki Oko <chariderpato@gmail.com>
     */
    private class HatenaPhotoLifeRssReaderTask extends AsyncTask<Void, Void, ArrayList<String>> {

        protected Context mContext;
        protected ProgressDialog mProgress;

        public HatenaPhotoLifeRssReaderTask(Context context) {
            this.mContext = context;
        }
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress = new ProgressDialog(mContext);
            // 戻るボタンが押された場合の処理
            mProgress.setOnCancelListener(new OnCancelListener() {
                
                public void onCancel(DialogInterface dialog) {
                    HatenaPhotoLifeRssReaderTask.this.cancel(true);
                }
            });
            mProgress.setMessage(mContext.getString(R.string.now_loading));
            mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgress.show();
        }
        
        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            ArrayList<String> imageUrls = new ArrayList<String>();
            final String feedUrl = "http://f.hatena.ne.jp/t/%E7%8C%AB?mode=rss";
            RssAtomFeedRetriever feedRetriever = new RssAtomFeedRetriever();
            SyndFeed feed = feedRetriever.getMostRecentNews(feedUrl);
            List<SyndEntry> entries = feed.getEntries();
            
            // RSSより、サムネイル用画像URLを抽出する
            for (SyndEntry entry : entries) {
                ArrayList list = (ArrayList) entry.getForeignMarkup();
                for (int i = 0; i < list.size(); i++) {
                    Element elm = (Element) list.get(i);
                    if (elm.getName().equals("imageurlmedium")) {
                        imageUrls.add(elm.getValue());
                        continue;
                    }
                }
            }
            return imageUrls;
        }

        @Override
        protected void onPostExecute(ArrayList<String> imageUrls) {
            mProgress.dismiss();
            if (isCancelled()) {
                return;
            }
            if (imageUrls != null) {
                HatenaPhotoLifeAdapter adapter = new HatenaPhotoLifeAdapter(imageUrls);
                mGrid.setAdapter(adapter);
                // 画像のダウンロードを実行する
                MultiTheadImageDownloader.execute(RemoteImageViewSampleActivity.this, imageUrls);
            }
            super.onPostExecute(imageUrls);
        }
    }

    /**
     * GridViewに渡すAdapterクラス
     * 
     * @author Toyoaki Oko <chariderpato@gmail.com>
     */
    private class HatenaPhotoLifeAdapter extends BaseAdapter {
        private ArrayList<String> mImageUrls;
        private Resources mRes;
        public int getCount() {
            return this.mImageUrls.size();
        }

        public String getItem(int position) {
            return this.mImageUrls.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public HatenaPhotoLifeAdapter(ArrayList<String> imageUrls) {
            this.mImageUrls = imageUrls;
            this.mRes = getResources();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = new HatenaImageView(RemoteImageViewSampleActivity.this);
                holder = new ViewHolder();
                holder.thumbnail = (HatenaImageView) convertView;
                int thumbnailSize = mRes.getDimensionPixelSize(R.dimen.thumbnail_width);
                holder.thumbnail.setLayoutParams(new GridView.LayoutParams(thumbnailSize, thumbnailSize));
                holder.thumbnail.setScaleType(ScaleType.FIT_CENTER);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            // 画像のURL
            final String imageUrlMedium = this.getItem(position);
            // RemoteImageViewに画像読み込み依頼を投げる
            Message msg = holder.thumbnail.mHandler.obtainMessage(RemoteImageView.IMG_DOWNLOADING,
                    1, 0, imageUrlMedium);
            msg.sendToTarget();
            return convertView;
        }
    }

    private class ViewHolder {
        HatenaImageView thumbnail;
    }

    private class HatenaImageView extends RemoteImageView {

        public HatenaImageView(Context context) {
            super(context);
        }

        /**
         * 画像が見つからなかった場合に表示する画像
         */
        @Override
        public void setImageNotFound() {
            setImageResource(R.drawable.not_found);
        }

        /**
         * 画像のダウンロード中に表示する画像
         */
        @Override
        public void setImageNowLoading() {
            setImageResource(R.drawable.now_loading);
        }
    }
}
