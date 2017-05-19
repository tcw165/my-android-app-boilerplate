package my.demo.news;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;
import com.facebook.imagepipeline.request.ImageRequest;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import my.demo.news.news.IMediaEntity;
import my.demo.news.news.INewsEntity;
import my.demo.news.news_detail.INewsDetailContract;
import my.demo.news.news_detail.NewsDetailPresenter;

/**
 * News detail view
 */
public class DetailViewActivity
    extends AppCompatActivity
    implements INewsDetailContract.View {

    // Data.
    private INewsEntity mNewsEntity;

    // Views.
    @BindView(R.id.title)
    TextView mTitleView;
    @BindView(R.id.news_image)
    DraweeView mImageView;
    @BindView(R.id.summary_content)
    TextView mSummaryView;
    @BindView(R.id.full_story_link)
    Button mButtonView;

    // View binder.
    private Unbinder mUnbinder;

    private INewsDetailContract.UserInteraction mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);

        mUnbinder = ButterKnife.bind(this);

        // Get data.
        final Bundle extras = getIntent().getExtras();
        if ((mNewsEntity = extras.getParcelable(Intent.ACTION_ATTACH_DATA)) != null) {

            // Title
            mTitleView.setText(mNewsEntity.getTitle());
            // Summary.
            mSummaryView.setText(mNewsEntity.getSummary());
            // External link button.
            if (TextUtils.isEmpty(mNewsEntity.getArticleUrl())) {
                mButtonView.setVisibility(View.GONE);
            }
            // Image.
            final List<IMediaEntity> medias = mNewsEntity.getMediaEntity();
            final String imageUrl = !medias.isEmpty() ?
                medias.get(medias.size() - 1).getUrl() : "";
            if (!TextUtils.isEmpty(imageUrl)) {
                DraweeController draweeController = Fresco
                    .newDraweeControllerBuilder()
                    .setImageRequest(ImageRequest.fromUri(Uri.parse(imageUrl)))
                    .setOldController(mImageView.getController()).build();
                mImageView.setController(draweeController);
            }
        }

        mPresenter = new NewsDetailPresenter(this);
    }

    public void onFullStoryClicked(View view) {
        if (mNewsEntity == null) return;

        mPresenter.openFullStory(mNewsEntity.getArticleUrl());
    }

    @Override
    public void showFullStoryUi(String url) {
        if (!URLUtil.isValidUrl(url)) return;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected / Private Methods ////////////////////////////////////////////

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unbind the views.
        mUnbinder.unbind();
    }
}
