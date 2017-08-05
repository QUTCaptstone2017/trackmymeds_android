package com.app.trackmymeds;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;

public class ScheduleActivity extends AppCompatActivity {
    private ExpandListAdapter ExpAdapter;
    private ArrayList<ExpandListGroup> ExpListItems;
    private ExpandableListView ExpandList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        ExpandList = (ExpandableListView) findViewById(R.id.list_view_medication_schedule);
        ExpListItems = SetStandardGroups();
        ExpAdapter = new ExpandListAdapter(ScheduleActivity.this, ExpListItems);
        ExpandList.setAdapter(ExpAdapter);


        ImageButton registerButton = (ImageButton)findViewById(R.id.button_add_medication);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goAddMedicine();
            }
        });

        Button deleteAccountButton = (Button)findViewById(R.id.button_delete_account);
        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goDeleteAccount();
            }
        });

        Button historyButton = (Button)findViewById(R.id.button_medication_history);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goMedicationHistory();
            }
        });
    }

    public void goMedicationHistory() {
        Intent intent = new Intent(this, MedicationHistoryActivity.class);
        startActivity(intent);
    }

    public void goDeleteAccount() {
        Intent intent = new Intent(this, DeleteAccountActivity.class);
        startActivity(intent);
    }

    public void goAddMedicine() {
        Intent intent = new Intent(this, AddMedicineActivity.class);
        startActivity(intent);
    }

    public ArrayList<ExpandListGroup> SetStandardGroups() {
        //TODO: Replace this dummy data...
        ArrayList<ExpandListGroup> resultList = new ArrayList<ExpandListGroup>();
        ArrayList<ExpandListChild> groupList = new ArrayList<ExpandListChild>();

        ExpandListGroup group1 = new ExpandListGroup();
        group1.setName("Morning");

        ExpandListChild ch1_1 = new ExpandListChild();
        ch1_1.setName("Pill 1");
        ch1_1.setTag(null);
        groupList.add(ch1_1);

        ExpandListChild ch1_2 = new ExpandListChild();
        ch1_2.setName("Pill 2");
        ch1_2.setTag(null);
        groupList.add(ch1_2);

        ExpandListChild ch1_3 = new ExpandListChild();
        ch1_3.setName("Pill 3");
        ch1_3.setTag(null);
        groupList.add(ch1_3);

        group1.setItems(groupList);
        groupList = new ArrayList<ExpandListChild>();

        ExpandListGroup group2 = new ExpandListGroup();
        group2.setName("Afternoon");

        ExpandListChild ch2_1 = new ExpandListChild();
        ch2_1.setName("Pill 4");
        ch2_1.setTag(null);
        groupList.add(ch2_1);

        ExpandListChild ch2_2 = new ExpandListChild();
        ch2_2.setName("Pill 5");
        ch2_2.setTag(null);
        groupList.add(ch2_2);

        ExpandListChild ch2_3 = new ExpandListChild();
        ch2_3.setName("Pill 6");
        ch2_3.setTag(null);
        groupList.add(ch2_3);

        group2.setItems(groupList);
        groupList = new ArrayList<ExpandListChild>();

        ExpandListGroup group3 = new ExpandListGroup();
        group3.setName("Evening");

        ExpandListChild ch3_1 = new ExpandListChild();
        ch3_1.setName("Pill 7");
        ch3_1.setTag(null);
        groupList.add(ch3_1);

        ExpandListChild ch3_2 = new ExpandListChild();
        ch3_2.setName("Pill 8");
        ch3_2.setTag(null);
        groupList.add(ch3_2);

        ExpandListChild ch3_3 = new ExpandListChild();
        ch3_3.setName("Pill 9");
        ch3_3.setTag(null);
        groupList.add(ch3_3);

        ExpandListChild ch3_4 = new ExpandListChild();
        ch3_4.setName("Pill 10");
        ch3_4.setTag(null);
        groupList.add(ch3_4);

        ExpandListChild ch3_5 = new ExpandListChild();
        ch3_5.setName("Pill 11");
        ch3_5.setTag(null);
        groupList.add(ch3_5);

        group3.setItems(groupList);
        groupList = new ArrayList<ExpandListChild>();

        ExpandListGroup group4 = new ExpandListGroup();
        group4.setName("Night");

        ExpandListChild ch4_1 = new ExpandListChild();
        ch4_1.setName("Pill 12");
        ch4_1.setTag(null);
        groupList.add(ch4_1);

        ExpandListChild ch4_2 = new ExpandListChild();
        ch4_2.setName("Pill 13");
        ch4_2.setTag(null);
        groupList.add(ch4_2);

        group4.setItems(groupList);

        resultList.add(group1);
        resultList.add(group2);
        resultList.add(group3);
        resultList.add(group4);

        return resultList;
    }
}
