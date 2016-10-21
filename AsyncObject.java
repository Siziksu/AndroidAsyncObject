import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public final class AsyncObject<O> {

    private Runnable runnable;
    private Handler handler;
    private boolean executing;
    private RetGen<O> action;
    private FunGen<O> success;
    private FunGen<Throwable> fail;
    private FunVoid done;
    private boolean subscribeOnMainThread;

    public AsyncObject() {}

    public AsyncObject<O> subscribeOnMainThread() {
        subscribeOnMainThread = true;
        return this;
    }

    public boolean isExecuting() {
        return executing;
    }

    public AsyncObject<O> action(final RetGen<O> action) {
        this.action = action;
        return this;
    }

    public void subscribe(final FunGen<O> success) {
        this.success = success;
        run();
    }

    public void subscribe(final FunGen<O> success, final FunGen<Throwable> fail) {
        this.success = success;
        this.fail = fail;
        run();
    }

    public AsyncObject<O> done(final FunVoid done) {
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
                    O response = action.apply();
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
            handler.post(() -> success.apply(response));
        } else {
            success.apply(response);
        }
    }

    private void fail(final Exception e) {
        if (handler != null) {
            handler.post(() -> fail.apply(e));
        } else {
            fail.apply(e);
        }
    }

    private void done() {
        if (handler != null) {
            handler.post(() -> done.apply());
        } else {
            done.apply();
        }
    }
}
