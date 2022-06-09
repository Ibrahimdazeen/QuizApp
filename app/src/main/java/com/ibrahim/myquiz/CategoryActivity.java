package com.ibrahim.myquiz;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static com.ibrahim.myquiz.SplashActivity.catList;

public class CategoryActivity extends AppCompatActivity {
    private GridView cartGrid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Categories");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        cartGrid = findViewById(R.id.catGridview);

        /* LOCAL LIST OF CATEGORIES
        List<String> cartList = new ArrayList<>();

        cartList.add("Cart 1");
        cartList.add("Cart 2");
        cartList.add("Cart 3");
        cartList.add("Cart 4");
        cartList.add("Cart 5");
        cartList.add("Cart 6");*/

        //FETCHING CATEGORIES FROM SPLASH ACTIVITY
        CatGridAdapter adapter = new CatGridAdapter(catList );
        cartGrid.setAdapter(adapter);



    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
      if (item.getItemId()==android.R.id.home)
      {
          CategoryActivity.this.finish();
      }

        return super.onOptionsItemSelected(item);
    }
}
