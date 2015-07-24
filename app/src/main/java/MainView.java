import android.content.Context;
import android.view.View;

import com.wesleyyue.battlehack2015.R;

/**
 * Created by Wesley on 15-07-19.
 */
public class MainView extends View {

    private View BottomSlideOut;

    public MainView(Context context) {
        super(context);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);


        BottomSlideOut = findViewById(R.id.BottomSlideOut);
        BottomSlideOut.offsetTopAndBottom(300);
    }
}
