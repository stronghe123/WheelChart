package com.sign.signchart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Build;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CaoYongSheng
 * on 2019-04-22
 *
 * @author admin
 */
public class ChartBillView extends View {
    private ChartBillLayout mParent;
    private Context mContext;

    //提前刻画量
    private float mDrawOffset;

    private Paint mXLabelLinePaint, mXLabelTextNormalPaint, mXLabelTextSelectPaint, mXValuePointColorPaint, mXValuePointWhitePaint, mNormalValueTextPaint, mSelectValueTextPaint, mLinkLinePaint;
    //x轴轴线的路径
    private Path mXLabelLinePath;
    //x轴label未选中文字属性
    private Paint.FontMetrics mXLabelTextNormalMetrics;
    //x轴label选中文字属性
    private Paint.FontMetrics mXLabelTextSelectMetrics;
    //x轴label正常文字的高度
    private float mXLabelTextNormalHeight;
    //value选中文字的高度
    private float mValueNormalHeight;
    //value选中文字的高度
    private float mValueSelectHeight;
    //选中坐标文字属性
    private Paint.FontMetrics mSelectValueMetrics;
    //未选中坐标文字属性
    private Paint.FontMetrics mNormalValueMetrics;
    //值的坐标
    private List<PointF> mPointList;
    //选中值的路径
    private Path mPointPath;


    public ChartBillView(Context context, ChartBillLayout chartBillLayout) {
        super(context);
        mParent = chartBillLayout;
        init(context);
    }

