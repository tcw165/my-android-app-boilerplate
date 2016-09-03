ViewUtil
--------

It handles showing/hiding progress bar so far.

Because view stuff follows the lifecycle of the *Activity*. You are asked to pass the `Context` to the static method, `with`, to generate the *util* instance related to the given *context* for you.

#### Example

Show/Hide Progress bar:

``` java
// "this" is an activity.
ViewUtil
    .with(this)
    .setCancelable(true)
    .showProgressBar(getString(R.string.loading));

// "this" is an activity.
ViewUtil
    .with(this)
    .hideProgressBar();
```

WebApiUtil
----------

It handles the communication with the server API.

We use [retrofit](https://github.com/square/retrofit) as the HTTP/HTTPS library and the [IProgressBarView](../view/IProgressBarView.java) interface to delegate showing/hiding progress bar.

The activity/fragment is responsible to implement the `IProgressBarView ` interface to show/hide the progress bar in any way.

#### Example

Call the api and show the progress bar when it's processing:

```java
WebApiUtil
    .with(this)
    .showProgressBar(this)
    .getJsonWhatever())
```

The activity implementing the `IProgressBarView`:

```java
public class StartPageActivity extends AppCompatActivity
                               implements IProgressBarView {
    @Override
    public void showProgressBar() {
        // Show the progress bar.
    }

    @Override
    public void hideProgressBar() {
        // Hide the progress bar.
    }
}
```

PermUtil
--------

constructing...

ImageUtil
---------

constructing...
