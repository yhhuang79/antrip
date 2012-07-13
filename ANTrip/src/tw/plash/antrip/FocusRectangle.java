
package tw.plash.antrip;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class FocusRectangle extends View {

    @SuppressWarnings("unused")
    private static final String TAG = "FocusRectangle";

    private int xActual, yActual;

    public FocusRectangle(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private void setDrawable(int resid) {
        setBackgroundDrawable(getResources().getDrawable(resid));
    }

    public void showStart() {
        setDrawable(R.drawable.sk_auto_focusing);
    }

    public void showSuccess() {
        setDrawable(R.drawable.sk_auto_focused);
    }

    public void showFail() {
        setDrawable(R.drawable.sk_auto_fail);
    }

    public void clear() {
        setBackgroundDrawable(null);
    }

    public void setPosition(int x, int y) {
        if (x >= 0 && y >= 0) {
            xActual = x;
            yActual = y;
            redraw();
        }
    }

    public void redraw() {
        int size = getWidth() / 2;
        Log.e("focus rectangle", "size= " + getWidth());
        Log.e("focus rectangle", "left= " + (xActual - size) + ", top= " + (yActual - size) + ", right= " + (xActual + size) + ", bottom= " + (yActual + size));
        this.layout(xActual - size, yActual - size, xActual + size, yActual + size);
        invalidate();
    }
}