    public void init(Context context) {
        mContext = context;
        mXLabelLinePath = new Path();
        mPointPath = new Path();
        mPointList = new ArrayList<>();
        mDrawOffset = Utils.dp2px(context, mParent.getXLabelInterval());
        initPaint();
        checkAPILevel();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //起始、终止绘制值的下标
        int startIndex = (int) ((getScrollX() - mDrawOffset) / mParent.getXLabelInterval());
        int endIndex = (int) ((getScrollX() + getWidth() + mDrawOffset) / mParent.getXLabelInterval());
        int height = getHeight();
        //先画path路径
        mPointList.clear();
        for (int i = startIndex; i <= endIndex; i++) {
            if (i >= 0 && i < mParent.getData().size()) {
                float locationX = i * mParent.getXLabelInterval();
                if (mParent.getXLabelGravity() == ChartBillLayout.X_TOP) {
                    //30px的上方及下方空白间距+label文字高度+label文字和轴线的间距+选中值文字高度+12px的坐标点和文字间距
                    float distance = (float) ((height - 30 - mXLabelTextNormalHeight - mParent.getXLabelTextLineInterval() - mValueSelectHeight - 12) /
                            (mParent.getYMaxValue() - mParent.getYMinValue()) * (mParent.getData().get(i).getMoney() - mParent.getYMinValue()));
                    mPointList.add(new PointF(locationX, (height - distance - 15)));
                } else {
                    //20px的上方及下方空白间距+label文字高度+label文字和轴线的间距+选中值文字高度+12px的坐标点和文字间距
                    float distance = (float) ((height - 30 - mXLabelTextNormalHeight - mParent.getXLabelTextLineInterval() - mValueSelectHeight - 12) /
                            (mParent.getYMaxValue() - mParent.getYMinValue()) * (mParent.getData().get(i).getMoney() - mParent.getYMinValue()));
                    mPointList.add(new PointF(locationX, (height - distance - 15 - mXLabelTextNormalHeight - mParent.getXLabelTextLineInterval())));
                }
            }
        }
        drawValuePath(canvas);
        for (int i = startIndex; i <= endIndex; i++) {
            if (i >= 0 && i < mParent.getData().size()) {
                float locationX = i * mParent.getXLabelInterval();
                if (mParent.getXLabelGravity() == ChartBillLayout.X_TOP) {
                    int centerIndex = (int) (getScrollX() / mParent.getXLabelInterval() + Utils.getScreenWidth(mContext) / mParent.getXLabelInterval() / 2);
                    //x轴label文字在上面
                    if (mParent.getData().get(i).getMonth().length() > 0) {
                        //是否是中心线
                        if (centerIndex == i) {
                            canvas.drawText(mParent.getData().get(i).getMonth(), locationX, -mXLabelTextSelectMetrics.top, mXLabelTextSelectPaint);
                        } else {
                            canvas.drawText(mParent.getData().get(i).getMonth(), locationX, -mXLabelTextNormalMetrics.top, mXLabelTextNormalPaint);
                        }
                    }
                    //x轴轴线
                    mXLabelLinePath.reset();
                    mXLabelLinePath.moveTo(locationX, mXLabelTextNormalHeight + mParent.getXLabelTextLineInterval());
                    mXLabelLinePath.lineTo(locationX, height);
                    canvas.drawPath(mXLabelLinePath, mXLabelLinePaint);
                    //30px的上方及下方空白间距+label文字高度+label文字和轴线的间距+选中值文字高度+12px的坐标点和文字间距
                    float distance = (float) ((height - 30 - mXLabelTextNormalHeight - mParent.getXLabelTextLineInterval() - mValueSelectHeight - 12) /
                            (mParent.getYMaxValue() - mParent.getYMinValue()) * (mParent.getData().get(i).getMoney() - mParent.getYMinValue()));
                    //是否是中心值
                    if (centerIndex == i) {
                        mXValuePointColorPaint.setStrokeWidth(8);
                        canvas.drawCircle(locationX, (height - distance - 15), 4, mXValuePointColorPaint);
                        mXValuePointWhitePaint.setStrokeWidth(4);
                        canvas.drawCircle(locationX, (height - distance - 15), 8, mXValuePointWhitePaint);
                        mXValuePointColorPaint.setStrokeWidth(3);
                        canvas.drawCircle(locationX, (height - distance - 15), 12, mXValuePointColorPaint);
                        canvas.drawText("R$" + mParent.getData().get(i).getMoney(), locationX, (height - distance - 15 - mSelectValueMetrics.bottom - 12), mSelectValueTextPaint);
                    } else {
                        mXValuePointWhitePaint.setStrokeWidth(8);
                        canvas.drawCircle(locationX, (height - distance - 15), 4, mXValuePointWhitePaint);
                        mXValuePointColorPaint.setStrokeWidth(3);
                        canvas.drawCircle(locationX, (height - distance - 15), 8, mXValuePointColorPaint);
                        canvas.drawText("R$" + mParent.getData().get(i).getMoney(), locationX, (height - distance - 15 - mNormalValueMetrics.bottom - 12), mNormalValueTextPaint);
                    }
                } else {
                    int centerIndex = (int) (getScrollX() / mParent.getXLabelInterval() + Utils.getScreenWidth(mContext) / mParent.getXLabelInterval() / 2);
                    //x轴label文字在下面
                    if (mParent.getData().get(i).getMonth().length() > 0) {
                        //是否是中心线
                        if (centerIndex == i) {
                            canvas.drawText(mParent.getData().get(i).getMonth(), locationX, getHeight() - mXLabelTextSelectMetrics.bottom, mXLabelTextSelectPaint);
                        } else {
                            canvas.drawText(mParent.getData().get(i).getMonth(), locationX, getHeight() - mXLabelTextNormalMetrics.bottom, mXLabelTextNormalPaint);
                        }
                    }
                    //x轴轴线
                    mXLabelLinePath.reset();
                    mXLabelLinePath.moveTo(locationX, 0);
                    mXLabelLinePath.lineTo(locationX, getHeight() - mXLabelTextNormalHeight - mParent.getXLabelTextLineInterval());
                    canvas.drawPath(mXLabelLinePath, mXLabelLinePaint);
                    //30px的上方及下方空白间距+label文字高度+label文字和轴线的间距+选中值文字高度+12px的坐标点和文字间距
                    float distance = (float) ((height - 30 - mXLabelTextNormalHeight - mParent.getXLabelTextLineInterval() - mValueSelectHeight - 12) /
                            (mParent.getYMaxValue() - mParent.getYMinValue()) * (mParent.getData().get(i).getMoney() - mParent.getYMinValue()));
                    //是否是中心值
                    if (centerIndex == i) {
                        mXValuePointColorPaint.setStrokeWidth(8);
                        canvas.drawCircle(locationX, (height - distance - 15 - mXLabelTextNormalHeight - mParent.getXLabelTextLineInterval()), 4, mXValuePointColorPaint);
                        mXValuePointWhitePaint.setStrokeWidth(4);
                        canvas.drawCircle(locationX, (height - distance - 15 - mXLabelTextNormalHeight - mParent.getXLabelTextLineInterval()), 8, mXValuePointWhitePaint);
                        mXValuePointColorPaint.setStrokeWidth(3);
                        canvas.drawCircle(locationX, (height - distance - 15 - mXLabelTextNormalHeight - mParent.getXLabelTextLineInterval()), 12, mXValuePointColorPaint);
                        canvas.drawText("R$" + mParent.getData().get(i).getMoney(), locationX, (height - distance - 15 - mXLabelTextNormalHeight - mParent.getXLabelTextLineInterval() - mSelectValueMetrics.bottom - 12), mSelectValueTextPaint);
                    } else {
                        mXValuePointWhitePaint.setStrokeWidth(8);
                        canvas.drawCircle(locationX, (height - distance - 15 - mXLabelTextNormalHeight - mParent.getXLabelTextLineInterval()), 4, mXValuePointWhitePaint);
                        mXValuePointColorPaint.setStrokeWidth(3);
                        canvas.drawCircle(locationX, (height - distance - 15 - mXLabelTextNormalHeight - mParent.getXLabelTextLineInterval()), 8, mXValuePointColorPaint);
                        canvas.drawText("R$" + mParent.getData().get(i).getMoney(), locationX, (height - distance - 15 - mXLabelTextNormalHeight - mParent.getXLabelTextLineInterval() - mNormalValueMetrics.bottom - 12), mNormalValueTextPaint);
                    }
                }
            }
        }
    }

