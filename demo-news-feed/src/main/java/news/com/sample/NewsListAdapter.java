package news.com.sample;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;
import news.com.sample.news.IMediaEntity;
import news.com.sample.news.INewsEntity;
import news.com.sample.reactive.IOnClickObservable;

public class NewsListAdapter
    extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    implements IOnClickObservable<INewsEntity> {

    private final LayoutInflater mInflater;

    // State.
    private final List<INewsEntity> mItems = new ArrayList<>();

    // Reactive.
    private PublishSubject<INewsEntity> mOnClickSub = PublishSubject.create();

    public NewsListAdapter(final Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        final View view = mInflater.inflate(
            R.layout.list_item_news, parent, false);
        final RecyclerView.ViewHolder holder = new ViewHolder(view);

        // The observable and subject keep strong reference to the view.
        // Make sure to dispose the subject when the activity terminates if you
        // subscribe to the subject.
        RxView.clicks(view)
              .map(new Function<Object, INewsEntity>() {
                  @Override
                  public INewsEntity apply(Object ignored) throws Exception {
                      INewsEntity entity = mItems.get(holder.getAdapterPosition());
                      Log.d("xyz", "" + entity);
                      return entity;
                  }
              })
              .subscribeWith(mOnClickSub);

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder,
                                 int position) {
        final INewsEntity entity = mItems.get(position);
        final TextView newsTitle = ((ViewHolder) holder).newsTitle;
        final DraweeView imageView = ((ViewHolder) holder).imageView;

        // Set title.
        newsTitle.setText(entity.getTitle());

        // Load image.
        if (!entity.getMediaEntity().isEmpty()) {
            IMediaEntity mediaEntity = entity.getMediaEntity().get(0);
            String thumbnailURL = mediaEntity.getUrl();
            DraweeController draweeController = Fresco.newDraweeControllerBuilder().setImageRequest(ImageRequest.fromUri
                (Uri.parse(thumbnailURL))).setOldController(imageView.getController()).build();

            imageView.setController(draweeController);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public Observable<INewsEntity> getOnClickObservable() {
        return mOnClickSub;
    }

    public List<INewsEntity> getItems() {
        return mItems;
    }

    public void setItems(final List<INewsEntity> items) {
        // TODO: Apply DiffUtil.
        mItems.clear();
        mItems.addAll(items);

        notifyDataSetChanged();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clazz //////////////////////////////////////////////////////////////////

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.news_title)
        TextView newsTitle;
        @BindView(R.id.news_item_image)
        DraweeView imageView;

        ViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }
    }
}
