package com.finalproject.mosapp;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.OvershootInterpolator;

import java.util.ArrayList;


public class AnimatorSampleActivity extends AppCompatActivity {

    MainAdapter adapter;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.add) {
            adapter.add(BitmapFactory.decodeResource(getResources(), R.drawable.chip), adapter
                    .getItemCount());
        } else if (id == R.id.del) {
            adapter.remove(adapter.getItemCount() - 1);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_ani, menu);
        return true;
    }

    private static String[] data = new String[] {
            "Apple", "Ball", "Camera", "Day", "Egg", "Foo", "Google", "Hello", "Iron", "Japan", "Coke",
            "Dog", "Cat", "Yahoo", "Sony", "Canon", "Fujitsu", "USA", "Nexus", "LINE", "Haskell", "C++",
            "Java", "Go", "Swift", "Objective-c", "Ruby", "PHP", "Bash", "ksh", "C", "Groovy", "Kotlin"
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing_recycler);

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list);

        if (getIntent().getBooleanExtra("GRID", true)) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        recyclerView.setItemAnimator(new FadeInAnimator());

        adapter = new MainAdapter(this, new ArrayList());
        recyclerView.setAdapter(adapter);

        recyclerView.setItemAnimator(new FadeInAnimator(new OvershootInterpolator(1f)));
        recyclerView.getItemAnimator().setAddDuration(500);
        recyclerView.getItemAnimator().setRemoveDuration(500);

    }
}
