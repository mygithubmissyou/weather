package layout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.example.weather.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jack on 2017/9/29.
 */

public class LineChartView extends View {
    public LineChartView(Context context) {
        this(context, null);

    }

    public LineChartView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    public static void setData(List<Integer> degree, List<String> date) {
        degress_list = degree;
        date_list = date;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (degress_list != null && date_list != null) {
            mcanvas = canvas;
//            mcanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            vwidth = getWidth();
            vheight = getHeight();
            y_bottom = 100;
            margin = 30;
            padding = 20;
            maxVal = 26;
            minVal = 14;
            drawBaseLine();
            dotPaint = new Paint();
            dotPaint.setColor(color);
            dotPaint.setStrokeWidth(2);
            dotPaint.setStyle(Paint.Style.STROKE);
            dot_radius = 4;
            solidPaint = new Paint();
            solidPaint.setColor(color);
            solidPaint.setStrokeWidth(5);
            solidPaint.setAntiAlias(true);
            solidPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
            solidPaint.setStyle(Paint.Style.STROKE);
//        solidPaint.setStrokeJoin(Paint.Join.ROUND);
//        CornerPathEffect cornerPathEffect = new CornerPathEffect(300);
//        solidPaint.setPathEffect(cornerPathEffect);
            Path path = new Path();
            textPaint = new TextPaint();
            textPaint.setColor(color);
            textPaint.setStrokeWidth(4);
            textPaint.setTextSize(25);
            if (degress_list.size() > 1 && date_list.size() > 1) {//数据大于2才绘制曲线
                this.setVisibility(VISIBLE);
                subVal = (maxVal - minVal) / (degress_list.size() - 1) * 2;
                use_width = vwidth - margin * 2 - padding * 2;
                x_offset = use_width / (degress_list.size() - 1);
                y_offsset = 40;
                for (int i = 0; i < degress_list.size(); i++) {
                    int startX = margin + padding + i * x_offset;
                    int startY = baseLineHeight - y_offsset - degress_list.get(i) * 2;
                    mcanvas.drawCircle(startX, startY, dot_radius, dotPaint);//画园点
                    mcanvas.drawText(degress_list.get(i) + "°", startX - 15, startY - 15, textPaint);//写字

                    mcanvas.drawText(date_list.get(i).split("\\|")[0], startX - 25, baseLineHeight + 30, textPaint);
                    int font_len = date_list.get(i).split("\\|")[1].split("").length;
                    int font_offset = 0;
                    if (font_len > 4) {
                        font_offset = 20;
                        String substr=date_list.get(i).split("\\|")[1];
                        mcanvas.drawText(substr.substring(0,2), startX - font_offset, baseLineHeight + 60, textPaint);
                        mcanvas.drawText(substr.substring(2,substr.length()), startX - font_offset, baseLineHeight + 90, textPaint);
                    } else if(font_len>3) {
                        font_offset = 30;
                        mcanvas.drawText(date_list.get(i).split("\\|")[1], startX - font_offset, baseLineHeight + 60, textPaint);
                    }else {
                        font_offset = 15;
                        mcanvas.drawText(date_list.get(i).split("\\|")[1], startX - font_offset, baseLineHeight + 60, textPaint);
                    }
                    path.moveTo(startX + dot_radius, startY);
                    if (i < (degress_list.size() - 1)) {
                        int endX = startX + x_offset;
                        int subY = degress_list.get(i) - degress_list.get(i + 1);
                        int endY = startY + subY * 2;
                        int mX = (endX - startX) / 2 + startX;
                        int mY = startY + (subY > 0 ? -subY : subY) * 2;

                        path.quadTo(mX, mY, endX - dot_radius, endY);
                        mcanvas.drawPath(path, solidPaint);
//                mcanvas.drawLine(startX+dot_radius,startY,endX-dot_radius,endY,solidPaint);
                    }
                    int date_x = startX;
                    int date_y = baseLineHeight;
                    mcanvas.drawLine(startX, startY + dot_radius, date_x, date_y, linePaint);
                }
            }
            else {
                this.setVisibility(GONE);
            }
        }
    }
private Drawable drawable;
    private Canvas mcanvas;
    private int vheight;
    private int vwidth;
    private Paint linePaint;
    private Paint dotPaint;
    private Paint solidPaint;
    private TextPaint textPaint;
    private int color = Color.WHITE;
    private int x_offset;
    private int y_offsset;
    private int dot_radius;
    private static List<Integer> degress_list;
    private static List<String> date_list;
    private int y_bottom;
    private int use_width;
    private int margin;
    private int padding;
    private int baseLineHeight;
    private int maxVal;
    private int minVal;
    private int subVal;

    private void drawBaseLine() {
        linePaint = new Paint();
        linePaint.setColor(color);
        linePaint.setStrokeWidth(1);

        baseLineHeight = vheight - y_bottom;
        mcanvas.drawLine(margin, baseLineHeight, vwidth - margin, baseLineHeight, linePaint);
    }
}
