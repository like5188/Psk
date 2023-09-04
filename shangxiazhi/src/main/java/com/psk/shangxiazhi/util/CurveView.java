package com.psk.shangxiazhi.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.psk.shangxiazhi.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Description:
 * Author:zhangmengmeng
 * Date:2018/2/1
 * Time:下午3:44
 */

public class CurveView extends RelativeLayout {


    private ArrayList<Integer> entryList = new ArrayList<>();

    private LineChart mChart;


    public CurveView(Context context) {
        this(context, null);
    }

    public CurveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CurveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();

    }

    private void initView() {

        View view = View.inflate(getContext(), R.layout.item_curve_view, this);
        mChart = view.findViewById(R.id.line_chart);
    }

    public void initChartData(List<Integer> list, int type) {
        if (list == null || list.isEmpty()) {
            return;
        }

        if (entryList.size() > 0) {
            entryList.clear();
        }

        int size = list.size();

        //初始化数据
        if (size > 200) {
            //取300个点的平均值
            int count = size / 200;
            System.out.println("count = " + count);
            int totalSpeed = 0;

            if (count == 1) {
                for (int i = 0; i < size; i++) {
                    entryList.add(list.get(i));
                }
            } else if (count >= 2) {
                for (int i = 0; i < size; i++) {
                    if (i % count == 0 && i != 0) {
                        entryList.add(totalSpeed / (count - 1));
                        totalSpeed = 0;
                    } else {
                        totalSpeed += list.get(i);
                    }
                }
            }

        } else {
            for (int i = 0; i < size; i++) {
                entryList.add(list.get(i));
            }
        }


        //初始化图表
        Description description = new Description();
        if (size > 200) {
            description.setText("时间/min");//设置描述文字内容
        } else {
            description.setText("时间/s");//设置描述文字内容
        }
        description.setTextColor(this.getResources().getColor(R.color.chart_text));//设置描述文字的颜色
        description.setTextSize(10f);
        description.setXOffset(-15f);
        description.setYOffset(3f);//描述文字的偏移

        mChart.setDescription(description);//设置图表的描述文字，会显示在图表的右下角
        mChart.setTouchEnabled(false);//设置是否可触摸
        mChart.setNoDataText("当前数据为空");//设置当 chart 为空时显示的描述文字

//        mChart.getDescription().setEnabled(false);//隐藏右下角描述


        //setChartData下面的"圈数" 的设置
        Legend legend = mChart.getLegend();
        legend.setEnabled(true);//设置Legend启用或禁用。 如果禁用， Legend 将不会被绘制。
        legend.setFormSize(18f);
        legend.setForm(Legend.LegendForm.CIRCLE);//样式，圆形
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setXOffset(3f);
        legend.setYOffset(3f);


        //X轴 建议隐藏，然后自定义一组展示出的数据,如：分钟0 5 10 15 20 30等
        XAxis xAxis = mChart.getXAxis();
        xAxis.setEnabled(true);//设置轴启用或禁用
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(8);
        xAxis.setTextColor(this.getResources().getColor(R.color.chart_text));
        xAxis.setAxisMinimum(0);
        if (size <= 200) {
            xAxis.setAxisMaximum(size);
            xAxis.setValueFormatter(null);

        } else {
            xAxis.setAxisMaximum(200);
            xAxis.setLabelCount(size / 60 + 1, true);
            xAxis.setValueFormatter(new MyXFormatter(size));
        }

//        xAxis.setAxisMaximum(200);

        xAxis.setAxisLineWidth(2f);
        xAxis.setDrawGridLines(false);//是否展示网格线

        //Y轴   getAxisLeft
        YAxis yAxis = mChart.getAxisLeft();
        yAxis.setEnabled(true);
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxis.setTextSize(8);
        yAxis.setTextColor(this.getResources().getColor(R.color.chart_text));

        int min = 0;
        int max = 0;
        for (Integer integer : list) {
            min = Math.min(min, integer);
            max = Math.max(max, integer);
        }
        yAxis.setAxisMinimum(min);
        yAxis.setAxisMaximum(max);
        yAxis.setLabelCount(6, false);
        yAxis.setAxisLineWidth(2f);
        yAxis.setDrawGridLines(true);//是否展示网格线


        //右侧，一般不用，设为false即可
        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < entryList.size(); i++) {
            entries.add(new Entry(i, entryList.get(i)));
        }

        if (type == 1) {
            setChartData(entries);
        } else {
            setHeartChartData(entries);
        }

        //去除折线图上的小圆圈
        List<ILineDataSet> sets = mChart.getData().getDataSets();

        for (ILineDataSet iSet : sets) {

            LineDataSet set = (LineDataSet) iSet;
            set.setDrawValues(false);
            set.setDrawCircles(false);
//            set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set.setMode(LineDataSet.Mode.LINEAR);
        }

    }

    private void setChartData(ArrayList<Entry> yVals1) {

        mChart.invalidate();
        mChart.notifyDataSetChanged();
        LineDataSet set1;

        if (mChart.getData() != null && mChart.getData().getDataSetCount() > 0) {

            set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);

            set1.setValues(yVals1);

            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            set1 = new LineDataSet(yVals1, "转速");
            set1.setColor(Color.parseColor("#FF4081"));
            set1.setLineWidth(1f);
            //是否绘制阴影
            set1.setDrawFilled(true);
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.fade_right);
            set1.setFillDrawable(drawable);

            LineData data = new LineData(set1);
            data.setValueTextColor(Color.WHITE);
            data.setValueTextSize(9f);

            mChart.setData(data);
        }
    }

    private void setHeartChartData(ArrayList<Entry> yVals1) {

        mChart.invalidate();
        mChart.notifyDataSetChanged();
        LineDataSet set1;

        if (mChart.getData() != null && mChart.getData().getDataSetCount() > 0) {

            set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);

            set1.setValues(yVals1);

            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            set1 = new LineDataSet(yVals1, "心率");
            set1.setColor(Color.parseColor("#FF4081"));
            set1.setLineWidth(1f);
            //是否绘制阴影
            set1.setDrawFilled(true);
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.fade_right);
            set1.setFillDrawable(drawable);

            LineData data = new LineData(set1);
            data.setValueTextColor(Color.WHITE);
            data.setValueTextSize(9f);

            mChart.setData(data);
        }
    }


}