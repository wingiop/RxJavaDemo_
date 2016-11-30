package com.rxjavacase1;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.rxjavacase1.adapter.AppListAdapter;
import com.rxjavacase1.model.AppInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * @author baron (baronzhang[at]anjuke[dot]com)
 */
public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener
{
    @BindView(R.id.pull_down_srl)
    SwipeRefreshLayout mPullDownSRL;
    @BindView(R.id.app_list_rv)
    RecyclerView mAppListRV;

    private List<AppInfo> mAppInfoList;
    private AppListAdapter mAppListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mAppListRV.setLayoutManager(linearLayoutManager);
        mAppInfoList = new ArrayList<>();
        mAppListAdapter = new AppListAdapter(mAppInfoList);
        mAppListRV.setAdapter(mAppListAdapter);

        mPullDownSRL.setOnRefreshListener(this);

        mPullDownSRL.post(new Runnable()
        {
            @Override
            public void run()
            {
                // 实现自动下拉刷新
                mPullDownSRL.setRefreshing(true);
                onRefresh();
            }
        });
    }

    @Override
    public void onRefresh()
    {
//        if (mAppInfoList != null)
//        {
//            mAppInfoList.clear();
//            mAppListAdapter.notifyDataSetChanged();
//        }
        loadApp();
    }

    private void loadApp()
    {
        final PackageManager pm = MainActivity.this.getPackageManager();

        Observable.create(new Observable.OnSubscribe<ApplicationInfo>()
                {
                    @Override
                    public void call(Subscriber<? super ApplicationInfo> subscriber)
                    {
                        List<ApplicationInfo> applicationInfos = getApplicationInfoList(pm);
                        for (ApplicationInfo info : applicationInfos)
                        {
                            subscriber.onNext(info);
                        }
                        subscriber.onCompleted();
                    }
                })
                // 过滤出是否是系统应用
                .filter(new Func1<ApplicationInfo, Boolean>()
                {
                    @Override
                    public Boolean call(ApplicationInfo applicationInfo)
                    {
                        return (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                // 把ApplicationInfo装换为AppInfo
                .map(new Func1<ApplicationInfo, AppInfo>()
                {
                    @Override
                    public AppInfo call(ApplicationInfo applicationInfo)
                    {
                        AppInfo appInfo = new AppInfo();
                        appInfo.setAppName(applicationInfo.loadLabel(pm).toString());
                        appInfo.setAppIcon(applicationInfo.loadIcon(pm));
                        return appInfo;
                    }
                })
                // 发射、发布消息
                .subscribe(new Subscriber<AppInfo>()
                {
                    @Override
                    public void onStart()
                    {
                        super.onStart();
                        // 清理数据
                        mAppInfoList.clear();
                        mAppListAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCompleted()
                    {
                        mAppListAdapter.notifyDataSetChanged();
                        mPullDownSRL.setRefreshing(false);
                    }

                    @Override
                    public void onError(Throwable e)
                    {

                    }

                    @Override
                    public void onNext(AppInfo appInfo)
                    {
                        mAppInfoList.add(appInfo);
                    }
                });
    }

    private List<ApplicationInfo> getApplicationInfoList(final PackageManager pm)
    {
        return pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

}