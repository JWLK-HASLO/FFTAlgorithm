package co.haslo.fftalgorithm;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    int counter = 0;
    int N = 1024;
    double[] mag;

    LineChart lineChart;
    int DATA_RANGE = 100;
    LineData lineData;
    LineDataSet setValueTransfer;
    ArrayList<Entry> entryData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lineChart = (LineChart) findViewById(R.id.mp_chart);
        chartInit();
//        threadStart();

        double y[] = new double[N]; //Imaginary Part
        for(int i=0;i<N;i++){
            y[i] = 0;
        } //Imaginary Part는 0으로 채운다.

        double x[] = new double[N]; //Real Part
        for(int i=0;i<N;i++){
            x[i] = Math.sin(2*Math.PI*24*0.004*i) + Math.sin(2*Math.PI*48*0.004*i) + Math.sin(2*Math.PI*97*0.004*i);
        }

        FFT doFFT = new FFT(N);
        doFFT.fft(x,y); //FFT

        mag = new double[N/2];  //magnitude

        for(int k=0;k<N/2;k++) {
            mag[k] = Math.sqrt(Math.pow(x[k], 2) + Math.pow(y[k], 2));

            entryData.add(new Entry(k/4, (float)mag[k]));
        }

        setValueTransfer.notifyDataSetChanged();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();

    }

    private void chartInit() {
        lineChart.setAutoScaleMinMaxEnabled(true);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getXAxis().setAxisMaximum(130f);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        entryData = new ArrayList<Entry>();
        setValueTransfer = new LineDataSet(entryData, "Hz");
        setValueTransfer.setColor(Color.RED);
        setValueTransfer.setDrawValues(false);
        setValueTransfer.setDrawCircles(false);
        setValueTransfer.setAxisDependency(YAxis.AxisDependency.LEFT);

        lineData = new LineData();
        lineData.addDataSet(setValueTransfer);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    public void chartUpdate(int data) {
        if(entryData.size() > DATA_RANGE){
            entryData.remove(0);
            for(int i = 0; i < DATA_RANGE; i++){
                entryData.get(i).setX(i);
            }
        }
        entryData.add(new Entry(entryData.size(), data));
        setValueTransfer.notifyDataSetChanged();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    public void chartUpdateFFT(float data) {
        if(entryData.size() > DATA_RANGE){
            entryData.remove(0);
            for(int i = 0; i < DATA_RANGE; i++){
                entryData.get(i).setX(i);
            }
        }
        entryData.add(new Entry(entryData.size(), data));
        setValueTransfer.notifyDataSetChanged();
        lineChart.notifyDataSetChanged();
        lineChart.invalidate();
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0) {
                int data = 0;
                data = (int)(Math.random()*1024);
//                chartUpdate(data);
                chartUpdateFFT((float) mag[counter%mag.length] );
                counter++;
               Log.d("Handler", counter%mag.length+"");
            }
        }
    };

    class GraphThread extends Thread {
        @Override
        public void run() {
            int i = 0;
            while(true){
                handler.sendEmptyMessage(i);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private void threadStart() {
        GraphThread thread = new GraphThread();
        thread.setDaemon(true);
        thread.start();
    }


}