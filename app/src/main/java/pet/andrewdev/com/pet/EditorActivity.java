package pet.andrewdev.com.pet;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import pet.andrewdev.com.pet.data.PetContract;
import pet.andrewdev.com.pet.data.PetContract.PetEntry;
import pet.andrewdev.com.pet.data.PetDbHelper;

public class EditorActivity extends AppCompatActivity {
    private EditText name, breed, weight;
    private Spinner gender;

    private int mSelectedGender = PetEntry.GENDER_UNKNOWN;
    private PetDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        mDbHelper = new PetDbHelper(this);
        name = (EditText) findViewById(R.id.edt_name);
        breed = (EditText) findViewById(R.id.edt_breed);
        weight = (EditText) findViewById(R.id.edt_weight);
        gender = (Spinner) findViewById(R.id.spn_gender);
        setUpSpinner();
    }

    private void setUpSpinner() {
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter
                .createFromResource(this, R.array.options_gender, android.R.layout.simple_dropdown_item_1line);
        gender.setAdapter(genderSpinnerAdapter);
        gender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)){
                    if (selection.equals(getString(R.string.male))){
                        mSelectedGender = PetEntry.GENDER_MALE;
                    }
                    else if (selection.equals(getString(R.string.female))){
                        mSelectedGender = PetEntry.GENDER_FEMALE;
                    }
                    else {
                        mSelectedGender = PetEntry.GENDER_UNKNOWN;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_insert_data:
                insertPet();
                finish();
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertPet() {
        String tampName = name.getText().toString().trim();
        String tampBreed = breed.getText().toString().trim();
        String tampWeight = weight.getText().toString().trim();
        int weight = 0;
        if (!tampWeight.isEmpty()){
            weight = Integer.parseInt(tampWeight);
        }

        //SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(PetEntry.COLUMN_PET_NAME, tampName);
        values.put(PetEntry.COLUMN_PET_BREED, tampBreed);
        values.put(PetEntry.COLUMN_PET_GENDER, mSelectedGender);
        values.put(PetEntry.COLUMN_PET_WEIGHT, weight);

        //long newRowId = db.insert(PetEntry.TABLE_NAME, null, values);
        Uri uri = getContentResolver().insert(PetContract.CONTENT_URI, values);
        if (uri == null){
            Toast.makeText(this, getString(R.string.error_saving), Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, getString(R.string.pet_saved), Toast.LENGTH_SHORT).show();
        }
    }
}