    private void drawValuePath(Canvas canvas) {
        if (mPointList.size() <= 0) {
            return;
        }
        //保存曲线路径
        mPointPath.reset();
        float prePreviousPointX = Float.NaN;
        float prePreviousPointY = Float.NaN;
        float previousPointX = Float.NaN;
        float previousPointY = Float.NaN;
        float currentPointX = Float.NaN;
        float currentPointY = Float.NaN;
        float nextPointX;
        float nextPointY;
        int lineSize = mPointList.size();
        for (int valueIndex = 0; valueIndex < lineSize; ++valueIndex) {
            if (Float.isNaN(currentPointX)) {
                PointF point = mPointList.get(valueIndex);
                currentPointX = point.x;
                currentPointY = point.y;
            }
            if (Float.isNaN(previousPointX)) {
                //是否是第一个点
                if (valueIndex > 0) {
                    PointF point = mPointList.get(valueIndex - 1);
                    previousPointX = point.x;
                    previousPointY = point.y;
                } else {
                    //是的话就用当前点表示上一个点
                    previousPointX = currentPointX;
                    previousPointY = currentPointY;
                }
            }

            if (Float.isNaN(prePreviousPointX)) {
                //是否是前两个点
                if (valueIndex > 1) {
                    PointF point = mPointList.get(valueIndex - 2);
                    prePreviousPointX = point.x;
                    prePreviousPointY = point.y;
                } else {
                    //是的话就用当前点表示上上个点
                    prePreviousPointX = previousPointX;
                    prePreviousPointY = previousPointY;
                }
            }

            // 判断是不是最后一个点了
            if (valueIndex < lineSize - 1) {
                PointF point = mPointList.get(valueIndex + 1);
                nextPointX = point.x;
                nextPointY = point.y;
            } else {
                //是的话就用当前点表示下一个点
                nextPointX = currentPointX;
                nextPointY = currentPointY;
            }

            if (valueIndex == 0) {
                // 将Path移动到开始点
                mPointPath.moveTo(currentPointX, currentPointY);
            } else {
                // 求出控制点坐标
                float firstDiffX = (currentPointX - prePreviousPointX);
                float firstDiffY = (currentPointY - prePreviousPointY);
                float secondDiffX = (nextPointX - previousPointX);
                float secondDiffY = (nextPointY - previousPointY);
                float firstControlPointX = previousPointX + (0.16f * firstDiffX);
                float firstControlPointY = previousPointY + (0.16f * firstDiffY);
                float secondControlPointX = currentPointX - (0.16f * secondDiffX);
                float secondControlPointY = currentPointY - (0.16f * secondDiffY);
                //画出曲线
                mPointPath.cubicTo(firstControlPointX, firstControlPointY, secondControlPointX, secondControlPointY,
                        currentPointX, currentPointY);
            }

            // 更新值,
            prePreviousPointX = previousPointX;
            prePreviousPointY = previousPointY;
            previousPointX = currentPointX;
            previousPointY = currentPointY;
            currentPointX = nextPointX;
            currentPointY = nextPointY;
        }
        canvas.drawPath(mPointPath, mLinkLinePaint);
    }

