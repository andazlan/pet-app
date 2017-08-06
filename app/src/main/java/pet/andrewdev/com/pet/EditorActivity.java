package pet.andrewdev.com.pet;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import pet.andrewdev.com.pet.data.PetContract;
import pet.andrewdev.com.pet.data.PetContract.PetEntry;
import pet.andrewdev.com.pet.data.PetDbHelper;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_PET_LOADER = 0;
    private EditText name, breed, weight;
    private Spinner gender;

    private int mSelectedGender = PetEntry.GENDER_UNKNOWN;
    private boolean mPetHasChanged = false;
    private PetDbHelper mDbHelper;
    private Uri mCurrentUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentUri = intent.getData();



        mDbHelper = new PetDbHelper(this);
        name = (EditText) findViewById(R.id.edt_name);
        breed = (EditText) findViewById(R.id.edt_breed);
        weight = (EditText) findViewById(R.id.edt_weight);
        gender = (Spinner) findViewById(R.id.spn_gender);

        name.setOnTouchListener(mTouchListener);
        breed.setOnTouchListener(mTouchListener);
        weight.setOnTouchListener(mTouchListener);
        gender.setOnTouchListener(mTouchListener);

        setUpSpinner();

        if (mCurrentUri == null){
            setTitle(getString(R.string.add_pet));
        }
        else {
            setTitle(getString(R.string.edit_pet));
            getSupportLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
            invalidateOptionsMenu();
        }

    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mPetHasChanged = true;
            return false;
        }
    };

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
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentUri == null){
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
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
                savePet();
                finish();
                return true;
            case android.R.id.home:
                if (!mPetHasChanged){
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };

                showUnsavedChangeDialog(discardButtonListener);
                return true;
            case R.id.action_delete:
                showDeletionDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void savePet() {
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

        if (mCurrentUri != null){
            getContentResolver().update(mCurrentUri, values, null, null);
        }
        else {
            Uri uri = getContentResolver().insert(PetContract.CONTENT_URI, values);
            if (uri == null){
                Toast.makeText(this, getString(R.string.error_saving), Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, getString(R.string.pet_saved), Toast.LENGTH_SHORT).show();
            }
        }


        //long newRowId = db.insert(PetEntry.TABLE_NAME, null, values);

    }

    @Override
    public void onBackPressed() {
        if (!mPetHasChanged){
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };

        showUnsavedChangeDialog(discardButtonListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED,
                PetEntry.COLUMN_PET_GENDER,
                PetEntry.COLUMN_PET_WEIGHT };

        return new CursorLoader(this, mCurrentUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1){
            return;
        }

        if (data.moveToFirst()){
            showData(data);
        }
    }

    private void showData(Cursor data) {
        //int idColumnIndex = data.getColumnIndex(PetEntry._ID);
        int nameColumnIndex = data.getColumnIndex(PetEntry.COLUMN_PET_NAME);
        int breedColumnIndex = data.getColumnIndex(PetEntry.COLUMN_PET_BREED);
        int genderColumnIndex = data.getColumnIndex(PetEntry.COLUMN_PET_GENDER);
        int weightColumnIndex = data.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT);


        name.setText(data.getString(nameColumnIndex));
        breed.setText(data.getString(breedColumnIndex));
        weight.setText(String.valueOf(data.getInt(weightColumnIndex)));

        int tampGender = data.getInt(genderColumnIndex);

        switch (tampGender){
            case PetEntry.GENDER_UNKNOWN:
                gender.setSelection(0);
                break;
            case PetEntry.GENDER_MALE:
                gender.setSelection(1);
                break;
            case PetEntry.GENDER_FEMALE:
                gender.setSelection(2);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        name.setText("");
        breed.setText("");
        weight.setText("");
        gender.setSelection(0);
    }

    private void showUnsavedChangeDialog(DialogInterface.OnClickListener discardClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog);
        builder.setPositiveButton(R.string.discard, discardClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null){
                    dialog.dismiss();
                }
            }
        });

        AlertDialog aletDialog = builder.create();
        aletDialog.show();
    }

    private void showDeletionDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deletePet();
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null){
                    dialog.dismiss();
                }
            }
        });

        AlertDialog aletDialog = builder.create();
        aletDialog.show();
    }

    private void deletePet() {
        int result = getContentResolver().delete(mCurrentUri, null, null);
        if (result > 0){
            Toast.makeText(this, getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
            finish();
        }
        else{
            Toast.makeText(this, getString(R.string.delete_failed), Toast.LENGTH_SHORT).show();
        }
    }
}
