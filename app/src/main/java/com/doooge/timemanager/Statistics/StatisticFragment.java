package com.doooge.timemanager.Statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.doooge.timemanager.LocalDatabaseHelper;
import com.doooge.timemanager.R;
import com.doooge.timemanager.SpecificTask;
import com.doooge.timemanager.Type;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by fredpan on 2018/1/31.
 */

public class StatisticFragment extends Fragment implements OnChartValueSelectedListener {

    private static PieChart pieChart;
    private static PieDataSet pieDataSet;
    private LinearLayout linearLayout;
    private LocalDatabaseHelper ldb;
    //Stores all the SpecificTasks with key by type ID.
    private ArrayList<SpecificTask> allSpecificTasks;
    private PieChartHelper pieChartHelper;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.statistic_page, container, false);

        ldb = LocalDatabaseHelper.getInstance(getActivity());
        linearLayout = rootView.findViewById(R.id.typeList);
        pieChartHelper = new PieChartHelper(getActivity());


        /*
        All the charts' data will be refresh only if user add/remove SpecificTask.
        Since add/remove requires users go to new activity, we do not use setUserVisibleHint here!
        All the data will only be refresh at the time when this fragment created.
        We also leave setOffscreenPageLimit() to 1 as default so that statisic page will be refreshed once user goes to the quickAccessTaskFragment.
         */

        //PIECHART
        pieChart = rootView.findViewById(R.id.pieChart);
        //Show all types' percentages
        allSpecificTasks = ldb.getAllSpecificTask();
        pieDataSet = pieChartHelper.calculatePieChart(allSpecificTasks);
        pieDataSet.setSliceSpace(3f);

        //Draw label outside of pieChart
        pieDataSet.setValueLinePart1OffsetPercentage(90.f);
        pieDataSet.setValueLinePart1Length(.5f);
        pieDataSet.setValueLinePart2Length(.2f);
        pieDataSet.setValueTextColor(Color.BLACK);
        pieDataSet.setValueLineColor(Color.BLACK);
        pieDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        pieDataSet.setValueFormatter(new PercentFormatter());
        pieDataSet.setValueTextSize(21f);
        pieDataSet.setColors(R.color.gray, R.color.yellow); //TODO SetColor

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueTextColor(Color.BLACK);
        pieData.setValueFormatter(new PercentFormatter());
        pieChart.setData(pieData);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelColor(Color.BLACK);//Set data label color

        pieChart.setCenterText("Your Time" + "\n" + "Distribution");
        pieChart.setCenterTextSize(27f);


        pieChart.invalidate();
        pieChart.setOnChartValueSelectedListener(this);
        pieChartCreation();
        return rootView;
    }

    //PIECHART
    private void pieChartCreation() {

        ArrayList<Type> types = ldb.getAllType();
        Iterator<Type> iterator = types.iterator();

        while (iterator.hasNext()) {
            final Type type = iterator.next();
            final CheckBox ch = new CheckBox(getActivity());
            ch.setChecked(true);

            ch.setText(type.getName());
            ch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {                                     //if users select a specific type
                        ArrayList<SpecificTask> specificTasks = ldb.findSpecificTasksByType(type);
                        allSpecificTasks.addAll(specificTasks);
                        PieDataSet newPieDataSet = pieChartHelper.calculatePieChart(allSpecificTasks);
                        newPieDataSet.setSliceSpace(3f);
                        PieData newPieData = new PieData(newPieDataSet);
                        pieChart.setData(newPieData);
                        pieChart.notifyDataSetChanged();
                        pieChart.invalidate();
                    }
                    if (!isChecked) {                                    //if users cancelled the selection of a specific type
                        ArrayList<SpecificTask> newSpecificTasksList = new ArrayList<>();
                        Iterator<SpecificTask> subIterator = allSpecificTasks.iterator();
                        while (subIterator.hasNext()) {
                            SpecificTask specificTaskToBeAdded = subIterator.next();
                            Type typeToBeRemoved = specificTaskToBeAdded.getType();
                            if (typeToBeRemoved.getId() != type.getId()) {// if is not the one to be removed
                                newSpecificTasksList.add(specificTaskToBeAdded);
                            }
                        }
                        allSpecificTasks = null;
                        allSpecificTasks = newSpecificTasksList;
                        pieDataSet = pieChartHelper.calculatePieChart(allSpecificTasks);
                        pieDataSet.setSliceSpace(3f);
                        PieData newPieData = new PieData(pieDataSet);
                        pieChart.setData(newPieData);
                        pieChart.notifyDataSetChanged();
                        pieChart.invalidate();
                    }
                }
            });

            linearLayout.addView(ch);
        }
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Type type = (Type) e.getData();
        System.out.println(type == null);
        //TODO Linechart

    }

    @Override
    public void onNothingSelected() {

    }
}
