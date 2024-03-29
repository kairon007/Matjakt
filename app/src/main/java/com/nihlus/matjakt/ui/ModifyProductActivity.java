package com.nihlus.matjakt.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.nihlus.matjakt.MainActivity;
import com.nihlus.matjakt.constants.Constants;
import com.nihlus.matjakt.database.containers.EAN;
import com.nihlus.matjakt.outpan.OutpanAPI2;
import com.nihlus.matjakt.outpan.OutpanProduct;
import com.nihlus.matjakt.R;

import java.util.HashMap;

public class ModifyProductActivity extends AppCompatActivity
{
    private Bundle productData = new Bundle();
    private EAN ProductEAN;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_product);

        Intent intent = getIntent();

        int incomingIntent = intent.getIntExtra(Constants.GENERIC_INTENT_ID, -1);

        boolean bIsNewProduct = incomingIntent == Constants.INSERT_NEW_PRODUCT;
        boolean bIsModifyingProduct = incomingIntent == Constants.MODIFY_EXISTING_PRODUCT;

        if (bIsNewProduct)
        {
            setTitle(getResources().getString(R.string.title_activity_new_product));

            this.ProductEAN = intent.getParcelableExtra(Constants.PRODUCT_EAN);
        }
        else if (bIsModifyingProduct)
        {
            setTitle(getResources().getString(R.string.title_activity_edit_product));
            setVisibleProduct(intent.getBundleExtra(Constants.PRODUCT_BUNDLE));
        }
        else
        {
            setTitle(getResources().getString(R.string.debug_howDidYouGetHere));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_product, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent parentActivity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings({"unused", "UnusedParameters"})
    public void onClickOKButton(View view)
    {
        //verify data
        if (haveFieldsChanged())
        {
            // If we've altered any data, send it to the server
            if (areRequiredFieldsFilledOut())
            {
                EditText brandName = (EditText) findViewById(R.id.brandEdit);
                EditText productTitle = (EditText) findViewById(R.id.productNameEdit);
                EditText productAmount = (EditText) findViewById(R.id.productAmountEdit);

                Spinner amountSpinner = (Spinner) findViewById(R.id.amountSpinner);

                CheckBox isOrganic = (CheckBox)findViewById(R.id.isOrganicCheckbox);
                CheckBox isFairtrade = (CheckBox)findViewById(R.id.isFairtradeCheckbox);

                //bundle up the data needed by the server
                Bundle newProductData = new Bundle();
                newProductData.putParcelable(Constants.PRODUCT_EAN, ProductEAN);
                newProductData.putString(Constants.PRODUCT_BRAND_ATTRIBUTE, brandName.getText().toString());
                newProductData.putString(Constants.PRODUCT_TITLE_ATTRIBUTE, productTitle.getText().toString());

                newProductData.putString(Constants.PRODUCT_AMOUNT_ATTRIBUTE, productAmount.getText().toString()
                        + amountSpinner.getSelectedItem().toString());


                // Only include the checkboxes if they have been checked
                if (isOrganic.isChecked())
                {
                    newProductData.putBoolean(Constants.PRODUCT_ORGANIC_ATTRIBUTE, isOrganic.isChecked());
                }

                if (isFairtrade.isChecked())
                {
                    newProductData.putBoolean(Constants.PRODUCT_FAIRTRADE_ATTRIBUTE, isFairtrade.isChecked());
                }

                //create the task and send it to the server, close the modify activity when done
                UpdateOutpanProduct update = new UpdateOutpanProduct(this, getParent() ,newProductData);
                update.execute(getFinalProductString());
            }
        }
        else
        {
            // We didn't change anything, so we canceled the edit
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public void onBackPressed()
    {
        if (haveFieldsChanged())
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle(getResources().getString(R.string.dialog_confirm));
            builder.setMessage(getResources().getString(R.string.dialog_productDataChanged));

            builder.setPositiveButton(getResources().getString(R.string.dialog_Yes), new DialogInterface.OnClickListener()
            {

                public void onClick(DialogInterface dialog, int which)
                {
                    // Do nothing but close the dialog
                    dialog.dismiss();
                    setResult(RESULT_CANCELED);
                    ModifyProductActivity.this.finish();
                }

            });

            builder.setNegativeButton(getResources().getString(R.string.dialog_No), new DialogInterface.OnClickListener()
            {

                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    // Do nothing
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }
        else
        {
            setResult(RESULT_CANCELED);
            super.onBackPressed();
        }
    }

    public void setVisibleProduct(Bundle InProductData)
    {
        // Replace the member product data
        this.productData = InProductData;

        // Load the data again
        this.ProductEAN = productData.getParcelable(Constants.PRODUCT_EAN);

        EditText brandNameEdit = (EditText) findViewById(R.id.brandEdit);
        EditText productTitleEdit = (EditText) findViewById(R.id.productNameEdit);
        EditText productAmountEdit = (EditText) findViewById(R.id.productAmountEdit);

        Spinner productAmountSpinner = (Spinner) findViewById(R.id.amountSpinner);

        CheckBox isOrganic = (CheckBox)findViewById(R.id.isOrganicCheckbox);
        CheckBox isFairtrade = (CheckBox)findViewById(R.id.isFairtradeCheckbox);

        //load input data from the product data
        // Brand
        brandNameEdit.setText(productData.getString(Constants.PRODUCT_BRAND_ATTRIBUTE));

        // Name
        productTitleEdit.setText(productData.getString(Constants.PRODUCT_TITLE_ATTRIBUTE));

        // Amount
        if (productData.containsKey(Constants.PRODUCT_AMOUNT_ATTRIBUTE))
        {
            HashMap<String, String> amountValues = splitAmountValue(productData.getString(Constants.PRODUCT_AMOUNT_ATTRIBUTE));

            productAmountEdit.setText(amountValues.get(Constants.SPLITMAP_NUMBER));
            productAmountSpinner.setSelection(getUnitIndex(amountValues.get(Constants.SPLITMAP_UNIT)));
        }

        // Is it organic?
        isOrganic.setChecked(productData.getBoolean(Constants.PRODUCT_ORGANIC_ATTRIBUTE, false));
        // Is it fairtrade?
        isFairtrade.setChecked(productData.getBoolean(Constants.PRODUCT_FAIRTRADE_ATTRIBUTE, false));
    }

    private HashMap<String, String> splitAmountValue(String inString)
    {
        HashMap<String, String> list = new HashMap<>();

        String tempNumbers  = "";
        String tempChars = "";

        if (inString != null)
        {
            for (int i = 0; i < inString.length(); ++i)
            {
                char c = inString.charAt(i);
                if (Character.isDigit(c))
                {
                    tempNumbers += c;
                }
                else
                {
                    tempChars += c;
                }
            }
        }

        list.put(Constants.SPLITMAP_UNIT, tempChars);
        list.put(Constants.SPLITMAP_NUMBER, tempNumbers);

        return list;
    }

    private boolean areRequiredFieldsFilledOut()
    {
        boolean bHasIncompleteFields = false;

        EditText productBrand = (EditText) findViewById(R.id.brandEdit);
        EditText productTitle = (EditText) findViewById(R.id.productNameEdit);
        EditText productAmount = (EditText) findViewById(R.id.productAmountEdit);

        if (productBrand.getText().toString().isEmpty())
        {
            productBrand.setError(getResources().getString(R.string.prompt_fillOutField));
            bHasIncompleteFields = true;
        }

        if (productTitle.getText().toString().isEmpty())
        {
            productTitle.setError(getResources().getString(R.string.prompt_fillOutField));
            bHasIncompleteFields = true;
        }

        boolean isProductAmountNotFilledOut = productAmount.getText().toString().isEmpty();

        if (isProductAmountNotFilledOut)
        {
            productAmount.setError(getResources().getString(R.string.prompt_fillOutField));
            bHasIncompleteFields = true;
        }

        return !bHasIncompleteFields;
    }

    private boolean haveFieldsChanged()
    {
        boolean bFieldsHaveChanged = false;

        EditText productBrandEdit = (EditText) findViewById(R.id.brandEdit);
        EditText productTitleEdit = (EditText) findViewById(R.id.productNameEdit);
        EditText productAmountEdit = (EditText) findViewById(R.id.productAmountEdit);
        Spinner productAmountSpinner = (Spinner) findViewById(R.id.amountSpinner);

        if (!productBrandEdit.getText().toString().equals(productData.getString(Constants.PRODUCT_BRAND_ATTRIBUTE)))
        {
            bFieldsHaveChanged = true;
        }

        if (!productTitleEdit.getText().toString().equals(productData.getString(Constants.PRODUCT_TITLE_ATTRIBUTE)))
        {
            bFieldsHaveChanged = true;
        }

        String productAmount = productAmountEdit.getText().toString() + productAmountSpinner.getSelectedItem().toString();
        if (!productAmount.equals(productData.getString(Constants.PRODUCT_AMOUNT_ATTRIBUTE)))
        {
            bFieldsHaveChanged = true;
        }

        HashMap<String, String> amountMap = splitAmountValue(productData.getString(Constants.PRODUCT_AMOUNT_ATTRIBUTE));
        if (amountMap.size() > 0)
        {
            int indexOfAmountUnit = getUnitIndex(amountMap.get(Constants.SPLITMAP_UNIT));
            if ((productAmountSpinner.getSelectedItemPosition() != indexOfAmountUnit))
            {
                bFieldsHaveChanged = true;
            }
        }



        return bFieldsHaveChanged;
    }

    private int getUnitIndex(String InUnit)
    {
        if (!InUnit.isEmpty())
        {
            String[] unitTypes = getResources().getStringArray(R.array.ui_amount_types);

            int i = 0;
            for (String unitType : unitTypes)
            {
                if (unitType.equals(InUnit))
                {
                    return i;
                }
                ++i;
            }
        }

        return 0;
    }

    private String getFinalProductString()
    {
        EditText manufacturerName = (EditText) findViewById(R.id.brandEdit);
        EditText productTitle = (EditText) findViewById(R.id.productNameEdit);

        EditText productAmount = (EditText) findViewById(R.id.productAmountEdit);
        Spinner productAmountSpinner = (Spinner) findViewById(R.id.amountSpinner);

        return manufacturerName.getText().toString() + " " +
                productTitle.getText().toString() + " " +
                productAmount.getText().toString() +
                productAmountSpinner.getSelectedItem().toString();
    }

    //async posting of the update to the database
    class UpdateOutpanProduct extends AsyncTask<String, Void, OutpanProduct>
    {
        private final Activity modifyProductActivity;
        private final Activity parentActivity;
        private final Bundle ProductData;

        private final ProgressDialog dialog;

        UpdateOutpanProduct(Activity inModifyProductActivity, Activity inParentActivity, Bundle InProductData)
        {
            this.modifyProductActivity = inModifyProductActivity;
            this.parentActivity = inParentActivity;
            this.ProductData = InProductData;

            dialog = new ProgressDialog(inModifyProductActivity);
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            this.dialog.setMessage(getResources().getString(R.string.dialog_updatingProductInfo));
            this.dialog.show();
        }

        protected OutpanProduct doInBackground(String... inputs)
        {
            OutpanAPI2 api = new OutpanAPI2(Constants.OutpanAPIKey);
            api.setProductName((EAN)ProductData.getParcelable(Constants.PRODUCT_EAN), inputs[0]);

            api.setProductAttribute((EAN)ProductData.getParcelable(Constants.PRODUCT_EAN),
                    Constants.PRODUCT_BRAND_ATTRIBUTE,
                    ProductData.getString(Constants.PRODUCT_BRAND_ATTRIBUTE));

            api.setProductAttribute((EAN)ProductData.getParcelable(Constants.PRODUCT_EAN),
                    Constants.PRODUCT_TITLE_ATTRIBUTE,
                    ProductData.getString(Constants.PRODUCT_TITLE_ATTRIBUTE));


            if (ProductData.containsKey(Constants.PRODUCT_AMOUNT_ATTRIBUTE))
            {
                boolean isEmpty = splitAmountValue(ProductData.getString(Constants.PRODUCT_AMOUNT_ATTRIBUTE))
                        .get(Constants.SPLITMAP_NUMBER).isEmpty();

                if (!isEmpty)
                {
                    api.setProductAttribute((EAN)ProductData.getParcelable(Constants.PRODUCT_EAN),
                            Constants.PRODUCT_AMOUNT_ATTRIBUTE,
                            ProductData.getString(Constants.PRODUCT_AMOUNT_ATTRIBUTE));
                }
            }

            if (ProductData.containsKey(Constants.PRODUCT_ORGANIC_ATTRIBUTE))
            {
                api.setProductAttribute((EAN)ProductData.getParcelable(Constants.PRODUCT_EAN),
                        Constants.PRODUCT_ORGANIC_ATTRIBUTE,
                        String.valueOf(ProductData.getBoolean(Constants.PRODUCT_ORGANIC_ATTRIBUTE)));
            }

            if (ProductData.containsKey(Constants.PRODUCT_FAIRTRADE_ATTRIBUTE))
            {
                api.setProductAttribute((EAN)ProductData.getParcelable(Constants.PRODUCT_EAN),
                        Constants.PRODUCT_FAIRTRADE_ATTRIBUTE,
                        String.valueOf(ProductData.getBoolean(Constants.PRODUCT_FAIRTRADE_ATTRIBUTE)));
            }

            return api.getProduct((EAN)ProductData.getParcelable(Constants.PRODUCT_EAN));
        }

        // TODO: Update the values in the existing ViewProductActivity instead of loading a new one
        protected void onPostExecute(OutpanProduct result)
        {
            super.onPostExecute(result);
            if (this.dialog.isShowing() && result != null)
            {
                this.dialog.dismiss();

                Intent resultIntent = new Intent();
                resultIntent.putExtra(Constants.PRODUCT_BUNDLE, result.getBundle());

                modifyProductActivity.setResult(RESULT_OK, resultIntent);
                modifyProductActivity.finish();

                /*
                if (parentActivity instanceof MainActivity)
                {
                    Intent viewProductIntent = new Intent (parentActivity, ViewProductActivity.class);
                    viewProductIntent.putExtra(Constants.PRODUCT_BUNDLE, result.getBundle());
                    parentActivity.startActivity(viewProductIntent);

                    modifyProductActivity.setResult(RESULT_OK);
                    modifyProductActivity.finish();
                }
                else
                {

                }
                */
            }
            else
            {
                modifyProductActivity.setResult(RESULT_CANCELED);
                this.dialog.dismiss();

                Toast.makeText(modifyProductActivity, getResources().getString(R.string.prompt_productUpdateFailed), Toast.LENGTH_LONG).show();
            }
        }

    }
}
