package com.shoppinmate.android;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.R;
import com.google.android.gms.maps.model.LatLng;
import com.shoppinmate.android.exception.TimeSpanParseException;
import com.shoppinmate.android.model.ShopInfo;
import com.shoppinmate.android.model.TimeSpan;
import com.shoppinmate.android.parser.OpeningHoursParser;
import com.shoppinmate.android.validation.TextValidator;
import com.shoppinmate.android.format.HourMinuteFormatter;

import java.util.ArrayList;
import java.util.List;

public class EditShopActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Button saveButton;
    private Button closeButton;
    private ImageButton buttonUseForAllOtherDays;
    private EditText shopNameEdit;
    private EditText shopAddressEdit;
//    private Spinner spinner;
    private EditText shopWebPageEdit;
    private CheckBox shopActiveCheckBox;
    private TableLayout openingHoursTableLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_shop);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        saveButton = (Button) findViewById(R.id.saveButton);
        buttonUseForAllOtherDays =
                (ImageButton) findViewById(R.id.buttonUseForAllOtherDays);

        shopNameEdit = (EditText) findViewById(R.id.shopNameEdit);
        shopAddressEdit = (EditText) findViewById(R.id.shopAddressEdit);
        closeButton = (Button) findViewById(R.id.cancelButton);
//        spinner = (Spinner) findViewById(R.id.shopSizeSpinner);
        shopWebPageEdit = (EditText) findViewById(R.id.shopWebPageEdit);
        shopActiveCheckBox = (CheckBox) findViewById(R.id.shopActiveCheckBox);
        openingHoursTableLayout = (TableLayout)findViewById(R.id.openingHoursTableLayout);

        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent returnIntent = new Intent();

                LatLng position;

                if (getIntent().hasExtra("shopInfo")){
                    position = ((ShopInfo)getIntent().getParcelableExtra("shopInfo")).getPosition();
                } else {
                    position = getIntent().getParcelableExtra("position");
                    toolbar.setTitle(R.string.add_new_shop);
                }

                ShopInfo si = buildShopInfo(position);
                if (si == null){
                    return;
                }
                if (getIntent().hasExtra("shopInfo")){
                    si.setId(((ShopInfo)getIntent().getParcelableExtra("shopInfo")).getId());
                }
                returnIntent.putExtra("shopInfo", si);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        buttonUseForAllOtherDays.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAndShowCopyDaysDialog();
            }
        });

//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.shop_size_array, android.R.layout.simple_spinner_item);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinner.setPrompt("Select your favorite Planet!");
//
//        spinner.setAdapter(
//                new NothingSelectedSpinnerAdapter(
//                        adapter,
//                        R.layout.shop_size_spinner_row_nothing_selected,
//                        // R.layout.contact_spinner_nothing_selected_dropdown, // Optional
//                        this));

        if (getIntent().hasExtra("shopInfo")){
            fillValuesFromShopInfo((ShopInfo)getIntent().getParcelableExtra("shopInfo"));
        }

        // Validation
        shopNameEdit.addTextChangedListener(new TextValidator(shopNameEdit) {
            @Override
            public void validate(TextView textView, String text) {
                TextInputLayout layout = (TextInputLayout) textView.getParent();
                if (TextUtils.isEmpty(text)) {
                    layout.setError(getResources().getText(R.string.error_shop_name_empty));
                } else {
                    layout.setError(null);
                    layout.setErrorEnabled(false);
                }
            }
        });

        for(EditText et : getAllDaysInputs()){
            et.addTextChangedListener(new TextValidator(et) {
                @Override
                public void validate(TextView textView, String text) {
                    TextInputLayout layout = ((TextInputLayout)textView.getParent());
                    try {
                        new OpeningHoursParser().fromString(text);
                        layout.setError(null);
                        layout.setErrorEnabled(false);
                    } catch (TimeSpanParseException e) {
                        layout.setError(e.getMessage());
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    if (getIntent().hasExtra("shopInfo")) {
            menu
                    .add(0, 0, 0, "Remove")
                    .setIcon(R.drawable.ic_delete_white_24dp)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case 0:
                createAndShowDeleteDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private List<EditText> getAllDaysInputs(){
        List<EditText> editTextList = new ArrayList<>();
        for (int i = 0; i < openingHoursTableLayout.getChildCount(); i++){
            TableRow row = (TableRow)openingHoursTableLayout.getChildAt(i);
            editTextList.add((EditText) ((TextInputLayout)row.getChildAt(0)).getChildAt(0));
        }
        return editTextList;
    }

    private void fillValuesFromShopInfo(ShopInfo shopInfo){
        shopNameEdit.setText(shopInfo.getName());
        shopAddressEdit.setText(shopInfo.getAddress());
        shopWebPageEdit.setText(shopInfo.getUrl());
        shopActiveCheckBox.setSelected(shopInfo.isActive());

        List<EditText> allDaysInputs = getAllDaysInputs();
        int index = 0;
        for (ShopInfo.DAYS day : ShopInfo.DAYS.values()) {
            List<TimeSpan> timeSpans = shopInfo.getOpeningHours().get(day.toString());
            allDaysInputs.get(index).setText(HourMinuteFormatter.formatTimeSpans(timeSpans));
            index++;
        }
    }

    private ShopInfo buildShopInfo(LatLng position){
        ShopInfo si = new ShopInfo();

        List<EditText> validations = new ArrayList<>();
        List<EditText> dayInputs = getAllDaysInputs();
        validations.add(shopNameEdit);
        validations.addAll(dayInputs);

        for (EditText et : validations){
            if (!isTextValid(et)){
                et.requestFocus();
                return null;
            }
        }

        si.setActive(shopActiveCheckBox.isChecked());

        si.setAddress(shopAddressEdit.getText().toString());
        si.setName(shopNameEdit.getText().toString());
        si.setUrl(shopWebPageEdit.getText().toString());
        si.setPosition(position);

        OpeningHoursParser parser = new OpeningHoursParser();

        int i = 0;
        for (ShopInfo.DAYS day : ShopInfo.DAYS.values()){
            try {
                si.getOpeningHours().put(day.toString(), parser.fromString(dayInputs.get(i++).getText().toString()));
            } catch (TimeSpanParseException e) {
                e.printStackTrace();
                dayInputs.get(i).requestFocus();
                return null;
            }
        }
        return si;
    }

    private boolean isTextValid(EditText et){
        et.setText(et.getText());
        return !((TextInputLayout)et.getParent()).isErrorEnabled();
    }

    private void copyMondayToAllOthers(){
        List<EditText> editTextList = getAllDaysInputs();
        String monday = editTextList.get(0).getText().toString();
        for (EditText et : editTextList){
            et.setText(monday);
        }
    }

    private void createAndShowCopyDaysDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.question_copy_opening_hours));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                copyMondayToAllOthers();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void createAndShowDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.question_delete_shop));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                Intent returnIntent = new Intent();
                ShopInfo si = getIntent().getParcelableExtra("shopInfo");
                returnIntent.putExtra("shopInfo",si);
                setResult(ShopListActivity.RESULT_DELETE,returnIntent);
                finish();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