    //初始化画笔
    private void initPaint() {
        //x轴轴线
        mXLabelLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mXLabelLinePaint.setStrokeWidth(mParent.getXLabelLineWidth());
        mXLabelLinePaint.setColor(mParent.getXLabelLineColor());
        mXLabelLinePaint.setStyle(Paint.Style.STROKE);
        //是否是虚线
        if (mParent.getXLabelLineLength() > 0 && mParent.getXLabelLineDashLength() > 0) {
            mXLabelLinePaint.setPathEffect(new DashPathEffect(new float[]{mParent.getXLabelLineLength(), mParent.getXLabelLineDashLength()}, 0));
        }
        //x轴label文字
        mXLabelTextNormalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mXLabelTextNormalPaint.setTextSize(mParent.getXLabelTextNormalSize());
        mXLabelTextNormalPaint.setColor(mParent.getXLabelTextNormalColor());
        mXLabelTextNormalPaint.setTextAlign(Paint.Align.CENTER);
        mXLabelTextNormalMetrics = mXLabelTextNormalPaint.getFontMetrics();
        mXLabelTextNormalHeight = mXLabelTextNormalMetrics.bottom - mXLabelTextNormalMetrics.top;
        //x轴label文字
        mXLabelTextSelectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mXLabelTextSelectPaint.setTextSize(mParent.getXLabelTextSelectSize());
        mXLabelTextSelectPaint.setColor(mParent.getXLabelTextSelectColor());
        mXLabelTextSelectPaint.setTextAlign(Paint.Align.CENTER);
        mXLabelTextSelectMetrics = mXLabelTextNormalPaint.getFontMetrics();
        //坐标点的绘制
        mXValuePointColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mXValuePointColorPaint.setStrokeWidth(3);
        mXValuePointColorPaint.setColor(getResources().getColor(R.color.blue));
        mXValuePointColorPaint.setStyle(Paint.Style.STROKE);
        mXValuePointColorPaint.setStrokeCap(Paint.Cap.ROUND);
        //坐标点白色填充的绘制
        mXValuePointWhitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mXValuePointWhitePaint.setStrokeWidth(5);
        mXValuePointWhitePaint.setColor(Color.WHITE);
        mXValuePointWhitePaint.setStyle(Paint.Style.STROKE);
        mXValuePointWhitePaint.setStrokeCap(Paint.Cap.ROUND);
        //未选中坐标值
        mNormalValueTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mNormalValueTextPaint.setTextSize(mParent.getNormalValueTextSize());
        mNormalValueTextPaint.setColor(mParent.getNormalValueTextColor());
        mNormalValueTextPaint.setTextAlign(Paint.Align.CENTER);
        mNormalValueMetrics = mNormalValueTextPaint.getFontMetrics();
        mValueNormalHeight = mNormalValueMetrics.bottom - mNormalValueMetrics.top;
        //选中坐标值
        mSelectValueTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSelectValueTextPaint.setTextSize(mParent.getSelectValueTextSize());
        mSelectValueTextPaint.setColor(mParent.getSelectValueTextColor());
        mSelectValueTextPaint.setTextAlign(Paint.Align.CENTER);
        mSelectValueMetrics = mSelectValueTextPaint.getFontMetrics();
        mValueSelectHeight = mSelectValueMetrics.bottom - mSelectValueMetrics.top;
        //连接线
        mLinkLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinkLinePaint.setColor(mParent.getLinkLineColor());
        mLinkLinePaint.setStrokeWidth(mParent.getLinkLineWidth());
        mLinkLinePaint.setStyle(Paint.Style.STROKE);
    }

    //API小于18则关闭硬件加速，否则setAntiAlias()方法不生效
    private void checkAPILevel() {
        if (Build.VERSION.SDK_INT < 18) {
            setLayerType(LAYER_TYPE_NONE, null);
        }
    }
}
