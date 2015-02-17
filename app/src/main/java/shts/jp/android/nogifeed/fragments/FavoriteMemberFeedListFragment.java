package shts.jp.android.nogifeed.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.apache.http.Header;

import java.util.List;

import shts.jp.android.nogifeed.R;

// TODO: お気に入りメンバーがいないときは EmptyView を表示する
// TODO: インストール後に何度か起動された時、アプリ評価を誘導する View を表示する
// TODO: View にお気に入り機能と共有機能を追加する
public class FavoriteMemberFeedListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = FavoriteMemberFeedListFragment.class.getSimpleName();

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private List<String> mFavoriteUrls;
    private RecyclerView mRecyclerView;
    private final shts.jp.android.nogifeed.models.Entries mEntries = new shts.jp.android.nogifeed.models.Entries();
    private int mRequestCounter = 0;
    private static final Object LOCK_OBJECT = new Object();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite_feed_list, null);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true); // アイテムは固定サイズ

        // SwipeRefreshLayoutの設定
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.nogifeed, R.color.nogifeed, R.color.nogifeed, R.color.nogifeed);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupFavoriteMemberFeed();
    }

    private void setupFavoriteMemberFeed() {
        mFavoriteUrls = shts.jp.android.nogifeed.utils.DataStoreUtils.getAllFavoriteLink(getActivity());
        if (mFavoriteUrls == null || mFavoriteUrls.isEmpty()) {
            if (mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
            shts.jp.android.nogifeed.common.Logger.d(TAG, "setupFavoriteMemberFeed() : no favorite feed.");
            Toast.makeText(getActivity(), R.string.empty_favorite, Toast.LENGTH_LONG).show();
            return;
        }
        for (String s : mFavoriteUrls) {
            shts.jp.android.nogifeed.common.Logger.v(TAG, "setupFavoriteMemberFeed() : favorite url(" + s + ")");
            getAllFeed(s);
        }
    }

    private void getAllFeed(String url) {
        // clear feed list before add new feed.
        mEntries.clear();

        boolean ret = shts.jp.android.nogifeed.api.AsyncRssClient.read(getActivity().getApplicationContext(),
                url, new shts.jp.android.nogifeed.listener.RssClientListener() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, shts.jp.android.nogifeed.models.Entries entries) {

                        synchronized (LOCK_OBJECT) {
                            shts.jp.android.nogifeed.common.Logger.v(TAG, "getAllFeed() : mRequestCounter("
                                    + mRequestCounter + ") favorite url size("
                                    + mFavoriteUrls.size() + ")");

                            mRequestCounter++;
                            mEntries.cat(entries);
                            if (mRequestCounter >= mFavoriteUrls.size()) {
                                mRequestCounter = 0;
                                setupAdapter(mEntries.sort());
                            }
                        }

                        if (mSwipeRefreshLayout.isRefreshing()) {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        // Show error toast
                        Toast.makeText(getActivity(), getResources().getString(R.string.feed_failure),
                                Toast.LENGTH_SHORT).show();

                        synchronized (LOCK_OBJECT) {
                            mRequestCounter++;
                            if (mRequestCounter >= mFavoriteUrls.size()) {
                                mRequestCounter = 0;
                                setupAdapter(mEntries.sort());
                            }
                        }
                        if (mSwipeRefreshLayout.isRefreshing()) {
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                });

        if (!ret) {
            // Show error toast
            Toast.makeText(getActivity(), getResources().getString(R.string.feed_failure),
                    Toast.LENGTH_SHORT).show();
            if (mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    private void setupAdapter(shts.jp.android.nogifeed.models.Entries entries) {
        shts.jp.android.nogifeed.common.Logger.v("setupAdapter()", "setupAdapter() : size( " + entries.size() + ") entry("
                + entries.toString() + ")");
        mRecyclerView.setAdapter(new shts.jp.android.nogifeed.adapters.FavoriteFeedListAdapter(getActivity(), entries));
    }

    @Override
    public void onRefresh() {
        setupFavoriteMemberFeed();
    }

}