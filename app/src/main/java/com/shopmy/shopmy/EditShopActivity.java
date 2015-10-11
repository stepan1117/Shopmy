package com.shopmy.shopmy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.shopmy.shopmy.adapter.NothingSelectedSpinnerAdapter;
import com.shopmy.shopmy.exception.TimeSpanParseException;
import com.shopmy.shopmy.model.OpeningHours;
import com.shopmy.shopmy.model.ShopInfo;
import com.shopmy.shopmy.parser.OpeningHoursParser;
import com.shopmy.shopmy.validation.TextValidator;

import java.util.ArrayList;
import java.util.List;

public class EditShopActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_shop);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final Button saveButton = (Button) findViewById(R.id.saveButton);
        final ImageButton buttonUseForAllOtherDays =
                (ImageButton) findViewById(R.id.buttonUseForAllOtherDays);

        final EditText shopNameEdit = (EditText) findViewById(R.id.shopNameEdit);
        final EditText shopAddressEdit = (EditText) findViewById(R.id.shopAddressEdit);

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

                ShopInfo si = buildShopInfo((LatLng)getIntent().getParcelableExtra("position"));
                if (si == null){
                    return;
                }
                returnIntent.putExtra("shopInfo", si);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

        final Button closeButton = (Button) findViewById(R.id.cancelButton);
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
                createAndShowAlertDialog();
            }
        });

        Spinner spinner = (Spinner) findViewById(R.id.shopSizeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.shop_size_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setPrompt("Select your favorite Planet!");

        spinner.setAdapter(
                new NothingSelectedSpinnerAdapter(
                        adapter,
                        R.layout.shop_size_spinner_row_nothing_selected,
                        // R.layout.contact_spinner_nothing_selected_dropdown, // Optional
                        this));


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

    private List<EditText> getAllDaysInputs(){
        List<EditText> editTextList = new ArrayList<>();
        TableLayout layout = (TableLayout)findViewById(R.id.openingHoursTableLayout);
        for (int i = 0; i < layout.getChildCount(); i++){
            TableRow row = (TableRow)layout.getChildAt(i);
            editTextList.add((EditText) ((TextInputLayout)row.getChildAt(0)).getChildAt(0));
        }
        return editTextList;
    }

    private ShopInfo buildShopInfo(LatLng position){
        ShopInfo si = new ShopInfo();
        final EditText shopNameEdit = (EditText) findViewById(R.id.shopNameEdit);
        final EditText shopAddressEdit = (EditText) findViewById(R.id.shopAddressEdit);
        final EditText shopWebPageEdit = (EditText) findViewById(R.id.shopWebPageEdit);
        final CheckBox shopActiveCheckBox = (CheckBox) findViewById(R.id.shopActiveCheckBox);

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
        return !((TextInputLayout)et.getParent()).isErrorEnabled();
    }

    private void copyMondayToAllOthers(){
        List<EditText> editTextList = getAllDaysInputs();
        String monday = editTextList.get(0).getText().toString();
        for (EditText et : editTextList){
            et.setText(monday);
        }
    }

    private void createAndShowAlertDialog() {
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



}
