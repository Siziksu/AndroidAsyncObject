import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public final class AsyncObject<O> {

    private Runnable runnable;
    private Handler handler;
    private boolean executing;
    private Function<O> action;
    private Provider<O> success;
    private Provider<Throwable> fail;
    private Consumer done;
    private boolean subscribeOnMainThread;

    public AsyncObject() {}

    public AsyncObject<O> subscribeOnMainThread() {
        subscribeOnMainThread = true;
        return this;
    }

    public boolean isExecuting() {
        return executing;
    }

    public AsyncObject<O> action(final Function<O> action) {
        this.action = action;
        return this;
    }

    public void subscribe(final Provider<O> success) {
        this.success = success;
        run();
    }

    public void subscribe(final Provider<O> success, final Provider<Throwable> fail) {
        this.success = success;
        this.fail = fail;
        run();
    }

    public AsyncObject<O> done(final Consumer done) {
        this.done = done;
        return this;
    }

    public void run() {
        if (action != null) {
            if (subscribeOnMainThread) {
                handler = new Handler(Looper.getMainLooper());
            } else {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    handler = new Handler();
                }
            }
            Thread thread = new Thread(obtainRunnable());
            thread.setName("async-object-thread-" + thread.getId());
            thread.start();
        } else {
            throw new RuntimeException("There is no action to be executed");
        }
    }

    private Runnable obtainRunnable() {
        if (runnable == null) {
            runnable = () -> {
                executing = true;
                try {
                    O response = action.execute();
                    if (response != null && success != null) {
                        success(response);
                    }
                } catch (Exception e) {
                    if (fail != null) {
                        fail(e);
                    } else {
                        Log.e("AsyncObject", e.getMessage(), e);
                    }
                }
                executing = false;
                if (done != null) {
                    done();
                }
            };
        }
        return runnable;
    }

    private void success(final O response) {
        if (handler != null) {
            handler.post(() -> success.provide(response));
        } else {
            success.provide(response);
        }
    }

    private void fail(final Exception e) {
        if (handler != null) {
            handler.post(() -> fail.provide(e));
        } else {
            fail.provide(e);
        }
    }

    private void done() {
        if (handler != null) {
            handler.post(() -> done.consume());
        } else {
            done.consume();
        }
    }
}
