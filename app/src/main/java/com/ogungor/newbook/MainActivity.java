package com.ogungor.newbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    ArrayList<String> locationName;
    ArrayList<Integer> locationId;
    ArrayAdapter arrayAdapter;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        listView = findViewById(R.id.listView);
        locationId = new ArrayList<>();
        locationName = new ArrayList<>();

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, locationName);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("locationId", locationId.get(i));
                intent.putExtra("info", "old");
                startActivity(intent);
            }
        });


        getLocation();

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_location, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        intent = new Intent(MainActivity.this, MapsActivity.class);
        intent.putExtra("info", "new");
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }


    public void getLocation() {
        try {
            SQLiteDatabase database = this.openOrCreateDatabase("Location", MODE_PRIVATE, null);
            Cursor cursor = database.rawQuery("select * from location", null);
            int locationIx = cursor.getColumnIndex("locationname");
            int idIx = cursor.getColumnIndex("id");

            while (cursor.moveToNext()) {
                locationName.add(cursor.getString(locationIx));
                locationId.add(cursor.getInt(idIx));
            }

            arrayAdapter.notifyDataSetChanged();
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}