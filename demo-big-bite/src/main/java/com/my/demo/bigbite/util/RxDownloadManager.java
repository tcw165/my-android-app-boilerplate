//package com.my.demo.bigbite.util;
//
//import android.app.DownloadManager;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//
//import java.io.File;
//import java.util.HashMap;
//import java.util.Map;
//
//import io.reactivex.Observable;
//import io.reactivex.Observer;
//import io.reactivex.disposables.Disposable;
//import io.reactivex.subjects.Subject;
//
//public class RxDownloadManager extends Observable<File> {
//
//    private static final Map<String, Long> sTaskIds = new HashMap<>();
//
//    private final String mUrl;
//
//    public RxDownloadManager(final String url) {
//        mUrl = url;
//    }
//
//    @Override
//    protected void subscribeActual(Observer<? super File> observer) {
//        // Get DownloadManager
//        final DownloadManager downloadManager = (DownloadManager)
//            context.getSystemService(Context.DOWNLOAD_SERVICE);
//        final Disposable disposable = new DownloadStatusReceiver(observer);
//
//        if (sTaskIds.containsKey(mUrl)) {
//            final long taskId = sTaskIds.get(mUrl);
//        } else {
//            // A new task.
//            final long taskId = downloadManager.enqueue(getFace68Request(dirName));
//        }
//    }
//
//    ///////////////////////////////////////////////////////////////////////////
//    // Protected / Private Methods ////////////////////////////////////////////
//
//    // TODO: Refactor this in case of that too many services are registered.
//    private static class DownloadStatusReceiver
//        extends BroadcastReceiver
//        implements Disposable {
//
//        private final long mTaskId;
//        private final Subject<File> mSubject;
//
//        public DownloadStatusReceiver(final long taskId,
//                                      final Subject<File> subject) {
//            mTaskId = taskId;
//            mSubject = subject;
//        }
//
//        @Override
//        public void onReceive(Context context,
//                              Intent intent) {
//            final DownloadManager downloadManager = (DownloadManager)
//                context.getSystemService(Context.DOWNLOAD_SERVICE);
//            // The task ID.
//            final long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0L);
//            if (mTaskId != id) return;
//
////            DLibModelHelper.checkDownloadTask(downloadManager, id, mSubject);
//        }
//
//        @Override
//        public void dispose() {
//
//        }
//
//        @Override
//        public boolean isDisposed() {
//            return false;
//        }
//    }
//}
