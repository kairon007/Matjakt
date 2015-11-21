package com.nihlus.matjakt.Containers;

import com.nihlus.matjakt.Constants.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by jarl on 11/7/15.
 */
public class MatjaktPrice
{
    public int ID;
    public String EAN;
    public double Price;
    public String Currency;
    public int StoreID;
    public MatjaktStore Store;
    public boolean Offer;
    public Date Timestamp;

    public MatjaktPrice(JSONObject InObject)
    {
        try
        {
            this.ID = InObject.getInt(Constants.API_PARAM_ID);
            this.EAN = InObject.getString(Constants.API_PARAM_EAN);
            this.Price = InObject.getDouble(Constants.API_PARAM_PRICE);
            this.Currency = InObject.getString(Constants.API_PARAM_CURRENCY);
            this.StoreID = InObject.getInt(Constants.API_PARAM_STORE);
            this.Offer = InObject.getBoolean(Constants.API_PARAM_OFFER);

            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date InDate = formatter.parse(InObject.getString(Constants.API_PARAM_TIMESTAMP));
            this.Timestamp = InDate;
        }
        catch (JSONException jex)
        {
            jex.printStackTrace();
        }
        catch (ParseException pex)
        {
            pex.printStackTrace();
        }
    }

    public HashMap<String, String> getHashMap()
    {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(Constants.PRICEMAPID_CHAIN, Store.Chain);

        //if the entry is an "Add" entry (i.e, just a plus sign), don't add the price or the comma
        if (!Store.Chain.equals("+"))
        {
            //TODO: Add city reverse geocoding here
            hashMap.put(Constants.PRICEMAPID_EXTRA, Store.Name);
            hashMap.put(Constants.PRICEMAPID_PRICE, getPriceString(Price, Currency));
        }
        else
        {
            hashMap.put(Constants.PRICEMAPID_EXTRA, "");
            hashMap.put(Constants.PRICEMAPID_PRICE, "");
        }

        hashMap.put(Constants.PRICEMAPID_LAT, String.valueOf(Store.Latitude));
        hashMap.put(Constants.PRICEMAPID_LON, String.valueOf(Store.Longitude));
        //TODO: Add city reverse geocoding here
        //hashMap.put(Constants.PRICEMAPID_LOC, Location);
        hashMap.put(Constants.PRICEMAPID_TIMESTAMP, Timestamp.toString());

        return hashMap;
    }

    private String getPriceString(double InPrice, String InCurrency)
    {
        // Check if the decimals of the price are even
        double RoundedPrice = Math.floor(InPrice);

        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.FLOOR);

        double PriceDecimals = Double.valueOf(df.format(InPrice - RoundedPrice));

        int DecimalsAsInt = (int)(PriceDecimals * 100);
        boolean shouldHaveTrailingZero = (DecimalsAsInt % 10) == 0;

        String numberString;
        if (shouldHaveTrailingZero)
        {
            numberString = String.valueOf(InPrice) + "0";
        }
        else
        {
            numberString = String.valueOf(InPrice);
        }

        numberString += " " + InCurrency;
        return numberString;
    }
}
